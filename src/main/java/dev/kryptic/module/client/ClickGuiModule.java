package dev.kryptic.module.client;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.settings.ModeSetting;

public class ClickGuiModule extends Module {
   public final BooleanSetting blur = this.addSetting(new BooleanSetting("Blur", "Gaussian-blur the world behind the GUI", true));
   public final SliderSetting blurStrength = this.addSetting(new SliderSetting("Blur Strength", "How strong the background blur is", 6.0, 1.0, 10.0, 1.0));
   public final ModeSetting font = this.addSetting(new ModeSetting(
         "Font", "Typeface for the whole client — the menu and every HUD element",
         "Kryptic", "Kryptic", "Bold", "Xuong", "Vanilla", "Mono", "Ten"));

   public final ModeSetting sort = this.addSetting(new ModeSetting(
         "Sort", "Order the modules run in down each column",
         "Curated", "Curated", "A-Z", "Z-A", "Enabled First", "Registered"));

   public ClickGuiModule() {
      super("Click GUI", "The Kryptic Client menu. In menus, Shift opens it too.", Category.CLIENT);
      this.getKeybind().set(Integer.valueOf(344));
   }
}
