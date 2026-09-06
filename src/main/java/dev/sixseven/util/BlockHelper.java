package dev.sixseven.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public final class BlockHelper {
   private BlockHelper() {
   }

   public static boolean isBlockAt(BlockPos pos, Block block) {
      MinecraftClient client = MinecraftClient.getInstance();
      return client.world != null && client.world.getBlockState(pos).isOf(block);
   }

   public static boolean isAnchorCharged(BlockPos pos) {
      MinecraftClient client = MinecraftClient.getInstance();
      return isBlockAt(pos, Blocks.RESPAWN_ANCHOR) && (Integer)client.world.getBlockState(pos).get(RespawnAnchorBlock.CHARGES) != 0;
   }

   public static boolean isAnchorUncharged(BlockPos pos) {
      MinecraftClient client = MinecraftClient.getInstance();
      return isBlockAt(pos, Blocks.RESPAWN_ANCHOR) && (Integer)client.world.getBlockState(pos).get(RespawnAnchorBlock.CHARGES) == 0;
   }

   public static void interactBlock(BlockHitResult blockHit, boolean value) {
      interactBlock(blockHit, Hand.MAIN_HAND, value);
   }

   public static void interactBlock(BlockHitResult blockHit, Hand hand, boolean value) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.interactionManager != null) {
         ActionResult actionResult = client.interactionManager.interactBlock(client.player, hand, blockHit);
         if (actionResult.isAccepted() && value) {
            client.player.swingHand(hand);
         }
      }
   }
}
