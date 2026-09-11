package dev.kryptic.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * Who you are fighting.
 *
 * There is no such thing as a "target" in vanilla, so this reconstructs one
 * from the two signals that exist: who you last hit, and who is under your
 * crosshair now. The first is the stronger signal — you swung at them on
 * purpose — so it wins while it is fresh, and the crosshair fills the gap
 * before the first hit lands and after the fight moves on.
 *
 * The hold is what makes it usable. Without it the panel would appear for the
 * frame of each swing and vanish between them, which is worse than not having
 * it: a health bar you cannot read is just flicker in the corner.
 */
public final class CombatTarget {

   /** How long someone stays "the target" after you last hit them. */
   private static final long HOLD_NANOS = 4_000_000_000L;

   private static LivingEntity attacked;
   private static long attackedAt;

   private CombatTarget() {
   }

   /** Called from the attack mixin, on every swing that lands on an entity. */
   public static void attacked(Entity entity) {
      if (entity instanceof LivingEntity living) {
         attacked = living;
         attackedAt = System.nanoTime();
      }
   }

   /** Forget the current target — on disconnect, or when it dies. */
   public static void clear() {
      attacked = null;
      attackedAt = 0L;
   }

   /**
    * The entity to show, or null when there is nothing worth showing.
    *
    * A dead or unloaded target is dropped rather than held for the rest of the
    * timeout: the interesting moment is the fight, and a corpse's health bar
    * stuck at zero for four seconds reads as a bug.
    */
   public static LivingEntity current() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null || client.world == null) {
         return null;
      }

      if (attacked != null) {
         boolean stale = System.nanoTime() - attackedAt > HOLD_NANOS;
         if (stale || !attacked.isAlive() || attacked.isRemoved()) {
            attacked = null;
         } else {
            return attacked;
         }
      }

      if (client.crosshairTarget instanceof EntityHitResult hit
            && client.crosshairTarget.getType() == HitResult.Type.ENTITY
            && hit.getEntity() instanceof LivingEntity living
            && living != client.player
            && living.isAlive()) {
         return living;
      }

      return null;
   }
}
