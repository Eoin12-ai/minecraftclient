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

   /**
    * Perceived brightness, 0..1, ignoring alpha.
    *
    * Rec. 709 weights rather than a flat average: the eye is far more
    * sensitive to green than to blue, and a plain mean calls a saturated blue
    * as bright as a mid grey.
    */
   /**
    * The red-to-green ramp used by health and durability bars.
    *
    * Interpolating red to green straight through RGB passes through olive mud:
    * at 50% the colour is #9A9A6C, which reads as neither danger nor safety
    * and is exactly where a health bar most needs to be legible. Going via an
    * amber midpoint keeps every value on the ramp a colour that means
    * something at a glance.
    *
    * @param fraction 0 is empty, 1 is full; anything outside is clamped
    */
   /**
    * Interpolate two colours including their alpha.
    *
    * {@link #lerp} blends the colour channels and keeps the first argument's
    * alpha, which is right for a tint but wrong for a translucent gradient --
    * a glass panel that fades from one alpha to another needs the alpha to
    * travel with it.
    */
   public static int lerpArgb(int from, int to, float t) {
      float f = Math.clamp(t, 0.0F, 1.0F);
      int a = Math.round(alpha(from) + (alpha(to) - alpha(from)) * f);
      int r = Math.round(red(from) + (red(to) - red(from)) * f);
      int g = Math.round(green(from) + (green(to) - green(from)) * f);
      int b = Math.round(blue(from) + (blue(to) - blue(from)) * f);
      return argb(a, r, g, b);
   }

   public static int healthRamp(float fraction) {
      float f = Math.clamp(fraction, 0.0F, 1.0F);
      int low = -1684147;      // #E64D4D
      int mid = -20393;        // #FFB047
      int high = -11671924;    // #4DE68C
      return f < 0.5F ? lerp(low, mid, f * 2.0F) : lerp(mid, high, (f - 0.5F) * 2.0F);
   }

   public static float luminance(int n) {
      return (0.2126F * red(n) + 0.7152F * green(n) + 0.0722F * blue(n)) / 255.0F;
   }

   /**
    * Near-black or near-white, whichever will be legible on the given colour.
    *
    * A control that draws a knob or a label in a fixed colour works until the
    * accent behind it is bright — a white knob on a white track is a switch
    * with nothing in it. Picking by brightness keeps every theme legible
    * without a per-theme table to maintain.
    */
   public static int contrastOn(int background) {
      return luminance(background) > 0.55F ? -15790321 : -1;   // #0F0F0F / white
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
