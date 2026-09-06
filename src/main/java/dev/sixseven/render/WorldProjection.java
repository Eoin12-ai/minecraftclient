package dev.sixseven.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public final class WorldProjection {
   private static final Matrix4f mvp = new Matrix4f();
   private static final Vector4f scratch = new Vector4f();
   private static Vec3d camPos = Vec3d.ZERO;
   private static int fbWidth;
   private static int fbHeight;
   private static float partialTick;
   private static boolean valid;

   private WorldProjection() {
   }

   public static void capture(Matrix4f matrix, float f) {
      MinecraftClient client = MinecraftClient.getInstance();
      Camera camera = client.gameRenderer.getCamera();
      if (camera == null) {
         valid = false;
      } else {
         Quaternionf rotation = camera.getRotation().conjugate(new Quaternionf());
         mvp.set(matrix).rotate(rotation);
         camPos = camera.getCameraPos();
         Framebuffer framebuffer = client.getFramebuffer();
         fbWidth = framebuffer.textureWidth;
         fbHeight = framebuffer.textureHeight;
         partialTick = f;
         valid = true;
      }
   }

   public static void invalidate() {
      valid = false;
   }

   public static boolean isValid() {
      return valid;
   }

   public static float partialTick() {
      return partialTick;
   }

   public static float[] project(double d, double coord, double currentScore) {
      float[] f = projectRaw(d, coord, currentScore);
      if (f == null) {
         return null;
      } else {
         float f3 = OverlayRenderer.uiScale();
         return new float[]{f[0] / f3, f[1] / f3};
      }
   }

   public static float[] projectRaw(double d, double coord, double currentScore) {
      if (!valid) {
         return null;
      } else {
         scratch.set((float)(d - camPos.x), (float)(coord - camPos.y), (float)(currentScore - camPos.z), 1.0F);
         mvp.transform(scratch);
         if (scratch.w <= 1.0E-4F) {
            return null;
         } else {
            float f = scratch.x / scratch.w;
            float f5 = scratch.y / scratch.w;
            float f6 = (f * 0.5F + 0.5F) * (float)fbWidth;
            float f7 = (1.0F - (f5 * 0.5F + 0.5F)) * (float)fbHeight;
            return new float[]{f6, f7};
         }
      }
   }
}
