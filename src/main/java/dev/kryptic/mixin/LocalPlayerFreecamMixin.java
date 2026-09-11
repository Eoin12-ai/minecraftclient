package dev.kryptic.mixin;

import dev.kryptic.module.misc.FreecamModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayerEntity.class})
public abstract class LocalPlayerFreecamMixin {
   @Inject(
      method = {"tickMovement"},
      at = {@At("HEAD")}
   )
   private void kryptic$freecamReapplyBeforeAiStep(CallbackInfo callbackInfo) {
      this.kryptic$reapply();
   }

   @Inject(
      method = {"sendMovementPackets"},
      at = {@At("HEAD")}
   )
   private void kryptic$freecamReapplyBeforeSendPosition(CallbackInfo callbackInfo) {
      this.kryptic$reapply();
   }

   private void kryptic$reapply() {
      ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == player) {
         FreecamModule freecamModule = FreecamModule.get();
         if (freecamModule != null && freecamModule.isActive()) {
            FreecamModule.reapplyBodyInput(client);
         }
      }
   }
}
