package pkg1;

import net.minecraft.client.MinecraftClient;

public final class MouseMixinUtil {
   private MouseMixinUtil() {
   }

   public static boolean isEnabled() {
      return FreecamModule.getVal() != null || FreelookModule.getVal() != null;
   }

   public static boolean check(double var0, double var2) {
      FreecamModule var4 = FreecamModule.getVal();
      if (var4 != null) {
         var4.run(var0, var2);
         return true;
      } else {
         FreelookModule var5 = FreelookModule.getVal();
         if (var5 != null) {
            var5.run(var0, var2);
            return true;
         } else {
            return false;
         }
      }
   }

   public static boolean check2(double var0) {
      if (var0 == 0.0) {
         return false;
      } else {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.currentScreen == null && var2.player != null) {
            FreecamModule var3 = FreecamModule.getVal();
            return var3 != null && var3.check2(var0);
         } else {
            return false;
         }
      }
   }
}
