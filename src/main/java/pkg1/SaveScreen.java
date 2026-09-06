package pkg1;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import util.Inner1;
import util.UtilsUtil;

public class SaveScreen extends Screen {
   private static final int intVal = 320;
   private static final int intVal2 = 104;
   private Screen class437;
   private Setting<String> val;
   private Inner1 val2;
   private int intVal3;
   private int intVal4;

   public SaveScreen(Screen var1, Setting<String> var2) {
      super(Text.literal("Enter " + var2.string));
      this.class437 = var1;
      this.val = var2;
   }

   protected void init() {
      super.init();
      this.intVal3 = (this.width - 320) / 2;
      this.intVal4 = (this.height - 104) / 2;
      this.val2 = UtilsUtil.valOf2(this.textRenderer, this.intVal3 + 14, this.intVal4 + 36, 292, 20, this.val.string);
      this.val2.setMaxLength(256);
      this.val2.setText(this.val.getObject());
      this.addSelectableChild(this.val2);
      this.setInitialFocus(this.val2);
      this.addDrawableChild(UtilsUtil.valOf("Save", this.intVal3 + 320 - 14 - 142, this.intVal4 + 104 - 32, 70, 20, this::run3));
      this.addDrawableChild(UtilsUtil.valOf("Cancel", this.intVal3 + 320 - 14 - 70, this.intVal4 + 104 - 32, 70, 20, this::close));
   }

   private void run3() {
      this.val.run2(this.val2.getText());
      this.client.setScreen(this.class437);
   }

   public void render(DrawContext var1, int var2, int var3, float var4) {
      UtilsUtil.run(var1, this.width, this.height);
      UtilsUtil.run9(var1, this.intVal3, this.intVal4, 320, 104);
      UtilsUtil.run2(var1, this.textRenderer, this.val.string, this.intVal3 + 14, this.intVal4 + 14);
      this.val2.render(var1, var2, var3, var4);
      super.render(var1, var2, var3, var4);
   }

   public boolean shouldPause() {
      return false;
   }

   public void close() {
      this.client.setScreen(this.class437);
   }
}
