package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public final class PedroAmethystModule extends Module {
   private static final int intVal = 20;
   private static final double doubleVal = 10.0;
   private static final ActivityChunkFinderModuleEntry2 val_2 = new ActivityChunkFinderModuleEntry2(150, 0, 255, 255);
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.getVal();
   private final Setting<Integer> val3_2 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("alpha")
            .valOf2("Opacity of the box drawn around each budding amethyst block.")
            .valOf3(125)
            .valOf5(1, 255)
            .getVal()
      );
   private final Setting<Integer> val4 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper6().valOf("min-y").valOf2("Lowest Y the scan looks at.").valOf3(-64).valOf5(-64, 320).getVal());
   private final Setting<Integer> val5 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("max-y")
            .valOf2("Highest Y the scan looks at. Keep this above the surface to catch shallow geodes.")
            .valOf3(320)
            .valOf5(-64, 320)
            .getVal()
      );
   private final Setting<Boolean> val6 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("tracers").valOf2("Draws a line from your crosshair to every found block.").valOf3(true).getVal()
      );
   private final Map<Long, List<BlockPos>> map = new ConcurrentHashMap<>();
   private final Set<Long> set = ConcurrentHashMap.newKeySet();
   private volatile List<BlockPos> list = List.of();
   private volatile int intVal2;
   private ExecutorService executorService;
   private int intVal3;

   public PedroAmethystModule() {
      super(SwyzzyAddon.val2, "pedro-amethyst", "Highlights budding amethyst in the loaded chunks.");
   }

   @Override
   public void run6() {
      this.executorService = Executors.newFixedThreadPool(2, PedroAmethystModule::threadOf);
      this.run15();
      this.intVal3 = 20;
   }

   @Override
   public void run7() {
      if (this.executorService != null) {
         this.executorService.shutdownNow();

         try {
            this.executorService.awaitTermination(50L, TimeUnit.MILLISECONDS);
         } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
         }

         this.executorService = null;
      }

      this.run15();
   }

   @Override
   public String getString2() {
      int var1 = this.list.size();
      if (var1 > 0) {
         return String.valueOf(var1);
      } else {
         int var2 = this.set.size();
         return var2 == 0 ? null : var2 + " hidden";
      }
   }

   public Set<BlockPos> getSet() {
      return Set.copyOf(this.list);
   }

   public Set<Long> getSet2() {
      return Set.copyOf(this.set);
   }

   private void run15() {
      this.intVal2++;
      this.map.clear();
      this.set.clear();
      this.list = List.of();
   }

   @InternalHelper5
   private void run4(AmethystChunkFinderModuleHelper var1) {
      if (var1.chunk() != null) {
         this.run2(var1.chunk());
      }
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.world != null && class310.player != null) {
         if (++this.intVal3 >= 20) {
            this.intVal3 = 0;
            HashSet var2 = new HashSet();

            for (Chunk var4 : ActivityChunkFinderModuleUtil.getIterable()) {
               if (var4 instanceof WorldChunk var5) {
                  var2.add(var5.getPos().toLong());
                  this.run2(var5);
               }
            }

            boolean var6 = this.map.keySet().retainAll(var2);
            this.set.retainAll(var2);
            if (var6) {
               this.run16();
            }
         }
      } else {
         this.run15();
      }
   }

   private void run2(WorldChunk var1) {
      ExecutorService var2 = this.executorService;
      if (var2 != null && !var2.isShutdown()) {
         int var3 = this.intVal2;
         long var4 = var1.getPos().toLong();

         try {
            var2.submit(() -> this.run5(var3, var1, var4));
         } catch (RejectedExecutionException var7) {
         }
      }
   }

   private static PedroAmethystModule.Inner1 valOf(WorldChunk var0, int var1, int var2) {
      ChunkSection[] var3 = var0.getSectionArray();
      int var4 = var0.getBottomSectionCoord();
      int var5 = Math.max(var0.getBottomY(), Math.min(var1, var2));
      int var6 = Math.min(var0.getBottomY() + var0.getHeight() - 1, Math.max(var1, var2));
      ChunkPos var7 = var0.getPos();
      ArrayList var8 = new ArrayList();
      boolean var9 = false;

      for (int var10 = 0; var10 < var3.length; var10++) {
         ChunkSection var11 = var3[var10];
         if (var11 != null && !var11.isEmpty()) {
            int var12 = (var4 + var10) * 16;
            if (var12 + 15 >= var5 && var12 <= var6 && var11.getBlockStateContainer().hasAny(PedroAmethystModule::check)) {
               var9 = true;

               for (int var13 = 0; var13 < 16; var13++) {
                  int var14 = var12 + var13;
                  if (var14 >= var5 && var14 <= var6) {
                     for (int var15 = 0; var15 < 16; var15++) {
                        for (int var16 = 0; var16 < 16; var16++) {
                           BlockState var17 = var11.getBlockState(var16, var13, var15);
                           if (check(var17)) {
                              var8.add(new BlockPos(var7.getStartX() + var16, var14, var7.getStartZ() + var15));
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return new Inner1(List.copyOf(var8), var9);
   }

   private static boolean check(BlockState var0) {
      return var0.isOf(Blocks.BUDDING_AMETHYST);
   }

   private void run16() {
      ArrayList var1 = new ArrayList();

      for (List var3 : this.map.values()) {
         var1.addAll(var3);
      }

      this.list = List.copyOf(var1);
   }

   @InternalHelper5
   private void run3(ActivityChunkFinderModuleData var1) {
      List var2 = this.list;
      if (class310.world != null && class310.player != null && !var2.isEmpty()) {
         ActivityChunkFinderModuleEntry2 var3 = new ActivityChunkFinderModuleEntry2(val_2.intVal, val_2.intVal2, val_2.intVal3, this.val3_2.getObject());

         for (BlockPos var5 : (Iterable<BlockPos>)(Object)(var2)) {
            var1.val.run2(var5, var3, val_2, RenderMode.Both, 0);
         }

         if (this.val6.getObject()) {
            double var12 = BlockEspPlusModuleData.class243.x;
            double var6 = BlockEspPlusModuleData.class243.y;
            double var8 = BlockEspPlusModuleData.class243.z;
            if (class310.gameRenderer.getCamera() != null) {
               Vec3d var10 = Vec3d.fromPolar(class310.gameRenderer.getCamera().getPitch(), class310.gameRenderer.getCamera().getYaw());
               var12 += var10.x * 10.0;
               var6 += var10.y * 10.0;
               var8 += var10.z * 10.0;
            }

            for (BlockPos var11 : (Iterable<BlockPos>)(Object)(var2)) {
               var1.val.run6(var12, var6, var8, var11.getX() + 0.5, var11.getY() + 0.5, var11.getZ() + 0.5, val_2);
            }
         }
      }
   }

   private void run5(int var1, WorldChunk var2, long var3) {
      if (this.intVal2 == var1 && class310.world != null) {
         try {
            PedroAmethystModule.Inner1 var5 = valOf(var2, this.val4.getObject(), this.val5.getObject());
            if (this.intVal2 != var1) {
               return;
            }

            if (var5.paletteBudding()) {
               this.set.add(var3);
            } else {
               this.set.remove(var3);
            }

            if (var5.blocks().isEmpty()) {
               this.map.remove(var3);
            } else {
               this.map.put(var3, var5.blocks());
            }

            this.run16();
         } catch (Throwable var6) {
            SwyzzyAddon.logger.warn("Pedro Amethyst could not scan {}.", new ChunkPos(var3), var6);
         }
      }
   }

   private static Thread threadOf(Runnable var0) {
      Thread var1 = new Thread(var0, "PedroAmethyst-Scanner");
      var1.setDaemon(true);
      var1.setPriority(3);
      return var1;
   }

   final static class Inner1 {
      private List<BlockPos> blocks;
      private boolean paletteBudding;

      Inner1(List<BlockPos> var1, boolean var2) {
         this.blocks = var1;
         this.paletteBudding = var2;
      }

      public List<BlockPos> blocks() {
         return this.blocks;
      }

      public boolean paletteBudding() {
         return this.paletteBudding;
      }
   }
}
