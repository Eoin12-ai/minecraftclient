package dev.sixseven.render;

import dev.sixseven.module.client.JumpCirclesModule;
import java.util.Deque;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public final class JumpCircleRenderer {
   private static final Identifier TEXTURE_67 = Identifier.of("sixsevenclient", "textures/misc/67.png");
   private static final Identifier TEXTURE_RING = Identifier.of("sixsevenclient", "textures/misc/ring.png");
   private static final float POP_IN_TIME = 0.25F;
   private static final float FADE_OUT_TIME = 0.4F;
   private static final float SHOCKWAVE_TIME = 0.45F;
   private static final float PULSE_SPEED = 10.0F;
   private static final int SHIMMER_TARGET_RGB = 16761566;

   private JumpCircleRenderer() {
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d vec, JumpCirclesModule jumpCirclesModule) {
      Deque<JumpCirclesModule.JumpCircle> deque = jumpCirclesModule.circles();
      if (!deque.isEmpty()) {
         long l = System.nanoTime();
         float f = Math.max(0.1F, jumpCirclesModule.lifetime.getFloat());

         while (!deque.isEmpty() && ((JumpCirclesModule.JumpCircle)deque.peekFirst()).ageSeconds(l) > f) {
            deque.removeFirst();
         }

         if (!deque.isEmpty()) {
            float f3 = jumpCirclesModule.size.getFloat();
            int n = currentBaseRgb(jumpCirclesModule);
            VertexConsumer consumer = immediate.getBuffer(net.minecraft.client.render.RenderLayer.getEntityTranslucentEmissive(TEXTURE_67));

            for (JumpCirclesModule.JumpCircle jumpCircle : deque) {
               renderDecal(matrices, consumer, jumpCircle, vec, l, f, f3, n);
            }

            if (jumpCirclesModule.shockwave.get()) {
               VertexConsumer consumer2 = immediate.getBuffer(net.minecraft.client.render.RenderLayer.getEntityTranslucentEmissive(TEXTURE_RING));

               for (JumpCirclesModule.JumpCircle jumpCircle2 : deque) {
                  renderShockwave(matrices, consumer2, jumpCircle2, vec, l, f3, n);
               }
            }
         }
      }
   }

   private static void renderDecal(
      MatrixStack matrices, VertexConsumer consumer, JumpCirclesModule.JumpCircle jumpCircle, Vec3d vec, long l, float tickDelta, float tickDelta2, int n
   ) {
      float f = jumpCircle.ageSeconds(l);
      float f7;
      float f8;
      if (f < 0.25F) {
         float f9 = f / 0.25F;
         f7 = easeOutBack(f9);
         f8 = easeOutCubic(Math.min(1.0F, f9 * 2.0F));
      } else if (f > tickDelta - 0.4F) {
         float f10 = (f - (tickDelta - 0.4F)) / 0.4F;
         f7 = lerp(easeOutCubic(f10), 1.0F, 1.3F);
         f8 = 1.0F - easeInQuad(f10);
      } else {
         f7 = 1.0F;
         f8 = 1.0F;
      }

      float f11 = 0.5F + 0.5F * (float)Math.sin((f * 10.0F));
      f8 *= 0.82F + 0.18F * f11;
      int localX = lerpRgb(n, 16761566, f11 * 0.35F);
      int localZ = withAlpha(localX, f8);
      matrices.push();
      matrices.translate(jumpCircle.x - vec.x, jumpCircle.y + (double)jumpCircle.yLift - vec.y, jumpCircle.z - vec.z);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - jumpCircle.yawDegrees));
      emitQuad(matrices.peek(), consumer, 0.5F * tickDelta2 * f7, localZ);
      matrices.pop();
   }

   private static void renderShockwave(MatrixStack matrices, VertexConsumer consumer, JumpCirclesModule.JumpCircle jumpCircle, Vec3d vec, long l, float tickDelta, int n) {
      float f = jumpCircle.ageSeconds(l);
      if (!(f >= 0.45F)) {
         float f5 = f / 0.45F;
         float f6 = lerp(easeOutCubic(f5), 0.35F, 2.2F) * tickDelta;
         float f7 = 0.85F * (1.0F - easeInQuad(f5));
         int offset = withAlpha(n, f7);
         matrices.push();
         matrices.translate(jumpCircle.x - vec.x, jumpCircle.y + (double)(jumpCircle.yLift * 0.5F) + 0.004F - vec.y, jumpCircle.z - vec.z);
         emitQuad(matrices.peek(), consumer, f6, offset);
         matrices.pop();
      }
   }

   private static int currentBaseRgb(JumpCirclesModule jumpCirclesModule) {
      if (jumpCirclesModule.rainbow.get()) {
         float f = (float)(System.currentTimeMillis() % 4000L) / 4000.0F;
         return MathHelper.hsvToRgb(f, 0.75F, 1.0F) & 16777215;
      } else {
         return jumpCirclesModule.baseRgb();
      }
   }

   private static void emitQuad(Entry entry, VertexConsumer consumer, float f, int n) {
      vertex(entry, consumer, -f, -f, 0.0F, 0.0F, n);
      vertex(entry, consumer, -f, f, 0.0F, 1.0F, n);
      vertex(entry, consumer, f, f, 1.0F, 1.0F, n);
      vertex(entry, consumer, f, -f, 1.0F, 0.0F, n);
   }

   private static void vertex(Entry entry, VertexConsumer consumer, float f, float f5, float f6, float f7, int n) {
      consumer.vertex(entry, f, 0.0F, f5)
         .color(n)
         .texture(f6, f7)
         .overlay(OverlayTexture.DEFAULT_UV)
         .light(15728880)
         .normal(entry, 0.0F, 1.0F, 0.0F);
   }

   private static int withAlpha(int n, float f) {
      int offset = (int)(clamp01(f) * 255.0F);
      return offset << 24 | n & 16777215;
   }

   private static int lerpRgb(int n, int localY, float f) {
      int step = (int)MathHelper.lerp(f, (float)(n >> 16 & 0xFF), (float)(localY >> 16 & 0xFF));
      int step2 = (int)MathHelper.lerp(f, (float)(n >> 8 & 0xFF), (float)(localY >> 8 & 0xFF));
      int n9 = (int)MathHelper.lerp(f, (float)(n & 0xFF), (float)(localY & 0xFF));
      return step << 16 | step2 << 8 | n9;
   }

   private static float clamp01(float f) {
      return f < 0.0F ? 0.0F : (f > 1.0F ? 1.0F : f);
   }

   private static float lerp(float f, float f4, float f5) {
      return f4 + (f5 - f4) * f;
   }

   private static float easeOutCubic(float f) {
      f = clamp01(f);
      float f3 = 1.0F - f;
      return 1.0F - f3 * f3 * f3;
   }

   private static float easeInQuad(float f) {
      f = clamp01(f);
      return f * f;
   }

   private static float easeOutBack(float f) {
      f = clamp01(f);
      float f5 = 2.2F;
      float f6 = f5 + 1.0F;
      float f7 = f - 1.0F;
      return 1.0F + f6 * f7 * f7 * f7 + f5 * f7 * f7;
   }
}
