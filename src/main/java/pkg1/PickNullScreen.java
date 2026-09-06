package pkg1;

import java.awt.Color;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import util.Inner1;
import util.UtilsUtil;

public final class PickNullScreen extends Screen {
   private static final int[] intArray = new int[]{11822335, 4886015, 2742760, 4381050, 15749714, 16750141, 15817653, 16777215, 16766011, 10181046};
   private static final int intVal = 296;
   private static final int intVal2 = 236;
   private static final int intVal3 = 160;
   private static final int intVal4 = 16;
   private static final int intVal5 = 12;
   private static final int intVal6 = 18;
   private Screen class437;
   private String string;
   private Supplier<ActivityChunkFinderModuleHelper4> supplier;
   private Consumer<ActivityChunkFinderModuleHelper4> consumer;
   private float floatVal;
   private float floatVal2;
   private float floatVal3;
   private int intVal7;
   private Inner1 val;
   private PickNullScreen.State val2 = PickNullScreen.State.NONE;
   private int intVal8;
   private int intVal9;

   public PickNullScreen(Screen var1, Setting<ActivityChunkFinderModuleHelper4> var2) {
      this(var1, var2.string, var2::getObject, var2::run2);
   }

   public PickNullScreen(Screen var1, String var2, Supplier<ActivityChunkFinderModuleHelper4> var3, Consumer<ActivityChunkFinderModuleHelper4> var4) {
      super(Text.literal("Pick null"));
      this.class437 = var1;
      this.string = var2;
      this.supplier = var3;
      this.consumer = var4;
   }

   protected void init() {
      super.init();
      this.intVal8 = (this.width - 296) / 2;
      this.intVal9 = (this.height - 236) / 2;
      ActivityChunkFinderModuleHelper4 var1 = this.supplier.get();
      this.intVal7 = var1.intVal4;
      float[] var2 = Color.RGBtoHSB(var1.intVal, var1.intVal2, var1.intVal3, null);
      this.floatVal = var2[0];
      this.floatVal2 = var2[1];
      this.floatVal3 = var2[2];
      this.val = UtilsUtil.valOf2(this.textRenderer, this.intVal8 + 12, this.intVal9 + 236 - 58, 96, 18, "Hex");
      this.val.setMaxLength(9);
      this.val.setText(this.getString3());
      this.val.setChangedListener(this::run3);
      this.addSelectableChild(this.val);
      this.addDrawableChild(UtilsUtil.valOf("Save", this.intVal8 + 296 - 12 - 130, this.intVal9 + 236 - 30, 62, 20, this::run16));
      this.addDrawableChild(UtilsUtil.valOf("Cancel", this.intVal8 + 296 - 12 - 64, this.intVal9 + 236 - 30, 64, 20, this::close));
   }

   private int getInt() {
      return this.intVal8 + 12;
   }

   private int getInt2() {
      return this.intVal9 + 28;
   }

   private int getInt3() {
      return this.getInt() + 160 + 12;
   }

   private int getInt4() {
      return this.getInt3() + 16 + 12;
   }

   public boolean mouseClicked(double _cx, double _cy, int _cb) {
      double var3 = _cx;
      double var5 = _cy;
      int var7 = this.intVal9 + 236 - 84;
      if (var5 >= var7 && var5 < var7 + 18) {
         for (int var8 = 0; var8 < intArray.length; var8++) {
            int var9 = this.intVal8 + 12 + var8 * 22;
            if (var3 >= var9 && var3 < var9 + 18) {
               this.run2(intArray[var8]);
               return true;
            }
         }
      }

      if (check(var3, var5, this.getInt(), this.getInt2(), 160, 160)) {
         this.val2 = PickNullScreen.State.SQUARE;
         this.run(var3, var5);
         return true;
      } else if (check(var3, var5, this.getInt3(), this.getInt2(), 16, 160)) {
         this.val2 = PickNullScreen.State.HUE;
         this.run(var3, var5);
         return true;
      } else if (check(var3, var5, this.getInt4(), this.getInt2(), 16, 160)) {
         this.val2 = PickNullScreen.State.ALPHA;
         this.run(var3, var5);
         return true;
      } else {
         return super.mouseClicked(_cx, _cy, _cb);
      }
   }

