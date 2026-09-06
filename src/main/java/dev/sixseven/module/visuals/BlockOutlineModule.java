package dev.sixseven.module.visuals;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.ColorSetting;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.SliderSetting;
import dev.sixseven.settings.ModeSetting;

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
