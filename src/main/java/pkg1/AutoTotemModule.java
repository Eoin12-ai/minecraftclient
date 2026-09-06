package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public final class AutoTotemModule extends Module {
   private static final int intVal = 45;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Double> val2_2 = this.val_2
      .addSetting(
         new AutoTotemModuleHelper2()
            .valOf("health")
            .valOf2("Only swap a totem in once health plus absorption drops to this value. 20 means always.")
            .valOf3(20.0)
            .valOf5(1.0, 20.0)
            .getVal()
      );
   private final Setting<Integer> val3_2 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper6().valOf("delay").valOf2("Ticks to wait between two swap attempts.").valOf3(1).valOf5(0, 20).getVal());
   private final Setting<Boolean> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("work-in-inventory").valOf2("Also swap while the inventory screen is open.").valOf3(true).getVal()
      );
   private int intVal2;

   public AutoTotemModule() {
      super(SwyzzyAddon.val, "auto-totem", "Moves a totem of undying into your off hand automatically.");
   }

   @Override
   public void run6() {
      this.intVal2 = 0;
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.interactionManager != null) {
         if (class310.currentScreen == null || this.val4.getObject() && class310.currentScreen instanceof InventoryScreen) {
            if (class310.player.currentScreenHandler == class310.player.playerScreenHandler) {
               if (this.intVal2 > 0) {
                  this.intVal2--;
               } else if (!class310.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
                  if (!(class310.player.getHealth() + class310.player.getAbsorptionAmount() > this.val2_2.getObject())) {
                     int var2 = this.getInt();
                     if (var2 >= 0) {
                        int var3 = class310.player.currentScreenHandler.syncId;
                        class310.interactionManager.clickSlot(var3, var2, 0, SlotActionType.PICKUP, class310.player);
                        class310.interactionManager.clickSlot(var3, 45, 0, SlotActionType.PICKUP, class310.player);
                        if (!class310.player.currentScreenHandler.getCursorStack().isEmpty()) {
                           class310.interactionManager.clickSlot(var3, var2, 0, SlotActionType.PICKUP, class310.player);
                        }

                        this.intVal2 = this.val3_2.getObject();
                     }
                  }
               }
            }
         }
      }
   }

   private int getInt() {
      for (int var1 = 0; var1 < class310.player.getInventory().getMainStacks().size(); var1++) {
         ItemStack var2 = class310.player.getInventory().getStack(var1);
         if (var2.isOf(Items.TOTEM_OF_UNDYING)) {
            return var1 < 9 ? var1 + 36 : var1;
         }
      }

      return -1;
   }

   @Override
   public String getString2() {
      return class310.player != null && class310.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) ? "ready" : "empty";
   }
}
