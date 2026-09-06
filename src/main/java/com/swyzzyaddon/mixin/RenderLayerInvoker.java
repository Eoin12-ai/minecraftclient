package com.swyzzyaddon.mixin;

import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * RenderLayerInvoker — stubbed for 1.21.5 compat.
 * RenderSetup and method_75940 are snapshot-only APIs.
 */
@Mixin({RenderLayer.class})
public interface RenderLayerInvoker {
    // stub — RenderSetup invoker not available in 1.21.5 stable Yarn
}
