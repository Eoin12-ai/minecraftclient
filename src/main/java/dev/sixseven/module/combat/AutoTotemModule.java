package dev.sixseven.module.combat;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.SliderSetting;
import dev.sixseven.util.InventoryHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;

public class AutoTotemModule extends Module {
   public final SliderSetting delay = this.addSetting(new SliderSetting("Delay", "", 4.0, 1.0, 40.0, 1.0));
   public final BooleanSetting forceTotem = this.addSetting(new BooleanSetting("Force Totem", "", false));
   public final BooleanSetting hotbarTotem = this.addSetting(new BooleanSetting("Hotbar Totem", "", false));
   public final SliderSetting hotbarSlot = this.addSetting(new SliderSetting("Hotbar Slot", "", 1.0, 1.0, 9.0, 1.0));
   public final SliderSetting hotbarDelay = this.addSetting(new SliderSetting("Hotbar Delay", "", 4.0, 1.0, 40.0, 1.0));
   private int tickCounter;
   private int pendingHotbarSlot = -1;

   public AutoTotemModule() {
      super("Auto Totem", "Totem to offhand/hotbar — hotbar select + F swap (+ SWAP from main inv when needed)", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.tickCounter = 0;
      this.pendingHotbarSlot = -1;
   }

   @Override
   protected void onDisable() {
      this.tickCounter = 0;
      this.pendingHotbarSlot = -1;
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.interactionManager != null && canRun(client)) {
         this.tickCounter++;
         int delay2 = this.hotbarTotem.get() && this.needsHotbarWork(client) && !this.needsOffhandWork(client) ? this.hotbarDelay.getInt() : this.delay.getInt();
         if (this.tickCounter >= delay2) {
            this.tickCounter = 0;
            ClientPlayerEntity player = client.player;
            int slot = this.hotbarSlot.getInt() - 1;
            if (this.hotbarTotem.get() && this.needsHotbarWork(client)) {
               if (player.getInventory().getStack(slot).isOf(Items.TOTEM_OF_UNDYING)) {
                  InventoryHelper.selectHotbarSlot(slot);
               } else {
                  int n = findTotemInMainInventory(client);
                  if (n != -1 && canContainerClick(client)) {
                     InventoryHelper.swapInventoryToHotbar(n, slot);
                  }
               }
            } else if (!this.needsOffhandWork(client)) {
               this.pendingHotbarSlot = -1;
            } else if (!player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) || this.forceTotem.get()) {
               int localX = findTotemInHotbar(client);
               if (localX != -1) {
                  this.pendingHotbarSlot = localX;
                  InventoryHelper.selectHotbarSlot(localX);
                  if (player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
                     InventoryHelper.swapOffhand();
                  }
               } else if (this.pendingHotbarSlot >= 0) {
                  InventoryHelper.selectHotbarSlot(this.pendingHotbarSlot);
                  if (player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
                     InventoryHelper.swapOffhand();
                     this.pendingHotbarSlot = -1;
                  }
               } else {
                  int localZ = findTotemInMainInventory(client);
                  if (localZ != -1 && canContainerClick(client)) {
                     int slot2 = slot;
                     if (!this.hotbarTotem.get()) {
                        slot2 = player.getInventory().getSelectedSlot();
                     }

                     InventoryHelper.swapInventoryToHotbar(localZ, slot2);
                     this.pendingHotbarSlot = slot2;
                  } else if (player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
                     InventoryHelper.swapOffhand();
                  }
               }
            }
         }
      }
   }

   private static boolean canRun(MinecraftClient client) {
      return client.currentScreen == null || client.currentScreen instanceof InventoryScreen;
   }

   private static boolean canContainerClick(MinecraftClient client) {
      return client.currentScreen != null || !isMoving(client);
   }

   private static boolean isMoving(MinecraftClient client) {
      if (client.player == null) {
         return false;
      } else if (!client.options.forwardKey.isPressed()
         && !client.options.backKey.isPressed()
         && !client.options.leftKey.isPressed()
         && !client.options.rightKey.isPressed()
         && !client.options.jumpKey.isPressed()
         && !client.player.isSprinting()
         && !client.player.isSneaking()) {
         double d = client.player.getVelocity().x;
         double coord = client.player.getVelocity().z;
         return d * d + coord * coord > 0.0025;
      } else {
         return true;
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
}
