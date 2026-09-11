package dev.kryptic.module.combat;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.AnchorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Tells you whether the crystal you are about to place is worth placing.
 *
 * It does not place, break or aim at anything — it is arithmetic you could do
 * yourself, done every tick instead of guessed at. The question it answers is
 * the one that decides a crystal fight: does this position hurt them more than
 * it hurts me, and by enough to be worth the pearl.
 *
 * <p>Both figures are estimates. A faithful copy of vanilla's explosion needs
 * ray casts through every block between the crystal and each player, and this
 * does not do that — it uses distance falloff alone. So it is reliable about
 * "this is clearly a bad trade" and unreliable about small margins. The
 * numbers are here to stop obvious mistakes, not to shave a heart.
 */
public class CrystalOptimiserModule extends Module {

   public final SliderSetting range = this.addSetting(new SliderSetting("Scan Range",
      "How far out to look for crystals worth judging", 8.0, 2.0, 16.0, 0.5, "m"));

   public final SliderSetting minRatio = this.addSetting(new SliderSetting("Worth It Above",
      "Their damage divided by yours must beat this", 1.5, 0.5, 5.0, 0.1, "x"));

   public final SliderSetting maxSelf = this.addSetting(new SliderSetting("Never Above",
      "Call it a bad trade if it would do you more than this", 8.0, 1.0, 20.0, 0.5));

   public final BooleanSetting ignoreFriends = this.addSetting(new BooleanSetting("Ignore Yourself Only",
      "Judge against every other player, not just the nearest", true));

   public CrystalOptimiserModule() {
      super("Crystal Optimiser", "Rates a crystal's damage trade before you commit to it", Category.COMBAT);
   }

   /** What the module currently thinks, or null when there is nothing to judge. */
   public Verdict verdict() {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity self = client.player;
      if (!this.isEnabled() || self == null || client.world == null) {
         return null;
      }

      double r = this.range.get();
      Box box = self.getBoundingBox().expand(r);
      EndCrystalEntity best = null;
      double nearest = Double.MAX_VALUE;
      for (EndCrystalEntity crystal : client.world.getEntitiesByClass(EndCrystalEntity.class, box, c -> true)) {
         double d = self.squaredDistanceTo(crystal);
         if (d < nearest) {
            nearest = d;
            best = crystal;
         }
      }

      if (best == null) {
         return null;
      }

      Vec3d at = new Vec3d(best.getX(), best.getY(), best.getZ());
      float mine = damageAt(self, at);
      float theirs = 0.0F;
      for (PlayerEntity other : client.world.getPlayers()) {
         if (other == self) {
            continue;
         }

         theirs = Math.max(theirs, damageAt(other, at));
      }

      return new Verdict(mine, theirs, this.judge(mine, theirs));
   }

   private boolean judge(float mine, float theirs) {
      if (mine > this.maxSelf.getFloat()) {
         return false;
      }

      if (mine <= 0.01F) {
         return theirs > 0.5F;                  // free damage, if it does anything at all
      }

      return theirs / mine >= this.minRatio.getFloat();
   }

   /** Distance falloff only; see the class note on why this is an estimate. */
   private static float damageAt(PlayerEntity player, Vec3d source) {
      // Entity.getPos() is not a name on this version; getX/getY/getZ are.
      double distance = new Vec3d(player.getX(), player.getY(), player.getZ())
            .distanceTo(source);
      double radius = 12.0;                     // a crystal explodes with power 6
      if (distance >= radius) {
         return 0.0F;
      }

      double exposure = 1.0 - distance / radius;
      return (float) ((exposure * exposure * 7.0 + exposure) * 2.0 * 6.0 + 1.0);
   }

   /**
    * @param selfDamage  estimated damage to you
    * @param bestTarget  estimated damage to the player it hurts most
    * @param worthIt     whether it clears your thresholds
    */
   public record Verdict(float selfDamage, float bestTarget, boolean worthIt) {
   }
}
