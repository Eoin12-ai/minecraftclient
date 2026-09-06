package dev.sixseven.module;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.Setting;

public class Placeholder extends Module {
   public Placeholder(String str, String str3, Category category, Setting<?>... temp) {
      super(str, str3, category);

      for (Setting setting : temp) {
         this.addSetting(setting);
      }
   }
}
