package dev.kryptic.module.combat;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Swaps between your elytra and a chestplate on one key.
 *
 * The module has no key of its own — it uses the module's keybind, so it lives
 * in the same list as everything else you have bound rather than inventing a
 * second place to look.
 *
 * <p>The swap is three clicks, not one: pick up the replacement, drop it into
 * the chest slot, put what came back where the replacement was. There is no
 * single "equip this" packet a client can send, so this is the whole move, and
 * it has to happen inside one tick or the server sees a half-finished
 * inventory.
 */
public class ElytraSwapModule extends Module {

   /** The chest armour slot in the player's own screen handler. */
   private static final int CHEST_SLOT = 6;

   public final SliderSetting cooldown = this.addSetting(new SliderSetting("Cooldown",
      "Ticks before the bind will swap again", 6.0, 1.0, 40.0, 1.0));

   public final BooleanSetting preferSlot = this.addSetting(new BooleanSetting("Prefer A Slot",
      "Look for the elytra in one chosen slot before searching the rest", false));

   public final SliderSetting slot = this.addSetting(new SliderSetting("Elytra Slot",
      "Which hotbar slot to look in first", 9.0, 1.0, 9.0, 1.0));

   private long lastSwap;

   public ElytraSwapModule() {
      super("Elytra Swap", "Trades your chestplate for an elytra and back", Category.COMBAT);
   }

   /**
    * Each toggle swaps once — on the way on and on the way off alike.
    *
    * The obvious shape for a press-to-act module is to swap in onEnable and
    * switch itself back off, but that cannot work here: setEnabled sets the
    * flag before calling onEnable, so disabling from inside it fires the
    * toggle callbacks in the order false, true. The module ends up off while
    * the module list and the toast both believe it is on.
    *
    * Swapping on both edges avoids the re-entrancy entirely and gives the same
    * feel: one press, one swap, whichever way the toggle went.
    */
   @Override
   protected void onEnable() {
      this.swap();
   }

   @Override
   protected void onDisable() {
      this.swap();
   }

   private void swap() {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (player == null || client.interactionManager == null || client.currentScreen != null) {
         return;
      }

      // Guards a held bind firing both edges in consecutive ticks, which would
      // swap and immediately swap back.
      long now = System.currentTimeMillis();
      if (now - this.lastSwap < this.cooldown.getInt() * 50L) {
         return;
      }

      this.lastSwap = now;

      boolean wearingElytra = player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
      int source = wearingElytra ? this.findChestplate(player) : this.findElytra(player);
      if (source < 0) {
         return;
      }

      // Inventory indices and screen-handler indices are not the same thing:
      // the hotbar is 0-8 in the inventory and 36-44 on screen.
      int screenSlot = source < 9 ? source + 36 : source;
      int syncId = player.currentScreenHandler.syncId;

      client.interactionManager.clickSlot(syncId, screenSlot, 0, SlotActionType.PICKUP, player);
      client.interactionManager.clickSlot(syncId, CHEST_SLOT, 0, SlotActionType.PICKUP, player);
      client.interactionManager.clickSlot(syncId, screenSlot, 0, SlotActionType.PICKUP, player);
   }

   private int findElytra(ClientPlayerEntity player) {
      if (this.preferSlot.get()) {
         int wanted = this.slot.getInt() - 1;
         if (player.getInventory().getStack(wanted).isOf(Items.ELYTRA)) {
            return wanted;
         }
      }

      for (int i = 0; i < 36; i++) {
         if (player.getInventory().getStack(i).isOf(Items.ELYTRA)) {
            return i;
         }
      }

      return -1;
   }

   /**
    * Anything that goes in the chest slot and is not an elytra.
    *
    * Asking the item what slot it equips to, rather than listing the four
    * chestplate materials, means a modded or future chestplate works without
    * anyone updating a list here.
    */
   private int findChestplate(ClientPlayerEntity player) {
      for (int i = 0; i < 36; i++) {
         ItemStack stack = player.getInventory().getStack(i);
         if (stack.isOf(Items.ELYTRA)) {
            continue;
         }

         EquippableComponent equippable = stack.get(DataComponentTypes.EQUIPPABLE);
         if (equippable != null && equippable.slot() == EquipmentSlot.CHEST) {
            return i;
         }
      }

      return -1;
   }
}
