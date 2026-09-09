package dev.kryptic.hud.components;

import dev.kryptic.hud.HudComponent;
import dev.kryptic.hud.HudSurface;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class InfoHud extends HudComponent {
   private static final float HEIGHT = 22.0F;
   private static final float FONT_SIZE = 13.0F;
   private static final float PAD_X = 9.0F;
   private final ThemeManager themes;
   private final String label;
   private final Supplier<String> value;

   /**
    * Optional tint for the value, derived from the value itself. Left null the
    * number takes the theme's primary text colour like any other readout; set,
    * it says at a glance whether the number is a good one.
    */
   private ToIntFunction<String> valueColour;

   public InfoHud(String str, ThemeManager themeManager, String str3, Supplier<String> supplier, float f, float f3, BooleanSupplier booleanSupplier) {
      super(str, f, f3, booleanSupplier);
      this.themes = themeManager;
      this.label = str3;
      this.value = supplier;
   }

   /** Reads the value back to decide its tint. Returns this for chaining. */
   public InfoHud tinted(ToIntFunction<String> colour) {
      this.valueColour = colour;
      return this;
   }

   private String currentValue() {
      try {
         return this.value.get();
      } catch (Exception ex) {
         return "—";
      }
   }

   @Override
   public float measureWidth(NVGRenderer nVGRenderer) {
      return 9.0F + nVGRenderer.textWidth(this.label, 13.0F) + 5.0F + nVGRenderer.textWidth(this.currentValue(), 13.0F) + 9.0F;
   }

   @Override
   public float measureHeight(NVGRenderer nVGRenderer) {
      return 22.0F;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      Theme theme = this.themes.current();
      float f = tickDelta2 + tickDelta4 / 2.0F;
      HudSurface.pill(nVGRenderer, tickDelta, tickDelta2, tickDelta3, tickDelta4, theme);
      float f3 = tickDelta + 9.0F;
      f3 += nVGRenderer.textGradient(this.label, f3, f, 13.0F, theme.accentBright(), theme.accent());
      String shown = this.currentValue();
      int colour = theme.textPrimary();
      if (this.valueColour != null) {
         try {
            int graded = this.valueColour.applyAsInt(shown);
            // zero means "no opinion" — a fully transparent colour would
            // otherwise erase the readout rather than leave it alone
            if (graded != 0) colour = graded;
         } catch (Exception ex) {
            // a readout that cannot be graded is still a readout
         }
      }
      nVGRenderer.text(shown, f3 + 5.0F, f, 13.0F, colour);
   }
}
