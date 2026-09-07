package dev.sixseven.mixin;

import dev.sixseven.module.misc.FreecamModule;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Camera.class})
public abstract class CameraFreecamMixin {
   @Shadow
   protected abstract void setPos(Vec3d vec);

   @Shadow
   protected abstract void setRotation(float vec, float f);

   @Inject(
      method = {"update"},
      at = {@At("TAIL")}
   )
   private void sixsevenclient$freecam(World world, Entity entity, boolean flag, boolean flag2, float f, CallbackInfo callbackInfo) {
      FreecamModule freecamModule = FreecamModule.get();
      if (freecamModule != null && freecamModule.isActive()) {
         this.setRotation(freecamModule.getInterpolatedYaw(f), freecamModule.getInterpolatedPitch(f));
         this.setPos(freecamModule.getInterpolatedPos(f));
      }
   }
}
