package dev.sixseven.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import net.minecraft.util.math.Vec3d;

public final class EspBoxRenderer {
   private static final float TRACER_START = 0.35F;
   private static final float TRACER_NEAR = 0.1F;

   private EspBoxRenderer() {
   }

   public static void outline(
      Immediate immediate, MatrixStack matrices, Vec3d vec, double d, double coord, double currentScore, double coord3, double coord4, double coord5, int n, float tickDelta
   ) {
      VertexConsumer consumer = immediate.getBuffer(FlatOverlay.LINES);
      Entry entry = matrices.peek();
      float f = (float)(d - vec.x);
      float f7 = (float)(coord - vec.y);
      float f8 = (float)(currentScore - vec.z);
      float f9 = (float)(coord3 - vec.x);
      float f10 = (float)(coord4 - vec.y);
      float f11 = (float)(coord5 - vec.z);
      line(consumer, entry, f, f7, f8, f9, f7, f8, n, tickDelta);
      line(consumer, entry, f9, f7, f8, f9, f7, f11, n, tickDelta);
      line(consumer, entry, f9, f7, f11, f, f7, f11, n, tickDelta);
      line(consumer, entry, f, f7, f11, f, f7, f8, n, tickDelta);
      line(consumer, entry, f, f10, f8, f9, f10, f8, n, tickDelta);
      line(consumer, entry, f9, f10, f8, f9, f10, f11, n, tickDelta);
      line(consumer, entry, f9, f10, f11, f, f10, f11, n, tickDelta);
      line(consumer, entry, f, f10, f11, f, f10, f8, n, tickDelta);
      line(consumer, entry, f, f7, f8, f, f10, f8, n, tickDelta);
      line(consumer, entry, f9, f7, f8, f9, f10, f8, n, tickDelta);
      line(consumer, entry, f9, f7, f11, f9, f10, f11, n, tickDelta);
      line(consumer, entry, f, f7, f11, f, f10, f11, n, tickDelta);
   }

   public static void fill(
      Immediate immediate, MatrixStack matrices, Vec3d vec, double d, double coord, double currentScore, double coord3, double coord4, double coord5, int n
   ) {
      VertexConsumer consumer = immediate.getBuffer(FlatOverlay.FILL);
      Entry entry = matrices.peek();
      float f = (float)(d - vec.x);
      float f7 = (float)(coord - vec.y);
      float f8 = (float)(currentScore - vec.z);
      float f9 = (float)(coord3 - vec.x);
      float f10 = (float)(coord4 - vec.y);
      float f11 = (float)(coord5 - vec.z);
      quad(consumer, entry, n, f, f7, f8, f9, f7, f8, f9, f7, f11, f, f7, f11);
      quad(consumer, entry, n, f, f10, f8, f, f10, f11, f9, f10, f11, f9, f10, f8);
      quad(consumer, entry, n, f, f7, f8, f, f10, f8, f9, f10, f8, f9, f7, f8);
      quad(consumer, entry, n, f, f7, f11, f9, f7, f11, f9, f10, f11, f, f10, f11);
      quad(consumer, entry, n, f, f7, f8, f, f7, f11, f, f10, f11, f, f10, f8);
      quad(consumer, entry, n, f9, f7, f8, f9, f10, f8, f9, f10, f11, f9, f7, f11);
   }

   public static void tracer(Immediate immediate, MatrixStack matrices, Vec3d vec, net.minecraft.util.math.Vec3d vector3fc, double d, double coord, double currentScore, int n, float tickDelta) {
      float f = vector3fc.x;
      float f13 = vector3fc.y;
      float f14 = vector3fc.z;
      float f15 = (float)Math.sqrt((double)(f * f + f13 * f13 + f14 * f14));
      if (f15 > 1.0E-6F) {
         f /= f15;
         f13 /= f15;
         f14 /= f15;
      }

      float f16 = f * 0.35F;
      float f17 = f13 * 0.35F;
      float f18 = f14 * 0.35F;
      float f19 = (float)(d - vec.x);
      float f20 = (float)(coord - vec.y);
      float f21 = (float)(currentScore - vec.z);
      float f22 = f19 * f + f20 * f13 + f21 * f14;
      if (f22 < 0.1F) {
         float f23 = -0.25F / (f22 - 0.35F);
         f19 = f16 + (f19 - f16) * f23;
         f20 = f17 + (f20 - f17) * f23;
         f21 = f18 + (f21 - f18) * f23;
      }

      VertexConsumer consumer = immediate.getBuffer(FlatOverlay.LINES);
      Entry entry = matrices.peek();
      line(consumer, entry, f16, f17, f18, f19, f20, f21, n, tickDelta);
   }

   public static void ring(Immediate immediate, MatrixStack matrices, Vec3d vec, double d, double coord, double currentScore, double coord3, int n, int localX, float tickDelta) {
      VertexConsumer consumer = immediate.getBuffer(FlatOverlay.LINES);
      Entry entry = matrices.peek();
      float f = (float)(coord - vec.y);
      double coord4 = d + coord3;
      double coord5 = currentScore;

      for (int localZ = 1; localZ <= n; localZ++) {
         double coord6 = (Math.PI * 2) * (double)localZ / (double)n;
         double coord7 = d + Math.cos(coord6) * coord3;
         double coord8 = currentScore + Math.sin(coord6) * coord3;
         line(
            consumer,
            entry,
            (float)(coord4 - vec.x),
            f,
            (float)(coord5 - vec.z),
            (float)(coord7 - vec.x),
            f,
            (float)(coord8 - vec.z),
            localX,
            tickDelta
         );
         coord4 = coord7;
         coord5 = coord8;
      }
   }

   public static void flush(Immediate immediate) {
      FlatOverlay.flush(immediate);
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

   private static void quad(
      VertexConsumer consumer,
      Entry entry,
      int n,
      float f,
      float f13,
      float f14,
      float f15,
      float f16,
      float f17,
      float f18,
      float f19,
      float f20,
      float f21,
      float f22,
      float f23
   ) {
      consumer.vertex(entry, f, f13, f14).color(n);
      consumer.vertex(entry, f15, f16, f17).color(n);
      consumer.vertex(entry, f18, f19, f20).color(n);
      consumer.vertex(entry, f21, f22, f23).color(n);
   }
}
