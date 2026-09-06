package util;

import net.minecraft.client.render.RenderLayer;

/**
 * RenderPipelineUtils — stubbed for 1.21.5 compat.
 * com.mojang.blaze3d.pipeline.RenderPipeline is a snapshot-only API.
 */
public final class RenderPipelineUtils {
    private RenderPipelineUtils() {}

    public static RenderLayer getLayer(String name) {
        return RenderLayer.getDebugQuads();
    }

    public static RenderLayer getLinesLayer(String name) {
        return RenderLayer.getLines();
    }
}
