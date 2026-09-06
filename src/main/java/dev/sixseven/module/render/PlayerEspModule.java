package dev.sixseven.module.render;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.ColorSetting;
import dev.sixseven.settings.ModeSetting;

public class PlayerEspModule extends Module {
   public final ModeSetting style = this.addSetting(new ModeSetting("Style", "Highlight style", "Outline", "1C0", "Outline", "Glow"));
   public final ColorSetting color = this.addSetting(new ColorSetting("Color", "Highlight color", -49508));
   public final BooleanSetting tracers = this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each player", false));

   public PlayerEspModule() {
      super("PlayerESP", "Highlights players through walls", Category.RENDER);
   }
}
