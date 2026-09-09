package dev.kryptic.gui.widget;

import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.settings.KeybindSetting;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import dev.kryptic.util.UiSounds;

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

   /**
    * The bound key, drawn as a keycap.
    *
    * A flat pill looked the same as the value chip on a slider, which made a
    * keybind read as another readout rather than as something you press. A cap
    * with a lip under it looks like a key, and while it is listening the lip
    * goes and the cap sits flush, the way a real key does when it is held down.
    */
   @Override
   public void render(NVGRenderer nvg, float mouseX, float mouseY) {
      Theme theme = this.theme();
      float mid = this.y + HEIGHT / 2.0F;
      boolean hovered = this.contains(mouseX, mouseY);

      nvg.text(this.setting.getName(), this.x, mid, 12.0F,
            hovered || this.listening ? theme.textPrimary() : theme.textMuted());

      String label = this.listening ? "press a key" : this.setting.keyName();
      float fontSize = this.listening ? 10.5F : 11.5F;
      float capW = Math.max(34.0F, nvg.textWidth(label, fontSize) + 14.0F);
      float capH = 16.0F;
      float capX = this.x + this.width - capW;
      float capY = mid - capH / 2.0F;

      if (this.listening) {
         float pulse = (float)(0.5 + 0.5 * Math.sin(System.nanoTime() / 2.2E8));
         nvg.rect(capX, capY, capW, capH, 4.5F, Colors.withAlpha(theme.accent(), 0.30F));
         nvg.rectOutline(capX, capY, capW, capH, 4.5F, 1.2F,
               Colors.withAlpha(theme.accentBright(), 0.35F + 0.55F * pulse));
      } else {
         // the lip: a sliver of darker cap peeking out below, so the face reads
         // as raised rather than printed on the background
         nvg.rect(capX, capY + 1.5F, capW, capH, 4.5F, Colors.withAlpha(0xFF000000, 0.55F));
         nvg.rect(capX, capY, capW, capH - 1.0F, 4.5F,
               Colors.withAlpha(0xFF2A2636, hovered ? 0.98F : 0.85F));
         nvg.rectOutline(capX, capY, capW, capH - 1.0F, 4.5F, 1.0F,
               Colors.withAlpha(theme.accent(), hovered ? 0.55F : 0.22F));
      }

      nvg.text(label, capX + (capW - nvg.textWidth(label, fontSize)) / 2.0F,
            this.listening ? mid : mid - 0.5F, fontSize,
            this.listening ? theme.accentBright() : theme.textPrimary());
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
