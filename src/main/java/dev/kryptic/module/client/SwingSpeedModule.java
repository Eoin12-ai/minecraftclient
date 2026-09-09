package dev.kryptic.module.client;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.SliderSetting;

public class SwingSpeedModule extends Module {
   public final SliderSetting speed = this.addSetting(new SliderSetting("Speed", "Swing speed multiplier (<1 = slower/smoother)", 0.3, 0.1, 3.0, 0.1, "x"));

   public SwingSpeedModule() {
      super("SwingSpeed", "Adjusts hand swing animation speed", Category.CLIENT);
   }

   public float multiplier() {
      return this.speed.getFloat();
   }
}
