package com.swyzzyaddon.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pkg1.FreecamModule;
import pkg1.FreelookModule;

@Mixin({Camera.class})
public abstract class CameraMixin {
   @Shadow
   private boolean ready;
   @Shadow
   private World area;
   @Shadow
   private Entity focusedEntity;
   @Shadow
   private boolean thirdPerson;
   @Shadow
   private float lastTickProgress;
   @Shadow
   private float cameraY;
   @Shadow
   private float lastCameraY;

   @Shadow
   protected abstract void setPos(double var1, double var3, double var5);

   @Shadow
   protected abstract void setRotation(float var1, float var2);

   @Shadow
   protected abstract void moveBy(float var1, float var2, float var3);

   @Inject(
      method = {"method_19321"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void swyzzy$camera(World var1, Entity var2, boolean var3, boolean var4, float var5, CallbackInfo var6) {
      FreecamModule var7 = FreecamModule.getVal();
      FreelookModule var8 = var7 == null ? FreelookModule.getVal() : null;
      if (var7 != null || var8 != null) {
         this.ready = true;
         this.area = var1;
         this.focusedEntity = var2;
         this.thirdPerson = true;
         this.lastTickProgress = var5;
         if (var7 != null) {
            Vec3d var9 = var7.getclass243();
            this.setRotation(var7.getFloat(), var7.getFloat2());
            this.setPos(var9.x, var9.y, var9.z);
         } else {
            this.setRotation(var8.getFloat(), var8.getFloat2());
            this.setPos(
               MathHelper.lerp(var5, var2.lastX, var2.getX()),
               MathHelper.lerp(var5, var2.lastY, var2.getY()) + MathHelper.lerp(var5, this.lastCameraY, this.cameraY),
               MathHelper.lerp(var5, var2.lastZ, var2.getZ())
            );
            this.moveBy(-var8.getFloat3(), 0.0F, 0.0F);
         }

         var6.cancel();
      }
   }
}
