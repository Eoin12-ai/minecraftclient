package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import util.Utils_2;

public final class StorageEspModule extends Module {
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.valOf("Storage Types");
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Render");
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Performance");
   private final Setting<Boolean> val4 = this.valOf("chests", "Highlights normal and trapped chests.", true);
   private final Setting<Boolean> val5 = this.valOf("ender-chests", "Highlights ender chests.", true);
   private final Setting<Boolean> val6 = this.valOf("shulker-boxes", "Highlights every shulker box color.", true);
   private final Setting<Boolean> val7 = this.valOf("barrels", "Highlights barrels.", true);
   private final Setting<Boolean> val8 = this.valOf("hoppers", "Highlights hoppers.", true);
   private final Setting<Boolean> val9 = this.valOf("furnaces", "Highlights furnaces, blast furnaces and smokers.", true);
   private final Setting<Integer> val10 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("range")
            .valOf2("Maximum horizontal render distance in blocks.")
            .valOf3(192)
            .valOf4(16, 512)
            .valOf5(16, 512)
            .getVal()
      );
   private final Setting<Integer> val11 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("max-rendered")
            .valOf2("Maximum number of storage blocks rendered at once.")
            .valOf3(500)
            .valOf4(10, 2000)
            .valOf5(50, 1000)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val12 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("fill-color")
            .valOf2("Color and opacity of the box fill.")
            .valOf3(new ActivityChunkFinderModuleHelper4(161, 75, 255, 55))
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val13 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("outline-color")
            .valOf2("Color and opacity of the box outline.")
            .valOf3(new ActivityChunkFinderModuleHelper4(196, 128, 255, 235))
            .getVal()
      );
   private final Setting<Boolean> val14 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("tracers").valOf2("Draws lines from the camera to visible storage blocks.").valOf3(false).getVal()
      );
   private final Setting<Integer> val15 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-interval")
            .valOf2("Ticks between storage rescans. Raise it if the scan costs you frames.")
            .valOf3(10)
            .valOf4(5, 100)
            .valOf5(5, 100)
            .getVal()
      );
   private final List<BlockPos> list = new ArrayList<>();
   private int intVal;

   public StorageEspModule() {
      super(SwyzzyAddon.val2, "storage-esp", "Highlights selected storage blocks in loaded chunks.");
      Utils_2.run(this, "Storage ESP");
   }

   @Override
   public void run6() {
      this.intVal = 0;
      this.run12();
   }

   @Override
   public void run7() {
      this.list.clear();
   }

   @Override
   public String getString2() {
      return this.list.isEmpty() ? null : String.valueOf(this.list.size());
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (++this.intVal >= this.val15.getObject()) {
         this.intVal = 0;
         this.run12();
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData var1) {
      if (class310.player != null && !this.list.isEmpty()) {
         ActivityChunkFinderModuleEntry2 var2 = new ActivityChunkFinderModuleEntry2(this.val12.getObject());
         ActivityChunkFinderModuleEntry2 var3 = new ActivityChunkFinderModuleEntry2(this.val13.getObject());
         boolean var4 = this.val14.getObject();
         int var5 = Math.min(this.val11.getObject(), this.list.size());

         for (int var6 = 0; var6 < var5; var6++) {
            BlockPos var7 = this.list.get(var6);
            var1.val.run2(var7, var2, var3, RenderMode.Both, 0);
            if (var4) {
               var1.val
                  .run6(
                     BlockEspPlusModuleData.class243.x,
                     BlockEspPlusModuleData.class243.y,
                     BlockEspPlusModuleData.class243.z,
                     var7.getX() + 0.5,
                     var7.getY() + 0.5,
                     var7.getZ() + 0.5,
                     var3
                  );
            }
         }
      }
   }

   private Setting<Boolean> valOf(String var1, String var2, boolean var3) {
      return this.val_2.addSetting(new ActivityChunkFinderModuleHelper8().valOf(var1).valOf2(var2).valOf3(var3).getVal());
   }

   private void run12() {
      this.list.clear();
      if (class310.player != null && class310.world != null) {
         int var1 = this.val10.getObject();
         double var2 = (double)var1 * var1;
         double var4 = class310.player.getX();
         double var6 = class310.player.getZ();
         ChunkPos var8 = class310.player.getChunkPos();
         int var9 = var1 / 16 + 1;

         for (Chunk var11 : ActivityChunkFinderModuleUtil.getIterable()) {
            if (var11 instanceof WorldChunk var12) {
               ChunkPos var13 = var12.getPos();
               if (Math.abs(var13.x - var8.x) <= var9 && Math.abs(var13.z - var8.z) <= var9) {
                  for (BlockPos var15 : var12.getBlockEntityPositions()) {
                     double var16 = var15.getX() + 0.5 - var4;
                     double var18 = var15.getZ() + 0.5 - var6;
                     if (!(var16 * var16 + var18 * var18 > var2)) {
                        StorageEspModule.State var20 = this.valOf2(var12.getBlockState(var15));
                        if (var20 != null && this.check(var20)) {
                           this.list.add(var15.toImmutable());
                        }
                     }
                  }
               }
            }
         }

         this.list.sort(Comparator.comparingDouble(this::doubleOf));
      }
   }

   private double doubleOf(BlockPos var1) {
      return class310.player == null ? Double.MAX_VALUE : class310.player.squaredDistanceTo(var1.getX() + 0.5, var1.getY() + 0.5, var1.getZ() + 0.5);
   }

   private StorageEspModule.State valOf2(BlockState var1) {
      Block var2 = var1.getBlock();
      if (var2 instanceof ChestBlock) {
         return StorageEspModule.State.CHEST;
      } else if (var2 == Blocks.ENDER_CHEST) {
         return StorageEspModule.State.ENDER_CHEST;
      } else if (var2 instanceof ShulkerBoxBlock) {
         return StorageEspModule.State.SHULKER;
      } else if (var2 == Blocks.BARREL) {
         return StorageEspModule.State.BARREL;
      } else if (var2 == Blocks.HOPPER) {
         return StorageEspModule.State.HOPPER;
      } else {
         return var2 != Blocks.FURNACE && var2 != Blocks.BLAST_FURNACE && var2 != Blocks.SMOKER ? null : StorageEspModule.State.FURNACE;
      }
   }

   private boolean check(StorageEspModule.State var1) {
      return switch (var1) {
         case CHEST -> this.val4.getObject();
         case ENDER_CHEST -> this.val5.getObject();
         case SHULKER -> this.val6.getObject();
         case BARREL -> this.val7.getObject();
         case HOPPER -> this.val8.getObject();
         case FURNACE -> this.val9.getObject();
      };
   }

   enum State {
      CHEST,
      ENDER_CHEST,
      SHULKER,
      BARREL,
      HOPPER,
      FURNACE;

      private static StorageEspModule.State[] getValArray() {
         return new StorageEspModule.State[]{CHEST, ENDER_CHEST, SHULKER, BARREL, HOPPER, FURNACE};
      }
   }
}
