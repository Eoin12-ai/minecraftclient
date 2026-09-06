package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

public final class BlockEspPlusModule extends Module {
   private static final int intVal = 1;
   private static final int intVal2 = 100;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Render");
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Block Notifier");
   private final Setting<List<Block>> val4 = this.val_2
      .addSetting(new BlockEspPlusModuleHelper2().valOf("blocks").valOf2("Blocks to highlight, trace and notify about.").valOf4(this::run14).getVal());
   private final Setting<Integer> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-radius")
            .valOf2("Loaded chunk radius to search around the player.")
            .valOf3(8)
            .valOf4(1, 32)
            .valOf5(1, 24)
            .valOf8(this::run11)
            .getVal()
      );
   private final Setting<Integer> val6 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("max-rendered-blocks")
            .valOf2("Maximum highlighted blocks rendered at once.")
            .valOf3(600)
            .valOf4(10, 3000)
            .valOf5(50, 1500)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val7 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("fill-color")
            .valOf2("Inner fill color of highlighted blocks.")
            .valOf3(new ActivityChunkFinderModuleHelper4(80, 160, 255, 45))
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val8 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("outline-color")
            .valOf2("Outer line color of highlighted blocks.")
            .valOf3(new ActivityChunkFinderModuleHelper4(80, 160, 255, 235))
            .getVal()
      );
   private final Setting<Boolean> val9 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("tracers").valOf2("Draw tracer lines to highlighted blocks.").valOf3(false).getVal());
   private final Setting<Integer> val10 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("max-tracers")
            .valOf2("Maximum tracer lines rendered at once.")
            .valOf3(80)
            .valOf4(1, 500)
            .valOf5(10, 200)
            .valOf9(this::getBoolean2)
            .getVal()
      );
   private final Setting<Boolean> val11 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("block-notifier")
            .valOf2("Notify when a selected block becomes visible to Block ESP+.")
            .valOf3(false)
            .getVal()
      );
   private final Setting<Boolean> val12 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("chat-notifier")
            .valOf2("Show selected block notifications in chat.")
            .valOf3(true)
            .valOf5(this::getBoolean)
            .getVal()
      );
   private final ArrayDeque<ChunkPos> arrayDeque = new ArrayDeque<>();
   private final Set<Long> set = new HashSet<>();
   private final Map<BlockPos, Block> map = new HashMap<>();
   private ClientWorld class638;
   private int intVal3;

   public BlockEspPlusModule() {
      super(SwyzzyAddon.val2, "block-esp-plus", "Highlights selected blocks with clean colors, tracers and notifications.");
   }

   @Override
   public void run6() {
      if (class310.world != this.class638) {
         this.map.clear();
      }

      this.class638 = class310.world;
      this.run10(false);
   }

   @Override
   public void run7() {
      this.arrayDeque.clear();
      this.set.clear();
   }

   @Override
   public String getString2() {
      return this.map.isEmpty() ? null : String.valueOf(this.map.size());
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null && !this.val4.getObject().isEmpty()) {
         if (++this.intVal3 >= 100 || this.arrayDeque.isEmpty()) {
            this.intVal3 = 0;
            this.run13();
         }

         for (int var2 = 0; var2 < 1 && !this.arrayDeque.isEmpty(); var2++) {
            ChunkPos var3 = this.arrayDeque.removeFirst();
            this.set.remove(var3.toLong());
            WorldChunk var4 = class310.world.getChunkManager().getWorldChunk(var3.x, var3.z);
            if (var4 == null) {
               this.run9(var3);
            } else {
               this.run4(var4);
            }
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData2 var1) {
      if (!this.val4.getObject().isEmpty()) {
         BlockPos var2 = var1.class2338.toImmutable();
         Block var3 = var1.class2680.getBlock();
         if (this.val4.getObject().contains(var3)) {
            this.run5(var2, var3);
         } else {
            this.map.remove(var2);
         }
      }
   }

   @InternalHelper5
   private void run3(ActivityChunkFinderModuleData var1) {
      ActivityChunkFinderModuleEntry2 var2 = this.val7.getObject();
      ActivityChunkFinderModuleEntry2 var3 = this.val8.getObject();
      int var4 = 0;
      int var5 = 0;
      int var6 = this.val6.getObject();
      boolean var7 = this.val9.getObject();
      int var8 = this.val10.getObject();

      for (BlockPos var10 : this.map.keySet()) {
         if (var4++ >= var6) {
            break;
         }

         var1.val.run2(var10, var2, var3, RenderMode.Both, 0);
         if (var7 && var5++ < var8) {
            var1.val
               .run6(
                  BlockEspPlusModuleData.class243.x,
                  BlockEspPlusModuleData.class243.y,
                  BlockEspPlusModuleData.class243.z,
                  var10.getX() + 0.5,
                  var10.getY() + 0.5,
                  var10.getZ() + 0.5,
                  var3
               );
         }
      }
   }

   private void run4(WorldChunk var1) {
      this.run9(var1.getPos());
      ChunkPos var2 = var1.getPos();
      HashSet var3 = new HashSet<>(this.val4.getObject());
      Mutable var4 = new Mutable();
      int var5 = var1.getBottomY();
      int var6 = var5 + var1.getHeight() - 1;

      for (int var7 = var5; var7 <= var6; var7++) {
         for (int var8 = var2.getStartX(); var8 <= var2.getEndX(); var8++) {
            for (int var9 = var2.getStartZ(); var9 <= var2.getEndZ(); var9++) {
               var4.set(var8, var7, var9);
               BlockState var10 = var1.getBlockState(var4);
               Block var11 = var10.getBlock();
               if (var3.contains(var11)) {
                  this.run5(var4.toImmutable(), var11);
               }
            }
         }
      }
   }

   private void run5(BlockPos var1, Block var2) {
      if (this.map.putIfAbsent(var1, var2) == null && this.val11.getObject()) {
         this.run8(var1, var2);
      }
   }

   private void run8(BlockPos var1, Block var2) {
      String var3 = new ItemStack(var2).getName().getString();
      String var4 = var3 + " at " + var1.getX() + ", " + var1.getY() + ", " + var1.getZ();
      this.run4(var4, new Object[0]);
      if (this.val12.getObject() && class310.player != null) {
         class310.player.sendMessage(Text.literal("Block ESP+: null"), false);
      }
   }

   private void run9(ChunkPos var1) {
      this.map.keySet().removeIf(var905 -> BlockEspPlusModule.check(var1, var905));
   }

   private void run12() {
      this.run10(true);
   }

   private void run10(boolean var1) {
      this.arrayDeque.clear();
      this.set.clear();
      if (var1) {
         this.map.clear();
      }

      this.intVal3 = 0;
      this.run13();
   }

   private void run13() {
      if (class310.player != null) {
         ChunkPos var1 = class310.player.getChunkPos();
         int var2 = this.val5.getObject();

         for (Chunk var4 : ActivityChunkFinderModuleUtil.getIterable()) {
            ChunkPos var5 = var4.getPos();
            if (Math.abs(var5.x - var1.x) <= var2 && Math.abs(var5.z - var1.z) <= var2 && this.set.add(var5.toLong())) {
               this.arrayDeque.addLast(var5);
            }
         }
      }
   }

   private static boolean check(ChunkPos var0, BlockPos var1) {
      return var1.getX() >> 4 == var0.x && var1.getZ() >> 4 == var0.z;
   }

   private Boolean getBoolean() {
      return this.val11.getObject();
   }

   private Boolean getBoolean2() {
      return this.val9.getObject();
   }

   private void run11(Integer var1) {
      this.run12();
   }

   private void run14(List var1) {
      this.run12();
   }
}
