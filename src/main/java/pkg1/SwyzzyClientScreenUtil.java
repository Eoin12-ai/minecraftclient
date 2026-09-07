package pkg1;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class SwyzzyClientScreenUtil {
   private static final Identifier class2960 = Identifier.of("swyzzyclient", "textures/gui/logo.png");
   private static final float floatVal = 1024.0F;
   private static final float floatVal2 = 342.0F;
   private static final float floatVal3 = 0.68F;
   private static final float floatVal4 = 0.24F;
   private static final int intVal = 3;
   private static Boolean booleanVal;

   private SwyzzyClientScreenUtil() {
   }

   public static void run(DrawContext var0, int var1, int var2, float var3) {
      if (isEnabled() && !(var3 <= 0.01F)) {
         int var4 = Math.round(var1 * 0.68F);
         int var5 = Math.round(var4 * 0.33398438F);
         int var6 = (var1 - var4) / 2;
         int var7 = (var2 - var5) / 2;
         int var8 = Math.max(1, Math.round(61.199997F * var3 / 9.0F));
         int var9 = var8 << 24 | 16777215;

         for (int var10 = -1; var10 <= 1; var10++) {
            for (int var11 = -1; var11 <= 1; var11++) {
               var0.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, class2960, var6 + var10 * 3, var7 + var11 * 3, 0.0F, 0.0F, var4, var5, var4, var5, var9);
            }
         }
      }
   }

   private static boolean isEnabled() {
      if (booleanVal == null) {
         MinecraftClient var0 = MinecraftClient.getInstance();
         booleanVal = var0.getResourceManager() != null && var0.getResourceManager().getResource(class2960).isPresent();
      }

      return booleanVal;
   }
}
