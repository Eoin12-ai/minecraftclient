package dev.sixseven.module.render;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.SliderSetting;

public class FullbrightModule extends Module {
   public final SliderSetting gamma = this.addSetting(new SliderSetting("Gamma", "Brightness boost", 12.0, 1.0, 15.0, 1.0));

   public FullbrightModule() {
      super("FullBright", "Maximum brightness everywhere", Category.RENDER);
   }
}
