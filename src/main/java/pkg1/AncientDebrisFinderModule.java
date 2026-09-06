package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import util.ListUtils_2;

public final class AncientDebrisFinderModule extends Module {
   private static final int intVal = 4;
   private static final int intVal2 = 20;
   private static final int intVal3 = 12;
   private static final float floatVal = 0.6F;
   private static final int intVal4 = 40;
   private static final int[] intArray = new int[]{7, 6};
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Render");
   private final Setting<Boolean> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("scan-loaded")
            .valOf2("Box real ancient debris in loaded chunks. Exact, does not need a seed.")
            .valOf3(true)
            .valOf4(this::run5)
            .getVal()
      );
   private final Setting<Boolean> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("seed-prediction")
            .valOf2("Predict debris in the wider area from the seed. Only shows after it calibrates against real debris.")
            .valOf3(true)
            .valOf4(this::run13)
            .getVal()
      );
   private final Setting<String> val5 = this.val_2
      .addSetting(
         new AdminDetectorModuleHelper2()
            .valOf("seed")
            .valOf2("World seed used for prediction. Leave empty to use the singleplayer seed.")
            .valOf3("")
            .valOf4(this::run4)
            .getVal()
      );
   private final Setting<Integer> val6 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("chunk-radius")
            .valOf2("How many chunks around you to scan and predict.")
            .valOf3(6)
            .valOf4(1, 24)
            .valOf5(1, 12)
            .valOf8(this::run11)
            .getVal()
      );
   private final Setting<Integer> val7 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("max-boxes")
            .valOf2("Maximum number of boxes rendered at once.")
            .valOf3(1500)
            .valOf4(50, 20000)
            .valOf5(100, 5000)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val8 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("predicted-color")
            .valOf2("Outline color of seed-predicted debris.")
            .valOf3(new ActivityChunkFinderModuleHelper4(180, 100, 255, 255))
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val9 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("confirmed-color")
            .valOf2("Outline color of real debris found in loaded chunks.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 80, 210, 255))
            .getVal()
      );
   private final Setting<Boolean> val10 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("hide-predicted-when-confirmed")
            .valOf2("Do not draw a predicted box where a real debris box already sits.")
            .valOf3(true)
            .getVal()
      );
   private final Map<Long, List<BlockPos>> map = new HashMap<>();
   private final Map<Long, List<BlockPos>> map2 = new HashMap<>();
   private final Set<Long> set = new HashSet<>();
   private final Set<Long> set2 = new HashSet<>();
   private long longVal;
   private boolean bool_2;
   private boolean bool2;
   private long longVal2 = Long.MIN_VALUE;
   private int intVal5;
   private boolean bool3;
   private int intVal6;
   private int intVal7;
   private boolean bool4;
   private int intVal8;

   public AncientDebrisFinderModule() {
      super(SwyzzyAddon.val5, "ancient-debris-finder", "Boxes real and seed-predicted ancient debris in the nether.");
   }

   @Override
   public void run6() {
      this.run12();
   }

   @Override
   public void run7() {
      this.run12();
   }

   private void run12() {
      this.map.clear();
      this.map2.clear();
      this.set.clear();
      this.set2.clear();
      this.longVal2 = Long.MIN_VALUE;
      this.bool_2 = false;
      this.bool2 = false;
      this.bool3 = false;
      this.intVal8 = 0;
   }

   @Override
   public String getString2() {
      int var1 = this.set2.size();
      if (this.val4.getObject() && !this.bool3) {
         return var1 > 0 ? var1 + " real, calibrating" : null;
      } else {
         int var2 = 0;

         for (List var4 : this.map.values()) {
            var2 += var4.size();
         }

         if (var2 == 0 && var1 == 0) {
            return null;
         } else {
            return var1 > 0 ? var2 + "/" + var1 : String.valueOf(var2);
         }
      }
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null && class310.world.getRegistryKey() == World.NETHER) {
         this.intVal5++;
         if (this.val3_2.getObject()) {
            this.run18();
         } else if (!this.map2.isEmpty()) {
            this.run16();
         }

         if (!this.val4.getObject()) {
            if (!this.map.isEmpty()) {
               this.map.clear();
            }
         } else if (this.isEnabled2()) {
            if (!this.bool3 && this.intVal5 % 20 == 0) {
               this.run15();
            }

            if (this.bool3) {
               long var2 = class310.player.getChunkPos().toLong();
               if (var2 != this.longVal2) {
                  this.longVal2 = var2;
                  this.run3(class310.player.getChunkPos());
               }
            }
         }
      } else {
         if (!this.map.isEmpty() || !this.map2.isEmpty()) {
            this.run12();
         }
      }
   }

   private boolean isEnabled2() {
      if (this.bool_2) {
         return true;
      } else {
         String var1 = this.val5.getObject().trim();
         if (!var1.isEmpty()) {
            try {
               this.longVal = Long.parseLong(var1);
               this.bool_2 = true;
               return true;
            } catch (NumberFormatException var3) {
               if (!this.bool2) {
                  this.run4("'%s' is not a valid numeric seed.", new Object[]{var1});
                  this.bool2 = true;
               }

               return false;
            }
         } else if (class310.getServer() != null && class310.getServer().getOverworld() != null) {
            this.longVal = class310.getServer().getOverworld().getSeed();
            this.bool_2 = true;
            return true;
         } else {
            if (!this.bool2) {
               this.run4("No seed set. Enter the world seed in the module settings for prediction.", new Object[0]);
               this.bool2 = true;
            }

            return false;
         }
      }
   }

   private void run15() {
      if (this.set2.size() >= 12) {
         HashSet var1 = new HashSet();

         for (long var3 : this.map2.keySet()) {
            int var5 = ChunkPos.getPackedX(var3);
            int var6 = ChunkPos.getPackedZ(var3);

            for (int var7 = -1; var7 <= 1; var7++) {
               for (int var8 = -1; var8 <= 1; var8++) {
                  var1.add(ChunkPos.toLong(var5 + var7, var6 + var8));
               }
            }
         }

         int var24 = this.set2.size();
         int var25 = -1;
         int var4 = 0;
         int var26 = 0;
         boolean var27 = true;

         for (int var10 : intArray) {
            for (boolean var14 : new boolean[]{true, false}) {
               for (int var15 = 0; var15 < 40; var15++) {
                  HashSet var16 = new HashSet();

                  for (long var18 : (long[])(Object)var1) {
                     int var20 = ChunkPos.getPackedX(var18);
                     int var21 = ChunkPos.getPackedZ(var18);

                     for (BlockPos var23 : ListUtils_2.listOf2(var20, var21, this.longVal, var10, var15, var14)) {
                        var16.add(var23.asLong());
                     }
                  }

                  int var30 = 0;

                  for (long var19 : this.set2) {
                     if (var16.contains(var19)) {
                        var30++;
                     }
                  }

                  if (var30 > var25) {
                     var25 = var30;
                     var4 = var10;
                     var26 = var15;
                     var27 = var14;
                  }
               }
            }
         }

         if (var25 >= Math.ceil(var24 * 0.6F)) {
            this.bool3 = true;
            this.intVal6 = var4;
            this.intVal7 = var26;
            this.bool4 = var27;
            this.intVal8 = var24;
            this.longVal2 = Long.MIN_VALUE;
            this.run4("Calibrated: step %d index %d (%s), covers %d/%d real debris.", new Object[]{var4, var26, var27 ? "xoroshiro" : "legacy", var25, var24});
         }
      }
   }

   private void run3(ChunkPos var1) {
      this.map.clear();
      int var2 = this.val6.getObject();

      for (int var3 = -var2; var3 <= var2; var3++) {
         for (int var4 = -var2; var4 <= var2; var4++) {
            int var5 = var1.x + var3;
            int var6 = var1.z + var4;
            List var7 = ListUtils_2.listOf2(var5, var6, this.longVal, this.intVal6, this.intVal7, this.bool4);
            if (!var7.isEmpty()) {
               this.map.put(ChunkPos.toLong(var5, var6), var7);
            }
         }
      }
   }

   private void run16() {
      this.map2.clear();
      this.set2.clear();
      this.set.clear();
   }

   private void run18() {
      int var1 = this.val6.getObject();
      ChunkPos var2 = class310.player.getChunkPos();
      int var3 = 0;

      for (Chunk var5 : ActivityChunkFinderModuleUtil.getIterable()) {
         if (var3 >= 4) {
            break;
         }

         if (var5 instanceof WorldChunk var6) {
            ChunkPos var7 = var6.getPos();
            long var8 = var7.toLong();
            if (!this.set.contains(var8) && Math.abs(var7.x - var2.x) <= var1 && Math.abs(var7.z - var2.z) <= var1) {
               this.set.add(var8);
               var3++;
               List var10 = this.listOf(var6);
               if (!var10.isEmpty()) {
                  this.map2.put(var8, var10);

                  for (BlockPos var12 : (Iterable<BlockPos>)(Object)(var10)) {
                     this.set2.add(var12.asLong());
                  }
               }
            }
         }
      }
   }

   private List<BlockPos> listOf(WorldChunk var1) {
      ArrayList var2 = new ArrayList();
      ChunkPos var3 = var1.getPos();
      ChunkSection[] var4 = var1.getSectionArray();
      int var5 = var1.getBottomY();

      for (int var6 = 0; var6 < var4.length; var6++) {
         ChunkSection var7 = var4[var6];
         if (var7 != null && !var7.isEmpty() && var7.hasAny(AncientDebrisFinderModule::check)) {
            int var8 = var5 + var6 * 16;

            for (int var9 = 0; var9 < 16; var9++) {
               for (int var10 = 0; var10 < 16; var10++) {
                  for (int var11 = 0; var11 < 16; var11++) {
                     if (var7.getBlockState(var10, var9, var11).isOf(Blocks.ANCIENT_DEBRIS)) {
                        var2.add(new BlockPos(var3.getStartX() + var10, var8 + var9, var3.getStartZ() + var11));
                     }
                  }
               }
            }
         }
      }

      return var2;
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData var1) {
      ActivityChunkFinderModuleEntry2 var2 = this.val9.getObject();
      ActivityChunkFinderModuleEntry2 var3 = this.val8.getObject();
      int var4 = this.val7.getObject();
      boolean var5 = this.val10.getObject();
      int var6 = 0;

      for (List var8 : this.map2.values()) {
         for (BlockPos var10 : (Iterable<BlockPos>)(Object)(var8)) {
            if (var6++ >= var4) {
               return;
            }

            var1.val.run2(var10, var2, var2, RenderMode.Lines, 0);
         }
      }

      for (List var12 : this.map.values()) {
         for (BlockPos var14 : (Iterable<BlockPos>)(Object)(var12)) {
            if (!var5 || !this.set2.contains(var14.asLong())) {
               if (var6++ >= var4) {
                  return;
               }

               var1.val.run2(var14, var3, var3, RenderMode.Lines, 0);
            }
         }
      }
   }

   private static boolean check(BlockState var0) {
      return var0.isOf(Blocks.ANCIENT_DEBRIS);
   }

   private void run11(Integer var1) {
      this.run12();
   }

   private void run4(String var1) {
      this.run12();
   }

   private void run13(Boolean var1) {
      this.run12();
   }

   private void run5(Boolean var1) {
      this.run12();
   }
}
