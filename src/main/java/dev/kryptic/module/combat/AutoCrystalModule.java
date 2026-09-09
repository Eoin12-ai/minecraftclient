package dev.kryptic.module.combat;

import net.minecraft.util.math.Vec3d;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.KeybindSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.BlockHelper;
import dev.kryptic.util.InventoryHelper;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class AutoCrystalModule extends Module {
   public final KeybindSetting activateKey = this.addSetting(new KeybindSetting("Activate Key", "Hold to place + break (default RMB)", 1));
   public final SliderSetting breakDelay = this.addSetting(new SliderSetting("Break Delay", "Ticks between crystal breaks", 1.0, 0.0, 20.0, 1.0, "t"));
   public final SliderSetting placeDelay = this.addSetting(new SliderSetting("Place Delay", "Ticks between crystal places", 1.0, 0.0, 20.0, 1.0, "t"));
   public final BooleanSetting autoObsidian = this.addSetting(new BooleanSetting("Auto Obsidian", "Lay an obsidian base when you're not aiming at one", true));
   public final SliderSetting obsidianDelay = this.addSetting(
      new SliderSetting("Obsidian Delay", "Ticks between obsidian placements", 2.0, 0.0, 20.0, 1.0, "t")
   );
   public final SliderSetting range = this.addSetting(
      new SliderSetting("Break Range", "Max distance to a crystal you'll break (vanilla reach ~3)", 3.0, 1.0, 6.0, 0.5, "m")
   );
   public final BooleanSetting switchBack = this.addSetting(new BooleanSetting("Switch Back", "Return to your original hotbar slot when released", true));
   private int breakCooldown;
   private int placeCooldown;
   private int obsidianCooldown;
   private int savedSlot = -1;
   private int lastBrokenId = -1;
   private boolean wasActive;

   public AutoCrystalModule() {
      super("Auto Crystal", "Hold RMB to auto place + break crystals, laying an obsidian base when needed", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.resetAll();
   }

   @Override
   protected void onDisable() {
      this.restoreSlot();
      this.resetAll();
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.interactionManager != null && client.world != null && client.currentScreen == null && this.isTriggerHeld(client)) {
         this.wasActive = true;
         this.tickCooldowns();
         if (this.activateKey.get() == 1) {
            client.options.useKey.setPressed(false);
         }

         HitResult hit = client.crosshairTarget;
         if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof EndCrystalEntity crystal) {
            if (this.tryBreak(client, crystal)) {
               this.lastBrokenId = crystal.getId();
            }

            return;
         }

         if (hit instanceof BlockHitResult blockHit && blockHit.getType() == Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            if (isCrystalBase(client.world, pos)) {
               this.serviceBase(client, blockHit, pos);
            } else if (this.autoObsidian.get()) {
               this.layObsidian(client, blockHit, pos);
            }
         }
      } else {
         this.endActiveHold();
      }
   }

   private void serviceBase(MinecraftClient client, BlockHitResult blockHit, BlockPos pos) {
      EndCrystalEntity crystal = crystalOn(client.world, pos);
      if (crystal != null) {
         if (this.tryBreak(client, crystal)) {
            this.lastBrokenId = crystal.getId();
         }
      } else {
         this.lastBrokenId = -1;
         if (this.placeCooldown == 0 && canFitCrystal(client.world, pos)) {
            Hand hand = this.equip(client, Items.END_CRYSTAL);
            if (hand != null) {
               BlockHelper.interactBlock(blockHit, hand, true);
               this.placeCooldown = this.placeDelay.getInt();
            }
         }
      }
   }

   private void layObsidian(MinecraftClient client, BlockHitResult blockHit, BlockPos pos) {
      if (this.obsidianCooldown == 0) {
         BlockPos pos3 = client.world.getBlockState(pos).isReplaceable() ? pos : pos.offset(blockHit.getSide());
         if (client.world.getBlockState(pos3).isReplaceable() && canFitCrystal(client.world, pos3)) {
            Hand hand = this.equip(client, Items.OBSIDIAN);
            if (hand != null) {
               BlockHelper.interactBlock(blockHit, hand, true);
               this.obsidianCooldown = Math.max(1, this.obsidianDelay.getInt());
            }
         }
      }
   }

   private boolean tryBreak(MinecraftClient client, EndCrystalEntity crystal) {
      if (this.breakCooldown != 0) {
         return false;
      } else if (crystal.getId() == this.lastBrokenId) {
         return false;
      } else if (client.player.getEyePos().distanceTo(new Vec3d(crystal.getX(), crystal.getY(), crystal.getZ())) > this.range.get()) {
         return false;
      } else {
         client.interactionManager.attackEntity(client.player, crystal);
         client.player.swingHand(Hand.MAIN_HAND);
         this.breakCooldown = this.breakDelay.getInt();
         return true;
      }
   }

   @Nullable
   private Hand equip(MinecraftClient client, Item item) {
      ClientPlayerEntity player = client.player;
      if (player.getMainHandStack().isOf(item)) {
         return Hand.MAIN_HAND;
      } else if (player.getOffHandStack().isOf(item)) {
         return Hand.OFF_HAND;
      } else {
         int slot = InventoryHelper.getHotbarSlot(item);
         if (slot < 0) {
            return null;
         } else {
            if (this.savedSlot < 0) {
               this.savedSlot = player.getInventory().getSelectedSlot();
            }

            InventoryHelper.selectHotbarSlot(slot);
            return Hand.MAIN_HAND;
         }
      }
   }

   private static boolean isCrystalBase(World world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      return state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.BEDROCK);
   }

   @Nullable
   private static EndCrystalEntity crystalOn(World world, BlockPos pos) {
      BlockPos pos3 = pos.up();
      Box box = new Box(
         (double)pos3.getX() + 0.125,
         (double)pos3.getY() - 0.1,
         (double)pos3.getZ() + 0.125,
         (double)pos3.getX() + 0.875,
         (double)pos3.getY() + 2.5,
         (double)pos3.getZ() + 0.875
      );
      List list = world.getEntitiesByClass(EndCrystalEntity.class, box, arg -> true);
      return list.isEmpty() ? null : (EndCrystalEntity)list.get(0);
   }

   private static boolean canFitCrystal(World world, BlockPos pos) {
      BlockPos pos3 = pos.up();
      if (!world.getBlockState(pos3).isAir()) {
         return false;
      } else {
         Box box = new Box(
            (double)pos3.getX(),
            (double)pos3.getY(),
            (double)pos3.getZ(),
            (double)pos3.getX() + 1.0,
            (double)pos3.getY() + 2.0,
            (double)pos3.getZ() + 1.0
         );
         return world.getNonSpectatingEntities(Entity.class, box).isEmpty();
      }
   }

   private void tickCooldowns() {
      if (this.breakCooldown > 0) {
         this.breakCooldown--;
      }

      if (this.placeCooldown > 0) {
         this.placeCooldown--;
      }

      if (this.obsidianCooldown > 0) {
         this.obsidianCooldown--;
      }
   }

   private boolean isTriggerHeld(MinecraftClient client) {
      int n = this.activateKey.get();
      if (n == -1) {
         return false;
      } else {
         return n <= 7 ? GLFW.glfwGetMouseButton(client.getWindow().getHandle(), n) == 1 : InputUtil.isKeyPressed(client.getWindow(), n);
      }
   }

   private void endActiveHold() {
      if (this.wasActive) {
         this.restoreSlot();
         this.breakCooldown = 0;
         this.placeCooldown = 0;
         this.obsidianCooldown = 0;
         this.lastBrokenId = -1;
         this.wasActive = false;
      }
   }

   private void restoreSlot() {
      if (this.switchBack.get() && this.savedSlot >= 0) {
         InventoryHelper.selectHotbarSlot(this.savedSlot);
      }

      this.savedSlot = -1;
   }

   private void resetAll() {
      this.breakCooldown = 0;
      this.placeCooldown = 0;
      this.obsidianCooldown = 0;
      this.savedSlot = -1;
      this.lastBrokenId = -1;
      this.wasActive = false;
   }
}
