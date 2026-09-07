package dev.sixseven.render;

import dev.sixseven.module.render.StorageEspModule;
import java.util.List;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Vector3fc;

public final class StorageEspRenderer {
   private static final int MAX_CHUNK_RADIUS = 16;
   private static final int MAX_RESULTS = 6000;
   private static final float BOX_INFLATE = 0.002F;
   private static final double CHEST_INSET = 0.0625;
   private static final int INTERACTED_RGB = 6579300;
   private static final float LINE_WIDTH = 1.0F;
   private static final float TRACER_WIDTH = 1.15F;
   private static final int TRACER_ALPHA = 180;
   private static final IncrementalScan<StorageEspRenderer.Hit> SCAN = new IncrementalScan<>(48, 80000, 20);

   private StorageEspRenderer() {
   }

   public static void clear() {
      SCAN.clear();
   }

   public static int cachedCount() {
      return SCAN.get().size();
   }

   public static long cachedShulkerCount() {
      return SCAN.get().stream().filter(arg -> arg.type() == StorageEspModule.StorageType.SHULKER).count();
   }

   public static void scan(StorageEspModule storageEspModule) {
      double d = storageEspModule.range.get();
      double coord = d * d;
      int n = (int)Math.ceil(d / 16.0) + 1;
      int client = Math.min(Math.min(16, n), (Integer)MinecraftClient.getInstance().options.getViewDistance().getValue());
      SCAN.tick(client, (arg, arg2) -> scanChunk(arg, coord, arg2));
   }

