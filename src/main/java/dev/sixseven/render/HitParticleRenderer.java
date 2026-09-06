package dev.sixseven.render;

import dev.sixseven.module.visuals.HitParticlesModule;
import dev.sixseven.util.Colors;
import java.util.Deque;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public final class HitParticleRenderer {
   private static final Identifier TEXTURE_67 = Identifier.of("sixsevenclient", "textures/misc/67.png");
   private static final int HEART_SEGMENTS = 20;
   private static final float[] HEART_X = new float[21];
   private static final float[] HEART_Y = new float[21];
   private static final float SHOCK_TIME = 0.5F;

   private HitParticleRenderer() {
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d vec, HitParticlesModule hitParticlesModule) {
      Deque deque = hitParticlesModule.particles();
      Deque deque2 = hitParticlesModule.shocks();
      if (!deque.isEmpty() || !deque2.isEmpty()) {
         long l = System.nanoTime();

         while (
            !deque.isEmpty()
               && ((HitParticlesModule.HitParticle)deque.peekFirst()).ageSeconds(l) > ((HitParticlesModule.HitParticle)deque.peekFirst()).lifetime
         ) {
            deque.removeFirst();
         }

         while (!deque2.isEmpty() && ((HitParticlesModule.Shock)deque2.peekFirst()).ageSeconds(l) > 0.5F) {
            deque2.removeFirst();
         }

         if (!deque.isEmpty() || !deque2.isEmpty()) {
            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            Vector3f vector3f = new Vector3f(camera.getHorizontalPlane());
            Vector3f vector3f2 = new Vector3f();
            vector3f.cross(new Vector3f(0.0F, 1.0F, 0.0F), vector3f2);
            if (vector3f2.lengthSquared() < 1.0E-6F) {
               vector3f2.set(1.0F, 0.0F, 0.0F);
            }

            vector3f2.normalize();
            Vector3f vector3f3 = new Vector3f();
            vector3f2.cross(vector3f, vector3f3);
            vector3f3.normalize();
            float f = hitParticlesModule.glowStrength();
            Entry entry = matrices.peek();
            VertexConsumer consumer = immediate.getBuffer(FlatOverlay.LINES);

            for (HitParticlesModule.Shock shock : deque2) {
               renderShock(consumer, entry, vec, vector3f2, vector3f3, shock, l);
            }

            boolean active = false;

            for (HitParticlesModule.HitParticle hitParticle : deque) {
               float f5 = hitParticle.ageSeconds(l);
               if (!(f5 < 0.0F) && !(f5 > hitParticle.lifetime)) {
                  if (hitParticle.styleId == 2) {
                     renderLightning(consumer, entry, vec, vector3f2, vector3f3, hitParticle, f5);
                  } else if (hitParticle.styleId == 3) {
                     active = true;
                  }
               }
            }

            VertexConsumer consumer2 = immediate.getBuffer(FlatOverlay.FILL);

            for (HitParticlesModule.HitParticle hitParticle2 : deque) {
               float f6 = hitParticle2.ageSeconds(l);
               if (!(f6 < 0.0F) && !(f6 > hitParticle2.lifetime)) {
                  if (hitParticle2.styleId == 1) {
                     renderHeart(consumer2, entry, vec, vector3f2, vector3f3, hitParticle2, f6, f);
                  } else if (hitParticle2.styleId == 0) {
                     renderSpark(consumer2, entry, vec, vector3f2, vector3f3, hitParticle2, f6, f);
                  }
               }
            }

            FlatOverlay.flush(immediate);
            if (active) {
               VertexConsumer consumer3 = immediate.getBuffer(RenderLayers.entityTranslucentEmissive(TEXTURE_67));

               for (HitParticlesModule.HitParticle hitParticle3 : deque) {
                  if (hitParticle3.styleId == 3) {
                     float f7 = hitParticle3.ageSeconds(l);
                     if (!(f7 < 0.0F) && !(f7 > hitParticle3.lifetime)) {
                        render67(consumer3, entry, vec, vector3f2, vector3f3, vector3f, hitParticle3, f7);
                     }
                  }
               }
            }
         }
      }
   }

   private static void renderSpark(
      VertexConsumer consumer, Entry entry, Vec3d vec, Vector3f vector3f, Vector3f vector3f2, HitParticlesModule.HitParticle hitParticle, float tickDelta, float tickDelta2
   ) {
      float f = tickDelta / hitParticle.lifetime;
      float f16 = fadeAlpha(f);
      if (!(f16 <= 0.01F)) {
         float f17 = (float)(hitParticle.getX(tickDelta) - vec.x);
         float f18 = (float)(hitParticle.getY(tickDelta) - vec.y);
         float f19 = (float)(hitParticle.getZ(tickDelta) - vec.z);
         float f20 = hitParticle.vy - hitParticle.gravity * tickDelta;
         float f21 = hitParticle.vx * vector3f.x + f20 * vector3f.y + hitParticle.vz * vector3f.z;
         float f22 = hitParticle.vx * vector3f2.x + f20 * vector3f2.y + hitParticle.vz * vector3f2.z;
         float f23 = MathHelper.sqrt(f21 * f21 + f22 * f22);
         float f24;
         float f25;
         if (f23 > 1.0E-4F) {
            f24 = f21 / f23;
            f25 = f22 / f23;
         } else {
            f24 = 0.0F;
            f25 = 1.0F;
         }

         float f26 = 0.35F + 0.65F * (1.0F - f);
         float f27 = 0.28F * hitParticle.size * f26;
         float f28 = 0.055F * hitParticle.size * f26;
         Vector3f vector3f3 = axis(vector3f, vector3f2, f24 * f27, f25 * f27);
         Vector3f vector3f4 = axis(vector3f, vector3f2, -f25 * f28, f24 * f28);
         int n = Colors.withAlpha(Colors.lighten(hitParticle.rgb, 0.55F), f16);
         quad(consumer, entry, f17, f18, f19, vector3f3, vector3f4, n);
         if (tickDelta2 > 0.01F) {
            float f29 = 0.06F * hitParticle.size * f26 * (1.0F + 0.6F * tickDelta2);
            Vector3f vector3f5 = axis(vector3f, vector3f2, f29, f29);
            Vector3f vector3f6 = axis(vector3f, vector3f2, -f29, f29);
            int offset = Colors.withAlpha(hitParticle.rgb, f16 * 0.22F * tickDelta2);
            quad(consumer, entry, f17, f18, f19, vector3f5, vector3f6, offset);
         }
      }
   }

   private static void renderHeart(
      VertexConsumer consumer, Entry entry, Vec3d vec, Vector3f vector3f, Vector3f vector3f2, HitParticlesModule.HitParticle hitParticle, float tickDelta, float tickDelta2
   ) {
      float f = tickDelta / hitParticle.lifetime;
      float f9 = fadeAlpha(f);
      if (!(f9 <= 0.01F)) {
         float f10 = f < 0.2F ? easeOutBack(f / 0.2F) : 1.0F;
         float f11 = 0.16F * hitParticle.size * f10;
         float f12 = (float)(hitParticle.getX(tickDelta) - vec.x);
         float f13 = (float)(hitParticle.getY(tickDelta) - vec.y);
         float f14 = (float)(hitParticle.getZ(tickDelta) - vec.z);
         float f15 = 0.18F * MathHelper.sin((double)(tickDelta * 6.0F + hitParticle.rot));
         int n = Colors.withAlpha(Colors.lighten(hitParticle.rgb, 0.25F), f9);
         fanHeart(consumer, entry, vector3f, vector3f2, f12, f13, f14, f11, f15, n);
         if (tickDelta2 > 0.01F) {
            int offset = Colors.withAlpha(hitParticle.rgb, f9 * 0.3F * tickDelta2);
            fanHeart(consumer, entry, vector3f, vector3f2, f12, f13, f14, f11 * (1.35F + 0.35F * tickDelta2), f15, offset);
         }
      }
   }

   private static void fanHeart(
      VertexConsumer consumer, Entry entry, Vector3f vector3f, Vector3f vector3f2, float f, float f10, float f11, float f12, float f13, int n
   ) {
      for (int offset = 0; offset < 20; offset++) {
         float f14 = (HEART_X[offset] + f13 * HEART_Y[offset]) * f12;
         float f15 = HEART_Y[offset] * f12;
         float f16 = (HEART_X[offset + 1] + f13 * HEART_Y[offset + 1]) * f12;
         float f17 = HEART_Y[offset + 1] * f12;
         emitVertex(consumer, entry, f, f10, f11, n);
         emitVertex(consumer, entry, f + vector3f.x * f14 + vector3f2.x * f15, f10 + vector3f.y * f14 + vector3f2.y * f15, f11 + vector3f.z * f14 + vector3f2.z * f15, n);
         emitVertex(consumer, entry, f + vector3f.x * f16 + vector3f2.x * f17, f10 + vector3f.y * f16 + vector3f2.y * f17, f11 + vector3f.z * f16 + vector3f2.z * f17, n);
         emitVertex(consumer, entry, f, f10, f11, n);
      }
   }

   private static void renderLightning(
      VertexConsumer consumer, Entry entry, Vec3d vec, Vector3f vector3f, Vector3f vector3f2, HitParticlesModule.HitParticle hitParticle, float tickDelta
   ) {
      float f = tickDelta / hitParticle.lifetime;
      float f29 = fadeAlpha(f);
      if (!(f29 <= 0.01F)) {
         float f30 = 0.45F + 0.55F * MathHelper.abs(MathHelper.sin((double)(tickDelta * 42.0F + hitParticle.rot)));
         f29 *= f30;
         float f31 = (float)(hitParticle.ox - vec.x);
         float f32 = (float)(hitParticle.oy - vec.y);
         float f33 = (float)(hitParticle.oz - vec.z);
         float f34 = (float)(hitParticle.getX(tickDelta) - vec.x);
         float f35 = (float)(hitParticle.getY(tickDelta) - vec.y);
         float f36 = (float)(hitParticle.getZ(tickDelta) - vec.z);
         float f37 = f34 - f31;
         float f38 = f35 - f32;
         float f39 = f36 - f33;
         float f40 = f37 * vector3f.x + f38 * vector3f.y + f39 * vector3f.z;
         float f41 = f37 * vector3f2.x + f38 * vector3f2.y + f39 * vector3f2.z;
         float f42 = MathHelper.sqrt(f40 * f40 + f41 * f41);
         float f43;
         float f44;
         if (f42 > 1.0E-4F) {
            f43 = -f41 / f42;
            f44 = f40 / f42;
         } else {
            f43 = 1.0F;
            f44 = 0.0F;
         }

         float f45 = 0.16F * hitParticle.size;
         int n = Colors.withAlpha(Colors.lighten(hitParticle.rgb, 0.6F), f29);
         float f46 = 2.4F * hitParticle.size;
         byte b = 4;
         float f47 = f31;
         float f48 = f32;
         float f49 = f33;

         for (int offset = 1; offset <= b; offset++) {
            float f50 = (float)offset / (float)b;
            float f51 = MathHelper.sin((double)(f50 * (float) Math.PI));
            float f52 = offset == b ? 0.0F : (hash(hitParticle, offset) * 2.0F - 1.0F) * f45 * f51;
            float f53 = f31 + f37 * f50 + (vector3f.x * f43 + vector3f2.x * f44) * f52;
            float f54 = f32 + f38 * f50 + (vector3f.y * f43 + vector3f2.y * f44) * f52;
            float f55 = f33 + f39 * f50 + (vector3f.z * f43 + vector3f2.z * f44) * f52;
            line(consumer, entry, f47, f48, f49, f53, f54, f55, n, f46);
            f47 = f53;
            f48 = f54;
            f49 = f55;
         }
      }
   }

   private static void render67(
      VertexConsumer consumer, Entry entry, Vec3d vec, Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3, HitParticlesModule.HitParticle hitParticle, float tickDelta
   ) {
      float f = tickDelta / hitParticle.lifetime;
      float f14 = fadeAlpha(f);
      if (!(f14 <= 0.01F)) {
         float f15 = f < 0.22F ? easeOutBack(f / 0.22F) : 1.0F;
         float f16 = 0.16F * hitParticle.size * f15;
         float f17 = hitParticle.rot + hitParticle.rotSpeed * tickDelta * 0.5F;
         float f18 = MathHelper.cos((double)f17);
         float f19 = MathHelper.sin((double)f17);
         Vector3f vector3f4 = axis(vector3f, vector3f2, f18 * f16, f19 * f16);
         Vector3f vector3f5 = axis(vector3f, vector3f2, -f19 * f16, f18 * f16);
         float f20 = (float)(hitParticle.getX(tickDelta) - vec.x);
         float f21 = (float)(hitParticle.getY(tickDelta) - vec.y);
         float f22 = (float)(hitParticle.getZ(tickDelta) - vec.z);
         int n = Colors.withAlpha(hitParticle.rgb, f14);
         float f23 = -vector3f3.x;
         float f24 = -vector3f3.y;
         float f25 = -vector3f3.z;
         texVertex(consumer, entry, f20 - vector3f4.x - vector3f5.x, f21 - vector3f4.y - vector3f5.y, f22 - vector3f4.z - vector3f5.z, 0.0F, 0.0F, n, f23, f24, f25);
         texVertex(consumer, entry, f20 - vector3f4.x + vector3f5.x, f21 - vector3f4.y + vector3f5.y, f22 - vector3f4.z + vector3f5.z, 0.0F, 1.0F, n, f23, f24, f25);
         texVertex(consumer, entry, f20 + vector3f4.x + vector3f5.x, f21 + vector3f4.y + vector3f5.y, f22 + vector3f4.z + vector3f5.z, 1.0F, 1.0F, n, f23, f24, f25);
         texVertex(consumer, entry, f20 + vector3f4.x - vector3f5.x, f21 + vector3f4.y - vector3f5.y, f22 + vector3f4.z - vector3f5.z, 1.0F, 0.0F, n, f23, f24, f25);
      }
   }

   private static void renderShock(VertexConsumer consumer, Entry entry, Vec3d vec, Vector3f vector3f, Vector3f vector3f2, HitParticlesModule.Shock shock, long l) {
      float f = shock.ageSeconds(l);
      if (!(f < 0.0F) && !(f >= 0.5F)) {
         float f17 = f / 0.5F;
         float f18 = 0.15F + easeOutCubic(f17) * 1.15F;
         float f19 = 0.8F * (1.0F - easeInQuad(f17));
         int n = Colors.withAlpha(Colors.lighten(shock.rgb, 0.2F), f19);
         float f20 = (float)(shock.x - vec.x);
         float f21 = (float)(shock.y - vec.y);
         float f22 = (float)(shock.z - vec.z);
         byte b = 28;
         float f23 = 0.0F;
         float f24 = 0.0F;
         float f25 = 0.0F;

         for (int offset = 0; offset <= b; offset++) {
            float f26 = (float)offset / (float)b * (float) (Math.PI * 2);
            float f27 = MathHelper.cos((double)f26) * f18;
            float f28 = MathHelper.sin((double)f26) * f18;
            float f29 = f20 + vector3f.x * f27 + vector3f2.x * f28;
            float f30 = f21 + vector3f.y * f27 + vector3f2.y * f28;
            float f31 = f22 + vector3f.z * f27 + vector3f2.z * f28;
            if (offset > 0) {
               line(consumer, entry, f23, f24, f25, f29, f30, f31, n, 2.2F);
            }

            f23 = f29;
            f24 = f30;
            f25 = f31;
         }
      }
   }

   private static Vector3f axis(Vector3f vector3f, Vector3f vector3f2, float f, float f3) {
      return new Vector3f(vector3f.x * f + vector3f2.x * f3, vector3f.y * f + vector3f2.y * f3, vector3f.z * f + vector3f2.z * f3);
   }

   private static void quad(VertexConsumer consumer, Entry entry, float f, float f4, float f5, Vector3f vector3f, Vector3f vector3f2, int n) {
      emitVertex(consumer, entry, f - vector3f.x - vector3f2.x, f4 - vector3f.y - vector3f2.y, f5 - vector3f.z - vector3f2.z, n);
      emitVertex(consumer, entry, f + vector3f.x - vector3f2.x, f4 + vector3f.y - vector3f2.y, f5 + vector3f.z - vector3f2.z, n);
      emitVertex(consumer, entry, f + vector3f.x + vector3f2.x, f4 + vector3f.y + vector3f2.y, f5 + vector3f.z + vector3f2.z, n);
      emitVertex(consumer, entry, f - vector3f.x + vector3f2.x, f4 - vector3f.y + vector3f2.y, f5 - vector3f.z + vector3f2.z, n);
   }

   private static void emitVertex(VertexConsumer consumer, Entry entry, float f, float f4, float f5, int n) {
      consumer.vertex(entry, f, f4, f5).color(n);
   }

   private static void line(VertexConsumer consumer, Entry entry, float f, float f8, float f9, float f10, float f11, float f12, int n, float f13) {
      Vector3f vector3f = new Vector3f(f10 - f, f11 - f8, f12 - f9);
      if (vector3f.lengthSquared() > 1.0E-9F) {
         vector3f.normalize();
      } else {
         vector3f.set(0.0F, 1.0F, 0.0F);
      }

      consumer.vertex(entry, f, f8, f9).color(n).normal(entry, vector3f).lineWidth(f13);
      consumer.vertex(entry, f10, f11, f12).color(n).normal(entry, vector3f).lineWidth(f13);
   }

   private static void texVertex(
      VertexConsumer consumer, Entry entry, float f, float f9, float f10, float f11, float f12, int n, float f13, float f14, float f15
   ) {
      consumer.vertex(entry, f, f9, f10)
         .color(n)
         .texture(f11, f12)
         .overlay(OverlayTexture.DEFAULT_UV)
         .light(15728880)
         .normal(entry, f13, f14, f15);
   }

   private static float hash(HitParticlesModule.HitParticle hitParticle, int n) {
      float f = MathHelper.sin((double)((float)(hitParticle.ox * 12.9898 + hitParticle.oz * 78.233 + (double)hitParticle.rot * 3.17 + (double)n * 43.123)))
         * 43758.547F;
      return f - (float)MathHelper.floor(f);
   }

   private static float fadeAlpha(float f) {
      f = clamp01(f);
      return f < 0.12F ? f / 0.12F : 1.0F - easeInQuad((f - 0.12F) / 0.88F);
   }

   private static float clamp01(float f) {
      return f < 0.0F ? 0.0F : (f > 1.0F ? 1.0F : f);
   }

   private static float easeInQuad(float f) {
      f = clamp01(f);
      return f * f;
   }

   private static float easeOutCubic(float f) {
      f = clamp01(f);
      float f3 = 1.0F - f;
      return 1.0F - f3 * f3 * f3;
   }

   private static float easeOutBack(float f) {
      f = clamp01(f);
      float f5 = 2.4F;
      float f6 = f5 + 1.0F;
      float f7 = f - 1.0F;
      return 1.0F + f6 * f7 * f7 * f7 + f5 * f7 * f7;
   }

   static {
      for (int n = 0; n <= 20; n++) {
         double d = (double)n / 20.0 * Math.PI * 2.0;
         double d2 = 16.0 * Math.pow(Math.sin(d), 3.0);
         double d3 = 13.0 * Math.cos(d) - 5.0 * Math.cos(2.0 * d) - 2.0 * Math.cos(3.0 * d) - Math.cos(4.0 * d);
         HEART_X[n] = (float)(d2 / 17.0);
         HEART_Y[n] = (float)(d3 / 17.0);
      }
   }
}
