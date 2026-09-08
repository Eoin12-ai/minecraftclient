package dev.sixseven.spotify;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sixseven.SixSevenClient;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;

public final class SpotifyService {
   private final Path artPath = FabricLoader.getInstance().getConfigDir().resolve("sixsevenclient_art.img");
   private volatile SpotifyState state = SpotifyState.INACTIVE;
   private Process process;
   private BufferedWriter commandWriter;
   private volatile boolean stopped;
   private int restarts;

   public SpotifyState state() {
      return this.state;
   }

   public Path artPath() {
      return this.artPath;
   }

   public void start() {
      if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
         SixSevenClient.LOGGER.info("SpotifyHUD bridge disabled (not Windows)");
      } else {
         Thread thread = new Thread(this::runBridge, "epsteinclient-spotify-bridge");
         thread.setDaemon(true);
         thread.start();
      }
   }

   public synchronized void stop() {
      this.stopped = true;
      if (this.process != null) {
         this.process.destroy();
      }
   }

   private void runBridge() {
      while (!this.stopped && this.restarts < 4) {
         try {
            Path path = this.extractScript();
            ProcessBuilder processBuilder = new ProcessBuilder(
               "powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", path.toString(), this.artPath.toString()
            );
            processBuilder.redirectErrorStream(false);
            synchronized (this) {
               if (this.stopped) {
                  return;
               }

               this.process = processBuilder.start();
               this.commandWriter = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream(), StandardCharsets.UTF_8));
            }

            String message;
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.process.getInputStream(), StandardCharsets.UTF_8))) {
               while ((message = bufferedReader.readLine()) != null) {
                  this.parseLine(message.trim());
               }
            }

            this.process.waitFor();
         } catch (Exception ex) {
            SixSevenClient.LOGGER.warn("Spotify bridge died: {}", ex.toString());
         }

         this.state = SpotifyState.INACTIVE;
         this.restarts++;
      }
   }

   private Path extractScript() throws Exception {
      Path path = FabricLoader.getInstance().getConfigDir();
      Files.createDirectories(path);
      Path path2 = path.resolve("sixsevenclient_smtc_bridge.ps1");

      try (InputStream inputStream = SpotifyService.class.getClassLoader().getResourceAsStream("assets/sixsevenclient/spotify/smtc_bridge.ps1")) {
         if (inputStream == null) {
            throw new IllegalStateException("bridge script missing from mod resources");
         }

         Files.write(path2, inputStream.readAllBytes());
      }

      return path2;
   }

   private void parseLine(String message) {
      if (!message.isEmpty() && message.startsWith("{")) {
         try {
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            if (!jsonObject.has("active") || !jsonObject.get("active").getAsBoolean()) {
               this.state = SpotifyState.INACTIVE;
               return;
            }

            this.state = new SpotifyState(
               true,
               jsonObject.get("title").getAsString(),
               jsonObject.get("artist").getAsString(),
               jsonObject.get("posMs").getAsLong(),
               jsonObject.get("durMs").getAsLong(),
               jsonObject.get("playing").getAsBoolean(),
               jsonObject.has("canSeek") && jsonObject.get("canSeek").getAsBoolean(),
               jsonObject.has("artV") ? jsonObject.get("artV").getAsInt() : 0,
               jsonObject.has("vol") ? jsonObject.get("vol").getAsInt() : -1,
               System.nanoTime()
            );
         } catch (Exception ex) {
         }
      }
   }

   private synchronized void send(String message) {
      if (this.commandWriter != null) {
         try {
            this.commandWriter.write(message);
            this.commandWriter.newLine();
            this.commandWriter.flush();
         } catch (Exception ex) {
            SixSevenClient.LOGGER.warn("Spotify bridge command failed: {}", ex.toString());
         }
      }
   }

   public void next() {
      this.send("NEXT");
   }

   public void previous() {
      this.send("PREV");
   }

   public void togglePlay() {
      this.send("PLAYPAUSE");
   }

   public void seekTo(long l) {
      this.send("SEEK " + Math.max(0L, l));
      SpotifyState spotifyState = this.state;
      if (spotifyState.active()) {
         this.state = new SpotifyState(
            true, spotifyState.title(), spotifyState.artist(), l, spotifyState.durMs(), spotifyState.playing(), spotifyState.canSeek(), spotifyState.artVersion(), spotifyState.volume(), System.nanoTime()
         );
      }
   }

   public void setVolume(int n) {
      int offset = Math.clamp((long)n, 0, 100);
      this.send("VOLUME " + offset);
      SpotifyState spotifyState = this.state;
      if (spotifyState.active()) {
         this.state = new SpotifyState(
            true, spotifyState.title(), spotifyState.artist(), spotifyState.posMs(), spotifyState.durMs(), spotifyState.playing(), spotifyState.canSeek(), spotifyState.artVersion(), offset, spotifyState.receivedNanos()
         );
      }
   }
}
