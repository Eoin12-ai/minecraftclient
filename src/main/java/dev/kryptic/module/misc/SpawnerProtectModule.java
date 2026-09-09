package dev.kryptic.module.misc;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.settings.StringSetting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SpawnerProtectModule extends Module {
   public final SliderSetting targetStackCount = this.addSetting(
      new SliderSetting("Stacks To Deposit", "How many full stacks of spawners to mine before stashing.", 3.0, 1.0, 30.0, 1.0)
   );
   public final SliderSetting scanRange = this.addSetting(
      new SliderSetting("Trigger Range", "Horizontal distance a stranger triggers the routine.", 64.0, 16.0, 128.0, 1.0, "m")
   );
   public final SliderSetting rotationSpeed = this.addSetting(
      new SliderSetting("Rotation Speed", "Degrees per tick the look direction is nudged toward its target.", 15.0, 1.0, 30.0, 1.0)
   );
   public final BooleanSetting detectBlockUpdates = this.addSetting(
      new BooleanSetting("Detect Block Updates", "Detects distant players by their out-of-render block break/place packets.", true)
   );
   public final StringSetting whitelist = this.addSetting(new StringSetting("Whitelist", "Comma-separated names to ignore.", "", 256, "Steve, Alex"));
   public final SliderSetting breakRange = this.addSetting(
      new SliderSetting("Break Range", "How close a spawner must be before it is broken.", 5.5, 1.0, 8.0, 0.5, "m")
   );
   public final BooleanSetting doubleCheck = this.addSetting(
      new BooleanSetting("Double Check", "Requires two distant breaks within the window before triggering.", true)
   );
   public final SliderSetting doubleCheckWindow = this.addSetting(
      new SliderSetting("Double Check Window", "Time window for the double check.", 60.0, 1.0, 600.0, 1.0, "s")
   );
   public final StringSetting webhookUrl = this.addSetting(
      new StringSetting("Webhook URL", "Optional Discord webhook for alerts.", "", 256, "https://discord.com/api/webhooks/...")
   );
   private final MinecraftClient mc = MinecraftClient.getInstance();
   private SpawnerProtectModule.State currentState = SpawnerProtectModule.State.WAITING_FOR_STRANGER;
   private BlockPos targetBlock = null;
   private BlockPos targetChest = null;
   private int chestTick = 0;
   private int breakCooldown = 0;
   private int lagWaitTicks = 0;
   private boolean strangerDetected = false;
   private final Set<Integer> verifiedPlayers = new HashSet<>();
   private int blockBreakCounter = 0;
   private long lastBreakTimestamp = 0L;
   private int shopSequence = 0;
   private int shopTick = 0;
   private int miningTicks = 0;

   public SpawnerProtectModule() {
      super("SpawnerProtect", "Auto-salvages your spawners when a stranger approaches, then logs out.", Category.MISC);
      SliderSetting sliderSetting = this.doubleCheckWindow;
      BooleanSetting booleanSetting = this.doubleCheck;
      sliderSetting.visibleWhen(booleanSetting::get);
   }

   @Override
   protected void onEnable() {
      this.resetModule();
   }

   @Override
   protected void onDisable() {
      this.stopMovement();
      this.updateSneak(false);
   }

   private void resetModule() {
      this.currentState = SpawnerProtectModule.State.WAITING_FOR_STRANGER;
      this.strangerDetected = false;
      this.lagWaitTicks = 0;
      this.verifiedPlayers.clear();
      this.blockBreakCounter = 0;
      this.lastBreakTimestamp = 0L;
      this.resetState();
   }

   private void resetState() {
      this.targetChest = null;
      this.targetBlock = null;
      this.chestTick = 0;
      this.breakCooldown = 0;
      this.miningTicks = 0;
   }

   public boolean isTriggered() {
      return this.strangerDetected;
   }

   public String phase() {
      return this.currentState.name();
   }

   public boolean detectBlockUpdatesEnabled() {
      return this.detectBlockUpdates.get();
   }

   public void onBlockDestructionPacket(int n, BlockPos pos) {
      ClientPlayerEntity player = this.mc.player;
      ClientWorld world = this.mc.world;
      if (!this.strangerDetected && player != null && world != null && !this.isNearSpawn() && n != player.getId()) {
         Entity entity = world.getEntityById(n);
         if (entity instanceof PlayerEntity player3) {
            if (this.isWhitelisted(player3.getName().getString())) {
               return;
            }
         } else if (entity == null) {
            for (PlayerEntity player2 : world.getPlayers()) {
               if (player2.getEyePos().distanceTo(Vec3d.ofCenter(pos)) < 8.0 && this.isWhitelisted(player2.getName().getString())) {
                  return;
               }
            }
         }

         double d = player.getX() - (double)pos.getX();
         double coord = player.getZ() - (double)pos.getZ();
         double currentScore = Math.sqrt(d * d + coord * coord);
         if (currentScore <= this.scanRange.get()) {
            this.strangerDetected = true;
            this.currentState = SpawnerProtectModule.State.WORKING;
            String name2 = entity != null ? entity.getName().getString() : "Invisible/Anti-ESP";
            this.warn("\ud83d\udea8 PACKET DETECT: " + name2 + " started breaking! (Horizontal: " + (int)currentScore + "m)");
            this.sendWebhook(
               "\ud83d\udea8 **PACKET DETECT:** `" + name2 + "` started breaking! (Horizontal: " + (int)currentScore + "m, Y: " + pos.getY() + ")."
            );
         }
      }
   }

   public void onServerBlockUpdate(BlockPos pos, BlockState state, boolean value) {
      ClientPlayerEntity player = this.mc.player;
      ClientWorld world = this.mc.world;
      if (!this.strangerDetected && player != null && world != null && !this.isNearSpawn()) {
         for (PlayerEntity player2 : world.getPlayers()) {
            if (player2.getEyePos().distanceTo(Vec3d.ofCenter(pos)) < 8.0 && this.isWhitelisted(player2.getName().getString())) {
               return;
            }
         }

         if (pos.getY() > -64 && state.isAir()) {
            BlockState state2 = world.getBlockState(pos);
            if (!state2.isAir() && !(state2.getBlock() instanceof ShulkerBoxBlock)) {
               double d = player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
               if (!(d < 6.0)) {
                  double coord = player.getX() - (double)pos.getX();
                  double currentScore = player.getZ() - (double)pos.getZ();
                  double coord3 = Math.sqrt(coord * coord + currentScore * currentScore);
                  if (!(coord3 > this.scanRange.get())) {
                     if (this.doubleCheck.get()) {
                        long l = System.currentTimeMillis();
                        if (l - this.lastBreakTimestamp > (long)this.doubleCheckWindow.getInt() * 1000L) {
                           this.blockBreakCounter = 0;
                        }

                        this.blockBreakCounter++;
                        this.lastBreakTimestamp = l;
                        if (this.blockBreakCounter < 2) {
                           return;
                        }
                     }

                     this.strangerDetected = true;
                     this.currentState = SpawnerProtectModule.State.WORKING;
                     String name2 = value ? "multi-block break" : "block break";
                     this.warn("\ud83d\udea8 REMOTE DETECT: a block broke nearby! (Horizontal: " + (int)coord3 + "m, Y: " + pos.getY() + ")");
                     this.sendWebhook(
                        "\ud83d\udea8 **REMOTE DETECT ("
                           + name2
                           + "):** an invisible or far-away player broke a block! (Horizontal: "
                           + (int)coord3
                           + "m, Y: "
                           + pos.getY()
                           + ")"
                     );
                  }
               }
            }
         }
      }
   }

   @Override
   public void onTick() {
      ClientPlayerEntity player = this.mc.player;
      ClientWorld world = this.mc.world;
      if (player != null && world != null) {
         if (this.isNearSpawn()) {
            if (this.strangerDetected) {
               this.resetModule();
            }
         } else {
            if (!this.strangerDetected) {
               for (PlayerEntity player3 : world.getPlayers()) {
                  if (player3 != player && !this.isWhitelisted(player3.getName().getString())) {
                     double d = player.getX() - player3.getX();
                     double coord = player.getZ() - player3.getZ();
                     double currentScore = Math.sqrt(d * d + coord * coord);
                     if (!(currentScore > this.scanRange.get())) {
                        boolean id = this.verifiedPlayers.contains(player3.getId());
                        if (!id && player3.isUsingItem()) {
                           id = true;
                           this.verifiedPlayers.add(player3.getId());
                        }

                        if (id) {
                           this.strangerDetected = true;
                           this.currentState = SpawnerProtectModule.State.WORKING;
                           String name2 = player3.getName().getString();
                           this.warn("⚠ PLAYER SPOTTED (verified): " + name2 + " (Horizontal: " + (int)currentScore + "m)");
                           name2 = player3.getName().getString();
                           this.sendWebhook(
                              "⚠ **PLAYER SPOTTED (verified):** `" + name2 + "` (Horizontal: " + (int)currentScore + "m, Y: " + player3.getBlockY() + ")!"
                           );
                           break;
                        }
                     }
                  }
               }
            }

            if (!this.strangerDetected) {
               for (Entity entity2 : world.getEntities()) {
                  if (entity2.getType() == EntityType.ENDER_PEARL) {
                     if (entity2 instanceof ProjectileEntity) {
                        ProjectileEntity projectileEntity = (ProjectileEntity)entity2;
                        Entity entity = projectileEntity.getOwner();
                        if (entity instanceof PlayerEntity) {
                           PlayerEntity player4 = (PlayerEntity)entity;
                           if (this.isWhitelisted(player4.getName().getString())) {
                              continue;
                           }
                        }
                     }

                     double coord3 = player.getX() - entity2.getX();
                     double coord4 = player.getZ() - entity2.getZ();
                     double coord5 = Math.sqrt(coord3 * coord3 + coord4 * coord4);
                     if (coord5 <= this.scanRange.get()) {
                        this.strangerDetected = true;
                        this.currentState = SpawnerProtectModule.State.WORKING;
                        this.warn("⚠ ENDER PEARL SPOTTED! (Horizontal: " + (int)coord5 + "m)");
                        this.sendWebhook("⚠ **ENDER PEARL SPOTTED!** Someone threw a pearl. (Horizontal: " + (int)coord5 + "m)!");
                        break;
                     }
                  }
               }
            }

            if (this.currentState != SpawnerProtectModule.State.WAITING_FOR_STRANGER) {
               if (this.currentState != SpawnerProtectModule.State.OPENING_CHEST
                  && this.currentState != SpawnerProtectModule.State.DEPOSITING_ITEMS
                  && this.currentState != SpawnerProtectModule.State.BUYING_ECHEST) {
                  this.updateSneak(true);
               }

               this.handleRotation();
               switch (this.currentState) {
                  case WORKING:
                     this.handleWorking();
                     break;
                  case GOING_TO_CHEST:
                     this.handleGoingToChest();
                     break;
                  case OPENING_CHEST:
                     this.handleOpeningChest();
                     break;
                  case DEPOSITING_ITEMS:
                     this.handleDepositing();
                     break;
                  case FINAL_EXIT:
                     this.handleFinalExit();
                     break;
                  case BUYING_ECHEST:
                     this.handleBuyingEChest();
                     break;
                  case PLACING_ECHEST:
                     this.handlePlacingEChest();
               }
            }
         }
      }
   }

   private void handleRotation() {
      ClientPlayerEntity player = this.mc.player;
      ClientWorld world = this.mc.world;
      if (player != null && world != null) {
         Vec3d vec = null;
         if (this.currentState == SpawnerProtectModule.State.WORKING) {
            ItemEntity itemEntity = this.findDroppedSpawner();
            if (itemEntity != null) {
               vec = new Vec3d(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ());
            } else {
               if (this.targetBlock == null
                  || world.getBlockState(this.targetBlock).getBlock() != Blocks.SPAWNER
                  || player.getBlockPos().getSquaredDistance(this.targetBlock) > 256.0) {
                  this.targetBlock = this.findRandomBlock(Blocks.SPAWNER, 16);
                  this.miningTicks = 0;
               }

               if (this.targetBlock != null) {
                  vec = Vec3d.ofCenter(this.targetBlock);
               }
            }
         } else if ((this.currentState == SpawnerProtectModule.State.GOING_TO_CHEST || this.currentState == SpawnerProtectModule.State.OPENING_CHEST)
            && this.targetChest != null) {
            vec = Vec3d.ofCenter(this.targetChest);
         }

         if (vec != null) {
            this.smoothLook(vec);
         }
      }
   }

   private void smoothLook(Vec3d vec) {
      ClientPlayerEntity player = this.mc.player;
      if (player != null) {
         Vec3d vec2 = player.getEyePos();
         double d = vec.x - vec2.x;
         double coord = vec.y - vec2.y;
         double currentScore = vec.z - vec2.z;
         double coord3 = Math.sqrt(d * d + currentScore * currentScore);
         float f = (float)Math.toDegrees(Math.atan2(-d, currentScore));
         float f4 = (float)(-Math.toDegrees(Math.atan2(coord, coord3)));
         float f5 = this.rotationSpeed.getFloat();
         player.setYaw(player.getYaw() + MathHelper.clamp(MathHelper.wrapDegrees(f - player.getYaw()), -f5, f5));
         player.setPitch(player.getPitch() + MathHelper.clamp(MathHelper.wrapDegrees(f4 - player.getPitch()), -f5, f5));
      }
   }

   private void handleWorking() {
      ClientPlayerEntity player = this.mc.player;
      ClientWorld world = this.mc.world;
      if (player != null && world != null) {
         if (player.currentScreenHandler != player.playerScreenHandler) {
            player.closeHandledScreen();
         }

         ItemEntity itemEntity = this.findDroppedSpawner();
         if (itemEntity != null) {
            this.lagWaitTicks = 0;
            this.stopBreaking();
            this.mc.options.forwardKey.setPressed(true);
         } else if (this.getSpawnerCount() >= this.targetStackCount.getInt() * 64) {
            this.goToChest();
         } else {
            if (this.targetBlock == null
               || world.getBlockState(this.targetBlock).getBlock() != Blocks.SPAWNER
               || player.getBlockPos().getSquaredDistance(this.targetBlock) > 256.0) {
               this.targetBlock = this.findRandomBlock(Blocks.SPAWNER, 16);
               this.miningTicks = 0;
            }

            if (this.targetBlock == null) {
               this.stopMovement();
               if (this.lagWaitTicks < 40) {
                  this.lagWaitTicks++;
               } else if (this.getSpawnerCount() > 0) {
                  this.goToChest();
               } else {
                  this.currentState = SpawnerProtectModule.State.FINAL_EXIT;
                  this.lagWaitTicks = 0;
               }
            } else {
               this.lagWaitTicks = 0;
               double d = player.getEyePos().distanceTo(Vec3d.ofCenter(this.targetBlock));
               if (d <= this.breakRange.get()) {
                  this.mc.options.forwardKey.setPressed(false);
                  this.miningTicks++;
                  if (this.miningTicks > 60) {
                     this.mc
                        .interactionManager
                        .interactBlock(
                           player, Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(this.targetBlock), Direction.UP, this.targetBlock, false)
                        );
                     player.swingHand(Hand.MAIN_HAND);
                     this.miningTicks = 0;
                  }

                  if (this.breakCooldown <= 0) {
                     this.mc.interactionManager.updateBlockBreakingProgress(this.targetBlock, Direction.UP);
                     player.swingHand(Hand.MAIN_HAND);
                     this.mc.options.attackKey.setPressed(true);
                     this.breakCooldown = 6;
                  } else {
                     this.breakCooldown--;
                  }
               } else {
                  this.stopBreaking();
                  this.mc.options.forwardKey.setPressed(true);
               }
            }
         }
      }
   }

   private void goToChest() {
      this.stopMovement();
      this.targetChest = this.findNearestBlock(Blocks.ENDER_CHEST, 4);
      if (this.targetChest != null) {
         this.currentState = SpawnerProtectModule.State.GOING_TO_CHEST;
      } else if (this.getEnderChestCount() > 0) {
         this.currentState = SpawnerProtectModule.State.PLACING_ECHEST;
         this.shopTick = 0;
      } else {
         this.currentState = SpawnerProtectModule.State.BUYING_ECHEST;
         this.shopSequence = 0;
         this.shopTick = 0;
      }
   }

   private void handleGoingToChest() {
      ClientPlayerEntity player = this.mc.player;
      if (player != null) {
         if (this.targetChest == null) {
            this.targetChest = this.findNearestBlock(Blocks.ENDER_CHEST, 4);
         }

         if (this.targetChest == null) {
            this.currentState = SpawnerProtectModule.State.WORKING;
         } else {
            this.mc.options.forwardKey.setPressed(true);
            if (player.getBlockPos().isWithinDistance(this.targetChest, 4.0)) {
               this.stopMovement();
               this.currentState = SpawnerProtectModule.State.OPENING_CHEST;
            }
         }
      }
   }

   private void handleOpeningChest() {
      ClientPlayerEntity player = this.mc.player;
      if (player != null) {
         this.updateSneak(false);
         if (this.chestTick % 12 == 0 && this.targetChest != null) {
            this.mc
               .interactionManager
               .interactBlock(player, Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(this.targetChest), Direction.UP, this.targetChest, false));
         }

         this.chestTick++;
         if (player.currentScreenHandler instanceof GenericContainerScreenHandler) {
            this.chestTick = 0;
            this.currentState = SpawnerProtectModule.State.DEPOSITING_ITEMS;
            this.lagWaitTicks = 0;
         }
      }
   }

   private void handleBuyingEChest() {
      ClientPlayerEntity player = this.mc.player;
      if (player != null) {
         this.shopTick++;
         if (this.shopSequence > 0 && this.mc.currentScreen == null && this.shopTick > 60) {
            this.say("Shop screen closed, retrying...");
            this.shopSequence = 0;
            this.shopTick = 0;
         } else if (this.shopTick >= 30) {
            switch (this.shopSequence) {
               case 0:
                  this.say("Opening shop: /shop");
                  if (this.mc.getNetworkHandler() != null) {
                     this.mc.getNetworkHandler().sendChatCommand("shop");
                  }

                  this.shopSequence = 1;
                  this.shopTick = 0;
                  break;
               case 1:
                  if (this.mc.currentScreen != null && this.screenTitle().contains("SHOP")) {
                     this.say("Shop: selecting the End category...");
                     this.clickSlot(11, SlotActionType.PICKUP);
                     this.shopSequence = 2;
                     this.shopTick = 0;
                  }
                  break;
               case 2:
                  if (this.mc.currentScreen != null && this.screenTitle().contains("END")) {
                     this.say("Shop: selecting Ender Chest...");
                     this.clickSlot(9, SlotActionType.PICKUP);
                     this.shopSequence = 3;
                     this.shopTick = 0;
                  }
                  break;
               case 3:
                  if (this.mc.currentScreen != null && this.screenTitle().contains("ENDER CHEST")) {
                     this.say("Shop: confirming purchase...");
                     this.clickSlot(25, SlotActionType.PICKUP);
                     this.shopSequence = 4;
                     this.shopTick = 0;
                  }
                  break;
               case 4:
                  if (this.getEnderChestCount() > 0) {
                     this.say("Ender Chest purchased.");
                     player.closeHandledScreen();
                     this.currentState = SpawnerProtectModule.State.PLACING_ECHEST;
                     this.shopTick = 0;
                  } else if (this.shopTick > 100) {
                     this.say("Purchase failed (timeout).");
                     player.closeHandledScreen();
                     this.shopSequence = 0;
                     this.shopTick = 0;
                  }
            }
         }
      }
   }

   private void handlePlacingEChest() {
      ClientPlayerEntity player = this.mc.player;
      ClientWorld world = this.mc.world;
      if (player != null && world != null) {
         this.shopTick++;
         if (this.shopTick >= 10) {
            int bestSlot = -1;

            for (int slot = 0; slot < 9; slot++) {
               if (player.getInventory().getStack(slot).getItem() == Blocks.ENDER_CHEST.asItem()) {
                  bestSlot = slot;
                  break;
               }
            }

            if (bestSlot == -1) {
               for (int n = 9; n < 36; n++) {
                  if (player.getInventory().getStack(n).getItem() == Blocks.ENDER_CHEST.asItem()) {
                     this.mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, n, 0, SlotActionType.QUICK_MOVE, player);
                     this.shopTick = 0;
                     return;
                  }
               }

               this.currentState = SpawnerProtectModule.State.BUYING_ECHEST;
               this.shopSequence = 0;
            } else {
               player.getInventory().setSelectedSlot(bestSlot);
               BlockPos pos = player.getBlockPos();
               BlockPos pos4 = null;

               for (Direction direction : Direction.values()) {
                  if (direction != Direction.UP && direction != Direction.DOWN) {
                     BlockPos pos5 = pos.offset(direction);
                     if (world.getBlockState(pos5).isReplaceable()) {
                        pos4 = pos5;
                        break;
                     }
                  }
               }

               if (pos4 != null) {
                  this.mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter(pos4), Direction.UP, pos4, false));
                  player.swingHand(Hand.MAIN_HAND);
                  this.targetChest = pos4;
                  this.currentState = SpawnerProtectModule.State.GOING_TO_CHEST;
               } else {
                  this.warn("No spot to place the Ender Chest!");
                  this.currentState = SpawnerProtectModule.State.FINAL_EXIT;
                  this.lagWaitTicks = 0;
               }
            }
         }
      }
   }

   private void handleDepositing() {
      ClientPlayerEntity player = this.mc.player;
      if (player != null) {
         if (this.lagWaitTicks < 15) {
            this.lagWaitTicks++;
         } else if (player.currentScreenHandler instanceof GenericContainerScreenHandler genericContainerScreenHandler) {
            int slot = genericContainerScreenHandler.slots.size() - 36;
            boolean visible = false;

            for (int n = 0; n < slot; n++) {
               ItemStack hotbarStack = genericContainerScreenHandler.getSlot(n).getStack();
               if (hotbarStack.isEmpty() || hotbarStack.getItem() == Blocks.SPAWNER.asItem() && hotbarStack.getCount() < hotbarStack.getMaxCount()) {
                  visible = true;
                  break;
               }
            }

            int slot2 = slot;

            for (int offset = 0; offset < 36; offset++) {
               int slot3 = slot2 + offset;
               if (genericContainerScreenHandler.getSlot(slot3).getStack().getItem() == Blocks.SPAWNER.asItem()) {
                  if (!visible) {
                     this.currentState = SpawnerProtectModule.State.FINAL_EXIT;
                     this.lagWaitTicks = 0;
                     return;
                  }

                  this.mc.interactionManager.clickSlot(genericContainerScreenHandler.syncId, slot3, 0, SlotActionType.QUICK_MOVE, player);
                  return;
               }
            }

            player.closeHandledScreen();
            if (this.findNearestBlock(Blocks.SPAWNER, 16) != null) {
               this.currentState = SpawnerProtectModule.State.WORKING;
            } else {
               this.currentState = SpawnerProtectModule.State.FINAL_EXIT;
               this.lagWaitTicks = 0;
            }
         }
      }
   }

   private void handleFinalExit() {
      if (this.lagWaitTicks == 0) {
         this.sendWebhook("\ud83c\udfc1 **DONE.** Waiting for items to save...");
         this.say("Task done — waiting 2s for the server to save, then disconnecting...");
      }

      this.lagWaitTicks++;
      if (this.lagWaitTicks > 40) {
         ClientPlayNetworkHandler clientPlayNetworkHandler = this.mc.getNetworkHandler();
         if (clientPlayNetworkHandler != null) {
            clientPlayNetworkHandler.getConnection().disconnect(Text.literal("SpawnerProtect: disconnected \u2014 someone approached your spawner"));
         }

         this.resetModule();
      }
   }

   private void updateSneak(boolean value) {
      ClientPlayerEntity player = this.mc.player;
      if (player != null) {
         player.setSneaking(value);
      }

      this.mc.options.sneakKey.setPressed(value);
   }

   private boolean isNearSpawn() {
      ClientPlayerEntity player = this.mc.player;
      return player != null && Math.abs(player.getX()) < 100.0 && Math.abs(player.getZ()) < 100.0;
   }

   private boolean isWhitelisted(String name2) {
      if (name2 != null && !name2.isEmpty()) {
         for (String item : this.whitelist.get().split("_")) {
            if (item.trim().equalsIgnoreCase(name2)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private String screenTitle() {
      return this.mc.currentScreen == null ? "" : this.mc.currentScreen.getTitle().getString().toUpperCase(Locale.ROOT);
   }

   private void clickSlot(int slot, SlotActionType slotActionType) {
      ClientPlayerEntity player = this.mc.player;
      if (player != null) {
         this.mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, slot, 0, slotActionType, player);
      }
   }

   private BlockPos findNearestBlock(Block block, int n) {
      ClientPlayerEntity player = this.mc.player;
      ClientWorld world = this.mc.world;
      if (player != null && world != null) {
         BlockPos pos = player.getBlockPos();
         BlockPos pos4 = null;
         double d = Double.MAX_VALUE;

         for (int localZ = -n; localZ <= n; localZ++) {
            for (int localY = -n; localY <= n; localY++) {
               for (int step = -n; step <= n; step++) {
                  BlockPos pos5 = pos.add(localZ, localY, step);
                  if (world.getBlockState(pos5).getBlock() == block) {
                     double coord = pos.getSquaredDistance(pos5);
                     if (coord < d) {
                        d = coord;
                        pos4 = pos5;
                     }
                  }
               }
            }
         }

         return pos4;
      } else {
         return null;
      }
   }

   private BlockPos findRandomBlock(Block block, int n) {
      ClientPlayerEntity player = this.mc.player;
      ClientWorld world = this.mc.world;
      if (player != null && world != null) {
         BlockPos pos = player.getBlockPos();
         ArrayList list = new ArrayList();
         double d = (double)n * (double)n;

         for (int localZ = -n; localZ <= n; localZ++) {
            for (int localY = -n; localY <= n; localY++) {
               for (int step = -n; step <= n; step++) {
                  BlockPos pos3 = pos.add(localZ, localY, step);
                  if (pos.getSquaredDistance(pos3) <= d && world.getBlockState(pos3).getBlock() == block) {
                     list.add(pos3);
                  }
               }
            }
         }

         return list.isEmpty() ? null : (BlockPos)list.get(new Random().nextInt(list.size()));
      } else {
         return null;
      }
   }

   private int getSpawnerCount() {
      return this.countItem(Blocks.SPAWNER.asItem(), 36);
   }

   private int getEnderChestCount() {
      return this.countItem(Blocks.ENDER_CHEST.asItem(), 45);
   }

   private int countItem(Item item, int n) {
      ClientPlayerEntity player = this.mc.player;
      if (player == null) {
         return 0;
      } else {
         int localX = 0;

         for (int localZ = 0; localZ < n; localZ++) {
            ItemStack hotbarStack = player.getInventory().getStack(localZ);
            if (hotbarStack.getItem() == item) {
               localX += hotbarStack.getCount();
            }
         }

         return localX;
      }
   }

   private ItemEntity findDroppedSpawner() {
      ClientPlayerEntity player = this.mc.player;
      ClientWorld world = this.mc.world;
      if (player != null && world != null) {
         ItemEntity itemEntity = null;
         double d = Double.MAX_VALUE;

         for (Entity entity : world.getEntities()) {
            if (entity instanceof ItemEntity) {
               ItemEntity itemEntity2 = (ItemEntity)entity;
               if (itemEntity2.getStack().getItem() == Blocks.SPAWNER.asItem()) {
                  double coord = (double)player.distanceTo(itemEntity2);
                  if (coord < 16.0 && coord < d) {
                     d = coord;
                     itemEntity = itemEntity2;
                  }
               }
            }
         }

         return itemEntity;
      } else {
         return null;
      }
   }

   private void stopBreaking() {
      this.mc.options.attackKey.setPressed(false);
      this.breakCooldown = 0;
   }

   private void stopMovement() {
      this.stopBreaking();
      this.mc.options.forwardKey.setPressed(false);
   }

   private void warn(String name2) {
      ClientPlayerEntity player = this.mc.player;
      if (player != null) {
         player.sendMessage(Text.literal("§c[SpawnerProtect] §f" + name2), false);
      }

      try {
         KrypticClient.notifications().pushInfo("SpawnerProtect · " + name2.replaceAll("\u00A7[0-9A-FK-ORa-fk-or]", ""));
      } catch (Exception ex) {
      }
   }

   private void say(String name2) {
      ClientPlayerEntity player = this.mc.player;
      if (player != null) {
         player.sendMessage(Text.literal("§7[SpawnerProtect] " + name2), false);
      }
   }

   private void sendWebhook(String name2) {
   }

   private static enum State {
      WAITING_FOR_STRANGER,
      WORKING,
      GOING_TO_CHEST,
      OPENING_CHEST,
      DEPOSITING_ITEMS,
      FINAL_EXIT,
      BUYING_ECHEST,
      PLACING_ECHEST;

      private static SpawnerProtectModule.State[] $values() {
         return new SpawnerProtectModule.State[]{
            WAITING_FOR_STRANGER, WORKING, GOING_TO_CHEST, OPENING_CHEST, DEPOSITING_ITEMS, FINAL_EXIT, BUYING_ECHEST, PLACING_ECHEST
         };
      }
   }
}
