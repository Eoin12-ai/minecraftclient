package dev.kryptic.util;

public final class Colors {
   private Colors() {
   }

   public static int argb(int n, int localZ, int localY, int step) {
      return (n & 0xFF) << 24 | (localZ & 0xFF) << 16 | (localY & 0xFF) << 8 | step & 0xFF;
   }

   public static int rgb(int n, int localX, int localZ) {
      return argb(255, n, localX, localZ);
   }

   public static int withAlpha(int n, int offset) {
      return n & 16777215 | (offset & 0xFF) << 24;
   }

   public static int withAlpha(int n, float f) {
      return withAlpha(n, (int)(Math.clamp(f, 0.0F, 1.0F) * 255.0F));
   }

   public static int alpha(int n) {
      return n >>> 24 & 0xFF;
   }

   public static int red(int n) {
      return n >> 16 & 0xFF;
   }

   public static int green(int n) {
      return n >> 8 & 0xFF;
   }

   public static int blue(int n) {
      return n & 0xFF;
   }

   public static int lerp(int n, int step, float f) {
      f = Math.clamp(f, 0.0F, 1.0F);
      int step2 = (int)((float)alpha(n) + (float)(alpha(step) - alpha(n)) * f);
      int n9 = (int)((float)red(n) + (float)(red(step) - red(n)) * f);
      int n10 = (int)((float)green(n) + (float)(green(step) - green(n)) * f);
      int n11 = (int)((float)blue(n) + (float)(blue(step) - blue(n)) * f);
      return argb(step2, n9, n10, n11);
   }

   public static int lighten(int n, float f) {
      return lerp(n, withAlpha(-1, alpha(n)), f);
   }

   public static int darken(int n, float f) {
      return lerp(n, withAlpha(-16777216, alpha(n)), f);
   }

   public static float[] rgbToHsv(int n) {
      float f = (float)red(n) / 255.0F;
      float f9 = (float)green(n) / 255.0F;
      float f10 = (float)blue(n) / 255.0F;
      float f11 = Math.max(f, Math.max(f9, f10));
      float f12 = Math.min(f, Math.min(f9, f10));
      float f13 = f11 - f12;
      float f14;
      if (f13 == 0.0F) {
         f14 = 0.0F;
      } else if (f11 == f) {
         f14 = 60.0F * ((f9 - f10) / f13 % 6.0F);
      } else if (f11 == f9) {
         f14 = 60.0F * ((f10 - f) / f13 + 2.0F);
      } else {
         f14 = 60.0F * ((f - f9) / f13 + 4.0F);
      }

      if (f14 < 0.0F) {
         f14 += 360.0F;
      }

      float f15 = f11 == 0.0F ? 0.0F : f13 / f11;
      return new float[]{f14, f15, f11};
   }

   public static int hsvToRgb(float f, float f10, float f11) {
      float f12 = f11 * f10;
      float f13 = f12 * (1.0F - Math.abs(f / 60.0F % 2.0F - 1.0F));
      float f14 = f11 - f12;
      float f15;
      float f16;
      float f17;
      if (f < 60.0F) {
         f15 = f12;
         f16 = f13;
         f17 = 0.0F;
      } else if (f < 120.0F) {
         f15 = f13;
         f16 = f12;
         f17 = 0.0F;
      } else if (f < 180.0F) {
         f15 = 0.0F;
         f16 = f12;
         f17 = f13;
      } else if (f < 240.0F) {
         f15 = 0.0F;
         f16 = f13;
         f17 = f12;
      } else if (f < 300.0F) {
         f15 = f13;
         f16 = 0.0F;
         f17 = f12;
      } else {
         f15 = f12;
         f16 = 0.0F;
         f17 = f13;
      }

      return rgb(Math.round((f15 + f14) * 255.0F), Math.round((f16 + f14) * 255.0F), Math.round((f17 + f14) * 255.0F));
   }
}
