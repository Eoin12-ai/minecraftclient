package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public final class PlayerRadarModule extends Module {
   private static final int intVal = 18;
   private static final int intVal2 = 18;
   private static final int intVal3 = 12;
   private static final int intVal4 = 8;
   private static final int intVal5 = 14;
   private static final int intVal6 = 2;
   private static final String string_2 = "RADAR";
   private static final long longVal = 3200L;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Appearance");
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Position");
   private final Setting<Boolean> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("show-cardinals").valOf2("Shows north, east, south and west on the radar.").valOf3(true).getVal()
      );
   private final Setting<Boolean> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("look-beam").valOf2("Shows a flashlight beam in the direction you are looking.").valOf3(true).getVal()
      );
   private final Setting<Boolean> val6 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("scan-sweep").valOf2("Shows the rotating radar scan line.").valOf3(true).getVal());
   private final Setting<Boolean> val7 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("show-coordinates")
            .valOf2("Shows the current X, Y and Z coordinates below the radar.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Integer> val8 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("radar-size")
            .valOf2("Diameter of the radar dial in pixels.")
            .valOf3(150)
            .valOf4(80, 320)
            .valOf5(80, 240)
            .getVal()
      );
   private final Setting<Integer> val9 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("player-head-size")
            .valOf2("Size of player skin heads on the radar.")
            .valOf3(12)
            .valOf4(6, 32)
            .valOf5(6, 24)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val10 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("accent-color")
            .valOf2("Accent color of the border, beam, sweep and rings.")
            .valOf3(new ActivityChunkFinderModuleHelper4(180, 100, 255, 255))
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val11 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("background-color")
            .valOf2("Panel background color and transparency.")
            .valOf3(new ActivityChunkFinderModuleHelper4(14, 15, 20, 235))
            .getVal()
      );
   private final Setting<Integer> val12 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("radar-x")
            .valOf2("Horizontal radar position. Open chat to drag it.")
            .valOf3(18)
            .valOf6(0)
            .valOf7(4000)
            .getVal()
      );
   private final Setting<Integer> val13 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("radar-y")
            .valOf2("Vertical radar position. Open chat to drag it.")
            .valOf3(18)
            .valOf6(0)
            .valOf7(4000)
            .getVal()
      );
   private int intVal7;

   public PlayerRadarModule() {
      super(SwyzzyAddon.val2, "player-radar", "Shows nearby players as skin heads on a radar.");
   }

   private int getInt() {
      return 17;
   }

   private int getInt2() {
      return ((Boolean)this.val7.getObject()) ? 17 : 0;
   }

   public int getInt3() {
      int var1 = this.intOf("RADAR") + 10 + this.intOf2(this.intVal7);
      return Math.max(this.val8.getObject(), var1) + 24;
   }

   public int getInt5() {
      return 12 + this.getInt() + 8 + this.val8.getObject() + this.getInt2() + 12;
   }

   @InternalHelper5
   private void run(AdminDetectorModuleData var1) {
      if (class310.player != null && class310.world != null && class310.getWindow() != null) {
         List var2 = this.getList();
         this.intVal7 = var2.size();
         int var3 = this.getInt3();
         int var4 = this.getInt5();
         int var5 = MathHelper.clamp(this.val12.getObject(), 0, Math.max(0, class310.getWindow().getScaledWidth() - var3));
         int var6 = MathHelper.clamp(this.val13.getObject(), 0, Math.max(0, class310.getWindow().getScaledHeight() - var4));
         int var7 = this.val8.getObject() / 2;
         int var8 = var5 + var3 / 2;
         int var9 = var6 + 12 + this.getInt() + 8 + var7;
         DrawContext var10 = var1.class332;
         this.run9(var10, var5, var6, var3, var4);
         this.run2(var10, var5, var6, var3, var2.size());
         this.run3(var10, var8, var9, var7);
         this.run8(var10, var8, var9, var7, var2);
         if (this.val7.getObject()) {
            this.run10(var10, var8, var9 + var7 + 6);
         }
      }
   }

   private void run9(DrawContext var1, int var2, int var3, int var4, int var5) {
      ActivityChunkFinderModuleHelper4 var6 = this.val11.getObject();
      int var7 = intOf3(var6.intVal, var6.intVal2, var6.intVal3, var6.intVal4);
      int var8 = this.getInt4();
      run15(var1, var2 - 1, var3 - 1, var4 + 2, var5 + 2, 15, AdminDetectorModuleUtil.intOf(var8, 0.45F));
      run15(var1, var2, var3, var4, var5, 14, var7);
   }

   private void run2(DrawContext var1, int var2, int var3, int var4, int var5) {
      int var6 = this.getInt();
      int var7 = var3 + 12 + (var6 - 9) / 2;
      this.run11(var1, var2 + 12, var7, -4604215);
      String var8 = var5 + " nearby";
      int var9 = class310.textRenderer.getWidth(var8);
      int var10 = this.intOf2(var5);
      Objects.requireNonNull(class310.textRenderer);
      int var12 = var2 + var4 - 12 - var10;
      int var13 = var3 + 12 + (var6 - 14) / 2;
      AdminDetectorModuleUtil.run2(var1, var12, var13, var10, 14, 7, AdminDetectorModuleUtil.intOf2(-14671316, this.getInt4(), 0.22F));
      TextRenderer var10001 = class310.textRenderer;
      MutableText var10002 = Text.literal(var8);
      int var10003 = var12 + (var10 - var9) / 2;
      byte var10005 = 14;
      var1.drawText(var10001, var10002, var10003, var13 + 2 + 1, -1447182, false);
   }

   private void run3(DrawContext var1, int var2, int var3, int var4) {
      int var5 = this.getInt4();
      int var6 = AdminDetectorModuleUtil.intOf(-16447990, 0.85F);
      int var7 = AdminDetectorModuleUtil.intOf(var5, 0.28F);
      int var8 = AdminDetectorModuleUtil.intOf(var5, 0.16F);
      run14(var1, var2, var3, var4, AdminDetectorModuleUtil.intOf(var5, 0.55F));
      run14(var1, var2, var3, Math.max(1, var4 - 2), var6);
      run13(var1, var2, var3, var4 * 2 / 3, var7);
      var1.fill(var2, var3 - var4 + 3, var2 + 1, var3 + var4 - 2, var8);
      var1.fill(var2 - var4 + 3, var3, var2 + var4 - 2, var3 + 1, var8);
      if (this.val5.getObject()) {
         this.run4(var1, var2, var3, var4);
      }

      if (this.val6.getObject()) {
         this.run5(var1, var2, var3, var4);
      }

      run14(var1, var2, var3, 3, var5);
      if (this.val4.getObject()) {
         this.run6(var1, var2, var3, var4);
      }
   }

   private void run4(DrawContext var1, int var2, int var3, int var4) {
      double var5 = Math.tan(Math.toRadians(20.0));
      int var7 = var4 - 2;

      for (byte var10 = 1; var10 <= var7; var10 += 3) {
         int var11 = (int)Math.round(var10 * var5);
         int var12 = (int)Math.sqrt(Math.max(0, var7 * var7 - var10 * var10));
         if (var11 > var12) {
            var11 = var12;
         }

         int var13 = (int)Math.round(70 * (1.0 - (double)var10 / var4));
         if (var13 > 0) {
            int var14 = 16777215 | var13 << 24;
            var1.fill(var2 - var11, var3 - var10, var2 + var11 + 1, var3 - var10 + 3, var14);
         }
      }
   }

   private void run5(DrawContext var1, int var2, int var3, int var4) {
      int var5 = this.getInt4();
      double var6 = System.currentTimeMillis() % 3200L / 3200.0 * (Math.PI * 2);
      int var8 = var4 - 2;
      double var10 = Math.toRadians(5.0);

      for (int var12 = 10; var12 >= 1; var12--) {
         double var13 = var6 - var12 * var10;
         float var15 = 1.0F - (float)var12 / 10;
         int var16 = (int)(110.0F * var15 * var15);
         if (var16 > 0) {
            run12(var1, var2, var3, var13, 2, var8, var5 & 16777215 | var16 << 24, 4, 4);
         }
      }

      run12(var1, var2, var3, var6, 2, var8, AdminDetectorModuleUtil.intOf2(var5, -1, 0.4F), 2, 2);
   }

   private void run6(DrawContext var1, int var2, int var3, int var4) {
      int var5 = AdminDetectorModuleUtil.intOf(-2235668, 0.9F);
      this.run7(var1, "N", 0.0, -1.0, var2, var3, var4, var5);
      this.run7(var1, "E", 1.0, 0.0, var2, var3, var4, var5);
      this.run7(var1, "S", 0.0, 1.0, var2, var3, var4, var5);
      this.run7(var1, "W", -1.0, 0.0, var2, var3, var4, var5);
   }

   private void run7(DrawContext var1, String var2, double var3, double var5, int var7, int var8, int var9, int var10) {
      PlayerRadarModule.Inner1 var11 = this.valOf(var3, var5, var9 - 9);
      int var12 = var7 + (int)Math.round(var11.getDouble()) - class310.textRenderer.getWidth(var2) / 2;
      int var13 = var8 + (int)Math.round(var11.getDouble2()) - 4;
      var1.drawText(class310.textRenderer, Text.literal(var2), var12, var13, var10, true);
   }

   private List<AbstractClientPlayerEntity> getList() {
      double var1 = Math.max(16.0, ((Integer)class310.options.getViewDistance().getValue()).intValue() * 16.0);
      double var3 = var1 * var1;
      return class310.world
         .getPlayers()
         .stream()
         .filter(PlayerRadarModule::check2)
         .filter(var905 -> this.check(var3, var905))
         .sorted(Comparator.comparingDouble(this::doubleOf).reversed())
         .toList();
   }

   private void run8(DrawContext var1, int var2, int var3, int var4, List<AbstractClientPlayerEntity> var5) {
      double var6 = Math.max(16.0, ((Integer)class310.options.getViewDistance().getValue()).intValue() * 16.0);
      int var8 = this.val9.getObject();
      int var9 = Math.max(1, var4 - var8 / 2 - 3);
      int var10 = this.getInt4();
      AbstractClientPlayerEntity var11 = var5.isEmpty() ? null : (AbstractClientPlayerEntity)var5.get(var5.size() - 1);

      for (AbstractClientPlayerEntity var13 : var5) {
         double var14 = var13.getX() - class310.player.getX();
         double var16 = var13.getZ() - class310.player.getZ();
         double var18 = Math.sqrt(var14 * var14 + var16 * var16);
         double var20 = var9 / var6;
         PlayerRadarModule.Inner1 var22 = this.valOf(var14, var16, Math.min((double)var9, var18 * var20));
         int var23 = var2 + (int)Math.round(var22.getDouble()) - var8 / 2;
         int var24 = var3 + (int)Math.round(var22.getDouble2()) - var8 / 2;
         if (var13 == var11) {
            run13(var1, var23 + var8 / 2, var24 + var8 / 2, var8 / 2 + 3, var10);
            run13(var1, var23 + var8 / 2, var24 + var8 / 2, var8 / 2 + 2, AdminDetectorModuleUtil.intOf(var10, 0.5F));
         }

         var1.fill(var23 - 1, var24 - 1, var23 + var8 + 1, var24 + var8 + 1, -586544368);
         PlayerSkinDrawer.draw(var1, var13.getSkin(), var23, var24, var8);
      }
   }

   private void run10(DrawContext var1, int var2, int var3) {
      String var4 = String.format(
         "X %d  Y %d  Z %d", MathHelper.floor(class310.player.getX()), MathHelper.floor(class310.player.getY()), MathHelper.floor(class310.player.getZ())
      );
      int var5 = class310.textRenderer.getWidth(var4);
      int var6 = var2 - var5 / 2;
      var1.drawText(class310.textRenderer, Text.literal(var4), var6, var3, -3551784, false);
   }

   private PlayerRadarModule.Inner1 valOf(double var1, double var3, double var5) {
      double var7 = Math.sqrt(var1 * var1 + var3 * var3);
      if (var7 < 1.0E-4) {
         new Inner1(0.0, 0.0);
      } else {
         double var9 = Math.toRadians(class310.player.getYaw());
         double var11 = var1 / var7;
         double var13 = var3 / var7;
         double var15 = -var11 * Math.cos(var9) - var13 * Math.sin(var9);
         double var17 = var11 * Math.sin(var9) - var13 * Math.cos(var9);
         return new Inner1(var15 * var5, var17 * var5);
      }
      return null;
   }

   private double doubleOf(AbstractClientPlayerEntity var1) {
      double var2 = var1.getX() - class310.player.getX();
      double var4 = var1.getZ() - class310.player.getZ();
      return var2 * var2 + var4 * var4;
   }

   private void run11(DrawContext var1, int var2, int var3, int var4) {
      int var5 = var2;

      for (int var6 = 0; var6 < 5; var6++) {
         String var7 = String.valueOf("RADAR".charAt(var6));
         var1.drawText(class310.textRenderer, Text.literal(var7), var5, var3, var4, false);
         var5 += class310.textRenderer.getWidth(var7) + 2;
      }
   }

   private int intOf(String var1) {
      int var2 = 0;

      for (int var3 = 0; var3 < var1.length(); var3++) {
         var2 += class310.textRenderer.getWidth(String.valueOf(var1.charAt(var3))) + 2;
      }

      return var2 - 2;
   }

   private int intOf2(int var1) {
      return class310.textRenderer.getWidth(var1 + " nearby") + 12;
   }

   private int getInt4() {
      ActivityChunkFinderModuleHelper4 var1 = this.val10.getObject();
      return intOf3(var1.intVal, var1.intVal2, var1.intVal3, var1.intVal4);
   }

   private static void run12(DrawContext var0, int var1, int var2, double var3, int var5, int var6, int var7, int var8, int var9) {
      double var10 = Math.cos(var3);
      double var12 = Math.sin(var3);
      int var14 = var5;

      while (var14 <= var6) {
         int var15 = var1 + (int)Math.round(var10 * var14);
         int var16 = var2 + (int)Math.round(var12 * var14);
         var0.fill(var15 - var8 / 2, var16 - var8 / 2, var15 - var8 / 2 + var8, var16 - var8 / 2 + var8, var7);
         var14 += var9;
      }
   }

   private static void run13(DrawContext var0, int var1, int var2, int var3, int var4) {
      if (var3 > 0) {
         int var5 = Math.min(96, Math.max(24, (int)Math.round((Math.PI * 2) * var3)));
         double var6 = (Math.PI * 2) / var5;

         for (int var8 = 0; var8 < var5; var8++) {
            double var9 = var8 * var6;
            int var11 = var1 + (int)Math.round(Math.cos(var9) * var3);
            int var12 = var2 + (int)Math.round(Math.sin(var9) * var3);
            var0.fill(var11, var12, var11 + 1, var12 + 1, var4);
         }
      }
   }

   private static void run14(DrawContext var0, int var1, int var2, int var3, int var4) {
      for (int var5 = -var3; var5 <= var3; var5++) {
         int var6 = (int)Math.round(Math.sqrt((double)var3 * var3 - (double)var5 * var5));
         var0.fill(var1 - var6, var2 + var5, var1 + var6 + 1, var2 + var5 + 1, var4);
      }
   }

   private static void run15(DrawContext var0, int var1, int var2, int var3, int var4, int var5, int var6) {
      if (var3 > 0 && var4 > 0) {
         int var7 = Math.min(var5, Math.min(var3 / 2, var4 / 2));
         var0.fill(var1, var2 + var7, var1 + var3, var2 + var4 - var7, var6);

         for (int var9 = 0; var9 < 5; var9++) {
            int var10 = var9 * var7 / 5;
            int var11 = (var9 + 1) * var7 / 5;
            int var12 = var7 - var10;
            int var13 = var7 - (int)Math.round(Math.sqrt(Math.max(0.0, (double)var7 * var7 - (double)var12 * var12)));
            var0.fill(var1 + var13, var2 + var10, var1 + var3 - var13, var2 + var11, var6);
            var0.fill(var1 + var13, var2 + var4 - var11, var1 + var3 - var13, var2 + var4 - var10, var6);
         }
      }
   }

   private static int intOf3(int var0, int var1, int var2, int var3) {
      return var3 << 24 | var0 << 16 | var1 << 8 | var2;
   }

   private boolean check(double var1, AbstractClientPlayerEntity var3) {
      return this.doubleOf(var3) <= var1;
   }

   private static boolean check2(AbstractClientPlayerEntity var0) {
      return var0 != class310.player && !var0.isRemoved();
   }

   final class Inner1 {
      private double doubleVal;
      private double doubleVal2;

      Inner1(double var1, double var3) {
         this.doubleVal = var1;
         this.doubleVal2 = var3;
      }

      public double getDouble() {
         return this.doubleVal;
      }

      public double getDouble2() {
         return this.doubleVal2;
      }
   }
}
