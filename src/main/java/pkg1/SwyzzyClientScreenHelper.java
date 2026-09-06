package pkg1;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class SwyzzyClientScreenHelper {
   public static final int intVal = 128;
   private static final int intVal2 = 20;
   private static final int intVal3 = 17;
   private static final int intVal4 = 15;
   private static final int intVal5 = 7;
   private SwyzzyAddonHelper val;
   private List<Module> list;
   private final Map<Module, InternalData> map = new IdentityHashMap<>();
   private final List<InternalHelper6> list2 = new ArrayList<>();
   private double doubleVal;
   private double doubleVal2;
   private boolean bool = true;
   private float floatVal = 1.0F;
   private float floatVal2;
   private int intVal6;
   private int intVal7;

   public SwyzzyClientScreenHelper(SwyzzyAddonHelper var1, List<Module> var2, double var3, double var5) {
      this.val = var1;
      this.list = var2;
      this.doubleVal = var3;
      this.doubleVal2 = var5;
   }

   public SwyzzyAddonHelper getVal() {
      return this.val;
   }

   public double getDouble() {
      return this.doubleVal;
   }

   public double getDouble2() {
      return this.doubleVal2;
   }

   public void run(double var1, double var3) {
      this.doubleVal = var1;
      this.doubleVal2 = var3;
   }

   public int getInt2() {
      return 20 + this.intVal7;
   }

   public boolean check(double var1, double var3) {
      return var1 >= this.doubleVal && var1 <= this.doubleVal + 128.0 && var3 >= this.doubleVal2 && var3 <= this.doubleVal2 + this.getInt2() + 2.0;
   }

   public boolean check2(double var1, double var3) {
      return var1 >= this.doubleVal && var1 <= this.doubleVal + 128.0 && var3 >= this.doubleVal2 && var3 <= this.doubleVal2 + 20.0;
   }

   public void run2(DrawContext var1, TextRenderer var2, int var3, int var4, float var5, String var6, int var7) {
      this.list2.clear();
      List var8 = this.listOf(var6);
      int var9 = (int)Math.round(this.doubleVal);
      int var10 = (int)Math.round(this.doubleVal2);
      this.floatVal = AdminDetectorModuleUtil.floatOf(this.floatVal, this.bool && !var8.isEmpty() ? 1.0F : 0.0F, var5, 14.0F);
      boolean var11 = this.check2(var3, var4);
      AdminDetectorModuleUtil.run5(var1, var9, var10, 128, 20, 3);
      int var12 = var11 ? AdminDetectorModuleUtil.getInt2() : AdminDetectorModuleUtil.getInt();
      int var13 = AdminDetectorModuleUtil.intOf2(AdminDetectorModuleUtil.getInt(), -12903061, 0.45F);
      AdminDetectorModuleUtil.run4(var1, var9, var10, 128, 20, 5, this.floatVal > 0.02F ? 0 : 5, var12, var13);
      String var14 = this.val.name().toUpperCase(Locale.ROOT);
      MutableText var10002 = Text.literal(var14);
      int var10003 = var9 + (128 - var2.getWidth(var14)) / 2;
      byte var10005 = 20;
      var1.drawText(var2, var10002, var10003, var10 + 5 + 1, -1, false);
      this.list2.add(new InternalHelper6(HEADER.HEADER, var9, var10, 128, 20, null, null));
      int var15 = var10 + 20;
      int var16 = this.intOf(var8, var5);
      int var17 = Math.max(17, var7 - var15 - 34);
      int var18 = Math.min(var16, var17);
      this.intVal6 = var16;
      this.intVal7 = Math.round(var18 * this.floatVal);
      if (this.intVal7 <= 0) {
         this.floatVal2 = 0.0F;
      } else {
         float var19 = Math.max(0, var16 - var18);
         if (this.floatVal2 > var19) {
            this.floatVal2 = var19;
         }

         if (this.floatVal2 < 0.0F) {
            this.floatVal2 = 0.0F;
         }

         AdminDetectorModuleUtil.run3(var1, var9, var15, 128, this.intVal7, 0, 5, -267711469);
         var1.enableScissor(var9, var15, var9 + 128, var15 + this.intVal7);
         int var20 = var15 - Math.round(this.floatVal2);
         int var21 = 0;

         for (Module var23 : (Iterable<Module>)(Object)(var8)) {
            InternalData var24 = this.valOf3(var23);
            boolean var26 = var3 >= var9 && var3 <= var9 + 128 && var4 >= var20 && var4 < var20 + 17 && var4 >= var15 && var4 < var15 + this.intVal7;
            var24.floatVal = AdminDetectorModuleUtil.floatOf(var24.floatVal, var26 ? 1.0F : 0.0F, var5, 16.0F);
            var24.floatVal2 = AdminDetectorModuleUtil.floatOf(var24.floatVal2, var23.isEnabled() ? 1.0F : 0.0F, var5, 13.0F);
            this.run3(var1, var2, var23, var24, var9, var20, var21);
            this.list2.add(new InternalHelper6(HEADER.MODULE, var9, var20, 128, 17, var23, null));
            var20 += 17;
            List var27 = listOf2(var23);
            int var28 = Math.round(var27.size() * 15 * var24.floatVal3);
            if (var28 > 0) {
               AdminDetectorModuleUtil.run(var1, var9, var20, 128, var28, -15987435);
               AdminDetectorModuleUtil.run(var1, var9, var20, 2, var28, AdminDetectorModuleUtil.intOf(AdminDetectorModuleUtil.getInt(), 0.75F));
               var1.enableScissor(var9, var20, var9 + 128, var20 + var28);
               int var29 = var20;

               for (Setting var31 : (Iterable<Setting>)(Object)(var27)) {
                  this.run4(var1, var2, var24, var31, var9, var29, var3, var4, var5);
                  if (var29 + 15 <= var20 + var28) {
                     this.list2.add(new InternalHelper6(HEADER.SETTING, var9, var29, 128, 15, var23, var31));
                  }

                  var29 += 15;
               }

               var1.disableScissor();
               var20 += var28;
            }

            var21++;
         }

         var1.disableScissor();
         if (var19 > 0.0F) {
            int var32 = Math.max(8, Math.round(this.intVal7 * ((float)var18 / var16)));
            int var33 = var15 + Math.round((this.intVal7 - var32) * (this.floatVal2 / var19));
            AdminDetectorModuleUtil.run2(var1, var9 + 128 - 3, var33, 2, var32, 1, AdminDetectorModuleUtil.intOf(AdminDetectorModuleUtil.getInt(), 0.7F));
         }
      }
   }

   private int intOf(List<Module> var1, float var2) {
      int var3 = 0;

      for (Module var5 : var1) {
         InternalData var6 = this.valOf3(var5);
         List var7 = listOf2(var5);
         float var8 = var6.bool && !var7.isEmpty() ? 1.0F : 0.0F;
         var6.floatVal3 = AdminDetectorModuleUtil.floatOf(var6.floatVal3, var8, var2, 12.0F);
         var3 += 17 + Math.round(var7.size() * 15 * var6.floatVal3);
      }

      return var3;
   }

   private void run3(DrawContext var1, TextRenderer var2, Module var3, InternalData var4, int var5, int var6, int var7) {
      int var8 = var7 % 2 == 0 ? -15658469 : -15461086;
      int var9 = AdminDetectorModuleUtil.intOf2(var8, AdminDetectorModuleUtil.intOf2(var8, -14013374, 0.9F), var4.floatVal);
      var9 = AdminDetectorModuleUtil.intOf2(var9, AdminDetectorModuleUtil.getInt3(), var4.floatVal2 * 0.9F);
      AdminDetectorModuleUtil.run(var1, var5, var6, 128, 17, var9);
      int var10 = Math.round(13.0F * Math.max(var4.floatVal2, var4.floatVal * 0.8F));
      if (var10 > 0) {
         AdminDetectorModuleUtil.run(var1, var5, var6 + (17 - var10) / 2, 2, var10, AdminDetectorModuleUtil.getInt());
      }

      String var11 = var3.title == null ? var3.string : var3.title;
      int var12 = AdminDetectorModuleUtil.intOf2(-7565402, -1, Math.max(var4.floatVal2, var4.floatVal * 0.6F));
      byte var10001 = 17;
      int var13 = var6 + 4 + 1;
      String var15 = var11;

      while (var2.getWidth(var15) > 106 && var15.length() > 3) {
         var15 = var15.substring(0, var15.length() - 2) + ".";
      }

      var1.drawText(var2, Text.literal(var15), var5 + 7, var13, var12, false);
      if (!listOf2(var3).isEmpty()) {
         String var16 = var4.bool ? "-" : "+";
         int var17 = AdminDetectorModuleUtil.intOf2(-7565402, AdminDetectorModuleUtil.getInt2(), Math.max(var4.floatVal, var4.floatVal3));
         var1.drawText(var2, Text.literal(var16), var5 + 128 - 7 - var2.getWidth(var16), var13, var17, false);
      }
   }

   private void run4(DrawContext var1, TextRenderer var2, InternalData var3, Setting<?> var4, int var5, int var6, int var7, int var8, float var9) {
      boolean var10 = var7 >= var5 && var7 <= var5 + 128 && var8 >= var6 && var8 < var6 + 15;
      float var11 = var3.map.merge(var4.string, var10 ? 1.0F : 0.0F, (var901, var902) -> SwyzzyClientScreenHelper.floatOf2(var9, var901, var902));
      if (var11 > 0.01F) {
         AdminDetectorModuleUtil.run(var1, var5, var6, 128, 15, AdminDetectorModuleUtil.intOf(AdminDetectorModuleUtil.getInt(), 0.1F * var11));
      }

      String var12 = addSetting(var4.string);
      byte var10001 = 15;
      int var13 = var6 + 3 + 1;
      Object var14 = var4.getObject();
      String var16 = var12;

      while (var2.getWidth(var16) > 74 && var16.length() > 3) {
         var16 = var16.substring(0, var16.length() - 2) + ".";
      }

      var1.drawText(var2, Text.literal(var16), var5 + 7 + 3, var13, -7565402, false);
      if (var14 instanceof Boolean var17) {
         float var20 = var3.map2.merge(var4.string, var17 ? 1.0F : 0.0F, (var901, var902) -> SwyzzyClientScreenHelper.floatOf(var9, var901, var902));
         byte var21 = 18;
         byte var22 = 8;
         int var23 = var5 + 128 - 7 - var21;
         int var24 = var6 + 3;
         AdminDetectorModuleUtil.run2(var1, var23, var24, var21, var22, 4, AdminDetectorModuleUtil.intOf2(-14473926, AdminDetectorModuleUtil.getInt(), var20));
         int var26 = var23 + 1 + Math.round(10 * var20);
         AdminDetectorModuleUtil.run2(var1, var26, var24 + 1, 6, 6, 3, AdminDetectorModuleUtil.intOf2(-9802363, -1, var20));
      } else if (var14 instanceof Number var18) {
         double var31 = doubleOf(var4.getDouble(), 0.0);
         double var35 = doubleOf(var4.getDouble2(), var14 instanceof Integer ? 100.0 : 10.0);
         double var37 = var35 <= var31 ? 0.0 : (var18.doubleValue() - var31) / (var35 - var31);
         String var38 = var14 instanceof Integer ? Integer.toString(var18.intValue()) : String.format(Locale.ROOT, "%.2f", var18.doubleValue());
         var1.drawText(
            var2,
            Text.literal(var38),
            var5 + 128 - 7 - var2.getWidth(var38),
            var13,
            AdminDetectorModuleUtil.intOf2(-1644558, AdminDetectorModuleUtil.getInt2(), var11),
            false
         );
         int var27 = var6 + 15 - 3;
         int var28 = var5 + 7;
         AdminDetectorModuleUtil.run2(var1, var28, var27, 114, 2, 1, -14473926);
         int var30 = (int)Math.round(114 * Math.max(0.0, Math.min(1.0, var37)));
         AdminDetectorModuleUtil.run2(var1, var28, var27, var30, 2, 1, AdminDetectorModuleUtil.getInt());
      } else if (var14 instanceof ActivityChunkFinderModuleHelper4 var19) {
         byte var32 = 9;
         int var34 = var5 + 128 - 7 - var32;
         int var36 = var6 + 3;
         AdminDetectorModuleUtil.run2(var1, var34 - 1, var36 - 1, 11, 11, 3, -16777216);
         AdminDetectorModuleUtil.run2(var1, var34, var36, var32, var32, 2, 0xFF000000 | var19.getInt() & 16777215);
      } else {
         String var33 = stringOf2(var14);

         while (var2.getWidth(var33) > 52 && var33.length() > 4) {
            var33 = var33.substring(0, var33.length() - 2) + ".";
         }

         var1.drawText(
            var2,
            Text.literal(var33),
            var5 + 128 - 7 - var2.getWidth(var33),
            var13,
            AdminDetectorModuleUtil.intOf2(-1644558, AdminDetectorModuleUtil.getInt2(), Math.max(var11, 0.35F)),
            false
         );
      }
   }

   public Setting<?> valOf(double var1, double var3, int var5, SwyzzyClientScreen var6) {
      for (int var7 = this.list2.size() - 1; var7 >= 0; var7--) {
         InternalHelper6 var8 = this.list2.get(var7);
         if (var8.check(var1, var3)) {
            switch (var8.kind()) {
               case HEADER:
                  if (var5 == 1) {
                     this.bool = !this.bool;
                  } else {
                     var6.run2(this, var1 - this.doubleVal, var3 - this.doubleVal2);
                  }

                  return null;
               case MODULE:
                  if (var5 == 1) {
                     InternalData var9 = this.valOf3(var8.module());
                     if (!listOf2(var8.module()).isEmpty()) {
                        var9.bool = !var9.bool;
                     }
                  } else if (var5 == 0) {
                     var8.module().run3();
                  }

                  return null;
               case SETTING:
                  return this.valOf2(var8, var1, var5);
            }
         }
      }

      return null;
   }

   private Setting<?> valOf2(InternalHelper6 var1, double var2, int var4) {
      Setting var5 = var1.setting();
      Object var6 = var5.getObject();
      MinecraftClient var7 = MinecraftClient.getInstance();
      if (var6 instanceof List && (Class)var5.getClass() == Block.class) {
         var7.setScreen(new SelectBlocksScreen(var7.currentScreen, var5));
      } else if (var6 instanceof Boolean var8) {
         var5.run2(!var8);
      } else {
         if (var6 instanceof Number) {
            this.run5(var5, var2);
            return var5;
         }

         if (var6 instanceof Enum var9) {
            Enum[] var10 = (Enum[])var9.getDeclaringClass().getEnumConstants();
            int var11 = var4 == 1 ? -1 : 1;
            var5.run2(var10[Math.floorMod(var9.ordinal() + var11, var10.length)]);
         } else if (var6 instanceof ActivityChunkFinderModuleHelper4) {
            var7.setScreen(new PickNullScreen(var7.currentScreen, var5));
         } else if (var6 instanceof Tab) {
            var7.setScreen(new InternalHelper4(var7.currentScreen, var5));
         } else if (var6 instanceof String) {
            if (var5.string.equals("config-manager")) {
               var7.setScreen(new ConfigManagerScreen(var7.currentScreen));
            } else {
               var7.setScreen(new SaveScreen(var7.currentScreen, var5));
            }
         }
      }

      return null;
   }

   public void run5(Setting<?> var1, double var2) {
      Object var4 = var1.getObject();
      if (var4 instanceof Number) {
         double var5 = (var2 - (this.doubleVal + 7.0)) / Math.max(1.0, 114.0);
         var5 = Math.max(0.0, Math.min(1.0, var5));
         double var7 = doubleOf(var1.getDouble(), 0.0);
         double var9 = doubleOf(var1.getDouble2(), var4 instanceof Integer ? 100.0 : 10.0);
         double var11 = var7 + var5 * (var9 - var7);
         if (var4 instanceof Integer) {
            ((Setting)var1).run2(Math.round(var11));
         } else if (var4 instanceof Float) {
            ((Setting)var1).run2(var11);
         } else {
            ((Setting)var1).run2(var11);
         }
      }
   }

   public boolean check3(double var1, double var3, double var5) {
      if (!this.check(var1, var3)) {
         return false;
      } else {
         float var7 = Math.max(0, this.intVal6 - this.intVal7);
         if (var7 <= 0.0F) {
            return false;
         } else {
            this.floatVal2 = Math.max(0.0F, Math.min(var7, this.floatVal2 - (float)var5 * 17.0F));
            return true;
         }
      }
   }

   private List<Module> listOf(String var1) {
      if (var1 != null && !var1.isBlank()) {
         String var2 = var1.toLowerCase(Locale.ROOT);
         ArrayList var3 = new ArrayList();

         for (Module var5 : this.list) {
            String var6 = var5.title == null ? var5.string : var5.title;
            if (var6.toLowerCase(Locale.ROOT).contains(var2) || var5.string.toLowerCase(Locale.ROOT).contains(var2)) {
               var3.add(var5);
            }
         }

         return var3;
      } else {
         return this.list;
      }
   }

   private InternalData valOf3(Module var1) {
      return this.map.computeIfAbsent(var1, SwyzzyClientScreenHelper::valOf4);
   }

   private static List<Setting<?>> listOf2(Module var0) {
      ArrayList var1 = new ArrayList();

      for (ActivityChunkFinderModuleEntry var3 : var0.val2.getList()) {
         for (Setting var5 : var3.getList()) {
            if (var5.isEnabled()) {
               var1.add(var5);
            }
         }
      }

      return var1;
   }

   private static double doubleOf(Double var0, double var1) {
      return var0 != null && Double.isFinite(var0) && !(Math.abs(var0) > 1.0E9) ? var0 : var1;
   }

   private static String addSetting(String var0) {
      return var0.replace('-', ' ').replace('_', ' ').toUpperCase(Locale.ROOT);
   }

   private static String stringOf2(Object var0) {
      if (var0 instanceof List var4) {
         return var4.isEmpty() ? "NONE" : var4.size() + (var4.stream().allMatch(Block.class::isInstance) ? " BLOCKS" : " ITEMS");
      } else if (var0 instanceof Tab var3) {
         return var3.getString3().toUpperCase(Locale.ROOT);
      } else if (var0 instanceof Enum var2) {
         return addSetting(var2.name());
      } else {
         String var1 = String.valueOf(var0);
         if (var1.isEmpty()) {
            var1 = "...";
         }

         if (var1.length() > 12) {
            var1 = var1.substring(0, 10) + "..";
         }

         return var1.toUpperCase(Locale.ROOT);
      }
   }

   private static InternalData valOf4(Module var0) {
      return new InternalData();
   }

   private static Float floatOf(float var0, Float var1, Float var2) {
      return AdminDetectorModuleUtil.floatOf(var1, var2, var0, 16.0F);
   }

   private static Float floatOf2(float var0, Float var1, Float var2) {
      return AdminDetectorModuleUtil.floatOf(var1, var2, var0, 16.0F);
   }
}
