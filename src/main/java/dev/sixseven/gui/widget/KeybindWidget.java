package dev.sixseven.gui.widget;

import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.settings.KeybindSetting;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import dev.sixseven.util.UiSounds;

public class KeybindWidget extends SettingWidget {
   public static final float HEIGHT = 22.0F;
   private final KeybindSetting setting;
   private boolean listening;

   public KeybindWidget(ThemeManager themeManager, KeybindSetting keybindSetting) {
      super(themeManager, keybindSetting);
      this.setting = keybindSetting;
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
      String text2 = this.listening ? "..." : this.setting.keyName();
      float f7 = Math.max(30.0F, nVGRenderer.textWidth(text2, 11.5F) + 12.0F);
      float f8 = this.x + this.width - f7;
      float f9 = 16.0F;
      float f10 = f - f9 / 2.0F;
      int n = this.listening ? Colors.withAlpha(theme.accent(), 0.3F) : Colors.withAlpha(-16777216, 0.45F);
      nVGRenderer.rect(f8, f10, f7, f9, f9 / 2.0F, n);
      if (this.listening) {
         float f11 = (float)(0.5 + 0.5 * Math.sin((double)System.nanoTime() / 2.2E8));
         nVGRenderer.rectOutline(f8, f10, f7, f9, f9 / 2.0F, 1.2F, Colors.withAlpha(theme.accentBright(), 0.4F + 0.6F * f11));
      }

      nVGRenderer.text(text2, f8 + (f7 - nVGRenderer.textWidth(text2, 11.5F)) / 2.0F, f, 11.5F, this.listening ? theme.accentBright() : theme.textPrimary());
   }

   @Override
   public boolean mouseClicked(float f, float f3, int n) {
      if (this.listening) {
         if (n == 0) {
            this.listening = false;
            return this.contains(f, f3);
         } else {
            this.setting.set(Integer.valueOf(n));
            this.listening = false;
            UiSounds.keybindSet();
            return true;
         }
      } else if (n == 0 && this.contains(f, f3)) {
         this.listening = true;
         UiSounds.keybindListen();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(int n) {
      if (!this.listening) {
         return false;
      } else {
         if (n == 256) {
            this.setting.set(Integer.valueOf(-1));
         } else {
            this.setting.set(Integer.valueOf(n));
         }

         this.listening = false;
         UiSounds.keybindSet();
         return true;
      }
   }

   @Override
   public boolean isListening() {
      return this.listening;
   }
}
