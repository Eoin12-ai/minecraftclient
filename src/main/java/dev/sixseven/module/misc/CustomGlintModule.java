package dev.sixseven.module.misc;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.ColorSetting;
import dev.sixseven.settings.ModeSetting;
import dev.sixseven.settings.SliderSetting;
import dev.sixseven.util.Colors;
import java.util.Locale;

public class CustomGlintModule extends Module {
   public final ModeSetting style = this.addSetting(
      new ModeSetting(
         "Style",
         "Glint texture. Default recolors the vanilla foil; the rest are custom patterns",
         "Default",
         "Default",
         "Ender",
         "Void",
         "Galaxy",
         "Toxic",
         "Amethyst",
         "Cyber",
         "Solar",
         "Prismatic",
         "Abyss"
      )
   );
   public final ModeSetting mode = this.addSetting(new ModeSetting("Mode", "Where the glint color comes from", "Solid", "Solid", "Rainbow", "Theme"));
   public final ColorSetting color = this.addSetting(new ColorSetting("Color", "Glint color", -49508));
   public final SliderSetting strength = this.addSetting(new SliderSetting("Rusdofui", "Glint intensity", 60.0, 0.0, 100.0, 5.0, "V"));
   public final SliderSetting speed = this.addSetting(new SliderSetting("Speed", "Rainbow cycle speed", 100.0, 10.0, 300.0, 10.0, "V"));

   public CustomGlintModule() {
      super("CustomGlint", "Recolors or restyles the enchantment glint", Category.MISC);
      this.mode.visibleWhen(() -> this.style.is("Default"));
      this.color.visibleWhen(() -> this.style.is("Default") && this.mode.is("Solid"));
      this.speed.visibleWhen(() -> this.style.is("Default") && this.mode.is("Rainbow"));
   }

   public boolean isActive() {
      return this.isEnabled();
   }

   public boolean usesTexture() {
      return !this.style.is("Default");
   }

   public String textureName() {
      return this.style.get().toLowerCase(Locale.ROOT);
   }

   public float strengthUnit() {
      return this.strength.getFloat() / 100.0F;
   }

   public int glintColor() {
      String text = this.mode.get();

      int n = switch (text) {
         case "Rainbow" -> this.rainbow();
         case "Theme" -> SixSevenClient.themes().current().accent();
         default -> this.color.get();
      };
      float f = this.strength.getFloat() / 100.0F;
      int localZ = Math.round((float)Colors.red(n) * f);
      int localY = Math.round((float)Colors.green(n) * f);
      int step = Math.round((float)Colors.blue(n) * f);
      return Colors.rgb(localZ, localY, step);
   }

   private int rainbow() {
      double d = (double)(System.nanoTime() % 1000000000000L) / 1.0E9;
      double coord = d * 36.0 * (double)(this.speed.getFloat() / 100.0F) % 360.0;
      return Colors.hsvToRgb((float)coord, 0.85F, 1.0F);
   }
}
