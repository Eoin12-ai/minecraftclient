package dev.sixseven.render;

import dev.sixseven.module.visuals.BlockOutlineModule;
import dev.sixseven.util.Colors;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

/**
 * BlockOutlineRenderer — stubbed for 1.21.5 build compatibility.
 * OutlineRenderState API is only available in newer MC snapshots.
 * This stub allows compilation; the mixin that calls this is also stubbed.
 */
public final class BlockOutlineRenderer {
    private BlockOutlineRenderer() {}

    public static void render(Immediate immediate, MatrixStack matrices,
                              Object outlineRenderState, Vec3d vec,
                              BlockOutlineModule mod) {
        // no-op stub — OutlineRenderState not available in 1.21.5 stable
    }

    private static int animatedColor(BlockOutlineModule mod, double t) {
        int base = mod.color.get();
        float pulse = pulseFactor(mod, t);
        int a = Colors.alpha(base);
        return Colors.withAlpha(base, (int)(a * (0.75f + 0.25f * pulse)));
    }

    private static float pulseFactor(BlockOutlineModule mod, double t) {
        return mod.pulse.get() ? (float)(Math.sin(t * 2.0 * Math.PI * mod.pulseSpeed.getFloat()) * 0.5 + 0.5) : 1.0f;
    }
}
