package dev.kryptic.mixin;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.module.misc.SkinProtectModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractClientPlayerEntity.class})
public class AbstractClientPlayerMixin {
   @Inject(
      method = {"getSkin"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void kryptic$replaceSkin(CallbackInfoReturnable<SkinTextures> callbackInfoReturnable) {
      ModuleManager moduleManager = KrypticClient.modules();
      if (moduleManager != null) {
         SkinProtectModule skinProtectModule = moduleManager.skinProtect;
         if (skinProtectModule != null && skinProtectModule.isEnabled()) {
            SkinTextures skinTextures = skinProtectModule.replacementSkin();
            if (skinTextures != null) {
               AbstractClientPlayerEntity abstractClientPlayerEntity = (AbstractClientPlayerEntity)(Object)this;
               if (skinProtectModule.shouldReplace(abstractClientPlayerEntity.getUuid())) {
                  callbackInfoReturnable.setReturnValue(skinTextures);
               }
            }
         }
      }
   }
}
