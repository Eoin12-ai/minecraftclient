package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Vector3d;
import util.Utils_2;

public final class SpawnerNotifierModule extends Module {
   private static final int intVal = 20;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Render");
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Alerts");
   private final Setting<Integer> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("range")
            .valOf2("Loaded chunk radius to scan for mob spawners.")
            .valOf3(8)
            .valOf4(1, 24)
            .valOf5(1, 24)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val5 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("pillar-color")
            .valOf2("Color of the vertical spawner marker.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 170, 40, 80))
            .getVal()
      );
   private final Setting<Integer> val6 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("pillar-opacity")
            .valOf2("Fill opacity of the vertical marker.")
            .valOf3(35)
            .valOf4(0, 255)
            .valOf5(0, 255)
            .getVal()
      );
   private final Setting<Boolean> val7 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("spawner-esp").valOf2("Draws a box around each detected spawner.").valOf3(true).getVal());
   private final Setting<ActivityChunkFinderModuleHelper4> val8 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("spawner-color")
            .valOf2("Color of the spawner box.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 205, 80, 255))
            .valOf5(this::getBoolean2)
            .getVal()
      );
   private final Setting<Integer> val9 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("spawner-alpha")
            .valOf2("Fill opacity of the spawner box.")
            .valOf3(70)
            .valOf4(0, 255)
            .valOf5(0, 255)
            .valOf9(this::getBoolean)
            .getVal()
      );
   private final Setting<Double> val10 = this.val2_2
      .addSetting(new AutoTotemModuleHelper2().valOf("text-scale").valOf2("Scale of the spawner label.").valOf3(1.0).valOf4(0.4, 2.0).valOf5(0.4, 2.0).getVal());
   private final Setting<Boolean> val11 = this.val3_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("chat-alerts").valOf2("Announces newly detected spawners in chat.").valOf3(true).getVal());
   private final Setting<Boolean> val12 = this.val3_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("sound-alerts").valOf2("Plays a notification sound for new spawners.").valOf3(true).getVal());
   private final Setting<Boolean> val13 = this.val3_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("toast-alerts").valOf2("Shows a toast for newly detected spawners.").valOf3(true).getVal());
   private final Setting<ActivityChunkFinderModuleHelper4> val14 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("line-color")
            .valOf2("Line color for spawner box outline.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 255, 255, 255))
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val15 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("side-color")
            .valOf2("Side color for spawner box sides.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 255, 255, 255))
            .getVal()
      );
   private final Setting<RenderMode> val16 = this.val2_2
      .addSetting(new BaseEspModuleHelper2<RenderMode>().valOf("shape-mode").valOf2("How the spawner box is rendered.").valOf3(RenderMode.Both).getVal());
   private final Map<BlockPos, String> map = new HashMap<>();
   private final Set<String> set = new HashSet<>();
   private final Vector3d vector3d = new Vector3d();
   private int intVal2;

   public SpawnerNotifierModule() {
      super(SwyzzyAddon.val2, "spawner-notifier", "Scans loaded chunks for mob spawners and alerts you.");
      Utils_2.run(this, "Spawner Finder");
   }

   @Override
   public void run6() {
      this.run13();
      this.run12();
   }

   @Override
   public void run7() {
      this.run13();
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null) {
         this.intVal2++;
         if (this.intVal2 % 20 == 0) {
            this.run12();
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData var1) {
      if (class310.player != null && !this.map.isEmpty()) {
         ActivityChunkFinderModuleEntry2 var2 = new ActivityChunkFinderModuleEntry2(this.val5.getObject());
         var2.intVal4 = this.val6.getObject();
         ActivityChunkFinderModuleEntry2 var3 = new ActivityChunkFinderModuleEntry2(this.val5.getObject());
         ActivityChunkFinderModuleEntry2 var4 = new ActivityChunkFinderModuleEntry2(this.val8.getObject());
         var4.intVal4 = this.val9.getObject();
         ActivityChunkFinderModuleEntry2 var5 = new ActivityChunkFinderModuleEntry2(this.val8.getObject());

         for (BlockPos var7 : this.map.keySet()) {
            var1.val
               .run(
                  var7.getX() + 0.35,
                  var7.getY(),
                  var7.getZ() + 0.35,
                  var7.getX() + 0.65,
                  var7.getY() + 1.0,
                  var7.getZ() + 0.65,
                  var2,
                  var3,
                  RenderMode.Both,
                  0
               );
            if (this.val7.getObject()) {
               var1.val.run2(var7, var4, var5, this.val16.getObject(), 0);
            }
         }
      }
   }

   @InternalHelper5
   private void run3(AdminDetectorModuleData var1) {
   }

   private void run12() {
      if (class310.player != null && class310.world != null) {
         ChunkPos var1 = class310.player.getChunkPos();
         HashMap var2 = new HashMap();

         for (Chunk var4 : ActivityChunkFinderModuleUtil.getIterable()) {
            if (var4 instanceof WorldChunk var5 && !this.check2(var5.getPos(), var1, this.val4.getObject())) {
               for (BlockEntity var7 : var5.getBlockEntities().values()) {
                  if (var7 instanceof MobSpawnerBlockEntity var8) {
                     String var9 = "Unknown";
                     Entity var10 = var8.getLogic().getRenderedEntity(class310.world, var8.getPos());
                     if (var10 != null) {
                        var9 = var10.getType().getName().getString();
                     } else if (class310.world != null) {
                        BlockState var11 = class310.world.getBlockState(var8.getPos());
                        if (var11.isOf(Blocks.SPAWNER)) {
                           var9 = "Empty";
                        }
                     }

                     BlockPos var13 = var8.getPos();
                     var2.put(var13, var9);
                     String var12 = var13.getX() + "," + var13.getY() + "," + var13.getZ();
                     if (!this.set.contains(var12)) {
                        this.set.add(var12);
                        this.run4(var13, var9);
                     }
                  }
               }
            }
         }

         this.map.clear();
         this.map.putAll(var2);
      }
   }

   private boolean check2(ChunkPos var1, ChunkPos var2, int var3) {
      return Math.abs(var1.x - var2.x) > var3 || Math.abs(var1.z - var2.z) > var3;
   }

   private void run4(BlockPos var1, String var2) {
      if (this.val11.getObject()) {
         this.run4("%s spawner at %d, %d, %d.", new Object[]{var2, var1.getX(), var1.getY(), var1.getZ()});
      }

      if (this.val12.getObject() && class310.player != null) {
         class310.player.playSound((SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.9F, 1.15F);
      }

      if (this.val13.getObject()) {
         this.run4(var2 + " spawner at " + var1.getX() + ", " + var1.getY() + ", " + var1.getZ(), new Object[0]);
      }
   }

   private void run13() {
      this.map.clear();
      this.set.clear();
      this.intVal2 = 0;
   }

   private Boolean getBoolean() {
      return this.val7.getObject();
   }

   private Boolean getBoolean2() {
      return this.val7.getObject();
   }
}
