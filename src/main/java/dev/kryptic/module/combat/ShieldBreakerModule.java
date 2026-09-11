package dev.kryptic.module.combat;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.InventoryHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

/**
 * Swaps to an axe when the player you are looking at raises a shield.
 *
 * An axe hit disables a shield for five seconds; nothing else in the game
 * does, which is why this is worth automating and why it is this narrow. It
 * swaps, hits once, and puts your original slot back.
 *
 * <p>The slot is restored on disable and on losing the target as well as after
 * the hit. Leaving someone holding an axe they did not choose, mid-fight, is a
 * worse outcome than not breaking the shield.
 */
public class ShieldBreakerModule extends Module {

   public final SliderSetting reach = this.addSetting(new SliderSetting("Max Distance",
      "Ignore anyone further away than this", 3.5, 1.0, 6.0, 0.1, "m"));

   public final BooleanSetting restore = this.addSetting(new BooleanSetting("Swap Back",
      "Return to the slot you were holding after the hit", true));

   public final SliderSetting cooldown = this.addSetting(new SliderSetting("Retry Every",
      "Ticks before trying again on the same shield", 12.0, 1.0, 60.0, 1.0));

   public final BooleanSetting waitForCharge = this.addSetting(new BooleanSetting("Wait For Charge",
      "Only swing on a full attack cooldown -- an uncharged axe does not disable a shield", true));

   /** -1 when we are not currently holding someone else's slot open. */
   private int previousSlot = -1;
   private int wait;

   public ShieldBreakerModule() {
      super("Shield Breaker", "Axe-swaps to break a raised shield, then swaps back", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.previousSlot = -1;
      this.wait = 0;
   }

   @Override
   protected void onDisable() {
      this.restoreSlot();
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (player == null || client.interactionManager == null || client.currentScreen != null) {
         this.restoreSlot();
         return;
      }

      if (this.wait > 0) {
         this.wait--;
         // The swap back happens the tick after the hit, not this one: the
         // server needs to see the attack arrive while the axe is still the
         // held item.
         this.restoreSlot();
         return;
      }

      PlayerEntity target = this.blockingTarget(client, player);
      if (target == null) {
         this.restoreSlot();
         return;
      }

      if (this.waitForCharge.get() && player.getAttackCooldownProgress(0.0F) < 1.0F) {
         return;
      }

      int axe = this.findAxe(player);
      if (axe < 0) {
         return;
      }

      if (this.previousSlot < 0) {
         this.previousSlot = player.getInventory().getSelectedSlot();
      }

      InventoryHelper.selectHotbarSlot(axe);
      client.interactionManager.attackEntity(player, target);
      player.swingHand(Hand.MAIN_HAND);
      this.wait = this.cooldown.getInt();
   }

   private PlayerEntity blockingTarget(MinecraftClient client, ClientPlayerEntity player) {
      if (!(client.crosshairTarget instanceof EntityHitResult hit)) {
         return null;
      }

      Entity entity = hit.getEntity();
      if (!(entity instanceof PlayerEntity other) || other == player || !other.isAlive()) {
         return null;
      }

      if (!other.isBlocking() || player.distanceTo(other) > this.reach.getFloat()) {
         return null;
      }

      return other;
   }

   /** Any axe in the hotbar. Material does not matter -- all of them disable a shield. */
   private int findAxe(ClientPlayerEntity player) {
      for (int slot = 0; slot < 9; slot++) {
         ItemStack stack = player.getInventory().getStack(slot);
         if (stack.getItem() instanceof AxeItem) {
            return slot;
         }
      }

      return -1;
   }

   private void restoreSlot() {
      if (this.previousSlot >= 0) {
         if (this.restore.get()) {
            InventoryHelper.selectHotbarSlot(this.previousSlot);
         }

         this.previousSlot = -1;
      }
   }
}
