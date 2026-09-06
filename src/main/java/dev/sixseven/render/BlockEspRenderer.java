package dev.sixseven.render;

import dev.sixseven.module.render.BlockEspModule;
import dev.sixseven.settings.BlockListSetting;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Vector3fc;

public final class BlockEspRenderer {
   private static final int MAX_CHUNK_RADIUS = 12;
   private static final int MAX_RESULTS = 8000;
   private static final double INSET = 0.002;
   private static final IncrementalScan<BlockEspRenderer.Hit> SCAN = new IncrementalScan<>(48, 80000, 20);
   private static Set<Block> lastWanted = Set.of();

   private BlockEspRenderer() {
   }

   public static void clear() {
      SCAN.clear();
      lastWanted = Set.of();
   }

   public static int cachedCount() {
      return SCAN.get().size();
   }

   public static void scan(BlockEspModule blockEspModule) {
      HashSet set = new HashSet();

      for (BlockListSetting.Target target : blockEspModule.targets.targets()) {
         if (target.enabled.get() && target.block() != null) {
            set.add(target.block());
         }
      }

      if (set.isEmpty()) {
         SCAN.clear();
         lastWanted = Set.of();
      } else {
         if (!set.equals(lastWanted)) {
            lastWanted = set;
            SCAN.markDirty();
         }

         int n = blockEspModule.rangeExtraChunks.getInt();
         int client = Math.min(12, (Integer)MinecraftClient.getInstance().options.getViewDistance().getValue() + n);
         SCAN.tick(client, (arg, arg2) -> scanChunk(arg, set, arg2));
      }
   }

   private static int scanChunk(WorldChunk chunk, Set<Block> set, List<BlockEspRenderer.Hit> list) {
      ChunkSection[] arr = chunk.getSectionArray();
      int n = chunk.getBottomSectionCoord();
      int n9 = chunk.getPos().getStartX();
      int n10 = chunk.getPos().getStartZ();
      short s = 0;
      if (list.size() >= 8000) {
         return 0;
      } else {
         for (int n11 = 0; n11 < arr.length; n11++) {
            ChunkSection chunkSection = arr[n11];
            if (!chunkSection.isEmpty() && chunkSection.hasAny(arg -> set.contains(arg.getBlock()))) {
               int n12 = n + n11 << 4;
               s += 4096;

               for (int n13 = 0; n13 < 16; n13++) {
                  for (int n14 = 0; n14 < 16; n14++) {
                     for (int n15 = 0; n15 < 16; n15++) {
                        Block block = chunkSection.getBlockState(n15, n13, n14).getBlock();
                        if (set.contains(block)) {
                           list.add(new BlockEspRenderer.Hit(n9 + n15, n12 + n13, n10 + n14, block));
                           if (list.size() >= 8000) {
                              return s;
                           }
                        }
                     }
                  }
               }
            }
         }

         return s;
      }
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d vec, BlockEspModule blockEspModule) {
      List list = SCAN.get();
      if (!list.isEmpty()) {
         HashMap map = new HashMap();

         for (BlockListSetting.Target target : blockEspModule.targets.targets()) {
            if (target.block() != null) {
               map.put(target.block(), target.color.get());
            }
         }

         int n = blockEspModule.lineColor.get();
         int step2 = Math.clamp((long)blockEspModule.highlightAlpha.getInt(), 0, 255);
         boolean ok = blockEspModule.shapeMode.is("Both") || blockEspModule.shapeMode.is("Lines");
         boolean ok2 = blockEspModule.shapeMode.is("Both") || blockEspModule.shapeMode.is("Sides");
         boolean ok3 = blockEspModule.tracers.get() && blockEspModule.tracer.get();

         for (BlockEspRenderer.Hit hit : list) {
            int n9 = map.getOrDefault(hit.block(), n) & 16777215;
            int n10 = n9 | step2 << 24;
            if (ok2) {
               EspBoxRenderer.fill(
                  immediate,
                  matrices,
                  vec,
                  (double)hit.x() + 0.002,
                  (double)hit.y() + 0.002,
                  (double)hit.z() + 0.002,
                  (double)(hit.x() + 1) - 0.002,
                  (double)(hit.y() + 1) - 0.002,
                  (double)(hit.z() + 1) - 0.002,
                  n10
               );
            }

            if (ok) {
               EspBoxRenderer.outline(
                  immediate,
                  matrices,
                  vec,
                  (double)hit.x() + 0.002,
                  (double)hit.y() + 0.002,
                  (double)hit.z() + 0.002,
                  (double)(hit.x() + 1) - 0.002,
                  (double)(hit.y() + 1) - 0.002,
                  (double)(hit.z() + 1) - 0.002,
                  n10,
                  1.6F
               );
            }
         }

         if (ok3) {
            int n11 = blockEspModule.tracerColor.get() >>> 24 & 0xFF;
            if (n11 == 0) {
               n11 = 200;
            }

            Vector3fc client = MinecraftClient.getInstance().gameRenderer.getCamera().getHorizontalPlane();

            for (BlockEspRenderer.Hit hit2 : list) {
               int n12 = map.getOrDefault(hit2.block(), n) & 16777215;
               int n13 = n12 | n11 << 24;
               EspBoxRenderer.tracer(immediate, matrices, vec, client, (double)hit2.x() + 0.5, (double)hit2.y() + 0.5, (double)hit2.z() + 0.5, n13, 1.2F);
            }
         }

         EspBoxRenderer.flush(immediate);
      }
   }

   private static record Hit(int x, int y, int z, Block block) {
   }
}
