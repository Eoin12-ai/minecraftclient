package dev.kryptic.module.render;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.ModeSetting;

public class PlayerEspModule extends Module {
   public final ModeSetting style = this.addSetting(new ModeSetting("Style", "Highlight style", "Outline", "Outline", "Glow"));
   public final ColorSetting color = this.addSetting(new ColorSetting("Color", "Highlight color", -1));
   public final BooleanSetting tracers = this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each player", false));

   public PlayerEspModule() {
      super("Player ESP", "Highlights players through walls", Category.RENDER);
   }
}
