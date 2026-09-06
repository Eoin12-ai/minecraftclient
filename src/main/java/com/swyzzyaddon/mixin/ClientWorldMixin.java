package com.swyzzyaddon.mixin;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientWorld.class})
public abstract class ClientWorldMixin {
   @Inject(
      method = {"method_41928"},
      at = {@At("TAIL")}
   )
   private void swyzzy$blockUpdate(BlockPos var1, BlockState var2, int var3, CallbackInfo var4) {
      SwyzzyAddon.run4(var1, var2);
   }
}
