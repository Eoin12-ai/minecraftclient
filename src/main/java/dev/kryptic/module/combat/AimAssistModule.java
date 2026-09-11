package dev.kryptic.module.combat;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Nudges your aim toward a target you are already close to.
 *
 * This is the module in the client most likely to get you banned, so it is
 * built to be defensible rather than strong. It never snaps and never sets
 * your rotation: it scales the mouse movement you are already making, so
 * every angle you send still came from your hand. Let go of the mouse and it
 * does nothing at all.
 *
 * <p>That is also its honest limitation. It helps you track something you are
 * already sweeping past; it will not acquire a target for you. A version that
 * did would be a different module with a different risk, and worth saying no
 * to on purpose rather than by omission.
 */
public class AimAssistModule extends Module {

   public final ModeSetting targets = this.addSetting(new ModeSetting("Assists Against",
      "Which entities are worth helping you track",
      "Players Only", "Players Only", "Players & Monsters", "Anything Alive"));

   public final SliderSetting strength = this.addSetting(new SliderSetting("Strength",
      "How much of the correction to apply. Low is the point.", 25.0, 0.0, 100.0, 1.0, "%"));

   public final SliderSetting fov = this.addSetting(new SliderSetting("Cone",
      "Only assist within this many degrees of where you are already pointing",
      30.0, 1.0, 90.0, 1.0, "°"));

   public final SliderSetting range = this.addSetting(new SliderSetting("Max Distance",
      "Ignore anything further away", 4.5, 1.0, 12.0, 0.5, "m"));

   public final BooleanSetting onlyWhenMoving = this.addSetting(new BooleanSetting("Only While Aiming",
      "Do nothing unless the mouse is actually moving", true));

   public final BooleanSetting verticalToo = this.addSetting(new BooleanSetting("Assist Pitch",
      "Help with up and down as well as left and right", false));

   public AimAssistModule() {
      super("Aim Assist", "Scales your own mouse movement toward a nearby target", Category.COMBAT);
   }

   /**
    * The extra yaw and pitch to add to this frame's mouse movement.
    *
    * @param deltaX raw horizontal mouse delta this frame
    * @param deltaY raw vertical mouse delta this frame
    * @return {@code null} when nothing should change, else a two-element array
    */
   public double[] assist(double deltaX, double deltaY) {
      if (!this.isEnabled()) {
         return null;
      }

      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (player == null || client.world == null || client.currentScreen != null) {
         return null;
      }

      // Nothing to scale means nothing to do. This is what keeps a still mouse
      // completely inert rather than slowly drifting onto someone.
      if (this.onlyWhenMoving.get() && Math.abs(deltaX) < 0.01 && Math.abs(deltaY) < 0.01) {
         return null;
      }

      LivingEntity target = this.pick(client, player);
      if (target == null) {
         return null;
      }

      float wantYaw = this.yawTo(player, target);
      float yawError = wrap(wantYaw - player.getYaw());
      double factor = this.strength.get() / 100.0;

      double outX = yawError * factor * 0.15;
      double outY = 0.0;
      if (this.verticalToo.get()) {
         float pitchError = this.pitchTo(player, target) - player.getPitch();
         outY = pitchError * factor * 0.15;
      }

      if (Math.abs(outX) < 0.0001 && Math.abs(outY) < 0.0001) {
         return null;
      }

      return new double[]{outX, outY};
   }

   private LivingEntity pick(MinecraftClient client, ClientPlayerEntity player) {
      double maxRange = this.range.get();
      double cone = this.fov.get();
      LivingEntity best = null;
      double bestError = Double.MAX_VALUE;

      for (Entity entity : client.world.getEntities()) {
         if (!(entity instanceof LivingEntity living) || entity == player || !living.isAlive()) {
            continue;
         }

         if (!this.wanted(living) || player.distanceTo(living) > maxRange) {
            continue;
         }

         double error = Math.abs(wrap(this.yawTo(player, living) - player.getYaw()));
         if (error <= cone && error < bestError) {
            bestError = error;
            best = living;
         }
      }

      return best;
   }

   private boolean wanted(LivingEntity living) {
      if (this.targets.is("Players Only")) {
         return living instanceof PlayerEntity;
      }

      if (this.targets.is("Players & Monsters")) {
         return living instanceof PlayerEntity || living instanceof Monster;
      }

      return true;
   }

   // getPos and getHeight are both used elsewhere in this codebase, so they are
   // known to map on this version; java.lang.Math.atan2 has no mapping at all.
   // Eye height is added rather than read from getEyePos so nothing here
   // depends on a name this codebase has never compiled.
   private static final double EYE_HEIGHT = 1.62;

   /**
    * Fold an angle into -180..180.
    *
    * MathHelper has this, but nothing else in the codebase has ever compiled a
    * call to it on this version, and it is three lines. Every other call in
    * this file is either java.lang.Math or a name already proven elsewhere.
    */
   private static float wrap(float degrees) {
      float d = degrees % 360.0F;
      if (d >= 180.0F) {
         d -= 360.0F;
      }

      if (d < -180.0F) {
         d += 360.0F;
      }

      return d;
   }

   /**
    * An entity's position as a vector.
    *
    * Entity.getPos() does not exist on this version -- every getPos in this
    * codebase is on a Chunk, which is why grepping for the call name alone
    * proved nothing. getX/getY/getZ are what the renderers use.
    */
   private static Vec3d posOf(net.minecraft.entity.Entity entity) {
      return new Vec3d(entity.getX(), entity.getY(), entity.getZ());
   }

   private float yawTo(ClientPlayerEntity player, LivingEntity target) {
      Vec3d eye = posOf(player).add(0.0, EYE_HEIGHT, 0.0);
      Vec3d at = posOf(target).add(0.0, target.getHeight() * 0.5, 0.0);
      return (float) (Math.atan2(at.z - eye.z, at.x - eye.x) * (180.0 / Math.PI)) - 90.0F;
   }

   private float pitchTo(ClientPlayerEntity player, LivingEntity target) {
      Vec3d eye = posOf(player).add(0.0, EYE_HEIGHT, 0.0);
      Vec3d at = posOf(target).add(0.0, target.getHeight() * 0.5, 0.0);
      double dx = at.x - eye.x;
      double dz = at.z - eye.z;
      double flat = Math.sqrt(dx * dx + dz * dz);
      return (float) (-(Math.atan2(at.y - eye.y, flat) * (180.0 / Math.PI)));
   }
}
