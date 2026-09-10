package dev.kryptic.util;

import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * The parts every anchor module needs, in one place.
 *
 * Most of this exists because of one Minecraft rule that is easy to forget and
 * ruins an anchor macro: <b>sneaking suppresses block use</b>. Vanilla's
 * interactBlock checks whether you are sneaking with something in your hand,
 * and if you are it skips using the block and places the held item instead.
 *
 * So an anchor macro that fires while you are crouched does not charge the
 * anchor — it puts a glowstone block on the floor. That is not a timing bug or
 * a race, it is the client doing exactly what vanilla asks for, and no amount
 * of delay tuning fixes it. Every interaction here goes through
 * {@link #canInteract} for that reason.
 */
public final class AnchorHelper {

   /** Glowstone charges an anchor; nothing else does. */
   public static final int MAX_CHARGES = 4;

   private AnchorHelper() {
   }

   private static MinecraftClient mc() {
      return MinecraftClient.getInstance();
   }

   /**
    * Whether a block interaction right now would actually use the block.
    *
    * Crouching is the whole point of this check. The others are the ordinary
    * "is the client in a state where clicking means anything" guards.
    */
   public static boolean canInteract() {
      MinecraftClient client = mc();
      ClientPlayerEntity player = client.player;
      if (player == null || client.interactionManager == null || client.world == null) {
         return false;
      }

      if (client.currentScreen != null) {
         return false;
      }

      // The one that matters. Sneaking with a full hand turns every "use the
      // block" into "place what you are holding".
      return !player.isSneaking();
   }

   /** The respawn anchor under the crosshair, or null. */
   public static BlockHitResult anchorUnderCrosshair() {
      MinecraftClient client = mc();
      if (client.crosshairTarget == null || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
         return null;
      }

      BlockHitResult hit = (BlockHitResult) client.crosshairTarget;
      return BlockHelper.isBlockAt(hit.getBlockPos(), Blocks.RESPAWN_ANCHOR) ? hit : null;
   }

   /** How many charges an anchor is holding, or -1 if that is not an anchor. */
   public static int charges(BlockPos pos) {
      MinecraftClient client = mc();
      if (client.world == null || !BlockHelper.isBlockAt(pos, Blocks.RESPAWN_ANCHOR)) {
         return -1;
      }

      return client.world.getBlockState(pos).get(RespawnAnchorBlock.CHARGES);
   }

   /**
    * Put one charge into an anchor.
    *
    * @return false when it could not be done — no glowstone, or you are
    *         crouched, in which case doing it anyway would place a block
    */
   public static boolean charge(BlockHitResult hit) {
      if (!canInteract()) {
         return false;
      }

      if (!InventoryHelper.swapToItem(Items.GLOWSTONE)) {
         return false;
      }

      BlockHelper.interactBlock(hit, true);
      return true;
   }

   /**
    * Set off a charged anchor.
    *
    * Detonating means using it with anything that is <em>not</em> glowstone in
    * hand — glowstone would add a charge instead. The caller picks the slot;
    * this only refuses when the hand would recharge it.
    */
   public static boolean detonate(BlockHitResult hit) {
      MinecraftClient client = mc();
      if (!canInteract() || client.player == null) {
         return false;
      }

      if (client.player.getMainHandStack().isOf(Items.GLOWSTONE)) {
         return false;                          // would charge it, not blow it
      }

      BlockHelper.interactBlock(hit, true);
      return true;
   }

   /**
    * Rough blast damage an anchor at {@code pos} would do to the player.
    *
    * An anchor explodes with power 5. This is the standard falloff — full
    * damage at the centre, nothing past the radius — rather than a faithful
    * reimplementation of Explosion, which would need ray casts through every
    * block between you and it. It is deliberately an estimate, and callers
    * treat it as one: it is used to refuse obviously fatal detonations, not to
    * shave a heart off a safe one.
    */
   public static float selfDamageEstimate(BlockPos pos) {
      ClientPlayerEntity player = mc().player;
      if (player == null) {
         return 0.0F;
      }

      double dx = player.getX() - (pos.getX() + 0.5);
      double dy = player.getY() - (pos.getY() + 0.5);
      double dz = player.getZ() - (pos.getZ() + 0.5);
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      double radius = 5.0 * 2.0;
      if (distance >= radius) {
         return 0.0F;
      }

      double exposure = (1.0 - distance / radius);
      double raw = (exposure * exposure * 7.0 + exposure) * 2.0 * 5.0 + 1.0;

      // Armour and resistance both cut it substantially. Halving is a coarse
      // stand-in and errs toward over-estimating the damage, which is the safe
      // direction for a check whose job is to refuse.
      return (float) raw;
   }
}
