package util;

import com.swyzzyaddon.SwyzzyAddon;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.minecraft.client.gui.DrawContext;
import pkg1.ActivityChunkFinderModuleData;
import pkg1.ActivityChunkFinderModuleHelper;

public final class SwyzzyAddonUtils {
   private SwyzzyAddonUtils() {
   }

   public static void run(SwyzzyAddon var0) {
      run2(var0);
      run3(var0);
   }

   private static void run2(SwyzzyAddon var0) {
      try {
         Class var1 = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback");
         run4(var1, "EVENT", var1, var905 -> SwyzzyAddonUtils.run6(var0, var905));
      } catch (ReflectiveOperationException var2) {
         SwyzzyAddon.logger.error("Could not register the Swyzzy HUD renderer.", var2);
      }
   }

   private static void run3(SwyzzyAddon var0) {
      String[] var1 = new String[]{
         "net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents", "net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents"
      };
      String[] var2 = var1;
      int var3 = var1.length;
      int var4 = 0;

      while (var4 < var3) {
         String var5 = var2[var4];

         try {
            Class var6 = Class.forName(var5);
            Class var7 = Class.forName("null$AfterEntities");
            run4(var6, "AFTER_ENTITIES", var7, var905 -> SwyzzyAddonUtils.run5(var0, var905));
            return;
         } catch (ClassNotFoundException var8) {
            var4++;
         } catch (ReflectiveOperationException var9) {
            SwyzzyAddon.logger.error("Could not register the Swyzzy world renderer.", var9);
            return;
         }
      }

      SwyzzyAddon.logger.error("No compatible Fabric world rendering API was found.");
   }

   private static void run4(Class<?> var0, String var1, Class<?> var2, SwyzzyAddonUtils.Inner1 var3) throws ReflectiveOperationException {
      Field var4 = var0.getField(var1);
      Object var5 = var4.get(null);
      Object var6 = Proxy.newProxyInstance(var2.getClassLoader(), new Class[]{var2}, (var913, var914, var915) -> SwyzzyAddonUtils.objectOf(var3, var913, var914, var915));
      Method var7 = Class.forName("net.fabricmc.fabric.api.event.Event").getMethod("register", Object.class);
      var7.invoke(var5, var6);
   }

   private static Object objectOf(SwyzzyAddonUtils.Inner1 var0, Object var1, Method var2, Object[] var3) throws Throwable {
      if (var2.getDeclaringClass() != Object.class) {
         var0.call(var3 == null ? new Object[0] : var3);
         return null;
      } else {
         String var4 = var2.getName();
         switch (var4.hashCode()) {
            case -1776922004:
               if (var4.equals("toString")) {
                  return "Swyzzy callback";
               }
               break;
            case -1295482945:
               if (var4.equals("equals")) {
                  return var1 == var3[0];
               }
               break;
            case 147696667:
               if (var4.equals("hashCode")) {
                  return System.identityHashCode(var1);
               }
         }

         return null;
      }
   }

   private static void run5(SwyzzyAddon var0, Object[] var1) {
      if (var1.length != 0) {
         ActivityChunkFinderModuleHelper var2 = ActivityChunkFinderModuleHelper.valOf(var1[0]);
         if (var2 != null) {
            try {
               var0.getVal().run3(new ActivityChunkFinderModuleData(var2));
            } finally {
               var2.run3();
            }
         }
      }
   }

   private static void run6(SwyzzyAddon var0, Object[] var1) {
      if (var1.length > 0 && var1[0] instanceof DrawContext var2) {
         var0.getVal2().run5(var2);
      }
   }

   @FunctionalInterface
   interface Inner1 {
      void call(Object[] var1);
   }
}
