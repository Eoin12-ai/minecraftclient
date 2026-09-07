package dev.sixseven.render;

import dev.sixseven.module.visuals.CustomAccessoriesModule;
import dev.sixseven.util.Colors;
import java.util.Deque;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public final class AccessoryRenderer {
   private static final Identifier TEXTURE_67 = Identifier.of("sixsevenclient", "textures/misc/67.png");
   private static final int CAPE_COLS = 7;
   private static final int CAPE_ROWS = 9;
   private static final float CAPE_WIDTH = 0.62F;
   private static final float CAPE_LENGTH = 1.05F;
   private static final RenderLayer CAPE_FILL = RenderLayer.getDebugQuads();

   private AccessoryRenderer() {
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d vec, CustomAccessoriesModule customAccessoriesModule) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (player != null && client.world != null) {
         boolean ok = client.options.getPerspective().isFirstPerson() && !customAccessoriesModule.firstPerson.get();
         float f = client.getRenderTickCounter().getTickProgress(false);
         double d = MathHelper.lerp((double)f, player.lastX, player.getX());
         double coord = MathHelper.lerp((double)f, player.lastY, player.getY());
         double currentScore = MathHelper.lerp((double)f, player.lastZ, player.getZ());
         float f10 = MathHelper.lerpAngleDegrees(f, player.lastBodyYaw, player.bodyYaw);
         float f11 = player.getHeight();
         float f12 = (float)Math.hypot(player.getX() - player.lastX, player.getZ() - player.lastZ);
         long l = System.nanoTime();
         float f13 = (float)(l % 1000000000000L) / 1.0E9F;
         int n = customAccessoriesModule.currentRgb();
         float f14 = customAccessoriesModule.glowStrength();
         Camera camera = client.gameRenderer.getCamera();
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
         Entry entry = matrices.peek();
         boolean found = customAccessoriesModule.cape.get() && !ok;
         boolean found2 = found && customAccessoriesModule.capeStyle.is("67");
         float[] f15 = null;
         float[] f16 = null;
         float[] f17 = null;
         if (found) {
            byte b = 80;
            f15 = new float[b];
            f16 = new float[b];
            f17 = new float[b];
            buildCape(f15, f16, f17, d, coord, currentScore, f11, f10, f12, f13, customAccessoriesModule.capePhysics.get(), vec);
         }

         VertexConsumer consumer = immediate.getBuffer(FlatOverlay.LINES);
         if (!ok && customAccessoriesModule.trail.get() && customAccessoriesModule.trailStyle.is("Echo")) {
            renderTrailEcho(consumer, entry, vec, customAccessoriesModule, l, n);
         }

         if (customAccessoriesModule.aura.get() && customAccessoriesModule.auraStyle.is("Ring")) {
            renderAuraRing(consumer, entry, vec, d, coord, currentScore, f13, n, f14);
         }

         if (found && !found2) {
            VertexConsumer consumer2 = immediate.getBuffer(CAPE_FILL);
            fillCape(consumer2, entry, f15, f16, f17, customAccessoriesModule.capeStyle.get(), f13, n);
            immediate.draw(CAPE_FILL);
         }

         VertexConsumer consumer3 = immediate.getBuffer(FlatOverlay.FILL);
         if (!ok && customAccessoriesModule.trail.get() && customAccessoriesModule.trailStyle.is("Ribbon")) {
            renderTrailRibbon(consumer3, entry, vec, vector3f2, customAccessoriesModule, l, n, f14);
         }

         if (!ok && customAccessoriesModule.trail.get() && customAccessoriesModule.trailStyle.is("Sparkle")) {
            renderTrailSparkle(consumer3, entry, vec, vector3f2, vector3f3, customAccessoriesModule, l, n, f14);
         }

         if (customAccessoriesModule.aura.get() && customAccessoriesModule.auraStyle.is("Orbit")) {
            renderAuraOrbit(consumer3, entry, vec, vector3f2, vector3f3, d, coord, currentScore, f13, n, f14);
         }

         FlatOverlay.flush(immediate);
         boolean ok2 = customAccessoriesModule.crown.get() && !ok;
         if (found2 || ok2) {
            VertexConsumer consumer4 = immediate.getBuffer(net.minecraft.client.render.RenderLayer.getEntityTranslucentEmissive(TEXTURE_67));
            if (found2) {
               texCape(consumer4, entry, f15, f16, f17, vector3f, n);
            }

            if (ok2) {
               renderCrown(consumer4, entry, vec, vector3f2, vector3f3, vector3f, d, coord, currentScore, f11, f13, n);
            }
         }
      }
   }

   private static void buildCape(
      float[] f,
      float[] f29,
      float[] f30,
      double d,
      double coord,
      double currentScore,
      float f31,
      float f32,
      float f33,
      float f34,
      boolean value,
      Vec3d vec
   ) {
      float f35 = f32 * (float) (Math.PI / 180.0);
      float f36 = -(float)Math.sin(f35);
      float f37 = (float)Math.cos(f35);
      float f38 = f37;
      float f39 = -f36;
      float f40 = -f36;
      float f41 = -f37;
      float f42 = (float)coord + f31 * 0.78F;
      float f43 = (float)d + f40 * 0.14F;
      float f44 = (float)currentScore + f41 * 0.14F;
      float f45 = value ? Math.min(f33 * 5.0F, 1.05F) : 0.0F;
      float f46 = 0.1F;
      float f47 = value ? 0.06F + f45 * 0.14F : 0.02F;

      for (int n = 0; n <= 9; n++) {
         float f48 = (float)n / 9.0F;
         float f49 = f48 * f48;
         float f50 = (f46 + f45) * f49;

         for (int localX = 0; localX <= 7; localX++) {
            float f51 = (float)localX / 7.0F;
            float f52 = (f51 - 0.5F) * 0.62F;
            float f53 = value ? (float)Math.sin((f34 * 6.5F - f48 * 4.2F + (float)localX * 0.7F)) * f47 * f48 : 0.0F;
            float f54 = value ? (float)Math.sin((f34 * 5.0F + f48 * 3.5F)) * 0.03F * f48 : 0.0F;
            float f55 = f50 + f53;
            int localZ = n * 8 + localX;
            f[localZ] = f43 + f38 * (f52 + f54) + f40 * f55 - (float)vec.x;
            f29[localZ] = f42 - f48 * 1.05F - (float)vec.y;
            f30[localZ] = f44 + f39 * (f52 + f54) + f41 * f55 - (float)vec.z;
         }
      }
   }

   private static void fillCape(VertexConsumer consumer, Entry entry, float[] f, float[] f7, float[] f8, String str, float f9, int n) {
      boolean ok = str.equals("Grid");
      boolean found = str.equals("Wave");
      int n16 = Colors.lighten(n, 0.22F);
      int n17 = darkenRgb(n, 0.18F);

      for (int n18 = 0; n18 < 9; n18++) {
         for (int n19 = 0; n19 < 7; n19++) {
            int n20 = n18 * 8 + n19;
            int n21 = n20 + 1;
            int n22 = n20 + 8;
            int n23 = n22 + 1;
            if (ok) {
               boolean found2 = (n18 + n19 & 1) == 0;
               int n24 = found2 ? Colors.lighten(n, 0.35F) : darkenRgb(n, 0.5F);
               int n25 = found2 ? withA(n24, 0.9F) : withA(n24, 0.6F);
               emitVertex(consumer, entry, f[n20], f7[n20], f8[n20], n25);
               emitVertex(consumer, entry, f[n22], f7[n22], f8[n22], n25);
               emitVertex(consumer, entry, f[n23], f7[n23], f8[n23], n25);
               emitVertex(consumer, entry, f[n21], f7[n21], f8[n21], n25);
            } else {
               float f10 = (float)n18 / 9.0F;
               float f11 = (float)(n18 + 1) / 9.0F;
               int n26 = lerpRgb(n16, n17, f10);
               int n27 = lerpRgb(n16, n17, f11);
               if (found) {
                  n26 = lerpRgb(n26, 16777215, highlight(f10, f9));
                  n27 = lerpRgb(n27, 16777215, highlight(f11, f9));
               }

               int n28 = withA(n26, 0.86F - 0.08F * f10);
               int n29 = withA(n27, 0.86F - 0.08F * f11);
               emitVertex(consumer, entry, f[n20], f7[n20], f8[n20], n28);
               emitVertex(consumer, entry, f[n22], f7[n22], f8[n22], n29);
               emitVertex(consumer, entry, f[n23], f7[n23], f8[n23], n29);
               emitVertex(consumer, entry, f[n21], f7[n21], f8[n21], n28);
            }
         }
      }
   }

   private static void texCape(VertexConsumer consumer, Entry entry, float[] f, float[] f11, float[] f12, Vector3f vector3f, int n) {
      int n9 = withA(n, 0.98F);
      float f13 = -vector3f.x;
      float f14 = -vector3f.y;
      float f15 = -vector3f.z;

      for (int n10 = 0; n10 < 9; n10++) {
         float f16 = (float)n10 / 9.0F;
         float f17 = (float)(n10 + 1) / 9.0F;

         for (int n11 = 0; n11 < 7; n11++) {
            float f18 = (float)n11 / 7.0F;
            float f19 = (float)(n11 + 1) / 7.0F;
            int n12 = n10 * 8 + n11;
            int n13 = n12 + 1;
            int n14 = n12 + 8;
            int n15 = n14 + 1;
            tex(consumer, entry, f[n12], f11[n12], f12[n12], f18, f16, n9, f13, f14, f15);
            tex(consumer, entry, f[n14], f11[n14], f12[n14], f18, f17, n9, f13, f14, f15);
            tex(consumer, entry, f[n15], f11[n15], f12[n15], f19, f17, n9, f13, f14, f15);
            tex(consumer, entry, f[n13], f11[n13], f12[n13], f19, f16, n9, f13, f14, f15);
         }
      }
   }

   private static float highlight(float f, float f4) {
      float f5 = (float)Math.sin(((f - f4 * 0.35F % 1.0F) * (float) (Math.PI * 2)));
      return Math.max(0.0F, f5) * 0.5F;
   }

   private static void renderTrailRibbon(
      VertexConsumer consumer, Entry entry, Vec3d vec, Vector3f vector3f, CustomAccessoriesModule customAccessoriesModule, long l, int n, float tickDelta
   ) {
      Deque<CustomAccessoriesModule.TrailNode> deque = customAccessoriesModule.trailNodes();
      if (deque.size() >= 2) {
         float f = Math.max(0.2F, customAccessoriesModule.trailLength.getFloat());
         float f24 = 0.28F * (0.7F + 0.6F * tickDelta);
         CustomAccessoriesModule.TrailNode trailNode = null;
         float f25 = 0.0F;
         float f26 = 0.0F;
         float f27 = 0.0F;
         float f28 = 0.0F;
         float f29 = 0.0F;
         float f30 = 0.0F;
         float f31 = 0.0F;
         float f32 = 0.0F;

         for (CustomAccessoriesModule.TrailNode trailNode2 : deque) {
            float f33 = trailNode2.ageSeconds(l);
            if (f33 > f) {
               trailNode = null;
            } else {
               float f34 = f33 / f;
               float f35 = f24 * (1.0F - f34);
               float f36 = (1.0F - f34) * (1.0F - f34);
               float f37 = (float)(trailNode2.x - vec.x);
               float f38 = (float)(trailNode2.y - vec.y);
               float f39 = (float)(trailNode2.z - vec.z);
               float f40 = f37 - vector3f.x * f35;
               float f41 = f38 - vector3f.y * f35;
               float f42 = f39 - vector3f.z * f35;
               float f43 = f37 + vector3f.x * f35;
               float f44 = f38 + vector3f.y * f35;
               float f45 = f39 + vector3f.z * f35;
               if (trailNode != null) {
                  int localX = withA(n, f26 * 0.85F);
                  int localZ = withA(Colors.lighten(n, 0.25F), f36 * 0.85F);
                  emitVertex(consumer, entry, f27, f28, f29, localX);
                  emitVertex(consumer, entry, f40, f41, f42, localZ);
                  emitVertex(consumer, entry, f43, f44, f45, localZ);
                  emitVertex(consumer, entry, f30, f31, f32, localX);
               }

               trailNode = trailNode2;
               f26 = f36;
               f27 = f40;
               f28 = f41;
               f29 = f42;
               f30 = f43;
               f31 = f44;
               f32 = f45;
            }
         }
      }
   }

   private static void renderTrailSparkle(
      VertexConsumer consumer, Entry entry, Vec3d vec, Vector3f vector3f, Vector3f vector3f2, CustomAccessoriesModule customAccessoriesModule, long l, int n, float tickDelta
   ) {
      Deque<CustomAccessoriesModule.TrailNode> deque = customAccessoriesModule.trailNodes();
      float f = Math.max(0.2F, customAccessoriesModule.trailLength.getFloat());
      int localZ = 0;

      for (CustomAccessoriesModule.TrailNode trailNode : deque) {
         int localY = localZ++;
         if ((localY & 1) != 1) {
            float f13 = trailNode.ageSeconds(l);
            if (!(f13 > f)) {
               float f14 = f13 / f;
               float f15 = 1.0F - f14;
               if (!(f15 <= 0.02F)) {
                  float f16 = f14 < 0.15F ? f14 / 0.15F : 1.0F;
                  float f17 = (hash(trailNode.nanos, 1) - 0.5F) * 0.5F;
                  float f18 = (hash(trailNode.nanos, 2) - 0.5F) * 0.4F + f14 * 0.35F;
                  float f19 = (hash(trailNode.nanos, 3) - 0.5F) * 0.5F;
                  float f20 = (float)(trailNode.x - vec.x) + f17;
                  float f21 = (float)(trailNode.y - vec.y) + f18;
                  float f22 = (float)(trailNode.z - vec.z) + f19;
                  float f23 = 0.05F * f16 * (0.7F + 0.6F * tickDelta) * (0.6F + 0.8F * (1.0F - f14));
                  int step = withA(Colors.lighten(n, 0.5F), f15);
                  diamond(consumer, entry, vector3f, vector3f2, f20, f21, f22, f23, step);
                  if (tickDelta > 0.01F) {
                     diamond(consumer, entry, vector3f, vector3f2, f20, f21, f22, f23 * 2.0F, withA(n, f15 * 0.25F * tickDelta));
                  }
               }
            }
         }
      }
   }

   private static void renderTrailEcho(VertexConsumer consumer, Entry entry, Vec3d vec, CustomAccessoriesModule customAccessoriesModule, long l, int n) {
      Deque<CustomAccessoriesModule.TrailNode> deque = customAccessoriesModule.trailNodes();
      float f = Math.max(0.2F, customAccessoriesModule.trailLength.getFloat());
      int localZ = 0;

      for (CustomAccessoriesModule.TrailNode trailNode : deque) {
         int localY = localZ++;
         if (localY % 5 == 0) {
            float f11 = trailNode.ageSeconds(l);
            if (!(f11 > f)) {
               float f12 = f11 / f;
               float f13 = (1.0F - f12) * 0.8F;
               if (!(f13 <= 0.02F)) {
                  int step = withA(n, f13);
                  float f14 = (float)(trailNode.x - 0.32 - vec.x);
                  float f15 = (float)(trailNode.x + 0.32 - vec.x);
                  float f16 = (float)(trailNode.y - 0.9 - vec.y);
                  float f17 = (float)(trailNode.y + 0.9 - vec.y);
                  float f18 = (float)(trailNode.z - 0.32 - vec.z);
                  float f19 = (float)(trailNode.z + 0.32 - vec.z);
                  box(consumer, entry, f14, f16, f18, f15, f17, f19, step, 1.6F);
               }
            }
         }
      }
   }

   private static void box(VertexConsumer consumer, Entry entry, float f, float f8, float f9, float f10, float f11, float f12, int n, float f13) {
      line(consumer, entry, f, f8, f9, f10, f8, f9, n, f13);
      line(consumer, entry, f10, f8, f9, f10, f8, f12, n, f13);
      line(consumer, entry, f10, f8, f12, f, f8, f12, n, f13);
      line(consumer, entry, f, f8, f12, f, f8, f9, n, f13);
      line(consumer, entry, f, f11, f9, f10, f11, f9, n, f13);
      line(consumer, entry, f10, f11, f9, f10, f11, f12, n, f13);
      line(consumer, entry, f10, f11, f12, f, f11, f12, n, f13);
      line(consumer, entry, f, f11, f12, f, f11, f9, n, f13);
      line(consumer, entry, f, f8, f9, f, f11, f9, n, f13);
      line(consumer, entry, f10, f8, f9, f10, f11, f9, n, f13);
      line(consumer, entry, f10, f8, f12, f10, f11, f12, n, f13);
      line(consumer, entry, f, f8, f12, f, f11, f12, n, f13);
   }

   private static void renderAuraOrbit(
      VertexConsumer consumer, Entry entry, Vec3d vec, Vector3f vector3f, Vector3f vector3f2, double d, double coord, double currentScore, float tickDelta, int n, float tickDelta2
   ) {
      byte b = 8;
      float f = 0.72F;
      float f12 = (float)(coord - vec.y) + 0.12F;
      float f13 = (float)(d - vec.x);
      float f14 = (float)(currentScore - vec.z);

      for (int localX = 0; localX < b; localX++) {
         float f15 = tickDelta * 1.7F + (float)localX * ((float) (Math.PI * 2) / (float)b);
         float f16 = (float)Math.cos(f15) * f;
         float f17 = (float)Math.sin(f15) * f;
         float f18 = f12 + (float)Math.sin((tickDelta * 2.4F + (float)localX)) * 0.18F + 0.25F;
         float f19 = f13 + f16;
         float f20 = f14 + f17;
         float f21 = 0.07F * (0.75F + 0.5F * tickDelta2);
         int localZ = withA(Colors.lighten(n, 0.45F), 0.95F);
         diamond(consumer, entry, vector3f, vector3f2, f19, f18, f20, f21, localZ);
         if (tickDelta2 > 0.01F) {
            diamond(consumer, entry, vector3f, vector3f2, f19, f18, f20, f21 * 2.1F, withA(n, 0.22F * tickDelta2));
         }
      }
   }

   private static void renderAuraRing(VertexConsumer consumer, Entry entry, Vec3d vec, double d, double coord, double currentScore, float tickDelta, int n, float tickDelta2) {
      float f = (float)(d - vec.x);
      float f8 = (float)(currentScore - vec.z);

      for (int localX = 0; localX < 2; localX++) {
         float f9 = tickDelta * 0.9F + (float)localX * 0.5F;
         float f10 = f9 - (float)Math.floor((double)f9);
         float f11 = 0.4F + f10 * 1.1F;
         float f12 = (1.0F - f10) * (0.7F + 0.3F * tickDelta2);
         if (!(f12 <= 0.02F)) {
            int localZ = withA(Colors.lighten(n, 0.2F), f12);
            float f13 = (float)(coord - vec.y) + 0.04F + (float)localX * 0.02F;
            ring(consumer, entry, f, f13, f8, f11, 40, localZ, 2.4F);
         }
      }
   }

   private static void renderCrown(
      VertexConsumer consumer,
      Entry entry,
      Vec3d vec,
      Vector3f vector3f,
      Vector3f vector3f2,
      Vector3f vector3f3,
      double d,
      double coord,
      double currentScore,
      float tickDelta,
      float tickDelta2,
      int n
   ) {
      float f = (float)Math.sin((tickDelta2 * 2.0F)) * 0.06F;
      float f12 = (float)(d - vec.x);
      float f13 = (float)(coord - vec.y) + tickDelta + 0.55F + f;
      float f14 = (float)(currentScore - vec.z);
      float f15 = 0.34F;
      float f16 = tickDelta2 * 1.4F;
      float f17 = (float)Math.cos(f16);
      float f18 = (float)Math.sin(f16);
      Vector3f vector3f4 = axis(vector3f, vector3f2, f17 * f15, f18 * f15);
      Vector3f vector3f5 = axis(vector3f, vector3f2, -f18 * f15, f17 * f15);
      int offset = withA(n, 0.98F);
      float f19 = -vector3f3.x;
      float f20 = -vector3f3.y;
      float f21 = -vector3f3.z;
      tex(consumer, entry, f12 - vector3f4.x - vector3f5.x, f13 - vector3f4.y - vector3f5.y, f14 - vector3f4.z - vector3f5.z, 0.0F, 0.0F, offset, f19, f20, f21);
      tex(consumer, entry, f12 - vector3f4.x + vector3f5.x, f13 - vector3f4.y + vector3f5.y, f14 - vector3f4.z + vector3f5.z, 0.0F, 1.0F, offset, f19, f20, f21);
      tex(consumer, entry, f12 + vector3f4.x + vector3f5.x, f13 + vector3f4.y + vector3f5.y, f14 + vector3f4.z + vector3f5.z, 1.0F, 1.0F, offset, f19, f20, f21);
      tex(consumer, entry, f12 + vector3f4.x - vector3f5.x, f13 + vector3f4.y - vector3f5.y, f14 + vector3f4.z - vector3f5.z, 1.0F, 0.0F, offset, f19, f20, f21);
   }

   private static Vector3f axis(Vector3f vector3f, Vector3f vector3f2, float f, float f3) {
      return new Vector3f(vector3f.x * f + vector3f2.x * f3, vector3f.y * f + vector3f2.y * f3, vector3f.z * f + vector3f2.z * f3);
   }

   private static void diamond(VertexConsumer consumer, Entry entry, Vector3f vector3f, Vector3f vector3f2, float f, float f5, float f6, float f7, int n) {
      Vector3f vector3f3 = axis(vector3f, vector3f2, f7, f7);
      Vector3f vector3f4 = axis(vector3f, vector3f2, -f7, f7);
      emitVertex(consumer, entry, f - vector3f3.x, f5 - vector3f3.y, f6 - vector3f3.z, n);
      emitVertex(consumer, entry, f + vector3f4.x, f5 + vector3f4.y, f6 + vector3f4.z, n);
      emitVertex(consumer, entry, f + vector3f3.x, f5 + vector3f3.y, f6 + vector3f3.z, n);
      emitVertex(consumer, entry, f - vector3f4.x, f5 - vector3f4.y, f6 - vector3f4.z, n);
   }

   private static void ring(VertexConsumer consumer, Entry entry, float f, float f11, float f12, float f13, int n, int localX, float f14) {
      float f15 = f + f13;
      float f16 = f12;

      for (int localZ = 1; localZ <= n; localZ++) {
         float f17 = (float)localZ / (float)n * (float) (Math.PI * 2);
         float f18 = f + (float)Math.cos(f17) * f13;
         float f19 = f12 + (float)Math.sin(f17) * f13;
         line(consumer, entry, f15, f11, f16, f18, f11, f19, localX, f14);
         f15 = f18;
         f16 = f19;
      }
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

      consumer.vertex(entry, f, f8, f9).color(n).normal(entry, vector3f);
      consumer.vertex(entry, f10, f11, f12).color(n).normal(entry, vector3f);
   }

   private static void tex(
      VertexConsumer consumer, Entry entry, float f, float f9, float f10, float f11, float f12, int n, float f13, float f14, float f15
   ) {
      consumer.vertex(entry, f, f9, f10)
         .color(n)
         .texture(f11, f12)
         .overlay(OverlayTexture.DEFAULT_UV)
         .light(15728880)
         .normal(entry, f13, f14, f15);
   }

   private static int withA(int n, float f) {
      return Colors.withAlpha(n, f);
   }

   private static int lerpRgb(int n, int localY, float f) {
      f = f < 0.0F ? 0.0F : (f > 1.0F ? 1.0F : f);
      int step = (int)MathHelper.lerp(f, (float)(n >> 16 & 0xFF), (float)(localY >> 16 & 0xFF));
      int step2 = (int)MathHelper.lerp(f, (float)(n >> 8 & 0xFF), (float)(localY >> 8 & 0xFF));
      int n9 = (int)MathHelper.lerp(f, (float)(n & 0xFF), (float)(localY & 0xFF));
      return step << 16 | step2 << 8 | n9;
   }

   private static int darkenRgb(int n, float f) {
      return lerpRgb(n, 0, f);
   }

   private static float hash(long l, int n) {
      float f = (float)Math.sin(((float)(l % 100000L) * 0.0131F + (float)n * 12.9898F)) * 43758.547F;
      return f - (float)MathHelper.floor(f);
   }
}
