package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Vector3d;
import util.Utils_2;

public final class EntityDebugModule extends Module {
   private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
   private static final int intVal = 24;
   private static final int intVal2 = 800;
   private static final int intVal3 = 20;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Rendering");
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Notifications");
   private final ActivityChunkFinderModuleEntry val4 = this.val2.valOf("Filters");
   private final ActivityChunkFinderModuleEntry val5 = this.val2.valOf("Performance");
   private final Setting<Integer> val6 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("range")
            .valOf2("Loaded chunk tracking radius.")
            .valOf3(8)
            .valOf4(1, 24)
            .valOf5(1, 24)
            .valOf8(this::run17)
            .getVal()
      );
   private final Setting<Boolean> val7 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("fill").valOf2("Renders a filled box around detected block entities.").valOf3(true).getVal());
   private final Setting<Boolean> val8 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("outline").valOf2("Renders an outline around detected block entities.").valOf3(true).getVal());
   private final Setting<Boolean> val9 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("tracers").valOf2("Draws tracers to detected block entities.").valOf3(false).getVal());
   private final Setting<Boolean> val10 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("name-tags").valOf2("Displays the block-entity type above each detection.").valOf3(true).getVal()
      );
   private final Setting<Boolean> val11 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("distance-text")
            .valOf2("Adds the distance to each name tag.")
            .valOf3(true)
            .valOf5(this::getBoolean5)
            .getVal()
      );
   private final Setting<Integer> val12 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper6().valOf("alpha").valOf2("ESP opacity.").valOf3(78).valOf4(10, 255).valOf5(10, 255).getVal());
   private final Setting<Boolean> val13 = this.val3_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("chat-logging").valOf2("Reports newly detected block entities in chat.").valOf3(false).getVal());
   private final Setting<List<Block>> val14 = this.val4
      .addSetting(
         new BlockEspPlusModuleHelper2()
            .valOf("block-entity-selector")
            .valOf2("Block-entity types tracked by Entity Debug.")
            .valOf3(List.of(getclass2248Array()))
            .getVal()
      );
   private final Setting<Integer> val15 = this.val5
      .addSetting(
         new ActivityChunkFinderModuleHelper6().valOf("scan-budget").valOf2("Chunks processed per tick.").valOf3(4).valOf4(1, 16).valOf5(1, 16).getVal()
      );
   private final Setting<Integer> val16 = this.val5
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-delay")
            .valOf2("Ticks between loaded chunk refreshes.")
            .valOf3(20)
            .valOf4(5, 100)
            .valOf5(5, 100)
            .getVal()
      );
   private final Setting<Integer> val17 = this.val5
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("cache-lifetime")
            .valOf2("Seconds before stale entries expire.")
            .valOf3(120)
            .valOf4(10, 300)
            .valOf5(10, 300)
            .getVal()
      );
   private final ArrayDeque<ChunkPos> arrayDeque = new ArrayDeque<>();
   private final Set<Long> set = new HashSet<>();
   private final Map<BlockPos, EntityDebugModule.Inner1> map = new HashMap<>();
   private final Map<Long, Set<BlockPos>> map2 = new HashMap<>();
   private final Vector3d vector3d = new Vector3d();
   private Object object;
   private String string_2;
   private int intVal4;
   private Set<Block> set2 = Set.of();
   private List<Block> list;
   private boolean bool_2;

   public EntityDebugModule() {
      super(SwyzzyAddon.val2, "entity-debug", "Entity Debug");
      Utils_2.run(this, "Entity Debug");
   }

   @Override
   public void run6() {
      this.object = class310.world;
      this.run19();
      this.run12();
   }

   @Override
   public void run7() {
      this.run19();
      this.object = null;
   }

   @Override
   public String getString2() {
      return this.map.isEmpty() ? null : String.valueOf(this.map.size());
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.world != null && class310.player != null) {
         if (class310.world != this.object) {
            this.object = class310.world;
            this.run19();
            this.run12();
         }

         this.intVal4++;
         if (this.intVal4 == 1 || this.intVal4 % this.val16.getObject() == 0) {
            this.run13();
         }

         this.run5(this.intVal4 == 1 ? 24 : this.val15.getObject());
         if (this.intVal4 % 20 == 0) {
            this.run16();
         }
      } else {
         this.run19();
         this.object = null;
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData2 var1) {
      if (class310.world != null && check(null, var1.class2680)) {
         this.run8(new ChunkPos(var1.class2338));
      }
   }

   @InternalHelper5
   private void run3(ActivityChunkFinderModuleData var1) {
      if (class310.player != null && !this.map.isEmpty()) {
         boolean var2 = this.val7.getObject() || this.val8.getObject();
         RenderMode var3 = this.val7.getObject() && ((Boolean)this.val8.getObject()) ? RenderMode.Both : (((Boolean)this.val7.getObject()) ? RenderMode.Sides : RenderMode.Lines);
         ActivityChunkFinderModuleEntry2 var4 = new ActivityChunkFinderModuleEntry2(221, 246, 255, this.val12.getObject());
         ActivityChunkFinderModuleEntry2 var5 = new ActivityChunkFinderModuleEntry2(221, 246, 255, Math.max(this.val12.getObject(), 220));
         double var6 = this.val6.getObject().intValue() * 16.0 + 24.0;
         double var8 = var6 * var6;
         boolean var10 = this.val9.getObject();
         int var11 = 0;

         for (EntityDebugModule.Inner1 var13 : this.map.values()) {
            if (var11 >= 800) {
               break;
            }

            if (!(this.doubleOf(var13.class2338) > var8)) {
               var11++;
               if (var2) {
                  var1.val.run2(var13.class2338, var4, var5, var3, 0);
               }

               if (var10) {
                  var1.val
                     .run6(
                        BlockEspPlusModuleData.class243.x,
                        BlockEspPlusModuleData.class243.y,
                        BlockEspPlusModuleData.class243.z,
                        var13.class2338.getX() + 0.5,
                        var13.class2338.getY() + 0.5,
                        var13.class2338.getZ() + 0.5,
                        var5
                     );
               }
            }
         }
      }
   }

   @InternalHelper5
   private void run4(AdminDetectorModuleData var1) {
   }

   private void run12() {
      if (class310.world != null && class310.player != null) {
         this.string_2 = class310.world.getRegistryKey().getValue().toString();
         this.intVal4 = 0;
         this.run13();
         this.run5(24);
      }
   }

   private void run13() {
      if (class310.player != null) {
         ChunkPos var1 = class310.player.getChunkPos();
         ArrayList var2 = new ArrayList();

         for (Chunk var4 : ActivityChunkFinderModuleUtil.getIterable()) {
            if (var4 instanceof WorldChunk var5 && !check3(var5.getPos(), var1, this.val6.getObject())) {
               var2.add(var5);
            }
         }

         var2.sort(Comparator.comparingLong(var905 -> EntityDebugModule.longOf2(var1, (WorldChunk)var905)));

         for (WorldChunk var7 : (Iterable<WorldChunk>)(Object)(var2)) {
            ChunkPos var8 = var7.getPos();
            if (this.set.add(var8.toLong())) {
               this.arrayDeque.addLast(var8);
            }
         }
      }
   }

   private void run5(int var1) {
      if (class310.world != null) {
         for (int var2 = 0; var2 < var1 && !this.arrayDeque.isEmpty(); var2++) {
            ChunkPos var3 = this.arrayDeque.removeFirst();
            this.set.remove(var3.toLong());
            WorldChunk var4 = class310.world.getChunkManager().getWorldChunk(var3.x, var3.z);
            if (var4 != null) {
               this.run9(var4);
            }
         }
      }
   }

   private void run8(ChunkPos var1) {
      this.arrayDeque.remove(var1);
      this.set.add(var1.toLong());
      this.arrayDeque.addFirst(var1);
   }

   private void run9(WorldChunk var1) {
      if (this.string_2 != null && class310.world != null) {
         long var2 = System.currentTimeMillis();
         Set var4 = this.getSet();
         ChunkPos var5 = var1.getPos();
         HashSet var6 = new HashSet();

         for (BlockPos var8 : var1.getBlockEntityPositions()) {
            BlockPos var9 = var8.toImmutable();
            BlockEntity var10 = class310.world.getBlockEntity(var9);
            if (var10 != null) {
               Block var11 = var10.getCachedState().getBlock();
               if (var4.contains(var11)) {
                  var6.add(var9);
                  this.run14(var9, var5, var11, var2);
               }
            }
         }

         if (this.bool_2) {
            this.run11(var1, var6, var2);
         }

         this.run10(var5, var6);
      }
   }

   private Set<Block> getSet() {
      List var1 = this.val14.getObject();
      if (var1 != this.list) {
         this.list = var1;
         this.set2 = Set.copyOf(var1);
         this.bool_2 = this.set2.contains(Blocks.RESPAWN_ANCHOR);
      }

      return this.set2;
   }

   private void run10(ChunkPos var1, Set<BlockPos> var2) {
      Set var3 = this.map2.get(var1.toLong());
      if (var3 != null) {
         var3.removeIf(var905 -> this.check7(var3, (BlockPos)var905));
         if (var3.isEmpty()) {
            this.map2.remove(var1.toLong());
         }
      }
   }

   private void run11(WorldChunk var1, Set<BlockPos> var2, long var3) {
      ChunkSection[] var5 = var1.getSectionArray();
      int var6 = var1.getBottomY();

      for (int var7 = 0; var7 < var5.length; var7++) {
         ChunkSection var8 = var5[var7];
         if (var8 != null && !var8.isEmpty() && var8.hasAny(EntityDebugModule::check6)) {
            int var9 = var6 + var7 * 16;

            for (int var10 = 0; var10 < 16; var10++) {
               for (int var11 = 0; var11 < 16; var11++) {
                  for (int var12 = 0; var12 < 16; var12++) {
                     if (var8.getBlockState(var11, var10, var12).isOf(Blocks.RESPAWN_ANCHOR)) {
                        BlockPos var13 = new BlockPos(var1.getPos().getStartX() + var11, var9 + var10, var1.getPos().getStartZ() + var12);
                        var2.add(var13);
                        this.run14(var13, var1.getPos(), Blocks.RESPAWN_ANCHOR, var3);
                     }
                  }
               }
            }
         }
      }
   }

   private void run14(BlockPos var1, ChunkPos var2, Block var3, long var4) {
      EntityDebugModule.Inner1 var6 = this.map.get(var1);
      if (var6 != null) {
         var6.longVal2 = var4;
         var6.intVal++;
      } else {
         EntityDebugModule.Inner1 var7 = new Inner1(var1, var2, var3, stringOf(var3), var4);
         this.map.put(var1, var7);
         this.map2.computeIfAbsent(var2.toLong(), EntityDebugModule::setOf).add(var1);
         if (this.val13.getObject()) {
            this.run15(var7);
         }
      }
   }

   private void run15(EntityDebugModule.Inner1 var1) {
      if (class310.player != null) {
         double var2 = Math.sqrt(this.doubleOf(var1.class2338));
         String var4 = addSetting(this.string_2);
         class310.player
            .sendMessage(
               Text.literal(
                  String.format(
                     Locale.ROOT,
                     "[Entity Debug] %s | XYZ: %d %d %d | Chunk: %d, %d | Distance: %.0fm | %s | %s",
                     var1.string,
                     var1.class2338.getX(),
                     var1.class2338.getY(),
                     var1.class2338.getZ(),
                     var1.class1923.x,
                     var1.class1923.z,
                     var2,
                     var4,
                     dateTimeFormatter.format(LocalTime.now())
                  )
               ),
               false
            );
      }
   }

   private void run16() {
      if (class310.player != null && class310.world != null) {
         ChunkPos var1 = class310.player.getChunkPos();
         int var2 = this.val6.getObject();
         long var3 = System.currentTimeMillis() - this.val17.getObject().intValue() * 1000L;
         this.map2.entrySet().removeIf(var905 -> this.check4(var1, var2, var3, var905));
      }
   }

   private void run18() {
      if (this.isEnabled()) {
         this.arrayDeque.clear();
         this.set.clear();
         this.map.clear();
         this.map2.clear();
         this.intVal4 = 0;
         this.run13();
      }
   }

   private void run19() {
      this.arrayDeque.clear();
      this.set.clear();
      this.map.clear();
      this.map2.clear();
      this.string_2 = null;
      this.intVal4 = 0;
   }

   private double doubleOf(BlockPos var1) {
      return class310.player == null ? Double.MAX_VALUE : class310.player.squaredDistanceTo(var1.getX() + 0.5, var1.getY() + 0.5, var1.getZ() + 0.5);
   }

   private static boolean check(BlockState var0, BlockState var1) {
      Block var2 = var0 == null ? Blocks.AIR : var0.getBlock();
      Block var3 = var1 == null ? Blocks.AIR : var1.getBlock();
      return check2(var2) || check2(var3);
   }

   private static boolean check2(Block var0) {
      return var0 instanceof BlockEntityProvider || var0 == Blocks.RESPAWN_ANCHOR;
   }

   private static Block[] getclass2248Array() {
      ArrayList var0 = new ArrayList();
      Registries.BLOCK.stream().filter(EntityDebugModule::check2).forEach(var0::add);
      if (!var0.contains(Blocks.RESPAWN_ANCHOR)) {
         var0.add(Blocks.RESPAWN_ANCHOR);
      }

      return ((java.util.List<Block>)(Object)var0).toArray(EntityDebugModule::class2248ArrayOf);
   }

   private static String stringOf(Block var0) {
      Identifier var1 = Registries.BLOCK.getId(var0);
      String var2 = var1 == null ? "block_entity" : var1.getPath();
      StringBuilder var3 = new StringBuilder(var2.length());
      boolean var4 = true;

      for (int var5 = 0; var5 < var2.length(); var5++) {
         char var6 = var2.charAt(var5);
         if (var6 == '_') {
            var3.append(' ');
            var4 = true;
         } else {
            var3.append(var4 ? Character.toUpperCase(var6) : var6);
            var4 = false;
         }
      }

      return var3.toString();
   }

   private static String addSetting(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         int var1 = var0.indexOf(58);
         String var2 = var1 < 0 ? var0 : var0.substring(var1 + 1);
         return var2.isEmpty() ? "Unknown" : Character.toUpperCase(var2.charAt(0)) + var2.substring(1).replace('_', ' ');
      } else {
         return "Unknown";
      }
   }

   private static boolean check3(ChunkPos var0, ChunkPos var1, int var2) {
      return Math.abs(var0.x - var1.x) > var2 || Math.abs(var0.z - var1.z) > var2;
   }

   private static long longOf(ChunkPos var0, ChunkPos var1) {
      long var2 = var0.x - var1.x;
      long var4 = var0.z - var1.z;
      return var2 * var2 + var4 * var4;
   }

   private static Block[] class2248ArrayOf(int var0) {
      return new Block[var0];
   }

   private boolean check4(ChunkPos var1, int var2, long var3, Map.Entry<Long, Set<BlockPos>> var5) {
      ChunkPos var6 = new ChunkPos(var5.getKey());
      if (!check3(var6, var1, var2) && class310.world.getChunkManager().getWorldChunk(var6.x, var6.z) != null) {
         var5.getValue().removeIf(a0x -> this.check5(var5.getKey(), a0x));
         return var5.getValue().isEmpty();
      } else {
         for (BlockPos var8 : var5.getValue()) {
            this.map.remove(var8);
         }

         return true;
      }
   }

   private boolean check5(long var1, BlockPos var3) {
      EntityDebugModule.Inner1 var4 = this.map.get(var3);
      if (var4 != null && var4.longVal2 >= var1) {
         return false;
      } else {
         this.map.remove(var3);
         return true;
      }
   }

   private static Set setOf(Long var0) {
      return new HashSet<>();
   }

   private static boolean check6(BlockState var0) {
      return var0.isOf(Blocks.RESPAWN_ANCHOR);
   }

   private boolean check7(Set var1, BlockPos var2) {
      if (var1.contains(var2)) {
         return false;
      } else {
         this.map.remove(var2);
         return true;
      }
   }

   private static long longOf2(ChunkPos var0, WorldChunk var1) {
      return longOf(var1.getPos(), var0);
   }

   private Boolean getBoolean5() {
      return this.val10.getObject();
   }

   private void run17(Integer var1) {
      this.run18();
   }

   final class Inner1 {
      final BlockPos class2338;
      final ChunkPos class1923;
      private Block class2248;
      final String string;
      private long longVal;
      long longVal2;
      int intVal = 1;

      Inner1(BlockPos var1, ChunkPos var2, Block var3, String var4, long var5) {
         this.class2338 = var1;
         this.class1923 = var2;
         this.class2248 = var3;
         this.string = var4;
         this.longVal = var5;
         this.longVal2 = var5;
      }
   }
}
