package com.swyzzyaddon.mixin;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientCommonNetworkHandler.class})
public abstract class ClientCommonNetworkHandlerMixin {
   @Inject(
      method = {"method_52787"},
      at = {@At("HEAD")}
   )
   private void swyzzy$sendPacket(Packet<?> var1, CallbackInfo var2) {
      SwyzzyAddon.run3(var1);
   }
}
