package dev.sixseven.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.chunk.WorldChunk;

public final class IncrementalScan<H> {
   private final int chunksPerTick;
   private final int blockBudgetPerTick;
   private final int idleTicks;
   private volatile List<H> published = List.of();
   private List<H> building = new ArrayList<>();
   private int cursor;
   private int[] order = new int[0];
   private int orderRadius = -1;
   private int sweepPcx = Integer.MIN_VALUE;
   private int sweepPcz = Integer.MIN_VALUE;
   private int cooldown;
   private boolean dirty;

   public IncrementalScan(int n, int localX, int localZ) {
      this.chunksPerTick = n;
      this.blockBudgetPerTick = localX;
      this.idleTicks = localZ;
   }

   public List<H> get() {
      return this.published;
   }

   public void markDirty() {
      this.dirty = true;
   }

   public void clear() {
      this.published = List.of();
      this.building = new ArrayList<>();
      this.cursor = 0;
      this.cooldown = 0;
      this.dirty = false;
      this.sweepPcx = this.sweepPcz = Integer.MIN_VALUE;
   }

   public void tick(int n, IncrementalScan.ChunkScanner<H> chunkScanner) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientWorld world = client.world;
      ClientPlayerEntity player = client.player;
      if (world != null && player != null) {
         if (this.orderRadius != n) {
            this.ensureOrder(n);
            this.dirty = true;
         }

         if (this.cursor == 0) {
            if (!this.dirty && this.cooldown > 0) {
               this.cooldown--;
               return;
            }

            this.sweepPcx = player.getChunkPos().x;
            this.sweepPcz = player.getChunkPos().z;
            this.building = new ArrayList<>();
            this.dirty = false;
         } else if (this.dirty) {
            this.sweepPcx = player.getChunkPos().x;
            this.sweepPcz = player.getChunkPos().z;
            this.cursor = 0;
            this.building = new ArrayList<>();
            this.dirty = false;
         }

         int localY = this.order.length;
         int step = 0;

         for (int step2 = 0; this.cursor < localY && step < this.chunksPerTick && step2 < this.blockBudgetPerTick; step++) {
            int n9 = this.order[this.cursor];
            short s = (short)(n9 >> 16);
            short s2 = (short)(n9 & 65535);
            step2 += chunkScanner.scan(world.getChunk(this.sweepPcx + s, this.sweepPcz + s2), this.building);
            this.cursor++;
         }

         if (this.cursor >= localY) {
            this.published = this.building;
            this.building = new ArrayList<>();
            this.cursor = 0;
            this.cooldown = this.idleTicks;
         }
      }
   }

   private void ensureOrder(int n) {
      if (this.orderRadius != n) {
         int step2 = 2 * n + 1;
         Integer[] num = new Integer[step2 * step2];
         int n9 = 0;

         for (int n10 = -n; n10 <= n; n10++) {
            for (int n11 = -n; n11 <= n; n11++) {
               num[n9++] = (n10 & 65535) << 16 | n11 & 65535;
            }
         }

         Arrays.sort(num, (arg, arg2) -> {
            short s = (short)(arg >> 16);
            short s2 = (short)(arg & 65535);
            short s3 = (short)(arg2 >> 16);
            short s4 = (short)(arg2 & 65535);
            return Integer.compare(s * s + s2 * s2, s3 * s3 + s4 * s4);
         });
         int[] n12 = new int[num.length];

         for (int n13 = 0; n13 < num.length; n13++) {
            n12[n13] = num[n13];
         }

         this.order = n12;
         this.orderRadius = n;
      }
   }

   public interface ChunkScanner<H> {
      int scan(WorldChunk chunk, List<H> list);
   }
}
