package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class NovaDebugModule extends Module {
   private static final int[][] intArray = new int[][]{{1, 3}, {1, 4}, {1, 5}, {2, 3}, {3, 3}, {4, 3}, {5, 3}, {4, 4}, {5, 5}};
   private static final int intVal = 48;
   private static final int intVal2 = -64;
   private static final int intVal3 = 128;
   private static final int intVal4 = 14;
   private static final int intVal5 = 5;
   private static final double doubleVal = 0.1;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Render");
   private final Setting<Boolean> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("smart-check")
            .valOf2("Use the density, shape and calcite checks instead of counting amethyst only.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Integer> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("sensitivity")
            .valOf2("1 = loose (more chunks), 10 = strict (fewer false flags).")
            .valOf3(8)
            .valOf4(1, 10)
            .valOf5(1, 10)
            .getVal()
      );
   private final Setting<Integer> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-radius")
            .valOf2("Radius multiplier; the scan covers radius * 5 chunks around you.")
            .valOf3(2)
            .valOf4(1, 5)
            .valOf5(1, 5)
            .getVal()
      );
   private final Setting<Integer> val6 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("chunks-per-tick")
            .valOf2("How many chunks are analysed per tick.")
            .valOf3(6)
            .valOf4(1, 20)
            .valOf5(1, 12)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val7 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("fill-color")
            .valOf2("Fill color of the flagged chunks.")
            .valOf3(new ActivityChunkFinderModuleHelper4(180, 60, 60, 255))
            .getVal()
      );
   private final Setting<Integer> val8 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper6().valOf("fill-alpha").valOf2("Opacity of the fill.").valOf3(30).valOf4(0, 255).valOf5(0, 255).getVal());
   private final Setting<ActivityChunkFinderModuleHelper4> val9 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("line-color")
            .valOf2("Outline color of the flagged chunks.")
            .valOf3(new ActivityChunkFinderModuleHelper4(180, 60, 60, 255))
            .getVal()
      );
   private final Setting<RenderMode> val10 = this.val2_2
      .addSetting(new BaseEspModuleHelper2<RenderMode>().valOf("shape-mode").valOf2("How the markers are drawn.").valOf3(RenderMode.Both).getVal());
   private final Set<Long> set = ConcurrentHashMap.newKeySet();
   private final Set<Long> set2 = ConcurrentHashMap.newKeySet();
   private final Set<Long> set3 = ConcurrentHashMap.newKeySet();
   private final Deque<ChunkPos> deque = new ArrayDeque<>();
   private final Map<Long, int[]> map = new ConcurrentHashMap<>();
   private final Set<Long> set4 = ConcurrentHashMap.newKeySet();
   private final AtomicBoolean atomicBoolean = new AtomicBoolean();
   private ExecutorService executorService;
   private byte[][][] byteArray;
   private int intVal6;

   public NovaDebugModule() {
      super(SwyzzyAddon.val2, "nova-debug", "Highlights geode-like chunks and deep chest clusters.");
   }

   @Override
   public void run6() {
      this.run12();
      this.executorService = Executors.newSingleThreadExecutor(NovaDebugModule::threadOf);
   }

   @Override
   public void run7() {
      if (this.executorService != null) {
         this.executorService.shutdownNow();
         this.executorService = null;
      }

      this.run12();
   }

   @Override
   public String getString2() {
      int var1 = this.set.size() + this.set2.size();
      return var1 == 0 ? null : String.valueOf(var1);
   }

   private void run12() {
      this.set.clear();
      this.set2.clear();
      this.set3.clear();
      this.deque.clear();
      this.map.clear();
      this.set4.clear();
      this.atomicBoolean.set(false);
      this.intVal6 = 0;
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.world != null && class310.player != null && this.executorService != null) {
         if (++this.intVal6 % 5 == 0) {
            this.run13();
         }

         if (!this.deque.isEmpty() && this.atomicBoolean.compareAndSet(false, true)) {
            ArrayList var2 = new ArrayList();

            while (var2.size() < this.val6.getObject() && !this.deque.isEmpty()) {
               ChunkPos var3 = this.deque.poll();
               WorldChunk var4 = class310.world.getChunkManager().getWorldChunk(var3.x, var3.z);
               if (var4 != null && !var4.isEmpty()) {
                  var2.add(var4);
               }
            }

            if (var2.isEmpty()) {
               this.atomicBoolean.set(false);
            } else {
               this.executorService.submit(() -> this.run11(var2));
            }
         }
      }
   }

   private void run13() {
      ChunkPos var1 = class310.player.getChunkPos();
      int var2 = this.val5.getObject() * 5;
      this.set3.removeIf(var905 -> NovaDebugModule.check5(var1, var2, var905));

      for (int var3 = var1.x - var2; var3 <= var1.x + var2; var3++) {
         for (int var4 = var1.z - var2; var4 <= var1.z + var2; var4++) {
            long var5 = ChunkPos.toLong(var3, var4);
            if (!this.set3.contains(var5)) {
               WorldChunk var7 = class310.world.getChunkManager().getWorldChunk(var3, var4);
               if (var7 != null && !var7.isEmpty()) {
                  this.deque.add(new ChunkPos(var3, var4));
                  this.set3.add(var5);
               }
            }
         }
      }
   }

   private void run2(WorldChunk var1) {
      long var2 = var1.getPos().toLong();
      if (this.check2(var1)) {
         this.set.add(var2);
      }

      if (this.check3(var1)) {
         this.set2.add(var2);
      }
   }

   private int getInt() {
      return 18 + (this.val4.getObject() - 1) * 3;
   }

   private float getFloat2() {
      return 0.01F + (this.val4.getObject() - 1) * 0.00111F;
   }

   private int getInt2() {
      return (int)(8.0 + (this.val4.getObject() - 1) * 1.11F);
   }

   private float getFloat() {
      return 0.258F - (this.val4.getObject() - 1) * 0.022F;
   }

   private float getFloat3() {
      return 8.15F - (this.val4.getObject() - 1) * 0.35F;
   }

   private static boolean check(BlockState var0) {
      return var0.isOf(Blocks.AMETHYST_CLUSTER)
         || var0.isOf(Blocks.LARGE_AMETHYST_BUD)
         || var0.isOf(Blocks.MEDIUM_AMETHYST_BUD)
         || var0.isOf(Blocks.SMALL_AMETHYST_BUD)
         || var0.isOf(Blocks.AMETHYST_BLOCK);
   }

   private boolean check2(WorldChunk var1) {
      ChunkSection[] var2 = var1.getSectionArray();
      int var3 = var1.getBottomY();
      int var4 = 0;
      int var5 = 0;
      int var6 = 0;
      int var7 = 0;
      int var8 = 0;
      int var9 = 0;
      ArrayList var10 = new ArrayList();
      byte[][][] var11 = this.getByteArray();

      for (int var12 = 0; var12 < var2.length; var12++) {
         int var13 = var3 + var12 * 16;
         if (var13 <= 48 && var13 + 16 >= -64) {
            ChunkSection var14 = var2[var12];
            if (var14 != null && !var14.isEmpty()) {
               for (int var15 = 0; var15 < 16; var15++) {
                  for (int var16 = 0; var16 < 16; var16++) {
                     int var17 = var13 + var16;
                     if (var17 >= -64 && var17 <= 48) {
                        int var18 = var17 - -64;

                        for (int var19 = 0; var19 < 16; var19++) {
                           BlockState var20 = var14.getBlockState(var15, var16, var19);
                           if (var20.isOf(Blocks.BUDDING_AMETHYST)) {
                              return false;
                           }

                           if (check(var20)) {
                              var11[var15][var18][var19] = 1;
                              var4++;
                              var7 += var15;
                              var8 += var17;
                              var9 += var19;
                              var10.add(new int[]{var15, var17, var19});
                           } else if (var20.isOf(Blocks.CALCITE)) {
                              var11[var15][var18][var19] = 2;
                              var5++;
                           } else if (var20.isOf(Blocks.SMOOTH_BASALT)) {
                              var11[var15][var18][var19] = 3;
                              var6++;
                           } else if (!var20.isAir() && !var20.isOf(Blocks.CAVE_AIR) && !var20.isOf(Blocks.VOID_AIR)) {
                              var11[var15][var18][var19] = 5;
                           } else {
                              var11[var15][var18][var19] = 4;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      if (var4 == 0) {
         return false;
      } else if (!this.val3_2.getObject()) {
         return var4 >= this.getInt();
      } else {
         int var24 = Integer.MAX_VALUE;
         int var25 = Integer.MIN_VALUE;

         for (int[] var28 : (Iterable<int[]>)(Object)(var10)) {
            var24 = Math.min(var24, var28[1]);
            var25 = Math.max(var25, var28[1]);
         }

         float var27 = var4 / (256.0F * Math.max(1, var25 - var24 + 1));
         float var29 = (float)var7 / var4;
         float var30 = (float)var8 / var4;
         float var31 = (float)var9 / var4;
         float var32 = 0.0F;

         for (int[] var36 : (Iterable<int[]>)(Object)(var10)) {
            float var21 = var36[0] - var29;
            float var22 = var36[1] - var30;
            float var23 = var36[2] - var31;
            var32 += (float)Math.sqrt(var21 * var21 + var22 * var22 + var23 * var23);
         }

         var32 /= var4;
         float var35 = floatOf(var11, var10);
         return var4 >= this.getInt()
            && var27 >= this.getFloat2()
            && var5 + var6 >= this.getInt2()
            && var5 >= 4
            && var6 >= 4
            && var32 <= this.getFloat3()
            && var35 <= this.getFloat();
      }
   }

   private static float floatOf(byte[][][] var0, List<int[]> var1) {
      int[] var2 = new int[]{1, -1, 0, 0, 0, 0};
      int[] var3 = new int[]{0, 0, 1, -1, 0, 0};
      int[] var4 = new int[]{0, 0, 0, 0, 1, -1};
      int var5 = 0;
      int var6 = 0;

      for (int[] var8 : var1) {
         int var9 = var8[0];
         int var10 = var8[1] - -64;
         int var11 = var8[2];

         for (int var12 = 0; var12 < 6; var12++) {
            int var13 = var9 + var2[var12];
            int var14 = var10 + var3[var12];
            int var15 = var11 + var4[var12];
            if (var13 >= 0 && var13 <= 15 && var14 >= 0 && var14 < 128 && var15 >= 0 && var15 <= 15) {
               byte var16 = var0[var13][var14][var15];
               if (var16 == 4) {
                  var5++;
               } else if (var16 >= 2) {
                  var6++;
               }
            }
         }
      }

      return var5 + var6 > 0 ? (float)var5 / (var5 + var6) : 0.0F;
   }

   private byte[][][] getByteArray() {
      if (this.byteArray == null) {
         this.byteArray = new byte[16][128][16];
      }

      for (byte[][] var4 : this.byteArray) {
         for (byte[] var8 : var4) {
            Arrays.fill(var8, (byte)0);
         }
      }

      return this.byteArray;
   }

   private boolean check3(WorldChunk var1) {
      ChunkSection[] var2 = var1.getSectionArray();
      int var3 = var1.getBottomY();
      int var4 = 0;

      for (int var5 = 0; var5 < var2.length; var5++) {
         int var6 = var3 + var5 * 16;
         if (var6 >= 0) {
            break;
         }

         ChunkSection var7 = var2[var5];
         if (var7 != null && !var7.isEmpty() && var7.getBlockStateContainer().hasAny(NovaDebugModule::check4)) {
            int var8 = Math.min(15, -var6 - 1);

            for (int var9 = 0; var9 < 16; var9++) {
               for (int var10 = 0; var10 < 16; var10++) {
                  for (int var11 = 0; var11 <= var8; var11++) {
                     if (check4(var7.getBlockState(var9, var11, var10))) {
                        if (++var4 >= 14) {
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

   private static boolean check4(BlockState var0) {
      return var0.isOf(Blocks.CHEST) || var0.isOf(Blocks.TRAPPED_CHEST);
   }

   @InternalHelper5
   private void run3(ActivityChunkFinderModuleData var1) {
      if (class310.world != null && class310.player != null) {
         if (!this.set.isEmpty() || !this.set2.isEmpty()) {
            double var4 = 63.0 + 0.1;
            ActivityChunkFinderModuleHelper4 var6 = this.val7.getObject();
            ActivityChunkFinderModuleHelper4 var7 = this.val9.getObject();
            int var8 = this.val8.getObject();
            ActivityChunkFinderModuleEntry2 var9 = new ActivityChunkFinderModuleEntry2(var6.intVal, var6.intVal2, var6.intVal3, var8);
            ActivityChunkFinderModuleEntry2 var10 = new ActivityChunkFinderModuleEntry2(var6.intVal, var6.intVal2, var6.intVal3, Math.min(255, var8 + 40));

            for (long var12 : this.set) {
               int var14 = ChunkPos.getPackedX(var12);
               int var15 = ChunkPos.getPackedZ(var12);
               int[] var16 = this.intArrayOf(var12);
               double var17 = var14 << 4;
               double var19 = var15 << 4;
               var1.val.run(var17, 63.0, var19, var17 + var16[0] * 16.0, var4, var19 + var16[1] * 16.0, var9, var7, this.val10.getObject(), 0);
            }

            for (long var22 : this.set2) {
               ChunkPos var23 = new ChunkPos(ChunkPos.getPackedX(var22), ChunkPos.getPackedZ(var22));
               var1.val
                  .run(var23.getStartX(), 63.0, var23.getStartZ(), var23.getEndX() + 1.0, var4, var23.getEndZ() + 1.0, var10, var7, this.val10.getObject(), 0);
            }
         }
      }
   }

   private int[] intArrayOf(long var1) {
      int[] var3 = this.map.get(var1);
      if (var3 != null) {
         return var3;
      } else {
         int var4 = ChunkPos.getPackedX(var1);
         int var5 = ChunkPos.getPackedZ(var1);
         ArrayList var6 = new ArrayList<>(Arrays.asList(intArray));
         Collections.shuffle(var6);

         for (int[] var8 : (Iterable<int[]>)(Object)(var6)) {
            boolean var9 = true;

            for (int var10 = 0; var10 < var8[0] && var9; var10++) {
               for (int var11 = 0; var11 < var8[1]; var11++) {
                  if (this.set4.contains(ChunkPos.toLong(var4 + var10, var5 + var11))) {
                     var9 = false;
                     break;
                  }
               }
            }

            if (var9) {
               for (int var13 = 0; var13 < var8[0]; var13++) {
                  for (int var14 = 0; var14 < var8[1]; var14++) {
                     this.set4.add(ChunkPos.toLong(var4 + var13, var5 + var14));
                  }
               }

               this.map.put(var1, var8);
               return var8;
            }
         }

         int[] var12 = new int[]{1, 1};
         this.set4.add(var1);
         this.map.put(var1, var12);
         return var12;
      }
   }

   private static boolean check5(ChunkPos var0, int var1, Long var2) {
      return Math.abs(ChunkPos.getPackedX(var2) - var0.x) > var1 + 2 || Math.abs(ChunkPos.getPackedZ(var2) - var0.z) > var1 + 2;
   }

   private void run11(List var1) {
      try {
         for (WorldChunk var3 : (Iterable<WorldChunk>)(Object)(var1)) {
            this.run2(var3);
         }
      } catch (Throwable var7) {
         SwyzzyAddon.logger.warn("Nova Debug chunk scan failed.", var7);
      } finally {
         this.atomicBoolean.set(false);
      }
   }

   private static Thread threadOf(Runnable var0) {
      Thread var1 = new Thread(var0, "NovaDebug-Scanner");
      var1.setDaemon(true);
      return var1;
   }
}
