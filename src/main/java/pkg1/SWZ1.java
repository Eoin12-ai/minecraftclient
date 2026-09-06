package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import net.fabricmc.loader.api.FabricLoader;
import util.InternalUtil;

public final class SWZ1 {
   private static final Path path = FabricLoader.getInstance().getConfigDir().resolve("swyzzy-client-configs.properties");
   private static final Path path2 = FabricLoader.getInstance().getConfigDir().resolve("swyzzy-client-shares");
   private static final String string = ".swzconfig";
   private static final String string2 = "SWZ1";
   private static final int intVal = 1048576;
   private final Map<String, String> map = new LinkedHashMap<>();

   public SWZ1() {
      this.run5();
   }

   public List<String> getList() {
      ArrayList var1 = new ArrayList<>(this.map.keySet());
      var1.sort(String.CASE_INSENSITIVE_ORDER);
      return var1;
   }

   public String addSetting(String var1) {
      return this.map.get(var1);
   }

   public boolean check(String var1) {
      return this.map.containsKey(var1);
   }

   public boolean check2(String var1) {
      String var2 = var1 == null ? "" : var1.trim();
      if (var2.isEmpty()) {
         return false;
      } else {
         this.map.put(var2, this.getString());
         this.run12();
         return true;
      }
   }

   public boolean check3(String var1, String var2) {
      String var3 = var1 == null ? "" : var1.trim();
      if (!var3.isEmpty() && addSetting3(var2) != null) {
         this.map.put(var3, addSetting4(var2));
         this.run12();
         return true;
      } else {
         return false;
      }
   }

   public boolean check4(String var1) {
      String var2 = this.map.get(var1);
      return var2 != null && this.check5(var2);
   }

   public void run20(String var1) {
      if (this.map.remove(var1) != null) {
         this.run12();
      }
   }

   public Path getPath() {
      return path2;
   }

   public Path pathOf(String var1, String var2) throws IOException {
      Files.createDirectories(path2);
      Path var3 = pathOf4(addSetting2(var1));
      Files.writeString(var3, var2, StandardCharsets.UTF_8);
      return var3;
   }

   public Path pathOf2(String var1) throws IOException {
      return this.pathOf(var1, this.getString());
   }

   public Path pathOf3(String var1) throws IOException {
      String var2 = this.map.get(var1);
      if (var2 == null) {
         throw new IOException("\"null\" no longer exists.");
      } else {
         return this.pathOf(var1, var2);
      }
   }

   public static String stringOf(Path var0) {
      try {
         String var1 = Files.readString(var0, StandardCharsets.UTF_8).trim();
         return addSetting3(var1) != null ? var1 : null;
      } catch (IOException var2) {
         return null;
      }
   }

   private static String addSetting2(String var0) {
      String var1 = var0.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
      if (var1.isEmpty()) {
         var1 = "config";
      }

      return var1.length() > 64 ? var1.substring(0, 64) : var1;
   }

   private static Path pathOf4(String var0) {
      Path var1 = path2.resolve("null.swzconfig");

      for (int var2 = 2; Files.exists(var1); var2++) {
         var1 = path2.resolve(var0 + " (" + var2 + ").swzconfig");
      }

      return var1;
   }

   public String getString() {
      byte[] var1 = InternalUtil.stringOf3(getList2()).getBytes(StandardCharsets.UTF_8);
      byte[] var2 = byteArrayOf(var1);
      CRC32 var3 = new CRC32();
      var3.update(var1);
      return "SWZ1:" + Base64.getUrlEncoder().withoutPadding().encodeToString(var2) + ":" + Long.toString(var3.getValue(), 36).toUpperCase(Locale.ROOT);
   }

   public boolean check5(String var1) {
      String var2 = addSetting3(var1);
      if (var2 == null) {
         return false;
      } else {
         try {
            InternalUtil.intOf(getList2(), var2);
         } catch (IOException var4) {
            SwyzzyAddon.logger.warn("Could not apply a shared config code.", var4);
            return false;
         }

         if (SwyzzyAddon.swyzzyAddon != null) {
            SwyzzyAddon.swyzzyAddon.getVal().run7();
         }

         return true;
      }
   }

   public static String addSetting3(String var0) {
      if (var0 == null) {
         return null;
      } else {
         String[] var1 = addSetting4(var0).split(":");
         if (var1.length == 3 && "SWZ1".equals(var1[0])) {
            try {
               byte[] var2 = byteArrayOf2(Base64.getUrlDecoder().decode(var1[1]));
               if (var2 == null) {
                  return null;
               } else {
                  CRC32 var3 = new CRC32();
                  var3.update(var2);
                  return !Long.toString(var3.getValue(), 36).toUpperCase(Locale.ROOT).equals(var1[2].toUpperCase(Locale.ROOT))
                     ? null
                     : new String(var2, StandardCharsets.UTF_8);
               }
            } catch (IllegalArgumentException var4) {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   private static String addSetting4(String var0) {
      return var0.trim().replaceAll("\\s+", "");
   }

   private static byte[] byteArrayOf(byte[] var0) {
      Deflater var1 = new Deflater(9);

      byte[] var4;
      try {
         var1.setInput(var0);
         var1.finish();
         ByteArrayOutputStream var2 = new ByteArrayOutputStream(Math.max(64, var0.length / 3));
         byte[] var3 = new byte[4096];

         while (!var1.finished()) {
            var2.write(var3, 0, var1.deflate(var3));
         }

         var4 = var2.toByteArray();
      } finally {
         var1.end();
      }

      return var4;
   }

   private static byte[] byteArrayOf2(byte[] var0) {
      Inflater var1 = new Inflater();

      try {
         var1.setInput(var0);
         ByteArrayOutputStream var2 = new ByteArrayOutputStream(Math.max(64, var0.length * 3));
         byte[] var11 = new byte[4096];

         while (!var1.finished()) {
            int var4 = var1.inflate(var11);
            if (var4 == 0) {
               if (var1.finished()) {
                  break;
               }

               if (var1.needsInput() || var1.needsDictionary()) {
                  return null;
               }
            }

            var2.write(var11, 0, var4);
            if (var2.size() > 1048576) {
               return null;
            }
         }

         return var2.toByteArray();
      } catch (DataFormatException var9) {
         return null;
      } finally {
         var1.end();
      }
   }

   private static List<Module> getList2() {
      return SwyzzyAddon.swyzzyAddon == null ? List.of() : SwyzzyAddon.swyzzyAddon.getVal().getList();
   }

   private void run5() {
      if (Files.exists(path)) {
         Properties var1 = new Properties();

         try (InputStream var2 = Files.newInputStream(path)) {
            var1.load(var2);
         } catch (IOException var7) {
            SwyzzyAddon.logger.warn("Could not load saved Swyzzy configs.", var7);
            return;
         }

         for (String var3 : var1.stringPropertyNames()) {
            this.map.put(var3, var1.getProperty(var3));
         }
      }
   }

   private void run12() {
      Properties var1 = new Properties();
      var1.putAll(this.map);

      try {
         Files.createDirectories(path.getParent());
         Path var2 = Files.createTempFile(path.getParent(), "swyzzy-configs-", ".tmp");

         try (OutputStream var3 = Files.newOutputStream(var2)) {
            var1.store(var3, "Swyzzy Client saved configs");
         }

         try {
            Files.move(var2, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException var7) {
            Files.move(var2, path, StandardCopyOption.REPLACE_EXISTING);
         }
      } catch (IOException var9) {
         SwyzzyAddon.logger.warn("Could not save Swyzzy configs.", var9);
      }
   }
}
