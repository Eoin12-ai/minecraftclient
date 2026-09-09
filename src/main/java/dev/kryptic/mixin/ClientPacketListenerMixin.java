package dev.kryptic.mixin;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.module.render.BlockEntityEspModule;
import dev.kryptic.util.TpsTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
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

   @Inject(
      method = {"onChunkData"},
      at = {@At("TAIL")}
   )
   private void kryptic$blockEntityChunk(ChunkDataS2CPacket chunkDataS2CPacket, CallbackInfo callbackInfo) {
      BlockEntityEspModule blockEntityEspModule = module();
      if (blockEntityEspModule != null && blockEntityEspModule.isEnabled() && blockEntityEspModule.chunkPacketsEnabled()) {
         try {
            chunkDataS2CPacket.getChunkData().getBlockEntities(chunkDataS2CPacket.getChunkX(), chunkDataS2CPacket.getChunkZ()).accept((arg, arg2, arg3) -> blockEntityEspModule.record(arg, arg2));
         } catch (Exception ex) {
         }
      }
   }

   @Inject(
      method = {"onBlockEntityUpdate"},
      at = {@At("TAIL")}
   )
   private void kryptic$blockEntityUpdate(BlockEntityUpdateS2CPacket blockEntityUpdateS2CPacket, CallbackInfo callbackInfo) {
      BlockEntityEspModule blockEntityEspModule = module();
      if (blockEntityEspModule != null && blockEntityEspModule.isEnabled() && blockEntityEspModule.beUpdatePacketsEnabled()) {
         try {
            blockEntityEspModule.record(blockEntityUpdateS2CPacket.getPos(), blockEntityUpdateS2CPacket.getBlockEntityType());
         } catch (Exception ex) {
         }
      }
   }

   private static BlockEntityEspModule module() {
      ModuleManager moduleManager = KrypticClient.modules();
      return moduleManager != null ? moduleManager.blockEntityEsp : null;
   }
}
