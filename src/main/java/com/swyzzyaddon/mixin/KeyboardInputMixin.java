package com.swyzzyaddon.mixin;

import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pkg1.FreecamModule;

@Mixin({KeyboardInput.class})
public abstract class KeyboardInputMixin {
   @Inject(
      method = {"method_3129"},
      at = {@At("TAIL")}
   )
   private void swyzzy$freezeInput(CallbackInfo var1) {
      PlayerInput var2 = FreecamModule.getclass10185();
      if (var2 != null) {
         KeyboardInput var3 = (KeyboardInput)(Object)this;
         var3.playerInput = var2;
         ((InputAccessor)var3)
            .swyzzy$setMovementVector(new Vec2f(swyzzy$axis(var2.left(), var2.right()), swyzzy$axis(var2.forward(), var2.backward())).normalize());
      }
   }

   private static float swyzzy$axis(boolean var0, boolean var1) {
      if (var0 == var1) {
         return 0.0F;
      } else {
         return var0 ? 1.0F : -1.0F;
      }
   }
}
