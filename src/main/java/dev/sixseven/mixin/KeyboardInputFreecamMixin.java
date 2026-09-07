package dev.sixseven.mixin;

import dev.sixseven.module.misc.FreecamModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardInput.class})
public class KeyboardInputFreecamMixin {
   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void sixsevenclient$freecamReapplyCachedBodyInput(CallbackInfo callbackInfo) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.player.input == (net.minecraft.client.input.Input)this) {
         FreecamModule freecamModule = FreecamModule.get();
         if (freecamModule != null && freecamModule.isActive()) {
            FreecamModule.reapplyBodyInput(client);
         }
      }
   }
}
