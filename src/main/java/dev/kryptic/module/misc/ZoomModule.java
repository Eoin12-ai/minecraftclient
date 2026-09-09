package dev.kryptic.module.misc;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;

public class ZoomModule extends Module {
   private static final double EASE_SPEED = 14.0;
   public final SliderSetting factor = this.addSetting(new SliderSetting("Factor", "Zoom factor", 4.0, 2.0, 10.0, 0.5, "x"));
   public final BooleanSetting smooth = this.addSetting(new BooleanSetting("Smooth", "Smooth zoom in/out", true));
   private double current = 1.0;
   private long lastNanos = 0L;

   public ZoomModule() {
      super("Zoom", "Optical zoom on a key", Category.MISC);
   }

   public double currentFactor() {
      long l = System.nanoTime();
      double d = this.lastNanos == 0L ? 0.0 : (double)(l - this.lastNanos) / 1.0E9;
      this.lastNanos = l;
      double coord = this.isEnabled() ? (double)this.factor.getFloat() : 1.0;
      if (!this.smooth.get()) {
         this.current = coord;
         return this.current;
      } else {
         double currentScore = 1.0 - Math.exp(-14.0 * Math.max(0.0, d));
         this.current = this.current + (coord - this.current) * currentScore;
         if (Math.abs(this.current - coord) < 0.001) {
            this.current = coord;
         }

         return this.current;
      }
   }
}
