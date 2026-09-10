package dev.kryptic.module.combat;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.AnchorHelper;
import dev.kryptic.util.InventoryHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Sets off two anchors back to back.
 *
 * One anchor rarely finishes a fight; two in quick succession does, and doing
 * it by hand means charging the second while the first is still exploding.
 * This runs both, remembering the first anchor so it can come back to it, and
 * gives up cleanly if either one stops being an anchor.
 *
 * <p>Like every anchor module here it stands down while you are crouched.
 * Sneaking makes Minecraft place your held item rather than use the block, so
 * running the sequence crouched puts glowstone on the floor instead of
 * charging anything — which is precisely the bug this module used to have.
 */
public class DoubleAnchorModule extends Module {

   public final SliderSetting gap = this.addSetting(new SliderSetting("Gap Between",
      "Ticks between the first detonation and starting the second", 4.0, 0.0, 40.0, 1.0));

   public final SliderSetting stepDelay = this.addSetting(new SliderSetting("Step Delay",
      "Ticks between each swap and click", 2.0, 0.0, 20.0, 1.0));

   public final SliderSetting weaponSlot = this.addSetting(new SliderSetting("Detonate Slot",
      "Hotbar slot to hold when setting one off -- anything but glowstone", 1.0, 1.0, 9.0, 1.0));

   public final BooleanSetting reuseFirst = this.addSetting(new BooleanSetting("Reuse The Same Anchor",
      "Recharge and blow the first anchor again, instead of needing a second one", true));

   public final BooleanSetting swapBack = this.addSetting(new BooleanSetting("Restore Slot",
      "Go back to the slot you were holding when the sequence ends", true));


   private int step;
   private int ticks;
   private int round;
   private BlockPos target;
   private int entrySlot = -1;

   public DoubleAnchorModule() {
      super("Double Anchor", "Charges and sets off two anchors in sequence", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.reset();
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      BlockHitResult hit = AnchorHelper.anchorUnderCrosshair();
      if (player != null && hit != null) {
         this.target = hit.getBlockPos();
         this.entrySlot = player.getInventory().getSelectedSlot();
         this.step = 1;
      }
   }

   @Override
   protected void onDisable() {
      this.restore();
      this.reset();
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (player == null || client.interactionManager == null || this.step == 0) {
         return;
      }

      this.ticks++;
      BlockHitResult hit = AnchorHelper.anchorUnderCrosshair();
      if (hit == null || this.target == null || !this.target.equals(hit.getBlockPos())) {
         this.restore();
         this.reset();
         return;
      }

      switch (this.step) {
         case 1 -> {                            // hold glowstone
            if (this.ticks >= this.stepDelay.getInt()) {
               if (AnchorHelper.charges(this.target) > 0) {
                  this.advance(3);              // already charged, go straight to it
               } else if (InventoryHelper.swapToItem(Items.GLOWSTONE)) {
                  this.advance(2);
               } else {
                  this.restore();
                  this.reset();                 // no glowstone anywhere in the hotbar
               }
            }
         }
         case 2 -> {                            // charge it
            if (this.ticks >= this.stepDelay.getInt()) {
               if (AnchorHelper.charge(hit)) {
                  this.advance(3);
               } else {
                  this.restore();
                  this.reset();
               }
            }
         }
         case 3 -> {                            // hold something that is not glowstone
            if (this.ticks >= this.stepDelay.getInt()) {
               InventoryHelper.selectHotbarSlot(this.weaponSlot.getInt() - 1);
               this.advance(4);
            }
         }
         case 4 -> {                            // set it off
            if (this.ticks >= this.stepDelay.getInt()) {
               if (safeToBlow()) {
                  AnchorHelper.detonate(hit);
               }

               this.round++;
               if (this.round >= 2 || !this.reuseFirst.get()) {
                  this.restore();
                  this.reset();
               } else {
                  this.advance(5);
               }
            }
         }
         case 5 -> {                            // breathe, then go round again
            if (this.ticks >= this.gap.getInt()) {
               this.advance(1);
            }
         }
         default -> this.reset();
      }
   }

   /** Whether Safe Anchor, if the user has it on, will allow this detonation. */
   private static boolean safeToBlow() {
      dev.kryptic.module.ModuleManager modules = dev.kryptic.KrypticClient.modules();
      if (modules == null || modules.safeAnchor == null) {
         return true;
      }

      return modules.safeAnchor.allows();
   }

   private void advance(int next) {
      this.step = next;
      this.ticks = 0;
   }

   private void restore() {
      if (this.swapBack.get() && this.entrySlot >= 0) {
         InventoryHelper.selectHotbarSlot(this.entrySlot);
      }
   }

   private void reset() {
      this.step = 0;
      this.ticks = 0;
      this.round = 0;
      this.target = null;
      this.entrySlot = -1;
   }
}
