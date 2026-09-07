package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import util.RenderPipelineUtils;

public final class ActivityChunkFinderModuleHelper {
   private MatrixStack class4587;
   private VertexConsumerProvider class4597;
   private static Vec3d class243;
   private Object object;
   public static final int intVal = 2;
   public static final int intVal2 = 4;
   public static final int intVal3 = 8;
   public static final int intVal4 = 16;
   public static final int intVal5 = 32;
   public static final int intVal6 = 64;
   public static final int intVal7 = 126;
   private static final ThreadLocal<ActivityChunkFinderModuleEntry2> threadLocal = new ThreadLocal<>();
   private static volatile Method method;
   private static volatile Method method2;
   private static volatile Method method3;
   private static volatile boolean bool;

   private ActivityChunkFinderModuleHelper(MatrixStack var1, VertexConsumerProvider var2, Vec3d var3) throws ReflectiveOperationException {
      this.class4587 = var1;
      this.class4597 = var2;
      class243 = var3;
      var1.push();
      var1.translate(-var3.x, -var3.y, -var3.z);
      this.object = methodOf2(var1.peek().getClass(), "getPositionMatrix", "method_23761").invoke(var1.peek());
   }

   public static ActivityChunkFinderModuleHelper valOf(Object var0) {
      try {
         MatrixStack var1 = (MatrixStack)objectOf(var0, "matrices", "matrixStack");
         VertexConsumerProvider var2 = (VertexConsumerProvider)objectOf(var0, "consumers");
         Camera var3 = MinecraftClient.getInstance().gameRenderer.getCamera();
         Vec3d var4 = (Vec3d)objectOf(var3, "getCameraPos", "getPos", "method_71156", "method_19326");
         class243 = var4;
         return new ActivityChunkFinderModuleHelper(var1, var2, var4);
      } catch (RuntimeException | ReflectiveOperationException var5) {
         SwyzzyAddon.logger.warn("Could not start Swyzzy world rendering.", var5);
         return null;
      }
   }

   public void run3() {
      this.class4587.pop();
   }

   public void run(
      double var1,
      double var3,
      double var5,
      double var7,
      double var9,
      double var11,
      ActivityChunkFinderModuleEntry2 var13,
      ActivityChunkFinderModuleEntry2 var14,
      RenderMode var15,
      int var16
   ) {
      try {
         if (var15 == RenderMode.Sides || var15 == RenderMode.Both) {
            this.run7(var1, var3, var5, var7, var9, var11, var13, var16);
         }

         if (var15 == RenderMode.Lines || var15 == RenderMode.Both) {
            this.run8(var1, var3, var5, var7, var9, var11, var14, var16);
         }
      } catch (ReflectiveOperationException var18) {
         SwyzzyAddon.logger.debug("Could not render a Swyzzy box.", var18);
      }
   }

   public void run2(BlockPos var1, ActivityChunkFinderModuleEntry2 var2, ActivityChunkFinderModuleEntry2 var3, RenderMode var4, int var5) {
      this.run(var1.getX(), var1.getY(), var1.getZ(), var1.getX() + 1, var1.getY() + 1, var1.getZ() + 1, var2, var3, var4, var5);
   }

   public void run4(double var1, double var3, double var5, double var7, double var9, double var11, ActivityChunkFinderModuleEntry2 var13, int var14) {
      try {
         this.run7(var1, var3, var5, var7, var9, var11, var13, var14);
      } catch (ReflectiveOperationException var16) {
         SwyzzyAddon.logger.debug("Could not render Swyzzy box sides.", var16);
      }
   }

   public void run5(double var1, double var3, double var5, double var7, double var9, double var11, ActivityChunkFinderModuleEntry2 var13, int var14) {
      try {
         this.run8(var1, var3, var5, var7, var9, var11, var13, var14);
      } catch (ReflectiveOperationException var16) {
         SwyzzyAddon.logger.debug("Could not render Swyzzy box lines.", var16);
      }
   }

   public void run6(double var1, double var3, double var5, double var7, double var9, double var11, ActivityChunkFinderModuleEntry2 var13) {
      try {
         VertexConsumer var14 = this.class4588Of("linesTranslucent", "lines", "getLines");
         this.run11(var14, var1, var3, var5, var7, var9, var11, var13);
      } catch (ReflectiveOperationException var15) {
         SwyzzyAddon.logger.debug("Could not render a Swyzzy line.", var15);
      }
   }

