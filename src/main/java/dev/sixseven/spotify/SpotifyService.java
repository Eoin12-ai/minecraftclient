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
   /** True while the macOS bridge is driving state; picks the command path. */
   private volatile boolean macBridge;

   public SpotifyState state() {
      return this.state;
   }

   public Path artPath() {
      return this.artPath;
   }

   public void start() {
      String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
      Runnable bridge;
      if (os.contains("win")) {
         bridge = this::runBridge;
      } else if (os.contains("mac") || os.contains("darwin")) {
         this.macBridge = true;
         bridge = this::runMacBridge;
      } else {
         // Linux Spotify exposes MPRIS over D-Bus, which needs a dependency
         // this mod does not carry. Saying so is better than a dead panel.
         SixSevenClient.LOGGER.info("SpotifyHUD bridge unavailable on this platform ({})", os);
         return;
      }
      Thread thread = new Thread(bridge, "epsteinclient-spotify-bridge");
      thread.setDaemon(true);
      thread.start();
   }

   // ── macOS ────────────────────────────────────────────────────────────────

   /**
    * Spotify's macOS build is scriptable, so there is no bridge process to keep
    * alive — each poll is one osascript call that prints a tab-delimited line.
    *
    * Reading fields out rather than having AppleScript emit JSON avoids having
    * to escape quotes and backslashes inside track titles, which is exactly the
    * sort of thing that turns one unusual song name into a parse failure.
    */
   private static final String MAC_POLL_SCRIPT = String.join("\n",
         "if application \"Spotify\" is running then",
         "  tell application \"Spotify\"",
         // one statement, one line: AppleScript needs an explicit continuation
         // character to wrap an expression, and a bare newline is a syntax error
         "    set out to (player state as text) & tab & (name of current track) & tab & "
               + "(artist of current track) & tab & (duration of current track as text) & tab & "
               + "(player position as text) & tab & (sound volume as text)",
         "  end tell",
         "else",
         "  set out to \"stopped\"",
         "end if",
         "return out");

   private void runMacBridge() {
      while (!this.stopped) {
         try {
            String line = runOsascript(MAC_POLL_SCRIPT);
            this.state = line == null ? SpotifyState.INACTIVE : parseMacLine(line);
         } catch (Exception ex) {
            this.state = SpotifyState.INACTIVE;
         }
         try {
            Thread.sleep(1000L);
         } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return;
         }
      }
   }

   /** Runs one AppleScript and returns its first output line, or null. */
   private static String runOsascript(String script) throws Exception {
      ProcessBuilder builder = new ProcessBuilder("osascript", "-e", script);
      builder.redirectErrorStream(false);
      Process proc = builder.start();
      String first;
      try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
         first = reader.readLine();
      }
      // a hung Spotify must not wedge the bridge thread forever
      if (!proc.waitFor(5L, java.util.concurrent.TimeUnit.SECONDS)) {
         proc.destroyForcibly();
         return null;
      }
      return first;
   }

   private static SpotifyState parseMacLine(String line) {
      String[] parts = line.split("\t", -1);
      if (parts.length < 6) return SpotifyState.INACTIVE;
      String playerState = parts[0].trim();
      if (playerState.equals("stopped")) return SpotifyState.INACTIVE;

      // duration comes back in milliseconds, position in fractional seconds
      long durMs = (long) parseDouble(parts[3]);
      long posMs = (long) (parseDouble(parts[4]) * 1000.0);
      int volume = (int) parseDouble(parts[5]);

      return new SpotifyState(
            true,
            parts[1],
            parts[2],
            posMs,
            durMs,
            playerState.equals("playing"),
            true,
            0,          // artwork is a URL here, not a local file; not fetched
            volume,
            System.nanoTime());
   }

   private static double parseDouble(String raw) {
      try {
         return Double.parseDouble(raw.trim());
      } catch (NumberFormatException ex) {
         return 0.0;
      }
   }

   /** Turns the bridge's line protocol into the matching AppleScript. */
   private void sendMac(String message) {
      String script;
      if (message.equals("NEXT")) {
         script = "tell application \"Spotify\" to next track";
      } else if (message.equals("PREV")) {
         script = "tell application \"Spotify\" to previous track";
      } else if (message.equals("PLAYPAUSE")) {
         script = "tell application \"Spotify\" to playpause";
      } else if (message.startsWith("SEEK ")) {
         double seconds = parseDouble(message.substring(5)) / 1000.0;
         script = "tell application \"Spotify\" to set player position to " + seconds;
      } else if (message.startsWith("VOLUME ")) {
         int volume = (int) parseDouble(message.substring(7));
         script = "tell application \"Spotify\" to set sound volume to " + volume;
      } else {
         return;
      }
      try {
         runOsascript(script);
      } catch (Exception ex) {
         SixSevenClient.LOGGER.warn("Spotify command failed: {}", ex.toString());
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
      if (this.macBridge) {
         this.sendMac(message);
         return;
      }
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
