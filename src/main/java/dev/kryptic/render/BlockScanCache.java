package dev.kryptic.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class BlockScanCache {
   private final Predicate<BlockState> predicate;
   private final int intervalTicks;
   private final int maxChunkRadius;
   private final int maxResults;
   private final double rangeSq;
   private volatile List<BlockPos> cache = List.of();
   private int tickCounter;

   public BlockScanCache(Predicate<BlockState> predicate2, int n, int localX, int localZ, double d) {
      this.predicate = predicate2;
      this.intervalTicks = n;
      this.maxChunkRadius = localX;
      this.maxResults = localZ;
      this.rangeSq = d * d;
   }

   public List<BlockPos> get() {
      return this.cache;
   }

   public void clear() {
      this.cache = List.of();
      this.tickCounter = 0;
   }

   public void scan() {
      if (this.tickCounter++ % this.intervalTicks == 0) {
         MinecraftClient client = MinecraftClient.getInstance();
         ClientWorld world = client.world;
         ClientPlayerEntity player = client.player;
         if (world != null && player != null) {
            int radius = Math.min(this.maxChunkRadius, (Integer)client.options.getViewDistance().getValue());
            int chunkPos = player.getChunkPos().x;
            int chunkPos2 = player.getChunkPos().z;
            ArrayList list = new ArrayList();

            for (int radius2 = chunkPos - radius; radius2 <= chunkPos + radius && list.size() < this.maxResults; radius2++) {
               for (int radius3 = chunkPos2 - radius; radius3 <= chunkPos2 + radius && list.size() < this.maxResults; radius3++) {
                  WorldChunk chunk = world.getChunk(radius2, radius3);
                  ChunkSection[] arr = chunk.getSectionArray();
                  int n = chunk.getBottomSectionCoord();
                  int n12 = chunk.getPos().getStartX();
                  int n13 = chunk.getPos().getStartZ();

                  for (int n14 = 0; n14 < arr.length; n14++) {
                     ChunkSection chunkSection = arr[n14];
                     if (!chunkSection.isEmpty() && chunkSection.hasAny(this.predicate)) {
                        int n15 = n + n14 << 4;

                        for (int n16 = 0; n16 < 16; n16++) {
                           for (int n17 = 0; n17 < 16; n17++) {
                              for (int n18 = 0; n18 < 16; n18++) {
                                 if (this.predicate.test(chunkSection.getBlockState(n18, n16, n17))) {
                                    int n19 = n12 + n18;
                                    int n20 = n15 + n16;
                                    int n21 = n13 + n17;
                                    if (!(player.squaredDistanceTo((double)n19 + 0.5, (double)n20 + 0.5, (double)n21 + 0.5) > this.rangeSq)) {
                                       list.add(new BlockPos(n19, n20, n21));
                                       if (list.size() >= this.maxResults) {
                                          break;
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            this.cache = list;
         }
      }
   }
}
