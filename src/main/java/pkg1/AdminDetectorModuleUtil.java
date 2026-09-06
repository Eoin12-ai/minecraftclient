package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.client.gui.DrawContext;

public final class AdminDetectorModuleUtil {
   public static final int intVal = -939194869;
   public static final int intVal2 = -267711469;
   public static final int intVal3 = 1610612736;
   public static final int intVal4 = -15658469;
   public static final int intVal5 = -15461086;
   public static final int intVal6 = -15987435;
   public static final int intVal7 = -1644558;
   public static final int intVal8 = -7565402;
   public static final int intVal9 = -14473926;

   private AdminDetectorModuleUtil() {
   }

   public static int getInt() {
      return SwyzzyAddon.val7.getVal().intOf(255);
   }

   public static int getInt2() {
      return intOf2(getInt(), -1, 0.35F);
   }

   public static int getInt3() {
      return intOf2(getInt(), -16382196, 0.55F);
   }

   public static int intOf(int var0, float var1) {
      int var2 = Math.round((var0 >>> 24 & 0xFF) * floatOf2(var1));
      return (var2 & 0xFF) << 24 | var0 & 16777215;
   }

   public static int intOf2(int var0, int var1, float var2) {
      float var3 = floatOf2(var2);
      int var4 = Math.round((var0 >>> 24 & 0xFF) * (1.0F - var3) + (var1 >>> 24 & 0xFF) * var3);
      int var5 = Math.round((var0 >> 16 & 0xFF) * (1.0F - var3) + (var1 >> 16 & 0xFF) * var3);
      int var6 = Math.round((var0 >> 8 & 0xFF) * (1.0F - var3) + (var1 >> 8 & 0xFF) * var3);
      int var7 = Math.round((var0 & 0xFF) * (1.0F - var3) + (var1 & 0xFF) * var3);
      return var4 << 24 | var5 << 16 | var6 << 8 | var7;
   }

   public static float floatOf(float var0, float var1, float var2, float var3) {
      float var4 = 1.0F - (float)Math.exp(-var3 * var2);
      float var5 = var0 + (var1 - var0) * var4;
      return Math.abs(var1 - var5) < 0.001F ? var1 : var5;
   }

   public static float floatOf2(float var0) {
      return var0 < 0.0F ? 0.0F : Math.min(var0, 1.0F);
   }

   public static void run(DrawContext var0, int var1, int var2, int var3, int var4, int var5) {
      if (var3 > 0 && var4 > 0) {
         var0.fill(var1, var2, var1 + var3, var2 + var4, var5);
      }
   }

   public static void run2(DrawContext var0, int var1, int var2, int var3, int var4, int var5, int var6) {
      run3(var0, var1, var2, var3, var4, var5, var5, var6);
   }

   public static void run3(DrawContext var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      if (var3 > 0 && var4 > 0) {
         int var8 = Math.min(var5, Math.min(var3 / 2, var4 / 2));
         int var9 = Math.min(var6, Math.min(var3 / 2, var4 / 2));

         for (int var10 = 0; var10 < var4; var10++) {
            int var11 = 0;
            if (var10 < var8) {
               int var12 = var8 - var10;
               var11 = var8 - (int)Math.round(Math.sqrt(Math.max(0, var8 * var8 - var12 * var12)));
            } else if (var10 >= var4 - var9) {
               int var13 = var10 - (var4 - var9) + 1;
               var11 = var9 - (int)Math.round(Math.sqrt(Math.max(0, var9 * var9 - var13 * var13)));
            }

            var0.fill(var1 + var11, var2 + var10, var1 + var3 - var11, var2 + var10 + 1, var7);
         }
      }
   }

   public static void run4(DrawContext var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      if (var3 > 0 && var4 > 0) {
         int var9 = Math.min(var5, Math.min(var3 / 2, var4 / 2));
         int var10 = Math.min(var6, Math.min(var3 / 2, var4 / 2));

         for (int var11 = 0; var11 < var4; var11++) {
            int var12 = 0;
            if (var11 < var9) {
               int var13 = var9 - var11;
               var12 = var9 - (int)Math.round(Math.sqrt(Math.max(0, var9 * var9 - var13 * var13)));
            } else if (var11 >= var4 - var10) {
               int var14 = var11 - (var4 - var10) + 1;
               var12 = var10 - (int)Math.round(Math.sqrt(Math.max(0, var10 * var10 - var14 * var14)));
            }

            int var15 = intOf2(var7, var8, var4 <= 1 ? 0.0F : (float)var11 / (var4 - 1));
            var0.fill(var1 + var12, var2 + var11, var1 + var3 - var12, var2 + var11 + 1, var15);
         }
      }
   }

   public static void run5(DrawContext var0, int var1, int var2, int var3, int var4, int var5) {
      for (int var6 = var5; var6 >= 1; var6--) {
         int var7 = intOf(-16777216, 0.05F * (var5 - var6 + 1) / var5);
         run2(var0, var1 - var6, var2 - var6, var3 + var6 * 2, var4 + var6 * 2, 5 + var6, var7);
      }
   }
}
