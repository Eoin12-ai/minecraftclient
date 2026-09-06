package dev.sixseven.render;

import dev.sixseven.module.render.BlockEntityEspModule;
import java.util.Collection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3fc;

public final class BlockEntityEspRenderer {
   private static final double INSET = 0.002;
   private static final float TRACER_WIDTH = 1.2F;

   private BlockEntityEspRenderer() {
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d vec, BlockEntityEspModule blockEntityEspModule) {
      Collection collection = blockEntityEspModule.entries();
      if (!collection.isEmpty()) {
         MinecraftClient client = MinecraftClient.getInstance();
         ClientWorld world = client.world;
         if (world != null) {
            boolean ok = blockEntityEspModule.mode.is("Full");
            int n = Math.clamp((long)blockEntityEspModule.highlightAlpha.getInt(), 0, 255);
            boolean found = blockEntityEspModule.showGhosts.get();
            boolean found2 = blockEntityEspModule.tracers.get();
            int localY = blockEntityEspModule.ghostTint.get();
            Vector3fc camera = found2 ? client.gameRenderer.getCamera().getHorizontalPlane() : null;

            for (BlockEntityEspModule.Cached cached : collection) {
               String text = cached.typeKey();
               if (blockEntityEspModule.blockEntities.isEnabled(text)) {
                  BlockPos pos = cached.pos();
                  boolean found3 = world.getBlockEntity(pos) == null;
                  if (!found3 || found) {
                     int step = tint(blockEntityEspModule.blockEntities.color(text) & 16777215, found3, localY);
                     int step2 = step | n << 24;
                     double d = (double)pos.getX();
                     double coord = (double)pos.getY();
                     double currentScore = (double)pos.getZ();
                     double coord3 = d + 1.0;
                     double coord4 = coord + 1.0;
                     double coord5 = currentScore + 1.0;
                     if (ok) {
                        EspBoxRenderer.fill(immediate, matrices, vec, d - 0.002, coord - 0.002, currentScore - 0.002, coord3 + 0.002, coord4 + 0.002, coord5 + 0.002, step2);
                     } else {
                        EspBoxRenderer.outline(immediate, matrices, vec, d, coord, currentScore, coord3, coord4, coord5, step2, 1.6F);
                     }

                     if (found2) {
                        int n9 = step | Math.max(n, 160) << 24;
                        EspBoxRenderer.tracer(immediate, matrices, vec, camera, d + 0.5, coord + 0.5, currentScore + 0.5, n9, 1.2F);
                     }
                  }
               }
            }

            EspBoxRenderer.flush(immediate);
         }
      }
   }

   private static int tint(int temp, boolean value, int n9) {
      if (!value) {
         return temp;
      } else {
         int n10 = temp >> 16 & 0xFF;
         int n11 = temp >> 8 & 0xFF;
         int n12 = temp & 0xFF;
         int n13 = n9 >> 16 & 0xFF;
         int n14 = n9 >> 8 & 0xFF;
         int n15 = n9 & 0xFF;
         return (n10 + n13) / 2 << 16 | (n11 + n14) / 2 << 8 | (n12 + n15) / 2;
      }
   }
}
