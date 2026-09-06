package com.swyzzyaddon.mixin;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public abstract class ClientPlayNetworkHandlerMixin {
   @Inject(
      method = {"method_11128"},
      at = {@At("HEAD")}
   )
   private void swyzzy$chunkData(ChunkDataS2CPacket var1, CallbackInfo var2) {
      SwyzzyAddon.run2(var1);
   }

   @Inject(
      method = {"method_11100"},
      at = {@At("HEAD")}
   )
   private void swyzzy$chunkDelta(ChunkDeltaUpdateS2CPacket var1, CallbackInfo var2) {
      SwyzzyAddon.run2(var1);
   }

   @Inject(
      method = {"method_11094"},
      at = {@At("HEAD")}
   )
   private void swyzzy$blockEntity(BlockEntityUpdateS2CPacket var1, CallbackInfo var2) {
      SwyzzyAddon.run2(var1);
   }

   @Inject(
      method = {"method_11112"},
      at = {@At("HEAD")},
      require = 0
   )
   private void swyzzy$entitySpawn(EntitySpawnS2CPacket var1, CallbackInfo var2) {
      SwyzzyAddon.run2(var1);
   }

   @Inject(
      method = {"method_11148"},
      at = {@At("HEAD")},
      require = 0
   )
   private void swyzzy$entityStatus(EntityStatusS2CPacket var1, CallbackInfo var2) {
      SwyzzyAddon.run2(var1);
   }

   @Inject(
      method = {"method_11146"},
      at = {@At("HEAD")},
      require = 0
   )
   private void swyzzy$playSound(PlaySoundS2CPacket var1, CallbackInfo var2) {
      SwyzzyAddon.run2(var1);
   }

   @Inject(
      method = {"method_11125"},
      at = {@At("HEAD")},
      require = 0
   )
   private void swyzzy$playSoundFromEntity(PlaySoundFromEntityS2CPacket var1, CallbackInfo var2) {
      SwyzzyAddon.run2(var1);
   }
}
