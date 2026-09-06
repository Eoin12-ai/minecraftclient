package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

public final class RedstoneActivityModule extends Module {
   private static final Set<String> set = Set.of(
      "repeater",
      "comparator",
      "redstone_wire",
      "redstone_torch",
      "redstone_wall_torch",
      "redstone_block",
      "redstone_lamp",
      "observer",
      "piston",
      "sticky_piston",
      "moving_piston",
      "dispenser",
      "dropper",
      "hopper",
      "target",
      "tripwire_hook",
      "note_block"
   );
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Render");
   private final Setting<Boolean> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("only-redstone")
            .valOf2("Count only redstone components. Off counts every block update, which also catches farms with no redstone in them.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Boolean> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("only-below")
            .valOf2("Ignore updates above the height below. Cuts out surface farms and leaves whatever is running underground.")
            .valOf3(false)
            .getVal()
      );
   private final Setting<Integer> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("y-level")
            .valOf2("Only count updates at or below this height. 0 is roughly where deepslate starts.")
            .valOf3(0)
            .valOf5(-64, 320)
            .valOf9(this.val4::getObject)
            .getVal()
      );
   private final Setting<Integer> val6 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("threshold")
            .valOf2("Updates needed inside the window before a chunk counts as active.")
            .valOf3(20)
            .valOf6(1)
            .valOf5(1, 200)
            .getVal()
      );
   private final Setting<Integer> val7 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("window")
            .valOf2("Length of the counting window, in milliseconds.")
            .valOf3(3000)
            .valOf6(200)
            .valOf5(200, 20000)
            .getVal()
      );
   private final Setting<Integer> val8 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("linger")
            .valOf2("How long a chunk stays highlighted after it goes quiet, in milliseconds.")
            .valOf3(8000)
            .valOf6(500)
            .valOf5(500, 60000)
            .getVal()
      );
   private final Setting<Boolean> val9 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("notify")
            .valOf2("Print a line the first time a chunk goes active. Only you see it.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Boolean> val10 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("play-sound").valOf2("Play a sound with the message.").valOf3(false).getVal());
   private final Setting<Boolean> val11 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("render").valOf2("Draw a box around active chunks.").valOf3(true).getVal());
   private final Setting<Integer> val12 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("box-height")
            .valOf2("How tall the box is.")
            .valOf3(4)
            .valOf6(1)
            .valOf5(1, 64)
            .valOf9(this.val11::getObject)
            .getVal()
      );
   private final Setting<Boolean> val13 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("draw-at-your-height")
            .valOf2("Draw the box at your own height instead of at the redstone.")
            .valOf3(true)
            .valOf5(this.val11::getObject)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val14 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("side-color")
            .valOf2("Fill colour.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 40, 40, 40))
            .valOf5(this.val11::getObject)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val15 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("line-color")
            .valOf2("Outline colour.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 40, 40, 255))
            .valOf5(this.val11::getObject)
            .getVal()
      );
   private final Setting<Boolean> val16 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("tracers")
            .valOf2("Draw a line from your crosshair to each active chunk, in the outline colour.")
            .valOf3(false)
            .getVal()
      );
   private final Map<Long, RedstoneActivityModule.Inner1> map = new ConcurrentHashMap<>();

   public RedstoneActivityModule() {
      super(SwyzzyAddon.val2, "redstone-activity", "Flags chunks that are producing a lot of block updates, which means something is running there.");
   }

   @Override
   public void run6() {
      this.map.clear();
   }

   @Override
   public void run7() {
      this.map.clear();
   }

   @Override
   public String getString2() {
      return this.map.isEmpty() ? null : String.valueOf(this.map.size());
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleData2 var1) {
      if (!this.val3_2.getObject() || check(var1.class2680.getBlock())) {
         int var2 = var1.class2338.getX();
         int var3 = var1.class2338.getY();
         int var4 = var1.class2338.getZ();
         if (!this.val4.getObject() || var3 <= this.val5.getObject()) {
            long var5 = ChunkPos.toLong(var2 >> 4, var4 >> 4);
            long var7 = System.currentTimeMillis();
            RedstoneActivityModule.Inner1 var9 = this.map.computeIfAbsent(var5, a0x -> RedstoneActivityModule.valOf(a0x, a0x));
            if (var7 - var9.longVal > this.val7.getObject().intValue()) {
               var9.longVal = var7;
               var9.intVal = 0;
               var9.bool = false;
            }

            var9.intVal++;
            var9.intVal2 = var3;
            if (var9.intVal >= this.val6.getObject()) {
               var9.longVal2 = var7;
               if (!var9.bool) {
                  var9.bool = true;
                  if (this.val9.getObject()) {
                     this.run4(
                        "Redstone activity at chunk %d, %d (block %d, %d) - %d updates.",
                        new Object[]{var2 >> 4, var4 >> 4, var2 >> 4 << 4, var4 >> 4 << 4, var9.intVal}
                     );
                  }

                  if (this.val10.getObject() && class310.player != null) {
                     class310.player.playSound((SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0F, 1.2F);
                  }
               }
            }
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData var1) {
      if (this.val11.getObject() || this.val16.getObject()) {
         if (class310.player != null) {
            long var2 = System.currentTimeMillis();
            HashSet var4 = new HashSet();

            for (Entry var6 : this.map.entrySet()) {
               RedstoneActivityModule.Inner1 var7 = (RedstoneActivityModule.Inner1)var6.getValue();
               if (var7.longVal2 != 0L) {
                  if (var2 - var7.longVal2 > this.val8.getObject().intValue()) {
                     var4.add((Long)var6.getKey());
                  } else {
                     ChunkPos var8 = new ChunkPos((Long)var6.getKey());
                     double var9 = var8.x << 4;
                     double var11 = var8.z << 4;
                     double var13 = ((Boolean)this.val13.getObject()) ? class310.player.getY() : var7.intVal2;
                     if (this.val11.getObject()) {
                        var1.val
                           .run(
                              var9,
                              var13,
                              var11,
                              var9 + 16.0,
                              var13 + this.val12.getObject().intValue(),
                              var11 + 16.0,
                              this.val14.getObject(),
                              this.val15.getObject(),
                              RenderMode.Both,
                              0
                           );
                     }

                     if (this.val16.getObject()) {
                        var1.val
                           .run6(
                              BlockEspPlusModuleData.class243.x,
                              BlockEspPlusModuleData.class243.y,
                              BlockEspPlusModuleData.class243.z,
                              var9 + 8.0,
                              var13,
                              var11 + 8.0,
                              this.val15.getObject()
                           );
                     }
                  }
               }
            }

            for (Long var16 : (Iterable<Long>)(Object)(var4)) {
               this.map.remove(var16);
            }
         }
      }
   }

   private static boolean check(Block var0) {
      Identifier var1 = Registries.BLOCK.getId(var0);
      return var1 != null && set.contains(var1.getPath());
   }

   private static RedstoneActivityModule.Inner1 valOf(long var0, Long var2) {
      RedstoneActivityModule.Inner1 var3 = new RedstoneActivityModule.Inner1();
      var3.longVal = var0;
      return var3;
   }

   final static class Inner1 {
      int intVal;
      long longVal;
      long longVal2;
      int intVal2;
      boolean bool;
   }
}
