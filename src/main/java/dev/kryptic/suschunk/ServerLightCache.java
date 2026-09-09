package dev.kryptic.suschunk;

import dev.kryptic.KrypticClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.LongConsumer;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

public final class ServerLightCache {
   public static final int SCAN_Y_MIN = -12;
   public static final int SCAN_Y_MAX = 80;
   private static final boolean DEBUG_LOG = Boolean.getBoolean("kryptic.sus.debug");
   private static final int TARGET_MIN_SECTION = ChunkSectionPos.getSectionCoord(-12);
   private static final int TARGET_MAX_SECTION = ChunkSectionPos.getSectionCoord(80);
   private static final ServerLightCache INSTANCE = new ServerLightCache();
   private final ConcurrentHashMap<Long, byte[]> sections = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Long, short[]> light5Cells = new ConcurrentHashMap<>();
   private final Set<Long> dirtyChunks = ConcurrentHashMap.newKeySet();
   private final List<LongConsumer> dirtyListeners = new CopyOnWriteArrayList<>();

   private ServerLightCache() {
   }

   public static ServerLightCache get() {
      return INSTANCE;
   }

   public void clear() {
      this.sections.clear();
      this.light5Cells.clear();
      this.dirtyChunks.clear();
   }

   public boolean consumeDirty(int n, int offset) {
      return this.dirtyChunks.remove(ChunkPos.toLong(n, offset));
   }

   public void addDirtyListener(LongConsumer longConsumer) {
      this.dirtyListeners.add(longConsumer);
   }

   public void markDirty(int n, int offset) {
      long chunkKey = ChunkPos.toLong(n, offset);
      this.dirtyChunks.add(chunkKey);

      for (LongConsumer longConsumer : this.dirtyListeners) {
         longConsumer.accept(chunkKey);
      }
   }

   public void ingest(int n, int n10, LightData lightData, World world) {
      if (world != null) {
         int n11 = world.getBottomSectionCoord() - 1;
         BitSet bitSet = lightData.getInitedBlock();
         BitSet bitSet2 = lightData.getUninitedBlock();
         List list = lightData.getBlockNibbles();
         int n12 = 0;
         boolean found = false;

         for (int n13 = bitSet.nextSetBit(0); n13 >= 0; n13 = bitSet.nextSetBit(n13 + 1)) {
            byte[] b = n12 < list.size() ? (byte[])list.get(n12) : null;
            n12++;
            int n14 = n11 + n13;
            if (b != null && b.length == 2048 && n14 >= TARGET_MIN_SECTION && n14 <= TARGET_MAX_SECTION) {
               long chunkKey = ChunkSectionPos.asLong(n, n14, n10);
               this.sections.put(chunkKey, (byte[])b.clone());
               int n15 = this.cacheLight5(chunkKey, b);
               if (DEBUG_LOG && n15 > 0) {
                  KrypticClient.LOGGER
                     .info("ServerLightCache: section {},{},{} cached {} light-5 positions",
                           new Object[]{n, n14, n10, n15});
               }

               found = true;
            }
         }

         for (int n16 = bitSet2.nextSetBit(0); n16 >= 0; n16 = bitSet2.nextSetBit(n16 + 1)) {
            int n17 = n11 + n16;
            if (n17 >= TARGET_MIN_SECTION && n17 <= TARGET_MAX_SECTION) {
               long chunkKey2 = ChunkSectionPos.asLong(n, n17, n10);
               this.light5Cells.remove(chunkKey2);
               if (this.sections.remove(chunkKey2) != null) {
                  found = true;
               }
            }
         }

         if (found) {
            this.markDirty(n, n10);
         }
      }
   }

   public int serverBlockLight(int n, int localY, int step) {
      if (localY >= -12 && localY <= 80) {
         byte[] b = this.sections
            .get(ChunkSectionPos.asLong(ChunkSectionPos.getSectionCoord(n), ChunkSectionPos.getSectionCoord(localY), ChunkSectionPos.getSectionCoord(step)));
         if (b == null) {
            return -1;
         } else {
            int step2 = (localY & 15) << 8 | (step & 15) << 4 | n & 15;
            int n9 = b[step2 >> 1] & 255;
            return (step2 & 1) == 0 ? n9 & 15 : n9 >> 4 & 15;
         }
      } else {
         return -1;
      }
   }

   public boolean isServerLight5(int n, int localX, int localZ) {
      return this.serverBlockLight(n, localX, localZ) == 5;
   }

   private int cacheLight5(long l, byte[] b) {
      short[] s = null;
      int temp = 0;

      for (int localZ = 0; localZ < 4096; localZ++) {
         int localY = b[localZ >> 1] & 255;
         int step = (localZ & 1) == 0 ? localY & 15 : localY >> 4 & 15;
         if (step == 5) {
            if (s == null) {
               s = new short[16];
            } else if (temp == s.length) {
               s = Arrays.copyOf(s, s.length * 2);
            }

            s[temp++] = (short)localZ;
         }
      }

      if (temp == 0) {
         this.light5Cells.remove(l);
      } else {
         this.light5Cells.put(l, Arrays.copyOf(s, temp));
      }

      return temp;
   }

   public List<BlockPos> light5Positions(int n, int n9) {
      ArrayList list = null;
      int n10 = n << 4;
      int n11 = n9 << 4;

      for (int n12 = TARGET_MIN_SECTION; n12 <= TARGET_MAX_SECTION; n12++) {
         short[] s = this.light5Cells.get(ChunkSectionPos.asLong(n, n12, n9));
         if (s != null) {
            int n13 = n12 << 4;

            for (short s2 : s) {
               int n14 = s2 & '\uffff';
               int n15 = n13 + (n14 >> 8);
               if (n15 >= -12 && n15 <= 80) {
                  if (list == null) {
                     list = new ArrayList();
                  }

                  list.add(new BlockPos(n10 + (n14 & 15), n15, n11 + (n14 >> 4 & 15)));
               }
            }
         }
      }

      if (list == null) return java.util.Collections.emptyList(); java.util.List r2 = list; return (java.util.List<net.minecraft.util.math.BlockPos>)r2;
   }

   public void injectForTest(int n, int n10, int n11, int n12) {
      int n13 = ChunkSectionPos.getSectionCoord(n);
      int n14 = ChunkSectionPos.getSectionCoord(n11);
      long chunkKey = ChunkSectionPos.asLong(n13, ChunkSectionPos.getSectionCoord(n10), n14);
      byte[] b = this.sections.computeIfAbsent(chunkKey, arg -> new byte[2048]);
      int n15 = (n10 & 15) << 8 | (n11 & 15) << 4 | n & 15;
      int n16 = n15 >> 1;
      int n17 = (n15 & 1) * 4;
      b[n16] = (byte)(b[n16] & ~(15 << n17) | (n12 & 15) << n17);
      this.cacheLight5(chunkKey, b);
      this.markDirty(n13, n14);
   }

   public boolean hasChunk(int chunkX, int chunkZ) {
      for (int n = TARGET_MIN_SECTION; n <= TARGET_MAX_SECTION; n++) {
         if (this.sections.containsKey(ChunkSectionPos.asLong(chunkX, n, chunkZ))) {
            return true;
         }
      }

      return false;
   }
}
