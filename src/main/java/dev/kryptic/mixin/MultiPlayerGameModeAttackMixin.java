package dev.kryptic.mixin;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
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
   private void kryptic$onAttack(PlayerEntity player, Entity entity, CallbackInfo callbackInfo) {
      if (player == MinecraftClient.getInstance().player && entity != player) {
         ModuleManager moduleManager = KrypticClient.modules();
      }
   }
}
