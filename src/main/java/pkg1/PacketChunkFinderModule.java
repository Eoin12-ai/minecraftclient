package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import util.Utils_2;

public final class PacketChunkFinderModule extends Module {
   private static final int intVal = -58;
   private static final int intVal2 = 30;
   private static final double doubleVal = 0.15;
   private static final int intVal3 = 24;
   private static final long longVal = 5000000L;
   private static final int intVal4 = 3;
   private static final int intVal5 = 40;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Packets");
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Render");
   private final Setting<Integer> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("sensitivity")
            .valOf2("Higher values require a larger connected amethyst cluster.")
            .valOf3(3)
            .valOf4(1, 16)
            .valOf5(1, 16)
            .valOf8(this::run20)
            .getVal()
      );
   private final Setting<Integer> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("required-packets")
            .valOf2("Relevant packets required for an amethyst chunk to be marked.")
            .valOf3(1)
            .valOf4(1, 20)
            .valOf5(1, 10)
            .valOf8(this::run17)
            .getVal()
      );
   private final Setting<Integer> val6 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("required-nearby-chunks")
            .valOf2("Packet-confirmed amethyst chunks required in one rendered area.")
            .valOf3(1)
            .valOf4(1, 8)
            .valOf5(1, 5)
            .valOf8(this::run14)
            .getVal()
      );
   private final Setting<Integer> val7 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-radius")
            .valOf2("Maximum chunk distance from the player for packet detections.")
            .valOf3(24)
            .valOf4(1, 32)
            .valOf5(1, 32)
            .valOf8(this::run12)
            .getVal()
      );
   private final Setting<Integer> val8 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("nearby-range")
            .valOf2("Maximum chunk distance used to join detections into one area.")
            .valOf3(8)
            .valOf4(1, 24)
            .valOf5(1, 16)
            .valOf8(this::run11)
            .getVal()
      );
   private final Setting<Boolean> val9 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("notifications").valOf2("Shows a toast for new packet-confirmed amethyst chunks.").valOf3(true).getVal()
      );
   private final Setting<Boolean> val10 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("chunk-data").valOf2("Uses full chunk data packets as confirmation.").valOf3(true).getVal());
   private final Setting<Boolean> val11 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("chunk-delta-update")
            .valOf2("Uses multi-block chunk update packets as confirmation.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Boolean> val12 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("block-entity-update").valOf2("Uses block-entity update packets as confirmation.").valOf3(true).getVal()
      );
   private final Setting<Boolean> val13 = this.val3_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("tracers").valOf2("Draws a tracer to each marked area.").valOf3(true).getVal());
   private final Setting<Integer> val14 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("height-offset")
            .valOf2("Vertical offset from the amethyst height so this marker stays separate from Tuff Chunk Finder.")
            .valOf3(4)
            .valOf4(-32, 32)
            .valOf5(-12, 12)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val15 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("side-color")
            .valOf2("Fill color of packet-confirmed chunks.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 45, 45, 45))
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val16 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("line-color")
            .valOf2("Outline and tracer color of packet-confirmed chunks.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 45, 45, 230))
            .getVal()
      );
   private final Setting<RenderMode> val17 = this.val3_2
      .addSetting(
         new BaseEspModuleHelper2<RenderMode>().valOf("shape-mode").valOf2("How packet-confirmed chunks are rendered.").valOf3(RenderMode.Both).getVal()
      );
   private final Map<ChunkPos, PacketChunkFinderModule.Inner3> map = new HashMap<>();
   private final Map<ChunkPos, PacketChunkFinderModule.Inner1> map2 = new HashMap<>();
   private final Map<ChunkPos, Integer> map3 = new HashMap<>();
   private final ConcurrentLinkedQueue<PacketChunkFinderModule.Inner5> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
   private final ArrayDeque<ChunkPos> arrayDeque = new ArrayDeque<>();
   private final Set<Long> set = new HashSet<>();
   private final Set<String> set2 = new HashSet<>();
   private List<PacketChunkFinderModule.Inner2> list = List.of();
   private ClientWorld class638;
   private int intVal6;

   public PacketChunkFinderModule() {
      super(SwyzzyAddon.val2, "packet-chunk-finder", "Packet Chunk Finder");
      Utils_2.run(this, "Packet Chunk Finder");
   }

   @Override
   public void run6() {
      this.run9();
      this.class638 = class310.world;
   }

   @Override
   public void run7() {
      this.run9();
   }

   @Override
   public String getString2() {
      return this.list.isEmpty() ? null : String.valueOf(this.list.size());
   }

   @InternalHelper5
   private void run(PacketChunkFinderModuleData var1) {
      if (class310.player != null && class310.world != null) {
         if (this.val10.getObject() && var1.object instanceof ChunkDataS2CPacket var2) {
            this.concurrentLinkedQueue
               .add(
                  new Inner5(
                     new ChunkPos(var2.getChunkX(), var2.getChunkZ()), PacketChunkFinderModule.State4.CHUNK_DATA
                  )
               );
         } else if (this.val11.getObject() && var1.object instanceof ChunkDeltaUpdateS2CPacket var3) {
            HashSet var10 = new HashSet();
            var3.visitUpdates((var913, var914) -> PacketChunkFinderModule.run10(var10, var913, var914));

            for (ChunkPos var7 : (Iterable<ChunkPos>)(Object)(var10)) {
               this.concurrentLinkedQueue.add(new Inner5(var7, PacketChunkFinderModule.State4.CHUNK_DELTA));
            }
         } else if (this.val12.getObject() && var1.object instanceof BlockEntityUpdateS2CPacket var4) {
            this.concurrentLinkedQueue.add(new Inner5(new ChunkPos(var4.getPos()), PacketChunkFinderModule.State4.BLOCK_ENTITY));
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null) {
         if (class310.world != this.class638) {
            this.run9();
            this.class638 = class310.world;
         }

         this.intVal6++;

         PacketChunkFinderModule.Inner5 var2;
         while ((var2 = this.concurrentLinkedQueue.poll()) != null) {
            this.run4(var2.pos(), var2.kind());
         }

         if (this.intVal6 % 40 == 0) {
            for (ChunkPos var4 : this.map.keySet()) {
               this.run8(var4);
            }
         }

         this.run19();
         boolean var13 = false;
         ArrayList var14 = new ArrayList();
         long var5 = System.nanoTime() + 5000000L;

         for (int var7 = 0; var7 < 24 && !this.arrayDeque.isEmpty() && (var7 <= 0 || System.nanoTime() < var5); var7++) {
            ChunkPos var8 = this.arrayDeque.removeFirst();
            this.set.remove(var8.toLong());
            if (this.intVal6 < this.map3.getOrDefault(var8, 0)) {
               var14.add(var8);
            } else {
               PacketChunkFinderModule.Inner3 var9 = this.map.get(var8);
               if (var9 != null && var9.getInt2() >= this.val5.getObject()) {
                  WorldChunk var10 = class310.world.getChunkManager().getWorldChunk(var8.x, var8.z);
                  if (var10 == null) {
                     var14.add(var8);
                  } else {
                     PacketChunkFinderModule.Inner1 var11 = this.valOf(var10, var9.getInt2());
                     if (var11 == null) {
                        var13 |= this.map2.remove(var8) != null;
                     } else {
                        PacketChunkFinderModule.Inner1 var12 = this.map2.put(var8, var11);
                        var13 |= !var11.equals(var12);
                     }
                  }
               } else {
                  var13 |= this.map2.remove(var8) != null;
               }
            }
         }

         for (ChunkPos var16 : (Iterable<ChunkPos>)(Object)(var14)) {
            this.run8(var16);
         }

         if (var13) {
            this.run13();
         }
      }
   }

   @InternalHelper5
   private void run3(ActivityChunkFinderModuleData var1) {
      ActivityChunkFinderModuleEntry2 var2 = this.val15.getObject();
      ActivityChunkFinderModuleEntry2 var3 = this.val16.getObject();

      for (PacketChunkFinderModule.Inner2 var5 : this.list) {
         double var6 = var5.markerY() + this.val14.getObject().intValue();
         var1.val
            .run(
               var5.minChunkX() * 16.0,
               var6,
               var5.minChunkZ() * 16.0,
               var5.maxChunkX() * 16.0 + 16.0,
               var6 + 0.15,
               var5.maxChunkZ() * 16.0 + 16.0,
               var2,
               var3,
               this.val17.getObject(),
               0
            );
         if (this.val13.getObject()) {
            var1.val
               .run6(
                  BlockEspPlusModuleData.class243.x,
                  BlockEspPlusModuleData.class243.y,
                  BlockEspPlusModuleData.class243.z,
                  (var5.minChunkX() + var5.maxChunkX() + 1) * 8.0,
                  var6,
                  (var5.minChunkZ() + var5.maxChunkZ() + 1) * 8.0,
                  var3
               );
         }
      }
   }

   private void run4(ChunkPos var1, PacketChunkFinderModule.State4 var2) {
      if (this.check2(var1)) {
         PacketChunkFinderModule.Inner3 var3 = this.map.getOrDefault(var1, PacketChunkFinderModule.Inner3.val);
         this.map.put(var1, var3.valOf(var2));
         this.map3.put(var1, this.intVal6 + 3);
         this.run8(var1);
      }
   }

   private PacketChunkFinderModule.Inner1 valOf(WorldChunk var1, int var2) {
      HashSet var3 = new HashSet<>(setOf(var1));
      int var4 = this.getInt();
      int var5 = 0;
      long var6 = 0L;
      int var8 = 0;

      while (!var3.isEmpty()) {
         BlockPos var9 = (BlockPos)var3.iterator().next();
         var3.remove(var9);
         ArrayDeque var10 = new ArrayDeque();
         var10.add(var9);
         int var11 = 0;
         long var12 = 0L;

         while (!var10.isEmpty()) {
            BlockPos var14 = (BlockPos)var10.removeFirst();
            var11++;
            var12 += var14.getY();

            for (int var15 = -1; var15 <= 1; var15++) {
               for (int var16 = -1; var16 <= 1; var16++) {
                  for (int var17 = -1; var17 <= 1; var17++) {
                     if (var15 != 0 || var16 != 0 || var17 != 0) {
                        BlockPos var18 = var14.add(var15, var16, var17);
                        if (var3.remove(var18)) {
                           var10.addLast(var18);
                        }
                     }
                  }
               }
            }
         }

         if (var11 >= var4) {
            var5++;
            var6 += var12;
            var8 += var11;
         }
      }

      return var5 == 0 ? null : new Inner1(var5, var2, (double)var6 / var8);
   }

   private static Set<BlockPos> setOf(WorldChunk var0) {
      HashSet var1 = new HashSet();
      ChunkPos var2 = var0.getPos();
      int var3 = Math.max(-58, var0.getBottomY());
      int var4 = Math.min(30, var0.getBottomY() + var0.getHeight() - 1);
      int var5 = Math.floorDiv(var3, 16);
      int var6 = Math.floorDiv(var4, 16);

      for (int var7 = var5; var7 <= var6; var7++) {
         ChunkSection var8 = var0.getSection(var0.sectionCoordToIndex(var7));
         if (!var8.isEmpty() && var8.hasAny(PacketChunkFinderModule::check)) {
            int var9 = var7 == var5 ? Math.floorMod(var3, 16) : 0;
            int var10 = var7 == var6 ? Math.floorMod(var4, 16) : 15;

            for (int var11 = var9; var11 <= var10; var11++) {
               int var12 = var7 * 16 + var11;

               for (int var13 = 0; var13 < 16; var13++) {
                  for (int var14 = 0; var14 < 16; var14++) {
                     if (check(var8.getBlockState(var13, var11, var14))) {
                        var1.add(new BlockPos(var2.getStartX() + var13, var12, var2.getStartZ() + var14));
                     }
                  }
               }
            }
         }
      }

      return var1;
   }

   private static boolean check(BlockState var0) {
      return var0.isOf(Blocks.AMETHYST_CLUSTER)
         || var0.isOf(Blocks.LARGE_AMETHYST_BUD)
         || var0.isOf(Blocks.MEDIUM_AMETHYST_BUD)
         || var0.isOf(Blocks.SMALL_AMETHYST_BUD)
         || var0.isOf(Blocks.AMETHYST_BLOCK)
         || var0.isOf(Blocks.BUDDING_AMETHYST);
   }

   private int getInt() {
      return 1 + (this.val4.getObject() - 1) * 2;
   }

   private void run13() {
      HashMap var1 = new HashMap<>(this.map2);
      ArrayList var2 = new ArrayList();
      int var3 = this.val8.getObject();

      while (!var1.isEmpty()) {
         ChunkPos var4 = (ChunkPos)var1.keySet().iterator().next();
         PacketChunkFinderModule.Inner1 var5 = (PacketChunkFinderModule.Inner1)var1.remove(var4);
         ArrayDeque var6 = new ArrayDeque();
         var6.add(var4);
         int var7 = var4.x;
         int var8 = var4.x;
         int var9 = var4.z;
         int var10 = var4.z;
         double var11 = var5.centerY();
         int var13 = var5.packets();
         int var14 = 1;

         while (!var6.isEmpty()) {
            ChunkPos var15 = (ChunkPos)var6.removeFirst();
            ArrayList var16 = new ArrayList();

            for (ChunkPos var18 : (Iterable<ChunkPos>)(Object)(var1.keySet())) {
               if (Math.abs(var18.x - var15.x) <= var3 && Math.abs(var18.z - var15.z) <= var3) {
                  var16.add(var18);
               }
            }

            for (ChunkPos var21 : (Iterable<ChunkPos>)(Object)(var16)) {
               PacketChunkFinderModule.Inner1 var19 = (PacketChunkFinderModule.Inner1)var1.remove(var21);
               var6.addLast(var21);
               var7 = Math.min(var7, var21.x);
               var8 = Math.max(var8, var21.x);
               var9 = Math.min(var9, var21.z);
               var10 = Math.max(var10, var21.z);
               var11 += var19.centerY();
               var13 += var19.packets();
               var14++;
            }
         }

         if (var14 >= this.val6.getObject()) {
            var2.add(new Inner2(var7, var8, var9, var10, var11 / var14, var14, var13));
         }
      }

      this.list = List.copyOf(var2);
      this.run15();
   }

   private void run15() {
      HashSet var1 = new HashSet();

      for (PacketChunkFinderModule.Inner2 var3 : this.list) {
         String var4 = var3.minChunkX() + ":" + var3.maxChunkX() + ":" + var3.minChunkZ() + ":" + var3.maxChunkZ();
         var1.add(var4);
         if (this.val9.getObject() && this.set2.add(var4)) {
            this.run5(var3);
         }
      }

      this.set2.retainAll(var1);
   }

   private void run5(PacketChunkFinderModule.Inner2 var1) {
      int var2 = (var1.minChunkX() + var1.maxChunkX() + 1) * 8;
      int var3 = (var1.minChunkZ() + var1.maxChunkZ() + 1) * 8;
      ChunkFinderV2ModuleUtil.run(
         class310, Items.AMETHYST_SHARD, this.title, var1.totalPackets() + " packets in " + var1.chunkCount() + " amethyst chunks at " + var2 + ", " + var3
      );
   }

   private void run8(ChunkPos var1) {
      if (this.set.add(var1.toLong())) {
         this.arrayDeque.addLast(var1);
      }
   }

   private void run16() {
      this.map2.clear();

      for (ChunkPos var2 : this.map.keySet()) {
         this.run8(var2);
      }

      this.run13();
   }

   private void run18() {
      this.run19();
      this.run13();
   }

   private void run19() {
      boolean var1 = this.map.keySet().removeIf(this::check5);
      var1 |= this.map2.keySet().removeIf(this::check4);
      this.map3.keySet().removeIf(this::check3);
      if (var1) {
         this.run13();
      }
   }

   private boolean check2(ChunkPos var1) {
      if (class310.player == null) {
         return false;
      } else {
         ChunkPos var2 = class310.player.getChunkPos();
         return Math.abs(var1.x - var2.x) <= this.val7.getObject() && Math.abs(var1.z - var2.z) <= this.val7.getObject();
      }
   }

   private void run9() {
      this.map.clear();
      this.map2.clear();
      this.map3.clear();
      this.concurrentLinkedQueue.clear();
      this.arrayDeque.clear();
      this.set.clear();
      this.set2.clear();
      this.list = List.of();
      this.intVal6 = 0;
   }

   private boolean check3(ChunkPos var1) {
      return !this.check2(var1);
   }

   private boolean check4(ChunkPos var1) {
      return !this.check2(var1);
   }

   private boolean check5(ChunkPos var1) {
      return !this.check2(var1);
   }

   private static void run10(Set var0, BlockPos var1, BlockState var2) {
      var0.add(new ChunkPos(var1));
   }

   private void run11(Integer var1) {
      this.run13();
   }

   private void run12(Integer var1) {
      this.run18();
   }

   private void run14(Integer var1) {
      this.run13();
   }

   private void run17(Integer var1) {
      this.run16();
   }

   private void run20(Integer var1) {
      this.run16();
   }

   final class Inner1 {
      private int clusters;
      private int packets;
      private double centerY;

      Inner1(int var1, int var2, double var3) {
         this.clusters = var1;
         this.packets = var2;
         this.centerY = var3;
      }

      public int clusters() {
         return this.clusters;
      }

      public int packets() {
         return this.packets;
      }

      public double centerY() {
         return this.centerY;
      }
   }

   final class Inner2 {
      private int minChunkX;
      private int maxChunkX;
      private int minChunkZ;
      private int maxChunkZ;
      private double markerY;
      private int chunkCount;
      private int totalPackets;

      Inner2(int var1, int var2, int var3, int var4, double var5, int var7, int var8) {
         this.minChunkX = var1;
         this.maxChunkX = var2;
         this.minChunkZ = var3;
         this.maxChunkZ = var4;
         this.markerY = var5;
         this.chunkCount = var7;
         this.totalPackets = var8;
      }

      public int minChunkX() {
         return this.minChunkX;
      }

      public int maxChunkX() {
         return this.maxChunkX;
      }

      public int minChunkZ() {
         return this.minChunkZ;
      }

      public int maxChunkZ() {
         return this.maxChunkZ;
      }

      public double markerY() {
         return this.markerY;
      }

      public int chunkCount() {
         return this.chunkCount;
      }

      public int totalPackets() {
         return this.totalPackets;
      }
   }

   final static class Inner3 {
      private int chunkData;
      private int chunkDelta;
      private int blockEntity;
      static final PacketChunkFinderModule.Inner3 val = new Inner3(0, 0, 0);

      private Inner3(int var1, int var2, int var3) {
         this.chunkData = var1;
         this.chunkDelta = var2;
         this.blockEntity = var3;
      }

      PacketChunkFinderModule.Inner3 valOf(PacketChunkFinderModule.State4 var1) {
         return switch (var1) {
            case CHUNK_DATA -> new Inner3(this.chunkData + 1, this.chunkDelta, this.blockEntity);
            case CHUNK_DELTA -> new Inner3(this.chunkData, this.chunkDelta + 1, this.blockEntity);
            case BLOCK_ENTITY -> new Inner3(this.chunkData, this.chunkDelta, this.blockEntity + 1);
         };
      }

      int getInt2() {
         return this.chunkData + this.chunkDelta + this.blockEntity;
      }

      public int chunkData() {
         return this.chunkData;
      }

      public int chunkDelta() {
         return this.chunkDelta;
      }

      public int blockEntity() {
         return this.blockEntity;
      }
   }

   final class Inner5 {
      private ChunkPos pos;
      private PacketChunkFinderModule.State4 kind;

      Inner5(ChunkPos var1, PacketChunkFinderModule.State4 var2) {
         this.pos = var1;
         this.kind = var2;
      }

      public ChunkPos pos() {
         return this.pos;
      }

      public PacketChunkFinderModule.State4 kind() {
         return this.kind;
      }
   }

   enum State4 {
      CHUNK_DATA,
      CHUNK_DELTA,
      BLOCK_ENTITY;

      private static PacketChunkFinderModule.State4[] getValArray() {
         return new PacketChunkFinderModule.State4[]{CHUNK_DATA, CHUNK_DELTA, BLOCK_ENTITY};
      }
   }
}
