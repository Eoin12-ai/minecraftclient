package dev.kryptic.module.combat;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.BlockHelper;
import dev.kryptic.util.InventoryHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;

/**
 * Places and breaks end crystals where you are already looking.
 *
 * The deliberate limit: it never rotates you and it never picks a target
 * across the map. It works on the block or crystal under your crosshair, which
 * means your aim is still your aim — the part a server can see is unchanged,
 * and what you get is the click speed and the ordering, not a robot.
 *
 * <p>Placing and breaking are one loop on purpose. A crystal you placed and
 * did not break is a crystal your opponent breaks, on their terms.
 */
public class AutoCrystalModule extends Module {

   public final BooleanSetting place = this.addSetting(new BooleanSetting("Place",
      "Put a crystal on obsidian or bedrock under the crosshair", true));

   public final BooleanSetting breakThem = this.addSetting(new BooleanSetting("Break",
      "Hit a crystal under the crosshair", true));

   public final SliderSetting placeDelay = this.addSetting(new SliderSetting("Place Delay",
      "Ticks between placements", 3.0, 0.0, 20.0, 1.0));

   public final SliderSetting breakDelay = this.addSetting(new SliderSetting("Break Delay",
      "Ticks between hits", 1.0, 0.0, 20.0, 1.0));

   public final SliderSetting reach = this.addSetting(new SliderSetting("Max Distance",
      "Ignore anything further than this", 4.5, 1.0, 6.0, 0.1, "m"));

   public final BooleanSetting respectOptimiser = this.addSetting(new BooleanSetting("Ask The Optimiser",
      "Do not place when Crystal Optimiser calls it a bad trade", true));

   public final BooleanSetting swapBack = this.addSetting(new BooleanSetting("Restore Slot",
      "Return to the slot you were holding after placing", true));

   private int placeWait;
   private int breakWait;
   private int entrySlot = -1;

   public AutoCrystalModule() {
      super("Auto Crystal", "Places and breaks crystals under your crosshair", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.placeWait = 0;
      this.breakWait = 0;
      this.entrySlot = -1;
   }

   @Override
   protected void onDisable() {
      this.restore();
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (player == null || client.interactionManager == null || client.world == null
            || client.currentScreen != null) {
         return;
      }

      if (this.placeWait > 0) {
         this.placeWait--;
      }

      if (this.breakWait > 0) {
         this.breakWait--;
      }

      HitResult target = client.crosshairTarget;
      if (target == null) {
         return;
      }

      // Breaking first: a crystal already sitting there is worth more than a
      // new one, and placing into it would fail anyway.
      if (this.breakThem.get() && this.breakWait == 0
            && target instanceof EntityHitResult entityHit
            && entityHit.getEntity() instanceof EndCrystalEntity crystal
            && player.distanceTo(crystal) <= this.reach.getFloat()) {
         client.interactionManager.attackEntity(player, crystal);
         player.swingHand(Hand.MAIN_HAND);
         this.breakWait = this.breakDelay.getInt();
         return;
      }

      if (!this.place.get() || this.placeWait > 0
            || !(target instanceof BlockHitResult blockHit)
            || target.getType() != HitResult.Type.BLOCK) {
         return;
      }

      BlockPos pos = blockHit.getBlockPos();
      if (!BlockHelper.isBlockAt(pos, Blocks.OBSIDIAN) && !BlockHelper.isBlockAt(pos, Blocks.BEDROCK)) {
         return;
      }

      double centreDistance = player.getPos().distanceTo(
            new net.minecraft.util.math.Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
      if (centreDistance > this.reach.get() + 1.0) {
         return;
      }

      // Sneaking would place the held item as a block rather than use the
      // support block, which is the same trap the anchor modules hit.
      if (player.isSneaking()) {
         return;
      }

      if (this.occupied(client, pos)) {
         return;
      }

      if (this.respectOptimiser.get() && !this.optimiserApproves()) {
         return;
      }

      if (this.entrySlot < 0) {
         this.entrySlot = player.getInventory().getSelectedSlot();
      }

      if (!InventoryHelper.swapToItem(Items.END_CRYSTAL)) {
         this.restore();
         return;
      }

      BlockHelper.interactBlock(blockHit, true);
      this.placeWait = this.placeDelay.getInt();
      this.restore();
   }

   /** Whether a crystal is already standing on this block. */
   private boolean occupied(MinecraftClient client, BlockPos pos) {
      Box above = new Box(pos.getX(), pos.getY() + 1.0, pos.getZ(),
            pos.getX() + 1.0, pos.getY() + 3.0, pos.getZ() + 1.0);
      return !client.world.getEntitiesByClass(EndCrystalEntity.class, above, c -> true).isEmpty();
   }

   private boolean optimiserApproves() {
      ModuleManager modules = KrypticClient.modules();
      if (modules == null || modules.crystalOptimiser == null || !modules.crystalOptimiser.isEnabled()) {
         return true;
      }

      CrystalOptimiserModule.Verdict verdict = modules.crystalOptimiser.verdict();
      return verdict == null || verdict.worthIt();
   }

   private void restore() {
      if (this.swapBack.get() && this.entrySlot >= 0) {
         InventoryHelper.selectHotbarSlot(this.entrySlot);
      }

      this.entrySlot = -1;
   }
}
