package dev.kryptic.hud.components;

import dev.kryptic.hud.HudComponent;
import dev.kryptic.hud.HudSurface;
import dev.kryptic.render.nanovg.NVGImages;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import net.minecraft.util.Identifier;

import java.util.function.BooleanSupplier;

/**
 * The client mark, in the corner.
 *
 * It was the word "Kryptic" set in whatever font was active, with a pulsing
 * glow behind it, a gradient across it and a breathing outline around the
 * plate — four animations on a static label. A mark that moves constantly stops
 * reading as a mark and starts reading as an effect, and this one sits on top
 * of the game for the whole session.
 *
 * The mark is drawn as the artwork now, still, on the same card as every other
 * HUD element.
 */
public class WatermarkHud extends HudComponent {

   private static final Identifier LOGO =
         Identifier.of("krypticclient", "textures/logo.png");

   /** The mark's own proportions, so it is never stretched. */
   private static final float ASPECT = 319.0f / 512.0f;

   private static final float HEIGHT = 30.0F;
   private static final float PAD = 12.0F;
   private static final float MARK_H = 15.0F;
   private static final float TEXT_SIZE = 15.0F;

   private final ThemeManager themes;

   public WatermarkHud(ThemeManager themeManager, BooleanSupplier visible) {
      super("watermark", 0.006F, 0.01F, visible);
      this.themes = themeManager;
   }

   private static float markWidth() {
      return MARK_H / ASPECT;
   }

   @Override
   public float measureWidth(NVGRenderer nvg) {
      int mark = NVGImages.fromResource(LOGO);
      float inner = mark > 0 ? markWidth() : nvg.textWidth("KRYPTIC", TEXT_SIZE);
      return PAD + inner + PAD;
   }

   @Override
   public float measureHeight(NVGRenderer nvg) {
      return HEIGHT;
   }

   @Override
   public void render(NVGRenderer nvg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      HudSurface.panel(nvg, x, y, w, h, h / 2.0F, theme);

      float midY = y + h / 2.0F;
      int mark = NVGImages.fromResource(LOGO);
      if (mark > 0) {
         float mw = markWidth();
         nvg.image(mark, x + (w - mw) / 2.0F, midY - MARK_H / 2.0F, mw, MARK_H, -1);
         return;
      }

      // the artwork is an asset; if it will not load the name still has to
      // appear rather than leaving an empty pill on screen
      float tw = nvg.textWidth("KRYPTIC", TEXT_SIZE);
      nvg.text("KRYPTIC", x + (w - tw) / 2.0F, midY, TEXT_SIZE,
            Colors.withAlpha(theme.textPrimary(), 0.95F));
   }
}
