package com.swyzzyaddon.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pkg1.MouseMixinUtil;

@Mixin({Mouse.class})
public abstract class MouseMixin {
   @WrapOperation(
      method = {"method_1606"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_5872(DD)V"
      )}
   )
   private void swyzzy$look(ClientPlayerEntity var1, double var2, double var4, Operation<Void> var6) {
      if (!MouseMixinUtil.check(var2, var4)) {
         var6.call(new Object[]{var1, var2, var4});
      }
   }

   @Inject(
      method = {"method_1598"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void swyzzy$scroll(long var1, double var3, double var5, CallbackInfo var7) {
      if (MouseMixinUtil.check2(var5)) {
         var7.cancel();
      }
   }
}
