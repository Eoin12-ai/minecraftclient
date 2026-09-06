package dev.sixseven.util;

import java.util.function.Predicate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class InventoryHelper {
   private InventoryHelper() {
   }

   private static MinecraftClient mc() {
      return MinecraftClient.getInstance();
   }

   public static int toScreenSlot(int slot) {
      return slot < 9 ? slot + 36 : slot;
   }

   public static void selectHotbarSlot(int slot) {
      MinecraftClient client = mc();
      if (slot >= 0 && slot <= 8 && client.player != null && client.player.getInventory().getSelectedSlot() != slot) {
         client.player.getInventory().setSelectedSlot(slot);
         if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
         }
      }
   }

   public static void swapOffhand() {
      MinecraftClient client = mc();
      if (client.player != null && client.getNetworkHandler() != null) {
         client.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
      }
   }

   public static void swapInventoryToHotbar(int slot, int slot2) {
      MinecraftClient client = mc();
      if (client.player != null && client.interactionManager != null && slot >= 9 && slot <= 35 && slot2 >= 0 && slot2 <= 8) {
         client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, toScreenSlot(slot), slot2, SlotActionType.SWAP, client.player);
      }
   }

   public static void swap(int slot) {
      selectHotbarSlot(slot);
   }

   public static Hand handHolding(Item item) {
      ClientPlayerEntity player = mc().player;
      if (player == null) {
         return null;
      } else if (player.getMainHandStack().isOf(item)) {
         return Hand.MAIN_HAND;
      } else {
         return player.getOffHandStack().isOf(item) ? Hand.OFF_HAND : null;
      }
   }

   public static int getHotbarSlot(Item item) {
      ClientPlayerEntity player = mc().player;
      if (player == null) {
         return -1;
      } else {
         for (int temp = 0; temp < 9; temp++) {
            if (player.getInventory().getStack(temp).isOf(item)) {
               return temp;
            }
         }

         return -1;
      }
   }

   public static int findItemSlot(Item item) {
      ClientPlayerEntity player = mc().player;
      if (player == null) {
         return -1;
      } else {
         for (int temp = 0; temp < 36; temp++) {
            if (player.getInventory().getStack(temp).isOf(item)) {
               return temp;
            }
         }

         return -1;
      }
   }

   public static boolean swapToItem(Item item) {
      int slot = getHotbarSlot(item);
      if (slot < 0) {
         return false;
      } else {
         selectHotbarSlot(slot);
         return true;
      }
   }

   public static boolean swapToStack(Predicate<ItemStack> predicate) {
      ClientPlayerEntity player = mc().player;
      if (player == null) {
         return false;
      } else {
         for (int slot = 0; slot < 9; slot++) {
            if (predicate.test(player.getInventory().getStack(slot))) {
               selectHotbarSlot(slot);
               return true;
            }
         }

         return false;
      }
   }

   public static int findEmptyHotbarSlot() {
      ClientPlayerEntity player = mc().player;
      if (player == null) {
         return -1;
      } else {
         for (int temp = 0; temp < 9; temp++) {
            if (player.getInventory().getStack(temp).isEmpty()) {
               return temp;
            }
         }

         return -1;
      }
   }
}
