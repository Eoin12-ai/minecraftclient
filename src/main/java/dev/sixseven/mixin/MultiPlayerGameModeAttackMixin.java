package dev.sixseven.mixin;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayerInteractionManager.class})
public class MultiPlayerGameModeAttackMixin {
   @Inject(
      method = {"attackEntity"},
      at = {@At("HEAD")}
   )
   private void sixsevenclient$onAttack(PlayerEntity player, Entity entity, CallbackInfo callbackInfo) {
      if (player == MinecraftClient.getInstance().player && entity != player) {
         ModuleManager moduleManager = SixSevenClient.modules();
         if (moduleManager != null && moduleManager.hitParticles != null && moduleManager.hitParticles.isEnabled()) {
            moduleManager.hitParticles.onHit(entity);
         }
      }
   }
}
