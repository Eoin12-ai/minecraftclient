package dev.sixseven.mixin;

import dev.sixseven.module.misc.FreeLookModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Entity.class})
public class EntityFreeLookMixin {
   @Inject(
      method = {"changeLookDirection(DD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sixsevenclient$freeLookTurn(double d, double d2, CallbackInfo callbackInfo) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (this == client.player) {
         FreeLookModule freeLookModule = FreeLookModule.get();
         if (freeLookModule != null && freeLookModule.cameraMode()) {
            freeLookModule.addCameraLook(temp, temp3);
            temp2.cancel();
         }
      }
   }
}
