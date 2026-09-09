package dev.kryptic.module.render;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;

public class MobEspModule extends Module {
   public final ColorSetting hostile = this.addSetting(new ColorSetting("Hostile", "Hostile color", -45715));
   public final ColorSetting passive = this.addSetting(new ColorSetting("Passive", "Passive color", -12654960));
   public final BooleanSetting passiveToo = this.addSetting(new BooleanSetting("Passive Too", "Include passive mobs", false));
   public final BooleanSetting tracers = this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each mob", false));

   public MobEspModule() {
      super("MobESP", "Highlights hostile mobs", Category.RENDER);
   }
}
