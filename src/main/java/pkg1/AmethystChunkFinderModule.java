package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class AmethystChunkFinderModule extends Module {
   private static final double doubleVal = 0.12;
   private static final double doubleVal2 = 0.1;
   private static final int intVal = 4;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Render");
   private final Setting<Integer> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("spread-radius")
            .valOf2("How far the score of one chunk bleeds into its neighbours.")
            .valOf3(4)
            .valOf4(2, 16)
            .valOf5(2, 16)
            .getVal()
      );
   private final Setting<Integer> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("sensitivity")
            .valOf2("Minimum accumulated score before a chunk is shown.")
            .valOf3(3)
            .valOf4(1, 20)
            .valOf5(1, 20)
            .getVal()
      );
   private final Setting<Integer> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("max-distance")
            .valOf2("Maximum chunk distance at which flagged chunks are kept.")
            .valOf3(50)
            .valOf4(8, 128)
            .valOf5(8, 128)
            .getVal()
      );
   private final Setting<Boolean> val6 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("ignore-logoff-area")
            .valOf2("Hides chunks around the place you logged off at, since those are your own.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Boolean> val7 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("auto-adjust")
            .valOf2("Collapses each connected group of flagged chunks down to its highest scoring chunks.")
            .valOf3(false)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val8 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("chunk-color")
            .valOf2("Fill color of flagged chunks.")
            .valOf3(new ActivityChunkFinderModuleHelper4(200, 130, 255, 80))
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val9 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("line-color")
            .valOf2("Outline color of flagged chunks.")
            .valOf3(new ActivityChunkFinderModuleHelper4(200, 130, 255, 255))
            .getVal()
      );
   private final Setting<Integer> val10 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("y-level")
            .valOf2("Y coordinate the chunk markers are drawn at.")
            .valOf3(63)
            .valOf4(-64, 320)
            .valOf5(-64, 320)
            .getVal()
      );
   private volatile int intVal2;
   private volatile ChunkPos class1923;
   private volatile ChunkPos class19232;
   private final Map<ChunkPos, Integer> map = new ConcurrentHashMap<>();
   private final Set<ChunkPos> set = ConcurrentHashMap.newKeySet();
   private final Map<ChunkPos, Integer> map2 = new ConcurrentHashMap<>();
   private ExecutorService executorService;
   private volatile Set<ChunkPos> set2 = Set.of();
   private int intVal3;

   public AmethystChunkFinderModule() {
      super(SwyzzyAddon.val2, "amethyst-chunk-finder", "Uses amethyst bud density to find bases.");
   }

   private static ExecutorService getExecutorService() {
      return Executors.newSingleThreadExecutor(AmethystChunkFinderModule::threadOf);
   }

   @Override
   public void run6() {
      this.executorService = getExecutorService();
      this.run15();
      if (class310.world != null && class310.player != null) {
         ChunkPos var1 = class310.player.getChunkPos();
         int var2 = (Integer)class310.options.getViewDistance().getValue() + 2;

         for (int var3 = var1.x - var2; var3 <= var1.x + var2; var3++) {
            for (int var4 = var1.z - var2; var4 <= var1.z + var2; var4++) {
               WorldChunk var5 = class310.world.getChunkManager().getWorldChunk(var3, var4);
               if (var5 != null) {
                  this.run(var5);
               }
            }
         }
      }
   }

   @Override
   public void run7() {
      if (this.class19232 != null) {
         this.class1923 = this.class19232;
      }

      this.run13();
      this.run15();
   }

   @Override
   public String getString2() {
      int var1 = this.set2.size();
      return var1 == 0 ? null : String.valueOf(var1);
   }

   @InternalHelper5
   private void run4(AmethystChunkFinderModuleHelper var1) {
      if (var1.chunk() != null) {
         this.run(var1.chunk());
      }
   }

   private void run13() {
      if (this.executorService != null && !this.executorService.isShutdown()) {
         this.executorService.shutdownNow();

         try {
            this.executorService.awaitTermination(50L, TimeUnit.MILLISECONDS);
         } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
         }
      }
   }

   private void run15() {
      this.intVal2++;
      this.map.clear();
      this.set.clear();
      this.map2.clear();
      this.set2 = Set.of();
      this.intVal3 = 0;
   }

   public void run(WorldChunk var1) {
      if (this.executorService != null && !this.executorService.isShutdown()) {
         ChunkPos var2 = var1.getPos();
         int var3 = this.intVal2;
         this.executorService.submit(() -> this.run8(var3, var2, var1));
      }
   }

   private void run11(ChunkPos var1, WorldChunk var2) {
      int var3 = this.val3_2.getObject();
      int var4 = this.map2.getOrDefault(var1, 0);
      int var5 = this.intOf(var2);

      for (int var6 = -var3; var6 <= var3; var6++) {
         for (int var7 = -var3; var7 <= var3; var7++) {
            ChunkPos var8 = new ChunkPos(var1.x + var6, var1.z + var7);
            if (var4 > 0) {
               { int var910 = var7; this.map.compute(var8, (var903, var904) -> AmethystChunkFinderModule.integerOf3(var910, var903, var904)); }
            }

            if (var5 > 0) {
               this.map.merge(var8, var5, Integer::sum);
            }
         }
      }

      this.map2.put(var1, var5);
   }

   private static Integer integerOf(int var0, Integer var1) {
      int var2 = (var1 == null ? 0 : var1) - var0;
      return var2 <= 0 ? null : var2;
   }

   private int intOf(WorldChunk var1) {
      for (ChunkSection var5 : var1.getSectionArray()) {
         if (var5 != null) {
            boolean var6 = var5.getBlockStateContainer().hasAny(AmethystChunkFinderModule::check2);
            if (var6) {
               return 1;
            }
         }
      }

      return 0;
   }

   private void run3(ChunkPos var1) {
      int var2 = this.map2.getOrDefault(var1, 0);
      this.set.remove(var1);
      this.map2.remove(var1);
      this.map.remove(var1);
      if (var2 > 0) {
         int var3 = this.val3_2.getObject();

         for (int var4 = -var3; var4 <= var3; var4++) {
            for (int var5 = -var3; var5 <= var3; var5++) {
               { int var911 = var5; this.map.compute(new ChunkPos(var1.x + var4, var1.z + var5), (var903, var904) -> AmethystChunkFinderModule.integerOf2(var911, var903, var904)); }
            }
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.world == null) {
         this.set2 = Set.of();
      } else {
         if (class310.player != null && class310.player.getY() <= 0.0) {
            this.class19232 = class310.player.getChunkPos();
         }

         if (++this.intVal3 >= 4) {
            this.intVal3 = 0;
            ClientWorld var2 = class310.world;

            for (ChunkPos var4 : this.set) {
               if (var2.getChunkManager().getWorldChunk(var4.x, var4.z) == null) {
                  this.run3(var4);
               }
            }

            this.map.keySet().removeIf(var905 -> AmethystChunkFinderModule.check(var2, var905));
            this.set2 = this.map.isEmpty() ? Set.of() : this.getSet();
         }
      }
   }

   private Set<ChunkPos> getSet() {
      if (class310.world != null && class310.player != null) {
         int var1 = this.val4.getObject();
         int var2 = this.val5.getObject();
         ChunkPos var3 = class310.player.getChunkPos();
         ChunkPos var4 = ((Boolean)this.val6.getObject()) ? this.class1923 : null;
         int var5 = var4 != null ? 1 + this.val3_2.getObject() : 0;
         HashMap var6 = new HashMap();

         for (Entry var8 : this.map.entrySet()) {
            ChunkPos var9 = (ChunkPos)var8.getKey();
            int var10 = (Integer)var8.getValue();
            if (Math.max(Math.abs(var9.x - var3.x), Math.abs(var9.z - var3.z)) <= var2
               && (var4 == null || Math.abs(var9.x - var4.x) > var5 || Math.abs(var9.z - var4.z) > var5)
               && var10 >= var1
               && this.set.contains(var9)
               && class310.world.getChunkManager().getWorldChunk(var9.x, var9.z) != null) {
               var6.put(var9, var10);
            }
         }

         return !((Boolean)this.val7.getObject()) ? var6.keySet() : setOf(var6);
      } else {
         return Set.of();
      }
   }

   private static Set<ChunkPos> setOf(Map<ChunkPos, Integer> var0) {
      HashSet var1 = new HashSet();
      HashSet var2 = new HashSet();

      for (ChunkPos var4 : var0.keySet()) {
         if (var1.add(var4)) {
            ArrayList var5 = new ArrayList();
            ArrayDeque var6 = new ArrayDeque();
            var6.add(var4);

            while (!var6.isEmpty()) {
               ChunkPos var7 = (ChunkPos)var6.poll();
               var5.add(var7);
               ChunkPos[] var8 = new ChunkPos[]{
                  new ChunkPos(var7.x + 1, var7.z), new ChunkPos(var7.x - 1, var7.z), new ChunkPos(var7.x, var7.z + 1), new ChunkPos(var7.x, var7.z - 1)
               };

               for (ChunkPos var12 : var8) {
                  if (var0.containsKey(var12) && var1.add(var12)) {
                     var6.add(var12);
                  }
               }
            }

            int var13 = 0;

            for (ChunkPos var16 : (Iterable<ChunkPos>)(Object)(var5)) {
               var13 = Math.max(var13, (Integer)var0.get(var16));
            }

            for (ChunkPos var17 : (Iterable<ChunkPos>)(Object)(var5)) {
               if ((Integer)var0.get(var17) == var13) {
                  var2.add(var17);
               }
            }
         }
      }

      return var2;
   }

   @InternalHelper5
   private void run5(ActivityChunkFinderModuleData var1) {
      Set var2 = this.set2;
      if (class310.world != null && class310.player != null && !var2.isEmpty()) {
         ActivityChunkFinderModuleEntry2 var3 = this.val8.getObject();
         ActivityChunkFinderModuleEntry2 var4 = this.val9.getObject();
         double var5 = this.val10.getObject().intValue();

         for (ChunkPos var8 : (Iterable<ChunkPos>)(Object)(var2)) {
            byte var9 = 0;
            if (var2.contains(new ChunkPos(var8.x, var8.z - 1))) {
               var9 = 8;
            }

            if (var2.contains(new ChunkPos(var8.x, var8.z + 1))) {
               var9 |= 16;
            }

            if (var2.contains(new ChunkPos(var8.x - 1, var8.z))) {
               var9 |= 32;
            }

            if (var2.contains(new ChunkPos(var8.x + 1, var8.z))) {
               var9 |= 64;
            }

            double var10 = var8.getStartX();
            double var12 = var8.getStartZ();
            var1.val.run4(var10, var5, var12, var10 + 16.0, var5 + 0.1, var12 + 16.0, var3, var9 | 8 | 16 | 32 | 64);
            var1.val.run4(var10, var5, var12, var10 + 16.0, var5 + 0.1, var12 + 16.0, var4, var9 | 2 | 4);
            var1.val.run5(var10, var5, var12, var10 + 16.0, var5 + 0.1, var12 + 16.0, var4, var9);
            var1.val.run5(var10 + 0.12, var5, var12 + 0.12, var10 + 16.0 - 0.12, var5 + 0.1, var12 + 16.0 - 0.12, var4, var9);
         }
      }
   }

   private static boolean check(ClientWorld var0, ChunkPos var1) {
      return var0.getChunkManager().getWorldChunk(var1.x, var1.z) == null;
   }

   private static Integer integerOf2(int var0, ChunkPos var1, Integer var2) {
      return integerOf(var0, var2);
   }

   private static boolean check2(BlockState var0) {
      return var0.isOf(Blocks.SMALL_AMETHYST_BUD) || var0.isOf(Blocks.MEDIUM_AMETHYST_BUD) || var0.isOf(Blocks.LARGE_AMETHYST_BUD);
   }

   private static Integer integerOf3(int var0, ChunkPos var1, Integer var2) {
      return integerOf(var0, var2);
   }

   private void run8(int var1, ChunkPos var2, WorldChunk var3) {
      try {
         if (this.intVal2 != var1) {
            return;
         }

         this.set.add(var2);
         this.run11(var2, var3);
      } catch (Throwable var5) {
         SwyzzyAddon.logger.warn("Amethyst Chunk Finder could not scan {}.", var2, var5);
      }
   }

   private static Thread threadOf(Runnable var0) {
      Thread var1 = new Thread(var0, "AmethystChunkFinder-Scanner");
      var1.setDaemon(true);
      return var1;
   }
}
