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
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

/**
 * Attacks whatever your crosshair is already on.
 *
 * It aims nothing and moves nothing — it only decides when to click. That is
 * the whole design: rotation is the part of a combat module a server can see,
 * so leaving it out entirely means this cannot betray you through your angles.
 *
 * <p>By default it waits for the attack cooldown to come back, because a swing
 * at 40% charge does 40% damage. Turning that off swings faster and hits
 * softer, which is worth it with some weapons and not with others.
 */
public class TriggerBotModule extends Module {

   public final ModeSetting targets = this.addSetting(new ModeSetting("Attacks",
      "Which entities under the crosshair are worth hitting",
      "Players & Monsters", "Players Only", "Players & Monsters", "Anything Alive"));

   public final SliderSetting delay = this.addSetting(new SliderSetting("Swing Delay",
      "Ticks to wait between attacks", 2.0, 0.0, 20.0, 1.0));

   public final BooleanSetting fullCharge = this.addSetting(new BooleanSetting("Wait For Charge",
      "Only swing once the attack cooldown is full, so hits do their real damage", true));

   public final BooleanSetting skipShielded = this.addSetting(new BooleanSetting("Skip Blocking",
      "Do not swing at a player who is holding up a shield", false));

   public final BooleanSetting skipBabies = this.addSetting(new BooleanSetting("Leave Babies",
      "Do not attack baby animals", true));

   public final SliderSetting reach = this.addSetting(new SliderSetting("Max Distance",
      "Ignore targets further away than this", 3.5, 1.0, 6.0, 0.1, "m"));

   private int cooldown;

   public TriggerBotModule() {
      super("Trigger Bot", "Attacks what is already under your crosshair", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.cooldown = 0;
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (player == null || client.interactionManager == null || client.currentScreen != null) {
         return;
      }

      if (this.cooldown > 0) {
         this.cooldown--;
         return;
      }

      if (!(client.crosshairTarget instanceof EntityHitResult hit)) {
         return;
      }

      Entity entity = hit.getEntity();
      if (!(entity instanceof LivingEntity living) || entity == player || !living.isAlive()) {
         return;
      }

      if (player.distanceTo(entity) > this.reach.getFloat()) {
         return;
      }

      if (!this.wanted(living)) {
         return;
      }

      if (this.skipShielded.get() && living instanceof PlayerEntity other && other.isBlocking()) {
         return;
      }

      if (this.skipBabies.get() && living instanceof PassiveEntity passive && passive.isBaby()) {
         return;
      }

      // getAttackCooldownProgress(0) is the value right now rather than part
      // way through the frame, which is what a tick-rate decision wants.
      if (this.fullCharge.get() && player.getAttackCooldownProgress(0.0F) < 1.0F) {
         return;
      }

      client.interactionManager.attackEntity(player, entity);
      player.swingHand(Hand.MAIN_HAND);
      this.cooldown = this.delay.getInt();
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
}
