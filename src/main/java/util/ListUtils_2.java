package util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

public final class ListUtils_2 {
   public static final int intVal = 7;
   public static final int intVal2 = 13;
   private static final int intVal3 = 0;
   private static final int intVal4 = 128;

   private ListUtils_2() {
   }

   public static List<BlockPos> listOf(int var0, int var1, long var2) {
      return listOf2(var0, var1, var2, 7, 13, true);
   }

   public static List<BlockPos> listOf2(int var0, int var1, long var2, int var4, int var5, boolean var6) {
      ChunkPos var7 = new ChunkPos(var0, var1);
      ChunkRandom var8 = new ChunkRandom((Random)(var6 ? new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()) : new CheckedRandom(RandomSeed.getSeed())));
      long var9 = var8.setPopulationSeed(var2, var7.getStartX(), var7.getStartZ());
      ArrayList var11 = new ArrayList();
      run(var11, var8, var9, var4, var5, var7, 3, ListUtils_2.State.TRAPEZOID, 8, 24);
      run(var11, var8, var9, var4, var5 + 1, var7, 2, ListUtils_2.State.UNIFORM, 8, 120);
      return var11;
   }

   private static void run(
      List<BlockPos> var0, ChunkRandom var1, long var2, int var4, int var5, ChunkPos var6, int var7, ListUtils_2.State var8, int var9, int var10
   ) {
      var1.setDecoratorSeed(var2, var5, var4);
      int var11 = var6.getStartX() + var1.nextInt(16);
      int var12 = var6.getStartZ() + var1.nextInt(16);
      int var13 = intOf(var1, var8, var9, var10);
      BlockPos var14 = new BlockPos(var11, var13, var12);
      int var15 = var1.nextInt(var7 + 1);

      for (int var16 = 0; var16 < var15; var16++) {
         int var17 = Math.min(var16, 7);
         int var18 = var14.getX() + intOf2(var1, var17);
         int var19 = var14.getY() + intOf2(var1, var17);
         int var20 = var14.getZ() + intOf2(var1, var17);
         if (var19 >= 0 && var19 < 128) {
            var0.add(new BlockPos(var18, var19, var20));
         }
      }
   }

   private static int intOf(Random var0, ListUtils_2.State var1, int var2, int var3) {
      int var4 = var3 - var2;
      if (var4 <= 0) {
         return var2;
      } else {
         return switch (var1) {
            case UNIFORM -> var2 + var0.nextInt(var4 + 1);
            case TRAPEZOID -> {
               int var5 = var4 / 2;
               int var6 = var4 - var5;
               yield var2 + var0.nextInt(var6 + 1) + var0.nextInt(var5 + 1);
            }
         };
      }
   }

   private static int intOf2(Random var0, int var1) {
      return Math.round((var0.nextFloat() - var0.nextFloat()) * var1);
   }

   enum State {
      UNIFORM,
      TRAPEZOID;

      private static ListUtils_2.State[] getValArray() {
         return new ListUtils_2.State[]{UNIFORM, TRAPEZOID};
      }
   }
}