   private void run7(double var1, double var3, double var5, double var7, double var9, double var11, ActivityChunkFinderModuleEntry2 var13, int var14) throws ReflectiveOperationException {
      VertexConsumer var15 = this.class4588Of("debugQuads", "getDebugQuads");
      if (check(var14, 4)) {
         this.run10(var15, var1, var3, var5, var7, var3, var5, var7, var3, var11, var1, var3, var11, var13);
      }

      if (check(var14, 2)) {
         this.run10(var15, var1, var9, var5, var1, var9, var11, var7, var9, var11, var7, var9, var5, var13);
      }

      if (check(var14, 8)) {
         this.run10(var15, var1, var3, var5, var1, var9, var5, var7, var9, var5, var7, var3, var5, var13);
      }

      if (check(var14, 16)) {
         this.run10(var15, var1, var3, var11, var7, var3, var11, var7, var9, var11, var1, var9, var11, var13);
      }

      if (check(var14, 32)) {
         this.run10(var15, var1, var3, var5, var1, var3, var11, var1, var9, var11, var1, var9, var5, var13);
      }

      if (check(var14, 64)) {
         this.run10(var15, var7, var3, var5, var7, var9, var5, var7, var9, var11, var7, var3, var11, var13);
      }
   }

   private void run8(double var1, double var3, double var5, double var7, double var9, double var11, ActivityChunkFinderModuleEntry2 var13, int var14) throws ReflectiveOperationException {
      VertexConsumer var15 = this.class4588Of("linesTranslucent", "lines", "getLines");
      if (check2(var14, 4, 8)) {
         this.run11(var15, var1, var3, var5, var7, var3, var5, var13);
      }

      if (check2(var14, 4, 64)) {
         this.run11(var15, var7, var3, var5, var7, var3, var11, var13);
      }

      if (check2(var14, 4, 16)) {
         this.run11(var15, var7, var3, var11, var1, var3, var11, var13);
      }

      if (check2(var14, 4, 32)) {
         this.run11(var15, var1, var3, var11, var1, var3, var5, var13);
      }

      if (check2(var14, 2, 8)) {
         this.run11(var15, var1, var9, var5, var7, var9, var5, var13);
      }

      if (check2(var14, 2, 64)) {
         this.run11(var15, var7, var9, var5, var7, var9, var11, var13);
      }

      if (check2(var14, 2, 16)) {
         this.run11(var15, var7, var9, var11, var1, var9, var11, var13);
      }

      if (check2(var14, 2, 32)) {
         this.run11(var15, var1, var9, var11, var1, var9, var5, var13);
      }

      if (check2(var14, 8, 32)) {
         this.run11(var15, var1, var3, var5, var1, var9, var5, var13);
      }

      if (check2(var14, 8, 64)) {
         this.run11(var15, var7, var3, var5, var7, var9, var5, var13);
      }

      if (check2(var14, 16, 64)) {
         this.run11(var15, var7, var3, var11, var7, var9, var11, var13);
      }

      if (check2(var14, 16, 32)) {
         this.run11(var15, var1, var3, var11, var1, var9, var11, var13);
      }
   }

   private static boolean check(int var0, int var1) {
      return (var0 & var1) == 0;
   }

   private static boolean check2(int var0, int var1, int var2) {
      return (var0 & var1) == 0 || (var0 & var2) == 0;
   }

   private void run9(VertexConsumer var1, double... var2) throws ReflectiveOperationException {
      ActivityChunkFinderModuleEntry2 var3 = threadLocal.get();

      for (byte var4 = 0; var4 < 12; var4 += 3) {
         this.run12(var1, var2[var4], var2[var4 + 1], var2[var4 + 2], var3, false, 0.0F, 1.0F, 0.0F);
      }
   }

   private void run10(
      VertexConsumer var1,
      double var2,
      double var4,
      double var6,
      double var8,
      double var10,
      double var12,
      double var14,
      double var16,
      double var18,
      double var20,
      double var22,
      double var24,
      ActivityChunkFinderModuleEntry2 var26
   ) throws ReflectiveOperationException {
      threadLocal.set(var26);

      try {
         this.run9(var1, var2, var4, var6, var8, var10, var12, var14, var16, var18, var20, var22, var24);
      } finally {
         threadLocal.remove();
      }
   }

   private void run11(
      VertexConsumer var1, double var2, double var4, double var6, double var8, double var10, double var12, ActivityChunkFinderModuleEntry2 var14
   ) throws ReflectiveOperationException {
      float var15 = (float)(var8 - var2);
      float var16 = (float)(var10 - var4);
      float var17 = (float)(var12 - var6);
      float var18 = (float)Math.sqrt(var15 * var15 + var16 * var16 + var17 * var17);
      if (var18 > 0.0F) {
         var15 /= var18;
         var16 /= var18;
         var17 /= var18;
      }

      this.run12(var1, var2, var4, var6, var14, true, var15, var16, var17);
      this.run12(var1, var8, var10, var12, var14, true, var15, var16, var17);
   }

   private void run12(
      VertexConsumer var1, double var2, double var4, double var6, ActivityChunkFinderModuleEntry2 var8, boolean var9, float var10, float var11, float var12
   ) throws ReflectiveOperationException {
      Method var13 = methodOf(var1.getClass(), this.object.getClass());
      Object var14 = var13.invoke(var1, this.object, (float)var2, (float)var4, (float)var6);
      VertexConsumer var15 = (VertexConsumer)var14;
      var15 = var15.color(var8.intVal, var8.intVal2, var8.intVal3, var8.intVal4);
      if (var9) {
         var15.normal(var10, var11, var12);
         Method var16 = methodOf3(var15.getClass());
         if (var16 != null) {
            var16.invoke(var15, 1.0F);
         }
      }
   }

