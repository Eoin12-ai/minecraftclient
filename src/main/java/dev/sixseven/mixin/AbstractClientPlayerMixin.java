package dev.sixseven.mixin;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.module.misc.SkinProtectModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AbstractClientPlayerEntity.class})
public class AbstractClientPlayerMixin {
   @Inject(
      method = {"getSkinTextures"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void sixsevenclient$replaceSkin(CallbackInfoReturnable<SkinTextures> callbackInfoReturnable) {
      ModuleManager moduleManager = SixSevenClient.modules();
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
