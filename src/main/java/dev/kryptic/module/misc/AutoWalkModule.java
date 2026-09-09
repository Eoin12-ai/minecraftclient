package dev.kryptic.module.misc;

import dev.kryptic.mixin.KeyMappingAccessor;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.client.util.InputUtil.Type;

public class AutoWalkModule extends Module {
   public final ModeSetting mode = this.addSetting(new ModeSetting("Mode", "Walking mode.", "Simple", "Simple", "Smart"));
   public final ModeSetting direction = this.addSetting(
      new ModeSetting("Direction", "The direction to walk in Simple mode.", "Forwards", "Forwards", "Backwards", "Left", "Right")
   );
   public final BooleanSetting disableOnInput = this.addSetting(new BooleanSetting("Disable On Input", "Disable the module on manual movement input.", false));
   public final BooleanSetting disableOnY = this.addSetting(new BooleanSetting("Disable On Y Change", "Disable the module if you move vertically.", false));
   public final BooleanSetting waitForChunks = this.addSetting(new BooleanSetting("Stop At Unloaded", "Do not walk into unloaded chunks.", true));

   public AutoWalkModule() {
      super("Auto Walk", "Automatically walks forward.", Category.MISC);
      this.direction.visibleWhen(() -> this.mode.is("Simple"));
      this.disableOnY.visibleWhen(() -> this.mode.is("Simple"));
      this.waitForChunks.visibleWhen(() -> this.mode.is("Simple"));
   }

   @Override
   protected void onDisable() {
      this.release(MinecraftClient.getInstance());
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.world != null) {
         this.release(client);
         if (this.mode.is("Smart")) {
            client.options.forwardKey.setPressed(true);
            client.options.sprintKey.setPressed(true);
            if (client.player.horizontalCollision && client.player.isOnGround()) {
               client.options.jumpKey.setPressed(true);
            }
         } else if (this.disableOnY.get() && client.player.lastY != client.player.getY()) {
            this.toggle();
         } else if (!this.waitForChunks.get() || this.chunkAheadLoaded(client)) {
            String text = this.direction.get();
            switch (text) {
               case "Forwards":
                  client.options.forwardKey.setPressed(true);
                  break;
               case "Backwards":
                  client.options.backKey.setPressed(true);
                  break;
               case "Left":
                  client.options.leftKey.setPressed(true);
                  break;
               case "Right":
                  client.options.rightKey.setPressed(true);
            }
         }
      } else {
         this.release(client);
      }
   }

   @Override
   public boolean onKeyPress(int n) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (this.disableOnInput.get() && client.currentScreen == null && this.isMovementKey(client, n)) {
         this.toggle();
      }

      return false;
   }

   private boolean chunkAheadLoaded(MinecraftClient client) {
      double d = Math.toRadians((double)client.player.getYaw());
      double coord = -Math.sin(d);
      double currentScore = Math.cos(d);
      String text = this.direction.get();
      double coord3;
      double coord4;
      switch (text) {
         case "Backwards":
            coord3 = -coord;
            coord4 = -currentScore;
            break;
         case "Left":
            coord3 = -currentScore;
            coord4 = coord;
            break;
         case "Right":
            coord3 = currentScore;
            coord4 = -coord;
            break;
         default:
            coord3 = coord;
            coord4 = currentScore;
      }

      int n = (int)Math.floor(client.player.getX() + coord3 * 2.0);
      int offset = (int)Math.floor(client.player.getZ() + coord4 * 2.0);
      return client.world.getChunkManager().isChunkLoaded(n >> 4, offset >> 4);
   }

   private void release(MinecraftClient client) {
      if (client.options != null) {
         client.options.forwardKey.setPressed(false);
         client.options.backKey.setPressed(false);
         client.options.leftKey.setPressed(false);
         client.options.rightKey.setPressed(false);
         client.options.jumpKey.setPressed(false);
         client.options.sprintKey.setPressed(false);
      }
   }

   private boolean isMovementKey(MinecraftClient client, int n) {
      GameOptions gameOptions = client.options;
      return matches(gameOptions.forwardKey, n)
         || matches(gameOptions.backKey, n)
         || matches(gameOptions.leftKey, n)
         || matches(gameOptions.rightKey, n)
         || matches(gameOptions.jumpKey, n)
         || matches(gameOptions.sneakKey, n);
   }

   private static boolean matches(KeyBinding keyBinding, int n) {
      Key key = ((KeyMappingAccessor)keyBinding).kryptic$getKey();
      return key.getCategory() == Type.KEYSYM && key.getCode() == n;
   }
}
