package dev.kryptic.module.misc;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.Colors;

public class CustomCrosshairModule extends Module {
   public final ModeSetting style = this.addSetting(
      new ModeSetting("Style", "Crosshair shape", "Cross", "Dot", "Cross", "Circle", "T-Shape", "Brackets", "Chevron", "Kryptic")
   );
   public final SliderSetting size = this.addSetting(new SliderSetting("Size", "Overall crosshair size", 7.0, 2.0, 24.0, 1.0, "px"));
   public final SliderSetting thickness = this.addSetting(new SliderSetting("Thickness", "Line / dot thickness", 2.0, 1.0, 6.0, 0.5, "px"));
   public final SliderSetting gap = this.addSetting(new SliderSetting("Center Gap", "Center gap for line styles", 3.0, 0.0, 12.0, 1.0, "px"));
   public final ColorSetting color = this.addSetting(new ColorSetting("Color", "Crosshair color", -678620));
   public final BooleanSetting rainbow = this.addSetting(new BooleanSetting("Rainbow", "Cycle through the rainbow", false));
   public final BooleanSetting centerDot = this.addSetting(new BooleanSetting("Center Dot", "Add a filled dot at the very center", false));
   public final BooleanSetting outline = this.addSetting(new BooleanSetting("Outline", "Dark border for contrast on any background", true));
   public final BooleanSetting glow = this.addSetting(new BooleanSetting("Glow", "Soft glow behind the crosshair", true));
   public final BooleanSetting hideVanilla = this.addSetting(new BooleanSetting("Hide Vanilla", "Hide Minecraft's default crosshair", true));

   public CustomCrosshairModule() {
      super("Custom Crosshair", "Draws a custom crosshair", Category.MISC);
   }

   public boolean shouldHideVanilla() {
      return this.isEnabled() && this.hideVanilla.get();
   }