   public boolean mouseDragged(double _cx, double _cy, int _cb, double _dx, double _dy) {
      if (this.val2 == PickNullScreen.State.NONE) {
         return super.mouseDragged(_cx, _cy, _cb, _dx, _dy);
      } else {
         this.run(_cx, _cy);
         return true;
      }
   }

   public boolean mouseReleased(double _cx, double _cy, int _cb) {
      this.val2 = PickNullScreen.State.NONE;
      return super.mouseReleased(_cx, _cy, _cb);
   }

   private void run(double var1, double var3) {
      float var5 = floatOf((float)((var3 - this.getInt2()) / 160.0));
      switch (this.val2) {
         case SQUARE:
            this.floatVal2 = floatOf((float)((var1 - this.getInt()) / 160.0));
            this.floatVal3 = 1.0F - var5;
            break;
         case HUE:
            this.floatVal = var5;
            break;
         case ALPHA:
            this.intVal7 = Math.round((1.0F - var5) * 255.0F);
            break;
         default:
            return;
      }

      this.run5();
   }

   private void run2(int var1) {
      float[] var2 = Color.RGBtoHSB(var1 >> 16 & 0xFF, var1 >> 8 & 0xFF, var1 & 0xFF, null);
      this.floatVal = var2[0];
      this.floatVal2 = var2[1];
      this.floatVal3 = var2[2];
      this.run5();
   }

   private void run3(String var1) {
      String var2 = var1.startsWith("#") ? var1.substring(1) : var1;
      if (var2.length() == 6 || var2.length() == 8) {
         try {
            long var3 = Long.parseLong(var2, 16);
            if (var2.length() == 8) {
               this.intVal7 = (int)(var3 >>> 24 & 255L);
            }

            float[] var5 = Color.RGBtoHSB((int)(var3 >> 16 & 255L), (int)(var3 >> 8 & 255L), (int)(var3 & 255L), null);
            this.floatVal = var5[0];
            this.floatVal2 = var5[1];
            this.floatVal3 = var5[2];
         } catch (NumberFormatException var6) {
         }
      }
   }

   private void run5() {
      if (this.val != null && !this.val.isFocused()) {
         this.val.setText(this.getString3());
      }
   }

   private String getString3() {
      int var1 = this.getInt5();
      return String.format(Locale.ROOT, "#%02X%02X%02X", var1 >> 16 & 0xFF, var1 >> 8 & 0xFF, var1 & 0xFF);
   }

   private int getInt5() {
      return Color.HSBtoRGB(this.floatVal, this.floatVal2, this.floatVal3) & 16777215;
   }

   private ActivityChunkFinderModuleHelper4 getVal() {
      int var1 = this.getInt5();
      return new ActivityChunkFinderModuleHelper4(var1 >> 16 & 0xFF, var1 >> 8 & 0xFF, var1 & 0xFF, this.intVal7);
   }

   private void run16() {
      this.consumer.accept(this.getVal());
      this.close();
   }

   public void render(DrawContext var1, int var2, int var3, float var4) {
      UtilsUtil.run(var1, this.width, this.height);
      UtilsUtil.run9(var1, this.intVal8, this.intVal9, 296, 236);
      UtilsUtil.run2(var1, this.textRenderer, this.string, this.intVal8 + 12, this.intVal9 + 12);
      this.run4(var1);
      this.run6(var1);
      this.run7(var1);
      this.run8(var1);
      int var5 = this.intVal8 + 296 - 12 - 76;
      int var6 = this.intVal9 + 236 - 58;
      run9(var1, var5, var6, 76, 18);
      var1.fill(var5, var6, var5 + 76, var6 + 18, this.intVal7 << 24 | this.getInt5());
      this.val.render(var1, var2, var3, var4);
      super.render(var1, var2, var3, var4);
   }

