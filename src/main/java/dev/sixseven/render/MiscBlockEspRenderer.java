package dev.sixseven.render;

import dev.sixseven.module.render.SpawnerNametagsModule;
import dev.sixseven.util.Colors;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.net.minecraft.util.math.Vec3d;

public final class MiscBlockEspRenderer {
   private static final int SPAWNER_COLOR = -24576;
   private static final double SPAWNER_RANGE = 16.0;
   private static final float TRACER_WIDTH = 1.2F;

   private MiscBlockEspRenderer() {
   }

   public static void renderSpawners(Immediate immediate, MatrixStack matrices, Vec3d vec, SpawnerNametagsModule spawnerNametagsModule) {
      List<BlockPos> list = spawnerNametagsModule.scan.get();
      if (!list.isEmpty()) {
         boolean ok = spawnerNametagsModule.rangeRing.get();
         boolean found = spawnerNametagsModule.box.get();
         boolean found2 = spawnerNametagsModule.tracers.get();
         if (ok || found || found2) {
            net.minecraft.util.math.Vec3d client = found2 ? net.minecraft.client.MinecraftClient.getInstance().gameRenderer.getCamera().getPos() : null;

            for (BlockPos pos : list) {
               if (found) {
                  EspBoxRenderer.outline(
                     immediate,
                     matrices,
                     vec,
                     (double)pos.getX(),
                     (double)pos.getY(),
                     (double)pos.getZ(),
                     (double)(pos.getX() + 1),
                     (double)(pos.getY() + 1),
                     (double)(pos.getZ() + 1),
                     -24576,
                     2.0F
                  );
               }

               if (ok) {
                  EspBoxRenderer.ring(
                     immediate,
                     matrices,
                     vec,
                     (double)pos.getX() + 0.5,
                     (double)pos.getY() + 0.5,
                     (double)pos.getZ() + 0.5,
                     16.0,
                     48,
                     Colors.withAlpha(-24576, 0.6F),
                     2.0F
                  );
               }

               if (found2) {
                  EspBoxRenderer.tracer(
                     immediate,
                     matrices,
                     vec,
                     client,
                     (double)pos.getX() + 0.5,
                     (double)pos.getY() + 0.5,
                     (double)pos.getZ() + 0.5,
                     Colors.withAlpha(-24576, 0.7F),
                     1.2F
                  );
               }
            }

            EspBoxRenderer.flush(immediate);
         }
      }
   }
}
