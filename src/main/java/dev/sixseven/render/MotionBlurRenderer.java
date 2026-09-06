package dev.sixseven.render;

import dev.sixseven.module.visuals.MotionBlurModule;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.util.Colors;
import java.nio.ByteBuffer;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL33C;

public final class MotionBlurRenderer {
   private static final double EASE_SPEED = 12.0;
   private static int historyTex = -1;
   private static int historyImage = -1;
   private static int texW;
   private static int texH;
   private static boolean primed;
   private static long lastNanos;
   private static double enableAmount;
   private static int framesRendered;
   private static float lastRetention;

   private MotionBlurRenderer() {
   }

   public static void render(NVGRenderer nVGRenderer, int n, int offset, MotionBlurModule motionBlurModule) {
      if (n > 0 && offset > 0) {
         long l = System.nanoTime();
         double d = lastNanos == 0L ? 0.0 : (double)(l - lastNanos) / 1.0E9;
         lastNanos = l;
         double coord = 1.0 - Math.exp(-12.0 * Math.max(0.0, d));
         enableAmount = enableAmount + (1.0 - enableAmount) * coord;
         if (enableAmount > 0.999) {
            enableAmount = 1.0;
         }

         if (historyTex == -1 || texW != n || texH != offset) {
            allocate(nVGRenderer, n, offset);
            primed = false;
         }

         double currentScore = motionBlurModule.retention();
         if (motionBlurModule.fpsCompensated.get() && d > 0.0) {
            currentScore = Math.pow(currentScore, d * 60.0);
         }

         double coord3 = Math.clamp(currentScore * enableAmount, 0.0, 0.97);
         lastRetention = (float)coord3;
         if (!primed) {
            copyToHistory(n, offset);
            primed = true;
            framesRendered++;
         } else {
            int bestSlot = -1;
            float f = motionBlurModule.tintAmount();
            if (f > 0.0F) {
               bestSlot = Colors.lerp(-1, Colors.withAlpha(motionBlurModule.accentColor(), 255), f);
            }

            nVGRenderer.beginFrame((float)n, (float)offset, 1.0F);
            nVGRenderer.save();
            nVGRenderer.alpha((float)coord3);
            nVGRenderer.image(historyImage, 0.0F, 0.0F, (float)n, (float)offset, bestSlot);
            nVGRenderer.restore();
            nVGRenderer.endFrame();
            copyToHistory(n, offset);
            framesRendered++;
         }
      }
   }

   private static void allocate(NVGRenderer nVGRenderer, int n, int offset) {
      long l = nVGRenderer.ctx();
      if (historyImage > 0) {
         NanoVG.nvgDeleteImage(l, historyImage);
         historyImage = -1;
      }

      if (historyTex != -1) {
         GL33C.glDeleteTextures(historyTex);
      }

      historyTex = GL33C.glGenTextures();
      GL33C.glActiveTexture(33984);
      GL33C.glBindTexture(3553, historyTex);
      GL33C.glTexImage2D(3553, 0, 32856, n, offset, 0, 6408, 5121, (ByteBuffer)null);
      GL33C.glTexParameteri(3553, 10241, 9729);
      GL33C.glTexParameteri(3553, 10240, 9729);
      GL33C.glTexParameteri(3553, 10242, 33071);
      GL33C.glTexParameteri(3553, 10243, 33071);
      GL33C.glTexParameteri(3553, 36421, 1);
      texW = n;
      texH = offset;
      historyImage = NanoVGGL3.nvglCreateImageFromHandle(l, historyTex, n, offset, 65544);
   }

   private static void copyToHistory(int n, int offset) {
      GL33C.glActiveTexture(33984);
      GL33C.glBindTexture(3553, historyTex);
      GL33C.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, n, offset);
   }

   public static void reset() {
      primed = false;
      enableAmount = 0.0;
      lastNanos = 0L;
   }

   public static int framesRendered() {
      return framesRendered;
   }

   public static float lastRetention() {
      return lastRetention;
   }
}
