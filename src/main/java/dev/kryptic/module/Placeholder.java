package dev.kryptic.module;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.Setting;

public class Placeholder extends Module {
   public Placeholder(String str, String str3, Category category, Setting<?>... temp) {
      super(str, str3, category);

      for (Setting setting : temp) {
         this.addSetting(setting);
      }
   }
}
