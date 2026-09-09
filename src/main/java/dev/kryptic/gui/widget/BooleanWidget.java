package dev.kryptic.gui.widget;

import dev.kryptic.render.anim.Animation;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import dev.kryptic.util.UiSounds;

/**
 * A sliding switch.
 *
 * This was a checkbox with a tick and a cross swapped by opacity, which reads
 * as two symbols fighting over one square rather than as one control moving.
 * A switch says which way it is pointing from across the menu, and the knob
 * travelling between the ends is the animation — nothing has to fade.
 */
public class BooleanWidget extends SettingWidget {

   public static final float HEIGHT = 22.0F;

   private static final float TRACK_W = 26.0F;
   private static final float TRACK_H = 13.0F;
   private static final float KNOB_R = 4.6F;

   private final BooleanSetting setting;
   private final Animation slide = new Animation(160.0F, 0.0F);

   public BooleanWidget(ThemeManager themeManager, BooleanSetting booleanSetting) {
      super(themeManager, booleanSetting);
      this.setting = booleanSetting;
      this.slide.snapTo(booleanSetting.get() ? 1.0F : 0.0F);
   }

   @Override
   public float height(NVGRenderer nVGRenderer) {
      return HEIGHT;
   }

   @Override
   public void render(NVGRenderer nvg, float mouseX, float mouseY) {
      Theme theme = this.theme();
      float mid = this.y + HEIGHT / 2.0F;
      boolean hovered = this.contains(mouseX, mouseY);

      this.slide.setTarget(this.setting.get() ? 1.0F : 0.0F);
      float t = Math.clamp(this.slide.value(), 0.0F, 1.0F);

      // the label brightens with the switch, so a column of settings shows
      // what is on without reading a single word
      int labelColour = hovered ? theme.textPrimary()
            : Colors.lerp(theme.textDisabled(), theme.textMuted(), t);
      nvg.text(this.setting.getName(), this.x, mid, 12.0F, labelColour);

      float trackX = this.x + this.width - TRACK_W;
      float trackY = mid - TRACK_H / 2.0F;
      int track = Colors.lerp(Colors.withAlpha(0xFF000000, 0.5F),
            Colors.withAlpha(theme.accent(), 0.85F), t);
      nvg.rect(trackX, trackY, TRACK_W, TRACK_H, TRACK_H / 2.0F, track);
      nvg.rectOutline(trackX, trackY, TRACK_W, TRACK_H, TRACK_H / 2.0F, 1.0F,
            Colors.withAlpha(0xFFFFFFFF, 0.10F + 0.14F * t));

      float knobX = trackX + KNOB_R + 1.9F + t * (TRACK_W - (KNOB_R + 1.9F) * 2.0F);
      if (t > 0.05F) {
         nvg.circleGlow(knobX, mid, KNOB_R, KNOB_R * 1.9F,
               Colors.withAlpha(theme.accentBright(), 0.55F * t));
      }
      nvg.circle(knobX, mid, KNOB_R, Colors.lerp(0xFFB8B4C6, 0xFFFFFFFF, t));
   }

   @Override
   public boolean mouseClicked(float mouseX, float mouseY, int button) {
      if (button == 0 && this.contains(mouseX, mouseY)) {
         this.setting.toggle();
         UiSounds.checkbox(this.setting.get());
         return true;
      }
      return false;
   }
}
