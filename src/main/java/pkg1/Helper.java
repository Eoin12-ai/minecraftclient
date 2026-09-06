package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

public final class Helper {
   private static final Path path = FabricLoader.getInstance().getConfigDir().resolve("swyzzy-client.properties");
   private static final int intVal = Red.Purple.getInt();
   private static final int intVal2 = 72;
   private int intVal3 = intVal;
   private int intVal4 = 72;
   private final Map<FPS, Boolean> map = new EnumMap<>(FPS.class);
   private final Map<FPS, InternalHelper2> map2 = new EnumMap<>(FPS.class);

   public void run3() {
      Properties var1 = new Properties();
      if (Files.exists(path)) {
         try (InputStream var2 = Files.newInputStream(path)) {
            var1.load(var2);
         } catch (IOException var10) {
            SwyzzyAddon.logger.warn("Could not load Swyzzy Addon settings.", var10);
         }
      }

      this.intVal3 = intOf2(var1.getProperty("accent-rgb"), Red.values()[intOf(var1.getProperty("accent"), Red.values().length, 0)].getInt()) & 16777215;
      this.intVal4 = intOf2(var1.getProperty("gui-key-code"), F10.values()[intOf(var1.getProperty("gui-key"), F10.values().length, 0)].getInt());

      for (FPS var5 : FPS.values()) {
         this.map.put(var5, Boolean.parseBoolean(var1.getProperty(var5.getString() + "-visible", "true")));
         int var6 = intOf2(var1.getProperty(var5.getString() + "-x"), var5.getInt());
         int var7 = intOf2(var1.getProperty(var5.getString() + "-y"), var5.getInt2());
         this.map2.put(var5, new InternalHelper2(var6, var7));
      }
   }

   public SwyzzyClientAppearanceAndHUDSettingsModuleHelper getVal() {
      return new SwyzzyClientAppearanceAndHUDSettingsModuleHelper(this.intVal3);
   }

   public int getInt() {
      return this.intVal4;
   }

   public void run(int var1) {
      this.intVal3 = var1 & 16777215;
      this.run8();
   }

   public void run2(int var1) {
      this.intVal4 = var1;
      this.run8();
   }

   public boolean check(FPS var1) {
      return this.map.getOrDefault(var1, true);
   }

   public void run4(FPS var1) {
      this.map.put(var1, !this.check(var1));
      this.run8();
   }

   public void run5(FPS var1, boolean var2) {
      this.map.put(var1, var2);
      this.run8();
   }

   public InternalHelper2 valOf(FPS var1) {
      return this.map2.getOrDefault(var1, new InternalHelper2(var1.getInt(), var1.getInt2()));
   }

   public void run6(FPS var1, int var2, int var3) {
      this.map2.put(var1, new InternalHelper2(var2, var3));
   }

   public void run7() {
      this.run8();
   }

   private void run8() {
      Properties var1 = new Properties();
      var1.setProperty("accent-rgb", Integer.toString(this.intVal3));
      var1.setProperty("gui-key-code", Integer.toString(this.intVal4));

      for (FPS var5 : FPS.values()) {
         InternalHelper2 var6 = this.valOf(var5);
         var1.setProperty(var5.getString() + "-visible", Boolean.toString(this.check(var5)));
         var1.setProperty(var5.getString() + "-x", Integer.toString(var6.getInt()));
         var1.setProperty(var5.getString() + "-y", Integer.toString(var6.getInt2()));
      }

      try {
         Files.createDirectories(path.getParent());

         try (OutputStream var10 = Files.newOutputStream(path)) {
            var1.store(var10, "Swyzzy Addon GUI settings");
         }
      } catch (IOException var9) {
         SwyzzyAddon.logger.warn("Could not save Swyzzy Addon settings.", var9);
      }
   }

   private static int intOf(String var0, int var1, int var2) {
      if (var0 == null) {
         return var2;
      } else {
         try {
            int var3 = Integer.parseInt(var0);
            return var3 >= 0 && var3 < var1 ? var3 : var2;
         } catch (NumberFormatException var4) {
            return var2;
         }
      }
   }

   private static int intOf2(String var0, int var1) {
      if (var0 == null) {
         return var1;
      } else {
         try {
            return Integer.parseInt(var0);
         } catch (NumberFormatException var3) {
            return var1;
         }
      }
   }
}
