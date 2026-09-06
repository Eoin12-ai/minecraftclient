package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.LightType;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import util.Utils_2;

public final class WaterChunkFinderModule extends Module {
   private static final int intVal = 60;
   private static final int intVal2 = 8;
   private static final double doubleVal = 102400.0;
   private static final double doubleVal2 = 0.22;
   private static final ActivityChunkFinderModuleEntry2 val_2 = new ActivityChunkFinderModuleEntry2(180, 0, 255, 95);
   private static final ActivityChunkFinderModuleEntry2 val2_2 = new ActivityChunkFinderModuleEntry2(180, 0, 255, 255);
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val4 = this.val2.valOf("Render");
   private final Setting<Integer> val5 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-radius")
            .valOf2("Loaded chunk radius to scan around the player.")
            .valOf3(4)
            .valOf4(1, 10)
            .valOf5(1, 10)
            .valOf8(this::run9)
            .getVal()
      );
   private final Setting<Integer> val6 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("min-score")
            .valOf2("Minimum old-chunk score required to flag a chunk.")
            .valOf3(3)
            .valOf4(1, 40)
            .valOf5(1, 40)
            .valOf8(this::run8)
            .getVal()
      );
   private final Setting<Integer> val7 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("beehive-weight")
            .valOf2("Score added for each full beehive or bee nest.")
            .valOf3(3)
            .valOf4(1, 10)
            .valOf5(1, 10)
            .valOf8(this::run5)
            .getVal()
      );
   private final Setting<Integer> val8 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("amethyst-weight")
            .valOf2("Score added for each lit deep amethyst cluster or budding amethyst block.")
            .valOf3(1)
            .valOf4(1, 10)
            .valOf5(1, 10)
            .valOf8(this::run11)
            .getVal()
      );
   private final Setting<Boolean> val9 = this.val4
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("fill-chunk")
            .valOf2("Fills flagged chunks instead of only drawing their outline.")
            .valOf3(false)
            .getVal()
      );
   private final Set<Long> set = new HashSet<>();
   private final Set<Long> set2 = new HashSet<>();
   private final Map<ChunkPos, WaterChunkFinderModule.Inner1> map = new HashMap<>();
   private final ArrayDeque<ChunkPos> arrayDeque = new ArrayDeque<>();
   private Object object;
   private ChunkPos class1923;
   private int intVal3;

   public WaterChunkFinderModule() {
      super(SwyzzyAddon.val2, "water-chunk-finder", "Scores old grown chunks using full beehives and lit deep amethyst clusters.");
      Utils_2.run(this, "Chunk Detector");
   }

   @Override
   public void run6() {
      this.object = class310.world;
      this.run12();
   }

   @Override
   public void run7() {
      this.run13();
      this.object = null;
   }

   @Override
   public String getString2() {
      return this.map.isEmpty() ? null : String.valueOf(this.map.size());
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null) {
         if (class310.world != this.object) {
            this.object = class310.world;
            this.run12();
         }

         ChunkPos var2 = class310.player.getChunkPos();
         if (++this.intVal3 >= 60 || this.arrayDeque.isEmpty() && !var2.equals(this.class1923)) {
            this.run4(var2);
         }

         for (int var3 = 0; var3 < 8 && !this.arrayDeque.isEmpty(); var3++) {
            ChunkPos var4 = this.arrayDeque.removeFirst();
            this.set2.remove(var4.toLong());
            WorldChunk var5 = class310.world.getChunkManager().getWorldChunk(var4.x, var4.z);
            if (var5 != null) {
               this.set.add(var4.toLong());
               this.run3(var5, var4);
            }
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData var1) {
      if (class310.player != null && class310.world != null) {
         for (WaterChunkFinderModule.Inner1 var3 : this.map.values()) {
            int var4 = var3.pos().getCenterX();
            int var5 = var3.pos().getCenterZ();
            if (!(class310.player.squaredDistanceTo(var4, 64.0, var5) > 102400.0)) {
               double var6 = class310.world.getTopY(Type.WORLD_SURFACE, var4, var5) + 0.05;
               RenderMode var8 = ((Boolean)this.val9.getObject()) ? RenderMode.Both : RenderMode.Lines;
               var1.val
                  .run(
                     var3.pos().getStartX(),
                     var6,
                     var3.pos().getStartZ(),
                     var3.pos().getEndX() + 1,
                     var6 + 0.22,
                     var3.pos().getEndZ() + 1,
                     val_2,
                     val2_2,
                     var8,
                     0
                  );
            }
         }
      }
   }

   private void run3(WorldChunk var1, ChunkPos var2) {
      int var3 = 0;
      int var4 = 0;
      int var5 = var1.getBottomY();
      int var6 = Math.min(80, var1.getBottomY() + var1.getHeight() - 1);
      Mutable var7 = new Mutable();

      for (int var8 = 0; var8 < 16; var8++) {
         for (int var9 = 0; var9 < 16; var9++) {
            int var10 = var2.getStartX() + var8;
            int var11 = var2.getStartZ() + var9;

            for (int var12 = var5; var12 <= var6; var12++) {
               var7.set(var10, var12, var11);
               BlockState var13 = var1.getBlockState(var7);
               Block var14 = var13.getBlock();
               if ((var14 == Blocks.BEEHIVE || var14 == Blocks.BEE_NEST)
                  && var13.contains(Properties.HONEY_LEVEL)
                  && (Integer)var13.get(Properties.HONEY_LEVEL) >= 5) {
                  var3++;
               } else if (var12 < 0
                  && (var14 == Blocks.AMETHYST_CLUSTER || var14 == Blocks.BUDDING_AMETHYST)
                  && class310.world.getLightLevel(LightType.BLOCK, var7) > 0) {
                  var4++;
               }
            }
         }
      }

      int var15 = var3 * this.val7.getObject() + var4 * this.val8.getObject();
      if (var15 < this.val6.getObject()) {
         this.map.remove(var2);
      } else {
         String var16 = "Old Chunk | B:" + var3 + " A:" + var4 + " S:" + var15;
         WaterChunkFinderModule.Inner1 var17 = this.map.put(var2, new Inner1(var2, var16));
         if (var17 == null) {
            this.run4("%s at %d, %d", new Object[]{var16, var2.x, var2.z});
         }
      }
   }

   private void run4(ChunkPos var1) {
      this.intVal3 = 0;
      this.class1923 = var1;
      int var2 = this.val5.getObject();
      ArrayList var3 = new ArrayList();

      for (Chunk var5 : ActivityChunkFinderModuleUtil.getIterable()) {
         if (var5 instanceof WorldChunk var6) {
            ChunkPos var7 = var6.getPos();
            if (!check2(var7, var1, var2) && !this.set.contains(var7.toLong())) {
               var3.add(var6);
            }
         }
      }

      var3.sort(Comparator.comparingLong(var905 -> WaterChunkFinderModule.longOf2(this.class1923, (WorldChunk)var905)));
      this.arrayDeque.clear();
      this.set2.clear();

      for (WorldChunk var9 : (Iterable<WorldChunk>)(Object)(var3)) {
         ChunkPos var10 = var9.getPos();
         if (this.set2.add(var10.toLong())) {
            this.arrayDeque.addLast(var10);
         }
      }
   }

   private void run12() {
      this.run13();
      if (class310.player != null && class310.world != null) {
         this.run4(class310.player.getChunkPos());
      }
   }

   private void run13() {
      this.set.clear();
      this.set2.clear();
      this.map.clear();
      this.arrayDeque.clear();
      this.class1923 = null;
      this.intVal3 = 60;
   }

   private static boolean check2(ChunkPos var0, ChunkPos var1, int var2) {
      return Math.abs(var0.x - var1.x) > var2 || Math.abs(var0.z - var1.z) > var2;
   }

   private static long longOf(ChunkPos var0, ChunkPos var1) {
      long var2 = var0.x - var1.x;
      long var4 = var0.z - var1.z;
      return var2 * var2 + var4 * var4;
   }

   private static long longOf2(ChunkPos var0, WorldChunk var1) {
      return longOf(var1.getPos(), var0);
   }

   private void run11(Integer var1) {
      this.run12();
   }

   private void run5(Integer var1) {
      this.run12();
   }

   private void run8(Integer var1) {
      this.run12();
   }

   private void run9(Integer var1) {
      this.run12();
   }

   final class Inner1 {
      private ChunkPos pos;
      private String label;

      Inner1(ChunkPos var1, String var2) {
         this.pos = var1;
         this.label = var2;
      }

      public ChunkPos pos() {
         return this.pos;
      }

      public String label() {
         return this.label;
      }
   }
}