   private void run4(DrawContext var1) {
      int var2 = this.getInt();
      int var3 = this.getInt2();

      for (int var4 = 0; var4 < 160; var4++) {
         float var5 = var4 / 159.0F;
         int var6 = 0xFF000000 | Color.HSBtoRGB(this.floatVal, var5, 1.0F) & 16777215;
         var1.fillGradient(var2 + var4, var3, var2 + var4 + 1, var3 + 160, var6, -16777216);
      }

      int var7 = var2 + Math.round(this.floatVal2 * 160.0F);
      int var8 = var3 + Math.round((1.0F - this.floatVal3) * 160.0F);
      var1.fill(var7 - 3, var8, var7 + 4, var8 + 1, -1);
      var1.fill(var7, var8 - 3, var7 + 1, var8 + 4, -1);
   }

   private void run6(DrawContext var1) {
      int var2 = this.getInt3();
      int var3 = this.getInt2();

      for (int var4 = 0; var4 < 6; var4++) {
         int var5 = 0xFF000000 | Color.HSBtoRGB(var4 / 6.0F, 1.0F, 1.0F) & 16777215;
         int var6 = 0xFF000000 | Color.HSBtoRGB((var4 + 1) / 6.0F, 1.0F, 1.0F) & 16777215;
         int var7 = var3 + Math.round(var4 * 160 / 6.0F);
         int var8 = var3 + Math.round((var4 + 1) * 160 / 6.0F);
         var1.fillGradient(var2, var7, var2 + 16, var8, var5, var6);
      }

      int var9 = var3 + Math.round(this.floatVal * 160.0F);
      var1.fill(var2 - 2, var9 - 1, var2 + 16 + 2, var9 + 1, -1);
   }

   private void run7(DrawContext var1) {
      int var2 = this.getInt4();
      int var3 = this.getInt2();
      run9(var1, var2, var3, 16, 160);
      int var4 = this.getInt5();
      var1.fillGradient(var2, var3, var2 + 16, var3 + 160, 0xFF000000 | var4, var4);
      int var5 = var3 + Math.round((1.0F - this.intVal7 / 255.0F) * 160.0F);
      var1.fill(var2 - 2, var5 - 1, var2 + 16 + 2, var5 + 1, -1);
   }

   private void run8(DrawContext var1) {
      int var2 = this.intVal9 + 236 - 84;

      for (int var3 = 0; var3 < intArray.length; var3++) {
         int var4 = this.intVal8 + 12 + var3 * 22;
         var1.fill(var4 - 1, var2 - 1, var4 + 18 + 1, var2 + 18 + 1, -14473937);
         var1.fill(var4, var2, var4 + 18, var2 + 18, 0xFF000000 | intArray[var3]);
      }
   }

   private static void run9(DrawContext var0, int var1, int var2, int var3, int var4) {
      var0.fill(var1, var2, var1 + var3, var2 + var4, -13881800);

      for (byte var5 = 0; var5 < var4; var5 += 8) {
         for (int var6 = var5 / 8 % 2 == 0 ? 0 : 8; var6 < var3; var6 += 16) {
            var0.fill(var1 + var6, var2 + var5, Math.min(var1 + var6 + 8, var1 + var3), Math.min(var2 + var5 + 8, var2 + var4), -11908009);
         }
      }
   }

   private static boolean check(double var0, double var2, int var4, int var5, int var6, int var7) {
      return var0 >= var4 && var0 < var4 + var6 && var2 >= var5 && var2 < var5 + var7;
   }

   private static float floatOf(float var0) {
      return Math.max(0.0F, Math.min(1.0F, var0));
   }

   public boolean shouldPause() {
      return false;
   }

   public void close() {
      this.client.setScreen(this.class437);
   }

   enum State {
      NONE,
      SQUARE,
      HUE,
      ALPHA;

      private static PickNullScreen.State[] getValArray() {
         return new PickNullScreen.State[]{NONE, SQUARE, HUE, ALPHA};
      }
   }
}
