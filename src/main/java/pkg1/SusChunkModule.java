package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class SusChunkModule extends Module {
   private static final int intVal = -58;
   private static final int intVal2 = 50;
   private static final int intVal3 = 30;
   private static final int intVal4 = 100;
   private static final int intVal5 = 5;
   private static final int intVal6 = 20;
   private static final int intVal7 = 20;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Integer> val2_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("simulation-distance")
            .valOf2("Chunks must be at this edge distance before new suspicious hits are created.")
            .valOf3(5)
            .valOf4(1, 32)
            .valOf5(1, 32)
            .getVal()
      );
   private final Setting<Integer> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("sensitivity")
            .valOf2("Lower values flag weaker suspicious chunks. Higher values reduce false positives.")
            .valOf3(5)
            .valOf4(1, 20)
            .valOf5(1, 20)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("color")
            .valOf2("Colour of every flagged chunk.")
            .valOf3(new ActivityChunkFinderModuleHelper4(0, 120, 255, 255))
            .getVal()
      );
   private final Setting<Boolean> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("tracers")
            .valOf2("Draw a line from your crosshair to the middle of each flagged area, in the same colour.")
            .valOf3(false)
            .getVal()
      );
   private final Setting<Boolean> val6 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("highlight-best").valOf2("Draw the strongest candidate more solid than the rest.").valOf3(false).getVal()
      );
   private final Setting<Boolean> val7 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("sticky")
            .valOf2("Once a chunk is flagged, keep it flagged even if a later pass decides it is not suspicious after all.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Integer> val8 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("cache-time")
            .valOf2("How many seconds findings survive after you move far enough away that the scan state is thrown out.")
            .valOf3(60)
            .valOf6(0)
            .valOf5(0, 600)
            .getVal()
      );
   private final Setting<Integer> val9 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6().valOf("alpha").valOf2("Opacity of the suspicious chunk ESP.").valOf3(42).valOf4(1, 255).valOf5(1, 255).getVal()
      );
   private final Setting<Boolean> val10 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("cave-vines").valOf2("Detect cave vine density chunks.").valOf3(false).getVal());
   private final Setting<Boolean> val11 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("vines").valOf2("Detect vine density chunks.").valOf3(false).getVal());
   private final Setting<Boolean> val12 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("amethyst").valOf2("Detect depleted amethyst geode edge chunks.").valOf3(true).getVal());
   private final Setting<Boolean> val13 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("bamboo").valOf2("Detect bamboo density chunks.").valOf3(false).getVal());
   private final Setting<Boolean> val14 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("bee-nest").valOf2("Detect bee nest and beehive chunks.").valOf3(false).getVal());
   private final Setting<Boolean> val15 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("rotated-deepslate")
            .valOf2("Detect horizontally rotated deepslate, a strong player placement signal.")
            .valOf3(false)
            .getVal()
      );
   private final Map<Long, SusChunkModule.Inner1> map = new ConcurrentHashMap<>();
   private final Map<Long, Integer> map2 = new ConcurrentHashMap<>();
   private final Map<Long, String> map3 = new ConcurrentHashMap<>();
   private final Map<Long, SusChunkModule.Inner2> map4 = new ConcurrentHashMap<>();
   private final Map<Long, Integer> map5 = new ConcurrentHashMap<>();
   private final Set<Long> set = ConcurrentHashMap.newKeySet();
   private final Map<Long, Long> map6 = new ConcurrentHashMap<>();
   private final ConcurrentLinkedQueue<Long> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
   private final Set<Long> set2 = ConcurrentHashMap.newKeySet();
   private final ConcurrentLinkedQueue<Long> concurrentLinkedQueue2 = new ConcurrentLinkedQueue<>();
   private final Set<Long> set3 = ConcurrentHashMap.newKeySet();
   private long longVal = Long.MIN_VALUE;
   private ChunkPos class1923;
   private int intVal8;
   private boolean bool_2;

   public SusChunkModule() {
      super(SwyzzyAddon.val2, "sus-chunk", "Finds edge-loaded chunks with selected suspicious block signals.");
   }

   @Override
   public void run6() {
      this.run16();
      this.run12();
   }

   @Override
   public void run7() {
      this.run16();
   }

   @Override
   public String getString2() {
      return this.set.isEmpty() ? null : String.valueOf(this.set.size());
   }

   @InternalHelper5
   private void run4(AmethystChunkFinderModuleHelper var1) {
      if (var1.chunk() != null && class310.player != null) {
         this.run3(var1.chunk().getPos());
      }
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.world != null && class310.player != null) {
         this.intVal8++;
         ChunkPos var2 = class310.player.getChunkPos();
         if (this.class1923 != null && intOf4(var2, this.class1923) > 32) {
            this.run18();
            this.run14(this.val8.getObject().intValue() * 1000L);
            this.run12();
            this.class1923 = var2;
         } else {
            this.class1923 = var2;
            if (this.intVal8 % 200 == 0) {
               this.run12();
            }

            Long var3;
            for (int var4 = 0; var4 < 2 && (var3 = this.concurrentLinkedQueue.poll()) != null; var4++) {
               this.set2.remove(var3);
               this.run5(var3);
            }

            for (int var6 = 0; var6 < 8 && (var3 = this.concurrentLinkedQueue2.poll()) != null; var6++) {
               this.set3.remove(var3);
               this.run9(var3);
            }

            this.run19();
            if (this.bool_2 && this.intVal8 % 10 == 0) {
               this.run13();
               this.bool_2 = false;
            }
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData var1) {
      if (class310.world != null && class310.player != null && !this.set.isEmpty()) {
         if (this.bool_2 || this.map4.isEmpty()) {
            this.run13();
            this.bool_2 = false;
         }

         ActivityChunkFinderModuleEntry2 var2 = this.valOf5(this.val9.getObject());
         ActivityChunkFinderModuleEntry2 var3 = ((Boolean)this.val6.getObject()) ? this.valOf5(Math.min(255, Math.max(this.val9.getObject() + 40, this.val9.getObject() * 2)))
            : var2;
         int var4 = 0;
         SusChunkModule.Inner2 var5 = this.map4.get(this.longVal);
         if (var5 != null) {
            this.run11(var1, var5, var3);
            var4++;
         }

         for (SusChunkModule.Inner2 var7 : this.map4.values()) {
            if (var7.leader != this.longVal) {
               if (var4 >= 20) {
                  break;
               }

               this.run11(var1, var7, var2);
               var4++;
            }
         }
      }
   }

   private void run12() {
      if (class310.world != null && class310.player != null && this.isEnabled3()) {
         ChunkPos var1 = class310.player.getChunkPos();
         int var2 = Math.max(8, this.val2_2.getObject() + 2);
         ArrayList var3 = new ArrayList();

         for (int var4 = -var2; var4 <= var2; var4++) {
            for (int var5 = -var2; var5 <= var2; var5++) {
               ChunkPos var6 = new ChunkPos(var1.x + var4, var1.z + var5);
               if (intOf4(var6, var1) >= this.val2_2.getObject()) {
                  var3.add(var6);
               }
            }
         }

         var3.sort(Comparator.comparingInt(var905 -> SusChunkModule.intOf6(this.class1923, (ChunkPos)var905)).reversed());

         for (ChunkPos var8 : (Iterable<ChunkPos>)(Object)(var3)) {
            if (class310.world.getChunkManager().getWorldChunk(var8.x, var8.z) != null) {
               this.run3(var8);
            }
         }
      }
   }

   private void run3(ChunkPos var1) {
      if (this.isEnabled3()) {
         long var2 = var1.toLong();
         if (this.set2.add(var2)) {
            this.concurrentLinkedQueue.add(var2);
         }
      }
   }

   private void run8(ChunkPos var1) {
      for (int var2 = -1; var2 <= 1; var2++) {
         for (int var3 = -1; var3 <= 1; var3++) {
            long var4 = ChunkPos.toLong(var1.x + var2, var1.z + var3);
            if (this.set3.add(var4)) {
               this.concurrentLinkedQueue2.add(var4);
            }
         }
      }
   }

   private void run5(long var1) {
      if (class310.world != null) {
         ChunkPos var3 = new ChunkPos(var1);
         WorldChunk var4 = class310.world.getChunkManager().getWorldChunk(var3.x, var3.z);
         if (var4 == null) {
            int var5 = this.map5.merge(var1, 1, Integer::sum);
            if (var5 <= 20) {
               this.run3(var3);
            }
         } else {
            this.map5.remove(var1);
            this.map.put(var1, this.valOf(var4));
            this.run8(var3);
         }
      }
   }

   private SusChunkModule.Inner1 valOf(WorldChunk var1) {
      ChunkPos var2 = var1.getPos();
      int var3 = var2.getStartX();
      int var4 = var2.getStartZ();
      Mutable var5 = new Mutable();
      int var6 = 0;
      int var7 = 0;
      int var8 = 0;
      int var9 = 0;
      int var10 = 0;
      int var11 = 0;
      int var12 = 0;
      int var13 = 0;
      int var14 = 0;
      BlockPos var15 = null;
      boolean var16 = this.val12.getObject();
      boolean var17 = this.val10.getObject() || this.val11.getObject() || this.val13.getObject() || this.val14.getObject() || this.val15.getObject();
      ChunkSection[] var18 = var1.getSectionArray();
      int var19 = var1.getBottomY();

      for (int var20 = 0; var20 < var18.length; var20++) {
         ChunkSection var21 = var18[var20];
         if (var21 != null && !var21.isEmpty()) {
            int var22 = var19 + var20 * 16;
            if (var17 || var16 && check11(var22, var22 + 15, -58, 50)) {
               int var23 = var17 ? 0 : Math.max(0, -58 - var22);
               int var24 = var17 ? 15 : Math.min(15, 50 - var22);
               int var25 = 0;

               for (int var26 = 0; var26 < 16; var26++) {
                  for (int var27 = 0; var27 < 16; var27++) {
                     for (int var28 = var23; var28 <= var24; var28++) {
                        int var29 = var22 + var28;
                        BlockState var30 = var21.getBlockState(var26, var28, var27);
                        Block var31 = var30.getBlock();
                        int var32 = var3 + var26;
                        int var33 = var4 + var27;
                        boolean var34 = false;
                        if (var16 && check3(var31)) {
                           if (var29 >= -58 && var29 <= 30) {
                              var8++;
                              if (check4(var31)) {
                                 var7++;
                              }

                              if (check5(var31)) {
                                 var25++;
                              }

                              var34 = true;
                           }

                           if (var29 >= -58 && var29 <= 50 && var6 < 5) {
                              var6 = this.intOf(var32, var29, var33, var6, var5);
                           }
                        }

                        if (this.val10.getObject() && check6(var31)) {
                           var10++;
                           var34 = true;
                        }

                        if (this.val11.getObject() && check7(var31)) {
                           var11++;
                           var34 = true;
                        }

                        if (this.val13.getObject() && check8(var31)) {
                           var12++;
                           var34 = true;
                        }

                        if (this.val14.getObject() && check9(var30)) {
                           var13++;
                           var34 = true;
                        }

                        if (this.val15.getObject() && check10(var30)) {
                           var14++;
                           var34 = true;
                        }

                        if (var34 && var15 == null) {
                           var15 = new BlockPos(var32, var29, var33);
                        }
                     }
                  }
               }

               var9 = Math.max(var9, var25);
            }
         }
      }

      return new Inner1(Math.min(var6, 5), var7, var8, var9, var10, var11, var12, var13, var14, var15);
   }

   private int intOf(int var1, int var2, int var3, int var4, Mutable var5) {
      if (class310.world == null) {
         return var4;
      } else {
         int var6 = var4;

         for (int var7 = -1; var7 <= 1 && var6 < 5; var7++) {
            int var8 = var2 + var7;
            if (var8 >= -58 && var8 <= 50) {
               for (int var9 = -3; var9 <= 3 && var6 < 5; var9++) {
                  for (int var10 = -3; var10 <= 3; var10++) {
                     if (class310.world.getLightLevel(LightType.BLOCK, var5.set(var1 + var9, var8, var3 + var10)) == 5) {
                        if (++var6 >= 5) {
                           break;
                        }
                     }
                  }
               }
            }
         }

         return var6;
      }
   }

   private void run9(long var1) {
      SusChunkModule.Inner1 var3 = this.map.get(var1);
      if (var3 != null && this.isEnabled3()) {
         if (this.set.contains(var1) || class310.player == null || intOf4(new ChunkPos(var1), class310.player.getChunkPos()) >= this.val2_2.getObject()) {
            SusChunkModule.Inner3 var4 = this.valOf2(var3, var1);
            if (var4.value >= this.val3_2.getObject()) {
               this.set.add(var1);
               this.map6.remove(var1);
               this.map2.put(var1, var4.value);
               this.map3.put(var1, var4.reason);
               this.bool_2 = true;
            } else {
               this.run10(var1);
            }
         }
      }
   }

   private SusChunkModule.Inner3 valOf2(SusChunkModule.Inner1 var1, long var2) {
      int var4 = 0;
      String var5 = "";
      int var6 = this.intOf3(var2);
      if (this.val12.getObject() && var1.buddingCount == 0 && this.check(var2)) {
         int var7 = this.intOf2(var2);
         int var8 = 0;
         if (var1.maxAmethystSection >= 100) {
            var8 += 5;
         }

         var8 += Math.min(6, var1.shellAmethyst / 20);
         var8 += Math.min(6, var7 * 2);
         if (var1.amethystLightHits == 0 && var1.shellAmethyst >= 2) {
            var8 += 3;
         }

         if (var8 > var4) {
            var4 = var8;
            var5 = "amethyst";
         }
      }

      SusChunkModule.Inner3 var19 = this.valOf3(var4, var5, var1.caveVineCount / 18, "cave vines");
      var4 = var19.value;
      var5 = var19.reason;
      var19 = this.valOf3(var4, var5, var1.vineCount / 24, "vines");
      var4 = var19.value;
      var5 = var19.reason;
      var19 = this.valOf3(var4, var5, var1.bambooCount / 20, "bamboo");
      var4 = var19.value;
      var5 = var19.reason;
      var19 = this.valOf3(var4, var5, var1.beeNestCount * 5, "bee nest");
      var4 = var19.value;
      var5 = var19.reason;
      var19 = this.valOf3(var4, var5, var1.rotatedDeepslateCount / 12, "rotated deepslate");
      var4 = var19.value;
      var5 = var19.reason;
      if (var4 > 0) {
         var4 += Math.min(5, var6);
         if (var6 >= 3) {
            var4 += 2;
         }
      }

      if (var5.isEmpty()) {
         var5 = "suspicious";
      }

      return new Inner3(var4, var5);
   }

   private SusChunkModule.Inner3 valOf3(int var1, String var2, int var3, String var4) {
      return var3 > var1 ? new Inner3(var3, var4) : new Inner3(var1, var2);
   }

   private boolean check(long var1) {
      SusChunkModule.Inner1 var3 = this.map.get(var1);
      if (var3 != null && var3.shellAmethyst >= 2) {
         return true;
      } else {
         ChunkPos var4 = new ChunkPos(var1);

         for (int var5 = -1; var5 <= 1; var5++) {
            for (int var6 = -1; var6 <= 1; var6++) {
               if (var5 != 0 || var6 != 0) {
                  SusChunkModule.Inner1 var7 = this.map.get(ChunkPos.toLong(var4.x + var5, var4.z + var6));
                  if (var7 != null && var7.shellAmethyst >= 2) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   private int intOf2(long var1) {
      int var3 = 0;
      ChunkPos var4 = new ChunkPos(var1);

      for (int var5 = -1; var5 <= 1; var5++) {
         for (int var6 = -1; var6 <= 1; var6++) {
            if (var5 != 0 || var6 != 0) {
               SusChunkModule.Inner1 var7 = this.map.get(ChunkPos.toLong(var4.x + var5, var4.z + var6));
               if (var7 != null && var7.buddingCount <= 0 && var7.amethystLightHits <= 1) {
                  var3++;
               }
            }
         }
      }

      return var3;
   }

   private int intOf3(long var1) {
      int var3 = 0;
      ChunkPos var4 = new ChunkPos(var1);

      for (int var5 = -1; var5 <= 1; var5++) {
         for (int var6 = -1; var6 <= 1; var6++) {
            if (var5 != 0 || var6 != 0) {
               SusChunkModule.Inner1 var7 = this.map.get(ChunkPos.toLong(var4.x + var5, var4.z + var6));
               if (var7 != null && this.check2(var7)) {
                  var3++;
               }
            }
         }
      }

      return var3;
   }

   private boolean check2(SusChunkModule.Inner1 var1) {
      return this.val12.getObject() && var1.buddingCount == 0 && var1.shellAmethyst >= 2
         || this.val10.getObject() && var1.caveVineCount >= 48
         || this.val11.getObject() && var1.vineCount >= 64
         || this.val13.getObject() && var1.bambooCount >= 64
         || this.val14.getObject() && var1.beeNestCount > 0
         || this.val15.getObject() && var1.rotatedDeepslateCount >= 16;
   }

   private void run10(long var1) {
      if (!this.val7.getObject()) {
         if (this.set.remove(var1)) {
            this.bool_2 = true;
         }

         this.map2.remove(var1);
         this.map3.remove(var1);
      }
   }

   private void run13() {
      this.map4.clear();
      if (this.set.isEmpty()) {
         this.longVal = Long.MIN_VALUE;
      } else {
         HashSet var1 = new HashSet<>(this.set);

         while (!var1.isEmpty()) {
            long var2 = (Long)var1.iterator().next();
            SusChunkModule.Inner2 var4 = this.valOf4(var2, var1);
            this.map4.put(var4.leader, var4);
         }

         this.longVal = this.map4.values().stream().max(Comparator.comparingInt(SusChunkModule::intOf5)).map(SusChunkModule::longOf).orElse(Long.MIN_VALUE);
      }
   }

   private SusChunkModule.Inner2 valOf4(long var1, Set<Long> var3) {
      ArrayDeque var4 = new ArrayDeque();
      ArrayList var5 = new ArrayList();
      var4.add(var1);
      var3.remove(var1);
      int var6 = Integer.MAX_VALUE;
      int var7 = Integer.MIN_VALUE;
      int var8 = Integer.MAX_VALUE;
      int var9 = Integer.MIN_VALUE;
      long var10 = var1;
      int var12 = Integer.MIN_VALUE;
      int var13 = 0;

      while (!var4.isEmpty()) {
         long var14 = (Long)var4.removeFirst();
         var5.add(var14);
         ChunkPos var16 = new ChunkPos(var14);
         var6 = Math.min(var6, var16.x);
         var7 = Math.max(var7, var16.x);
         var8 = Math.min(var8, var16.z);
         var9 = Math.max(var9, var16.z);
         int var17 = this.map2.getOrDefault(var14, 0);
         var13 += var17;
         if (var17 > var12) {
            var12 = var17;
            var10 = var14;
         }

         for (int var18 = -1; var18 <= 1; var18++) {
            for (int var19 = -1; var19 <= 1; var19++) {
               if (var18 != 0 || var19 != 0) {
                  long var20 = ChunkPos.toLong(var16.x + var18, var16.z + var19);
                  if (var3.remove(var20)) {
                     var4.addLast(var20);
                  }
               }
            }
         }
      }

      int var22 = var13 + Math.min(20, var5.size() * 3);
      return new Inner2(var10, var6, var7, var8, var9, var5.size(), var22);
   }

   private void run11(ActivityChunkFinderModuleData var1, SusChunkModule.Inner2 var2, ActivityChunkFinderModuleEntry2 var3) {
      var1.val
         .run(
            var2.minChunkX * 16.0, 64.0, var2.minChunkZ * 16.0, (var2.maxChunkX + 1) * 16.0, 64.06, (var2.maxChunkZ + 1) * 16.0, var3, var3, RenderMode.Both, 0
         );
      if (this.val5.getObject()) {
         double var4 = (var2.minChunkX + var2.maxChunkX + 1) * 8.0;
         double var6 = (var2.minChunkZ + var2.maxChunkZ + 1) * 8.0;
         var1.val.run6(BlockEspPlusModuleData.class243.x, BlockEspPlusModuleData.class243.y, BlockEspPlusModuleData.class243.z, var4, 64.0, var6, var3);
      }
   }

   private ActivityChunkFinderModuleEntry2 valOf5(int var1) {
      ActivityChunkFinderModuleHelper4 var2 = this.val4.getObject();
      return new ActivityChunkFinderModuleEntry2(var2.intVal, var2.intVal2, var2.intVal3, Math.max(1, Math.min(255, var1)));
   }

   private boolean isEnabled3() {
      return this.val10.getObject()
         || this.val11.getObject()
         || this.val12.getObject()
         || this.val13.getObject()
         || this.val14.getObject()
         || this.val15.getObject();
   }

   private static String stringOf(Block var0) {
      Identifier var1 = Registries.BLOCK.getId(var0);
      return var1 == null ? "" : var1.getPath();
   }

   private static boolean check3(Block var0) {
      String var1 = stringOf(var0);
      return var1.equals("amethyst_block")
         || var1.equals("budding_amethyst")
         || var1.equals("small_amethyst_bud")
         || var1.equals("medium_amethyst_bud")
         || var1.equals("large_amethyst_bud")
         || var1.equals("amethyst_cluster");
   }

   private static boolean check4(Block var0) {
      return stringOf(var0).equals("budding_amethyst");
   }

   private static boolean check5(Block var0) {
      return stringOf(var0).equals("amethyst_block");
   }

   private static boolean check6(Block var0) {
      String var1 = stringOf(var0);
      return var1.equals("cave_vines") || var1.equals("cave_vines_plant");
   }

   private static boolean check7(Block var0) {
      String var1 = stringOf(var0);
      return var1.equals("vine")
         || var1.equals("weeping_vines")
         || var1.equals("weeping_vines_plant")
         || var1.equals("twisting_vines")
         || var1.equals("twisting_vines_plant");
   }

   private static boolean check8(Block var0) {
      String var1 = stringOf(var0);
      return var1.equals("bamboo") || var1.equals("bamboo_sapling");
   }

   private static boolean check9(BlockState var0) {
      String var1 = stringOf(var0.getBlock());
      return var1.equals("bee_nest") ? true : var1.equals("beehive") && var0.contains(Properties.HONEY_LEVEL) && (Integer)var0.get(Properties.HONEY_LEVEL) >= 4;
   }

   private static boolean check10(BlockState var0) {
      return stringOf(var0.getBlock()).equals("deepslate") && var0.contains(Properties.AXIS) && var0.get(Properties.AXIS) != Axis.Y;
   }

   private static int intOf4(ChunkPos var0, ChunkPos var1) {
      return Math.max(Math.abs(var0.x - var1.x), Math.abs(var0.z - var1.z));
   }

   private static boolean check11(int var0, int var1, int var2, int var3) {
      return var0 <= var3 && var1 >= var2;
   }

   private void run16() {
      this.run18();
      this.map2.clear();
      this.map3.clear();
      this.map4.clear();
      this.set.clear();
      this.map6.clear();
      this.longVal = Long.MIN_VALUE;
      this.class1923 = null;
      this.intVal8 = 0;
      this.bool_2 = false;
   }

   private void run18() {
      this.map.clear();
      this.map5.clear();
      this.concurrentLinkedQueue.clear();
      this.set2.clear();
      this.concurrentLinkedQueue2.clear();
      this.set3.clear();
   }

   private void run14(long var1) {
      long var3 = System.currentTimeMillis() + var1;

      for (Long var6 : this.set) {
         this.map6.putIfAbsent(var6, var3);
      }
   }

   private void run19() {
      if (!this.map6.isEmpty()) {
         long var1 = System.currentTimeMillis();
         this.map6.entrySet().removeIf(var905 -> this.check12(var1, var905));
      }
   }

   private boolean check12(long var1, Entry var3) {
      if (var1 < (Long)var3.getValue()) {
         return false;
      } else {
         long var4 = (Long)(var3.getKey());
         if (this.set.remove(var4)) {
            this.bool_2 = true;
         }

         this.map2.remove(var4);
         this.map3.remove(var4);
         return true;
      }
   }

   private static Long longOf(SusChunkModule.Inner2 var0) {
      return var0.leader;
   }

   private static int intOf5(SusChunkModule.Inner2 var0) {
      return var0.score;
   }

   private static int intOf6(ChunkPos var0, ChunkPos var1) {
      return intOf4(var1, var0);
   }

   final class Inner1 {
      final int amethystLightHits;
      final int buddingCount;
      final int shellAmethyst;
      final int maxAmethystSection;
      final int caveVineCount;
      final int vineCount;
      final int bambooCount;
      final int beeNestCount;
      final int rotatedDeepslateCount;
      private BlockPos nearestSignal;

      Inner1(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, BlockPos var10) {
         this.amethystLightHits = var1;
         this.buddingCount = var2;
         this.shellAmethyst = var3;
         this.maxAmethystSection = var4;
         this.caveVineCount = var5;
         this.vineCount = var6;
         this.bambooCount = var7;
         this.beeNestCount = var8;
         this.rotatedDeepslateCount = var9;
         this.nearestSignal = var10;
      }

      public int amethystLightHits() {
         return this.amethystLightHits;
      }

      public int buddingCount() {
         return this.buddingCount;
      }

      public int shellAmethyst() {
         return this.shellAmethyst;
      }

      public int maxAmethystSection() {
         return this.maxAmethystSection;
      }

      public int caveVineCount() {
         return this.caveVineCount;
      }

      public int vineCount() {
         return this.vineCount;
      }

      public int bambooCount() {
         return this.bambooCount;
      }

      public int beeNestCount() {
         return this.beeNestCount;
      }

      public int rotatedDeepslateCount() {
         return this.rotatedDeepslateCount;
      }

      public BlockPos nearestSignal() {
         return this.nearestSignal;
      }
   }

   final class Inner2 {
      final long leader;
      final int minChunkX;
      final int maxChunkX;
      final int minChunkZ;
      final int maxChunkZ;
      private int size;
      final int score;

      Inner2(long var1, int var3, int var4, int var5, int var6, int var7, int var8) {
         this.leader = var1;
         this.minChunkX = var3;
         this.maxChunkX = var4;
         this.minChunkZ = var5;
         this.maxChunkZ = var6;
         this.size = var7;
         this.score = var8;
      }

      public long leader() {
         return this.leader;
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

      public int size() {
         return this.size;
      }

      public int score() {
         return this.score;
      }
   }

   final class Inner3 {
      final int value;
      final String reason;

      Inner3(int var1, String var2) {
         this.value = var1;
         this.reason = var2;
      }

      public int value() {
         return this.value;
      }

      public String reason() {
         return this.reason;
      }
   }
}
