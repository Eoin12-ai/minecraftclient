package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class ChunkFinderV2Module extends Module {
   private static final double doubleVal = 0.12;
   private static final double doubleVal2 = 0.1;
   private static final int intVal = 150;
   private static final int intVal2 = 30;
   private static final int intVal3 = 70;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Signals");
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Render");
   private final Setting<Integer> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("sensitivity")
            .valOf2("Higher values need more growth before a chunk counts as a hit.")
            .valOf3(1)
            .valOf4(1, 3)
            .valOf5(1, 3)
            .getVal()
      );
   private final Setting<Integer> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-interval")
            .valOf2("Seconds between full scans of the loaded chunks.")
            .valOf3(10)
            .valOf4(1, 60)
            .valOf5(1, 60)
            .getVal()
      );
   private final Setting<Integer> val6 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("mark-cooldown")
            .valOf2("Seconds a hit stays before a new one can replace it.")
            .valOf3(50)
            .valOf4(0, 300)
            .valOf5(0, 300)
            .getVal()
      );
   private final Setting<Integer> val7 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-radius")
            .valOf2("Chunk radius to scan, capped by your render distance.")
            .valOf3(32)
            .valOf4(4, 32)
            .valOf5(4, 32)
            .getVal()
      );
   private final Setting<Boolean> val8 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("toast").valOf2("Shows a toast when a chunk is found.").valOf3(true).getVal());
   private final Setting<Boolean> val9 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("bee-hives").valOf2("Flags chunks that contain occupied bee hives or nests.").valOf3(true).getVal()
      );
   private final Setting<Boolean> val10 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("vines").valOf2("Flags chunks with heavy vine growth.").valOf3(true).getVal());
   private final Setting<Boolean> val11 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("seagrass").valOf2("Flags chunks with dense seagrass below y70.").valOf3(true).getVal());
   private final Setting<Boolean> val12 = this.val3_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("tracer").valOf2("Draws a line from the camera to the found chunk.").valOf3(false).getVal());
   private final Setting<ActivityChunkFinderModuleHelper4> val13 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("area-fill")
            .valOf2("Fill color of the surrounding 3x3 area.")
            .valOf3(new ActivityChunkFinderModuleHelper4(30, 70, 200, 70))
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val14 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("area-line")
            .valOf2("Outline color of the surrounding 3x3 area.")
            .valOf3(new ActivityChunkFinderModuleHelper4(30, 70, 200, 255))
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val15 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("chunk-fill")
            .valOf2("Fill color of the found chunk.")
            .valOf3(new ActivityChunkFinderModuleHelper4(110, 170, 255, 130))
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val16 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("chunk-line")
            .valOf2("Outline color of the found chunk.")
            .valOf3(new ActivityChunkFinderModuleHelper4(110, 170, 255, 255))
            .getVal()
      );
   private final Setting<Integer> val17 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("y-level")
            .valOf2("Y coordinate the markers are drawn at.")
            .valOf3(63)
            .valOf4(-64, 320)
            .valOf5(-64, 320)
            .getVal()
      );
   private final Set<ChunkPos> set = ConcurrentHashMap.newKeySet();
   private final AtomicBoolean atomicBoolean = new AtomicBoolean();
   private ExecutorService executorService;
   private long longVal;
   private long longVal2;
   private long longVal3;

   public ChunkFinderV2Module() {
      super(SwyzzyAddon.val2, "chunk-finder-v2", "Finds chunks that have been loaded long enough for growth to pile up.");
   }

   @Override
   public void run6() {
      this.set.clear();
      this.longVal = 0L;
      this.longVal2 = 0L;
      this.longVal3 = 0L;
      this.atomicBoolean.set(false);
   }

   @Override
   public void run7() {
      this.set.clear();
      if (this.executorService != null) {
         this.executorService.shutdownNow();
      }

      this.atomicBoolean.set(false);
   }

   @Override
   public String getString2() {
      return this.set.isEmpty() ? null : String.valueOf(this.set.size());
   }

   private boolean check(ChunkPos var1) {
      if (class310.world != null && class310.player != null) {
         if (class310.world.getChunkManager().getWorldChunk(var1.x, var1.z) == null) {
            return false;
         } else {
            ChunkPos var2 = class310.player.getChunkPos();
            return Math.max(Math.abs(var1.x - var2.x), Math.abs(var1.z - var2.z)) <= (Integer)class310.options.getViewDistance().getValue();
         }
      } else {
         return false;
      }
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.world != null && class310.player != null) {
         long var2 = System.currentTimeMillis();
         if (!this.set.isEmpty()) {
            if (this.set.stream().anyMatch(this::check)) {
               this.longVal3 = 0L;
               return;
            }

            if (this.longVal3 == 0L) {
               this.longVal3 = var2;
            } else if (var2 - this.longVal3 >= 10000L) {
               this.set.clear();
               this.longVal3 = 0L;
            }
         }

         if (var2 - this.longVal >= this.val5.getObject().intValue() * 1000L && this.atomicBoolean.compareAndSet(false, true)) {
            this.longVal = var2;
            this.run12();
         }
      }
   }

   private void run12() {
      if (class310.world != null && class310.player != null) {
         ArrayList var1 = new ArrayList();
         ArrayList var2 = new ArrayList();
         this.run2(var1, var2);
         this.getExecutorService().submit(() -> this.run5(var1, var2));
      } else {
         this.atomicBoolean.set(false);
      }
   }

   private boolean check2(WorldChunk var1) {
      if (this.val9.getObject() && this.check3(var1)) {
         return true;
      } else {
         return this.val10.getObject() && this.check4(var1) ? true : this.val11.getObject() && this.check5(var1);
      }
   }

   private void run2(List<ChunkPos> var1, List<WorldChunk> var2) {
      ChunkPos var3 = class310.player.getChunkPos();
      int var4 = Math.min((Integer)class310.options.getViewDistance().getValue(), this.val7.getObject());
      ArrayList var5 = new ArrayList();

      for (int var6 = -var4; var6 <= var4; var6++) {
         for (int var7 = -var4; var7 <= var4; var7++) {
            var5.add(new ChunkPos(var3.x + var6, var3.z + var7));
         }
      }

      var5.sort(Comparator.comparingInt(var905 -> ChunkFinderV2Module.intOf(var3, (ChunkPos)var905)));

      for (ChunkPos var10 : (Iterable<ChunkPos>)(Object)(var5)) {
         WorldChunk var8 = class310.world.getChunkManager().getWorldChunk(var10.x, var10.z);
         if (var8 != null && !var8.isEmpty()) {
            var1.add(var10);
            var2.add(var8);
         }
      }
   }

   private boolean check3(WorldChunk var1) {
      int var2 = 0;

      for (BlockEntity var4 : var1.getBlockEntities().values()) {
         if (var4 instanceof BeehiveBlockEntity var5 && var5.getBeeCount() > 0) {
            if (++var2 >= this.val4.getObject()) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean check4(WorldChunk var1) {
      int var2 = 0;
      int var3 = 150 * this.val4.getObject();

      for (ChunkSection var7 : var1.getSectionArray()) {
         if (var7 != null && !var7.isEmpty() && var7.hasAny(ChunkFinderV2Module::check7)) {
            for (int var8 = 0; var8 < 16; var8++) {
               for (int var9 = 0; var9 < 16; var9++) {
                  for (int var10 = 0; var10 < 16; var10++) {
                     if (var7.getBlockState(var8, var9, var10).isOf(Blocks.VINE)) {
                        if (++var2 >= var3) {
                           return true;
                        }
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   private boolean check5(WorldChunk var1) {
      int var2 = 0;
      int var3 = 30 * this.val4.getObject();
      int var4 = var1.getBottomY();
      ChunkSection[] var5 = var1.getSectionArray();

      for (int var6 = 0; var6 < var5.length; var6++) {
         int var7 = var4 + var6 * 16;
         if (var7 > 70) {
            break;
         }

         ChunkSection var8 = var5[var6];
         if (var8 != null && !var8.isEmpty() && var8.hasAny(ChunkFinderV2Module::check6)) {
            for (int var9 = 0; var9 < 16; var9++) {
               for (int var10 = 0; var10 < 16; var10++) {
                  for (int var11 = 0; var11 < 16; var11++) {
                     BlockState var12 = var8.getBlockState(var9, var10, var11);
                     if (var12.isOf(Blocks.SEAGRASS) || var12.isOf(Blocks.TALL_SEAGRASS)) {
                        if (++var2 >= var3) {
                           return true;
                        }
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   private ExecutorService getExecutorService() {
      if (this.executorService == null || this.executorService.isShutdown()) {
         this.executorService = Executors.newSingleThreadExecutor(ChunkFinderV2Module::threadOf);
      }

      return this.executorService;
   }

   @InternalHelper5
   private void run3(ActivityChunkFinderModuleData var1) {
      if (class310.world != null && !this.set.isEmpty()) {
         boolean var2 = this.val12.getObject() && BlockEspPlusModuleData.class243 != null;
         ActivityChunkFinderModuleEntry2 var3 = this.val13.getObject();
         ActivityChunkFinderModuleEntry2 var4 = this.val14.getObject();
         ActivityChunkFinderModuleEntry2 var5 = this.val15.getObject();
         ActivityChunkFinderModuleEntry2 var6 = this.val16.getObject();

         for (ChunkPos var8 : this.set) {
            if (this.check(var8)) {
               double var9 = var8.getStartX();
               double var11 = var8.getStartZ();
               double var13 = this.val17.getObject().intValue();
               run4(var1, var9 - 16.0, var13, var11 - 16.0, var9 + 32.0, var13 + 0.1, var11 + 32.0, var3, var4);
               double var15 = var13 + 0.1 + 0.02;
               run4(var1, var9, var15, var11, var9 + 16.0, var15 + 0.1, var11 + 16.0, var5, var6);
               if (var2) {
                  var1.val
                     .run6(
                        BlockEspPlusModuleData.class243.x,
                        BlockEspPlusModuleData.class243.y,
                        BlockEspPlusModuleData.class243.z,
                        var9 + 8.0,
                        var15,
                        var11 + 8.0,
                        var6
                     );
               }
            }
         }
      }
   }

   private static void run4(
      ActivityChunkFinderModuleData var0,
      double var1,
      double var3,
      double var5,
      double var7,
      double var9,
      double var11,
      ActivityChunkFinderModuleEntry2 var13,
      ActivityChunkFinderModuleEntry2 var14
   ) {
      var0.val.run4(var1, var3, var5, var7, var9, var11, var13, 120);
      var0.val.run4(var1, var3, var5, var7, var9, var11, var14, 6);
      var0.val.run5(var1, var3, var5, var7, var9, var11, var14, 0);
      var0.val.run5(var1 + 0.12, var3, var5 + 0.12, var7 - 0.12, var9, var11 - 0.12, var14, 0);
   }

   private static Thread threadOf(Runnable var0) {
      Thread var1 = new Thread(var0, "chunk-finder-v2-scan");
      var1.setDaemon(true);
      return var1;
   }

   private static boolean check6(BlockState var0) {
      return var0.isOf(Blocks.SEAGRASS) || var0.isOf(Blocks.TALL_SEAGRASS);
   }

   private static boolean check7(BlockState var0) {
      return var0.isOf(Blocks.VINE);
   }

   private static int intOf(ChunkPos var0, ChunkPos var1) {
      return Math.max(Math.abs(var1.x - var0.x), Math.abs(var1.z - var0.z));
   }

   private void run5(List var1, List var2) {
      try {
         for (int var3 = 0; var3 < var1.size(); var3++) {
            if (this.check2((WorldChunk)var2.get(var3))) {
               if (System.currentTimeMillis() - this.longVal2 >= this.val6.getObject().intValue() * 1000L) {
                  this.longVal2 = System.currentTimeMillis();
                  ChunkPos var4 = (ChunkPos)var1.get(var3);
                  this.set.clear();
                  this.set.add(var4);
                  this.longVal3 = 0L;
                  if (this.val8.getObject()) {
                     ChunkFinderV2ModuleUtil.run10(this.title, "Chunk found at " + (var4.x * 16 + 8) + ", " + (var4.z * 16 + 8));
                  }
               }
               break;
            }
         }
      } catch (Throwable var8) {
         SwyzzyAddon.logger.warn("Chunk Finder V2 scan failed.", var8);
      } finally {
         this.atomicBoolean.set(false);
      }
   }
}
