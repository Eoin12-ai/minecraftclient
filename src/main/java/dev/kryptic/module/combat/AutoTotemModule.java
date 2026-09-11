package dev.kryptic.module.combat;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.InventoryHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;

/**
 * Keeps a totem in your offhand.
 *
 * The swap is only attempted when it is actually needed and actually possible,
 * rather than every tick: a client that spams inventory packets is both
 * useless and obvious. "Needed" means the offhand is not already holding one;
 * "possible" means no screen is open, because a container click while you have
 * a chest open goes to the chest.
 *
 * <p>Two thresholds rather than one. Health decides when it becomes urgent,
 * and the refill interval decides how often to retry once it has failed —
 * usually because there is no totem left, in which case retrying fast just
 * burns packets.
 */
public class AutoTotemModule extends Module {

   public final BooleanSetting always = this.addSetting(new BooleanSetting("Always Hold",
      "Keep a totem in the offhand at all times, not only when hurt", true));

   public final SliderSetting threshold = this.addSetting(new SliderSetting("Health Trigger",
      "With Always Hold off, only swap below this much health", 12.0, 1.0, 20.0, 1.0));

   public final SliderSetting interval = this.addSetting(new SliderSetting("Retry Every",
      "Ticks between attempts", 4.0, 1.0, 40.0, 1.0));

   public final BooleanSetting keepOffhand = this.addSetting(new BooleanSetting("Guard The Slot",
      "Put the totem back the moment it is used", true));

   private int cooldown;

   public AutoTotemModule() {
      super("Auto Totem", "Moves a totem into your offhand and keeps it there", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.cooldown = 0;
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (player == null || client.interactionManager == null) {
         return;
      }

      // A container click while a screen is open is sent to that screen's
      // handler, so it would move an item in the chest rather than the
      // inventory. Waiting is the only correct behaviour here.
      if (client.currentScreen != null) {
         return;
      }

      if (this.cooldown > 0) {
         this.cooldown--;
         return;
      }

      if (player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
         return;
      }

      if (!this.always.get() && !this.keepOffhand.get()
            && player.getHealth() > this.threshold.getFloat()) {
         return;
      }

      this.cooldown = this.interval.getInt();

      // The offhand swap always takes from the selected hotbar slot, so the
      // totem has to reach the hotbar first. These are two different moves and
      // the helper only does the first one: swapToItem searches the hotbar
      // alone, so a totem sitting in the main inventory needs a container
      // swap before it can be selected at all.
      int hotbar = InventoryHelper.getHotbarSlot(Items.TOTEM_OF_UNDYING);
      if (hotbar >= 0) {
         InventoryHelper.selectHotbarSlot(hotbar);
         InventoryHelper.swapOffhand();
         return;
      }

      int stored = InventoryHelper.findItemSlot(Items.TOTEM_OF_UNDYING);
      if (stored < 9) {
         return;                                  // none anywhere, or already handled above
      }

      // Pull it into the slot you are holding, then let the next tick move it
      // to the offhand -- the server has to see the container swap land before
      // the offhand swap means anything.
      InventoryHelper.swapInventoryToHotbar(stored, player.getInventory().getSelectedSlot());
   }
}
