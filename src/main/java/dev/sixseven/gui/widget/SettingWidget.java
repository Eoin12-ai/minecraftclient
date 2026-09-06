package dev.sixseven.gui.widget;

import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.settings.Setting;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;

public abstract class SettingWidget {
   protected final ThemeManager themes;
   private final Setting<?> boundSetting;
   protected float x;
   protected float y;
   protected float width;

   protected SettingWidget(ThemeManager themeManager, Setting<?> setting) {
      this.themes = themeManager;
      this.boundSetting = setting;
   }

   public boolean isVisible() {
      return this.boundSetting == null || this.boundSetting.isVisible();
   }

   protected Theme theme() {
      return this.themes.current();
   }

   public void setBounds(float f, float f4, float f5) {
      this.x = f;
      this.y = f4;
      this.width = f5;
   }

   public boolean contains(float f, float f3) {
      return f >= this.x && f <= this.x + this.width && f3 >= this.y && f3 <= this.y + this.height((NVGRenderer)null);
   }

   public abstract float height(NVGRenderer nVGRenderer);

   public abstract void render(NVGRenderer nVGRenderer, float f, float f2);

   public boolean mouseClicked(float f, float f3, int n) {
      return false;
   }

   public void mouseDragged(float f, float f3) {
   }

   public void mouseReleased() {
   }

   public boolean keyPressed(int n) {
      return false;
   }

   public boolean charTyped(int n) {
      return false;
   }

   public boolean isListening() {
      return false;
   }
}
