package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.chunk.WorldChunk;

public final class BaseChunksModule extends Module {
   private static final int intVal = 100;
   private static final double doubleVal = 122500.0;
   private static final double doubleVal2 = 0.22;
   private static final ActivityChunkFinderModuleEntry2 val_2 = new ActivityChunkFinderModuleEntry2(255, 40, 40, 90);
   private static final ActivityChunkFinderModuleEntry2 val2_2 = new ActivityChunkFinderModuleEntry2(255, 40, 40, 255);
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val4 = this.val2.valOf("Render");
   private final Setting<Integer> val5 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-radius")
            .valOf2("Loaded chunk radius to inspect around the player.")
            .valOf3(4)
            .valOf4(1, 12)
            .valOf5(1, 12)
            .getVal()
      );
   private final Setting<Integer> val6 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("min-inhabited-hours")
            .valOf2("Minimum accumulated inhabited time needed to flag a chunk.")
            .valOf3(12)
            .valOf4(1, 72)
            .valOf5(1, 72)
            .getVal()
      );
   private final Setting<Boolean> val7 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("require-nearby-player")
            .valOf2("Only scans while another player is visible nearby.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Integer> val8 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("player-radius")
            .valOf2("Chunk radius in which another rendered player must be present.")
            .valOf3(6)
            .valOf4(1, 24)
            .valOf5(1, 24)
            .valOf9(this::getBoolean2)
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
   private final Map<ChunkPos, BaseChunksModule.Inner1> map = new HashMap<>();
   private final Set<Long> set = new HashSet<>();
   private Object object;
   private int intVal2;

   public BaseChunksModule() {
      super(SwyzzyAddon.val2, "base-chunks", "Flags highly inhabited chunks, optionally only while another player is nearby.");
   }

   @Override
   public void run6() {
      this.object = class310.world;
      this.run15();
      this.intVal2 = 100;
   }

   @Override
   public void run7() {
      this.run15();
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
            this.run15();
            this.intVal2 = 100;
         }

         if (++this.intVal2 >= 100) {
            this.intVal2 = 0;
            if (this.val7.getObject() && !this.isEnabled2()) {
               this.map.clear();
               this.set.clear();
            } else {
               this.run12();
            }
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData var1) {
      if (class310.player != null && class310.world != null) {
         RenderMode var2 = ((Boolean)this.val9.getObject()) ? RenderMode.Both : RenderMode.Lines;

         for (BaseChunksModule.Inner1 var4 : this.map.values()) {
            ChunkPos var5 = var4.pos();
            int var6 = var5.getCenterX();
            int var7 = var5.getCenterZ();
            if (!(class310.player.squaredDistanceTo(var6, 64.0, var7) > 122500.0)) {
               double var8 = class310.world.getTopY(Type.WORLD_SURFACE, var6, var7) + 0.05;
               var1.val.run(var5.getStartX(), var8, var5.getStartZ(), var5.getEndX() + 1, var8 + 0.22, var5.getEndZ() + 1, val_2, val2_2, var2, 0);
            }
         }
      }
   }

   private void run12() {
      ChunkPos var1 = class310.player.getChunkPos();
      int var2 = this.val5.getObject();
      long var3 = this.val6.getObject().intValue() * 60L * 60L * 20L;
      HashMap var5 = new HashMap();
      HashSet var6 = new HashSet();

      for (int var7 = var1.x - var2; var7 <= var1.x + var2; var7++) {
         for (int var8 = var1.z - var2; var8 <= var1.z + var2; var8++) {
            WorldChunk var9 = class310.world.getChunkManager().getWorldChunk(var7, var8);
            if (var9 != null && var9.getInhabitedTime() >= var3) {
               ChunkPos var10 = new ChunkPos(var7, var8);
               double var11 = var9.getInhabitedTime() / 72000.0;
               BaseChunksModule.Inner1 var13 = new Inner1(var10, var11);
               var5.put(var10, var13);
               var6.add(var10.toLong());
               if (this.set.add(var10.toLong())) {
                  this.run4("Base? %.1fh loaded at %d, %d", new Object[]{var11, var7, var8});
               }
            }
         }
      }

      this.map.clear();
      this.map.putAll(var5);
      this.set.retainAll(var6);
   }

   private boolean isEnabled2() {
      ChunkPos var1 = class310.player.getChunkPos();
      int var2 = this.val8.getObject();

      for (PlayerEntity var4 : class310.world.getPlayers()) {
         if (var4 != class310.player) {
            ChunkPos var5 = var4.getChunkPos();
            if (Math.abs(var5.x - var1.x) <= var2 && Math.abs(var5.z - var1.z) <= var2) {
               return true;
            }
         }
      }

      return false;
   }

   private void run15() {
      this.map.clear();
      this.set.clear();
      this.intVal2 = 0;
   }

   private Boolean getBoolean2() {
      return this.val7.getObject();
   }

   final class Inner1 {
      private ChunkPos pos;
      private double inhabitedHours;

      Inner1(ChunkPos var1, double var2) {
         this.pos = var1;
         this.inhabitedHours = var2;
      }

      public ChunkPos pos() {
         return this.pos;
      }

      public double inhabitedHours() {
         return this.inhabitedHours;
      }
   }
}
