package dev.kryptic.gui.widget;

import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.settings.StringSetting;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import dev.kryptic.util.UiSounds;
import java.lang.invoke.StringConcatFactory;

public class StringWidget extends SettingWidget {
   private static final float HEIGHT = 34.0F;
   private static final float BOX_H = 18.0F;
   private static final float FONT = 12.0F;
   private final StringSetting setting;
   private boolean focused;

   public StringWidget(ThemeManager themeManager, StringSetting stringSetting) {
      super(themeManager, stringSetting);
      this.setting = stringSetting;
   }

   @Override
   public float height(NVGRenderer nVGRenderer) {
      return 34.0F;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2) {
      Theme theme = this.theme();
      nVGRenderer.text(this.setting.getName(), this.x, this.y + 8.0F, 12.0F, theme.textMuted());
      float f = this.x;
      float f7 = this.y + 14.0F;
      float f8 = this.width;
      int n = Colors.withAlpha(-16777216, 0.45F);
      nVGRenderer.rect(f, f7, f8, 18.0F, 9.0F, n);
      nVGRenderer.rectOutline(f, f7, f8, 18.0F, 9.0F, 1.1F, Colors.withAlpha(this.focused ? theme.accentBright() : theme.accent(), this.focused ? 0.9F : 0.35F));
      float f9 = f + 8.0F;
      float f10 = f7 + 9.0F;
      String text2 = this.setting.get();
      if (text2.isEmpty() && !this.focused) {
         nVGRenderer.text(this.setting.getPlaceholder(), f9, f10, 12.0F, theme.textDisabled());
      } else {
         float f11 = nVGRenderer.text(text2, f9, f10, 12.0F, theme.textPrimary());
         if (this.focused && System.nanoTime() / 400000000L % 2L == 0L) {
            nVGRenderer.rect(f9 + f11 + 1.5F, f10 - 6.0F, 1.4F, 12.0F, 0.7F, theme.accentBright());
         }
      }
   }

   @Override
   public boolean mouseClicked(float f, float f3, int n) {
      if (n == 0 && this.contains(f, f3)) {
         this.focused = true;
         UiSounds.select();
         return true;
      } else {
         this.focused = false;
         return false;
      }
   }

   @Override
   public boolean keyPressed(int n) {
      if (!this.focused) {
         return false;
      } else {
         switch (n) {
            case 256:
            case 257:
            case 335:
               this.focused = false;
               break;
            case 259:
               String text2 = this.setting.get();
               if (!text2.isEmpty()) {
                  this.setting.set(text2.substring(0, text2.length() - 1));
               }
         }

         return true;
      }
   }

   @Override
   public boolean charTyped(int n) {
      if (!this.focused) {
         return false;
      } else if (this.setting.get().length() >= this.setting.getMaxLength()) {
         return true;
      } else if (Character.isValidCodePoint(n) && !Character.isISOControl(n)) {
         StringSetting stringSetting = this.setting;
         String text2 = this.setting.get();
         stringSetting.set(text2 + new String(Character.toChars(n)));
         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean isListening() {
      return this.focused;
   }
}
