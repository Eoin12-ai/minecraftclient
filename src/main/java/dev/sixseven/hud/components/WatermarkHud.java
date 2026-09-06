package dev.sixseven.hud.components;

import dev.sixseven.hud.HudComponent;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import java.util.function.BooleanSupplier;

public class WatermarkHud extends HudComponent {
   private static final float HEIGHT = 30.0F;
   private static final float PAD = 13.0F;
   private static final float LOGO_SIZE = 18.0F;
   private static final float TEXT_SIZE = 14.0F;
   private final ThemeManager themes;

   public WatermarkHud(ThemeManager themeManager, BooleanSupplier booleanSupplier) {
      super("watermark", 0.006F, 0.01F, booleanSupplier);
      this.themes = themeManager;
   }

   @Override
   public float measureWidth(NVGRenderer nVGRenderer) {
      return 13.0F + nVGRenderer.textWidth("67", 18.0F) + 7.0F + nVGRenderer.textWidth("client", 14.0F) + 13.0F;
   }

   @Override
   public float measureHeight(NVGRenderer nVGRenderer) {
      return 30.0F;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      Theme theme = this.themes.current();
      float f = tickDelta2 + tickDelta4 / 2.0F;
      float f5 = (float)(0.5 + 0.5 * Math.sin((double)System.nanoTime() / 8.0E8));
      float f6 = (float)(0.5 + 0.5 * Math.sin((double)System.nanoTime() / 4.8E8));
      int n = Colors.lerp(theme.accentBright(), theme.accent(), f6);
      int offset = Colors.lerp(theme.accent(), theme.accentBright(), f6);
      nVGRenderer.glow(tickDelta, tickDelta2, tickDelta3, tickDelta4, tickDelta4 / 2.0F, 8.0F, Colors.withAlpha(theme.accent(), 0.1F + 0.1F * f5));
      nVGRenderer.rectGradient(tickDelta, tickDelta2, tickDelta3, tickDelta4, tickDelta4 / 2.0F, Colors.withAlpha(-15264995, 0.88F), Colors.withAlpha(-15856878, 0.88F), true);
      nVGRenderer.rectOutline(tickDelta, tickDelta2, tickDelta3, tickDelta4, tickDelta4 / 2.0F, 1.0F, Colors.withAlpha(Colors.lerp(theme.accent(), theme.accentBright(), f5), 0.55F));
      float f7 = tickDelta + 13.0F;
      nVGRenderer.textGlow("67", f7, f, 18.0F, Colors.withAlpha(theme.accent(), 0.45F + 0.3F * f5));
      nVGRenderer.textGradient("67", f7, f, 18.0F, n, offset);
      f7 += nVGRenderer.textWidth("67", 18.0F) + 7.0F;
      nVGRenderer.circle(f7 - 4.5F, f, 1.4F, Colors.withAlpha(theme.textMuted(), 0.8F));
      nVGRenderer.text("client", f7, f, 14.0F, Colors.withAlpha(-856073, 0.92F));
   }
}
