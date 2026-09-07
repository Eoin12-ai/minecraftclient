package dev.sixseven.mixin;

import dev.sixseven.module.misc.FreecamModule;
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
   private void sixsevenclient$freecamReapplyBeforeAiStep(CallbackInfo callbackInfo) {
      this.sixsevenclient$reapply();
   }

   @Inject(
      method = {"sendMovementPackets"},
      at = {@At("HEAD")}
   )
   private void sixsevenclient$freecamReapplyBeforeSendPosition(CallbackInfo callbackInfo) {
      this.sixsevenclient$reapply();
   }

   private void sixsevenclient$reapply() {
      ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == player) {
         FreecamModule freecamModule = FreecamModule.get();
         if (freecamModule != null && freecamModule.isActive()) {
            FreecamModule.reapplyBodyInput(player);
         }
      }
   }
}
