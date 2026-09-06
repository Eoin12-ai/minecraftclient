package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class ActivityChunkFinderModule extends Module {
   private static final int intVal = 8;
   private static final int intVal2 = 150;
   private static final double doubleVal = 0.06;
   private static final double doubleVal2 = 69.0;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Integer> val2_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("sensitivity")
            .valOf2("Amount of grass and fern growth required before a chunk is considered suspicious.")
            .valOf3(280)
            .valOf4(160, 700)
            .valOf5(160, 700)
            .valOf8(this::run11)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("color")
            .valOf2("Color of the predicted suspicious area.")
            .valOf3(new ActivityChunkFinderModuleHelper4(30, 230, 80, 255))
            .getVal()
      );
   private final Setting<Integer> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("transparency")
            .valOf2("Opacity of the predicted suspicious area.")
            .valOf3(140)
            .valOf4(0, 255)
            .valOf5(0, 255)
            .getVal()
      );
   private final Setting<Boolean> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("sound-alert").valOf2("Plays a sound when a new suspicious chunk is detected.").valOf3(true).getVal()
      );
   private final ArrayDeque<ChunkPos> arrayDeque = new ArrayDeque<>();
   private final Set<Long> set = new HashSet<>();
   private final Set<Long> set2 = new HashSet<>();
   private final Set<Long> set3 = new HashSet<>();
   private final Map<ChunkPos, ActivityChunkFinderModule.Inner1> map = new HashMap<>();
   private ClientWorld class638;
   private ChunkPos class1923;
   private int intVal3;

   public ActivityChunkFinderModule() {
      super(SwyzzyAddon.val2, "activity-chunk-finder", "Activity Chunk Finder");
   }

   @Override
   public void run6() {
      this.class638 = class310.world;
      this.run12();
   }

   @Override
   public void run7() {
      this.run13();
      this.class638 = null;
   }

   @Override
   public String getString2() {
      return this.map.isEmpty() ? null : String.valueOf(this.map.size());
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null) {
         if (class310.world != this.class638) {
            this.class638 = class310.world;
            this.run12();
         }

         ChunkPos var2 = class310.player.getChunkPos();
         if (++this.intVal3 >= 150 || this.arrayDeque.isEmpty() && !var2.equals(this.class1923)) {
            this.run4(var2);
         }

         for (int var3 = 0; var3 < 8 && !this.arrayDeque.isEmpty(); var3++) {
            ChunkPos var4 = this.arrayDeque.removeFirst();
            this.set.remove(var4.toLong());
            WorldChunk var5 = class310.world.getChunkManager().getWorldChunk(var4.x, var4.z);
            if (var5 != null) {
               this.set2.add(var4.toLong());
               this.run5(var5, var4);
            }
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData2 var1) {
      if (class310.player != null && class310.world != null) {
         ChunkPos var2 = new ChunkPos(var1.class2338);
         int var3 = this.getInt();
         ChunkPos var4 = class310.player.getChunkPos();
         if (Math.abs(var2.x - var4.x) <= var3 && Math.abs(var2.z - var4.z) <= var3) {
            this.set2.remove(var2.toLong());
            if (this.set.add(var2.toLong())) {
               this.arrayDeque.addFirst(var2);
            }
         }
      }
   }

   @InternalHelper5
   private void run3(ActivityChunkFinderModuleData var1) {
      if (class310.player != null && class310.world != null) {
         ActivityChunkFinderModuleHelper4 var2 = this.val3_2.getObject();
         int var3 = intOf2(this.val4.getObject(), 0, 255);
         ActivityChunkFinderModuleEntry2 var4 = new ActivityChunkFinderModuleEntry2(var2.intVal, var2.intVal2, var2.intVal3, var3);
         ActivityChunkFinderModuleEntry2 var5 = new ActivityChunkFinderModuleEntry2(var2.intVal, var2.intVal2, var2.intVal3, Math.min(255, var3 + 60));

         for (ActivityChunkFinderModule.Inner1 var7 : this.map.values()) {
            ChunkPos var8 = var7.chunk();
            var1.val.run(var8.getStartX(), 69.0, var8.getStartZ(), var8.getEndX() + 1, 69.06, var8.getEndZ() + 1, var4, var5, RenderMode.Both, 0);
         }
      }
   }

   private void run4(ChunkPos var1) {
      this.intVal3 = 0;
      this.class1923 = var1;
      int var2 = this.getInt();
      int var3 = var2 + 16;
      this.set2.removeIf(var905 -> ActivityChunkFinderModule.check7(var1, var3, var905));
      this.set3.removeIf(var905 -> ActivityChunkFinderModule.check6(var1, var3, var905));
      this.map.keySet().removeIf(var905 -> ActivityChunkFinderModule.check5(var1, var3, var905));
      ArrayList var4 = new ArrayList();

      for (Chunk var6 : ActivityChunkFinderModuleUtil.getIterable()) {
         if (var6 instanceof WorldChunk var7) {
            ChunkPos var8 = var7.getPos();
            if (!check4(var8, var1, var2) && !this.set2.contains(var8.toLong())) {
               var4.add(var7);
            }
         }
      }

      var4.sort(Comparator.comparingLong(var905 -> ActivityChunkFinderModule.longOf2(this.class1923, (WorldChunk)var905)));
      this.arrayDeque.clear();
      this.set.clear();

      for (WorldChunk var10 : (Iterable<WorldChunk>)(Object)(var4)) {
         ChunkPos var11 = var10.getPos();
         if (this.set.add(var11.toLong())) {
            this.arrayDeque.addLast(var11);
         }
      }
   }

   private void run5(WorldChunk var1, ChunkPos var2) {
      ArrayList var3 = new ArrayList();
      int var4 = 0;
      int var5 = Math.floorDiv(var1.getBottomY(), 16);
      int var6 = Math.floorDiv(var1.getBottomY() + var1.getHeight() - 1, 16);

      for (int var7 = var5; var7 <= var6; var7++) {
         ChunkSection var8 = var1.getSection(var1.sectionCoordToIndex(var7));
         if (!var8.isEmpty() && var8.hasAny(ActivityChunkFinderModule::check)) {
            for (int var9 = 0; var9 < 16; var9++) {
               int var10 = var7 * 16 + var9;

               for (int var11 = 0; var11 < 16; var11++) {
                  for (int var12 = 0; var12 < 16; var12++) {
                     BlockState var13 = var8.getBlockState(var11, var9, var12);
                     if (check(var13)) {
                        var4++;
                        var3.add(new BlockPos(var2.getStartX() + var11, var10, var2.getStartZ() + var12));
                     }
                  }
               }
            }
         }
      }

      int var14 = this.val2_2.getObject();
      int var15 = Math.max(140, (int)Math.round(var14 * 0.65));
      boolean var16 = var4 >= var14 || var4 >= var15 && check3(var3);
      if (!var16) {
         this.map.remove(var2);
         this.set3.remove(var2.toLong());
      } else {
         ActivityChunkFinderModule.Inner1 var17 = this.map.get(var2);
         if (var17 == null || var4 > var17.score()) {
            this.map.put(var2, new Inner1(var2, var4, System.currentTimeMillis()));
         }

         if (this.set3.add(var2.toLong()) && this.val5.getObject() && class310.player != null) {
            class310.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
         }
      }
   }

   private static boolean check(BlockState var0) {
      return check2(var0.getBlock());
   }

   private static boolean check2(Block var0) {
      return var0 == Blocks.TALL_GRASS || var0 == Blocks.SHORT_GRASS || var0 == Blocks.FERN || var0 == Blocks.LARGE_FERN;
   }

   private static boolean check3(List<BlockPos> var0) {
      if (var0.size() < 8) {
         return false;
      } else {
         HashSet var1 = new HashSet(var0);
         HashSet var2 = new HashSet();
         ArrayDeque var3 = new ArrayDeque();

         for (BlockPos var5 : var0) {
            if (var2.add(var5)) {
               var3.add(var5);
               int var6 = 0;

               while (!var3.isEmpty()) {
                  BlockPos var7 = (BlockPos)var3.removeLast();
                  if (++var6 >= 8) {
                     return true;
                  }

                  for (Direction var11 : Direction.values()) {
                     BlockPos var12 = var7.offset(var11);
                     if (var1.contains(var12) && var2.add(var12)) {
                        var3.addLast(var12);
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   private void run12() {
      this.run13();
      if (class310.player != null && class310.world != null) {
         this.run4(class310.player.getChunkPos());
      }
   }

   private void run13() {
      this.arrayDeque.clear();
      this.set.clear();
      this.set2.clear();
      this.set3.clear();
      this.map.clear();
      this.class1923 = null;
      this.intVal3 = 150;
   }

   private int getInt() {
      return class310.options == null ? 12 : Math.max(2, (Integer)class310.options.getViewDistance().getValue());
   }

   private static boolean check4(ChunkPos var0, ChunkPos var1, int var2) {
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

   private static long longOf2(ChunkPos var0, WorldChunk var1) {
      return longOf(var1.getPos(), var0);
   }

   private static boolean check5(ChunkPos var0, int var1, ChunkPos var2) {
      return check4(var2, var0, var1);
   }

   private static boolean check6(ChunkPos var0, int var1, Long var2) {
      return check4(new ChunkPos(var2), var0, var1);
   }

   private static boolean check7(ChunkPos var0, int var1, Long var2) {
      return check4(new ChunkPos(var2), var0, var1);
   }

   private void run11(Integer var1) {
      this.run12();
   }

   final class Inner1 {
      private ChunkPos chunk;
      private int score;
      private long firstSeenMs;

      Inner1(ChunkPos var1, int var2, long var3) {
         this.chunk = var1;
         this.score = var2;
         this.firstSeenMs = var3;
      }

      public ChunkPos chunk() {
         return this.chunk;
      }

      public int score() {
         return this.score;
      }

      public long firstSeenMs() {
         return this.firstSeenMs;
      }
   }
}
