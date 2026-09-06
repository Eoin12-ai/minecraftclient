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
import java.util.Map.Entry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class TuffChunkFinderModule extends Module {
   private static final int intVal = 24;
   private static final long longVal = 6000000L;
   private static final int intVal2 = 20;
   private static final int intVal3 = 1;
   private static final int intVal4 = 120;
   private static final double doubleVal = 0.15;
   private static final ActivityChunkFinderModuleEntry2 val_2 = new ActivityChunkFinderModuleEntry2(150, 0, 255, 255);
   private static final int intVal5 = 2;
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Render");
   private final Setting<Integer> val4 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("sensitivity")
            .valOf2("Amethyst blocks one geode needs before it counts as a geode. Higher is stricter.")
            .valOf3(3)
            .valOf5(1, 16)
            .valOf8(this::run17)
            .getVal()
      );
   private final Setting<Integer> val5 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("required-nearby-geodes")
            .valOf2("Counted geodes that must sit together within nearby-range before a chunk is marked. Never below two.")
            .valOf3(2)
            .valOf5(2, 8)
            .valOf8(this::run20)
            .getVal()
      );
   private final Setting<Integer> val6 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("min-y")
            .valOf2("Lowest Y the scan looks at. Sections without geode material are skipped anyway, so a wide range costs little.")
            .valOf3(-64)
            .valOf5(-64, 320)
            .valOf8(this::run15)
            .getVal()
      );
   private final Setting<Integer> val7 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("max-y")
            .valOf2("Highest Y the scan looks at. Keep this above the surface to catch shallow geodes.")
            .valOf3(320)
            .valOf5(-64, 320)
            .valOf8(this::run14)
            .getVal()
      );
   private final Setting<Boolean> val8 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("budding-only")
            .valOf2(
               "Only counts geodes confirmed by budding amethyst, so mined out ones drop out. Uses Pedro Amethyst as the source while that module is enabled."
            )
            .valOf3(false)
            .valOf4(this::run13)
            .getVal()
      );
   private final Setting<Integer> val9 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-radius")
            .valOf2("Loaded chunk radius to check around the player.")
            .valOf3(24)
            .valOf5(1, 32)
            .valOf8(this::run10)
            .getVal()
      );
   private final Setting<Integer> val10 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("nearby-range")
            .valOf2("Maximum chunk distance between separate geodes that belong to one suspicious area.")
            .valOf3(8)
            .valOf5(1, 24)
            .valOf8(this::run11)
            .getVal()
      );
   private final Setting<Boolean> val11 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("notifications")
            .valOf2("Shows a toast when a new qualifying amethyst cluster is found.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Boolean> val12 = this.val3_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("tracers").valOf2("Draws a tracer to each marked chunk.").valOf3(true).getVal());
   private final Setting<Boolean> val13 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("marker-at-surface")
            .valOf2("Draws the chunk marker at the terrain surface above the geode, so it stays out of the ground on hills and mountains.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Boolean> val14 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("marker-at-geode-height")
            .valOf2("Puts the marker at the depth of the geode. Off draws it at y-level instead, where it stays visible from the surface.")
            .valOf3(false)
            .valOf5(this::getBoolean5)
            .getVal()
      );
   private final Setting<Integer> val15 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("y-level")
            .valOf2("Y coordinate the chunk markers are drawn at.")
            .valOf3(63)
            .valOf5(-64, 320)
            .valOf9(this::getBoolean4)
            .getVal()
      );
   private final Setting<Integer> val16 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("height-offset")
            .valOf2("Vertical offset applied on top of the marker height.")
            .valOf3(0)
            .valOf4(-32, 32)
            .valOf5(-12, 12)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val17 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("side-color")
            .valOf2("Fill color of suspicious chunks. Transparency is controlled by Opacity.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 45, 45, 45))
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val18 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("line-color")
            .valOf2("Outline and tracer color. Transparency is controlled by Opacity.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 45, 45, 230))
            .getVal()
      );
   private final Setting<Integer> val19 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("opacity")
            .valOf2("Render strength of marked chunks. Low values look faint, high values look vivid and solid.")
            .valOf3(100)
            .valOf4(0, 255)
            .valOf5(0, 255)
            .getVal()
      );
   private final Setting<Boolean> val20 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("budding-esp")
            .valOf2("Boxes every single budding amethyst block. Ignored while Pedro Amethyst is enabled, since that module already draws them.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Integer> val21 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("budding-alpha")
            .valOf2("Opacity of those block boxes.")
            .valOf3(125)
            .valOf4(1, 255)
            .valOf5(1, 255)
            .getVal()
      );
   private final Setting<Boolean> val22 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("budding-tracers")
            .valOf2("Draws a tracer to every budding amethyst block instead of only to the areas.")
            .valOf3(false)
            .getVal()
      );
   private final Setting<RenderMode> val23 = this.val3_2
      .addSetting(new BaseEspModuleHelper2<RenderMode>().valOf("shape-mode").valOf2("How suspicious chunks are rendered.").valOf3(RenderMode.Both).getVal());
   private final Setting<Boolean> val24 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("cluster-markers")
            .valOf2("Marks the exact amethyst cluster positions with smaller, darker boxes above the chunk marker.")
            .valOf3(true)
            .getVal()
      );
   private final ArrayDeque<ChunkPos> arrayDeque = new ArrayDeque<>();
   private final Set<Long> set = new HashSet<>();
   private final Set<Long> set2 = new HashSet<>();
   private final Map<Long, Integer> map = new HashMap<>();
   private final Map<ChunkPos, TuffChunkFinderModule.Inner1> map2 = new HashMap<>();
   private final Set<String> set3 = new HashSet<>();
   private List<TuffChunkFinderModule.Inner4> list = List.of();
   private boolean bool_2;
   private int intVal6;
   private int intVal7;
   private int intVal8;
   private ClientWorld class638;

   public TuffChunkFinderModule() {
      super(SwyzzyAddon.val2, "tuff-chunk-finder", "Finds groups of untouched budding amethyst geodes.");
   }

   @Override
   public void run6() {
      this.class638 = class310.world;
      this.run18();
   }

   @Override
   public void run7() {
      this.arrayDeque.clear();
      this.set.clear();
      this.set2.clear();
      this.map.clear();
      this.map2.clear();
      this.set3.clear();
      this.list = List.of();
      this.bool_2 = false;
      this.class638 = null;
   }

   @Override
   public String getString2() {
      return this.list.isEmpty() ? null : String.valueOf(this.list.size());
   }

   @InternalHelper5
   private void run4(AmethystChunkFinderModuleHelper var1) {
      if (var1.chunk() != null) {
         this.run9(var1.chunk().getPos(), true, true);
      }
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null) {
         this.intVal8++;
         if (class310.world != this.class638) {
            this.class638 = class310.world;
            this.run18();
         }

         if (++this.intVal6 >= 20) {
            this.intVal6 = 0;
            this.run19();
         }

         boolean var2 = false;
         long var3 = System.nanoTime() + 6000000L;

         for (int var5 = 0; var5 < 24 && !this.arrayDeque.isEmpty() && (var5 <= 0 || System.nanoTime() < var3); var5++) {
            var2 = true;
            ChunkPos var6 = this.arrayDeque.removeFirst();
            this.set.remove(var6.toLong());
            WorldChunk var7 = class310.world.getChunkManager().getWorldChunk(var6.x, var6.z);
            if (var7 == null) {
               this.set2.remove(var6.toLong());
               this.bool_2 = this.bool_2 | this.map2.remove(var6) != null;
            } else {
               TuffChunkFinderModule.Inner1 var8 = this.valOf(var7);
               this.set2.add(var6.toLong());
               this.map.put(var6.toLong(), this.intVal8);
               if (var8.geodeBlocks().isEmpty()) {
                  this.bool_2 = this.bool_2 | this.map2.remove(var6) != null;
               } else {
                  TuffChunkFinderModule.Inner1 var9 = this.map2.put(var6, var8);
                  this.bool_2 = this.bool_2 | !var8.equals(var9);
               }
            }
         }

         if (var2 && this.bool_2 && (++this.intVal7 >= 1 || this.arrayDeque.isEmpty())) {
            this.run12();
            this.bool_2 = false;
            this.intVal7 = 0;
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData2 var1) {
      this.run9(new ChunkPos(var1.class2338), true, true);
   }

   @InternalHelper5
   private void run3(ActivityChunkFinderModuleData var1) {
      this.run5(var1);
      ActivityChunkFinderModuleHelper4 var2 = this.val17.getObject();
      ActivityChunkFinderModuleHelper4 var3 = this.val18.getObject();
      int var4 = this.val19.getObject();
      ActivityChunkFinderModuleEntry2 var5 = new ActivityChunkFinderModuleEntry2(var2.intVal, var2.intVal2, var2.intVal3, var4);
      ActivityChunkFinderModuleEntry2 var6 = new ActivityChunkFinderModuleEntry2(var3.intVal, var3.intVal2, var3.intVal3, var4);
      ActivityChunkFinderModuleEntry2 var7 = valOf2(var2, var4);
      ActivityChunkFinderModuleEntry2 var8 = valOf2(var3, var4);

      for (TuffChunkFinderModule.Inner4 var10 : this.list) {
         double var11 = this.doubleOf(var10) + this.val16.getObject().intValue();
         var1.val
            .run(
               var10.minChunkX() * 16,
               var11,
               var10.minChunkZ() * 16,
               var10.maxChunkX() * 16 + 16,
               var11 + 0.15,
               var10.maxChunkZ() * 16 + 16,
               var5,
               var6,
               this.val23.getObject(),
               0
            );
         if (this.val24.getObject()) {
            double var13 = var11 + 0.15;

            for (TuffChunkFinderModule.Inner2 var16 : var10.clusters()) {
               var1.val.run(var16.minX(), var13, var16.minZ(), var16.maxX() + 1, var13 + 0.15, var16.maxZ() + 1, var7, var8, this.val23.getObject(), 0);
            }
         }

         if (this.val12.getObject()) {
            var1.val
               .run6(
                  BlockEspPlusModuleData.class243.x,
                  BlockEspPlusModuleData.class243.y,
                  BlockEspPlusModuleData.class243.z,
                  (var10.minChunkX() + var10.maxChunkX() + 1) * 8.0,
                  var11,
                  (var10.minChunkZ() + var10.maxChunkZ() + 1) * 8.0,
                  var6
               );
         }
      }
   }

   private void run5(ActivityChunkFinderModuleData var1) {
      if (this.val20.getObject() || this.val22.getObject()) {
         PedroAmethystModule var2 = FakeRankModuleHelper.getVal().valOf(PedroAmethystModule.class);
         if (var2 == null || !var2.isEnabled()) {
            ActivityChunkFinderModuleEntry2 var3 = new ActivityChunkFinderModuleEntry2(val_2.intVal, val_2.intVal2, val_2.intVal3, this.val21.getObject());

            for (TuffChunkFinderModule.Inner1 var5 : this.map2.values()) {
               for (BlockPos var7 : var5.buddingBlocks()) {
                  if (this.val20.getObject()) {
                     var1.val.run2(var7, var3, val_2, RenderMode.Both, 0);
                  }

                  if (this.val22.getObject()) {
                     var1.val
                        .run6(
                           BlockEspPlusModuleData.class243.x,
                           BlockEspPlusModuleData.class243.y,
                           BlockEspPlusModuleData.class243.z,
                           var7.getX() + 0.5,
                           var7.getY() + 0.5,
                           var7.getZ() + 0.5,
                           val_2
                        );
                  }
               }
            }
         }
      }
   }

   private void run12() {
      ArrayList var1 = new ArrayList<>(this.getList());
      ArrayList var2 = new ArrayList();

      while (!var1.isEmpty()) {
         TuffChunkFinderModule.Inner3 var3 = (TuffChunkFinderModule.Inner3)var1.removeLast();
         ArrayDeque var4 = new ArrayDeque();
         var4.add(var3);
         int var5 = var3.minChunkX();
         int var6 = var3.maxChunkX();
         int var7 = var3.minChunkZ();
         int var8 = var3.maxChunkZ();
         double var9 = var3.getDouble();
         int var11 = 1;
         ArrayList var12 = new ArrayList();
         var12.add(var3.getVal());

         while (!var4.isEmpty()) {
            TuffChunkFinderModule.Inner3 var13 = (TuffChunkFinderModule.Inner3)var4.removeFirst();

            for (int var14 = var1.size() - 1; var14 >= 0; var14--) {
               TuffChunkFinderModule.Inner3 var15 = (TuffChunkFinderModule.Inner3)var1.get(var14);
               if (this.check3(var13, var15)) {
                  var1.remove(var14);
                  var4.addLast(var15);
                  var5 = Math.min(var5, var15.minChunkX());
                  var6 = Math.max(var6, var15.maxChunkX());
                  var7 = Math.min(var7, var15.minChunkZ());
                  var8 = Math.max(var8, var15.maxChunkZ());
                  var9 += var15.getDouble();
                  var11++;
                  var12.add(var15.getVal());
               }
            }
         }

         if (var11 >= Math.max(2, this.val5.getObject())) {
            var2.add(new Inner4(var5, var6, var7, var8, var9 / var11, var11, List.copyOf(var12)));
         }
      }

      this.list = List.copyOf(var2);
      this.run16();
   }

   private List<TuffChunkFinderModule.Inner3> getList() {
      HashSet var1 = new HashSet();
      HashSet var2 = new HashSet();
      HashSet var3 = new HashSet();
      HashSet var4 = new HashSet();

      for (Entry var6 : this.map2.entrySet()) {
         TuffChunkFinderModule.Inner1 var7 = (TuffChunkFinderModule.Inner1)var6.getValue();
         var1.addAll(var7.amethystBlocks());
         var2.addAll(var7.buddingBlocks());
         var3.addAll(var7.geodeBlocks());
         if (var7.paletteBudding()) {
            var4.add(((ChunkPos)var6.getKey()).toLong());
         }
      }

      PedroAmethystModule var24 = FakeRankModuleHelper.getVal().valOf(PedroAmethystModule.class);
      if (var24 != null && var24.isEnabled()) {
         var2 = new HashSet<>(var24.getSet());
         var4 = new HashSet<>(var24.getSet2());
      }

      boolean var25 = this.val8.getObject();
      ArrayList var26 = new ArrayList();
      int var8 = this.getInt();

      while (!var3.isEmpty()) {
         BlockPos var9 = (BlockPos)var3.iterator().next();
         var3.remove(var9);
         ArrayDeque var10 = new ArrayDeque();
         var10.add(var9);
         int var11 = 0;
         boolean var12 = false;
         int var13 = Integer.MAX_VALUE;
         int var14 = Integer.MIN_VALUE;
         int var15 = Integer.MAX_VALUE;
         int var16 = Integer.MIN_VALUE;
         int var17 = Integer.MAX_VALUE;
         int var18 = Integer.MIN_VALUE;

         while (!var10.isEmpty()) {
            BlockPos var19 = (BlockPos)var10.removeFirst();
            if (!var12 && (var2.contains(var19) || var4.contains(ChunkPos.toLong(var19.getX() >> 4, var19.getZ() >> 4)))) {
               var12 = true;
            }

            if (var1.contains(var19)) {
               var11++;
               var13 = Math.min(var13, var19.getX());
               var14 = Math.max(var14, var19.getX());
               var15 = Math.min(var15, var19.getY());
               var16 = Math.max(var16, var19.getY());
               var17 = Math.min(var17, var19.getZ());
               var18 = Math.max(var18, var19.getZ());
            }

            for (int var20 = -1; var20 <= 1; var20++) {
               for (int var21 = -1; var21 <= 1; var21++) {
                  for (int var22 = -1; var22 <= 1; var22++) {
                     if (var20 != 0 || var21 != 0 || var22 != 0) {
                        BlockPos var23 = var19.add(var20, var21, var22);
                        if (var3.remove(var23)) {
                           var10.addLast(var23);
                        }
                     }
                  }
               }
            }
         }

         if (var11 >= var8 && (!var25 || var12)) {
            var26.add(new Inner3(var13 >> 4, var14 >> 4, var17 >> 4, var18 >> 4, var13, var14, var15, var16, var17, var18));
         }
      }

      return var26;
   }

   private TuffChunkFinderModule.Inner1 valOf(WorldChunk var1) {
      HashSet var2 = new HashSet();
      HashSet var3 = new HashSet();
      HashSet var4 = new HashSet();
      boolean var5 = false;
      ChunkPos var6 = var1.getPos();
      int var7 = Math.min(this.val6.getObject(), this.val7.getObject());
      int var8 = Math.max(this.val6.getObject(), this.val7.getObject());
      int var9 = Math.max(var7, var1.getBottomY());
      int var10 = Math.min(var8, var1.getBottomY() + var1.getHeight() - 1);
      if (var9 > var10) {
         new Inner1(Set.of(), Set.of(), Set.of(), false);
      } else {
         int var11 = Math.floorDiv(var9, 16);
         int var12 = Math.floorDiv(var10, 16);

         for (int var13 = var11; var13 <= var12; var13++) {
            ChunkSection var14 = var1.getSection(var1.sectionCoordToIndex(var13));
            if (!var14.isEmpty()) {
               if (var14.getBlockStateContainer().hasAny(TuffChunkFinderModule::check)) {
                  var5 = true;
               }

               if (var14.hasAny(TuffChunkFinderModule::check2)) {
                  int var15 = var13 == var11 ? Math.floorMod(var9, 16) : 0;
                  int var16 = var13 == var12 ? Math.floorMod(var10, 16) : 15;

                  for (int var17 = var15; var17 <= var16; var17++) {
                     int var18 = var13 * 16 + var17;

                     for (int var19 = 0; var19 < 16; var19++) {
                        for (int var20 = 0; var20 < 16; var20++) {
                           BlockState var21 = var14.getBlockState(var19, var17, var20);
                           if (check2(var21)) {
                              BlockPos var22 = new BlockPos(var6.getStartX() + var19, var18, var6.getStartZ() + var20);
                              var3.add(var22);
                              if (check4(var21)) {
                                 var2.add(var22);
                              }

                              if (check(var21)) {
                                 var4.add(var22);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return new Inner1(Set.copyOf(var2), Set.copyOf(var3), Set.copyOf(var4), var5);
      }
      return null;
   }

   private static boolean check(BlockState var0) {
      return var0.isOf(Blocks.BUDDING_AMETHYST);
   }

   private static boolean check4(BlockState var0) {
      return var0.isOf(Blocks.AMETHYST_CLUSTER)
         || var0.isOf(Blocks.LARGE_AMETHYST_BUD)
         || var0.isOf(Blocks.MEDIUM_AMETHYST_BUD)
         || var0.isOf(Blocks.SMALL_AMETHYST_BUD)
         || var0.isOf(Blocks.AMETHYST_BLOCK)
         || check(var0);
   }

   private static boolean check2(BlockState var0) {
      return check4(var0) || var0.isOf(Blocks.CALCITE) || var0.isOf(Blocks.SMOOTH_BASALT);
   }

   private boolean check3(TuffChunkFinderModule.Inner3 var1, TuffChunkFinderModule.Inner3 var2) {
      int var3 = intOf3(var1.minChunkX(), var1.maxChunkX(), var2.minChunkX(), var2.maxChunkX());
      int var4 = intOf3(var1.minChunkZ(), var1.maxChunkZ(), var2.minChunkZ(), var2.maxChunkZ());
      int var5 = this.val10.getObject();
      return var3 <= var5 && var4 <= var5;
   }

   private static int intOf3(int var0, int var1, int var2, int var3) {
      if (var1 < var2) {
         return var2 - var1;
      } else {
         return var3 < var0 ? var0 - var3 : 0;
      }
   }

   private int getInt() {
      return 1 + (this.val4.getObject() - 1) * 2;
   }

   private void run16() {
      HashSet var1 = new HashSet();

      for (TuffChunkFinderModule.Inner4 var3 : this.list) {
         String var4 = var3.minChunkX() + ":" + var3.maxChunkX() + ":" + var3.minChunkZ() + ":" + var3.maxChunkZ();
         var1.add(var4);
         if (this.val11.getObject() && this.set3.add(var4)) {
            this.run8(var3);
         }
      }

      this.set3.retainAll(var1);
   }

   private void run8(TuffChunkFinderModule.Inner4 var1) {
      int var2 = (var1.minChunkX() + var1.maxChunkX() + 1) * 8;
      int var3 = (var1.minChunkZ() + var1.maxChunkZ() + 1) * 8;
      ChunkFinderV2ModuleUtil.run(class310, Items.AMETHYST_SHARD, this.title, var1.totalClusters() + " amethyst geodes at " + var2 + ", " + var3);
   }

   private void run18() {
      this.arrayDeque.clear();
      this.set.clear();
      this.set2.clear();
      this.map.clear();
      this.map2.clear();
      this.set3.clear();
      this.list = List.of();
      this.bool_2 = true;
      this.intVal6 = 0;
      this.intVal7 = 0;
      this.intVal8 = 0;
      this.run19();
   }

   private void run19() {
      if (class310.player != null && class310.world != null) {
         ChunkPos var1 = class310.player.getChunkPos();
         ArrayList var2 = new ArrayList();
         HashSet var3 = new HashSet();
         HashSet var4 = new HashSet();

         for (Chunk var6 : ActivityChunkFinderModuleUtil.getIterable()) {
            if (var6 instanceof WorldChunk var7) {
               ChunkPos var8 = var7.getPos();
               if (Math.abs(var8.x - var1.x) <= this.val9.getObject() && Math.abs(var8.z - var1.z) <= this.val9.getObject()) {
                  var2.add(var7);
                  var3.add(var8);
                  var4.add(var8.toLong());
               }
            }
         }

         this.arrayDeque.removeIf(a0x -> TuffChunkFinderModule.check8(var4, a0x));
         this.set.retainAll(var4);
         this.bool_2 = this.bool_2 | this.map2.keySet().removeIf(a0x -> TuffChunkFinderModule.check7(var4, a0x));
         this.set2.removeIf(a0x -> TuffChunkFinderModule.check6(var4, a0x));
         this.map.keySet().removeIf(a0x -> TuffChunkFinderModule.check5(var4, a0x));
         var2.sort(Comparator.comparing(this::booleanOf).thenComparingLong(a0x -> TuffChunkFinderModule.longOf2(var1, a0x)).thenComparingInt(this::intOf));

         for (WorldChunk var12 : (Iterable<WorldChunk>)(Object)(var2)) {
            long var13 = var12.getPos().toLong();
            int var9 = this.map.getOrDefault(var13, -1073741824);
            boolean var10 = this.intVal8 - var9 >= 120;
            if ((!this.set2.contains(var13) || var10) && this.set.add(var13)) {
               this.arrayDeque.addLast(var12.getPos());
            }
         }
      }
   }

   private void run9(ChunkPos var1, boolean var2, boolean var3) {
      if (class310.player != null && class310.world != null) {
         ChunkPos var4 = class310.player.getChunkPos();
         if (Math.abs(var1.x - var4.x) <= this.val9.getObject() && Math.abs(var1.z - var4.z) <= this.val9.getObject()) {
            long var5 = var1.toLong();
            if (var3) {
               this.set2.remove(var5);
               this.map.remove(var5);
            }

            if (this.set.add(var5)) {
               if (var2) {
                  this.arrayDeque.addFirst(var1);
               } else {
                  this.arrayDeque.addLast(var1);
               }
            } else if (var2 && this.arrayDeque.remove(var1)) {
               this.arrayDeque.addFirst(var1);
            }
         }
      }
   }

   private static long longOf(ChunkPos var0, ChunkPos var1) {
      long var2 = var0.x - var1.x;
      long var4 = var0.z - var1.z;
      return var2 * var2 + var4 * var4;
   }

   private static ActivityChunkFinderModuleEntry2 valOf2(ActivityChunkFinderModuleHelper4 var0, int var1) {
      return new ActivityChunkFinderModuleEntry2((int)(var0.intVal * 0.45), (int)(var0.intVal2 * 0.45), (int)(var0.intVal3 * 0.45), var1);
   }

   private double doubleOf(TuffChunkFinderModule.Inner4 var1) {
      if (this.val13.getObject()) {
         return this.doubleOf2(var1);
      } else {
         return ((Boolean)this.val14.getObject()) ? var1.markerY() : this.val15.getObject().intValue();
      }
   }

   private double doubleOf2(TuffChunkFinderModule.Inner4 var1) {
      if (class310.world == null) {
         return this.val15.getObject().intValue();
      } else {
         int var2 = (var1.minChunkX() + var1.maxChunkX() + 1) * 8;
         int var3 = (var1.minChunkZ() + var1.maxChunkZ() + 1) * 8;
         return class310.world.getTopY(Type.WORLD_SURFACE, var2, var3);
      }
   }

   private int intOf(WorldChunk var1) {
      return this.map.getOrDefault(var1.getPos().toLong(), -1073741824);
   }

   private static long longOf2(ChunkPos var0, WorldChunk var1) {
      return longOf(var1.getPos(), var0);
   }

   private Boolean booleanOf(WorldChunk var1) {
      return this.set2.contains(var1.getPos().toLong());
   }

   private static boolean check5(Set var0, Long var1) {
      return !var0.contains(new ChunkPos(var1));
   }

   private static boolean check6(Set var0, Long var1) {
      return !var0.contains(new ChunkPos(var1));
   }

   private static boolean check7(Set var0, ChunkPos var1) {
      return !var0.contains(var1);
   }

   private static boolean check8(Set var0, ChunkPos var1) {
      return !var0.contains(var1);
   }

   private Boolean getBoolean4() {
      return !this.val13.getObject() && !this.val14.getObject();
   }

   private Boolean getBoolean5() {
      return !this.val13.getObject();
   }

   private void run11(Integer var1) {
      if (this.isEnabled()) {
         this.run12();
      }
   }

   private void run10(Integer var1) {
      this.run18();
   }

   private void run13(Boolean var1) {
      this.run18();
   }

   private void run14(Integer var1) {
      this.run18();
   }

   private void run15(Integer var1) {
      this.run18();
   }

   private void run20(Integer var1) {
      this.run18();
   }

   private void run17(Integer var1) {
      this.run18();
   }

   final class Inner1 {
      private Set<BlockPos> amethystBlocks;
      private Set<BlockPos> geodeBlocks;
      private Set<BlockPos> buddingBlocks;
      private boolean paletteBudding;

      Inner1(Set<BlockPos> var1, Set<BlockPos> var2, Set<BlockPos> var3, boolean var4) {
         this.amethystBlocks = var1;
         this.geodeBlocks = var2;
         this.buddingBlocks = var3;
         this.paletteBudding = var4;
      }

      public Set<BlockPos> amethystBlocks() {
         return this.amethystBlocks;
      }

      public Set<BlockPos> geodeBlocks() {
         return this.geodeBlocks;
      }

      public Set<BlockPos> buddingBlocks() {
         return this.buddingBlocks;
      }

      public boolean paletteBudding() {
         return this.paletteBudding;
      }
   }

   final class Inner2 {
      private int minX;
      private int maxX;
      private int minZ;
      private int maxZ;

      Inner2(int var1, int var2, int var3, int var4) {
         this.minX = var1;
         this.maxX = var2;
         this.minZ = var3;
         this.maxZ = var4;
      }

      public int minX() {
         return this.minX;
      }

      public int maxX() {
         return this.maxX;
      }

      public int minZ() {
         return this.minZ;
      }

      public int maxZ() {
         return this.maxZ;
      }
   }

   final class Inner3 {
      private int minChunkX;
      private int maxChunkX;
      private int minChunkZ;
      private int maxChunkZ;
      private int minX;
      private int maxX;
      private int minY;
      private int maxY;
      private int minZ;
      private int maxZ;

      Inner3(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
         this.minChunkX = var1;
         this.maxChunkX = var2;
         this.minChunkZ = var3;
         this.maxChunkZ = var4;
         this.minX = var5;
         this.maxX = var6;
         this.minY = var7;
         this.maxY = var8;
         this.minZ = var9;
         this.maxZ = var10;
      }

      double getDouble() {
         return (this.minY + this.maxY) / 2.0;
      }

      TuffChunkFinderModule.Inner2 getVal() {
         return new Inner2(this.minX, this.maxX, this.minZ, this.maxZ);
      }

      public int minChunkX() {
         return this.minChunkX;
      }

      public int maxChunkX() {
         return this.maxChunkX;
      }

      public int minChunkZ() {
         return this.minChunkZ;
      }

      public int maxChunkZ() {
         return this.maxChunkZ;
      }

      public int minX() {
         return this.minX;
      }

      public int maxX() {
         return this.maxX;
      }

      public int minY() {
         return this.minY;
      }

      public int maxY() {
         return this.maxY;
      }

      public int minZ() {
         return this.minZ;
      }

      public int maxZ() {
         return this.maxZ;
      }
   }

   final class Inner4 {
      private int minChunkX;
      private int maxChunkX;
      private int minChunkZ;
      private int maxChunkZ;
      private double markerY;
      private int totalClusters;
      private List<TuffChunkFinderModule.Inner2> clusters;

      Inner4(int var1, int var2, int var3, int var4, double var5, int var7, List<TuffChunkFinderModule.Inner2> var8) {
         this.minChunkX = var1;
         this.maxChunkX = var2;
         this.minChunkZ = var3;
         this.maxChunkZ = var4;
         this.markerY = var5;
         this.totalClusters = var7;
         this.clusters = var8;
      }

      public int minChunkX() {
         return this.minChunkX;
      }

      public int maxChunkX() {
         return this.maxChunkX;
      }

      public int minChunkZ() {
         return this.minChunkZ;
      }

      public int maxChunkZ() {
         return this.maxChunkZ;
      }

      public double markerY() {
         return this.markerY;
      }

      public int totalClusters() {
         return this.totalClusters;
      }

      public List<TuffChunkFinderModule.Inner2> clusters() {
         return this.clusters;
      }
   }
}