   private VertexConsumer class4588Of(String... var1) throws ReflectiveOperationException {
      RenderLayer var2 = var1[0].startsWith("debug") ? RenderPipelineUtils.getLinesLayer("debug") : RenderPipelineUtils.getLayer("default");
      if (var2 == null) {
         var2 = class1921Of(var1);
      }

      Method var3 = method;
      if (var3 == null) {
         for (Method var7 : VertexConsumerProvider.class.getMethods()) {
            if (var7.getParameterCount() == 1
               && RenderLayer.class.isAssignableFrom(var7.getParameterTypes()[0])
               && VertexConsumer.class.isAssignableFrom(var7.getReturnType())) {
               var3 = var7;
               break;
            }
         }

         if (var3 == null) {
            throw new NoSuchMethodException("VertexConsumerProvider#getBuffer");
         }

         method = var3;
      }

      return (VertexConsumer)var3.invoke(this.class4597, var2);
   }

   private static RenderLayer class1921Of(String... var0) throws ReflectiveOperationException {
      String[] var1 = new String[]{
         var0[0],
         var0.length > 1 ? var0[1] : "",
         var0.length > 2 ? var0[2] : "",
         var0[0].startsWith("debug") ? "method_76023" : "method_76668",
         var0[0].startsWith("debug") ? "method_49042" : "method_23594"
      };
      Class var2 = null;

      for (String var6 : new String[]{"net.minecraft.client.render.RenderLayers", "net.minecraft.class_12249"}) {
         try {
            var2 = Class.forName(var6);
            break;
         } catch (ClassNotFoundException var15) {
         }
      }

      Class[] var16 = var2 == null ? new Class[]{RenderLayer.class} : new Class[]{var2, RenderLayer.class};

      for (Class var7 : var16) {
         for (String var11 : var1) {
            if (!var11.isEmpty()) {
               try {
                  Method var12 = var7.getDeclaredMethod(var11);
                  if (Modifier.isStatic(var12.getModifiers()) && RenderLayer.class.isAssignableFrom(var12.getReturnType())) {
                     var12.setAccessible(true);
                     return (RenderLayer)var12.invoke(null);
                  }
               } catch (NoSuchMethodException var13) {
               }

               try {
                  Field var20 = var7.getDeclaredField(var11.toUpperCase());
                  var20.setAccessible(true);
                  return (RenderLayer)var20.get(null);
               } catch (NoSuchFieldException var14) {
               }
            }
         }
      }

      throw new NoSuchMethodException("compatible RenderLayer");
   }

   private static Method methodOf(Class<?> var0, Class<?> var1) throws NoSuchMethodException {
      Method var2 = method2;
      if (var2 != null && var2.getParameterTypes()[0].isAssignableFrom(var1)) {
         return var2;
      } else {
         for (Method var6 : VertexConsumer.class.getMethods()) {
            Class[] var7 = var6.getParameterTypes();
            if (var7.length == 4
               && var7[0].isAssignableFrom(var1)
               && var7[1] == float.class
               && var7[2] == float.class
               && var7[3] == float.class
               && VertexConsumer.class.isAssignableFrom(var6.getReturnType())) {
               method2 = var6;
               return var6;
            }
         }

         throw new NoSuchMethodException("VertexConsumer#vertex(matrix, x, y, z)");
      }
   }

   private static Object objectOf(Object var0, String... var1) throws ReflectiveOperationException {
      for (String var5 : var1) {
         try {
            return var0.getClass().getMethod(var5).invoke(var0);
         } catch (NoSuchMethodException var7) {
         }
      }

      throw new NoSuchMethodException(String.join("/", var1));
   }

   private static Method methodOf2(Class<?> var0, String... var1) throws NoSuchMethodException {
      for (String var5 : var1) {
         try {
            return var0.getMethod(var5);
         } catch (NoSuchMethodException var7) {
         }
      }

      throw new NoSuchMethodException(String.join("/", var1));
   }

   private static Method methodOf3(Class<?> var0) {
      if (bool) {
         return method3;
      } else {
         try {
            method3 = methodOf4(var0, "lineWidth", "method_75298");
         } catch (NoSuchMethodException var2) {
            method3 = null;
         }

         bool = true;
         return method3;
      }
   }

   private static Method methodOf4(Class<?> var0, String... var1) throws NoSuchMethodException {
      for (String var5 : var1) {
         try {
            return var0.getMethod(var5, float.class);
         } catch (NoSuchMethodException var7) {
         }
      }

      throw new NoSuchMethodException(String.join("/", var1));
   }
}
