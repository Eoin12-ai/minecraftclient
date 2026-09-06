package dev.sixseven.gui.widget;

import dev.sixseven.render.anim.Animation;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.settings.ColorSetting;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import dev.sixseven.util.UiSounds;
import java.lang.invoke.StringConcatFactory;

public class ColorWidget extends SettingWidget {
   private static final float ROW = 22.0F;
   private static final float SV_H = 74.0F;
   private static final float HUE_H = 10.0F;
   private static final float BOTTOM_H = 22.0F;
   private static final float GAP = 6.0F;
   private final ColorSetting setting;
   private final Animation expand = new Animation(170.0F, 0.0F);
   private boolean expanded;
   private float hue;
   private float sat;
   private float val;
   private boolean draggingSv;
   private boolean draggingHue;
   private boolean hexFocused;
   private final StringBuilder hexBuffer = new StringBuilder();

   public ColorWidget(ThemeManager themeManager, ColorSetting colorSetting) {
      super(themeManager, colorSetting);
      this.setting = colorSetting;
      this.syncFromSetting();
   }

   private void syncFromSetting() {
      float[] f = Colors.rgbToHsv(this.setting.get());
      this.hue = f[0];
      this.sat = f[1];
      this.val = f[2];
   }

   public void setExpanded(boolean value) {
      if (value && !this.expanded) {
         this.syncFromSetting();
      }

      this.expanded = value;
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   private void apply() {
      this.setting.set(Integer.valueOf(Colors.hsvToRgb(this.hue, this.sat, this.val)));
   }

   @Override
   public float height(NVGRenderer nVGRenderer) {
      return 22.0F + this.expand.value() * 124.0F;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2) {
      Theme theme = this.theme();
      float f = this.y + 11.0F;
      nVGRenderer.text(this.setting.getName(), this.x, f, 12.5F, theme.textMuted());
      float f14 = 26.0F;
      float f15 = 14.0F;
      nVGRenderer.rect(this.x + this.width - f14, f - f15 / 2.0F, f14, f15, 5.0F, this.setting.get() | 0xFF000000);
      nVGRenderer.rectOutline(this.x + this.width - f14, f - f15 / 2.0F, f14, f15, 5.0F, 1.0F, Colors.withAlpha(-1, 0.25F));
      this.expand.setTarget(this.expanded ? 1.0F : 0.0F);
      float f16 = this.expand.value();
      if (!(f16 < 0.01F)) {
         nVGRenderer.save();
         nVGRenderer.scissor(this.x - 4.0F, this.y + 22.0F, this.width + 8.0F, f16 * 124.0F);
         nVGRenderer.alpha(f16);
         float f17 = this.svTop();
         int n = Colors.hsvToRgb(this.hue, 1.0F, 1.0F);
         nVGRenderer.rect(this.x, f17, this.width, 74.0F, 6.0F, n);
         nVGRenderer.rectGradient(this.x, f17, this.width, 74.0F, 6.0F, -1, Colors.withAlpha(-1, 0), false);
         nVGRenderer.rectGradient(this.x, f17, this.width, 74.0F, 6.0F, Colors.withAlpha(-16777216, 0), -16777216, true);
         float f18 = this.x + this.sat * this.width;
         float f19 = f17 + (1.0F - this.val) * 74.0F;
         nVGRenderer.circle(f18, f19, 6.0F, -1);
         nVGRenderer.circle(f18, f19, 4.2F, Colors.hsvToRgb(this.hue, this.sat, this.val));
         float f20 = this.hueTop();
         float f21 = this.width / 6.0F;

         for (int localY = 0; localY < 6; localY++) {
            int step = Colors.hsvToRgb((float)(localY * 60), 1.0F, 1.0F);
            int step2 = Colors.hsvToRgb((localY + 1) * 60 % 360 == 0 ? 359.9F : (float)((localY + 1) * 60), 1.0F, 1.0F);
            nVGRenderer.rectGradient(this.x + (float)localY * f21, f20, f21 + 0.5F, 10.0F, 0.0F, step, step2, false);
         }

         nVGRenderer.rectOutline(this.x, f20, this.width, 10.0F, 5.0F, 1.5F, Colors.withAlpha(-15988208, 0.9F));
         float f22 = this.x + this.hue / 360.0F * this.width;
         nVGRenderer.circle(f22, f20 + 5.0F, 6.0F, -1);
         nVGRenderer.circle(f22, f20 + 5.0F, 4.2F, n);
         float f23 = this.bottomTop();
         nVGRenderer.rect(this.x, f23, 30.0F, 18.0F, 5.0F, this.setting.get() | 0xFF000000);
         nVGRenderer.rectOutline(this.x, f23, 30.0F, 18.0F, 5.0F, 1.0F, Colors.withAlpha(-1, 0.25F));
         float f24 = this.x + 38.0F;
         float f25 = this.width - 38.0F;
         int n9 = this.hexFocused ? Colors.withAlpha(theme.accent(), 0.18F) : Colors.withAlpha(-16777216, 0.45F);
         nVGRenderer.rect(f24, f23, f25, 18.0F, 5.0F, n9);
         if (this.hexFocused) {
            nVGRenderer.rectOutline(f24, f23, f25, 18.0F, 5.0F, 1.2F, theme.accentBright());
         }

         String text2 = this.hexFocused
            ? this.hexBuffer + System.nanoTime( / 400000000L % 2L == 0L ? "," : "")
            : this.setting.hex();
         nVGRenderer.text(text2, f24 + 8.0F, f23 + 9.0F, 12.0F, this.hexFocused ? theme.textPrimary() : theme.textMuted());
         nVGRenderer.restore();
      }
   }

   private float svTop() {
      return this.y + 22.0F + 2.0F;
   }

   private float hueTop() {
      return this.svTop() + 74.0F + 6.0F;
   }

   private float bottomTop() {
      return this.hueTop() + 10.0F + 6.0F;
   }

   @Override
   public boolean mouseClicked(float f, float f3, int n) {
      if (n != 0) {
         return false;
      } else if (f3 >= this.y && f3 <= this.y + 22.0F && f >= this.x && f <= this.x + this.width) {
         this.expanded = !this.expanded;
         if (this.expanded) {
            this.syncFromSetting();
         }

         this.hexFocused = false;
         UiSounds.select();
         return true;
      } else if (!this.expanded) {
         return false;
      } else if (inRect(f, f3, this.x, this.svTop(), this.width, 74.0F)) {
         this.draggingSv = true;
         this.applySv(f, f3);
         return true;
      } else if (inRect(f, f3, this.x, this.hueTop() - 3.0F, this.width, 16.0F)) {
         this.draggingHue = true;
         this.applyHue(f);
         return true;
      } else if (inRect(f, f3, this.x + 38.0F, this.bottomTop(), this.width - 38.0F, 18.0F)) {
         this.hexFocused = true;
         this.hexBuffer.setLength(0);
         UiSounds.select();
         return true;
      } else {
         if (this.hexFocused) {
            this.commitHex();
         }

         return false;
      }
   }

   private static boolean inRect(float f, float f7, float f8, float f9, float f10, float f11) {
      return f >= f8 && f <= f8 + f10 && f7 >= f9 && f7 <= f9 + f11;
   }

   private void applySv(float f, float f3) {
      this.sat = Math.clamp((f - this.x) / this.width, 0.0F, 1.0F);
      this.val = 1.0F - Math.clamp((f3 - this.svTop()) / 74.0F, 0.0F, 1.0F);
      this.apply();
   }

   private void applyHue(float f) {
      this.hue = Math.clamp((f - this.x) / this.width, 0.0F, 1.0F) * 359.9F;
      this.apply();
   }

   @Override
   public void mouseDragged(float f, float f3) {
      if (this.draggingSv) {
         this.applySv(f, f3);
      }

      if (this.draggingHue) {
         this.applyHue(f);
      }
   }

   @Override
   public void mouseReleased() {
      this.draggingSv = false;
      this.draggingHue = false;
   }

   @Override
   public boolean keyPressed(int n) {
      if (!this.hexFocused) {
         return false;
      } else if (n == 256) {
         this.hexFocused = false;
         return true;
      } else if (n == 257 || n == 335) {
         this.commitHex();
         return true;
      } else if (n == 259) {
         if (!this.hexBuffer.isEmpty()) {
            this.hexBuffer.deleteCharAt(this.hexBuffer.length() - 1);
         }

         return true;
      } else {
         char ch = hexChar(n);
         if (ch != 0 && this.hexBuffer.length() < 6) {
            this.hexBuffer.append(ch);
            if (this.hexBuffer.length() == 6) {
               this.commitHex();
            }
         }

         return true;
      }
   }

   private static char hexChar(int n) {
      if (n >= 48 && n <= 57) {
         return (char)(48 + n - 48);
      } else if (n >= 320 && n <= 329) {
         return (char)(48 + n - 320);
      } else {
         return n >= 65 && n <= 70 ? (char)(65 + n - 65) : '\u0000';
      }
   }

   private void commitHex() {
      this.hexFocused = false;
      if (this.hexBuffer.length() == 6) {
         try {
            this.setting.set(Integer.valueOf(0xFF000000 | Integer.parseInt(this.hexBuffer.toString(), 16)));
            this.syncFromSetting();
            UiSounds.keybindSet();
         } catch (NumberFormatException ex) {
         }
      }

      this.hexBuffer.setLength(0);
   }

   @Override
   public boolean isListening() {
      return this.hexFocused;
   }
}
