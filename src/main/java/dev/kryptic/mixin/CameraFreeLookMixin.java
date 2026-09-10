package dev.kryptic.mixin;

import dev.kryptic.module.misc.FreeLookModule;
import dev.kryptic.module.misc.FreecamModule;
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
         target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"
      )
   )
   private void kryptic$freeLookRotation(Args args) {
      // Both modes need this. In Free Camera the mouse writes these angles and
      // the camera has to read them back; in Locked Camera nothing writes them
      // after enable, which is the point -- the camera holds the angle it was
      // enabled at while your body turns underneath it. Guarding this on
      // cameraMode() would leave Locked Camera doing nothing at all.
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
   private void kryptic$freeLookThroughWalls(float f, CallbackInfoReturnable<Float> callbackInfoReturnable) {
      FreeLookModule freeLookModule = FreeLookModule.get();
      if (freeLookModule != null && freeLookModule.seeThroughWalls()) {
         FreecamModule freecamModule = FreecamModule.get();
         if (freecamModule == null || !freecamModule.isActive()) {
            callbackInfoReturnable.setReturnValue(f);
         }
      }
   }
}
