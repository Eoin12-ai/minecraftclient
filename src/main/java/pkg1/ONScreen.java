package pkg1;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import util.Inner1;
import util.UtilsUtil;

public class ONScreen extends Screen {
   private Module val;
   private Screen class437;

   public ONScreen(Module var1, Screen var2) {
      super(Text.literal(var1.title + " Settings"));
      this.val = var1;
      this.class437 = var2;
   }

   protected void init() {
      byte var1 = 40;

      for (ActivityChunkFinderModuleEntry var4 : this.val.val2.getList()) {
         for (Setting var6 : var4.getList()) {
            if (var6.isEnabled()) {
               Object var7 = var6.getObject();
               if (var7 instanceof Boolean) {
                  Inner1[] var9 = new Inner1[1];
                  var9[0] = UtilsUtil.valOf(
                     UtilsUtil.addSetting(var6.string) + ": " + (((Boolean)var6.getObject()) ? "ON" : "OFF"), this.width / 2 - 100, var1, 200, 20, () -> ONScreen.run2(var6, var9, var6)
                  );
                  this.addDrawableChild(var9[0]);
               } else if (!(var7 instanceof Integer) && !(var7 instanceof Double) && !(var7 instanceof Float)) {
                  if (var7 instanceof Enum) {
                     Inner1[] var14 = new Inner1[1];
                     var14[0] = UtilsUtil.valOf(
                        UtilsUtil.addSetting(var6.string) + ": " + ((Enum)var6.getObject()).name(), this.width / 2 - 100, var1, 200, 20, () -> ONScreen.run(var6, var14, var6)
                     );
                     this.addDrawableChild(var14[0]);
                  } else {
                     this.addDrawableChild(
                        UtilsUtil.valOf(UtilsUtil.addSetting(var6.string) + ": " + var7, this.width / 2 - 100, var1, 200, 20, () -> ONScreen.run3())
                     );
                  }
               } else {
                  double var8 = var6.getDouble() != null ? var6.getDouble() : 0.0;
                  double var10 = var6.getDouble2() != null ? var6.getDouble2() : 100.0;
                  double var12 = ((Number)var7).doubleValue();
                  this.addDrawableChild(
                     new SliderWidget(
                        this.width / 2 - 100,
                        var1,
                        200,
                        20,
                        Text.literal(var6.string + ": " + this.stringOf(var12, var7)),
                        (var12 - var8) / (var10 - var8)
                     ) {
                        final Setting val;
                        final Object object;
                        final double doubleVal;
                        final double doubleVal2;
                        final ONScreen val2 = ONScreen.this;

                        {
                           this.val = var6;
                           this.object = var7;
                           this.doubleVal = var8;
                           this.doubleVal2 = var10;
                        }

                        protected void updateMessage() {
                           this.setMessage(Text.literal(this.val.string + ": " + this.val2.stringOf(this.getDouble(), this.object)));
                        }

                        protected void applyValue() {
                           double var1x = this.doubleVal + this.value * (this.doubleVal2 - this.doubleVal);
                           if (this.object instanceof Integer) {
                              this.val.run2((int)Math.round(var1x));
                           } else if (this.object instanceof Double) {
                              this.val.run2(var1x);
                           } else if (this.object instanceof Float) {
                              this.val.run2((float)var1x);
                           }
                        }

                        private double getDouble() {
                           return this.doubleVal + this.value * (this.doubleVal2 - this.doubleVal);
                        }
                     }
                  );
               }

               var1 += 24;
            }
         }
      }

      this.addDrawableChild(UtilsUtil.valOf("Back", this.width / 2 - 100, this.height - 30, 200, 20, this::close));
   }

   String stringOf(double var1, Object var3) {
      return var3 instanceof Integer ? String.valueOf(Math.round(var1)) : String.format("%.2f", var1);
   }

   public void render(DrawContext var1, int var2, int var3, float var4) {
      UtilsUtil.run(var1, this.width, this.height);
      UtilsUtil.run9(var1, this.width / 2 - 108, 12, 216, this.height - 24);
      UtilsUtil.run2(var1, this.textRenderer, this.val.title + " settings", this.width / 2 - 100, 22);
      super.render(var1, var2, var3, var4);
   }

   public boolean shouldPause() {
      return false;
   }

   public void close() {
      this.client.setScreen(this.class437);
   }

   private static void run3() {
   }

   private static void run(Setting var0, Inner1[] var1, Setting var2) {
      Enum[] var3 = (Enum[])((Enum)var0.getObject()).getDeclaringClass().getEnumConstants();
      int var4 = (((Enum)var0.getObject()).ordinal() + 1) % var3.length;
      var0.run2(var3[var4]);
      var1[0].setMessage(Text.literal(UtilsUtil.addSetting(var2.string) + ": " + ((Enum)var0.getObject()).name()));
   }

   private static void run2(Setting var0, Inner1[] var1, Setting var2) {
      var0.run2(!(Boolean)var0.getObject());
      var1[0].setMessage(Text.literal(UtilsUtil.addSetting(var2.string) + ": " + (((Boolean)var0.getObject()) ? "ON" : "OFF")));
   }
}
