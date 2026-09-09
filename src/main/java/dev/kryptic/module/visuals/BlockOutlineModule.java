package dev.kryptic.module.visuals;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.settings.ModeSetting;

public class BlockOutlineModule extends Module {
   public final ColorSetting color = this.addSetting(new ColorSetting("Color", "Outline color", -49508));
   public final BooleanSetting rainbow = this.addSetting(new BooleanSetting("Rainbow", "Cycle the outline through the rainbow", false));
   public final SliderSetting glow = this.addSetting(new SliderSetting("Glow", "Outer glow intensity", 60.0, 0.0, 100.0, 5.0, "V"));
   public final SliderSetting thickness = this.addSetting(new SliderSetting("Thickness", "Core line width", 2.5, 1.0, 6.0, 0.5, "px"));
   public final SliderSetting fillOpacity = this.addSetting(new SliderSetting("Fill", "Transparent fill inside the block", 12.0, 0.0, 60.0, 2.0, "V"));
   public final ModeSetting animation = this.addSetting(new ModeSetting("Animation", "Outline animation style", "Pulse", "Pulse", "Gradient Flow", "Static"));

   public BlockOutlineModule() {
      super("CustomBlockOutline", "Glowing animated outline on the targeted block", Category.VISUALS);
      this.setEnabled(true);
   }
}
