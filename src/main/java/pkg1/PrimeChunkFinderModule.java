package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.block.BlockState;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.chunk.WorldChunk;

public final class PrimeChunkFinderModule extends Module {
   private static final int intVal = 32;
   private static final int intVal2 = 1;
   private static final int intVal3 = 1;
   private static final long longVal = 10000L;
   private static final int intVal4 = 2;
   private static final ActivityChunkFinderModuleEntry2 val_2 = new ActivityChunkFinderModuleEntry2(42, 235, 94, 50);
   private static final ActivityChunkFinderModuleEntry2 val2_2 = new ActivityChunkFinderModuleEntry2(78, 255, 126, 210);
   private static final ActivityChunkFinderModuleEntry2 val3_2 = new ActivityChunkFinderModuleEntry2(42, 235, 94, 210);
   private static final ActivityChunkFinderModuleEntry2 val4 = new ActivityChunkFinderModuleEntry2(255, 42, 54, 50);
   private static final ActivityChunkFinderModuleEntry2 val5 = new ActivityChunkFinderModuleEntry2(255, 72, 82, 220);
   private static final ActivityChunkFinderModuleEntry2 val6 = new ActivityChunkFinderModuleEntry2(255, 42, 54, 220);
   private static final int[][] intArray = new int[][]{{8, 8}, {4, 4}, {12, 4}, {4, 12}, {12, 12}, {8, 4}, {8, 12}, {4, 8}, {12, 8}};
   private static final Set<String> set = Set.of(
      "entity.player",
      "item.armor.equip",
      "item.bucket",
      "item.crossbow",
      "item.elytra",
      "item.firecharge",
      "item.flintandsteel",
      "entity.arrow.shoot",
      "entity.fishing_bobber",
      ".place",
      ".break"
   );
   private static final Set<String> set2 = Set.of(
      "bogged",
      "breeze",
      "cave_spider",
      "creaking",
      "creeper",
      "drowned",
      "elder_guardian",
      "enderman",
      "evoker",
      "guardian",
      "husk",
      "phantom",
      "pillager",
      "ravager",
      "silverfish",
      "skeleton",
      "slime",
      "spider",
      "stray",
      "vex",
      "vindicator",
      "warden",
      "witch",
      "zombie",
      "zombie_villager"
   );
   private static final Set<String> set3 = Set.of("bee", "dolphin", "goat", "iron_golem", "llama", "panda", "polar_bear", "pufferfish", "trader_llama", "wolf");
   private static final Set<String> set4 = Set.of(
      "allay",
      "armadillo",
      "axolotl",
      "bat",
      "bee",
      "camel",
      "cat",
      "chicken",
      "cod",
      "cow",
      "dolphin",
      "donkey",
      "fox",
      "frog",
      "glow_squid",
      "goat",
      "horse",
      "iron_golem",
      "llama",
      "mooshroom",
      "mule",
      "ocelot",
      "panda",
      "parrot",
      "pig",
      "polar_bear",
      "pufferfish",
      "rabbit",
      "salmon",
      "sheep",
      "sniffer",
      "snow_golem",
      "squid",
      "tadpole",
      "trader_llama",
      "tropical_fish",
      "turtle",
      "villager",
      "wandering_trader",
      "wolf"
   );
   private final ActivityChunkFinderModuleEntry val7 = this.val2.getVal();
   private final Setting<List<EntityType<?>>> val8 = this.val7
      .addSetting(
         new PrimeChunkFinderModuleHelper2()
            .valOf("mobs")
            .valOf2("Choose which Overworld mobs can flag a prime chunk.")
            .valOf3(getList())
            .valOf5(PrimeChunkFinderModule::check4)
            .getVal()
      );
   private final Setting<Integer> val9 = this.val7
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("hostile-threshold")
            .valOf2("Selected hostile mobs required before a region is flagged.")
            .valOf3(2)
            .valOf4(1, 10)
            .valOf5(1, 10)
            .getVal()
      );
   private final Setting<Integer> val10 = this.val7
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("friendly-threshold")
            .valOf2("Selected friendly mobs required before a region is flagged.")
            .valOf3(4)
            .valOf4(1, 10)
            .valOf5(1, 10)
            .getVal()
      );
   private final Setting<Boolean> val11 = this.val7
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("debug")
            .valOf2("Detect real player network and sound signals below Y 0 using passive methods, without rendering Player ESP.")
            .valOf3(false)
            .getVal()
      );
   private final Setting<Boolean> val12 = this.val2
      .valOf("Notifications")
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("sound-alerts").valOf2("Play an alert when a new prime chunk is found.").valOf3(true).getVal());
   private final Map<ChunkPos, Double> map = new LinkedHashMap<>();
   private final Map<ChunkPos, Double> map2 = new LinkedHashMap<>();
   private final Map<Long, Integer> map3 = new LinkedHashMap<>();
   private final Map<Long, Long> map4 = new LinkedHashMap<>();
   private final Map<Integer, PrimeChunkFinderModule.Inner1> map5 = new LinkedHashMap<>();
   private int intVal5;
   private int intVal6;
   private int intVal7;
   private Object object;

   public PrimeChunkFinderModule() {
      super(SwyzzyAddon.val2, "prime-chunk-finder", "Highlights nearby prime-coordinate chunks with a translucent green land plate.");
   }

   @Override
   public void run6() {
      this.run19();
   }

   @Override
   public void run7() {
      this.run19();
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.world != null && class310.player != null) {
         if (this.intVal6 > 0 && --this.intVal6 == 0) {
            this.run16();
            if (this.intVal7 > 0) {
               this.intVal7--;
            }

            if (this.intVal7 > 0) {
               this.intVal6 = 4;
            }
         }

         if (this.object != class310.world) {
            this.object = class310.world;
            this.run12();
         }

         this.run13();
         ChunkPos var2 = class310.player.getChunkPos();
         if (!Boolean.TRUE.equals(this.val11.getObject())) {
            this.run20();
         }

         if (++this.intVal5 >= 2) {
            this.intVal5 = 0;
            this.run5(var2, true);
         }
      } else {
         this.object = null;
         this.intVal5 = 0;
         this.intVal6 = 0;
         this.intVal7 = 0;
         this.run20();
      }
   }

   private void run12() {
      if (class310.world != null && class310.player != null) {
         this.run2(this.map);
         this.run2(this.map2);
         this.intVal5 = 0;
      } else {
         this.intVal5 = 0;
      }
   }

   private void run2(Map<ChunkPos, Double> var1) {
      for (Entry var3 : (Iterable<Entry>)(Object)(new ArrayList(var1.entrySet()))) {
         ChunkPos var4 = (ChunkPos)var3.getKey();
         WorldChunk var5 = this.class2818Of(var4);
         if (var5 != null) {
            double var6 = this.doubleOf(class310.world, var5, var4);
            if (!Double.isNaN(var6)) {
               var1.put(var4, var6);
            }
         } else {
            var1.remove(var4);
         }
      }
   }

   private void run5(ChunkPos var1, boolean var2) {
      ClientWorld var3 = class310.world;
      if (var3 != null && World.OVERWORLD.equals(var3.getRegistryKey())) {
         ArrayList var4 = new ArrayList();
         ArrayList var5 = new ArrayList();

         for (Entity var7 : var3.getEntities()) {
            if (Boolean.TRUE.equals(this.val11.getObject())) {
               this.run3(var7, var1);
            }

            int var8 = this.intOf(var7, var1);
            if (var8 == 1) {
               var4.add(var7.getChunkPos());
            } else if (var8 == 2) {
               var5.add(var7.getChunkPos());
            }
         }

         for (Entry var16 : this.map5.entrySet()) {
            PrimeChunkFinderModule.Inner1 var18 = (PrimeChunkFinderModule.Inner1)var16.getValue();
            if (var3.getEntityById((Integer)var16.getKey()) == null && this.val8.getObject().contains(var18.type())) {
               int var9 = intOf2(var18.type());
               if (var9 == 1) {
                  var4.add(var18.chunk());
               } else if (var9 == 2) {
                  var5.add(var18.chunk());
               }
            }
         }

         LinkedHashSet var15 = new LinkedHashSet();
         if (var4.size() >= this.val9.getObject()) {
            var15.addAll(var4);
         }

         if (var5.size() >= this.val10.getObject()) {
            var15.addAll(var5);
         }

         ArrayList var17 = new ArrayList(var15);
         var17.sort(Comparator.comparingLong(var905 -> PrimeChunkFinderModule.longOf(var1, (ChunkPos)var905)));

         for (ChunkPos var20 : (Iterable<ChunkPos>)(Object)(var17)) {
            if (!this.map.containsKey(var20)) {
               WorldChunk var10 = this.class2818Of(var20);
               if (var10 != null) {
                  double var11 = this.doubleOf(var3, var10, var20);
                  if (!Double.isNaN(var11)) {
                     boolean var13 = this.check(var20);
                     this.map.put(var20, var11);
                     if (var2 && !var13) {
                        this.run15(var20);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean check(ChunkPos var1) {
      for (ChunkPos var3 : this.map.keySet()) {
         if (Math.abs(var3.x - var1.x) <= 1 && Math.abs(var3.z - var1.z) <= 1) {
            return true;
         }
      }

      return false;
   }

   private List<List<ChunkPos>> listOf(Set<ChunkPos> var1) {
      LinkedHashSet var2 = new LinkedHashSet(var1);
      ArrayList var3 = new ArrayList();

      while (!var2.isEmpty()) {
         ChunkPos var4 = (ChunkPos)var2.iterator().next();
         var2.remove(var4);
         ArrayList var5 = new ArrayList();
         var5.add(var4);

         for (int var6 = 0; var6 < var5.size(); var6++) {
            ChunkPos var7 = (ChunkPos)var5.get(var6);
            ArrayList var8 = new ArrayList();

            for (ChunkPos var10 : (Iterable<ChunkPos>)(Object)(var2)) {
               if (Math.abs(var10.x - var7.x) <= 1 && Math.abs(var10.z - var7.z) <= 1) {
                  var8.add(var10);
               }
            }

            var2.removeAll(var8);
            var5.addAll(var8);
         }

         var3.add(var5);
      }

      return var3;
   }

   private int intOf(Entity var1, ChunkPos var2) {
      if (var1 != null && var1 != class310.player && var1.isAlive()) {
         ChunkPos var3 = var1.getChunkPos();
         if (Math.abs(var3.x - var2.x) <= 32 && Math.abs(var3.z - var2.z) <= 32) {
            EntityType var4 = var1.getType();
            return !this.val8.getObject().contains(var4) ? 0 : intOf2(var4);
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   private static int intOf2(EntityType<?> var0) {
      String var1 = stringOf(var0);
      if (!set2.contains(var1) && !set3.contains(var1)) {
         return set4.contains(var1) ? 2 : 0;
      } else {
         return 1;
      }
   }

   private void run13() {
      ClientWorld var1 = class310.world;
      if (var1 != null) {
         this.map.keySet().removeIf(this::check7);
         this.map2.keySet().removeIf(this::check6);
         this.map3.keySet().removeIf(this::check2);
         this.map4.keySet().removeIf(this::check2);
         this.map5.entrySet().removeIf(this::check5);
      }
   }

   private boolean check2(long var1) {
      return this.class2818Of(new ChunkPos(ChunkPos.getPackedX(var1), ChunkPos.getPackedZ(var1))) == null;
   }

   private void run3(Entity var1, ChunkPos var2) {
      if (var1 != null && var1 != class310.player && var1.isAlive() && !(var1.getY() >= 0.0)) {
         ChunkPos var3 = var1.getChunkPos();
         if (Math.abs(var3.x - var2.x) <= 32 && Math.abs(var3.z - var2.z) <= 32 && var1 instanceof PlayerEntity) {
            this.run14(var3);
         }
      }
   }

   @InternalHelper5
   private void run4(PacketChunkFinderModuleData var1) {
      if (class310.world != null && class310.player != null && var1.object != null) {
         Object var2 = var1.object;
         if (var2 instanceof Packet) {
            if (var2 instanceof EntitySpawnS2CPacket var3) {
               this.run8(var3);
            }

            if (Boolean.TRUE.equals(this.val11.getObject())) {
               if (var2 instanceof PlaySoundFromEntityS2CPacket var8) {
                  this.run9(class310.world.getEntityById(var8.getEntityId()));
               } else if (var2 instanceof EntityStatusS2CPacket var4) {
                  this.run9(var4.getEntity(class310.world));
               } else if (var2 instanceof PlaySoundS2CPacket var5 && var5.getY() < 0.0) {
                  Identifier var6 = Registries.SOUND_EVENT.getId((SoundEvent)var5.getSound().value());
                  String var7 = var6 == null ? "" : var6.toString().toLowerCase(Locale.ROOT);
                  if (this.check3(var7)) {
                     this.run10(var5.getX(), var5.getY(), var5.getZ());
                  }
               }
            }
         }
      }
   }

   private void run8(EntitySpawnS2CPacket var1) {
      EntityType var2 = var1.getEntityType();
      if (check4(var2)) {
         BlockPos var3 = new BlockPos((int)Math.floor(var1.getX()), (int)Math.floor(var1.getY()), (int)Math.floor(var1.getZ()));
         this.map5.put(var1.getEntityId(), new Inner1(var2, new ChunkPos(var3)));
      }
   }

   private void run9(Entity var1) {
      if (var1 instanceof PlayerEntity && var1 != class310.player && var1.isAlive() && var1.getY() < 0.0) {
         ChunkPos var2 = var1.getChunkPos();
         ChunkPos var3 = class310.player.getChunkPos();
         if (Math.abs(var2.x - var3.x) <= 32 && Math.abs(var2.z - var3.z) <= 32) {
            this.run14(var2);
         }
      }
   }

   private void run10(double var1, double var3, double var5) {
      ChunkPos var7 = new ChunkPos(new BlockPos((int)Math.floor(var1), (int)Math.floor(var3), (int)Math.floor(var5)));
      ChunkPos var8 = class310.player.getChunkPos();
      if (Math.abs(var7.x - var8.x) <= 32 && Math.abs(var7.z - var8.z) <= 32) {
         this.run11(var7, 1);
      }
   }

   private boolean check3(String var1) {
      for (String var3 : set) {
         if (var1.contains(var3)) {
            return true;
         }
      }

      return false;
   }

   private void run11(ChunkPos var1, int var2) {
      if (var2 > 0 && !this.map2.containsKey(var1)) {
         long var3 = var1.toLong();
         long var5 = System.currentTimeMillis();
         long var7 = this.map4.getOrDefault(var3, 0L);
         int var9 = var5 - var7 > 10000L ? var2 : this.map3.getOrDefault(var3, 0) + var2;
         this.map4.put(var3, var5);
         this.map3.put(var3, var9);
         if (var9 >= 2) {
            this.run14(var1);
         }
      }
   }

   private void run14(ChunkPos var1) {
      if (!this.map2.containsKey(var1) && class310.world != null) {
         WorldChunk var2 = this.class2818Of(var1);
         if (var2 != null) {
            double var3 = this.doubleOf(class310.world, var2, var1);
            if (!Double.isNaN(var3)) {
               this.map2.put(var1, var3);
            }
         }
      }
   }

   private static boolean check4(EntityType<?> var0) {
      if (var0 == null) {
         return false;
      } else {
         String var1 = stringOf(var0);
         return set2.contains(var1) || set3.contains(var1) || set4.contains(var1);
      }
   }

   private static List<EntityType<?>> getList() {
      ArrayList var0 = new ArrayList();

      for (EntityType var2 : Registries.ENTITY_TYPE) {
         String var3 = stringOf(var2);
         if (set2.contains(var3)
            || var3.equals("cod")
            || var3.equals("pufferfish")
            || var3.equals("salmon")
            || var3.equals("tropical_fish")
            || var3.equals("glow_squid")
            || var3.equals("squid")) {
            var0.add(var2);
         }
      }

      return var0;
   }

   private double doubleOf(ClientWorld var1, WorldChunk var2, ChunkPos var3) {
      for (int[] var7 : intArray) {
         int var8 = var7[0];
         int var9 = var7[1];
         int var10 = var2.sampleHeightmap(Type.MOTION_BLOCKING, var8, var9);
         int var11 = var3.getStartX() + var8;
         int var12 = var3.getStartZ() + var9;

         for (int var13 = var10 - 1; var13 >= var2.getBottomY(); var13--) {
            BlockPos var14 = new BlockPos(var11, var13, var12);
            BlockState var15 = var1.getBlockState(var14);
            if (!var15.isAir() && var15.getFluidState().isEmpty()) {
               return var13 + 1.035;
            }
         }
      }

      return Double.NaN;
   }

   private void run15(ChunkPos var1) {
      int var2 = var1.getStartX() + 8;
      int var3 = var1.getStartZ() + 8;
      ChunkFinderV2ModuleUtil.run10("Prime Chunk Finder", "X : " + var2 + "  Z : " + var3);
      if (Boolean.TRUE.equals(this.val12.getObject())) {
         this.intVal7++;
         if (this.intVal6 <= 0) {
            this.intVal6 = 2;
         }
      }
   }

   private void run16() {
      if (Boolean.TRUE.equals(this.val12.getObject()) && class310.player != null && class310.getSoundManager() != null) {
         class310.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F));
      }
   }

   @InternalHelper5
   private void run17(ActivityChunkFinderModuleData var1) {
      if (class310.world != null && this.object == class310.world) {
         LinkedHashSet var2 = new LinkedHashSet<>(this.map.keySet());
         if (Boolean.TRUE.equals(this.val11.getObject())) {
            var2.removeAll(this.map2.keySet());
         }

         this.run18(var1, this.map, var2, val_2, val2_2, val3_2);
         if (Boolean.TRUE.equals(this.val11.getObject())) {
            this.run18(var1, this.map2, this.map2.keySet(), val4, val5, val6);
         }
      }
   }

   private void run18(
      ActivityChunkFinderModuleData var1,
      Map<ChunkPos, Double> var2,
      Set<ChunkPos> var3,
      ActivityChunkFinderModuleEntry2 var4,
      ActivityChunkFinderModuleEntry2 var5,
      ActivityChunkFinderModuleEntry2 var6
   ) {
      for (List var8 : this.listOf(var3)) {
         int var9 = Integer.MAX_VALUE;
         int var10 = Integer.MIN_VALUE;
         int var11 = Integer.MAX_VALUE;
         int var12 = Integer.MIN_VALUE;
         double var13 = 0.0;

         for (ChunkPos var16 : (Iterable<ChunkPos>)(Object)(var8)) {
            var9 = Math.min(var9, var16.x);
            var10 = Math.max(var10, var16.x);
            var11 = Math.min(var11, var16.z);
            var12 = Math.max(var12, var16.z);
            var13 += var2.getOrDefault(var16, 0.0);
         }

         double var25 = var13 / var8.size();
         double var17 = var9 * 16.0 + 1.0;
         double var19 = var11 * 16.0 + 1.0;
         double var21 = (var10 + 1) * 16.0 - 1.0;
         double var23 = (var12 + 1) * 16.0 - 1.0;
         var1.val.run(var17, var25, var19, var21, var25 + 0.065, var23, var4, var5, RenderMode.Both, 0);
         var1.val
            .run6(
               BlockEspPlusModuleData.class243.x,
               BlockEspPlusModuleData.class243.y,
               BlockEspPlusModuleData.class243.z,
               (var17 + var21) * 0.5,
               var25 + 0.1,
               (var19 + var23) * 0.5,
               var6
            );
      }
   }

   private WorldChunk class2818Of(ChunkPos var1) {
      if (class310.world == null) {
         return null;
      } else {
         WorldChunk var2 = class310.world.getChunkManager().getWorldChunk(var1.x, var1.z);
         return var2 != null && !var2.isEmpty() ? var2 : null;
      }
   }

   private void run19() {
      this.map.clear();
      this.run20();
      this.intVal5 = 0;
      this.intVal6 = 0;
      this.intVal7 = 0;
      this.object = null;
      this.map5.clear();
   }

   private void run20() {
      this.map2.clear();
      this.map3.clear();
      this.map4.clear();
   }

   private static String stringOf(EntityType<?> var0) {
      return Registries.ENTITY_TYPE.getId(var0).getPath();
   }

   private boolean check5(Map.Entry<Integer, PrimeChunkFinderModule.Inner1> var1) {
      return this.class2818Of(var1.getValue().chunk()) == null;
   }

   private boolean check6(ChunkPos var1) {
      return this.class2818Of(var1) == null;
   }

   private boolean check7(ChunkPos var1) {
      return this.class2818Of(var1) == null;
   }

   private static long longOf(ChunkPos var0, ChunkPos var1) {
      long var2 = var1.x - var0.x;
      long var4 = var1.z - var0.z;
      return var2 * var2 + var4 * var4;
   }

   final class Inner1 {
      private final EntityType<?> type;
      private ChunkPos chunk;

      Inner1(EntityType<?> var1, ChunkPos var2) {
         this.type = var1;
         this.chunk = var2;
      }

      @Override
      public boolean equals(Object var1) {
         if (this == var1) {
            return true;
         } else {
            return !(var1 instanceof PrimeChunkFinderModule.Inner1 var2)
               ? false
               : Objects.equals(this.type, var2.type) && Objects.equals(this.chunk, var2.chunk);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.type, this.chunk);
      }

      public EntityType<?> type() {
         return this.type;
      }

      public ChunkPos chunk() {
         return this.chunk;
      }
   }
}
