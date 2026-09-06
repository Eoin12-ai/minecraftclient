package dev.sixseven.mixin;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * LevelRendererMixin — stubbed for 1.21.5 build compat.
 * WorldRenderState/OutlineRenderState APIs exist only in newer snapshots.
 * ESP rendering is disabled in this build; will restore when targeting the correct MC version.
 */
@Mixin({WorldRenderer.class})
public class LevelRendererMixin {
    // All rendering hooks that depend on WorldRenderState/OutlineRenderState
    // are stubbed as no-ops for the 1.21.5 stable build.
}
