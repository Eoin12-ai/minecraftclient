package dev.sixseven.module.client;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.SliderSetting;
import dev.sixseven.settings.ModeSetting;

public class ClickGuiModule extends Module {
   public final BooleanSetting blur = this.addSetting(new BooleanSetting("Blur", "Gaussian-blur the world behind the GUI", true));
   public final SliderSetting blurStrength = this.addSetting(new SliderSetting("Blur Strength", "How strong the background blur is", 6.0, 1.0, 10.0, 1.0));
   public final ModeSetting font = this.addSetting(new ModeSetting("Font", "GUI font (Xuong TTF or vanilla-style)", "Xuong", "Xuong", "Vanilla"));

   public ClickGuiModule() {
      super("ClickGUI", "The Epstein Client menu. In menus, Shift opens it too.", Category.CLIENT);
      this.getKeybind().set(Integer.valueOf(344));
   }
}
