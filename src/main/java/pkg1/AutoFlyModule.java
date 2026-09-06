package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class AutoFlyModule extends Module {
   private static final String string_2 = "auto fly enabeled";
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Integer> val2_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("rocket-delay")
            .valOf2("Delay in ticks between automatic rocket uses while gliding.")
            .valOf3(200)
            .valOf4(1, 1200)
            .valOf5(20, 400)
            .getVal()
      );
   private final Setting<Integer> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("low-durability-threshold")
            .valOf2("Warns when the Elytra has this much durability or less remaining.")
            .valOf3(20)
            .valOf4(1, 432)
            .valOf5(1, 100)
            .getVal()
      );
   private int intVal;
   private boolean bool_2;

   public AutoFlyModule() {
      super(SwyzzyAddon.val2, "auto-fly", "Keeps your Elytra flight near a set height and uses rockets automatically.");
   }

   @Override
   public void run6() {
      this.intVal = 0;
      this.bool_2 = false;
   }

   @Override
   public void run7() {
      this.intVal = 0;
      this.bool_2 = false;
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null && class310.interactionManager != null) {
         this.run13();
         if (this.isEnabled2() && class310.player.isGliding()) {
            this.intVal++;
            if (this.intVal >= this.val2_2.getObject()) {
               this.intVal = 0;
               this.run15();
            }
         } else {
            this.intVal = 0;
         }
      }
   }

   @InternalHelper5
   private void run2(AdminDetectorModuleData var1) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null && var2.textRenderer != null) {
         var1.class332.drawText(var2.textRenderer, Text.literal("auto fly enabeled"), 10, 10, -1, true);
      }
   }

   private boolean isEnabled2() {
      ItemStack var1 = class310.player.getEquippedStack(EquipmentSlot.CHEST);
      return var1.isOf(Items.ELYTRA);
   }

   private void run13() {
      ItemStack var1 = class310.player.getEquippedStack(EquipmentSlot.CHEST);
      if (var1.isOf(Items.ELYTRA) && var1.isDamageable()) {
         int var2 = var1.getMaxDamage() - var1.getDamage();
         if (var2 > this.val3_2.getObject()) {
            this.bool_2 = false;
         } else if (!this.bool_2) {
            this.bool_2 = true;
            MutableText var3 = Text.literal("Auto Fly: Elytra durability is low (" + var2 + " left).");
            class310.player.sendMessage(var3, false);
            class310.player.sendMessage(var3, true);
            class310.player.playSound((SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0F, 0.8F);
         }
      } else {
         this.bool_2 = false;
      }
   }

   private void run15() {
      int var1 = this.getInt2();
      if (var1 != -1) {
         this.run3(var1);
      } else if (!this.isEnabled3()) {
         class310.player.sendMessage(Text.literal("Auto Fly: no rockets found in hotbar or inventory."), true);
      }
   }

   private void run3(int var1) {
      int var2 = class310.player.getInventory().getSelectedSlot();
      class310.player.getInventory().setSelectedSlot(var1);
      class310.interactionManager.interactItem(class310.player, Hand.MAIN_HAND);
      class310.player.swingHand(Hand.MAIN_HAND);
      class310.player.getInventory().setSelectedSlot(var2);
   }

   private boolean isEnabled3() {
      int var1 = this.getInt();
      if (var1 != -1 && class310.player.currentScreenHandler != null) {
         int var2 = class310.player.getInventory().getSelectedSlot();
         int var3 = this.intOf(var1);
         if (var3 == -1) {
            return false;
         } else {
            class310.interactionManager.clickSlot(class310.player.currentScreenHandler.syncId, var3, var2, SlotActionType.SWAP, class310.player);
            class310.interactionManager.interactItem(class310.player, Hand.MAIN_HAND);
            class310.player.swingHand(Hand.MAIN_HAND);
            class310.interactionManager.clickSlot(class310.player.currentScreenHandler.syncId, var3, var2, SlotActionType.SWAP, class310.player);
            return true;
         }
      } else {
         return false;
      }
   }

   private int getInt2() {
      for (int var1 = 0; var1 < 9; var1++) {
         ItemStack var2 = class310.player.getInventory().getStack(var1);
         if (var2.isOf(Items.FIREWORK_ROCKET)) {
            return var1;
         }
      }

      return -1;
   }

   private int getInt() {
      for (int var1 = 9; var1 <= 35; var1++) {
         ItemStack var2 = class310.player.getInventory().getStack(var1);
         if (var2.isOf(Items.FIREWORK_ROCKET)) {
            return var1;
         }
      }

      return -1;
   }

   private int intOf(int var1) {
      if (var1 >= 9 && var1 <= 35) {
         return var1;
      } else {
         return var1 >= 0 && var1 <= 8 ? 36 + var1 : -1;
      }
   }
}
