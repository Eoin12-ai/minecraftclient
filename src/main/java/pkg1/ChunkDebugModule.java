package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import util.Utils_2;

public final class ChunkDebugModule extends Module {
   private static final int intVal = -64;
   private static final int intVal2 = 48;
   private static final int intVal3 = 100;
   private static final int intVal4 = 2;
   private static final int intVal5 = 50;
   private static final double doubleVal = 0.1;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Render");
   private final Setting<Boolean> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("smart-check")
            .valOf2("Uses shell, compactness, and air-exposure checks to reduce false positives.")
            .valOf3(true)
            .valOf4(this::run9)
            .getVal()
      );
   private final Setting<Integer> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("sensitivity")
            .valOf2("Higher values accept weaker depleted-geode evidence.")
            .valOf3(5)
            .valOf4(1, 10)
            .valOf5(1, 10)
            .valOf8(this::run8)
            .getVal()
      );
   private final Setting<Integer> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-radius")
            .valOf2("Scan radius multiplier. Each step represents five chunks.")
            .valOf3(1)
            .valOf4(1, 5)
            .valOf5(1, 5)
            .valOf8(this::run11)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val6 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("fill-color")
            .valOf2("Color used for suspicious chunk markers.")
            .valOf3(new ActivityChunkFinderModuleHelper4(180, 60, 60, 255))
            .getVal()
      );
   private final Setting<Integer> val7 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("fill-alpha")
            .valOf2("Opacity of suspicious chunk markers.")
            .valOf3(30)
            .valOf4(0, 255)
            .valOf5(0, 255)
            .getVal()
      );
   private final Setting<Double> val8 = this.val2_2
      .addSetting(
         new AutoTotemModuleHelper2()
            .valOf("render-y")
            .valOf2("Y level used to render suspicious chunks.")
            .valOf3(63.0)
            .valOf4(-64.0, 320.0)
            .valOf5(-64.0, 320.0)
            .getVal()
      );
   private final ArrayDeque<ChunkPos> arrayDeque = new ArrayDeque<>();
   private final Set<Long> set = new HashSet<>();
   private final Map<Long, ChunkDebugModule.Inner1> map = new HashMap<>();
   private ClientWorld class638;
   private int intVal6;

   public ChunkDebugModule() {
      super(SwyzzyAddon.val2, "chunk-debug", "Chunk Debug");
      Utils_2.run(this, "Chunk Debug");
   }

   @Override
   public void run6() {
      this.class638 = class310.world;
      this.run13();
   }

   @Override
   public void run7() {
      this.run15();
      this.class638 = null;
   }

   @Override
   public String getString2() {
      return this.map.isEmpty() ? null : String.valueOf(this.map.size());
   }

   @InternalHelper5
   private void run4(AmethystChunkFinderModuleHelper var1) {
      if (var1.chunk() != null) {
         this.run5(var1.chunk().getPos(), true);
      }
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleData2 var1) {
      if (class310.player != null && class310.world != null) {
         if (var1.class2338.getY() >= -64 && var1.class2338.getY() <= 48) {
            this.run5(new ChunkPos(var1.class2338), true);
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null) {
         if (this.class638 != class310.world) {
            this.class638 = class310.world;
            this.run13();
         }

         if (++this.intVal6 >= 100) {
            this.intVal6 = 0;
            this.run12();
         }

         for (int var2 = 0; var2 < 2 && !this.arrayDeque.isEmpty(); var2++) {
            ChunkPos var3 = this.arrayDeque.removeFirst();
            this.set.remove(var3.toLong());
            WorldChunk var4 = class310.world.getChunkManager().getWorldChunk(var3.x, var3.z);
            if (var4 != null) {
               ChunkDebugModule.Inner1 var5 = this.valOf(var4);
               if (var5 == null) {
                  this.map.remove(var3.toLong());
               } else {
                  this.map.put(var3.toLong(), var5);
               }
            }
         }
      }
   }

   @InternalHelper5
   private void run3(ActivityChunkFinderModuleData var1) {
      if (class310.player != null && !this.map.isEmpty()) {
         ActivityChunkFinderModuleHelper4 var2 = this.val6.getObject();
         int var3 = intOf2(this.val7.getObject(), 0, 255);
         ActivityChunkFinderModuleEntry2 var4 = new ActivityChunkFinderModuleEntry2(var2.intVal, var2.intVal2, var2.intVal3, var3);
         ActivityChunkFinderModuleEntry2 var5 = new ActivityChunkFinderModuleEntry2(var2.intVal, var2.intVal2, var2.intVal3, Math.min(255, var3 + 100));
         double var6 = this.val8.getObject();
         int var8 = 0;
         ArrayList var9 = new ArrayList<>(this.map.values());
         var9.sort(Comparator.comparingInt(ChunkDebugModule.Inner1::score).reversed());

         for (ChunkDebugModule.Inner1 var11 : (Iterable<ChunkDebugModule.Inner1>)(Object)(var9)) {
            if (var8++ >= 50) {
               break;
            }

            ChunkPos var12 = var11.pos();
            var1.val.run(var12.getStartX(), var6, var12.getStartZ(), var12.getEndX() + 1, var6 + 0.1, var12.getEndZ() + 1, var4, var5, RenderMode.Both, 0);
         }
      }
   }

   private ChunkDebugModule.Inner1 valOf(WorldChunk var1) {
      ArrayList var2 = new ArrayList();
      int var3 = 0;
      ChunkPos var4 = var1.getPos();
      int var5 = Math.max(-64, var1.getBottomY());
      int var6 = Math.min(48, var1.getBottomY() + var1.getHeight() - 1);
      int var7 = Math.floorDiv(var5, 16);
      int var8 = Math.floorDiv(var6, 16);

      for (int var9 = var7; var9 <= var8; var9++) {
         ChunkSection var10 = var1.getSection(var1.sectionCoordToIndex(var9));
         if (!var10.isEmpty() && var10.hasAny(ChunkDebugModule::check)) {
            int var11 = var9 == var7 ? Math.floorMod(var5, 16) : 0;
            int var12 = var9 == var8 ? Math.floorMod(var6, 16) : 15;

            for (int var13 = var11; var13 <= var12; var13++) {
               int var14 = var9 * 16 + var13;

               for (int var15 = 0; var15 < 16; var15++) {
                  for (int var16 = 0; var16 < 16; var16++) {
                     BlockState var17 = var10.getBlockState(var15, var13, var16);
                     if (var17.isOf(Blocks.BUDDING_AMETHYST)) {
                        return null;
                     }

                     if (check4(var17)) {
                        var2.add(new BlockPos(var4.getStartX() + var15, var14, var4.getStartZ() + var16));
                     } else if (var17.isOf(Blocks.CALCITE) || var17.isOf(Blocks.SMOOTH_BASALT)) {
                        var3++;
                     }
                  }
               }
            }
         }
      }

      int var34 = Math.max(10, 54 - this.val4.getObject() * 5);
      if (var2.size() < var34) {
         return null;
      } else if (!this.val3_2.getObject()) {
         new Inner1(var4, var2.size());
      } else {
         int var35 = Math.max(3, 18 - this.val4.getObject());
         if (var3 < var35) {
            return null;
         } else {
            double var36 = 0.0;
            double var38 = 0.0;
            double var40 = 0.0;

            for (BlockPos var18 : (Iterable<BlockPos>)(Object)(var2)) {
               var36 += var18.getX();
               var38 += var18.getY();
               var40 += var18.getZ();
            }

            var36 /= var2.size();
            var38 /= var2.size();
            var40 /= var2.size();
            double var43 = 0.0;
            int var19 = 0;
            int var20 = 0;

            for (BlockPos var22 : (Iterable<BlockPos>)(Object)(var2)) {
               double var23 = var22.getX() - var36;
               double var25 = var22.getY() - var38;
               double var27 = var22.getZ() - var40;
               var43 += Math.sqrt(var23 * var23 + var25 * var25 + var27 * var27);
               if (class310.world != null) {
                  for (Direction var32 : Direction.values()) {
                     BlockState var33 = class310.world.getBlockState(var22.offset(var32));
                     if (!check4(var33)) {
                        var20++;
                        if (var33.isAir()) {
                           var19++;
                        }
                     }
                  }
               }
            }

            var43 /= var2.size();
            double var45 = var20 == 0 ? 0.0 : (double)var19 / var20;
            double var46 = 5.5 + this.val4.getObject().intValue() * 0.55;
            double var47 = 0.07 + this.val4.getObject().intValue() * 0.027;
            if (!(var43 > var46) && !(var45 > var47)) {
               int var48 = var2.size() + var3 - (int)Math.round(var43 * 2.0);
               return new Inner1(var4, var48);
            } else {
               return null;
            }
         }
      }
      return null;
   }

   private void run12() {
      if (class310.player != null && class310.world != null) {
         ChunkPos var1 = class310.player.getChunkPos();
         int var2 = this.val5.getObject() * 5;
         this.map.keySet().removeIf(var905 -> ChunkDebugModule.check3(var1, var2, var905));
         ArrayList var3 = new ArrayList();

         for (Chunk var5 : ActivityChunkFinderModuleUtil.getIterable()) {
            if (var5 instanceof WorldChunk var6) {
               ChunkPos var7 = var6.getPos();
               if (!check2(var7, var1, var2)) {
                  var3.add(var7);
               }
            }
         }

         var3.sort(Comparator.comparingLong(var905 -> ChunkDebugModule.longOf2(var1, (ChunkPos)var905)));

         for (ChunkPos var9 : (Iterable<ChunkPos>)(Object)(var3)) {
            this.run5(var9, false);
         }
      }
   }

   private void run5(ChunkPos var1, boolean var2) {
      if (class310.player != null) {
         int var3 = this.val5.getObject() * 5;
         if (!check2(var1, class310.player.getChunkPos(), var3) && this.set.add(var1.toLong())) {
            if (var2) {
               this.arrayDeque.addFirst(var1);
            } else {
               this.arrayDeque.addLast(var1);
            }
         }
      }
   }

   private void run13() {
      this.run15();
      if (class310.player != null && class310.world != null) {
         this.run12();
      }
   }

   private void run15() {
      this.arrayDeque.clear();
      this.set.clear();
      this.map.clear();
      this.intVal6 = 0;
   }

   private static boolean check(BlockState var0) {
      return var0.isOf(Blocks.BUDDING_AMETHYST) || check4(var0) || var0.isOf(Blocks.CALCITE) || var0.isOf(Blocks.SMOOTH_BASALT);
   }

   private static boolean check4(BlockState var0) {
      return var0.isOf(Blocks.AMETHYST_BLOCK)
         || var0.isOf(Blocks.SMALL_AMETHYST_BUD)
         || var0.isOf(Blocks.MEDIUM_AMETHYST_BUD)
         || var0.isOf(Blocks.LARGE_AMETHYST_BUD)
         || var0.isOf(Blocks.AMETHYST_CLUSTER);
   }

   private static boolean check2(ChunkPos var0, ChunkPos var1, int var2) {
      return Math.abs(var0.x - var1.x) > var2 || Math.abs(var0.z - var1.z) > var2;
   }

   private static long longOf(ChunkPos var0, ChunkPos var1) {
      long var2 = var0.x - var1.x;
      long var4 = var0.z - var1.z;
      return var2 * var2 + var4 * var4;
   }

   private static int intOf2(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   private static long longOf2(ChunkPos var0, ChunkPos var1) {
      return longOf(var1, var0);
   }

   private static boolean check3(ChunkPos var0, int var1, Long var2) {
      return check2(new ChunkPos(var2), var0, var1 + 3);
   }

   private void run11(Integer var1) {
      this.run13();
   }

   private void run8(Integer var1) {
      this.run13();
   }

   private void run9(Boolean var1) {
      this.run13();
   }

   final class Inner1 {
      private ChunkPos pos;
      private int score;

      Inner1(ChunkPos var1, int var2) {
         this.pos = var1;
         this.score = var2;
      }

      public ChunkPos pos() {
         return this.pos;
      }

      public int score() {
         return this.score;
      }
   }
}
