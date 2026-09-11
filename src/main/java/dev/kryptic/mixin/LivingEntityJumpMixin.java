package dev.kryptic.mixin;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntity.class})
public class LivingEntityJumpMixin {
   @Inject(
      method = {"jump"},
      at = {@At("HEAD")}
   )
   private void kryptic$onJump(CallbackInfo callbackInfo) {
      if (((Object)this) instanceof ClientPlayerEntity && ((ClientPlayerEntity)(Object)this) == MinecraftClient.getInstance().player) {
         ModuleManager moduleManager = KrypticClient.modules();
         if (moduleManager != null && moduleManager.jumpCircles != null && moduleManager.jumpCircles.isEnabled()) {
            moduleManager.jumpCircles.onPlayerJump((ClientPlayerEntity)(Object)this);
         }
      }
   }
}
