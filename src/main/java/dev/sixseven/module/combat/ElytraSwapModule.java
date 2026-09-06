package dev.sixseven.module.combat;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.KeybindSetting;
import dev.sixseven.settings.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class ElytraSwapModule extends Module {
   public final KeybindSetting activateKey = this.addSetting(new KeybindSetting("Activate Key", "", 71));
   public final SliderSetting swapDelay = this.addSetting(new SliderSetting("Swap Delay", "", 0.0, 0.0, 20.0, 1.0));
   public final BooleanSetting switchBack = this.addSetting(new BooleanSetting("Switch Back", "", true));
   public final SliderSetting switchDelay = this.addSetting(new SliderSetting("Switch Delay", "", 0.0, 0.0, 20.0, 1.0));
   public final BooleanSetting moveToSlot = this.addSetting(new BooleanSetting("Move To Slot", "", true));
   public final SliderSetting elytraSlot = this.addSetting(new SliderSetting("Elytra Slot", "", 9.0, 1.0, 9.0, 1.0));
   private boolean keyWasDown;
   private boolean swappedToElytra;
   private int tickCounter;
   private boolean waitingForSwap;
   private boolean waitingForSwitchBack;

   public ElytraSwapModule() {
      super("Elytra Swap", "Swap between Elytra and Chestplate with a keybind", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.resetState();
   }

   @Override
   protected void onDisable() {
      this.resetState();
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.interactionManager != null) {
         if (this.waitingForSwap) {
            this.tickCounter++;
            if (this.tickCounter >= this.swapDelay.getInt()) {
               this.performSwap(client);
               this.waitingForSwap = false;
               if (this.switchBack.get()) {
                  this.waitingForSwitchBack = true;
                  this.tickCounter = 0;
               }
            }
         } else if (this.waitingForSwitchBack) {
            this.tickCounter++;
            if (this.tickCounter >= this.switchDelay.getInt()) {
               this.performSwap(client);
               this.waitingForSwitchBack = false;
            }
         } else {
            int n = this.activateKey.get();
            if (n != -1) {
               long l = client.getWindow().getHandle();
               boolean pressed = n <= 7 ? GLFW.glfwGetMouseButton(l, n) == 1 : GLFW.glfwGetKey(l, n) == 1;
               if (pressed && !this.keyWasDown) {
                  this.waitingForSwap = true;
                  this.tickCounter = 0;
               }

               this.keyWasDown = pressed;
            }
         }
      }
   }

   private void performSwap(MinecraftClient client) {
      ItemStack stack = client.player.getEquippedStack(EquipmentSlot.CHEST);
      boolean ok = stack.isOf(Items.ELYTRA);
      int bestSlot = -1;
      if (ok) {
         for (int n = 0; n < 36; n++) {
            ItemStack hotbarStack = client.player.getInventory().getStack(n);
            EquippableComponent equippableComponent = (EquippableComponent)hotbarStack.get(DataComponentTypes.EQUIPPABLE);
            if (equippableComponent != null && equippableComponent.slot() == EquipmentSlot.CHEST && !hotbarStack.isOf(Items.ELYTRA)) {
               bestSlot = n;
               break;
            }
         }
      } else {
         if (this.moveToSlot.get()) {
            int slot = this.elytraSlot.getInt() - 1;
            ItemStack stack3 = client.player.getInventory().getStack(slot);
            if (stack3.isOf(Items.ELYTRA)) {
               bestSlot = slot;
            }
         }

         if (bestSlot == -1) {
            for (int offset = 0; offset < 36; offset++) {
               if (client.player.getInventory().getStack(offset).isOf(Items.ELYTRA)) {
                  bestSlot = offset;
                  break;
               }
            }
         }
      }

      if (bestSlot != -1) {
         int slot2 = bestSlot < 9 ? bestSlot + 36 : bestSlot;
         byte b = 6;
         client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, slot2, 0, SlotActionType.PICKUP, client.player);
         client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, b, 0, SlotActionType.PICKUP, client.player);
         client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, slot2, 0, SlotActionType.PICKUP, client.player);
         this.swappedToElytra = !ok;
      }
   }

   private void resetState() {
      this.keyWasDown = false;
      this.swappedToElytra = false;
      this.tickCounter = 0;
      this.waitingForSwap = false;
      this.waitingForSwitchBack = false;
   }
}
