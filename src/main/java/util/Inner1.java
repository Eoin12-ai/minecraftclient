package util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import pkg1.AdminDetectorModuleUtil;

public final class Inner1 extends TextFieldWidget {
   public Inner1(TextRenderer var1, int var2, int var3, int var4, int var5, Text var6) {
      super(var1, var2, var3, var4, var5, var6);
      this.setDrawsBackground(false);
      this.setEditableColor(-1775889);
      this.setUneditableColor(-8749684);
   }

   public void renderWidget(DrawContext var1, int var2, int var3, float var4) {
      int var5 = this.getX();
      int var6 = this.getY();
      int var7 = this.getWidth();
      int var8 = this.getHeight();
      boolean var9 = this.isFocused();
      AdminDetectorModuleUtil.run2(var1, var5 - 1, var6 - 1, var7 + 2, var8 + 2, 5, var9 ? UtilsUtil.intOf(UtilsUtil.getInt(), 120) : -13881800);
      AdminDetectorModuleUtil.run2(var1, var5, var6, var7, var8, 4, -16119023);
      AdminDetectorModuleUtil.run2(var1, var5, var6 + var8 - 2, var7, 2, 1, var9 ? UtilsUtil.getInt() : -13881800);
      super.renderWidget(var1, var2, var3, var4);
   }
}
