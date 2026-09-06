package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public final class Helper2 {
   private static final int intVal = 6;
   private static final int intVal2 = 4;
   private static final int intVal3 = 12;
   private static final int intVal4 = -771159535;
   private static final int intVal5 = -986377;
   private static final int intVal6 = -1;
   private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
   private final MinecraftClient class310 = MinecraftClient.getInstance();
   private List<SwyzzyAddon.Inner1> list;
   private Helper val;
   private final Map<String, Float> map = new HashMap<>();
   private long longVal;
   private FPS val2;
   private int intVal7;
   private int intVal8;
   private boolean bool;

   public Helper2(List<SwyzzyAddon.Inner1> var1, Helper var2) {
      this.list = var1;
      this.val = var2;
   }

   public void run5(DrawContext var1) {
      if (this.class310.getWindow() != null) {
         boolean var2 = this.class310.currentScreen instanceof ChatScreen;
         long var3 = System.nanoTime();
         float var5 = this.longVal == 0L ? 0.016666668F : Math.min(0.1F, (float)(var3 - this.longVal) / 1.0E9F);
         this.longVal = var3;
         this.run(var5);
         List var6 = this.listOf(var2);
         if (var2) {
            this.run11(var6);
            var6 = this.listOf(true);
         } else {
            this.val2 = null;
            this.bool = false;
         }

         for (InternalHelper3 var8 : (Iterable<InternalHelper3>)(Object)(var6)) {
            this.run4(var1, var8, var2);
         }

         SwyzzyAddon.run5(var1);
      }
   }

   private void run(float var1) {
      for (SwyzzyAddon.Inner1 var3 : this.list) {
         String var4 = var3.label().toUpperCase(Locale.ROOT);
         float var5 = var3.module().isEnabled() ? 1.0F : 0.0F;
         float var6 = AdminDetectorModuleUtil.floatOf(this.map.getOrDefault(var4, 0.0F), var5, var1, 12.0F);
         if (var6 <= 0.01F && var5 == 0.0F) {
            this.map.remove(var4);
         } else {
            this.map.put(var4, var6);
         }
      }
   }

   private List<InternalHelper3> listOf(boolean var1) {
      ArrayList var2 = new ArrayList();
      int var3 = this.class310.getWindow().getScaledWidth();
      int var4 = this.class310.getWindow().getScaledHeight();
      this.run2(var2, FPS.WATERMARK, "SWYZZY+", var3, var4);
      String var5 = this.class310.player == null
         ? "XYZ -- -- --"
         : String.format(
            Locale.ROOT,
            "XYZ %d %d %d",
            MathHelper.floor(this.class310.player.getX()),
            MathHelper.floor(this.class310.player.getY()),
            MathHelper.floor(this.class310.player.getZ())
         );
      this.run2(var2, FPS.COORDINATES, var5, var3, var4);
      this.run2(var2, FPS.FPS, this.class310.getCurrentFps() + " FPS", var3, var4);
      this.run2(var2, FPS.CLOCK, LocalTime.now().format(dateTimeFormatter), var3, var4);
      if (this.val.check(FPS.ACTIVE_MODULES) && (!this.map.isEmpty() || var1)) {
         java.util.List<String> var6 = new ArrayList<>(this.map.keySet());
         var6.sort(Comparator.comparingInt(this::intOf).reversed());
         if (var6.isEmpty()) {
            var6 = List.of("ACTIVE MODULES");
         }

         this.run3(var2, FPS.ACTIVE_MODULES, (List<String>)var6, var3, var4);
      }

      return var2;
   }

   private void run2(List<InternalHelper3> var1, FPS var2, String var3, int var4, int var5) {
      if (this.val.check(var2)) {
         this.run3(var1, var2, List.of(var3.toUpperCase(Locale.ROOT)), var4, var5);
      }
   }

   private void run3(List<InternalHelper3> var1, FPS var2, List<String> var3, int var4, int var5) {
      int var6 = var3.stream().mapToInt(this::intOf).max().orElse(1);
      int var7 = var6 + 12;
      int var8 = var3.size() * 12 + 8;
      InternalHelper2 var9 = this.val.valOf(var2);
      int var10 = intOf2(var9.getInt(), var7, var4);
      int var11 = MathHelper.clamp(var9.getInt2(), 0, Math.max(0, var5 - var8));
      var1.add(new InternalHelper3(var2, var10, var11, var7, var8, var3));
   }

   private int intOf(String var1) {
      int var2 = this.class310.textRenderer.getWidth(var1);
      String var3 = this.addSetting(var1);
      if (var3 != null) {
         var2 += this.class310.textRenderer.getWidth(" null");
      }

      return var2;
   }

   private String addSetting(String var1) {
      for (SwyzzyAddon.Inner1 var3 : this.list) {
         if (var3.label().toUpperCase(Locale.ROOT).equals(var1)) {
            String var4 = var3.module().getString2();
            return var4 != null && !var4.isBlank() ? var4 : null;
         }
      }

      return null;
   }

   private static int intOf2(int var0, int var1, int var2) {
      if (var0 == -1) {
         return Math.max(0, var2 - var1 - 3);
      } else {
         return var0 == -2 ? Math.max(0, (var2 - var1) / 2) : MathHelper.clamp(var0, 0, Math.max(0, var2 - var1));
      }
   }

   private void run4(DrawContext var1, InternalHelper3 var2, boolean var3) {
      int var4 = AdminDetectorModuleUtil.getInt();
      if (var2.element() == FPS.ACTIVE_MODULES) {
         this.run6(var1, var2, var4);
      } else {
         this.run7(var1, var2, var4);
      }

      if (var3) {
         this.run10(var1, var2, var4);
      }
   }

   private void run6(DrawContext var1, InternalHelper3 var2, int var3) {
      int var4 = var2.getInt() + var2.width();
      int var5 = var2.getInt2() + 4;

      for (String var7 : var2.lines()) {
         float var8 = this.map.getOrDefault(var7, 1.0F);
         int var9 = Math.round((1.0F - var8) * 10.0F);
         String var10 = this.addSetting(var7);
         int var11 = this.intOf(var7) + 12;
         int var12 = var4 - var11 + var9;
         AdminDetectorModuleUtil.run2(var1, var12, var5 - 1, var11 - 2, 12, 3, AdminDetectorModuleUtil.intOf(-771159535, var8));
         AdminDetectorModuleUtil.run2(var1, var4 - 2 + var9, var5 - 1, 2, 12, 1, AdminDetectorModuleUtil.intOf(var3, var8));
         int var14 = var12 + 6;
         var1.drawText(this.class310.textRenderer, Text.literal(var7), var14, var5 + 1, AdminDetectorModuleUtil.intOf(-986377, var8), false);
         if (var10 != null) {
            var1.drawText(
               this.class310.textRenderer,
               Text.literal(var10),
               var14 + this.class310.textRenderer.getWidth("null "),
               var5 + 1,
               AdminDetectorModuleUtil.intOf(AdminDetectorModuleUtil.intOf2(var3, -1, 0.35F), var8),
               false
            );
         }

         var5 += 12;
      }
   }

   private void run7(DrawContext var1, InternalHelper3 var2, int var3) {
      AdminDetectorModuleUtil.run2(var1, var2.getInt(), var2.getInt2(), var2.width(), var2.height(), 4, -771159535);
      AdminDetectorModuleUtil.run2(var1, var2.getInt(), var2.getInt2() + 2, 2, var2.height() - 4, 1, var3);
      String var4 = var2.lines().getFirst();
      int var5 = var2.getInt2() + 4 + 1;
      switch (var2.element().ordinal() + 1) {
         case 1:
            String var6 = var4.endsWith("+") ? var4.substring(0, var4.length() - 1) : var4;
            int var7 = var2.getInt() + (var2.width() - this.class310.textRenderer.getWidth(var4)) / 2;
            var1.drawText(this.class310.textRenderer, Text.literal(var6), var7, var5, -1, false);
            if (!var6.equals(var4)) {
               var1.drawText(this.class310.textRenderer, Text.literal("+"), var7 + this.class310.textRenderer.getWidth(var6), var5, var3, false);
            }
            break;
         case 2:
            this.run8(var1, var2, var4, "XYZ", var3, var5);
            break;
         case 3:
            this.run9(var1, var2, var4, "FPS", var3, var5);
            break;
         default:
            var1.drawText(this.class310.textRenderer, Text.literal(var4), var2.getInt() + 6, var5, AdminDetectorModuleUtil.intOf2(var3, -1, 0.55F), false);
      }
   }

   private void run8(DrawContext var1, InternalHelper3 var2, String var3, String var4, int var5, int var6) {
      int var7 = var2.getInt() + 6;
      var1.drawText(this.class310.textRenderer, Text.literal(var4), var7, var6, var5, false);
      String var8 = var3.startsWith(var4) ? var3.substring(var4.length()) : " null";
      var1.drawText(this.class310.textRenderer, Text.literal(var8), var7 + this.class310.textRenderer.getWidth(var4), var6, -1, false);
   }

   private void run9(DrawContext var1, InternalHelper3 var2, String var3, String var4, int var5, int var6) {
      int var7 = var2.getInt() + 6;
      String var8 = var3.endsWith(var4) ? var3.substring(0, var3.length() - var4.length()) : "null ";
      var1.drawText(this.class310.textRenderer, Text.literal(var8), var7, var6, -1, false);
      var1.drawText(
         this.class310.textRenderer,
         Text.literal(var4),
         var7 + this.class310.textRenderer.getWidth(var8),
         var6,
         AdminDetectorModuleUtil.intOf2(var5, -1, 0.2F),
         false
      );
   }

   private void run10(DrawContext var1, InternalHelper3 var2, int var3) {
      int var4 = var2.getInt() - 1;
      int var5 = var2.getInt2() - 1;
      int var6 = var2.width() + 2;
      int var7 = var2.height() + 2;
      int var8 = AdminDetectorModuleUtil.intOf(var3, this.val2 == var2.element() ? 1.0F : 0.6F);
      AdminDetectorModuleUtil.run(var1, var4, var5, var6, 1, var8);
      AdminDetectorModuleUtil.run(var1, var4, var5 + var7 - 1, var6, 1, var8);
      AdminDetectorModuleUtil.run(var1, var4, var5, 1, var7, var8);
      AdminDetectorModuleUtil.run(var1, var4 + var6 - 1, var5, 1, var7, var8);
   }

   private void run11(List<InternalHelper3> var1) {
      double var2 = this.class310.mouse.getX() * this.class310.getWindow().getScaledWidth() / this.class310.getWindow().getWidth();
      double var4 = this.class310.mouse.getY() * this.class310.getWindow().getScaledHeight() / this.class310.getWindow().getHeight();
      boolean var6 = GLFW.glfwGetMouseButton(this.class310.getWindow().getHandle(), 0) == 1;
      if (var6 && !this.bool) {
         for (int var7 = var1.size() - 1; var7 >= 0; var7--) {
            InternalHelper3 var8 = (InternalHelper3)var1.get(var7);
            if (var8.check(var2, var4)) {
               this.val2 = var8.element();
               this.intVal7 = (int)Math.round(var2 - var8.getInt());
               this.intVal8 = (int)Math.round(var4 - var8.getInt2());
               break;
            }
         }
      }

      if (var6 && this.val2 != null) {
         InternalHelper3 var12 = var1.stream().filter(this::check).findFirst().orElse(null);
         if (var12 != null) {
            int var13 = Math.max(0, this.class310.getWindow().getScaledWidth() - var12.width());
            int var9 = Math.max(0, this.class310.getWindow().getScaledHeight() - var12.height());
            int var10 = MathHelper.clamp((int)Math.round(var2) - this.intVal7, 0, var13);
            int var11 = MathHelper.clamp((int)Math.round(var4) - this.intVal8, 0, var9);
            this.val.run6(this.val2, var10, var11);
         }
      }

      if (!var6 && this.val2 != null) {
         this.val.run7();
         this.val2 = null;
      }

      this.bool = var6;
   }

   private boolean check(InternalHelper3 var1) {
      return var1.element() == this.val2;
   }
}
