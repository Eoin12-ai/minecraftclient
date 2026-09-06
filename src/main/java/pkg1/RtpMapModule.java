package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import util.Utils_2;

public final class RtpMapModule extends Module {
   private static final int intVal = 9;
   private static final double doubleVal = 50000.0;
   private static final double doubleVal2 = 225000.0;
   private static final int[] intArray = new int[]{
      82,
      100,
      101,
      102,
      103,
      104,
      105,
      106,
      91,
      83,
      44,
      75,
      42,
      41,
      40,
      39,
      38,
      92,
      84,
      45,
      14,
      13,
      12,
      11,
      10,
      37,
      93,
      85,
      46,
      74,
      3,
      2,
      1,
      25,
      36,
      94,
      86,
      47,
      72,
      71,
      5,
      4,
      24,
      35,
      95,
      87,
      51,
      17,
      9,
      8,
      7,
      23,
      34,
      96,
      88,
      54,
      18,
      61,
      62,
      21,
      22,
      33,
      97,
      89,
      26,
      27,
      28,
      29,
      30,
      59,
      32,
      98,
      90,
      107,
      108,
      109,
      110,
      111,
      112,
      113,
      99
   };
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.valOf("Display");
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Position");
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Visual");
   private final ActivityChunkFinderModuleEntry val4 = this.val2.valOf("Theme");
   private final Setting<Integer> val5 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("position-x")
            .valOf2("Horizontal position of the map. Open chat to drag it.")
            .valOf3(15)
            .valOf6(0)
            .valOf7(1920)
            .getVal()
      );
   private final Setting<Integer> val6 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("position-y")
            .valOf2("Vertical position of the map. Open chat to drag it.")
            .valOf3(15)
            .valOf6(0)
            .valOf7(1080)
            .getVal()
      );
   private final Setting<Integer> val7 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("cell-size")
            .valOf2("Pixel size of each 50k x 50k cell.")
            .valOf3(26)
            .valOf4(14, 56)
            .valOf5(14, 56)
            .getVal()
      );
   private final Setting<Boolean> val8 = this.valOf("enable-coordinates", "Shows the player X and Z position below the map.", true);
   private final Setting<Boolean> val9 = this.valOf("enable-legend", "Shows the glitched and media spot legend.", true);
   private final Setting<Boolean> val10 = this.valOf("enable-region-numbers", "Draws region IDs inside the map cells.", true);
   private final Setting<Boolean> val11 = this.valOf("enable-grid", "Draws the 50k grid lines.", true);
   private final Setting<Boolean> val12 = this.valOf("enable-player-indicator", "Shows the player position and facing direction.", true);
   private final Setting<Boolean> val13 = this.valOf("enable-distance-rings", "Draws subtle distance rings around spawn.", false);
   private final Setting<Boolean> val14 = this.valOf("enable-spot-zones", "Overlays the glitched and media spot cells.", true);
   private final Setting<Double> val15 = this.val3_2
      .addSetting(
         new AutoTotemModuleHelper2().valOf("panel-transparency").valOf2("Map panel transparency.").valOf3(0.68).valOf4(0.15, 1.0).valOf5(0.15, 1.0).getVal()
      );
   private final Setting<Double> val16 = this.val3_2
      .addSetting(new AutoTotemModuleHelper2().valOf("spot-opacity").valOf2("Spot overlay opacity.").valOf3(0.86).valOf4(0.2, 1.0).valOf5(0.2, 1.0).getVal());
   private final Setting<Double> val17 = this.val3_2
      .addSetting(
         new AutoTotemModuleHelper2().valOf("number-size").valOf2("Region number size and visibility.").valOf3(0.78).valOf4(0.4, 1.6).valOf5(0.4, 1.6).getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val18 = this.valOf2(
      "background-color", "Map panel color.", new ActivityChunkFinderModuleHelper4(10, 12, 18, 165)
   );
   private final Setting<ActivityChunkFinderModuleHelper4> val19 = this.valOf2(
      "player-color", "Player arrow color.", new ActivityChunkFinderModuleHelper4(255, 64, 72, 255)
   );
   private final Setting<ActivityChunkFinderModuleHelper4> val20 = this.valOf2(
      "grid-color", "Grid line color.", new ActivityChunkFinderModuleHelper4(58, 66, 82, 210)
   );
   private final Setting<ActivityChunkFinderModuleHelper4> val21 = this.valOf2(
      "number-color", "Region number color.", new ActivityChunkFinderModuleHelper4(236, 240, 248, 235)
   );
   private final List<RtpMapModule.Inner1> list = getList();

   public RtpMapModule() {
      super(SwyzzyAddon.val2, "rtp-map", "Shows the 50k RTP region map and known spot zones.");
      Utils_2.run(this, "Rtp Map");
   }

   @InternalHelper5
   private void run(AdminDetectorModuleData var1) {
      if (class310.player != null && class310.world != null && class310.getWindow() != null) {
         int var2 = this.val7.getObject();
         int var3 = var2 * 9;
         int var4 = (((Boolean)this.val8.getObject()) ? 15 : 0) + (((Boolean)this.val9.getObject()) ? 40 : 0);
         int var5 = MathHelper.clamp(this.val5.getObject(), 0, Math.max(0, class310.getWindow().getScaledWidth() - var3));
         int var6 = MathHelper.clamp(this.val6.getObject(), 0, Math.max(0, class310.getWindow().getScaledHeight() - var3 - var4));
         if (var5 != this.val5.getObject()) {
            this.val5.run2(var5);
         }

         if (var6 != this.val6.getObject()) {
            this.val6.run2(var6);
         }

         DrawContext var7 = var1.class332;
         var7.fill(var5, var6, var5 + var3, var6 + var3, intOf3(this.val18.getObject(), this.val15.getObject()));
         if (this.val13.getObject()) {
            this.run5(var7, var5, var6, var2);
         }

         if (this.val14.getObject()) {
            this.run2(var7, var5, var6, var2);
         }

         if (this.val11.getObject()) {
            this.run3(var7, var5, var6, var2);
         }

         if (this.val10.getObject()) {
            this.run4(var7, var5, var6, var2);
         }

         if (this.val12.getObject()) {
            this.run6(var7, var5, var6, var2);
         }

         this.run7(var7, var5, var6 + var3 + 4);
      }
   }

   private Setting<Boolean> valOf(String var1, String var2, boolean var3) {
      return this.val_2.addSetting(new ActivityChunkFinderModuleHelper8().valOf(var1).valOf2(var2).valOf3(var3).getVal());
   }

   private Setting<ActivityChunkFinderModuleHelper4> valOf2(String var1, String var2, ActivityChunkFinderModuleHelper4 var3) {
      return this.val4.addSetting(new ActivityChunkFinderModuleHelper10().valOf(var1).valOf2(var2).valOf3(var3).getVal());
   }

   private void run2(DrawContext var1, int var2, int var3, int var4) {
      for (RtpMapModule.Inner1 var6 : this.list) {
         int var7 = var2 + var6.column() * var4 + 1;
         int var8 = var3 + var6.row() * var4 + 1;
         var1.fill(var7, var8, var7 + var4 - 1, var8 + var4 - 1, intOf4(var6.color(), this.val16.getObject()));
      }
   }

   private void run3(DrawContext var1, int var2, int var3, int var4) {
      int var5 = var4 * 9;
      int var6 = intOf2(this.val20.getObject());

      for (int var7 = 0; var7 <= 9; var7++) {
         int var8 = var2 + var7 * var4;
         int var9 = var3 + var7 * var4;
         var1.fill(var8, var3, var8 + 1, var3 + var5, var6);
         var1.fill(var2, var9, var2 + var5, var9 + 1, var6);
      }
   }

   private void run4(DrawContext var1, int var2, int var3, int var4) {
      int var5 = intOf2(this.val21.getObject());
      boolean var6 = var4 < 18 && ((Double)this.val17.getObject()) < 0.7;

      for (int var7 = 0; var7 < 9; var7++) {
         for (int var8 = 0; var8 < 9; var8++) {
            String var9 = String.valueOf(intArray[var7 * 9 + var8]);
            if (!var6 || var9.length() <= 2) {
               int var10 = var2 + var8 * var4 + (var4 - class310.textRenderer.getWidth(var9)) / 2;
               int var11 = var3 + var7 * var4 + (var4 - 9) / 2 + 1;
               var1.drawText(class310.textRenderer, Text.literal(var9), var10, var11, var5, true);
            }
         }
      }
   }

   private void run5(DrawContext var1, int var2, int var3, int var4) {
      double var5 = var2 + 9 * var4 / 2.0;
      double var7 = var3 + 9 * var4 / 2.0;

      for (int var10 = 1; var10 <= 9; var10++) {
         double var11 = var10 * var4 / 2.0;
         double var13 = var5 + var11;
         double var15 = var7;

         for (int var17 = 1; var17 <= 64; var17++) {
            double var18 = (Math.PI * 2) * var17 / 64.0;
            double var20 = var5 + Math.cos(var18) * var11;
            double var22 = var7 + Math.sin(var18) * var11;
            run9(var1, var13, var15, var20, var22, 1181643908, var2, var3, var2 + 9 * var4, var3 + 9 * var4);
            var13 = var20;
            var15 = var22;
         }
      }
   }

   private void run6(DrawContext var1, int var2, int var3, int var4) {
      double var5 = (class310.player.getX() + 225000.0) / 50000.0;
      double var7 = (class310.player.getZ() + 225000.0) / 50000.0;
      if (!(var5 < 0.0) && !(var5 >= 9.0) && !(var7 < 0.0) && !(var7 >= 9.0)) {
         int var9 = var2 + (int)Math.round(var5 * var4);
         int var10 = var3 + (int)Math.round(var7 * var4);
         double var11 = Math.toRadians(class310.player.getYaw() - 90.0F);
         int var13 = Math.max(5, Math.min(9, var4 / 3));
         int var14 = var9 + (int)Math.round(Math.cos(var11) * var13);
         int var15 = var10 + (int)Math.round(Math.sin(var11) * var13);
         int var16 = var9 + (int)Math.round(Math.cos(var11 + 2.45) * var13);
         int var17 = var10 + (int)Math.round(Math.sin(var11 + 2.45) * var13);
         int var18 = var9 + (int)Math.round(Math.cos(var11 - 2.45) * var13);
         int var19 = var10 + (int)Math.round(Math.sin(var11 - 2.45) * var13);
         run10(var1, var14, var15, var16, var17, var18, var19, intOf2(this.val19.getObject()));
      }
   }

   private void run7(DrawContext var1, int var2, int var3) {
      if (this.val8.getObject()) {
         String var4 = String.format("X %d  Z %d", MathHelper.floor(class310.player.getX()), MathHelper.floor(class310.player.getZ()));
         var1.drawText(class310.textRenderer, Text.literal(var4), var2, var3, -986377, true);
         var3 += 15;
      }

      if (this.val9.getObject()) {
         this.run8(var1, var2, var3, -340103425, "Glitched / no RTP spots");
         this.run8(var1, var2, var3 + 12, -348091137, "Media / baltop");
         this.run8(var1, var2, var3 + 24, -339686435, "Media / other bases");
      }
   }

   private void run8(DrawContext var1, int var2, int var3, int var4, String var5) {
      var1.fill(var2, var3 + 1, var2 + 9, var3 + 10, var4);
      var1.drawText(class310.textRenderer, Text.literal(var5), var2 + 13, var3 + 1, -2038289, false);
   }

   private static void run9(DrawContext var0, double var1, double var3, double var5, double var7, int var9, int var10, int var11, int var12, int var13) {
      int var14 = Math.max(1, (int)Math.ceil(Math.max(Math.abs(var5 - var1), Math.abs(var7 - var3))));

      for (int var15 = 0; var15 <= var14; var15++) {
         double var16 = (double)var15 / var14;
         int var18 = (int)Math.round(var1 + (var5 - var1) * var16);
         int var19 = (int)Math.round(var3 + (var7 - var3) * var16);
         if (var18 >= var10 && var18 < var12 && var19 >= var11 && var19 < var13) {
            var0.fill(var18, var19, var18 + 1, var19 + 1, var9);
         }
      }
   }

   private static void run10(DrawContext var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      int var8 = Math.min(var2, Math.min(var4, var6));
      int var9 = Math.max(var2, Math.max(var4, var6));

      for (int var10 = var8; var10 <= var9; var10++) {
         int var11 = Integer.MAX_VALUE;
         int var12 = Integer.MIN_VALUE;
         int[] var13 = new int[]{intOf(var1, var2, var3, var4, var10), intOf(var3, var4, var5, var6, var10), intOf(var5, var6, var1, var2, var10)};

         for (int var17 : var13) {
            if (var17 != Integer.MAX_VALUE) {
               var11 = Math.min(var11, var17);
               var12 = Math.max(var12, var17);
            }
         }

         if (var11 <= var12 && var11 != Integer.MAX_VALUE) {
            var0.fill(var11, var10, var12 + 1, var10 + 1, var7);
         }
      }
   }

   private static int intOf(int var0, int var1, int var2, int var3, int var4) {
      if (var4 < Math.min(var1, var3) || var4 > Math.max(var1, var3)) {
         return Integer.MAX_VALUE;
      } else {
         return var1 == var3 ? (var0 + var2) / 2 : var0 + (var2 - var0) * (var4 - var1) / (var3 - var1);
      }
   }

   private static int intOf2(ActivityChunkFinderModuleHelper4 var0) {
      return (var0.intVal4 & 0xFF) << 24 | (var0.intVal & 0xFF) << 16 | (var0.intVal2 & 0xFF) << 8 | var0.intVal3 & 0xFF;
   }

   private static int intOf3(ActivityChunkFinderModuleHelper4 var0, double var1) {
      int var3 = MathHelper.clamp((int)Math.round(var0.intVal4 * var1), 0, 255);
      return var3 << 24 | (var0.intVal & 0xFF) << 16 | (var0.intVal2 & 0xFF) << 8 | var0.intVal3 & 0xFF;
   }

   private static int intOf4(int var0, double var1) {
      int var3 = MathHelper.clamp((int)Math.round((var0 >>> 24) * var1), 0, 255);
      return var3 << 24 | var0 & 16777215;
   }

   private static List<RtpMapModule.Inner1> getList() {
      ArrayList var0 = new ArrayList();
      run11(
         var0,
         -843419905,
         new double[][]{{-200000.0, 200000.0}, {-200000.0, 150000.0}, {-100000.0, -200000.0}, {0.0, -200000.0}, {50000.0, -200000.0}, {200000.0, 100000.0}}
      );
      run11(var0, -683635457, new double[][]{{0.0, 0.0}, {50000.0, 0.0}, {0.0, 50000.0}, {50000.0, 150000.0}});
      run11(
         var0,
         -1011827504,
         new double[][]{
            {-200000.0, 0.0},
            {-200000.0, 100000.0},
            {0.0, -50000.0},
            {50000.0, 50000.0},
            {150000.0, 50000.0},
            {-50000.0, 150000.0},
            {0.0, 150000.0},
            {100000.0, 150000.0},
            {150000.0, 150000.0},
            {200000.0, 150000.0},
            {-50000.0, 200000.0},
            {100000.0, 200000.0},
            {150000.0, 200000.0},
            {200000.0, 200000.0}
         }
      );
      return List.copyOf(var0);
   }

   private static void run11(List<RtpMapModule.Inner1> var0, int var1, double[][] var2) {
      for (double[] var6 : var2) {
         int var7 = (int)Math.floor((var6[0] + 225000.0) / 50000.0);
         int var8 = (int)Math.floor((var6[1] + 225000.0) / 50000.0);
         if (var7 >= 0 && var7 < 9 && var8 >= 0 && var8 < 9) {
            var0.removeIf(var905 -> RtpMapModule.check(var8, var8, var905));
            var0.add(new Inner1(var7, var8, var1));
         }
      }
   }

   private static boolean check(int var0, int var1, RtpMapModule.Inner1 var2) {
      return var2.column() == var0 && var2.row() == var1;
   }

   final static class Inner1 {
      private int column;
      private int row;
      private int color;

      Inner1(int var1, int var2, int var3) {
         this.column = var1;
         this.row = var2;
         this.color = var3;
      }

      public int column() {
         return this.column;
      }

      public int row() {
         return this.row;
      }

      public int color() {
         return this.color;
      }
   }
}
