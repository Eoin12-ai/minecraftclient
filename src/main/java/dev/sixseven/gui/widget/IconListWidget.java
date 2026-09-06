package dev.sixseven.gui.widget;

import dev.sixseven.gui.ClickGuiScreen;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.settings.IconListSetting;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import dev.sixseven.util.UiSounds;
import net.minecraft.client.MinecraftClient;

public class IconListWidget extends SettingWidget {
   private static final float ROW = 26.0F;
   private final IconListSetting setting;

   public IconListWidget(ThemeManager themeManager, IconListSetting iconListSetting) {
      super(themeManager, iconListSetting);
      this.setting = iconListSetting;
   }

   @Override
   public float height(NVGRenderer nVGRenderer) {
      return 26.0F;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2) {
      Theme theme = this.theme();
      float f = this.y + 13.0F;
      nVGRenderer.text(this.setting.getName(), this.x, f, 12.5F, theme.textMuted());
      float f7 = 88.0F;
      float f8 = 18.0F;
      float f9 = this.x + this.width - f7;
      float f10 = f - f8 / 2.0F;
      boolean ok = tickDelta >= f9 && tickDelta <= f9 + f7 && tickDelta2 >= f10 && tickDelta2 <= f10 + f8;
      nVGRenderer.rectGradient(f9, f10, f7, f8, f8 / 2.0F, theme.headerTop(), theme.headerBottom(), true);
      nVGRenderer.rectOutline(f9, f10, f7, f8, f8 / 2.0F, 1.1F, Colors.withAlpha(ok ? theme.accentBright() : theme.accent(), ok ? 0.9F : 0.4F));
      String text2 = "Pick";
      float f11 = nVGRenderer.textWidth(text2, 12.0F);
      nVGRenderer.textGradient(text2, f9 + (f7 - f11) / 2.0F, f10 + f8 / 2.0F, 12.0F, theme.accentBright(), theme.accent());
      long l = this.setting.enabledCount();
      String text3 = l + "/" + this.setting.size();
      nVGRenderer.text(text3, f9 - 8.0F - nVGRenderer.textWidth(text3, 11.5F), f, 11.5F, theme.textDisabled());
   }

   @Override
   public boolean mouseClicked(float f, float f3, int n) {
      if (n == 0 && this.contains(f, f3)) {
         if (MinecraftClient.getInstance().currentScreen instanceof ClickGuiScreen clickGuiScreen) {
            clickGuiScreen.openIconPicker(this.setting);
            UiSounds.select();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
