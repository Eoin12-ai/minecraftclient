package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public final class KeyPearlModule extends Module {
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Boolean> val2_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("missing-pearl-message")
            .valOf2("Shows a message when no ender pearl is in the hotbar.")
            .valOf3(true)
            .getVal()
      );

   public KeyPearlModule() {
      super(SwyzzyAddon.val2, "key-pearl", "Throws an ender pearl from the hotbar when its keybind is pressed.");
   }

   @Override
   public void run6() {
      try {
         this.run12();
      } finally {
         this.run(false);
      }
   }

   private void run12() {
      if (class310.player != null && class310.interactionManager != null) {
         int var1 = this.getInt5();
         if (var1 == -1) {
            if (this.val2_2.getObject()) {
               class310.player.sendMessage(Text.literal("Key Pearl: no ender pearl in hotbar."), true);
            }
         } else {
            int var2 = class310.player.getInventory().getSelectedSlot();
            class310.player.getInventory().setSelectedSlot(var1);
            class310.interactionManager.interactItem(class310.player, Hand.MAIN_HAND);
            class310.player.swingHand(Hand.MAIN_HAND);
            class310.player.getInventory().setSelectedSlot(var2);
         }
      }
   }

   private int getInt5() {
      for (int var1 = 0; var1 < 9; var1++) {
         ItemStack var2 = class310.player.getInventory().getStack(var1);
         if (var2.isOf(Items.ENDER_PEARL)) {
            return var1;
         }
      }

      return -1;
   }
}
