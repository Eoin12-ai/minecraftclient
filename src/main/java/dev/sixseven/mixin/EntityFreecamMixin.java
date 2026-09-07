package dev.sixseven.mixin;

import dev.sixseven.module.misc.FreecamModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public class EntityFreecamMixin {
   @Inject(
      method = {"isInvisibleTo"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sixsevenclient$freecamSeeOwnBody(PlayerEntity player, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
      Entity entity = (Entity)(Object)this;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && entity == client.player && player == client.player) {
         FreecamModule freecamModule = FreecamModule.get();
         if (freecamModule != null && freecamModule.isActive() && freecamModule.isShowPlayerModel()) {
            callbackInfoReturnable.setReturnValue(false);
         }
      }
   }
}
