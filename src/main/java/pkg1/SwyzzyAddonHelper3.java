package pkg1;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public final class SwyzzyAddonHelper3 {
   private static final int intVal = 14;
   private static final int intVal2 = -1;
   private final MinecraftClient class310 = MinecraftClient.getInstance();
   private Runnable runnable;
   private InternalHelper7 val;
   private double doubleVal;
   private double doubleVal2;
   private boolean bool;

   public SwyzzyAddonHelper3(Runnable var1) {
      this.runnable = var1;
   }

   public void run(AdminDetectorModuleData var1) {
      if (this.class310.currentScreen instanceof ChatScreen && this.class310.getWindow() != null) {
         double var2 = this.class310.mouse.getX() * this.class310.getWindow().getScaledWidth() / this.class310.getWindow().getWidth();
         double var4 = this.class310.mouse.getY() * this.class310.getWindow().getScaledHeight() / this.class310.getWindow().getHeight();
         boolean var6 = GLFW.glfwGetMouseButton(this.class310.getWindow().getHandle(), 0) == 1;
         List var7 = this.getList();
         boolean var8 = this.val != null;
         if (var6 && !this.bool) {
            this.run2(var7, var2, var4);
         }

         if (var6 && this.val != null) {
            this.run3(var2, var4);
         }

         if (!var6) {
            if (var8 && this.runnable != null) {
               this.runnable.run();
            }

            this.val = null;
         }

         this.bool = var6;

         for (InternalHelper7 var10 : (Iterable<InternalHelper7>)(Object)(var7)) {
            this.run4(var1, var10);
         }
      } else {
         this.val = null;
         this.bool = false;
      }
   }

   private void run2(List<InternalHelper7> var1, double var2, double var4) {
      for (int var6 = var1.size() - 1; var6 >= 0; var6--) {
         InternalHelper7 var7 = (InternalHelper7)var1.get(var6);
         if (var7.check(var2, var4)) {
            this.val = var7;
            this.doubleVal = var2 - var7.getInt();
            this.doubleVal2 = var4 - var7.getInt2();
            return;
         }
      }
   }

   private void run3(double var1, double var3) {
      int var5 = Math.max(0, this.class310.getWindow().getScaledWidth() - this.val.width());
      int var6 = Math.max(0, this.class310.getWindow().getScaledHeight() - this.val.height());
      int var7 = MathHelper.clamp((int)Math.round(var1 - this.doubleVal), 0, var5);
      int var8 = MathHelper.clamp((int)Math.round(var3 - this.doubleVal2), 14, var6);
      if (this.val.windowPixels()) {
         double var9 = (double)this.class310.getWindow().getScaledWidth() / this.class310.getWindow().getWidth();
         double var11 = (double)this.class310.getWindow().getScaledHeight() / this.class310.getWindow().getHeight();
         int var13 = (int)Math.round(var7 / var9);
         int var14 = (int)Math.round(var8 / var11);
         int var15 = (int)Math.ceil(14.0 / var11);
         int var16 = Math.max(0, this.class310.getWindow().getWidth() - this.val.rawWidth());
         int var17 = Math.max(var15, this.class310.getWindow().getHeight() - this.val.rawHeight());
         this.val.xSetting().run2(MathHelper.clamp(var13, 0, var16));
         this.val.ySetting().run2(MathHelper.clamp(var14, var15, var17));
      } else {
         this.val.xSetting().run2(var7);
         this.val.ySetting().run2(var8);
      }
   }

   private void run4(AdminDetectorModuleData var1, InternalHelper7 var2) {
      int var3 = var2.getInt();
      int var4 = Math.max(0, var2.getInt2() - 14);
      boolean var5 = this.val != null && this.val.check2(var2);
      int var6 = var5 ? AdminDetectorModuleUtil.getInt2() : AdminDetectorModuleUtil.intOf(AdminDetectorModuleUtil.getInt(), 0.85F);
      AdminDetectorModuleUtil.run3(var1.class332, var3, var4, var2.width(), 14, 4, 0, var6);
      AdminDetectorModuleUtil.run(var1.class332, var3, var2.getInt2(), var2.width(), 1, AdminDetectorModuleUtil.intOf(var6, 0.5F));
      AdminDetectorModuleUtil.run(var1.class332, var3, var2.getInt2() + var2.height() - 1, var2.width(), 1, AdminDetectorModuleUtil.intOf(var6, 0.5F));
      var1.class332.drawText(this.class310.textRenderer, var2.label(), var3 + 5, var4 + 3, -1, false);
   }

   private List<InternalHelper7> getList() {
      ArrayList var1 = new ArrayList(4);

      for (Module var3 : new ArrayList<>(FakeRankModuleHelper.getVal().getCollection())) {
         if (var3 instanceof RtpMapModule) {
            this.run5(var1, var3, "Drag RTP Map");
         } else if (var3 instanceof AdminDetectorModule) {
            this.run6(var1, var3);
         } else if (var3 instanceof PlayerRadarModule) {
            this.run7(var1, var3);
         }
      }

      return var1;
   }

   private void run5(List<InternalHelper7> var1, Module var2, String var3) {
      Setting var4 = this.valOf(var2, "position-x");
      Setting var5 = this.valOf(var2, "position-y");
      Setting var6 = this.valOf(var2, "cell-size");
      if (var4 != null && var5 != null && var6 != null) {
         int var7 = Math.max(32, (Integer)var6.getObject() * 9);
         var1.add(new InternalHelper7(var2, var3, var4, var5, (Integer)var4.getObject(), (Integer)var5.getObject(), var7, var7, var7, var7, false));
      }
   }

   private void run6(List<InternalHelper7> var1, Module var2) {
      Setting var3 = this.valOf(var2, "hud-x");
      Setting var4 = this.valOf(var2, "hud-y");
      Setting var5 = this.valOf(var2, "admin-list-size");
      if (var3 != null && var4 != null && var5 != null) {
         int var6 = ((Integer)var3.getObject()) >= 0 ? (Integer)var3.getObject() : 10;
         int var7 = ((Integer)var4.getObject()) >= 0 ? (Integer)var4.getObject() : 10;
         AdminDetectorModule var8 = (AdminDetectorModule)var2;
         int var9 = var8.getInt();
         int var10 = var8.getInt5();
         var1.add(new InternalHelper7(var2, "Drag Admin List", var3, var4, var6, var7, var9, var10, var9, var10, false));
      }
   }

   private void run7(List<InternalHelper7> var1, Module var2) {
      Setting var3 = this.valOf(var2, "radar-x");
      Setting var4 = this.valOf(var2, "radar-y");
      if (var3 != null && var4 != null && var2 instanceof PlayerRadarModule var5) {
         int var6 = var5.getInt3();
         int var7 = var5.getInt5();
         var1.add(
            new InternalHelper7(var2, "Drag Player Radar", var3, var4, (Integer)var3.getObject(), (Integer)var4.getObject(), var6, var7, var6, var7, false)
         );
      }
   }

   private Setting<Integer> valOf(Module var1, String var2) {
      Setting var3 = var1.val2.valOf2(var2);
      return var3 != null && var3.getObject() instanceof Integer ? var3 : null;
   }
}
