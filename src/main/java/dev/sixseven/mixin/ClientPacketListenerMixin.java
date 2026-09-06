package dev.sixseven.mixin;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.module.render.BlockEntityEspModule;
import dev.sixseven.module.misc.SpawnerProtectModule;
import dev.sixseven.util.TpsTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
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
   private void sixsevenclient$trackTps(WorldTimeUpdateS2CPacket worldTimeUpdateS2CPacket, CallbackInfo callbackInfo) {
      TpsTracker.onTimePacket();
   }

   @Inject(
      method = {"sendChatCommand"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sixsevenclient$fakeCommands(String str, CallbackInfo callbackInfo) {
      ModuleManager moduleManager = SixSevenClient.modules();
      if (moduleManager != null) {
         try {
            if (moduleManager.fakePay != null && moduleManager.fakePay.tryIntercept(temp2)) {
               temp.cancel();
               return;
            }

            if (moduleManager.fakeStats != null && moduleManager.fakeStats.tryInterceptBalance(temp2)) {
               temp.cancel();
            }
         } catch (Exception ex) {
         }
      }
   }

   @Inject(
      method = {"onChunkData"},
      at = {@At("TAIL")}
   )
   private void sixsevenclient$blockEntityChunk(ChunkDataS2CPacket chunkDataS2CPacket, CallbackInfo callbackInfo) {
      BlockEntityEspModule blockEntityEspModule = module();
      if (blockEntityEspModule != null && blockEntityEspModule.isEnabled() && blockEntityEspModule.chunkPacketsEnabled()) {
         try {
            temp.getChunkData().getBlockEntities(temp.getChunkX(), temp.getChunkZ()).accept((arg, arg2, arg3) -> temp2.record(arg, arg2));
         } catch (Exception ex) {
         }
      }
   }

   @Inject(
      method = {"onBlockEntityUpdate"},
      at = {@At("TAIL")}
   )
   private void sixsevenclient$blockEntityUpdate(BlockEntityUpdateS2CPacket blockEntityUpdateS2CPacket, CallbackInfo callbackInfo) {
      BlockEntityEspModule blockEntityEspModule = module();
      if (blockEntityEspModule != null && blockEntityEspModule.isEnabled() && blockEntityEspModule.beUpdatePacketsEnabled()) {
         try {
            temp2.record(temp.getPos(), temp.getBlockEntityType());
         } catch (Exception ex) {
         }
      }
   }

   private static BlockEntityEspModule module() {
      ModuleManager moduleManager = SixSevenClient.modules();
      return moduleManager != null ? moduleManager.blockEntityEsp : null;
   }

   private static SpawnerProtectModule spawnerProtect() {
      ModuleManager moduleManager = SixSevenClient.modules();
      return moduleManager != null ? moduleManager.spawnerProtect : null;
   }

   @Inject(
      method = {"onBlockBreakingProgress"},
      at = {@At("HEAD")}
   )
   private void sixsevenclient$spawnerProtectDestruction(BlockBreakingProgressS2CPacket blockBreakingProgressS2CPacket, CallbackInfo callbackInfo) {
      SpawnerProtectModule spawnerProtectModule = spawnerProtect();
      if (spawnerProtectModule != null && spawnerProtectModule.isEnabled() && MinecraftClient.getInstance().isOnThread()) {
         try {
            temp2.onBlockDestructionPacket(temp.getEntityId(), temp.getPos());
         } catch (Exception ex) {
         }
      }
   }

   @Inject(
      method = {"onBlockUpdate"},
      at = {@At("HEAD")}
   )
   private void sixsevenclient$spawnerProtectBlockUpdate(BlockUpdateS2CPacket blockUpdateS2CPacket, CallbackInfo callbackInfo) {
      SpawnerProtectModule spawnerProtectModule = spawnerProtect();
      if (spawnerProtectModule != null && spawnerProtectModule.isEnabled() && spawnerProtectModule.detectBlockUpdatesEnabled() && MinecraftClient.getInstance().isOnThread()) {
         try {
            temp2.onServerBlockUpdate(temp.getPos(), temp.getState(), false);
         } catch (Exception ex) {
         }
      }
   }

   @Inject(
      method = {"onChunkDeltaUpdate"},
      at = {@At("HEAD")}
   )
   private void sixsevenclient$spawnerProtectSectionUpdate(ChunkDeltaUpdateS2CPacket chunkDeltaUpdateS2CPacket, CallbackInfo callbackInfo) {
      SpawnerProtectModule spawnerProtectModule = spawnerProtect();
      if (spawnerProtectModule != null && spawnerProtectModule.isEnabled() && spawnerProtectModule.detectBlockUpdatesEnabled() && MinecraftClient.getInstance().isOnThread()) {
         try {
            temp.visitUpdates((arg, arg2) -> temp2.onServerBlockUpdate(arg, arg2, true));
         } catch (Exception ex) {
         }
      }
   }
}