   private static int scanChunk(WorldChunk chunk, double d, List<StorageEspRenderer.Hit> list) {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null) {
         return 0;
      } else {
         ChunkSection[] arr = chunk.getSectionArray();
         int n = chunk.getBottomSectionCoord();
         int n12 = chunk.getPos().getStartX();
         int n13 = chunk.getPos().getStartZ();
         short s = 0;
         if (list.size() >= 6000) {
            return 0;
         } else {
            for (int n14 = 0; n14 < arr.length; n14++) {
               ChunkSection chunkSection = arr[n14];
               if (!chunkSection.isEmpty() && chunkSection.hasAny(StorageEspRenderer::isStorage)) {
                  int n15 = n + n14 << 4;
                  s += 4096;

                  for (int n16 = 0; n16 < 16; n16++) {
                     for (int n17 = 0; n17 < 16; n17++) {
                        for (int n18 = 0; n18 < 16; n18++) {
                           StorageEspModule.StorageType state = classify(chunkSection.getBlockState(n18, n16, n17).getBlock());
                           if (state != null) {
                              int n19 = n12 + n18;
                              int n20 = n15 + n16;
                              int n21 = n13 + n17;
                              if (!(player.squaredDistanceTo((double)n19 + 0.5, (double)n20 + 0.5, (double)n21 + 0.5) > d)) {
                                 list.add(new StorageEspRenderer.Hit(n19, n20, n21, state));
                                 if (list.size() >= 6000) {
                                    return s;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            return s;
         }
      }
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d vec, StorageEspModule storageEspModule) {
      List<StorageEspRenderer.Hit> list = SCAN.get();
      if (!list.isEmpty()) {
         MinecraftClient client = MinecraftClient.getInstance();
         ClientWorld world = client.world;
         if (world != null) {
            boolean ok = storageEspModule.mode.is("Full");
            int n = Math.max(0, Math.min(255, storageEspModule.highlightAlpha.getInt())) << 24;
            boolean ok2 = storageEspModule.tracers.get();
            boolean ok3 = storageEspModule.hideOpened();
            net.minecraft.util.math.Vec3d camera = client.gameRenderer.getCamera().getPos();

            for (StorageEspRenderer.Hit hit : list) {
               StorageEspModule.StorageType storageType = hit.type();
               if (storageEspModule.isTypeEnabled(storageType)) {
                  boolean ok4 = storageEspModule.isInteracted(hit.x(), hit.y(), hit.z());
                  if (!ok4 || !ok3) {
                     int localZ = ok4 ? 6579300 : storageEspModule.colorFor(storageType) & 16777215;
                     int localY = n | localZ;
                     double d = (double)hit.x();
                     double coord = (double)hit.y();
                     double currentScore = (double)hit.z();
                     double coord3 = d + 1.0;
                     double coord4 = coord + 1.0;
                     double coord5 = currentScore + 1.0;
                     if (storageType == StorageEspModule.StorageType.CHEST
                        || storageType == StorageEspModule.StorageType.TRAPPED
                        || storageType == StorageEspModule.StorageType.ENDER) {
                        d += 0.0625;
                        currentScore += 0.0625;
                        coord3 -= 0.0625;
                        coord4 -= 0.125;
                        coord5 -= 0.0625;
                        if (storageType == StorageEspModule.StorageType.CHEST || storageType == StorageEspModule.StorageType.TRAPPED) {
                           BlockState state = world.getBlockState(new BlockPos(hit.x(), hit.y(), hit.z()));
                           if (state.getBlock() instanceof ChestBlock && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                              Direction direction = (Direction)state.get(ChestBlock.FACING);
                              ChestType chestType = (ChestType)state.get(ChestBlock.CHEST_TYPE);
                              Direction direction2 = chestType == ChestType.LEFT ? direction.rotateYClockwise() : direction.rotateYCounterclockwise();
                              if (direction2 == Direction.WEST) {
                                 d = (double)hit.x();
                              } else if (direction2 == Direction.EAST) {
                                 coord3 = (double)(hit.x() + 1);
                              } else if (direction2 == Direction.NORTH) {
                                 currentScore = (double)hit.z();
                              } else if (direction2 == Direction.SOUTH) {
                                 coord5 = (double)(hit.z() + 1);
                              }
                           }
                        }
                     }

                     if (ok) {
                        EspBoxRenderer.fill(
                           immediate, matrices, vec, d - 0.002F, coord - 0.002F, currentScore - 0.002F, coord3 + 0.002F, coord4 + 0.002F, coord5 + 0.002F, localY
                        );
                     } else {
                        EspBoxRenderer.outline(immediate, matrices, vec, d, coord, currentScore, coord3, coord4, coord5, localY, 1.0F);
                     }

                     if (ok2) {
                        int step = -1275068416 | localZ;
                        EspBoxRenderer.tracer(immediate, matrices, vec, camera, (d + coord3) / 2.0, (coord + coord4) / 2.0, (currentScore + coord5) / 2.0, step, 1.15F);
                     }
                  }
               }
            }

            EspBoxRenderer.flush(immediate);
         }
      }
   }

   private static boolean isStorage(BlockState state) {
      return classify(state.getBlock()) != null;
   }

   private static StorageEspModule.StorageType classify(Block block) {
      if (block instanceof TrappedChestBlock) {
         return StorageEspModule.StorageType.TRAPPED;
      } else if (block instanceof ChestBlock) {
         return StorageEspModule.StorageType.CHEST;
      } else if (block instanceof EnderChestBlock) {
         return StorageEspModule.StorageType.ENDER;
      } else if (block instanceof ShulkerBoxBlock) {
         return StorageEspModule.StorageType.SHULKER;
      } else if (block instanceof BarrelBlock) {
         return StorageEspModule.StorageType.BARREL;
      } else if (block instanceof SpawnerBlock) {
         return StorageEspModule.StorageType.SPAWNER;
      } else if (block instanceof HopperBlock) {
         return StorageEspModule.StorageType.HOPPER;
      } else if (block instanceof AbstractFurnaceBlock) {
         return StorageEspModule.StorageType.FURNACE;
      } else if (block instanceof BrewingStandBlock) {
         return StorageEspModule.StorageType.FURNACE;
      } else {
         return block instanceof DispenserBlock ? StorageEspModule.StorageType.HOPPER : null;
      }
   }

   private static record Hit(int x, int y, int z, StorageEspModule.StorageType type) {
   }
}
