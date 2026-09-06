package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import util.Utils_2;

public final class ChunkActivityTrackerModule extends Module {
   private static final int intVal = 32;
   private static final int intVal2 = 12;
   private static final int intVal3 = 32;
   private static final int intVal4 = 6;
   private static final int intVal5 = 23;
   private static final int intVal6 = 120;
   private static final double doubleVal = 0.12;
   private static final ActivityChunkFinderModuleEntry2 val_2 = new ActivityChunkFinderModuleEntry2(170, 60, 255, 40);
   private static final ActivityChunkFinderModuleEntry2 val2_2 = new ActivityChunkFinderModuleEntry2(170, 60, 255, 255);
   private static final ActivityChunkFinderModuleEntry2 val3_2 = new ActivityChunkFinderModuleEntry2(55, 12, 95, 25);
   private static final ActivityChunkFinderModuleEntry2 val4 = new ActivityChunkFinderModuleEntry2(95, 25, 160, 200);
   private static final ActivityChunkFinderModuleEntry2 val5 = new ActivityChunkFinderModuleEntry2(0, 0, 0, 255);
   private static final Map<Block, Set<Block>> map = getMap();
   private final ActivityChunkFinderModuleEntry val6 = this.val2.getVal();
   private final Setting<Integer> val7 = this.val6
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("usage")
            .valOf2("Higher values use less memory and CPU, but scan a smaller area less often.")
            .valOf3(0)
            .valOf4(0, 10)
            .valOf5(0, 10)
            .getVal()
      );
   private final Setting<Boolean> val8 = this.val6
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("show-mapped-chunks").valOf2("Outlines which chunks are currently fingerprinted.").valOf3(true).getVal()
      );
   private final ChunkActivityTrackerModule.Inner1 val9 = new ChunkActivityTrackerModule.Inner1();
   private final Set<Long> set = ConcurrentHashMap.newKeySet();
   private final Set<Long> set2 = ConcurrentHashMap.newKeySet();
   private volatile List<ChunkActivityTrackerModule.Inner2> list = List.of();
   private volatile Set<Long> set3 = Set.of();
   private volatile long[] longArray = new long[0];
   private volatile int[] intArray = new int[0];
   private int intVal7 = -1;
   private int intVal8;
   private ChunkPos class1923;
   private World class1937;
   private String string_2 = "";
   private int intVal9;
   private int intVal10 = -1;
   private ExecutorService executorService;

   public ChunkActivityTrackerModule() {
      super(SwyzzyAddon.val2, "chunk-activity-tracker", "Chunk Activity Tracker");
      Utils_2.run(this, "Chunk Activity Tracker");
   }

   private static Map<Block, Set<Block>> getMap() {
      HashMap var0 = new HashMap();
      run(var0, Blocks.KELP_PLANT, Blocks.KELP);
      run(var0, Blocks.WATER, Blocks.KELP);
      run(var0, Blocks.DIRT, Blocks.GRASS_BLOCK);
      run(var0, Blocks.FIRE, Blocks.AIR);
      run(var0, Blocks.COCOA, Blocks.COCOA);
      run(var0, Blocks.SWEET_BERRY_BUSH, Blocks.SWEET_BERRY_BUSH);
      run(var0, Blocks.BEEHIVE, Blocks.BEEHIVE);
      run(var0, Blocks.BEE_NEST, Blocks.BEE_NEST);

      for (Block var4 : new Block[]{Blocks.AIR, Blocks.CAVE_AIR}) {
         run(var0, var4, Blocks.VINE, Blocks.SUGAR_CANE, Blocks.SNOW, Blocks.BAMBOO, Blocks.FIRE);
      }

      for (Block var8 : new Block[]{
         Blocks.OAK_LEAVES,
         Blocks.SPRUCE_LEAVES,
         Blocks.BIRCH_LEAVES,
         Blocks.JUNGLE_LEAVES,
         Blocks.ACACIA_LEAVES,
         Blocks.DARK_OAK_LEAVES,
         Blocks.MANGROVE_LEAVES,
         Blocks.CHERRY_LEAVES,
         Blocks.PALE_OAK_LEAVES,
         Blocks.AZALEA_LEAVES,
         Blocks.FLOWERING_AZALEA_LEAVES
      }) {
         run(var0, var8, Blocks.AIR);
      }

      return var0;
   }

   private static void run(Map<Block, Set<Block>> var0, Block var1, Block... var2) {
      var0.computeIfAbsent(var1, ChunkActivityTrackerModule::setOf4).addAll(Set.of(var2));
   }

   @Override
   public void run6() {
      String var1 = this.getString();
      if (!var1.equals(this.string_2)) {
         this.val9.run3();
         this.string_2 = var1;
      }

      this.set.clear();
      this.set2.clear();
      this.list = List.of();
      this.set3 = Set.of();
      this.longArray = new long[0];
      this.intArray = new int[0];
      this.intVal7 = -1;
      this.intVal8 = 0;
      this.class1923 = null;
      this.class1937 = null;
      this.intVal9 = 0;
      this.intVal10 = -1;
      this.executorService = Executors.newSingleThreadExecutor(ChunkActivityTrackerModule::threadOf);
   }

   @Override
   public void run7() {
      if (this.executorService != null) {
         this.executorService.shutdownNow();
      }

      this.executorService = null;
   }

   @Override
   public String getString2() {
      return this.list.isEmpty() ? null : String.valueOf(this.list.size());
   }

   private String getString() {
      ServerInfo var1 = class310.getCurrentServerEntry();
      return var1 == null ? "singleplayer" : var1.address;
   }

   private boolean isEnabled3() {
      return class310.world == null || class310.player == null || class310.world.getRegistryKey() != World.OVERWORLD;
   }

   private int getInt() {
      return Math.max(1, 32 - 2 * this.val7.getObject());
   }

   private int getInt2() {
      return class310.options == null ? 12 : Math.max(2, (Integer)class310.options.getViewDistance().getValue());
   }

   public void run2(Runnable var1) {
      ExecutorService var2 = this.executorService;
      if (var2 != null && !var2.isShutdown()) {
         try {
            var2.submit(() -> ChunkActivityTrackerModule.run13(var1));
         } catch (RejectedExecutionException var4) {
         }
      }
   }

   @InternalHelper5
   private void run3(ActivityChunkFinderModuleHelper3 var1) {
      if (!this.isEnabled3()) {
         if (class310.world != this.class1937) {
            this.class1937 = class310.world;
            this.set.clear();
            this.set3 = Set.of();
            this.class1923 = null;
            this.intVal10 = -1;
            this.intVal7 = -1;
            this.intVal8 = 0;
            this.run19();
         }

         ChunkPos var2 = class310.player.getChunkPos();
         int var3 = this.getInt();
         if (!var2.equals(this.class1923) || var3 != this.intVal10) {
            this.set3 = setOf(var2, var3);
            this.intVal10 = var3;
            if (!var2.equals(this.class1923)) {
               this.intVal9++;
            }

            this.class1923 = var2;
         }

         if (++this.intVal8 >= 20) {
            this.intVal8 = 0;
            this.run19();
         }

         if (((Integer)this.val7.getObject()) <= 0 || this.intVal9 >= this.val7.getObject()) {
            this.intVal9 = 0;
            int var4 = this.getInt2() + 1;

            for (int var5 = -var4; var5 <= var4; var5++) {
               for (int var6 = -var4; var6 <= var4; var6++) {
                  int var7 = var2.x + var5;
                  int var8 = var2.z + var6;
                  long var9 = ChunkPos.toLong(var7, var8);
                  if (!this.val9.check(var9) && !this.set.contains(var9) && class310.world.isChunkLoaded(var7, var8)) {
                     WorldChunk var11 = class310.world.getChunk(var7, var8);
                     if (var11 != null) {
                        this.set.add(var9);
                        this.run2(() -> this.run12(var7, var8, var11, var9));
                     }
                  }
               }
            }
         }
      }
   }

   private static Set<Long> setOf(ChunkPos var0, int var1) {
      HashSet var2 = new HashSet();

      for (int var3 = -var1; var3 <= var1; var3++) {
         int var4 = var1 - Math.abs(var3);
         int var5 = Math.max(0, 3 - var4);
         int var6 = var1 - var5;

         for (int var7 = -var6; var7 <= var6; var7++) {
            var2.add(ChunkPos.toLong(var0.x + var7, var0.z + var3));
         }
      }

      return var2;
   }

   private static int[][] intArrayOf(Chunk var0) {
      ChunkSection[] var1 = var0.getSectionArray();
      int[][] var2 = new int[var1.length][];

      for (int var3 = 6; var3 <= 23 && var3 < var1.length; var3++) {
         ChunkSection var4 = var1[var3];
         if (var4 != null && !var4.isEmpty()) {
            int[] var5 = new int[4096];
            int var6 = 0;

            for (int var7 = 0; var7 < 16; var7++) {
               for (int var8 = 0; var8 < 16; var8++) {
                  for (int var9 = 0; var9 < 16; var9++) {
                     var5[var6++] = Block.getRawIdFromState(var4.getBlockState(var9, var7, var8));
                  }
               }
            }

            var2[var3] = var5;
         }
      }

      return var2;
   }

   @InternalHelper5
   private void run4(AmethystChunkFinderModuleHelper var1) {
      if (!this.isEnabled3() && var1.chunk() != null) {
         ChunkPos var2 = var1.chunk().getPos();
         if (this.set3.contains(var2.toLong())) {
            WorldChunk var3 = var1.chunk();
            this.run2(() -> this.run11(var2, var3));
         }
      }
   }

   private void run5(ChunkPos var1, Chunk var2) {
      int[][] var3 = intArrayOf(var2);

      for (long var5 : this.val9.listOf(var1.x, var1.z, var3)) {
         Block var7 = Block.getStateFromRawId(intOf2(var5)).getBlock();
         Block var8 = Block.getStateFromRawId(intOf3(var5)).getBlock();
         if (map.getOrDefault(var7, Set.of()).contains(var8)) {
            this.set2.add(var1.toLong());
            this.run8(var1);
            break;
         }
      }

      this.val9.run(var1.x, var1.z, var3);
   }

   private void run8(ChunkPos var1) {
      ArrayList var2 = new ArrayList();
      ArrayList var3 = new ArrayList();

      for (ChunkActivityTrackerModule.Inner2 var5 : this.list) {
         if (check(var5, var1)) {
            var3.addAll(var5.sightings());
         } else {
            var2.add(var5);
         }
      }

      var3.add(ChunkPos.toLong(var1.x, var1.z));

      while (var3.size() > 32) {
         var3.remove(0);
      }

      var2.add(new Inner2(var3, setOf2(var3)));
      this.list = List.copyOf(var2);
   }

   private static boolean check(ChunkActivityTrackerModule.Inner2 var0, ChunkPos var1) {
      for (long var3 : var0.sightings()) {
         if (Math.abs(ChunkPos.getPackedX(var3) - var1.x) <= 24 && Math.abs(ChunkPos.getPackedZ(var3) - var1.z) <= 24) {
            return true;
         }
      }

      return false;
   }

   private static Set<Long> setOf2(List<Long> var0) {
      Object var1 = null;

      for (int var2 = var0.size() - 1; var2 >= 0; var2--) {
         long var3 = (Long)var0.get(var2);
         Set var5 = setOf3(ChunkPos.getPackedX(var3), ChunkPos.getPackedZ(var3));
         if (var1 == null) {
            var1 = var5;
         } else {
            HashSet var6 = new HashSet((Collection)var1);
            var6.retainAll(var5);
            if (!var6.isEmpty()) {
               var1 = var6;
            }
         }
      }

      return (Set<Long>)(var1 == null ? Set.of() : var1);
   }

   private static Set<Long> setOf3(int var0, int var1) {
      HashSet var2 = new HashSet();

      for (int var3 = -12; var3 <= 12; var3++) {
         for (int var4 = -12; var4 <= 12; var4++) {
            var2.add(ChunkPos.toLong(var0 + var3, var1 + var4));
         }
      }

      return var2;
   }

   private void run19() {
      Set var1 = this.val9.getSet();
      if (var1.size() != this.intVal7) {
         this.intVal7 = var1.size();
         long[] var2 = new long[var1.size()];
         int[] var3 = new int[var2.length];
         int var4 = 0;

         for (long var6 : (long[])(Object)var1) {
            if (var4 == var2.length) {
               break;
            }

            int var8 = ChunkPos.getPackedX(var6);
            int var9 = ChunkPos.getPackedZ(var6);
            int var10 = intOf(var1, var8, var9);
            if (var10 != 120) {
               var2[var4] = var6;
               var3[var4] = var10;
               var4++;
            }
         }

         this.longArray = Arrays.copyOf(var2, var4);
         this.intArray = Arrays.copyOf(var3, var4);
      }
   }

   private static int intOf(Set<Long> var0, int var1, int var2) {
      byte var3 = 0;
      if (var0.contains(ChunkPos.toLong(var1, var2 - 1))) {
         var3 = 8;
      }

      if (var0.contains(ChunkPos.toLong(var1, var2 + 1))) {
         var3 |= 16;
      }

      if (var0.contains(ChunkPos.toLong(var1 - 1, var2))) {
         var3 |= 32;
      }

      if (var0.contains(ChunkPos.toLong(var1 + 1, var2))) {
         var3 |= 64;
      }

      return var3;
   }

   @InternalHelper5
   private void run9(ActivityChunkFinderModuleData var1) {
      if (!this.isEnabled3()) {
         if (this.val8.getObject()) {
            long[] var2 = this.longArray;
            int[] var3 = this.intArray;

            for (int var4 = 0; var4 < var2.length && var4 < var3.length; var4++) {
               double var5 = ChunkPos.getPackedX(var2[var4]) * 16.0;
               double var7 = ChunkPos.getPackedZ(var2[var4]) * 16.0;
               var1.val.run(var5, 63.0, var7, var5 + 16.0, 63.1, var7 + 16.0, val5, val5, RenderMode.Lines, var3[var4]);
            }
         }

         for (ChunkActivityTrackerModule.Inner2 var10 : this.list) {
            HashSet var11 = new HashSet<>(var10.zone());
            var11.removeAll(this.set2);
            run10(var1, var11, val_2, val2_2);
         }

         run10(var1, this.set2, val3_2, val4);
      }
   }

   private static void run10(ActivityChunkFinderModuleData var0, Set<Long> var1, ActivityChunkFinderModuleEntry2 var2, ActivityChunkFinderModuleEntry2 var3) {
      for (long var5 : var1) {
         int var7 = ChunkPos.getPackedX(var5);
         int var8 = ChunkPos.getPackedZ(var5);
         int var9 = intOf(var1, var7, var8);
         double var10 = var7 * 16.0;
         double var12 = var8 * 16.0;
         double var14 = var10 + 16.0;
         double var16 = var12 + 16.0;
         var0.val.run4(var10, 63.0, var12, var14, 63.1, var16, var2, var9 | 120);
         var0.val.run4(var10, 63.0, var12, var14, 63.1, var16, var3, var9 | 2 | 4);
         var0.val.run5(var10, 63.0, var12, var14, 63.1, var16, var3, var9);
         var0.val.run5(var10 + 0.12, 63.0, var12 + 0.12, var14 - 0.12, 63.1, var16 - 0.12, var3, var9);
      }
   }

   static long longOf(int var0, int var1) {
      return (long)var0 << 32 | var1 & 4294967295L;
   }

   private static int intOf2(long var0) {
      return (int)(var0 >> 32);
   }

   private static int intOf3(long var0) {
      return (int)var0;
   }

   private void run11(ChunkPos var1, WorldChunk var2) {
      this.run5(var1, var2);
   }

   private void run12(int var1, int var2, WorldChunk var3, long var4) {
      try {
         this.val9.run(var1, var2, intArrayOf(var3));
      } finally {
         this.set.remove(var4);
      }
   }

   private static void run13(Runnable var0) {
      try {
         if (class310.world != null) {
            var0.run();
         }
      } catch (Throwable var2) {
      }
   }

   private static Thread threadOf(Runnable var0) {
      Thread var1 = new Thread(var0, "swyzzy-chunk-activity");
      var1.setDaemon(true);
      var1.setPriority(3);
      return var1;
   }

   private static Set setOf4(Block var0) {
      return new HashSet<>();
   }

   final class Inner1 {
      private static final int intVal = Block.getRawIdFromState(Blocks.AIR.getDefaultState());
      private final Map<Long, int[][]> map = new ConcurrentHashMap<>();
      private final Set<Long> set = ConcurrentHashMap.newKeySet();

      boolean check(long var1) {
         return this.map.containsKey(var1);
      }

      Set<Long> getSet() {
         return this.set;
      }

      void run3() {
         this.map.clear();
         this.set.clear();
      }

      void run(int var1, int var2, int[][] var3) {
         long var4 = ChunkPos.toLong(var1, var2);
         this.map.put(var4, var3);
         boolean var6 = false;

         for (int var7 = 6; var7 <= 23 && var7 < var3.length; var7++) {
            if (var3[var7] != null) {
               var6 = true;
               break;
            }
         }

         if (var6) {
            this.set.add(var4);
         } else {
            this.set.remove(var4);
         }
      }

      List<Long> listOf(int var1, int var2, int[][] var3) {
         int[][] var4 = this.map.get(ChunkPos.toLong(var1, var2));
         if (var4 != null && !check2(var4)) {
            ArrayList var5 = new ArrayList();

            for (int var6 = 6; var6 <= 23; var6++) {
               int[] var7 = var6 < var4.length ? var4[var6] : null;
               int[] var8 = var6 < var3.length ? var3[var6] : null;
               if (var7 != null || var8 != null) {
                  if (var7 != null && var8 != null) {
                     if (!Arrays.equals(var7, var8)) {
                        for (int var13 = 0; var13 < var7.length && var13 < var8.length; var13++) {
                           if (var7[var13] != var8[var13]) {
                              var5.add(ChunkActivityTrackerModule.longOf(var7[var13], var8[var13]));
                           }
                        }
                     }
                  } else {
                     int[] var9 = var7 != null ? var7 : var8;

                     for (int var10 = 0; var10 < var9.length; var10++) {
                        int var11 = var7 != null ? var7[var10] : 0;
                        int var12 = var8 != null ? var8[var10] : 0;
                        if (var11 != var12) {
                           var5.add(ChunkActivityTrackerModule.longOf(var11, var12));
                        }
                     }
                  }
               }
            }

            return var5;
         } else {
            return List.of();
         }
      }

      private static boolean check2(int[][] var0) {
         for (int var1 = 6; var1 <= 23 && var1 < var0.length; var1++) {
            int[] var2 = var0[var1];
            if (var2 != null) {
               for (int var6 : var2) {
                  if (var6 != intVal) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   final class Inner2 {
      private List<Long> sightings;
      private Set<Long> zone;

      Inner2(List<Long> var1, Set<Long> var2) {
         this.sightings = var1;
         this.zone = var2;
      }

      public List<Long> sightings() {
         return this.sightings;
      }

      public Set<Long> zone() {
         return this.zone;
      }
   }
}
