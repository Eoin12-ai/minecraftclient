package dev.kryptic.module.combat;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.AnchorHelper;
import dev.kryptic.util.InventoryHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

/**
 * Charges and sets off the respawn anchor you are looking at.
 *
 * This is the sequence you would do by hand — glowstone, click, swap off
 * glowstone, click — run at a fixed rhythm so it does not depend on how fast
 * you can hit two keys. It does not choose targets and it does not aim: the
 * anchor is whichever one is already under your crosshair.
 *
 * <p>It will not fire while you are crouched. Sneaking makes Minecraft place
 * the held item instead of using the block, so a macro that ignores it puts a
 * glowstone block on the floor instead of charging anything.
 */
public class AutoAnchorModule extends Module {

   public final BooleanSetting requireHold = this.addSetting(new BooleanSetting("Hold Right Click",
      "Only run while the right mouse button is held", true));

   public final SliderSetting chargeDelay = this.addSetting(new SliderSetting("Charge Delay",
      "Ticks between the swap and the charge", 2.0, 0.0, 20.0, 1.0));

   public final SliderSetting blowDelay = this.addSetting(new SliderSetting("Detonate Delay",
      "Ticks between charging and setting it off", 3.0, 0.0, 20.0, 1.0));

   public final SliderSetting weaponSlot = this.addSetting(new SliderSetting("Detonate Slot",
      "Hotbar slot to hold when setting it off -- anything but glowstone", 1.0, 1.0, 9.0, 1.0));

   public final BooleanSetting swapBack = this.addSetting(new BooleanSetting("Restore Slot",
      "Go back to the slot you were holding when the sequence ends", true));


   /** 0 idle, 1 swap to glowstone, 2 charge, 3 swap off, 4 detonate. */
   private int step;
   private int ticks;
   private BlockPos target;
   private int entrySlot = -1;

   public AutoAnchorModule() {
      super("Auto Anchor", "Charges and sets off the anchor under your crosshair", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.reset();
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
      if (player == null || client.interactionManager == null || client.world == null) {
         return;
      }

      if (this.step == 0) {
         this.maybeStart(client, player);
         return;
      }

      this.ticks++;
      BlockHitResult hit = AnchorHelper.anchorUnderCrosshair();
      if (hit == null || this.target == null || !this.target.equals(hit.getBlockPos())) {
         // looked away mid-sequence; abandon it rather than act on a new block
         this.restore();
         this.reset();
         return;
      }

      switch (this.step) {
         case 1 -> {
            if (this.ticks >= this.chargeDelay.getInt()) {
               this.advance(2);
            }
         }
         case 2 -> {
            if (AnchorHelper.charge(hit)) {
               this.advance(3);
            } else {
               this.restore();
               this.reset();                    // no glowstone, or crouched
            }
         }
         case 3 -> {
            if (this.ticks >= this.blowDelay.getInt()) {
               InventoryHelper.selectHotbarSlot(this.weaponSlot.getInt() - 1);
               this.advance(4);
            }
         }
         case 4 -> {
            // Safe Anchor is a veto, not a step: it never fires anything, it
            // only refuses. Asking it unconditionally is fine -- it answers
            // yes whenever it is switched off.
            if (safeToBlow()) {
               AnchorHelper.detonate(hit);
            }

            this.restore();
            this.reset();
         }
         default -> this.reset();
      }
   }

   private void maybeStart(MinecraftClient client, ClientPlayerEntity player) {
      if (this.requireHold.get()
            && GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) != GLFW.GLFW_PRESS) {
         return;
      }

      BlockHitResult hit = AnchorHelper.anchorUnderCrosshair();
      if (hit == null) {
         return;
      }

      this.target = hit.getBlockPos();
      this.entrySlot = player.getInventory().getSelectedSlot();

      // An anchor that already has charges only needs setting off.
      this.step = AnchorHelper.charges(this.target) > 0 ? 3 : 1;
      this.ticks = 0;
      if (this.step == 1) {
         InventoryHelper.swapToItem(net.minecraft.item.Items.GLOWSTONE);
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
      this.target = null;
      this.entrySlot = -1;
   }
}
