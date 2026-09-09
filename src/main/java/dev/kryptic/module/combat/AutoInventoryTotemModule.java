package dev.kryptic.module.combat;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoInventoryTotemModule extends Module {
   public final SliderSetting delay = this.addSetting(new SliderSetting("Delay", "", 4.0, 1.0, 40.0, 1.0));
   public final BooleanSetting forceTotem = this.addSetting(new BooleanSetting("Force Totem", "", false));
   public final BooleanSetting hotbarTotem = this.addSetting(new BooleanSetting("Hotbar Totem", "", false));
   public final SliderSetting hotbarSlot = this.addSetting(new SliderSetting("Hotbar Slot", "", 1.0, 1.0, 9.0, 1.0));
   public final SliderSetting hotbarDelay = this.addSetting(new SliderSetting("Hotbar Delay", "", 4.0, 1.0, 40.0, 1.0));
   private int tickCounter;

   public AutoInventoryTotemModule() {
      super("Inv Totem", "Moves totems only while inventory (E) is open — click-based, not world-auto", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.tickCounter = 0;
   }

   @Override
   protected void onDisable() {
      this.tickCounter = 0;
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.interactionManager != null && client.currentScreen instanceof InventoryScreen) {
         this.tickCounter++;
         int delay2 = this.hotbarTotem.get() && this.needsHotbarWork(client) && !this.needsOffhandWork(client) ? this.hotbarDelay.getInt() : this.delay.getInt();
         if (this.tickCounter >= delay2) {
            this.tickCounter = 0;
            if (this.hotbarTotem.get() && this.needsHotbarWork(client)) {
               int n = findTotemInMainInventory(client);
               if (n != -1) {
                  int slot = this.hotbarSlot.getInt() - 1;
                  if (slot >= 0 && slot <= 8) {
                     this.performHotbarSwap(client, n, slot);
                  }
               }
            } else if (this.needsOffhandWork(client)) {
               int offset = findTotemForOffhand(client);
               if (offset != -1) {
                  this.performOffhandSwap(client, offset);
               }
            }
         }
      }
   }

   private boolean needsOffhandWork(MinecraftClient client) {
      boolean stack = client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
      return this.forceTotem.get() || !stack;
   }

   private boolean needsHotbarWork(MinecraftClient client) {
      if (!this.hotbarTotem.get()) {
         return false;
      } else {
         int slot = this.hotbarSlot.getInt() - 1;
         return !client.player.getInventory().getStack(slot).isOf(Items.TOTEM_OF_UNDYING);
      }
   }

   private static int findTotemInMainInventory(MinecraftClient client) {
      for (int temp = 9; temp < 36; temp++) {
         if (client.player.getInventory().getStack(temp).isOf(Items.TOTEM_OF_UNDYING)) {
            return temp;
         }
      }

      return -1;
   }

   private static int findTotemInHotbar(MinecraftClient client) {
      for (int temp = 0; temp < 9; temp++) {
         if (client.player.getInventory().getStack(temp).isOf(Items.TOTEM_OF_UNDYING)) {
            return temp;
         }
      }

      return -1;
   }

   private static int findTotemForOffhand(MinecraftClient client) {
      int n = findTotemInMainInventory(client);
      return n != -1 ? n : findTotemInHotbar(client);
   }

   private static int toScreenSlot(int slot) {
      return slot < 9 ? slot + 36 : slot;
   }

   private void performOffhandSwap(MinecraftClient client, int slot) {
      int n = client.player.currentScreenHandler.syncId;
      int slot2 = toScreenSlot(slot);
      client.interactionManager.clickSlot(n, slot2, 0, SlotActionType.PICKUP, client.player);
      client.interactionManager.clickSlot(n, 45, 0, SlotActionType.PICKUP, client.player);
      client.interactionManager.clickSlot(n, slot2, 0, SlotActionType.PICKUP, client.player);
   }

   private void performHotbarSwap(MinecraftClient client, int slot, int slot2) {
      int n = client.player.currentScreenHandler.syncId;
      client.interactionManager.clickSlot(n, slot, slot2, SlotActionType.SWAP, client.player);
   }
}
