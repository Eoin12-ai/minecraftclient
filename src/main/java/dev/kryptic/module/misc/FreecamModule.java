package dev.kryptic.module.misc;

import dev.kryptic.KrypticClient;
import dev.kryptic.mixin.ClientInputAccessor;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class FreecamModule extends Module {
   private static FreecamModule instance;
   public final SliderSetting speed = this.addSetting(new SliderSetting("Speed", "Camera fly speed", 1.0, 0.1, 50.0, 0.1, "x"));
   public final SliderSetting verticalMultiplier = this.addSetting(new SliderSetting("Vertical speed", "Up/down speed multiplier", 1.0, 0.2, 5.0, 0.05, "x"));
   public final BooleanSetting smoothing = this.addSetting(new BooleanSetting("Smoothing", "Ease camera movement", true));
   public final BooleanSetting showPlayerModel = this.addSetting(new BooleanSetting("Show own body", "Render your body while detached", true));
   public final BooleanSetting showHands = this.addSetting(new BooleanSetting("Show hands", "Keep first-person hands visible", true));
   public final BooleanSetting bodyFollowsKeys = this.addSetting(new BooleanSetting("Body uses movement keys", "Physical WASD also move the body", false));
   public final SliderSetting lookSensitivity = this.addSetting(new SliderSetting("Look Sensitivity", "Camera look sensitivity", 0.5, 0.1, 2.0, 0.05));
   public final SliderSetting freecamChunkDistance = this.addSetting(
      new SliderSetting("Chunk distance", "Chunks to load around the camera", 12.0, 2.0, 32.0, 1.0)
   );
   private double currentX;
   private double currentY;
   private double currentZ;
   private double prevX;
   private double prevY;
   private double prevZ;
   private float currentYaw;
   private float currentPitch;
   private float prevYaw;
   private float prevPitch;
   private double savedX;
   private double savedY;
   private double savedZ;
   private float savedYaw;
   private float savedPitch;
   private boolean savedAbilitiesFlying;
   private boolean savedSmartCull;
   private Perspective perspectiveBeforeFreecam;
   private boolean switchedPerspectiveForBody;
   private ChunkPos lastSyncedCamChunk;
   private int lastSyncedLoadDistance = Integer.MIN_VALUE;
   private boolean activationPending;
   private boolean active = false;
   private boolean latchedForward;
   private boolean latchedBack;
   private boolean latchedLeft;
   private boolean latchedRight;
   private boolean latchedJump;
   private boolean latchedSneak;
   private boolean latchedSprint;
   private int autopilotWarmupTicks;

   public FreecamModule() {
      super("Freecam", "Detached camera (WASD = fly). Body can keep walking; mining uses real aim.", Category.MISC);
      instance = this;
   }

   public static FreecamModule get() {
      return instance;
   }

   @Override
   protected void onEnable() {
      this.activationPending = true;
      this.active = false;
   }

   @Override
   protected void onDisable() {
      MinecraftClient client = MinecraftClient.getInstance();
      this.clearMovementLatches();
      this.activationPending = false;
      this.active = false;
      if (this.switchedPerspectiveForBody) {
         client.options.setPerspective(this.perspectiveBeforeFreecam);
         this.switchedPerspectiveForBody = false;
      }

      this.restoreViewOnlyClientState(client);
      this.restoreVanillaChunkLoading(client);
      if (client.player != null) {
         client.player.getAbilities().flying = this.savedAbilitiesFlying;
      }
   }

   public void tryCompleteActivation(MinecraftClient client) {
      if (this.isEnabled() && this.activationPending && client.player != null && client.world != null) {
         this.savedX = client.player.getX();
         this.savedY = client.player.getY();
         this.savedZ = client.player.getZ();
         this.savedYaw = client.player.getYaw();
         this.savedPitch = client.player.getPitch();
         this.currentX = this.prevX = this.savedX;
         this.currentY = this.prevY = this.savedY + (double)client.player.getStandingEyeHeight();
         this.currentZ = this.prevZ = this.savedZ;
         this.currentYaw = this.prevYaw = this.savedYaw;
         this.currentPitch = this.prevPitch = this.savedPitch;
         this.savedAbilitiesFlying = client.player.getAbilities().flying;
         this.switchedPerspectiveForBody = false;
         if (this.showPlayerModel.get()) {
            this.perspectiveBeforeFreecam = client.options.getPerspective();
            if (this.perspectiveBeforeFreecam.isFirstPerson()) {
               client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
               this.switchedPerspectiveForBody = true;
            }
         }

         this.activationPending = false;
         this.active = true;
         this.captureMovementLatches(client);
         this.maybeLatchWalkFromVelocity(client);
         this.autopilotWarmupTicks = 40;
         this.lastSyncedCamChunk = null;
         this.lastSyncedLoadDistance = Integer.MIN_VALUE;
         this.applyViewOnlyClientState(client);
         this.syncFreecamChunkLoading(client);
         reapplyBodyInput(client);
      }
   }

   private void captureMovementLatches(MinecraftClient client) {
      GameOptions gameOptions = client.options;
      ClientPlayerEntity player = client.player;
      PlayerInput playerInput = player != null ? player.input.playerInput : PlayerInput.DEFAULT;
      this.latchedForward = gameOptions.forwardKey.isPressed() || playerInput.forward();
      this.latchedBack = gameOptions.backKey.isPressed() || playerInput.backward();
      this.latchedLeft = gameOptions.leftKey.isPressed() || playerInput.left();
      this.latchedRight = gameOptions.rightKey.isPressed() || playerInput.right();
      this.latchedJump = gameOptions.jumpKey.isPressed() || playerInput.jump();
      this.latchedSneak = gameOptions.sneakKey.isPressed() || playerInput.sneak() || player != null && player.isSneaking();
      this.latchedSprint = gameOptions.sprintKey.isPressed() || playerInput.sprint();
      this.mergeLatchFromAutoWalk();
      this.mergeLatchFromLivingSpeed(client.player);
   }

   private void mergeLatchFromAutoWalk() {
      AutoWalkModule autoWalkModule = KrypticClient.modules() != null ? KrypticClient.modules().autoWalk : null;
      if (autoWalkModule != null && autoWalkModule.isEnabled()) {
         this.latchedForward = true;
      }
   }

   public void mergeAutopilotFromCurrentState(MinecraftClient client) {
      if (client.player != null) {
         this.mergeLatchFromAutoWalk();
         if (!this.bodyFollowsKeys.get()) {
            this.latchedSneak = this.latchedSneak | client.player.isSneaking();
         } else {
            GameOptions gameOptions = client.options;
            PlayerInput playerInput = client.player.input.playerInput;
            this.latchedForward = this.latchedForward | (gameOptions.forwardKey.isPressed() || playerInput.forward());
            this.latchedBack = this.latchedBack | (gameOptions.backKey.isPressed() || playerInput.backward());
            this.latchedLeft = this.latchedLeft | (gameOptions.leftKey.isPressed() || playerInput.left());
            this.latchedRight = this.latchedRight | (gameOptions.rightKey.isPressed() || playerInput.right());
            this.latchedJump = this.latchedJump | (gameOptions.jumpKey.isPressed() || playerInput.jump());
            this.latchedSneak = this.latchedSneak | (gameOptions.sneakKey.isPressed() || playerInput.sneak());
            this.latchedSprint = this.latchedSprint | (gameOptions.sprintKey.isPressed() || playerInput.sprint());
         }

         this.mergeLatchFromLivingSpeed(client.player);
         this.mergeVelocityIntoLatch(client.player);
      }
   }

   private void mergeLatchFromLivingSpeed(ClientPlayerEntity player) {
      if (player != null) {
         float f = player.forwardSpeed;
         float f3 = player.sidewaysSpeed;
         if (f > 0.015F) {
            this.latchedForward = true;
         }

         if (f < -0.015F) {
            this.latchedBack = true;
         }

         if (f3 > 0.015F) {
            this.latchedLeft = true;
         }

         if (f3 < -0.015F) {
            this.latchedRight = true;
         }
      }
   }

   private void mergeVelocityIntoLatch(ClientPlayerEntity player) {
      Vec3d vec = player.getVelocity();
      double d = vec.x;
      double coord = vec.z;
      if (!(d * d + coord * coord < 1.0E-10)) {
         Vec3d vec2 = flatLook(player.getYaw());
         double currentScore = d * vec2.x + coord * vec2.z;
         double coord3 = d * -vec2.z + coord * vec2.x;
         if (Math.abs(currentScore) >= Math.abs(coord3)) {
            if (currentScore > 0.008) {
               this.latchedForward = true;
            } else if (currentScore < -0.008) {
               this.latchedBack = true;
            }
         } else if (coord3 > 0.008) {
            this.latchedLeft = true;
         } else if (coord3 < -0.008) {
            this.latchedRight = true;
         }
      }
   }

   private void maybeLatchWalkFromVelocity(MinecraftClient client) {
      if (client.player != null && !this.latchedForward && !this.latchedBack && !this.latchedLeft && !this.latchedRight) {
         Vec3d vec = client.player.getVelocity();
         double d = vec.x;
         double coord = vec.z;
         if (!(d * d + coord * coord < 1.0E-8)) {
            Vec3d vec2 = flatLook(client.player.getYaw());
            double currentScore = d * vec2.x + coord * vec2.z;
            double coord3 = d * -vec2.z + coord * vec2.x;
            if (Math.abs(currentScore) > Math.abs(coord3)) {
               if (currentScore > 0.008) {
                  this.latchedForward = true;
               } else if (currentScore < -0.008) {
                  this.latchedBack = true;
               }
            } else if (coord3 > 0.008) {
               this.latchedLeft = true;
            } else if (coord3 < -0.008) {
               this.latchedRight = true;
            }
         }
      }
   }

   private static Vec3d flatLook(float f) {
      double d = Math.toRadians((double)f);
      return new Vec3d(-Math.sin(d), 0.0, Math.cos(d));
   }

   private void clearMovementLatches() {
      this.latchedForward = this.latchedBack = this.latchedLeft = this.latchedRight = false;
      this.latchedJump = this.latchedSneak = this.latchedSprint = false;
      this.autopilotWarmupTicks = 0;
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (this.isEnabled()) {
         this.tryCompleteActivation(client);
         if (this.active && client.player != null) {
            if (this.autopilotWarmupTicks > 0) {
               this.autopilotWarmupTicks--;
               this.mergeAutopilotFromCurrentState(client);
            }

            this.prevX = this.currentX;
            this.prevY = this.currentY;
            this.prevZ = this.currentZ;
            this.prevYaw = this.currentYaw;
            this.prevPitch = this.currentPitch;
            float f = this.speed.getFloat();
            float f4 = this.verticalMultiplier.getFloat();
            float f5 = this.smoothing.get() ? 0.5F : 1.0F;
            GameOptions gameOptions = client.options;
            double d = 0.0;
            double coord = 0.0;
            double currentScore = 0.0;
            if (gameOptions.forwardKey.isPressed()) {
               d++;
            }

            if (gameOptions.backKey.isPressed()) {
               d--;
            }

            if (gameOptions.leftKey.isPressed()) {
               coord++;
            }

            if (gameOptions.rightKey.isPressed()) {
               coord--;
            }

            if (gameOptions.jumpKey.isPressed()) {
               currentScore++;
            }

            if (gameOptions.sneakKey.isPressed()) {
               currentScore--;
            }

            ClientPlayerEntity player = client.player;
            if (player.getAbilities().creativeMode) {
               player.getAbilities().flying = false;
            }

            double coord3 = Math.toRadians((double)this.currentYaw);
            double coord4 = -Math.sin(coord3) * d * (double)f + Math.cos(coord3) * coord * (double)f;
            double coord5 = Math.cos(coord3) * d * (double)f + Math.sin(coord3) * coord * (double)f;
            double coord6 = currentScore * (double)f * (double)f4;
            this.currentX += coord4 * (double)f5;
            this.currentY += coord6 * (double)f5;
            this.currentZ += coord5 * (double)f5;
            this.syncFreecamChunkLoading(client);
         }
      }
   }

   public static void reapplyBodyInput(MinecraftClient client) {
      FreecamModule freecamModule = instance;
      if (freecamModule != null && freecamModule.isActive() && client.player != null) {
         ClientPlayerEntity player = client.player;
         GameOptions gameOptions = client.options;
         boolean pressed = freecamModule.bodyFollowsKeys.get();
         boolean pressed2 = freecamModule.latchedForward || pressed && gameOptions.forwardKey.isPressed();
         boolean pressed3 = freecamModule.latchedBack || pressed && gameOptions.backKey.isPressed();
         boolean pressed4 = freecamModule.latchedLeft || pressed && gameOptions.leftKey.isPressed();
         boolean pressed5 = freecamModule.latchedRight || pressed && gameOptions.rightKey.isPressed();
         boolean pressed6 = freecamModule.latchedJump;
         boolean pressed7 = freecamModule.latchedSneak || pressed && gameOptions.sneakKey.isPressed();
         boolean pressed8 = freecamModule.latchedSprint || pressed && gameOptions.sprintKey.isPressed();
         if (pressed) {
            if (gameOptions.backKey.isPressed()) {
               freecamModule.latchedForward = false;
            }

            if (gameOptions.forwardKey.isPressed()) {
               freecamModule.latchedBack = false;
            }

            if (gameOptions.rightKey.isPressed()) {
               freecamModule.latchedLeft = false;
            }

            if (gameOptions.leftKey.isPressed()) {
               freecamModule.latchedRight = false;
            }
         }

         player.input.playerInput = new PlayerInput(pressed2, pressed3, pressed4, pressed5, pressed6, pressed7, pressed8);
         float f = (pressed4 ? 1.0F : 0.0F) - (pressed5 ? 1.0F : 0.0F);
         float f3 = (pressed2 ? 1.0F : 0.0F) - (pressed3 ? 1.0F : 0.0F);
         Vec2f vec2f = new Vec2f(f, f3).normalize();
         ((ClientInputAccessor)player.input).kryptic$setMoveVector(vec2f);
         player.setSneaking(pressed7);
      }
   }

   public boolean hasLatchedLocomotion() {
      return this.latchedForward || this.latchedBack || this.latchedLeft || this.latchedRight || this.latchedJump || this.latchedSneak;
   }

   private void syncFreecamChunkLoading(MinecraftClient client) {
      if (this.active && client.world != null && client.player != null) {
         ClientChunkManager clientChunkManager = client.world.getChunkManager();
         ChunkPos chunkPos = new ChunkPos((int)Math.floor(this.currentX) >> 4, (int)Math.floor(this.currentZ) >> 4);
         ChunkPos chunkPos2 = client.player.getChunkPos();
         ChunkPos chunkPos3 = chunkPos;
         int n = Math.max(Math.abs(chunkPos.x - chunkPos2.x), Math.abs(chunkPos.z - chunkPos2.z));
         if (n + 4 > 32) {
            int step = (chunkPos.x + chunkPos2.x) / 2;
            int step2 = (chunkPos.z + chunkPos2.z) / 2;
            chunkPos3 = new ChunkPos(step, step2);
         }

         int n9 = Math.max(
            Math.max(Math.abs(chunkPos3.x - chunkPos.x), Math.abs(chunkPos3.z - chunkPos.z)),
            Math.max(Math.abs(chunkPos3.x - chunkPos2.x), Math.abs(chunkPos3.z - chunkPos2.z))
         );
         int n10 = Math.min(32, Math.max(2, Math.round(this.freecamChunkDistance.getFloat())));
         int n11 = Math.min(32, Math.max(n10, n9 + 4));
         if (n11 != this.lastSyncedLoadDistance) {
            clientChunkManager.updateLoadDistance(n11);
            this.lastSyncedLoadDistance = n11;
         }

         if (this.lastSyncedCamChunk == null || chunkPos3.x != this.lastSyncedCamChunk.x || chunkPos3.z != this.lastSyncedCamChunk.z) {
            clientChunkManager.setChunkMapCenter(chunkPos3.x, chunkPos3.z);
            this.lastSyncedCamChunk = chunkPos3;
            if (client.worldRenderer != null) {
               client.worldRenderer.scheduleTerrainUpdate();
            }
         }
      }
   }

   private void restoreVanillaChunkLoading(MinecraftClient client) {
      this.lastSyncedCamChunk = null;
      this.lastSyncedLoadDistance = Integer.MIN_VALUE;
      if (client.world != null) {
         ClientChunkManager clientChunkManager = client.world.getChunkManager();
         if (client.player != null) {
            ChunkPos chunkPos = client.player.getChunkPos();
            clientChunkManager.setChunkMapCenter(chunkPos.x, chunkPos.z);
         }

         clientChunkManager.updateLoadDistance(client.options.getClampedViewDistance());
         if (client.worldRenderer != null) {
            client.worldRenderer.scheduleTerrainUpdate();
         }
      }
   }

   private void applyViewOnlyClientState(MinecraftClient client) {
      this.savedSmartCull = client.chunkCullingEnabled;
      client.chunkCullingEnabled = false;
   }

   private void restoreViewOnlyClientState(MinecraftClient client) {
      client.chunkCullingEnabled = this.savedSmartCull;
      if (client.interactionManager != null) {
         client.interactionManager.cancelBlockBreaking();
      }
   }

   public boolean isActive() {
      return this.isEnabled() && this.active;
   }

   public boolean isShowPlayerModel() {
      return this.showPlayerModel.get();
   }

   public boolean isShowHands() {
      return this.showHands.get();
   }

   public boolean renderHands() {
      return !this.isActive() || this.isShowHands();
   }

   public boolean wasHoldingSneak() {
      return this.latchedSneak;
   }

   public double getInterpolatedX(float f) {
      return MathHelper.lerp((double)f, this.prevX, this.currentX);
   }

   public double getInterpolatedY(float f) {
      return MathHelper.lerp((double)f, this.prevY, this.currentY);
   }

   public double getInterpolatedZ(float f) {
      return MathHelper.lerp((double)f, this.prevZ, this.currentZ);
   }

   public float getInterpolatedYaw(float f) {
      return MathHelper.lerp(f, this.prevYaw, this.currentYaw);
   }

   public float getInterpolatedPitch(float f) {
      return MathHelper.lerp(f, this.prevPitch, this.currentPitch);
   }

   public Vec3d getInterpolatedPos(float f) {
      return new Vec3d(this.getInterpolatedX(f), this.getInterpolatedY(f), this.getInterpolatedZ(f));
   }

   public void setRotation(float f, float f3) {
      this.currentYaw = f;
      this.currentPitch = MathHelper.clamp(f3, -90.0F, 90.0F);
   }

   public float getCurrentYaw() {
      return this.currentYaw;
   }

   public float getCurrentPitch() {
      return this.currentPitch;
   }

   public float getLookSensitivity() {
      return this.lookSensitivity.getFloat();
   }
}
