package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class BaseEspModule extends Module {
   private static final int intVal = 2;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Render");
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Chunk Mark");
   private final Setting<Integer> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-radius")
            .valOf2("Loaded chunk radius to inspect for concentrated base blocks.")
            .valOf3(8)
            .valOf4(1, 24)
            .valOf5(1, 24)
            .valOf8(this::run11)
            .getVal()
      );
   private final Setting<Integer> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("min-blocks")
            .valOf2("Base indicator blocks needed in one chunk.")
            .valOf3(10)
            .valOf4(2, 128)
            .valOf5(2, 128)
            .getVal()
      );
   private final Setting<Boolean> val6 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("chat-alerts").valOf2("Reports newly detected base chunks in chat.").valOf3(true).getVal());
   private final Setting<Boolean> val7 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("sound-alerts").valOf2("Plays a sound for a newly detected base chunk.").valOf3(true).getVal());
   private final Setting<ActivityChunkFinderModuleHelper4> val8 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("box-color")
            .valOf2("Color of detected base bounds.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 72, 72, 95))
            .getVal()
      );
   private final Setting<Integer> val9 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("box-alpha")
            .valOf2("Fill opacity of detected base bounds.")
            .valOf3(75)
            .valOf4(0, 255)
            .valOf5(0, 255)
            .getVal()
      );
   private final Setting<Boolean> val10 = this.val3_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("chunk-mark").valOf2("Also marks the complete detected chunk.").valOf3(true).getVal());
   private final Setting<BaseEspModule.State> val11 = this.val3_2
      .addSetting(
         new BaseEspModuleHelper2<BaseEspModule.State>()
            .valOf("chunk-mark-mode")
            .valOf2("How detected chunks are marked.")
            .valOf3(BaseEspModule.State.Slab)
            .valOf5(this::getBoolean3)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val12 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("chunk-mark-color")
            .valOf2("Color of the full chunk marker.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 170, 40, 90))
            .valOf5(this::getBoolean2)
            .getVal()
      );
   private final Setting<Integer> val13 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("chunk-mark-opacity")
            .valOf2("Fill opacity of the chunk marker.")
            .valOf3(45)
            .valOf4(0, 255)
            .valOf5(0, 255)
            .valOf9(this::getBoolean)
            .getVal()
      );
   private final Map<Long, BaseEspModule.Inner2> map = new HashMap<>();
   private final Set<Long> set = new HashSet<>();
   private final List<ChunkPos> list = new ArrayList<>();
   private Object object;
   private ChunkPos class1923;
   private int intVal2;

   public BaseEspModule() {
      super(SwyzzyAddon.val2, "base-esp", "Finds concentrated storage and utility blocks that can indicate a player base.");
   }

   @Override
   public void run6() {
      this.run13();
      this.object = class310.world;
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
            this.run13();
            this.object = class310.world;
         }

         ChunkPos var2 = class310.player.getChunkPos();
         if (!var2.equals(this.class1923) || this.list.isEmpty()) {
            this.run8(var2);
         }

         for (int var3 = 0; var3 < 2 && !this.list.isEmpty(); var3++) {
            if (this.intVal2 >= this.list.size()) {
               this.intVal2 = 0;
               this.run9(var2);
            }

            this.run3(this.list.get(this.intVal2++));
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData var1) {
      if (class310.world != null) {
         ActivityChunkFinderModuleHelper4 var2 = this.val8.getObject();
         ActivityChunkFinderModuleEntry2 var3 = new ActivityChunkFinderModuleEntry2(var2.intVal, var2.intVal2, var2.intVal3, this.val9.getObject());
         ActivityChunkFinderModuleEntry2 var4 = new ActivityChunkFinderModuleEntry2(var2.intVal, var2.intVal2, var2.intVal3, Math.max(180, var2.intVal4));
         boolean var5 = this.val10.getObject();

         for (BaseEspModule.Inner2 var7 : this.map.values()) {
            var1.val.run(var7.minX, var7.minY, var7.minZ, var7.maxX + 1, var7.maxY + 1, var7.maxZ + 1, var3, var4, RenderMode.Both, 0);
            if (var5) {
               this.run4(var1, var7.pos);
            }
         }
      }
   }

   private void run3(ChunkPos var1) {
      WorldChunk var2 = class310.world.getChunkManager().getWorldChunk(var1.x, var1.z);
      if (var2 == null) {
         this.run10(var1.toLong());
      } else {
         int var3 = 0;
         int var4 = Integer.MAX_VALUE;
         int var5 = Integer.MAX_VALUE;
         int var6 = Integer.MAX_VALUE;
         int var7 = Integer.MIN_VALUE;
         int var8 = Integer.MIN_VALUE;
         int var9 = Integer.MIN_VALUE;
         ChunkSection[] var10 = var2.getSectionArray();
         int var11 = var2.getBottomY();

         for (int var12 = 0; var12 < var10.length; var12++) {
            ChunkSection var13 = var10[var12];
            if (var13 != null && !var13.isEmpty() && var13.hasAny(BaseEspModule::check)) {
               int var14 = var11 + var12 * 16;

               for (int var15 = 0; var15 < 16; var15++) {
                  for (int var16 = 0; var16 < 16; var16++) {
                     for (int var17 = 0; var17 < 16; var17++) {
                        if (check(var13.getBlockState(var16, var15, var17))) {
                           var3++;
                           int var18 = var1.getStartX() + var16;
                           int var19 = var14 + var15;
                           int var20 = var1.getStartZ() + var17;
                           var4 = Math.min(var4, var18);
                           var5 = Math.min(var5, var19);
                           var6 = Math.min(var6, var20);
                           var7 = Math.max(var7, var18);
                           var8 = Math.max(var8, var19);
                           var9 = Math.max(var9, var20);
                        }
                     }
                  }
               }
            }
         }

         long var21 = var1.toLong();
         if (var3 < this.val5.getObject()) {
            this.run10(var21);
         } else {
            this.map.put(var21, new Inner2(var1, var3, var4, var5, var6, var7, var8, var9));
            if (this.set.add(var21)) {
               this.run5(var1, var3);
            }
         }
      }
   }

   private void run4(ActivityChunkFinderModuleData var1, ChunkPos var2) {
      ActivityChunkFinderModuleHelper4 var3 = this.val12.getObject();
      ActivityChunkFinderModuleEntry2 var4 = new ActivityChunkFinderModuleEntry2(var3.intVal, var3.intVal2, var3.intVal3, this.val13.getObject());
      ActivityChunkFinderModuleEntry2 var5 = new ActivityChunkFinderModuleEntry2(var3.intVal, var3.intVal2, var3.intVal3, Math.max(160, var3.intVal4));
      int var6 = class310.world.getBottomY();
      int var7 = class310.world.getTopYInclusive() + 1;
      if (this.val11.getObject() == BaseEspModule.State.Slab || this.val11.getObject() == BaseEspModule.State.Both) {
         var1.val.run(var2.getStartX(), var6, var2.getStartZ(), var2.getEndX() + 1, var6 + 0.25, var2.getEndZ() + 1, var4, var5, RenderMode.Both, 0);
      }

      if (this.val11.getObject() == BaseEspModule.State.Pillar || this.val11.getObject() == BaseEspModule.State.Both) {
         var1.val.run(var2.getStartX(), var6, var2.getStartZ(), var2.getEndX() + 1, var7, var2.getEndZ() + 1, var4, var5, RenderMode.Lines, 0);
      }
   }

   private void run5(ChunkPos var1, int var2) {
      if (this.val6.getObject()) {
         this.run4("Base signal: %d blocks in chunk %d, %d.", new Object[]{var2, var1.x, var1.z});
      }

      if (this.val7.getObject() && class310.player != null) {
         class310.player.playSound((SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.8F, 0.75F);
      }

      if (class310.getToastManager() != null) {
         this.run4(var2 + " indicator blocks at " + var1.getCenterX() + ", " + var1.getCenterZ(), new Object[0]);
      }
   }

   private void run8(ChunkPos var1) {
      this.class1923 = var1;
      this.intVal2 = 0;
      this.list.clear();
      int var2 = this.val4.getObject();

      for (int var3 = -var2; var3 <= var2; var3++) {
         for (int var4 = -var2; var4 <= var2; var4++) {
            this.list.add(new ChunkPos(var1.x + var3, var1.z + var4));
         }
      }

      this.list.sort(Comparator.comparingInt(var905 -> BaseEspModule.intOf2(var1, var905)));
   }

   private void run9(ChunkPos var1) {
      int var2 = this.val4.getObject();
      this.map.keySet().removeIf(var905 -> BaseEspModule.check3(var1, var2, var905));
      this.set.retainAll(this.map.keySet());
   }

   private void run10(long var1) {
      this.map.remove(var1);
      this.set.remove(var1);
   }

   private void run12() {
      this.list.clear();
      this.intVal2 = 0;
   }

   private void run13() {
      this.map.clear();
      this.set.clear();
      this.list.clear();
      this.class1923 = null;
      this.intVal2 = 0;
   }

   private static boolean check(BlockState var0) {
      Block var1 = var0.getBlock();
      Identifier var2 = Registries.BLOCK.getId(var1);
      String var3 = var2 == null ? "" : var2.getPath();
      return var3.equals("chest")
         || var3.equals("trapped_chest")
         || var3.equals("barrel")
         || var3.endsWith("_shulker_box")
         || var3.equals("hopper")
         || var3.equals("dispenser")
         || var3.equals("dropper")
         || var3.equals("furnace")
         || var3.equals("blast_furnace")
         || var3.equals("smoker")
         || var3.equals("brewing_stand")
         || var3.equals("enchanting_table")
         || var3.equals("crafting_table")
         || var3.equals("ender_chest")
         || var3.equals("respawn_anchor");
   }

   private static int intOf(ChunkPos var0, ChunkPos var1) {
      int var2 = var0.x - var1.x;
      int var3 = var0.z - var1.z;
      return var2 * var2 + var3 * var3;
   }

   private static boolean check2(ChunkPos var0, ChunkPos var1, int var2) {
      return Math.abs(var0.x - var1.x) > var2 || Math.abs(var0.z - var1.z) > var2;
   }

   private static boolean check3(ChunkPos var0, int var1, Long var2) {
      return check2(new ChunkPos(var2), var0, var1);
   }

   private static int intOf2(ChunkPos var0, ChunkPos var1) {
      return intOf(var1, var0);
   }

   private Boolean getBoolean() {
      return this.val10.getObject();
   }

   private Boolean getBoolean2() {
      return this.val10.getObject();
   }

   private Boolean getBoolean3() {
      return this.val10.getObject();
   }

   private void run11(Integer var1) {
      this.run12();
   }

   final class Inner2 {
      final ChunkPos pos;
      private int count;
      final int minX;
      final int minY;
      final int minZ;
      final int maxX;
      final int maxY;
      final int maxZ;

      Inner2(ChunkPos var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
         this.pos = var1;
         this.count = var2;
         this.minX = var3;
         this.minY = var4;
         this.minZ = var5;
         this.maxX = var6;
         this.maxY = var7;
         this.maxZ = var8;
      }

      public ChunkPos pos() {
         return this.pos;
      }

      public int count() {
         return this.count;
      }

      public int minX() {
         return this.minX;
      }

      public int minY() {
         return this.minY;
      }

      public int minZ() {
         return this.minZ;
      }

      public int maxX() {
         return this.maxX;
      }

      public int maxY() {
         return this.maxY;
      }

      public int maxZ() {
         return this.maxZ;
      }
   }

   public enum State {
      Slab,
      Pillar,
      Both;

      private static BaseEspModule.State[] getValArray() {
         return new BaseEspModule.State[]{Slab, Pillar, Both};
      }
   }
}
