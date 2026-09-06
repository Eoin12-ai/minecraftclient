package util;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.Locale;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import pkg1.AdminDetectorModuleUtil;

public final class UtilsUtil {
   public static final int intVal = -267645676;
   public static final int intVal2 = -15460833;
   public static final int intVal3 = -15131867;
   public static final int intVal4 = -14473937;
   public static final int intVal5 = -13881800;
   public static final int intVal6 = -4276020;
   public static final int intVal7 = -8749684;
   public static final int intVal8 = -1073346807;

   private UtilsUtil() {
   }

   public static int getInt() {
      return SwyzzyAddon.val7.getVal().intOf(255);
   }

   public static int intOf(int var0, int var1) {
      return (Math.max(0, Math.min(255, var1)) & 0xFF) << 24 | var0 & 16777215;
   }

   public static void run(DrawContext var0, int var1, int var2) {
      var0.fill(0, 0, var1, var2, -1073346807);
      var0.fillGradient(0, 0, var1, var2, 806619688, 1342505737);
   }

   public static void run9(DrawContext var0, int var1, int var2, int var3, int var4) {
      AdminDetectorModuleUtil.run5(var0, var1, var2, var3, var4, 4);
      AdminDetectorModuleUtil.run2(var0, var1, var2, var3, var4, 6, -267645676);
      AdminDetectorModuleUtil.run3(var0, var1, var2, var3, 3, 3, 0, getInt());
   }

   public static void run2(DrawContext var0, TextRenderer var1, String var2, int var3, int var4) {
      var0.drawText(var1, Text.literal(addSetting(var2)), var3, var4, getInt(), false);
   }

   public static String addSetting(String var0) {
      return var0.replace('-', ' ').replace('_', ' ').toUpperCase(Locale.ROOT);
   }

   public static Inner1 valOf(String var0, int var1, int var2, int var3, int var4, Runnable var5) {
      return new Inner1(net.minecraft.client.MinecraftClient.getInstance().textRenderer, var1, var2, var3, var4, Text.literal(var0));
   }

   public static Inner1 valOf2(TextRenderer var0, int var1, int var2, int var3, int var4, String var5) {
      return new Inner1(var0, var1, var2, var3, var4, Text.literal(var5));
   }
}
