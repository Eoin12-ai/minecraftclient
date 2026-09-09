package dev.kryptic.render;

public final class BlurHook {
   private static volatile float override = -1.0F;

   private BlurHook() {
   }

   public static void set(float f) {
      override = f;
   }

   public static void clear() {
      override = -1.0F;
   }

   public static int apply(int n) {
      float f = override;
      return f < 0.0F ? n : Math.round(Math.clamp(f, 0.0F, 10.0F));
   }

   public static boolean isActive() {
      return override >= 0.0F;
   }
}
