package com.swyzzyaddon.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({RenderLayer.class})
public interface RenderLayerInvoker {
   @Invoker("method_75940")
   static RenderLayer swyzzy$of(String var0, RenderSetup var1) {
      throw new AssertionError();
   }
}
