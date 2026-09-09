package dev.kryptic.gui.widget;

import dev.kryptic.render.anim.Animation;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import dev.kryptic.util.UiSounds;

/**
 * A value on a track, with the number in a chip beside it.
 *
 * The value used to be plain text at the far right, which put it level with the
 * label and made the two compete. In a chip it reads as the thing the track is
 * setting, and the chip picks up the accent as the value rises, so a glance
 * down the column shows which sliders are wound up.
 *
 * The knob is a rounded bar rather than a circle. A circle on a 5 px track has
 * to overhang it to be visible; a bar sits in the track and stays legible when
 * the track is thin.
 */
public class SliderWidget extends SettingWidget {

   public static final float HEIGHT = 29.0F;

   private static final float TRACK_H = 4.0F;
   private static final float KNOB_W = 4.0F;
   private static final float KNOB_H = 12.0F;
   private static final float LABEL_SIZE = 12.0F;
   private static final float VALUE_SIZE = 11.0F;

   private final SliderSetting setting;
   private final Animation fill = new Animation(90.0F, 0.0F);
   private boolean dragging;

   public SliderWidget(ThemeManager themeManager, SliderSetting sliderSetting) {
      super(themeManager, sliderSetting);
      this.setting = sliderSetting;
      this.fill.snapTo((float) sliderSetting.getNormalized());
   }

   @Override
   public float height(NVGRenderer nVGRenderer) {
      return HEIGHT;
   }

   @Override
   public void render(NVGRenderer nvg, float mouseX, float mouseY) {
      Theme theme = this.theme();
      boolean hovered = this.contains(mouseX, mouseY);
      float labelY = this.y + 8.0F;

      this.fill.setTarget((float) this.setting.getNormalized());
      float t = Math.clamp(this.fill.value(), 0.0F, 1.0F);

      nvg.text(this.setting.getName(), this.x, labelY, LABEL_SIZE,
            hovered || this.dragging ? theme.textPrimary() : theme.textMuted());

      // ── the value chip ────────────────────────────────────────────────────
      String value = this.setting.formatValue();
      float textW = nvg.textWidth(value, VALUE_SIZE);
      float chipW = textW + 12.0F;
      float chipH = 14.0F;
      float chipX = this.x + this.width - chipW;
      float chipY = labelY - chipH / 2.0F;

      int chipFill = Colors.withAlpha(theme.accent(), 0.16F + 0.20F * t);
      nvg.rect(chipX, chipY, chipW, chipH, 4.0F, chipFill);
      if (this.dragging) {
         nvg.rectOutline(chipX, chipY, chipW, chipH, 4.0F, 1.0F,
               Colors.withAlpha(theme.accentBright(), 0.65F));
      }
      nvg.text(value, chipX + (chipW - textW) / 2.0F, labelY, VALUE_SIZE,
            this.dragging ? theme.accentBright() : theme.textPrimary());

      // ── the track ─────────────────────────────────────────────────────────
      float trackY = this.y + HEIGHT - 9.0F - TRACK_H / 2.0F;
      nvg.rect(this.x, trackY, this.width, TRACK_H, TRACK_H / 2.0F,
            Colors.withAlpha(0xFF000000, 0.5F));

      float filled = Math.max(TRACK_H, t * this.width);
      nvg.rectGradient(this.x, trackY, filled, TRACK_H, TRACK_H / 2.0F,
            theme.accent(), theme.accentBright(), false);

      float knobX = this.x + t * (this.width - KNOB_W);
      float knobY = trackY + TRACK_H / 2.0F - KNOB_H / 2.0F;
      if (this.dragging || hovered) {
         nvg.glow(knobX, knobY, KNOB_W, KNOB_H, KNOB_W / 2.0F, 5.0F,
               Colors.withAlpha(theme.accentHover(), this.dragging ? 0.55F : 0.30F));
      }
      nvg.rect(knobX, knobY, KNOB_W, KNOB_H, KNOB_W / 2.0F, 0xFFFFFFFF);
   }

   @Override
   public boolean mouseClicked(float mouseX, float mouseY, int button) {
      // the top strip belongs to the label and the chip, so a click there does
      // not yank the value to wherever the pointer happened to be
      if (button == 0 && this.contains(mouseX, mouseY) && mouseY >= this.y + 14.0F) {
         this.dragging = true;
         this.applyMouse(mouseX);
         return true;
      }
      return false;
   }

   @Override
   public void mouseDragged(float mouseX, float mouseY) {
      if (this.dragging) {
         this.applyMouse(mouseX);
      }
   }

   @Override
   public void mouseReleased() {
      this.dragging = false;
   }

   private void applyMouse(float mouseX) {
      double before = this.setting.getNormalized();
      this.setting.setNormalized((mouseX - this.x) / this.width);
      if (this.setting.getNormalized() != before) {
         UiSounds.sliderTick((float) this.setting.getNormalized());
      }
   }
}
