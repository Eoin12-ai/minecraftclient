package dev.sixseven.render;

import dev.sixseven.util.Colors;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import dev.sixseven.module.visuals.BlockOutlineModule;
public final class BlockOutlineRenderer {
   private BlockOutlineRenderer() {
   }

   public static void render(Immediate immediate, MatrixStack matrices, OutlineRenderState outlineRenderState, Vec3d vec, BlockOutlineModule blockOutlineModule) {
      double d = (double)outlineRenderState.pos().getX() - vec.x;
      double coord = (double)outlineRenderState.pos().getY() - vec.y;
      double currentScore = (double)outlineRenderState.pos().getZ() - vec.z;
      double coord3 = (double)(System.nanoTime() % 1000000000000L) / 1.0E9;
      int n = animatedColor(blockOutlineModule, coord3);
      float f = pulseFactor(blockOutlineModule, coord3);
      float f5 = blockOutlineModule.fillOpacity.getFloat() / 100.0F;
      if (f5 > 0.004F) {
         VertexConsumer consumer = immediate.getBuffer(RenderLayers.debugQuads());
         int offset = Colors.withAlpha(n, f5 * (0.75F + 0.25F * f));

         for (Box box : outlineRenderState.shape().getBoundingBoxes()) {
            emitBox(matrices, consumer, box.expand(-0.002).offset(d, coord, currentScore), offset);
         }

         immediate.drawCurrentLayer();
      }

      float f6 = blockOutlineModule.thickness.getFloat();
      float f7 = blockOutlineModule.glow.getFloat() / 100.0F;
      if (f7 > 0.02F) {
         VertexConsumer consumer2 = immediate.getBuffer(RenderLayers.secondaryBlockOutline());
         VertexRendering.drawOutline(matrices, consumer2, outlineRenderState.shape(), d, coord, currentScore, Colors.withAlpha(n, (0.16F + 0.22F * f) * f7), f6 * 3.2F);
         immediate.drawCurrentLayer();
         VertexConsumer consumer3 = immediate.getBuffer(RenderLayers.lines());
         VertexRendering.drawOutline(matrices, consumer3, outlineRenderState.shape(), d, coord, currentScore, Colors.withAlpha(n, (0.3F + 0.25F * f) * f7), f6 * 2.0F);
         immediate.drawCurrentLayer();
      }

      VertexConsumer consumer4 = immediate.getBuffer(RenderLayers.lines());
      VertexRendering.drawOutline(matrices, consumer4, outlineRenderState.shape(), d, coord, currentScore, Colors.withAlpha(n, 0.85F + 0.15F * f), f6);
      immediate.drawCurrentLayer();
   }

   private static int animatedColor(BlockOutlineModule blockOutlineModule, double d) {
      if (blockOutlineModule.rainbow.get()) {
         return Colors.hsvToRgb((float)(d * 42.0 % 360.0), 0.75F, 1.0F);
      } else {
         int temp = blockOutlineModule.color.get() | 0xFF000000;
         if (blockOutlineModule.animation.is("Gradient Flow")) {
            float[] f = Colors.rgbToHsv(temp);
            float f3 = (f[0] + (float)(Math.sin(d * 1.6) * 28.0)) % 360.0F;
            if (f3 < 0.0F) {
               f3 += 360.0F;
            }

            return Colors.hsvToRgb(f3, Math.max(0.4F, f[1]), f[2]);
         } else {
            return temp;
         }
      }
   }

   private static float pulseFactor(BlockOutlineModule blockOutlineModule, double d) {
      return blockOutlineModule.animation.is("Pulse") ? (float)(0.5 + 0.5 * Math.sin(d * Math.PI * 2.0 / 1.6)) : 1.0F;
   }

   private static void emitBox(MatrixStack matrices, VertexConsumer consumer, Box box, int n) {
      Entry entry = matrices.peek();
      float f = (float)box.minX;
      float f7 = (float)box.minY;
      float f8 = (float)box.minZ;
      float f9 = (float)box.maxX;
      float f10 = (float)box.maxY;
      float f11 = (float)box.maxZ;
      quad(consumer, entry, n, f, f7, f8, f9, f7, f8, f9, f7, f11, f, f7, f11);
      quad(consumer, entry, n, f, f10, f8, f, f10, f11, f9, f10, f11, f9, f10, f8);
      quad(consumer, entry, n, f, f7, f8, f, f10, f8, f9, f10, f8, f9, f7, f8);
      quad(consumer, entry, n, f, f7, f11, f9, f7, f11, f9, f10, f11, f, f10, f11);
      quad(consumer, entry, n, f, f7, f8, f, f7, f11, f, f10, f11, f, f10, f8);
      quad(consumer, entry, n, f9, f7, f8, f9, f10, f8, f9, f10, f11, f9, f7, f11);
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
