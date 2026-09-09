package dev.kryptic.mixin;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.util.TpsTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public class ClientPacketListenerMixin {
   @Inject(
      method = {"onWorldTimeUpdate"},
      at = {@At("HEAD")}
   )
   private void kryptic$trackTps(WorldTimeUpdateS2CPacket worldTimeUpdateS2CPacket, CallbackInfo callbackInfo) {
      TpsTracker.onTimePacket();
   }

   @Inject(
      method = {"sendChatCommand"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void kryptic$fakeCommands(String str, CallbackInfo callbackInfo) {
      ModuleManager moduleManager = KrypticClient.modules();
      if (moduleManager != null) {
         try {
            if (moduleManager.fakePay != null && moduleManager.fakePay.tryIntercept(str)) {
               callbackInfo.cancel();
               return;
            }

            if (moduleManager.fakeStats != null && moduleManager.fakeStats.tryInterceptBalance(str)) {
               callbackInfo.cancel();
            }
         } catch (Exception ex) {
         }
      }
   }
}
