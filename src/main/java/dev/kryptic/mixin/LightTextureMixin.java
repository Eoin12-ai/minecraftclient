package dev.kryptic.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
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
         target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"
      )},
      slice = {@Slice(
         from = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/GameOptions;getGamma()Lnet/minecraft/client/option/SimpleOption;"
         )
      )}
   )
   private Object kryptic$fullbrightGamma(Object value2) {
      ModuleManager moduleManager = KrypticClient.modules();
      return moduleManager != null && moduleManager.fullbright != null && moduleManager.fullbright.isEnabled() ? (double)moduleManager.fullbright.gamma.getFloat() : value2;
   }
}
