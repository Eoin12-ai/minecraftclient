package dev.sixseven.mixin;

import dev.sixseven.module.misc.FreecamModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Mouse.class})
public abstract class MouseHandlerFreecamMixin {
   @Shadow
   private double cursorDeltaX;
   @Shadow
   private double cursorDeltaY;

   @Inject(
      method = {"updateMouse"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sixsevenclient$freecamMouse(double dParam, CallbackInfo callbackInfo) {
      FreecamModule freecamModule = FreecamModule.get();
      if (freecamModule != null && freecamModule.isActive()) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.currentScreen == null) {
            double d = (Double)client.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
            double coord = d * d * d * 8.0;
            double currentScore = this.cursorDeltaX * coord * (double)freecamModule.getLookSensitivity();
            double coord3 = this.cursorDeltaY * coord * (double)freecamModule.getLookSensitivity();
            float f = freecamModule.getCurrentYaw() + (float)currentScore * 0.15F;
            float f3 = freecamModule.getCurrentPitch() + (float)coord3 * 0.15F;
            freecamModule.setRotation(f, f3);
            callbackInfo.cancel();
         }
      }
   }
}
