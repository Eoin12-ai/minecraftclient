package dev.sixseven.render;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.render.ChunkFinderModule;
import dev.sixseven.util.Colors;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

public final class ChunkFinderRenderer {
   private static final float RENDER_Y = 55.0F;
   private static final int FILL_ALPHA = 190;

   private ChunkFinderRenderer() {
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d vec, ChunkFinderModule chunkFinderModule) {
      if (!chunkFinderModule.flaggedChunks().isEmpty()) {
         int n = SixSevenClient.themes().current().accent();
         int localX = Colors.withAlpha(Colors.darken(n, 0.58F), 190);
         int localZ = Colors.withAlpha(n, 255);

         for (long l : chunkFinderModule.flaggedChunks()) {
            double d = (double)ChunkPos.getPackedX(l) * 16.0;
            double coord = (double)ChunkPos.getPackedZ(l) * 16.0;
            double currentScore = d + 16.0;
            double coord3 = coord + 16.0;
            FlatOverlay.fillQuad(immediate, matrices, vec, d, coord, currentScore, coord3, 55.0, localX);
            FlatOverlay.edge(immediate, matrices, vec, d, coord, currentScore, coord, 55.0, localZ, 2.0F);
            FlatOverlay.edge(immediate, matrices, vec, currentScore, coord, currentScore, coord3, 55.0, localZ, 2.0F);
            FlatOverlay.edge(immediate, matrices, vec, currentScore, coord3, d, coord3, 55.0, localZ, 2.0F);
            FlatOverlay.edge(immediate, matrices, vec, d, coord3, d, coord, 55.0, localZ, 2.0F);
         }

         FlatOverlay.flush(immediate);
      }
   }
}
