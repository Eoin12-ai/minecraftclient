package dev.kryptic.mixin;

import dev.kryptic.suschunk.ServerLightCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public class LightPacketMixin {
   @Inject(
      method = {"onLightUpdate"},
      at = {@At("TAIL")}
   )
   private void kryptic$captureLightUpdate(LightUpdateS2CPacket lightUpdateS2CPacket, CallbackInfo callbackInfo) {
      ServerLightCache.get().ingest(lightUpdateS2CPacket.getChunkX(), lightUpdateS2CPacket.getChunkZ(), lightUpdateS2CPacket.getData(), MinecraftClient.getInstance().world);
   }

   @Inject(
      method = {"onChunkData"},
      at = {@At("TAIL")}
   )
   private void kryptic$captureChunkLight(ChunkDataS2CPacket lightUpdateS2CPacket, CallbackInfo callbackInfo) {
      ServerLightCache.get().ingest(lightUpdateS2CPacket.getChunkX(), lightUpdateS2CPacket.getChunkZ(), lightUpdateS2CPacket.getLightData(), MinecraftClient.getInstance().world);
   }
}
