package dev.sixseven.gui.widget;

import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.settings.ModeSetting;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.UiSounds;
import java.util.ArrayList;
import java.util.List;

public class ModeWidget extends SettingWidget {
   private static final float LINE_HEIGHT = 16.0F;
   private static final float FONT_SIZE = 12.5F;
   private static final float GAP = 10.0F;
   private final ModeSetting setting;
   private final List<float[]> optionBounds = new ArrayList<>();
   private int lines = 1;

   public ModeWidget(ThemeManager themeManager, ModeSetting modeSetting) {
      super(themeManager, modeSetting);
      this.setting = modeSetting;
   }

   @Override
   public float height(NVGRenderer nVGRenderer) {
      return 17.0F + (float)this.lines * 16.0F + 3.0F;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2) {
      Theme theme = this.theme();
      nVGRenderer.text(this.setting.getName(), this.x, this.y + 8.0F, 12.5F, theme.textMuted());
      this.optionBounds.clear();
      float f = this.x;
      float f4 = this.y + 17.0F + 8.0F;
      this.lines = 1;

      for (String item : this.setting.getModes()) {
         float f5 = nVGRenderer.textWidth(item, 12.5F);
         if (f + f5 > this.x + this.width && f > this.x) {
            f = this.x;
            f4 += 16.0F;
            this.lines++;
         }

         boolean ok = this.setting.is(item);
         boolean ok2 = tickDelta >= f && tickDelta <= f + f5 && tickDelta2 >= f4 - 8.0F && tickDelta2 <= f4 + 8.0F;
         if (ok) {
            nVGRenderer.textGradient(item, f, f4, 12.5F, theme.accentBright(), theme.accent());
         } else {
            nVGRenderer.text(item, f, f4, 12.5F, ok2 ? theme.textPrimary() : theme.textDisabled());
         }

         this.optionBounds.add(new float[]{f, f4 - 8.0F, f5});
         f += f5 + 10.0F;
      }
   }

   @Override
   public boolean mouseClicked(float f, float f4, int n) {
      if (n != 0) {
         return false;
      } else {
         List list = this.setting.getModes();

         for (int offset = 0; offset < this.optionBounds.size() && offset < list.size(); offset++) {
            float[] f5 = this.optionBounds.get(offset);
            if (f >= f5[0] && f <= f5[0] + f5[2] && f4 >= f5[1] && f4 <= f5[1] + 16.0F) {
               this.setting.set((String)list.get(offset));
               UiSounds.select();
               return true;
            }
         }

         return false;
      }
   }
}
