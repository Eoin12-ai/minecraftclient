package dev.sixseven.gui.widget;

import dev.sixseven.render.anim.Animation;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import dev.sixseven.util.UiSounds;

public class BooleanWidget extends SettingWidget {
   public static final float HEIGHT = 22.0F;
   private static final float BOX = 14.0F;
   private final BooleanSetting setting;
   private final Animation check = new Animation(150.0F, 0.0F);

   public BooleanWidget(ThemeManager themeManager, BooleanSetting booleanSetting) {
      super(themeManager, booleanSetting);
      this.setting = booleanSetting;
      this.check.snapTo(booleanSetting.get() ? 1.0F : 0.0F);
   }

   @Override
   public float height(NVGRenderer nVGRenderer) {
      return 22.0F;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2) {
      Theme theme = this.theme();
      float f = this.y + 11.0F;
      nVGRenderer.text(this.setting.getName(), this.x, f, 12.5F, theme.textMuted());
      this.check.setTarget(this.setting.get() ? 1.0F : 0.0F);
      float f5 = this.check.value();
      float f6 = this.x + this.width - 14.0F;
      float f7 = f - 7.0F;
      int n = Colors.lerp(Colors.withAlpha(-16777216, 0.45F), theme.accent(), f5);
      nVGRenderer.rect(f6, f7, 14.0F, 14.0F, 4.0F, n);
      if (f5 > 0.02F) {
         nVGRenderer.save();
         nVGRenderer.alpha(f5);
         nVGRenderer.checkmark(f6, f7, 14.0F, 2.0F, -1);
         nVGRenderer.restore();
      }

      if (f5 < 0.98F) {
         nVGRenderer.save();
         nVGRenderer.alpha(1.0F - f5);
         nVGRenderer.cross(f6, f7, 14.0F, 1.8F, theme.textDisabled());
         nVGRenderer.restore();
      }
   }

   @Override
   public boolean mouseClicked(float f, float f3, int n) {
      if (n == 0 && this.contains(f, f3)) {
         this.setting.toggle();
         UiSounds.checkbox(this.setting.get());
         return true;
      } else {
         return false;
      }
   }
}
