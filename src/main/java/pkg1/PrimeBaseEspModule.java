package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

public final class PrimeBaseEspModule extends Module {
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Thresholds");
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Sound");
   private final ActivityChunkFinderModuleEntry val4 = this.val2.valOf("Open space");
   private final ActivityChunkFinderModuleEntry val5 = this.val2.valOf("Render");
   private final Setting<Integer> val6 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("y-level")
            .valOf2("Only count things at or below this height. 0 is roughly where deepslate starts.")
            .valOf3(0)
            .valOf5(-64, 320)
            .getVal()
      );
   private final Setting<Integer> val7 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6().valOf("check-interval").valOf2("How often to count, in ticks.").valOf3(20).valOf6(1).valOf5(1, 100).getVal()
      );
   private final Setting<Boolean> val8 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("notify").valOf2("Print the chunk in chat when a base is found. Only you see it.").valOf3(true).getVal()
      );
   private final Setting<Integer> val9 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("max-tracked")
            .valOf2("Stop collecting after this many chunks.")
            .valOf3(100)
            .valOf6(1)
            .valOf5(1, 500)
            .getVal()
      );
   private final Setting<Integer> val10 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("hoppers")
            .valOf2("Hoppers in one chunk before it counts. 0 turns this off.")
            .valOf3(10)
            .valOf6(0)
            .valOf5(0, 64)
            .getVal()
      );
   private final Setting<Integer> val11 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("chests")
            .valOf2("Chests in one chunk before it counts. 0 turns this off.")
            .valOf3(6)
            .valOf6(0)
            .valOf5(0, 64)
            .getVal()
      );
   private final Setting<Integer> val12 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("shulkers")
            .valOf2("Shulker boxes in one chunk before it counts. 0 turns this off.")
            .valOf3(4)
            .valOf6(0)
            .valOf5(0, 64)
            .getVal()
      );
   private final Setting<Integer> val13 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("barrels")
            .valOf2("Barrels in one chunk before it counts. 0 turns this off.")
            .valOf3(4)
            .valOf6(0)
            .valOf5(0, 64)
            .getVal()
      );
   private final Setting<Integer> val14 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("armor-stands")
            .valOf2("Armour stands in one chunk before it counts. 0 turns this off.")
            .valOf3(10)
            .valOf6(0)
            .valOf5(0, 64)
            .getVal()
      );
   private final Setting<Boolean> val15 = this.val3_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("play-sound").valOf2("Ding when a new base is found.").valOf3(true).getVal());
   private final Setting<PrimeBaseEspModule.State2> val16 = this.val3_2
      .addSetting(
         new BaseEspModuleHelper2<PrimeBaseEspModule.State2>()
            .valOf("ding")
            .valOf2("Which sound to use.")
            .valOf3(PrimeBaseEspModule.State2.Tick)
            .valOf5(this.val15::getObject)
            .getVal()
      );
   private final Setting<Double> val17 = this.val3_2
      .addSetting(
         new AutoTotemModuleHelper2()
            .valOf("volume")
            .valOf2("How loud the ding is.")
            .valOf3(1.0)
            .valOf4(0.0, 1.0)
            .valOf5(0.0, 1.0)
            .valOf7(this.val15::getObject)
            .getVal()
      );
   private final Setting<Boolean> val18 = this.val4
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("highlight-space")
            .valOf2("Flood fill the open air around the storage that flagged the chunk and draw it, so you see the shape of the room.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<PrimeBaseEspModule.State7> val19 = this.val4
      .addSetting(
         new BaseEspModuleHelper2<PrimeBaseEspModule.State7>()
            .valOf("space-mode")
            .valOf2("Bounds is one box around the room. Shell draws only the open blocks touching something solid. Full draws every open block.")
            .valOf3(PrimeBaseEspModule.State7.Shell)
            .valOf5(this.val18::getObject)
            .getVal()
      );
   private final Setting<Integer> val20 = this.val4
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("max-blocks")
            .valOf2("Give up after filling this many blocks. An open cave system can run for thousands, and this stops it.")
            .valOf3(3000)
            .valOf6(64)
            .valOf5(64, 20000)
            .valOf9(this.val18::getObject)
            .getVal()
      );
   private final Setting<Integer> val21 = this.val4
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("radius")
            .valOf2("How far the fill may spread from the storage, in blocks.")
            .valOf3(24)
            .valOf6(2)
            .valOf5(2, 64)
            .valOf9(this.val18::getObject)
            .getVal()
      );
   private final Setting<Boolean> val22 = this.val4
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("stop-at-tunnels")
            .valOf2("Stop the fill where the space narrows, so it maps the base and not the corridors leading out of it.")
            .valOf3(true)
            .valOf5(this.val18::getObject)
            .getVal()
      );
   private final Setting<Integer> val23 = this.val4
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("min-width")
            .valOf2("The fill only passes through a spot this wide on both horizontal axes. Lower it if rooms come out cut short.")
            .valOf3(4)
            .valOf4(2, 16)
            .valOf5(2, 16)
            .valOf9(this::getBoolean2)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val24 = this.val4
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("space-side-color")
            .valOf2("Fill colour of the open space.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 160, 60, 25))
            .valOf5(this.val18::getObject)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val25 = this.val4
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("space-line-color")
            .valOf2("Outline colour of the open space.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 160, 60, 90))
            .valOf5(this.val18::getObject)
            .getVal()
      );
   private final Setting<Boolean> val26 = this.val5
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("render").valOf2("Draw a box around flagged chunks.").valOf3(true).getVal());
   private final Setting<PrimeBaseEspModule.State5> val27 = this.val5
      .addSetting(
         new BaseEspModuleHelper2<PrimeBaseEspModule.State5>()
            .valOf("render-y")
            .valOf2("Draw at the storage, at your height, or at a fixed height.")
            .valOf3(PrimeBaseEspModule.State5.AtPlayer)
            .valOf5(this.val26::getObject)
            .getVal()
      );
   private final Setting<Integer> val28 = this.val5
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("fixed-y")
            .valOf2("Height to draw at when render-y is Fixed.")
            .valOf3(64)
            .valOf5(-64, 320)
            .valOf9(this::getBoolean)
            .getVal()
      );
   private final Setting<Integer> val29 = this.val5
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("box-height")
            .valOf2("How tall the box is.")
            .valOf3(4)
            .valOf6(1)
            .valOf5(1, 64)
            .valOf9(this.val26::getObject)
            .getVal()
      );
   private final Setting<Boolean> val30 = this.val5
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("tracers")
            .valOf2("Draw a line from your crosshair to each flagged chunk, in the outline colour.")
            .valOf3(false)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val31 = this.val5
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("side-color")
            .valOf2("Fill colour.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 60, 60, 40))
            .valOf5(this.val26::getObject)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val32 = this.val5
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("line-color")
            .valOf2("Outline colour.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 60, 60, 255))
            .valOf5(this.val26::getObject)
            .getVal()
      );
   private final Map<Long, PrimeBaseEspModule.Inner3> map = new HashMap<>();
   private int intVal;

   public PrimeBaseEspModule() {
      super(SwyzzyAddon.val2, "prime-base-esp", "Dings and highlights chunks holding enough deep storage to look like a base.");
   }

   @Override
   public void run6() {
      this.map.clear();
      this.intVal = 0;
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
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null) {
         if (this.map.size() < this.val9.getObject()) {
            if (--this.intVal <= 0) {
               this.intVal = this.val7.getObject();
               java.util.HashMap<Long, PrimeBaseEspModule.Inner1> var2 = new HashMap<>();

               for (Chunk var4 : ActivityChunkFinderModuleUtil.getIterable()) {
                  if (var4 instanceof WorldChunk var5) {
                     for (BlockEntity var7 : var5.getBlockEntities().values()) {
                        BlockPos var8 = var7.getPos();
                        if (var8.getY() <= this.val6.getObject()) {
                           PrimeBaseEspModule.Inner1 var9 = this.valOf(var2, var8.getX(), var8.getZ());
                           if (var7 instanceof ShulkerBoxBlockEntity) {
                              var9.intVal3++;
                              var9.run(var8);
                           } else if (var7 instanceof ChestBlockEntity) {
                              var9.intVal2++;
                              var9.run(var8);
                           } else if (var7 instanceof BarrelBlockEntity) {
                              var9.intVal4++;
                              var9.run(var8);
                           } else if (var7 instanceof HopperBlockEntity) {
                              var9.intVal++;
                              var9.run(var8);
                           }
                        }
                     }
                  }
               }

               if (((Integer)this.val14.getObject()) > 0) {
                  for (Entity var12 : class310.world.getEntities()) {
                     if (var12 instanceof ArmorStandEntity && var12.getY() <= this.val6.getObject().intValue()) {
                        PrimeBaseEspModule.Inner1 var14 = this.valOf(var2, (int)Math.floor(var12.getX()), (int)Math.floor(var12.getZ()));
                        var14.intVal5++;
                        var14.run(var12.getBlockPos());
                     }
                  }
               }

               for (Entry var13 : (Iterable<Entry>)(Object)(var2.entrySet())) {
                  if (!this.map.containsKey(var13.getKey())) {
                     String var15 = this.stringOf((PrimeBaseEspModule.Inner1)var13.getValue());
                     if (var15 != null) {
                        this.run2((Long)var13.getKey(), (PrimeBaseEspModule.Inner1)var13.getValue(), var15);
                        if (this.map.size() >= this.val9.getObject()) {
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private PrimeBaseEspModule.Inner1 valOf(Map<Long, PrimeBaseEspModule.Inner1> var1, int var2, int var3) {
      return var1.computeIfAbsent(ChunkPos.toLong(var2 >> 4, var3 >> 4), PrimeBaseEspModule::valOf3);
   }

   private String stringOf(PrimeBaseEspModule.Inner1 var1) {
      if (((Integer)this.val10.getObject()) > 0 && var1.intVal >= this.val10.getObject()) {
         return var1.intVal + " hoppers";
      } else if (((Integer)this.val11.getObject()) > 0 && var1.intVal2 >= this.val11.getObject()) {
         return var1.intVal2 + " chests";
      } else if (((Integer)this.val12.getObject()) > 0 && var1.intVal3 >= this.val12.getObject()) {
         return var1.intVal3 + " shulkers";
      } else if (((Integer)this.val13.getObject()) > 0 && var1.intVal4 >= this.val13.getObject()) {
         return var1.intVal4 + " barrels";
      } else {
         return ((Integer)this.val14.getObject()) > 0 && var1.intVal5 >= ((Integer)this.val14.getObject()) ? var1.intVal5 + " armour stands" : null;
      }
   }

   private void run2(long var1, PrimeBaseEspModule.Inner1 var3, String var4) {
      ChunkPos var5 = new ChunkPos(var1);
      PrimeBaseEspModule.Inner6 var6 = ((Boolean)this.val18.getObject()) ? this.valOf2(var3.class2338) : null;
      PrimeBaseEspModule.Inner3 var7 = new Inner3(var5.x, var5.z, var3.intVal6, var4, var6);
      this.map.put(var1, var7);
      if (this.val8.getObject()) {
         this.run4("Base at chunk %d, %d (block %d, %d) - %s.", new Object[]{var7.intVal, var7.intVal2, var7.intVal << 4, var7.intVal2 << 4, var4});
      }

      if (this.val15.getObject() && class310.player != null) {
         class310.player.playSound(this.getclass34146(), this.val17.getObject().floatValue(), 1.0F);
      }
   }

   private PrimeBaseEspModule.Inner6 valOf2(BlockPos var1) {
      if (var1 != null && class310.world != null) {
         HashSet var2 = new HashSet();
         ArrayDeque var3 = new ArrayDeque();
         ArrayList var4 = new ArrayList();
         HashSet var5 = new HashSet();

         for (Direction var9 : Direction.values()) {
            BlockPos var10 = var1.offset(var9);
            if (this.check3(var10) && var2.add(var10.asLong())) {
               var3.add(var10);
            }
         }

         int var21 = this.val20.getObject();
         int var22 = this.val21.getObject();
         int var23 = var1.getX();
         int var24 = var1.getY();
         int var25 = var1.getZ();
         int var11 = var23;
         int var12 = var24;
         int var13 = var25;

         while (!var3.isEmpty() && var4.size() < var21) {
            BlockPos var14 = (BlockPos)var3.poll();
            var4.add(var14);
            var5.add(var14.asLong());
            var23 = Math.min(var23, var14.getX());
            var11 = Math.max(var11, var14.getX());
            var24 = Math.min(var24, var14.getY());
            var12 = Math.max(var12, var14.getY());
            var25 = Math.min(var25, var14.getZ());
            var13 = Math.max(var13, var14.getZ());

            for (Direction var18 : Direction.values()) {
               BlockPos var19 = var14.offset(var18);
               if (this.check3(var19)
                  && Math.abs(var19.getX() - var1.getX()) <= var22
                  && Math.abs(var19.getY() - var1.getY()) <= var22
                  && Math.abs(var19.getZ() - var1.getZ()) <= var22
                  && this.check(var19)
                  && var2.add(var19.asLong())) {
                  var3.add(var19);
               }
            }
         }

         ArrayList var26 = var4;
         if (this.val19.getObject() == PrimeBaseEspModule.State7.Shell) {
            var26 = new ArrayList();

            for (BlockPos var28 : (Iterable<BlockPos>)(Object)(var4)) {
               for (Direction var20 : Direction.values()) {
                  if (!var5.contains(var28.offset(var20).asLong())) {
                     var26.add(var28);
                     break;
                  }
               }
            }
         }

         new Inner6(this.listOf(var26), new BlockPos(var23, var24, var25), new BlockPos(var11, var12, var13), var4.size());
      } else {
         return new Inner6(List.of(), var1, var1, 0);
      }
      return null;
   }

   private boolean check(BlockPos var1) {
      if (!this.val22.getObject()) {
         return true;
      } else {
         int var2 = this.val23.getObject();
         return this.check2(var1, 1, 0, var2) && this.check2(var1, 0, 1, var2);
      }
   }

   private boolean check2(BlockPos var1, int var2, int var3, int var4) {
      int var5 = 1;

      for (int var6 = 1; var5 < var4 && this.check3(var1.add(var2 * var6, 0, var3 * var6)); var6++) {
         var5++;
      }

      for (int var7 = 1; var5 < var4 && this.check3(var1.add(-var2 * var7, 0, -var3 * var7)); var7++) {
         var5++;
      }

      return var5 >= var4;
   }

   private boolean check3(BlockPos var1) {
      return class310.world != null && class310.world.getBlockState(var1).isAir();
   }

   private void run3(ActivityChunkFinderModuleData var1, PrimeBaseEspModule.Inner6 var2) {
      if (var2 != null && !var2.boxes.isEmpty()) {
         ActivityChunkFinderModuleEntry2 var3 = this.val24.getObject();
         ActivityChunkFinderModuleEntry2 var4 = this.val25.getObject();
         if (this.val19.getObject() == PrimeBaseEspModule.State7.Bounds) {
            var1.val
               .run(
                  var2.min.getX(),
                  var2.min.getY(),
                  var2.min.getZ(),
                  var2.max.getX() + 1.0,
                  var2.max.getY() + 1.0,
                  var2.max.getZ() + 1.0,
                  var3,
                  var4,
                  RenderMode.Both,
                  0
               );
         } else {
            for (double[] var6 : var2.boxes) {
               var1.val.run(var6[0], var6[1], var6[2], var6[3], var6[4], var6[5], var3, var4, RenderMode.Both, 0);
            }
         }
      }
   }

   private List<double[]> listOf(Collection<BlockPos> var1) {
      java.util.HashMap<Integer, java.util.Set<Long>> var2 = new HashMap<>();

      for (BlockPos var4 : var1) {
         ((java.util.Set)var2.computeIfAbsent(var4.getY(), a0x -> PrimeBaseEspModule.setOf(a0x))).add(longOf(var4.getX(), var4.getZ()));
      }

      HashMap var17 = new HashMap();

      for (Entry var5 : (Iterable<Entry>)(Object)(var2.entrySet())) {
         Set var6 = (Set)var5.getValue();
         HashSet var7 = new HashSet();
         ArrayList var8 = new ArrayList(var6);
         var8.sort(null);

         for (Long var10 : (Iterable<Long>)(Object)(var8)) {
            if (var6.contains(var10)) {
               int var11 = (int)(var10 >> 32);
               int var12 = (int)(var10 & 4294967295L);
               int var13 = 1;

               while (var6.contains(longOf(var11 + var13, var12))) {
                  var13++;
               }

               int var14 = 1;

               while (this.check4(var6, var11, var12 + var14, var13)) {
                  var14++;
               }

               for (int var15 = 0; var15 < var13; var15++) {
                  for (int var16 = 0; var16 < var14; var16++) {
                     var6.remove(longOf(var11 + var15, var12 + var16));
                  }
               }

               var7.add(new Inner4(var11, var12, var13, var14));
            }
         }

         var17.put((Integer)var5.getKey(), var7);
      }

      ArrayList var19 = new ArrayList();
      ArrayList var20 = new ArrayList(var17.keySet());
      var20.sort(null);

      for (int var22 : (int[])(Object)var20) {
         for (PrimeBaseEspModule.Inner4 var24 : (Iterable<PrimeBaseEspModule.Inner4>)(Object)(new ArrayList((Collection)var17.get(var22)))) {
            if (((Set)var17.get(var22)).remove(var24)) {
               int var25 = 1;

               while (var17.containsKey(var22 + var25) && ((Set)var17.get(var22 + var25)).remove(var24)) {
                  var25++;
               }

               var19.add(new double[]{var24.intVal, var22, var24.intVal2, var24.intVal + var24.intVal3, var22 + var25, var24.intVal2 + var24.intVal4});
            }
         }
      }

      return var19;
   }

   private boolean check4(Set<Long> var1, int var2, int var3, int var4) {
      for (int var5 = 0; var5 < var4; var5++) {
         if (!var1.contains(longOf(var2 + var5, var3))) {
            return false;
         }
      }

      return true;
   }

   private static long longOf(int var0, int var1) {
      return (long)var0 << 32 | var1 & 4294967295L;
   }

   private SoundEvent getclass34146() {
      return switch ((PrimeBaseEspModule.State2)this.val16.getObject()) {
         case Bell -> SoundEvents.BLOCK_BELL_USE;
         case Chime -> (SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value();
         case LevelUp -> SoundEvents.ENTITY_PLAYER_LEVELUP;
         case Ping -> (SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_PLING.value();
         case Conduit -> SoundEvents.BLOCK_CONDUIT_ACTIVATE;
         default -> (SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_HAT.value();
      };
   }

   @InternalHelper5
   private void run4(ActivityChunkFinderModuleData var1) {
      if (this.val26.getObject() || this.val30.getObject() || this.val18.getObject()) {
         if (class310.player != null) {
            for (PrimeBaseEspModule.Inner3 var3 : this.map.values()) {
               if (this.val18.getObject()) {
                  this.run3(var1, var3.space);
               }

               double var4 = var3.intVal << 4;
               double var6 = var3.intVal2 << 4;

               double var8 = switch ((PrimeBaseEspModule.State5)this.val27.getObject()) {
                  case AtPlayer -> class310.player.getY();
                  case Fixed -> this.val28.getObject().intValue();
                  default -> var3.intVal3 == Integer.MIN_VALUE ? class310.player.getY() : var3.intVal3;
               };
               if (this.val26.getObject()) {
                  var1.val
                     .run(
                        var4,
                        var8,
                        var6,
                        var4 + 16.0,
                        var8 + this.val29.getObject().intValue(),
                        var6 + 16.0,
                        this.val31.getObject(),
                        this.val32.getObject(),
                        RenderMode.Both,
                        0
                     );
               }

               if (this.val30.getObject()) {
                  var1.val
                     .run6(
                        BlockEspPlusModuleData.class243.x,
                        BlockEspPlusModuleData.class243.y,
                        BlockEspPlusModuleData.class243.z,
                        var4 + 8.0,
                        var8,
                        var6 + 8.0,
                        this.val32.getObject()
                     );
               }
            }
         }
      }
   }

   private static Set setOf(Integer var0) {
      return new HashSet<>();
   }

   private static PrimeBaseEspModule.Inner1 valOf3(Long var0) {
      return new PrimeBaseEspModule.Inner1();
   }

   private Boolean getBoolean() {
      return this.val26.getObject() && this.val27.getObject() == PrimeBaseEspModule.State5.Fixed;
   }

   private Boolean getBoolean2() {
      return this.val18.getObject() && this.val22.getObject();
   }

   final static class Inner1 {
      int intVal;
      int intVal2;
      int intVal3;
      int intVal4;
      int intVal5;
      int intVal6 = Integer.MIN_VALUE;
      BlockPos class2338;

      void run(BlockPos var1) {
         if (this.intVal6 == Integer.MIN_VALUE) {
            this.intVal6 = var1.getY();
            this.class2338 = var1.toImmutable();
         }
      }
   }

   final class Inner3 {
      final int intVal;
      final int intVal2;
      final int intVal3;
      private String reason;
      final PrimeBaseEspModule.Inner6 space;

      Inner3(int var1, int var2, int var3, String var4, PrimeBaseEspModule.Inner6 var5) {
         this.intVal = var1;
         this.intVal2 = var2;
         this.intVal3 = var3;
         this.reason = var4;
         this.space = var5;
      }

      public int getInt() {
         return this.intVal;
      }

      public int getInt2() {
         return this.intVal2;
      }

      public int getInt3() {
         return this.intVal3;
      }

      public String reason() {
         return this.reason;
      }

      public PrimeBaseEspModule.Inner6 space() {
         return this.space;
      }
   }

   final class Inner4 {
      final int intVal;
      final int intVal2;
      final int intVal3;
      final int intVal4;

      Inner4(int var1, int var2, int var3, int var4) {
         this.intVal = var1;
         this.intVal2 = var2;
         this.intVal3 = var3;
         this.intVal4 = var4;
      }

      public int getInt() {
         return this.intVal;
      }

      public int getInt2() {
         return this.intVal2;
      }

      public int getInt3() {
         return this.intVal3;
      }

      public int getInt4() {
         return this.intVal4;
      }
   }

   final class Inner6 {
      final List<double[]> boxes;
      final BlockPos min;
      final BlockPos max;
      private int blocks;

      Inner6(List<double[]> var1, BlockPos var2, BlockPos var3, int var4) {
         this.boxes = var1;
         this.min = var2;
         this.max = var3;
         this.blocks = var4;
      }

      public List<double[]> boxes() {
         return this.boxes;
      }

      public BlockPos min() {
         return this.min;
      }

      public BlockPos max() {
         return this.max;
      }

      public int blocks() {
         return this.blocks;
      }
   }

   public enum State2 {
      Tick,
      Bell,
      Chime,
      LevelUp,
      Ping,
      Conduit;

      private static PrimeBaseEspModule.State2[] getValArray() {
         return new PrimeBaseEspModule.State2[]{Tick, Bell, Chime, LevelUp, Ping, Conduit};
      }
   }

   public enum State5 {
      AtBlock,
      AtPlayer,
      Fixed;

      private static PrimeBaseEspModule.State5[] getValArray() {
         return new PrimeBaseEspModule.State5[]{AtBlock, AtPlayer, Fixed};
      }
   }

   public enum State7 {
      Bounds,
      Shell,
      Full;

      private static PrimeBaseEspModule.State7[] getValArray() {
         return new PrimeBaseEspModule.State7[]{Bounds, Shell, Full};
      }
   }
}
