package dev.sixseven.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin({LightmapTextureManager.class})
public class LightTextureMixin {
   @ModifyExpressionValue(
      method = {"update"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/SimpleOption;getValue()Ljava/lang/Object;"
      )},
      slice = {@Slice(
         from = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/GameOptions;getGamma()Lnet/minecraft/SimpleOption;"
         )
      )}
   )
   private Object sixsevenclient$fullbrightGamma(Object value2) {
      ModuleManager moduleManager = SixSevenClient.modules();
      return moduleManager != null && moduleManager.fullbright != null && moduleManager.fullbright.isEnabled() ? (double)moduleManager.fullbright.gamma.getFloat() : value2;
   }
}
