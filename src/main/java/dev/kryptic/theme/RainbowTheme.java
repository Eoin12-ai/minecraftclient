package dev.kryptic.theme;

import dev.kryptic.util.Colors;

public class RainbowTheme extends Theme {
   public RainbowTheme() {
      super("Rainbow", -678620, false);
   }

   @Override
   public int accent() {
      double d = (double)(System.nanoTime() % 1000000000000L) / 1.0E9;
      return Colors.hsvToRgb((float)(d * 36.0 % 360.0), 0.72F, 1.0F);
   }
}
