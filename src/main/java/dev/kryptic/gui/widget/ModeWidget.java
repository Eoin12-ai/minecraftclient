package dev.kryptic.gui.widget;

import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import dev.kryptic.util.UiSounds;

import java.util.ArrayList;
import java.util.List;

/**
 * The options as chips.
 *
 * They used to be bare words separated by ten pixels of nothing, with the
 * selected one tinted. That gives no hit target — you aim at a word and hope —
 * and on a long list of options it reads as a sentence rather than a set of
 * choices. A chip has an edge, so it is obvious what is clickable, obvious
 * where one option ends and the next begins, and obvious which is selected
 * without depending on colour alone.
 */
public class ModeWidget extends SettingWidget {

   private static final float FONT_SIZE = 11.5F;
   private static final float CHIP_H = 17.0F;
   private static final float CHIP_PAD = 8.0F;
   private static final float CHIP_GAP = 4.0F;
   private static final float ROW_GAP = 4.0F;
   private static final float LABEL_H = 17.0F;

   private final ModeSetting setting;
   private final List<float[]> optionBounds = new ArrayList<>();
   private int rows = 1;

   public ModeWidget(ThemeManager themeManager, ModeSetting modeSetting) {
      super(themeManager, modeSetting);
      this.setting = modeSetting;
   }

   @Override
   public float height(NVGRenderer nVGRenderer) {
      return LABEL_H + this.rows * CHIP_H + (this.rows - 1) * ROW_GAP + 4.0F;
   }

   @Override
   public void render(NVGRenderer nvg, float mouseX, float mouseY) {
      Theme theme = this.theme();
      nvg.text(this.setting.getName(), this.x, this.y + 8.0F, 12.0F, theme.textMuted());

      this.optionBounds.clear();
      float cx = this.x;
      float cy = this.y + LABEL_H;
      this.rows = 1;

      for (String option : this.setting.getModes()) {
         float chipW = nvg.textWidth(option, FONT_SIZE) + CHIP_PAD * 2.0F;

         // wrap before drawing, so a chip is never clipped by the panel edge
         if (cx + chipW > this.x + this.width && cx > this.x) {
            cx = this.x;
            cy += CHIP_H + ROW_GAP;
            this.rows++;
         }

         boolean selected = this.setting.is(option);
         boolean hovered = mouseX >= cx && mouseX <= cx + chipW
               && mouseY >= cy && mouseY <= cy + CHIP_H;

         if (selected) {
            nvg.rectGradient(cx, cy, chipW, CHIP_H, CHIP_H / 2.0F,
                  Colors.withAlpha(theme.accent(), 0.95F),
                  Colors.withAlpha(theme.accentBright(), 0.95F), false);
            nvg.rectOutline(cx, cy, chipW, CHIP_H, CHIP_H / 2.0F, 1.0F,
                  Colors.withAlpha(0xFFFFFFFF, 0.28F));
         } else {
            nvg.rect(cx, cy, chipW, CHIP_H, CHIP_H / 2.0F,
                  Colors.withAlpha(0xFF000000, hovered ? 0.30F : 0.42F));
            nvg.rectOutline(cx, cy, chipW, CHIP_H, CHIP_H / 2.0F, 1.0F,
                  Colors.withAlpha(theme.accent(), hovered ? 0.45F : 0.16F));
         }

         int textColour = selected ? Colors.contrastOn(theme.accent())
               : (hovered ? theme.textPrimary() : theme.textDisabled());
         nvg.text(option, cx + CHIP_PAD, cy + CHIP_H / 2.0F, FONT_SIZE, textColour);

         this.optionBounds.add(new float[]{cx, cy, chipW});
         cx += chipW + CHIP_GAP;
      }
   }

   @Override
   public boolean mouseClicked(float mouseX, float mouseY, int button) {
      if (button != 0) return false;

      List<String> modes = this.setting.getModes();
      for (int i = 0; i < this.optionBounds.size() && i < modes.size(); i++) {
         float[] bounds = this.optionBounds.get(i);
         if (mouseX >= bounds[0] && mouseX <= bounds[0] + bounds[2]
               && mouseY >= bounds[1] && mouseY <= bounds[1] + CHIP_H) {
            this.setting.set(modes.get(i));
            UiSounds.select();
            return true;
         }
      }
      return false;
   }
}
