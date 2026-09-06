package dev.sixseven.gui.panel;

import dev.sixseven.SixSevenClient;
import dev.sixseven.gui.ClickGuiState;
import dev.sixseven.gui.widget.BooleanWidget;
import dev.sixseven.gui.widget.ColorWidget;
import dev.sixseven.gui.widget.SettingWidget;
import dev.sixseven.gui.widget.SliderWidget;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.ColorSetting;
import dev.sixseven.settings.Setting;
import dev.sixseven.settings.SliderSetting;
import dev.sixseven.theme.SoundSettings;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import dev.sixseven.util.UiSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ThemesPanel extends Panel {
   private static final float ROW_H = 26.0F;
   private static final float ADD_ROW_H = 28.0F;
   private static final float SECTION_H = 24.0F;
   private final ColorWidget accentWidget;
   private final ColorSetting accentProxy;
   private final List<SettingWidget> soundWidgets = new ArrayList<>();
   private static final int STARTUP_FIRST_WIDGET = 5;
   private int hoveredRow = -1;
   private float lastStartY = Float.MIN_VALUE;

   public ThemesPanel(final ThemeManager themeManager, ClickGuiState clickGuiState) {
      super(themeManager, clickGuiState.panel("__themes__"));
      this.accentProxy = new ColorSetting("Accent", "Custom theme accent color", themeManager.current().accent()) {
         public void set(Integer num) {
            super.set(num);
            Theme theme = themeManager.current();
            if (theme.isCustom()) {
               theme.setAccent(num | 0xFF000000);
            }
         }
      };
      this.accentWidget = new ColorWidget(themeManager, this.accentProxy);
      SoundSettings soundSettings = SixSevenClient.sounds();
      if (soundSettings != null) {
         for (Setting setting : soundSettings.all()) {
            if (setting instanceof SliderSetting sliderSetting) {
               this.soundWidgets.add(new SliderWidget(themeManager, sliderSetting));
            } else if (setting instanceof BooleanSetting booleanSetting) {
               this.soundWidgets.add(new BooleanWidget(themeManager, booleanSetting));
            }
         }
      }
   }

   @Override
   protected String title() {
      return "Themes";
   }

   @Override
   protected float contentHeight(NVGRenderer nVGRenderer) {
      float temp = 12.0F + (float)this.themes.getThemes().size() * 26.0F + 28.0F;
      if (this.themes.current().isCustom()) {
         temp += this.accentWidget.height(nVGRenderer) + 6.0F;
      }

      temp += 48.0F;

      for (SettingWidget settingWidget : this.soundWidgets) {
         temp += settingWidget.height(nVGRenderer) + 3.0F;
      }

      return temp;
   }

   @Override
   protected void renderContent(NVGRenderer nVGRenderer, float contentTop, float tickDelta2, float tickDelta3, float tickDelta4, float tickDelta5, float edgeFade) {
      this.lastStartY = contentTop;
      Theme theme = this.themes.current();
      float f = contentTop;
      int n = 0;
      int bestSlot = -1;

      for (Theme theme2 : this.themes.getThemes()) {
         float f6 = this.edgeFade(f, f + 26.0F, tickDelta4, tickDelta5);
         nVGRenderer.save();
         nVGRenderer.alpha(f6);
         boolean ok = theme2 == theme;
         boolean ok2 = tickDelta3 >= f && tickDelta3 <= f + 26.0F && tickDelta2 >= this.panelState.x + 6.0F && tickDelta2 <= this.panelState.x + 210.0F - 6.0F;
         if (ok2) {
            bestSlot = n;
         }

         n++;
         if (ok || ok2) {
            nVGRenderer.rect(this.panelState.x + 6.0F, f, 198.0F, 26.0F, 7.0F, Colors.withAlpha(this.theme().accent(), ok ? 0.16F : 0.08F));
         }

         float f7 = f + 13.0F;
         nVGRenderer.circle(this.panelState.x + 20.0F, f7, 6.0F, theme2.accent());
         if (ok) {
            nVGRenderer.rectOutline(this.panelState.x + 20.0F - 9.0F, f7 - 9.0F, 18.0F, 18.0F, 9.0F, 1.5F, this.theme().accentBright());
         }

         nVGRenderer.text(theme2.getName(), this.panelState.x + 36.0F, f7, 13.5F, ok ? this.theme().textPrimary() : this.theme().textMuted());
         if (theme2.isCustom()) {
            nVGRenderer.cross(this.panelState.x + 210.0F - 28.0F, f7 - 6.0F, 12.0F, 1.6F, this.theme().textDisabled());
         }

         nVGRenderer.restore();
         f += 26.0F;
      }

      if (theme.isCustom()) {
         this.accentWidget.setBounds(this.panelState.x + 14.0F, f + 3.0F, 182.0F);
         this.accentWidget.render(nVGRenderer, tickDelta2, tickDelta3);
         f += this.accentWidget.height(nVGRenderer) + 6.0F;
      }

      float f8 = this.edgeFade(f, f + 28.0F, tickDelta4, tickDelta5);
      nVGRenderer.save();
      nVGRenderer.alpha(f8);
      boolean ok3 = tickDelta3 >= f && tickDelta3 <= f + 28.0F - 4.0F && tickDelta2 >= this.panelState.x + 6.0F && tickDelta2 <= this.panelState.x + 210.0F - 6.0F;
      nVGRenderer.rect(this.panelState.x + 6.0F, f, 198.0F, 24.0F, 7.0F, Colors.withAlpha(this.theme().accent(), ok3 ? 0.22F : 0.12F));
      String text2 = "+  Add Custom";
      nVGRenderer.text(
         text2,
         this.panelState.x + (210.0F - nVGRenderer.textWidth(text2, 13.0F)) / 2.0F,
         f + 12.0F,
         13.0F,
         ok3 ? this.theme().accentBright() : this.theme().textPrimary()
      );
      nVGRenderer.restore();
      if (ok3) {
         bestSlot = 999;
      }

      f += 28.0F;
      f = this.sectionHeader(nVGRenderer, "Sounds", f, tickDelta4, tickDelta5);

      for (int offset = 0; offset < this.soundWidgets.size(); offset++) {
         if (offset == 5) {
            f = this.sectionHeader(nVGRenderer, "Startup Sound", f, tickDelta4, tickDelta5);
         }

         SettingWidget settingWidget = this.soundWidgets.get(offset);
         settingWidget.setBounds(this.panelState.x + 14.0F, f, 182.0F);
         float f9 = this.edgeFade(f, f + settingWidget.height(nVGRenderer), tickDelta4, tickDelta5);
         nVGRenderer.save();
         nVGRenderer.alpha(f9);
         settingWidget.render(nVGRenderer, tickDelta2, tickDelta3);
         nVGRenderer.restore();
         f += settingWidget.height(nVGRenderer) + 3.0F;
      }

      if (bestSlot != this.hoveredRow && bestSlot != -1) {
         UiSounds.hover();
      }

      this.hoveredRow = bestSlot;
   }

   private float sectionHeader(NVGRenderer nVGRenderer, String text2, float f, float f7, float f8) {
      float f9 = this.edgeFade(f, f + 24.0F, f7, f8);
      nVGRenderer.save();
      nVGRenderer.alpha(f9);
      float f10 = f + 12.0F + 3.0F;
      nVGRenderer.textGradient(text2.toUpperCase(Locale.ROOT), this.panelState.x + 14.0F, f10, 12.0F, this.theme().accentBright(), this.theme().accent());
      float f11 = this.panelState.x + 14.0F + nVGRenderer.textWidth(text2.toUpperCase(Locale.ROOT), 12.0F) + 8.0F;
      nVGRenderer.rect(f11, f10 - 0.5F, Math.max(0.0F, this.panelState.x + 210.0F - 14.0F - f11), 1.0F, 0.5F, Colors.withAlpha(this.theme().accent(), 0.3F));
      nVGRenderer.restore();
      return f + 24.0F;
   }

   @Override
   public boolean mouseClicked(float f, float f4, int n) {
      if (this.accentWidget.mouseClicked(f, f4, n)) {
         return true;
      } else {
         for (SettingWidget settingWidget : this.soundWidgets) {
            if (settingWidget.mouseClicked(f, f4, n)) {
               return true;
            }
         }

         if (n != 0) {
            return false;
         } else {
            float f5 = this.firstRowY();
            if (f5 == Float.MIN_VALUE) {
               return false;
            } else {
               for (Theme theme2 : this.themes.getThemes()) {
                  if (f >= f5 && f <= f5 + 26.0F && f4 >= this.panelState.x + 6.0F && f4 <= this.panelState.x + 210.0F - 6.0F) {
                     if (theme2.isCustom() && f4 >= this.panelState.x + 210.0F - 34.0F) {
                        this.themes.removeCustom(theme2);
                     } else {
                        this.themes.select(theme2);
                        if (theme2.isCustom()) {
                           this.accentProxy.set(Integer.valueOf(theme2.accent()));
                        }
                     }

                     return true;
                  }

                  f5 += 26.0F;
               }

               if (this.themes.current().isCustom()) {
                  f5 += this.accentWidget.height((NVGRenderer)null) + 6.0F;
               }

               if (f >= f5 && f <= f5 + 28.0F - 4.0F && f4 >= this.panelState.x + 6.0F && f4 <= this.panelState.x + 210.0F - 6.0F) {
                  Theme theme = this.themes.addCustom(this.themes.current().accent());
                  this.themes.select(theme);
                  this.accentProxy.set(Integer.valueOf(theme.accent()));
                  return true;
               } else {
                  return false;
               }
            }
         }
      }
   }

   private float firstRowY() {
      return this.lastStartY;
   }

   @Override
   public void mouseDragged(float f, float f3) {
      this.accentWidget.mouseDragged(f, f3);

      for (SettingWidget settingWidget : this.soundWidgets) {
         settingWidget.mouseDragged(f, f3);
      }
   }

   @Override
   public void mouseReleased() {
      this.accentWidget.mouseReleased();

      for (SettingWidget settingWidget : this.soundWidgets) {
         settingWidget.mouseReleased();
      }
   }

   @Override
   public boolean keyPressed(int n) {
      return this.accentWidget.keyPressed(n);
   }

   @Override
   public boolean isListening() {
      return this.accentWidget.isListening();
   }
}
