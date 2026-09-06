package dev.sixseven.gui.widget;

import dev.sixseven.render.anim.Animation;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.settings.SliderSetting;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import dev.sixseven.util.UiSounds;

public class SliderWidget extends SettingWidget {
   public static final float HEIGHT = 27.0F;
   private static final float BAR_HEIGHT = 5.0F;
   private static final float KNOB_RADIUS = 5.0F;
   private final SliderSetting setting;
   private final Animation fill = new Animation(90.0F, 0.0F);
   private boolean dragging;

   public SliderWidget(ThemeManager themeManager, SliderSetting sliderSetting) {
      super(themeManager, sliderSetting);
      this.setting = sliderSetting;
      this.fill.snapTo((float)sliderSetting.getNormalized());
   }

   @Override
   public float height(NVGRenderer nVGRenderer) {
      return 27.0F;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2) {
      Theme theme = this.theme();
      float f = this.y + 8.0F;
      nVGRenderer.text(this.setting.getName(), this.x, f, 12.5F, theme.textMuted());
      String text2 = this.setting.formatValue();
      nVGRenderer.text(text2, this.x + this.width - nVGRenderer.textWidth(text2, 12.5F), f, 12.5F, theme.textPrimary());
      float f6 = this.y + 27.0F - 5.0F - 5.0F;
      this.fill.setTarget((float)this.setting.getNormalized());
      float f7 = Math.clamp(this.fill.value(), 0.0F, 1.0F);
      nVGRenderer.rect(this.x, f6, this.width, 5.0F, 2.5F, Colors.withAlpha(-16777216, 0.45F));
      float f8 = Math.max(5.0F, f7 * this.width);
      nVGRenderer.rectGradient(this.x, f6, f8, 5.0F, 2.5F, theme.accent(), theme.accentBright(), false);
      float f9 = this.x + f7 * (this.width - 5.0F) + 2.5F;
      if (this.dragging) {
         nVGRenderer.circleGlow(f9, f6 + 2.5F, 5.0F, 5.0F, theme.accentHover());
      }

      nVGRenderer.circle(f9, f6 + 2.5F, 5.0F, -1);
   }

   @Override
   public boolean mouseClicked(float f, float f3, int n) {
      if (n == 0 && this.contains(f, f3) && !(f3 < this.y + 10.0F)) {
         this.dragging = true;
         this.applyMouse(f);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void mouseDragged(float f, float f3) {
      if (this.dragging) {
         this.applyMouse(f);
      }
   }

   @Override
   public void mouseReleased() {
      this.dragging = false;
   }

   private void applyMouse(float f) {
      double d = this.setting.getNormalized();
      this.setting.setNormalized((double)((f - this.x) / this.width));
      if (this.setting.getNormalized() != d) {
         UiSounds.sliderTick((float)this.setting.getNormalized());
      }
   }
}
