package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

public final class FutureDebugModule extends Module {
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Boolean> val2_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("Smart Check")
            .valOf2("Adds density, shell, spread and exposure checks on top of the raw amethyst count.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Integer> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("Sensitivity")
            .valOf2("Higher values loosen every threshold, so more chunks pass.")
            .valOf3(5)
            .valOf5(1, 10)
            .getVal()
      );
   private final Setting<Integer> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("Scan Radius")
            .valOf2("Radius around you that is scanned, in units of five chunks.")
            .valOf3(1)
            .valOf5(1, 5)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("Fill Color")
            .valOf2("Colour of the chunk markers.")
            .valOf3(new ActivityChunkFinderModuleHelper4(180, 60, 60, 255))
            .getVal()
      );
   private final Setting<Integer> val6 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper6().valOf("Fill Alpha").valOf2("Opacity of the chunk markers.").valOf3(30).valOf5(0, 255).getVal());
   private final Set<Long> set = ConcurrentHashMap.newKeySet();
   private final Set<ChunkPos> set2 = ConcurrentHashMap.newKeySet();
   private ExecutorService executorService;
   private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
   private int intVal = 0;
   private static final int[][] intArray = new int[][]{{1, 3}, {1, 4}, {1, 5}, {2, 3}, {3, 3}, {4, 3}, {5, 3}, {4, 4}, {5, 5}};
   private final Map<Long, int[]> map = new ConcurrentHashMap<>();
   private final Set<Long> set3 = ConcurrentHashMap.newKeySet();

   public FutureDebugModule() {
      super(SwyzzyAddon.val2, "future-debug", "FutureDebug");
   }

   @Override
   public void run6() {
      this.set.clear();
      this.set2.clear();
      this.map.clear();
      this.set3.clear();
      this.intVal = 0;
      this.atomicBoolean.set(false);
   }

   @Override
   public void run7() {
      this.set.clear();
      this.set2.clear();
      this.map.clear();
      this.set3.clear();
      if (this.executorService != null) {
         this.executorService.shutdownNow();
         this.executorService = null;
      }
   }

   @Override
   public String getString2() {
      int var1 = this.set.size();
      return var1 == 0 ? null : String.valueOf(var1);
   }

   private int getInt() {
      return (int)(40.0 - (this.val3_2.getObject() - 1) * 3.5555555555555554);
   }

   private float getFloat() {
      return 0.015F - (this.val3_2.getObject() - 1) * 0.0013333333F;
   }

   private int getInt2() {
      return (int)(15.0 - (this.val3_2.getObject() - 1) * 1.3333333333333333);
   }

   private float getFloat2() {
      return 0.08F + (this.val3_2.getObject() - 1) * 0.027F;
   }

   private float getFloat3() {
      return 6.0F + (this.val3_2.getObject() - 1) * 0.44444445F;
   }

   private boolean check(BlockState var1) {
      return var1.isOf(Blocks.AMETHYST_CLUSTER)
         || var1.isOf(Blocks.LARGE_AMETHYST_BUD)
         || var1.isOf(Blocks.MEDIUM_AMETHYST_BUD)
         || var1.isOf(Blocks.SMALL_AMETHYST_BUD)
         || var1.isOf(Blocks.AMETHYST_BLOCK);
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
      int var10 = 0;
      int var11 = 0;
      ArrayList var12 = new ArrayList();
      byte[][][] var13 = new byte[16][128][16];

      for (int var14 = 0; var14 < var2.length; var14++) {
         int var15 = var3 + var14 * 16;
         if (var15 <= 48 && var15 + 16 >= -64) {
            ChunkSection var16 = var2[var14];
            if (var16 != null && !var16.isEmpty()) {
               for (int var17 = 0; var17 < 16; var17++) {
                  for (int var18 = 0; var18 < 16; var18++) {
                     int var19 = var15 + var18;
                     if (var19 >= -64 && var19 <= 48) {
                        int var20 = var19 + 64;
                        if (var20 >= 0 && var20 < 128) {
                           for (int var21 = 0; var21 < 16; var21++) {
                              BlockState var22 = var16.getBlockState(var17, var18, var21);
                              if (var22.isOf(Blocks.BUDDING_AMETHYST)) {
                                 return false;
                              }

                              if (this.check(var22)) {
                                 var13[var17][var20][var21] = 1;
                                 var4++;
                                 var9 += var17;
                                 var10 += var19;
                                 var11 += var21;
                                 var12.add(new int[]{var17, var19, var21});
                              } else if (var22.isOf(Blocks.CALCITE)) {
                                 var13[var17][var20][var21] = 2;
                                 var5++;
                              } else if (var22.isOf(Blocks.SMOOTH_BASALT)) {
                                 var13[var17][var20][var21] = 3;
                                 var6++;
                              } else if (!var22.isAir() && !var22.isOf(Blocks.CAVE_AIR) && !var22.isOf(Blocks.VOID_AIR)) {
                                 var13[var17][var20][var21] = 5;
                              } else {
                                 var13[var17][var20][var21] = 4;
                              }
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
      } else if (!this.val2_2.getObject()) {
         return var4 >= this.getInt();
      } else {
         int var34 = Integer.MAX_VALUE;
         int var35 = Integer.MIN_VALUE;

         for (int[] var38 : (Iterable<int[]>)(Object)(var12)) {
            if (var38[1] < var34) {
               var34 = var38[1];
            }

            if (var38[1] > var35) {
               var35 = var38[1];
            }
         }

         float var37 = var4 / (256.0F * Math.max(1, var35 - var34 + 1));
         float var39 = (float)var9 / var4;
         float var40 = (float)var10 / var4;
         float var41 = (float)var11 / var4;
         float var42 = 0.0F;

         for (int[] var46 : (Iterable<int[]>)(Object)(var12)) {
            float var23 = var46[0] - var39;
            float var24 = var46[1] - var40;
            float var25 = var46[2] - var41;
            var42 += (float)Math.sqrt(var23 * var23 + var24 * var24 + var25 * var25);
         }

         var42 /= var4;
         int[] var45 = new int[]{1, -1, 0, 0, 0, 0};
         int[] var47 = new int[]{0, 0, 1, -1, 0, 0};
         int[] var48 = new int[]{0, 0, 0, 0, 1, -1};

         for (int[] var51 : (Iterable<int[]>)(Object)(var12)) {
            int var26 = var51[0];
            int var27 = var51[1] + 64;
            int var28 = var51[2];

            for (int var29 = 0; var29 < 6; var29++) {
               int var30 = var26 + var45[var29];
               int var31 = var27 + var47[var29];
               int var32 = var28 + var48[var29];
               if (var30 >= 0 && var30 <= 15 && var31 >= 0 && var31 < 128 && var32 >= 0 && var32 <= 15) {
                  byte var33 = var13[var30][var31][var32];
                  if (var33 == 4) {
                     var7++;
                  } else if (var33 >= 2) {
                     var8++;
                  }
               }
            }
         }

         int var50 = var7 + var8;
         float var52 = var50 > 0 ? (float)var7 / var50 : 0.0F;
         return var4 >= this.getInt() && var37 >= this.getFloat() && var5 + var6 >= this.getInt2() && var42 <= this.getFloat3() && var52 <= this.getFloat2();
      }
   }

   private boolean check3(WorldChunk var1) {
      int var2 = 0;
      ChunkSection[] var3 = var1.getSectionArray();
      int var4 = var1.getBottomY();

      for (int var5 = 0; var5 < var3.length; var5++) {
         int var6 = var4 + var5 * 16;
         if (var6 >= 0) {
            break;
         }

         ChunkSection var7 = var3[var5];
         if (var7 != null && !var7.isEmpty() && var7.hasAny(FutureDebugModule::check6)) {
            int var8 = Math.min(15, -var6 - 1);

            for (int var9 = 0; var9 < 16; var9++) {
               for (int var10 = 0; var10 < 16; var10++) {
                  for (int var11 = 0; var11 <= var8; var11++) {
                     BlockState var12 = var7.getBlockState(var9, var11, var10);
                     if (var12.isOf(Blocks.CHEST) || var12.isOf(Blocks.TRAPPED_CHEST)) {
                        if (++var2 >= 10) {
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

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.world != null && class310.player != null) {
         ChunkPos var2 = class310.player.getChunkPos();
         int var3 = this.val4.getObject() * 5;
         this.set.removeIf(var905 -> FutureDebugModule.check5(var2, var3, var905));
         this.set2.removeIf(var905 -> FutureDebugModule.check4(var2, var3, var905));
         if (++this.intVal % 5 == 0) {
            if (this.atomicBoolean.compareAndSet(false, true)) {
               if (this.executorService == null || this.executorService.isShutdown()) {
                  this.executorService = Executors.newSingleThreadExecutor(FutureDebugModule::threadOf);
               }

               ArrayList var4 = new ArrayList();
               ArrayList var5 = new ArrayList();

               for (int var6 = -var3; var6 <= var3; var6++) {
                  for (int var7 = -var3; var7 <= var3; var7++) {
                     ChunkPos var8 = new ChunkPos(var2.x + var6, var2.z + var7);
                     WorldChunk var9 = class310.world.getChunkManager().getWorldChunk(var8.x, var8.z, false);
                     if (var9 != null && !var9.isEmpty()) {
                        var4.add(var8);
                        var5.add(var9);
                     }
                  }
               }

               this.executorService.submit((Runnable)this::run3);
            }
         }
      }
   }

   private int[] intArrayOf(long var1) {
      if (this.map.containsKey(var1)) {
         return this.map.get(var1);
      } else {
         int var3 = ChunkPos.getPackedX(var1);
         int var4 = ChunkPos.getPackedZ(var1);
         ArrayList var5 = new ArrayList<>(Arrays.asList(intArray));
         Collections.shuffle(var5);

         for (int[] var7 : (Iterable<int[]>)(Object)(var5)) {
            int var8 = var7[0];
            int var9 = var7[1];
            boolean var10 = true;

            label51:
            for (int var11 = 0; var11 < var8; var11++) {
               for (int var12 = 0; var12 < var9; var12++) {
                  if (this.set3.contains(ChunkPos.toLong(var3 + var11, var4 + var12))) {
                     var10 = false;
                     break label51;
                  }
               }
            }

            if (var10) {
               for (int var13 = 0; var13 < var8; var13++) {
                  for (int var14 = 0; var14 < var9; var14++) {
                     this.set3.add(ChunkPos.toLong(var3 + var13, var4 + var14));
                  }
               }

               this.map.put(var1, new int[]{var8, var9});
               return new int[]{var8, var9};
            }
         }

         this.set3.add(var1);
         this.map.put(var1, new int[]{1, 1});
         return new int[]{1, 1};
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData var1) {
      if (class310.world != null && class310.player != null) {
         if (!this.set.isEmpty()) {
            ActivityChunkFinderModuleHelper4 var2 = this.val5.getObject();
            ActivityChunkFinderModuleEntry2 var3 = new ActivityChunkFinderModuleEntry2(var2.intVal, var2.intVal2, var2.intVal3, this.val6.getObject());

            for (long var5 : this.set) {
               int var7 = ChunkPos.getPackedX(var5);
               int var8 = ChunkPos.getPackedZ(var5);
               int[] var9 = this.intArrayOf(var5);
               double var10 = var7 << 4;
               double var12 = var8 << 4;
               double var14 = var10 + var9[0] * 16.0;
               double var16 = var12 + var9[1] * 16.0;
               var1.val.run(var10, 63.0, var12, var14, 63.1, var16, var3, var3, RenderMode.Sides, 0);
            }
         }
      }
   }

   private void run3(ArrayList var1, ArrayList var2) {
      try {
         for (int var3 = 0; var3 < var1.size(); var3++) {
            ChunkPos var4 = (ChunkPos)var1.get(var3);
            WorldChunk var5 = (WorldChunk)var2.get(var3);
            if (this.check2(var5)) {
               this.set.add(var4.toLong());
            }

            if (this.check3(var5)) {
               this.set2.add(var4);
            }
         }
      } catch (Exception var9) {
      } finally {
         this.atomicBoolean.set(false);
      }
   }

   private static Thread threadOf(Runnable var0) {
      Thread var1 = new Thread(var0, "futuredebug-scan");
      var1.setDaemon(true);
      return var1;
   }

   private static boolean check4(ChunkPos var0, int var1, ChunkPos var2) {
      return Math.abs(var2.x - var0.x) > var1 + 2 || Math.abs(var2.z - var0.z) > var1 + 2;
   }

   private static boolean check5(ChunkPos var0, int var1, Long var2) {
      int var3 = ChunkPos.getPackedX(var2);
      int var4 = ChunkPos.getPackedZ(var2);
      return Math.abs(var3 - var0.x) > var1 + 3 || Math.abs(var4 - var0.z) > var1 + 3;
   }

   private static boolean check6(BlockState var0) {
      return var0.isOf(Blocks.CHEST) || var0.isOf(Blocks.TRAPPED_CHEST);
   }
}
