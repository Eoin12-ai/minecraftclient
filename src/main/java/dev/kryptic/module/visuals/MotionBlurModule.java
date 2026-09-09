package dev.kryptic.module.visuals;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.render.MotionBlurRenderer;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;

public class MotionBlurModule extends Module {
   public final SliderSetting strength = this.addSetting(new SliderSetting("Rusdofui", "Blur strength", 30.0, 5.0, 100.0, 5.0, "V").withLabel(arg -> {
      int n = (int)Math.round(arg);
      String text = n <= 20 ? "Subtle" : (n <= 45 ? "Balanced" : (n <= 70 ? "Smooth" : (n <= 90 ? "Heavy" : "Cinematic")));
      return n + "% · " + text;
   }));
   public final BooleanSetting pinkTrails = this.addSetting(new BooleanSetting("Pink Trails", "Tint the trails with your theme accent (67 look)", true));
   public final SliderSetting tint = this.addSetting(new SliderSetting("Tint", "How strongly trails take the theme color", 30.0, 0.0, 100.0, 5.0, "V"));
   public final BooleanSetting fpsCompensated = this.addSetting(new BooleanSetting("FPS Compensated", "Keep the blur consistent across framerates", true));

   public MotionBlurModule() {
      super("MotionBlur", "Cinematic motion blur", Category.VISUALS);
      SliderSetting sliderSetting = this.tint;
      BooleanSetting booleanSetting = this.pinkTrails;
      sliderSetting.visibleWhen(booleanSetting::get);
   }

   @Override
   protected void onDisable() {
      MotionBlurRenderer.reset();
   }

   public double retention() {
      return Math.clamp((double)this.strength.getFloat() / 100.0, 0.05, 0.95);
   }

   public float tintAmount() {
      return this.pinkTrails.get() ? (float)Math.clamp((double)this.tint.getFloat() / 100.0, 0.0, 1.0) : 0.0F;
   }

   public int accentColor() {
      return KrypticClient.themes().current().accent();
   }
}
