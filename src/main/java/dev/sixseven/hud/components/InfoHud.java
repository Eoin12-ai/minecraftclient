package dev.sixseven.hud.components;

import dev.sixseven.hud.HudComponent;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class InfoHud extends HudComponent {
   private static final float HEIGHT = 22.0F;
   private static final float FONT_SIZE = 13.0F;
   private static final float PAD_X = 9.0F;
   private final ThemeManager themes;
   private final String label;
   private final Supplier<String> value;

   public InfoHud(String str, ThemeManager themeManager, String str3, Supplier<String> supplier, float f, float f3, BooleanSupplier booleanSupplier) {
      super(str, f, f3, booleanSupplier);
      this.themes = themeManager;
      this.label = str3;
      this.value = supplier;
   }

   private String currentValue() {
      try {
         return this.value.get();
      } catch (Exception ex) {
         return "L";
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
      nVGRenderer.rectGradient(tickDelta, tickDelta2, tickDelta3, tickDelta4, tickDelta4 / 2.0F, theme.background(), theme.backgroundTo(), true);
      float f3 = tickDelta + 9.0F;
      f3 += nVGRenderer.textGradient(this.label, f3, f, 13.0F, theme.accentBright(), theme.accent());
      nVGRenderer.text(this.currentValue(), f3 + 5.0F, f, 13.0F, theme.textPrimary());
   }
}
