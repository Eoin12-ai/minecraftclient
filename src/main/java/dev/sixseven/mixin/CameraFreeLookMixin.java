package dev.sixseven.mixin;

import dev.sixseven.module.misc.FreeLookModule;
import dev.sixseven.module.misc.FreecamModule;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({Camera.class})
public abstract class CameraFreeLookMixin {
   @ModifyArgs(
      method = {"update"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/Camera;setRotation(FF)V"
      )
   )
   private void sixsevenclient$freeLookRotation(Args args) {
      FreeLookModule freeLookModule = FreeLookModule.get();
      if (freeLookModule != null && freeLookModule.isActive()) {
         FreecamModule freecamModule = FreecamModule.get();
         if (freecamModule == null || !freecamModule.isActive()) {
            args.set(0, freeLookModule.getCameraYaw());
            args.set(1, freeLookModule.getCameraPitch());
         }
      }
   }

   @Inject(
      method = {"clipToSpace"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sixsevenclient$freeLookThroughWalls(float f, CallbackInfoReturnable<Float> callbackInfoReturnable) {
      FreeLookModule freeLookModule = FreeLookModule.get();
      if (freeLookModule != null && freeLookModule.seeThroughWalls()) {
         FreecamModule freecamModule = FreecamModule.get();
         if (freecamModule == null || !freecamModule.isActive()) {
            callbackInfoReturnable.setReturnValue(f);
         }
      }
   }
}