   private int resolveColor() {
      if (this.rainbow.get()) {
         float f = (float)(System.nanoTime() % 3000000000L) / 3.0E9F;
         return 0xFF000000 | Colors.hsvToRgb(f, 0.85F, 1.0F) & 16777215;
      } else {
         return this.color.get();
      }
   }

   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2) {
      int n = this.resolveColor();
      int offset = this.outline.get() ? -1342177280 : 0;
      float f = this.thickness.getFloat();
      float f4 = this.size.getFloat();
      float f5 = this.gap.getFloat();
      if (this.glow.get()) {
         nVGRenderer.circleGlow(tickDelta, tickDelta2, f4 + 2.0F, 6.0F, Colors.withAlpha(n, 0.45F));
      }

      String text2 = this.style.get();
      switch (text2) {
         case "Dot":
            this.dot(nVGRenderer, tickDelta, tickDelta2, Math.max(1.5F, f4 * 0.35F), n, offset);
            break;
         case "Cross":
            this.cross(nVGRenderer, tickDelta, tickDelta2, f4, f, f5, n, offset, true, true);
            break;
         case "T-Shape":
            this.cross(nVGRenderer, tickDelta, tickDelta2, f4, f, f5, n, offset, false, true);
            break;
         case "Circle":
            this.ring(nVGRenderer, tickDelta, tickDelta2, f4, f, n, offset);
            break;
         case "Brackets":
            this.brackets(nVGRenderer, tickDelta, tickDelta2, f4, f, f5, n, offset);
            break;
         case "Chevron":
            if (offset != 0) {
               nVGRenderer.chevron(tickDelta, tickDelta2 + f4 * 0.15F, f4 + 2.0F, f + 2.0F, offset, true);
            }

            nVGRenderer.chevron(tickDelta, tickDelta2 + f4 * 0.15F, f4, f, n, true);
            break;
         case "Kryptic":
            this.logo(nVGRenderer, tickDelta, tickDelta2, f4, n);
            break;
         default:
            this.cross(nVGRenderer, tickDelta, tickDelta2, f4, f, f5, n, offset, true, true);
      }

      if (this.centerDot.get() && !this.style.is("Dot") && !this.style.is("Kryptic")) {
         this.dot(nVGRenderer, tickDelta, tickDelta2, Math.max(1.2F, f * 0.8F), n, offset);
      }
   }

   private void dot(NVGRenderer nVGRenderer, float f, float f4, float f5, int n, int offset) {
      if (offset != 0) {
         nVGRenderer.circle(f, f4, f5 + 1.0F, offset);
      }

      nVGRenderer.circle(f, f4, f5, n);
   }

   private void ring(NVGRenderer nVGRenderer, float f, float f5, float f6, float f7, int n, int offset) {
      if (offset != 0) {
         nVGRenderer.circleOutline(f, f5, f6, f7 + 2.0F, offset);
      }

      nVGRenderer.circleOutline(f, f5, f6, f7, n);
   }

   private void cross(NVGRenderer nVGRenderer, float f, float f6, float f7, float f8, float f9, int n, int offset, boolean value, boolean value2) {
      if (offset != 0) {
         this.segments(nVGRenderer, f, f6, f7, f8 + 2.0F, f9, offset, value, value2);
      }

      this.segments(nVGRenderer, f, f6, f7, f8, f9, n, value, value2);
   }

   private void segments(NVGRenderer nVGRenderer, float f, float f6, float f7, float f8, float f9, int n, boolean value, boolean value2) {
      nVGRenderer.line(f + f9, f6, f + f9 + f7, f6, f8, n);
      nVGRenderer.line(f - f9, f6, f - f9 - f7, f6, f8, n);
      if (value) {
         nVGRenderer.line(f, f6 - f9, f, f6 - f9 - f7, f8, n);
      }

      if (value2) {
         nVGRenderer.line(f, f6 + f9, f, f6 + f9 + f7, f8, n);
      }
   }

   private void brackets(NVGRenderer nVGRenderer, float f, float f8, float f9, float f10, float f11, int n, int offset) {
      float f12 = Math.max(2.0F, f9 * 0.5F);
      float f13 = f9 + f11 * 0.4F;
      if (offset != 0) {
         this.drawBrackets(nVGRenderer, f, f8, f13, f12, f10 + 2.0F, offset);
      }

      this.drawBrackets(nVGRenderer, f, f8, f13, f12, f10, n);
   }

   private void drawBrackets(NVGRenderer nVGRenderer, float f, float f6, float f7, float f8, float f9, int n) {
      nVGRenderer.line(f - f7, f6 - f7, f - f7 + f8, f6 - f7, f9, n);
      nVGRenderer.line(f - f7, f6 - f7, f - f7, f6 - f7 + f8, f9, n);
      nVGRenderer.line(f + f7, f6 - f7, f + f7 - f8, f6 - f7, f9, n);
      nVGRenderer.line(f + f7, f6 - f7, f + f7, f6 - f7 + f8, f9, n);
      nVGRenderer.line(f - f7, f6 + f7, f - f7 + f8, f6 + f7, f9, n);
      nVGRenderer.line(f - f7, f6 + f7, f - f7, f6 + f7 - f8, f9, n);
      nVGRenderer.line(f + f7, f6 + f7, f + f7 - f8, f6 + f7, f9, n);
      nVGRenderer.line(f + f7, f6 + f7, f + f7, f6 + f7 - f8, f9, n);
   }

   /**
    * The client's own mark, as a crosshair.
    *
    * This drew the literal text "67" -- the name of the client this one was
    * decompiled from -- in hot pink, in the middle of the screen, on a style
    * named after Kryptic. A K, drawn as strokes rather than set in a typeface,
    * so it reads the same whichever font is active.
    */
   private void logo(NVGRenderer nvg, float cx, float cy, float size, int colour) {
      float h = size * 2.0F;                 // full height of the letter
      float w = h * 0.62F;
      float t = Math.max(1.4F, size * 0.42F);  // stroke weight
      float left = cx - w / 2.0F;
      float top = cy - h / 2.0F;
      float bot = cy + h / 2.0F;
      int glow = Colors.withAlpha(colour, 0.45F);

      // drawn twice: a soft pass under a solid one, so it stays visible
      // against both a bright sky and a dark cave
      for (int pass = 0; pass < 2; pass++) {
         int c = pass == 0 ? glow : colour;
         float weight = pass == 0 ? t * 2.2F : t;
         nvg.line(left, top, left, bot, weight, c);                    // stem
         nvg.line(left + t * 0.6F, cy, left + w, top, weight, c);      // upper arm
         nvg.line(left + t * 0.6F, cy, left + w, bot, weight, c);      // lower leg
      }
   }
}
