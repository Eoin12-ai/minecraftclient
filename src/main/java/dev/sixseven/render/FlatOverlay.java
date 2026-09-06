package dev.sixseven.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import dev.sixseven.util.Colors;
import net.minecraft.RenderLayerBridge;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public final class FlatOverlay {
   private static final RenderPipeline FILL_PIPELINE = RenderPipeline.builder(new Snippet[]{RenderLayerBridge.getPositionColor()})
      .withLocation("sixsevenclient/pipeline/flat_fill")
      .withCull(false)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .build();
   public static final RenderLayer FILL = RenderLayerBridge.create(
      "sixsevenclient:flat_fill", RenderSetup.builder(FILL_PIPELINE).translucent().build()
   );
   private static final RenderPipeline LINE_PIPELINE = RenderPipeline.builder(new Snippet[]{RenderLayerBridge.getPositionColorLines()})
      .withLocation("sixsevenclient/pipeline/flat_lines")
      .withDepthWrite(false)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .build();
   public static final RenderLayer LINES = RenderLayerBridge.create("sixsevenclient:flat_lines", RenderSetup.builder(LINE_PIPELINE).build());

   private FlatOverlay() {
   }

   public static void fillQuad(Immediate immediate, MatrixStack matrices, Vec3d vec, double d, double coord, double currentScore, double coord3, double coord4, int n) {
      VertexConsumer consumer = immediate.getBuffer(FILL);
      Entry entry = matrices.peek();
      float f = (float)(d - vec.x);
      float f6 = (float)(coord - vec.z);
      float f7 = (float)(currentScore - vec.x);
      float f8 = (float)(coord3 - vec.z);
      float f9 = (float)(coord4 - vec.y);
      consumer.vertex(entry, f, f9, f6).color(n);
      consumer.vertex(entry, f, f9, f8).color(n);
      consumer.vertex(entry, f7, f9, f8).color(n);
      consumer.vertex(entry, f7, f9, f6).color(n);
   }

   public static void edge(
      Immediate immediate, MatrixStack matrices, Vec3d vec, double d, double coord, double currentScore, double coord3, double coord4, int n, float tickDelta
   ) {
      VertexConsumer consumer = immediate.getBuffer(LINES);
      Entry entry = matrices.peek();
      Vector3f vector3f = new Vector3f((float)(currentScore - d), 0.0F, (float)(coord3 - coord)).normalize();
      consumer.vertex(entry, (float)(d - vec.x), (float)(coord4 - vec.y), (float)(coord - vec.z))
         .color(n)
         .normal(entry, vector3f)
         .lineWidth(tickDelta);
      consumer.vertex(entry, (float)(currentScore - vec.x), (float)(coord4 - vec.y), (float)(coord3 - vec.z))
         .color(n)
         .normal(entry, vector3f)
         .lineWidth(tickDelta);
   }

   public static void marker(Immediate immediate, MatrixStack matrices, Vec3d vec, double d, double coord, double currentScore, double coord3, int n) {
      fillQuadRot(immediate, matrices, vec, d, coord, currentScore, coord3, n);
      int offset = Colors.withAlpha(n, 1.0F);
      edge(immediate, matrices, vec, d - coord3, coord, d, coord - coord3, currentScore, offset, 2.0F);
      edge(immediate, matrices, vec, d, coord - coord3, d + coord3, coord, currentScore, offset, 2.0F);
      edge(immediate, matrices, vec, d + coord3, coord, d, coord + coord3, currentScore, offset, 2.0F);
      edge(immediate, matrices, vec, d, coord + coord3, d - coord3, coord, currentScore, offset, 2.0F);
   }

   private static void fillQuadRot(Immediate immediate, MatrixStack matrices, Vec3d vec, double d, double coord, double currentScore, double coord3, int n) {
      VertexConsumer consumer = immediate.getBuffer(FILL);
      Entry entry = matrices.peek();
      float f = (float)(currentScore - vec.y);
      consumer.vertex(entry, (float)(d - coord3 - vec.x), f, (float)(coord - vec.z)).color(n);
      consumer.vertex(entry, (float)(d - vec.x), f, (float)(coord - coord3 - vec.z)).color(n);
      consumer.vertex(entry, (float)(d + coord3 - vec.x), f, (float)(coord - vec.z)).color(n);
      consumer.vertex(entry, (float)(d - vec.x), f, (float)(coord + coord3 - vec.z)).color(n);
   }

   public static void flush(Immediate immediate) {
      immediate.draw(FILL);
      immediate.draw(LINES);
   }
}
