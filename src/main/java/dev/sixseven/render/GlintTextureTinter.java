package dev.sixseven.render;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.misc.CustomGlintModule;

/**
 * GlintTextureTinter — stubbed for 1.21.5 compat.
 * The GpuTexture/getGlTexture API is snapshot-only.
 * Custom glint colors are disabled in this build.
 */
public final class GlintTextureTinter {
    private GlintTextureTinter() {}

    public static void onFrame() {
        // no-op: GpuTexture API not available in 1.21.5 stable Yarn
    }

    public static void onDisable() {
        // no-op
    }
}
