package dev.sixseven.render;

import dev.sixseven.module.render.DebugHoleEspModule;
import dev.sixseven.util.Colors;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import org.joml.Vector3fc;

public final class HoleEspRenderer {
   private static final int SCAN_INTERVAL_TICKS = 10;
   private static final int RADIUS = 16;
   private static final int VERTICAL = 6;
   private static final Set<Block> UNBREAKABLE = Set.of(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.BEDROCK, Blocks.RESPAWN_ANCHOR, Blocks.REINFORCED_DEEPSLATE);
   private static volatile List<HoleEspRenderer.Hole> cache = List.of();
   private static int tickCounter;

   private HoleEspRenderer() {
   }

   public static void clear() {
      cache = List.of();
      tickCounter = 0;
   }

   public static void scan(DebugHoleEspModule debugHoleEspModule) {
      if (tickCounter++ % 10 == 0) {
         MinecraftClient client = MinecraftClient.getInstance();
         ClientWorld world = client.world;
         ClientPlayerEntity player = client.player;
         if (world != null && player != null) {
            int n = debugHoleEspModule.depth.is("2B1") ? 1 : Integer.parseInt(debugHoleEspModule.depth.get());
            BlockPos pos = player.getBlockPos();
            Mutable mutablePos = new Mutable();
            ArrayList list = new ArrayList();

            for (int step2 = -16; step2 <= 16; step2++) {
               for (int n9 = -16; n9 <= 16; n9++) {
                  for (int n10 = -6; n10 <= 6; n10++) {
                     int n11 = pos.getX() + step2;
                     int n12 = pos.getY() + n10;
                     int n13 = pos.getZ() + n9;
                     if (isHole(world, mutablePos, n11, n12, n13, n)) {
                        list.add(new HoleEspRenderer.Hole(n11, n12, n13, wallsSafe(world, mutablePos, n11, n12, n13)));
                     }
                  }
               }
            }

            cache = list;
         }
      }
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d vec, DebugHoleEspModule debugHoleEspModule) {
      List list = cache;
      if (!list.isEmpty()) {
         int n = debugHoleEspModule.safe.get();
         int localX = debugHoleEspModule.unsafe.get();
         boolean found = debugHoleEspModule.tracers.get();
         Vector3fc client = found ? MinecraftClient.getInstance().gameRenderer.getCamera().getHorizontalPlane() : null;

         for (HoleEspRenderer.Hole hole : list) {
            int localZ = hole.safe() ? n : localX;
            EspBoxRenderer.outline(
               immediate,
               matrices,
               vec,
               (double)hole.x(),
               (double)hole.y(),
               (double)hole.z(),
               (double)(hole.x() + 1),
               (double)(hole.y() + 1),
               (double)(hole.z() + 1),
               localZ,
               2.0F
            );
            if (found) {
               EspBoxRenderer.tracer(
                  immediate, matrices, vec, client, (double)hole.x() + 0.5, (double)hole.y() + 0.5, (double)hole.z() + 0.5, Colors.withAlpha(localZ, 0.72F), 1.2F
               );
            }
         }

         EspBoxRenderer.flush(immediate);
      }
   }

   private static boolean isHole(ClientWorld world, Mutable mutablePos, int n, int localY, int step, int step2) {
      if (!world.getBlockState(mutablePos.set(n, localY - 1, step)).blocksMovement()) {
         return false;
      } else {
         for (int n9 = 0; n9 < step2; n9++) {
            if (!world.getBlockState(mutablePos.set(n, localY + n9, step)).isAir()) {
               return false;
            }
         }

         return world.getBlockState(mutablePos.set(n + 1, localY, step)).blocksMovement()
            && world.getBlockState(mutablePos.set(n - 1, localY, step)).blocksMovement()
            && world.getBlockState(mutablePos.set(n, localY, step + 1)).blocksMovement()
            && world.getBlockState(mutablePos.set(n, localY, step - 1)).blocksMovement();
      }
   }

   private static boolean wallsSafe(ClientWorld world, Mutable mutablePos, int n, int localX, int localZ) {
      return isUnbreakable(world.getBlockState(mutablePos.set(n, localX - 1, localZ)))
         && isUnbreakable(world.getBlockState(mutablePos.set(n + 1, localX, localZ)))
         && isUnbreakable(world.getBlockState(mutablePos.set(n - 1, localX, localZ)))
         && isUnbreakable(world.getBlockState(mutablePos.set(n, localX, localZ + 1)))
         && isUnbreakable(world.getBlockState(mutablePos.set(n, localX, localZ - 1)));
   }

   private static boolean isUnbreakable(BlockState state) {
      return UNBREAKABLE.contains(state.getBlock());
   }

   private static record Hole(int x, int y, int z, boolean safe) {
   }
}
