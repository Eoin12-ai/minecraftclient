package dev.sixseven.module.render;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.notification.NotificationManager;
import dev.sixseven.settings.SliderSetting;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class ChunkFinderModule extends Module {
   private static final int MIN_CHUNK_AGE_TICKS = 200;
   private static final int CHUNKS_PER_TICK = 8;
   public final SliderSetting mergeRadius = this.addSetting(
      new SliderSetting("Merge Radius", "Hives within this many chunks of each other merge to one middle ok", 4.0, 1.0, 8.0, 1.0, "SO ")
   );
   private final Set<Long> beehiveChunks = ConcurrentHashMap.newKeySet();
   private final Set<Long> notifiedChunks = ConcurrentHashMap.newKeySet();
   private final Map<Long, Integer> firstLoadedTicks = new ConcurrentHashMap<>();
   private volatile Set<Long> displayChunks = Set.of();
   private int scanCursor;
   private int tickCounter;
   private boolean displayDirty;
   private int lastMergeRadius;

   public ChunkFinderModule() {
      super("Chunk Finder", "Highlights Donut SMP chunks that match a rare world signal worth checking.", Category.RENDER);
   }

   @Override
   protected void onEnable() {
      this.clear();
   }

   @Override
   protected void onDisable() {
      this.clear();
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world != null && client.player != null) {
         this.tickCounter++;
         int n = (Integer)client.options.getViewDistance().getValue();
         ChunkPos chunkPos = client.player.getChunkPos();
         int n10 = n * 2 + 1;
         int n11 = n10 * n10;
         if (n11 > 0) {
            for (int n12 = 0; n12 < 8; n12++) {
               int n13 = this.scanCursor % n11;
               this.scanCursor = (this.scanCursor + 1) % n11;
               int n14 = n13 % n10 - n;
               int n15 = n13 / n10 - n;
               int n16 = chunkPos.x + n14;
               int n17 = chunkPos.z + n15;
               WorldChunk chunk = client.world.getChunkManager().getWorldChunk(n16, n17, false);
               if (chunk != null && !chunk.isEmpty()) {
                  long chunkKey = ChunkPos.toLong(n16, n17);
                  this.firstLoadedTicks.putIfAbsent(chunkKey, this.tickCounter);
                  boolean ok = this.tickCounter - this.firstLoadedTicks.get(chunkKey) >= 200;
                  if (ok && hasTargetSignal(chunk)) {
                     if (this.beehiveChunks.add(chunkKey)) {
                        this.displayDirty = true;
                     }

                     if (this.notifiedChunks.add(chunkKey)) {
                        this.showToast(new ChunkPos(n16, n17));
                     }
                  } else if (this.beehiveChunks.remove(chunkKey)) {
                     this.displayDirty = true;
                  }
               }
            }

            if (this.beehiveChunks.removeIf(arg -> outOfRange(arg, chunkPos, n))) {
               this.displayDirty = true;
            }

            this.firstLoadedTicks.keySet().removeIf(arg -> outOfRange(arg, chunkPos, n));
            if (this.displayDirty || this.mergeRadius.getInt() != this.lastMergeRadius) {
               this.rebuildDisplay();
            }
         }
      }
   }

   private void rebuildDisplay() {
      this.displayDirty = false;
      this.lastMergeRadius = this.mergeRadius.getInt();
      HashSet set = new HashSet<>(this.beehiveChunks);
      if (set.size() < 2) {
         this.displayChunks = Set.copyOf(set);
      } else {
         int radius = Math.max(1, this.lastMergeRadius);
         HashSet<Long> set2 = new HashSet<>();
         HashSet<Long> set3 = new HashSet<>();

         for (long l : set) {
            if (set3.add(l)) {
               ArrayList list = new ArrayList();
               ArrayDeque arrayDeque = new ArrayDeque();
               arrayDeque.add(l);

               while (!arrayDeque.isEmpty()) {
                  long queued = (Long)arrayDeque.poll();
                  list.add(queued);
                  int n = ChunkPos.getPackedX(queued);
                  int offset = ChunkPos.getPackedZ(queued);

                  for (int radius2 = -radius; radius2 <= radius; radius2++) {
                     for (int radius3 = -radius; radius3 <= radius; radius3++) {
                        if (radius2 != 0 || radius3 != 0) {
                           long chunkKey = ChunkPos.toLong(n + radius2, offset + radius3);
                           if (set.contains(chunkKey) && set3.add(chunkKey)) {
                              arrayDeque.add(chunkKey);
                           }
                        }
                     }
                  }
               }

               set2.add(list.size() == 1 ? (Long)list.get(0) : middleChunk(list));
            }
         }

         this.displayChunks = Set.copyOf(set2);
      }
   }

   private static long middleChunk(List<Long> list) {
      long l = 0L;
      long l4 = 0L;

      for (long l3 : list) {
         l += (long)ChunkPos.getPackedX(l3);
         l4 += (long)ChunkPos.getPackedZ(l3);
      }

      int n = (int)Math.round((double)l / (double)list.size());
      int offset = (int)Math.round((double)l4 / (double)list.size());
      return ChunkPos.toLong(n, offset);
   }

   private static boolean hasTargetSignal(WorldChunk chunk) {
      for (ChunkSection chunkSection : chunk.getSectionArray()) {
         if (chunkSection != null && !chunkSection.isEmpty() && chunkSection.getBlockStateContainer().hasAny(ChunkFinderModule::isFullHoney)) {
            for (int n = 0; n < 16; n++) {
               for (int localX = 0; localX < 16; localX++) {
                  for (int localZ = 0; localZ < 16; localZ++) {
                     if (isFullHoney(chunkSection.getBlockState(n, localZ, localX))) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   private static boolean isFullHoney(BlockState state) {
      return (state.isOf(Blocks.BEEHIVE) || state.isOf(Blocks.BEE_NEST))
         && state.contains(Properties.HONEY_LEVEL)
         && (Integer)state.get(Properties.HONEY_LEVEL) == 5;
   }

   private static boolean outOfRange(long l, ChunkPos chunkPos, int n) {
      return Math.abs(ChunkPos.getPackedX(l) - chunkPos.x) > n || Math.abs(ChunkPos.getPackedZ(l) - chunkPos.z) > n;
   }

   private void showToast(ChunkPos chunkPos) {
      if (SixSevenClient.notifications() != null) {
         NotificationManager notificationManager = SixSevenClient.notifications();
         int n = chunkPos.getCenterX();
         notificationManager.pushInfo("Chunk Finder · X " + n + " Z " + chunkPos.getCenterZ());
      }
   }

   public Set<Long> flaggedChunks() {
      return this.displayChunks;
   }

   public void clear() {
      this.beehiveChunks.clear();
      this.notifiedChunks.clear();
      this.firstLoadedTicks.clear();
      this.displayChunks = Set.of();
      this.scanCursor = 0;
      this.tickCounter = 0;
      this.displayDirty = false;
      this.lastMergeRadius = this.mergeRadius.getInt();
   }
}
