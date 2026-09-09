package dev.sixseven.render;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

/**
 * The one place anything gets drawn into the world.
 *
 * Every renderer under {@code dev.sixseven.render} was written to be called
 * with the same four things — a buffer source, the matrix stack, the camera
 * position, and its module — but nothing ever called them: the mixin that was
 * supposed to do it had been stubbed out to a comment. So the ESPs, the
 * nametags, the jump circles and the cosmetics all existed, appeared in the
 * ClickGUI, could be toggled, and drew nothing.
 *
 * Fabric's world render event replaces that mixin. BEFORE_DEBUG_RENDER is the
 * right point: terrain and entities are already on the framebuffer, and it is
 * the pass vanilla itself uses for overlay lines, so depth behaves the way an
 * ESP wants it to.
 *
 * Vertices go in camera-relative, which is what these renderers already do by
 * subtracting the camera position from every world coordinate.
 *
 * <p>Each renderer is called behind its own guard. A renderer that throws is
 * logged once and then left out for the rest of the session — one broken ESP
 * should not take the other fifteen down with it, and it certainly should not
 * throw sixty times a second into the log.
 */
public final class WorldRenderHook {

    /** Renderers that have thrown. Keyed by name so the entry survives a lambda. */
    private static final Set<String> failed = new HashSet<>();

    private WorldRenderHook() {
    }

    public static void init() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(WorldRenderHook::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        ModuleManager modules = SixSevenClient.modules();
        if (modules == null) return;

        // The world's own buffer source is an Immediate; anything else means the
        // pipeline has been replaced underneath us and these renderers cannot
        // safely draw into it.
        VertexConsumerProvider consumers = context.consumers();
        if (!(consumers instanceof VertexConsumerProvider.Immediate immediate)) return;

        MatrixStack matrices = context.matrices();
        if (matrices == null) return;

        Vec3d camera = client.gameRenderer.getCamera().getCameraPos();

        // ── cosmetics ────────────────────────────────────────────────────────
        run("accessories", () -> {
            if (modules.customAccessories != null && modules.customAccessories.isEnabled()) {
                AccessoryRenderer.render(immediate, matrices, camera, modules.customAccessories);
            }
        });
        run("jumpCircles", () -> {
            if (modules.jumpCircles != null && modules.jumpCircles.isEnabled()) {
                JumpCircleRenderer.render(immediate, matrices, camera, modules.jumpCircles);
            }
        });
        run("hitParticles", () -> {
            if (modules.hitParticles != null && modules.hitParticles.isEnabled()) {
                HitParticleRenderer.render(immediate, matrices, camera, modules.hitParticles);
            }
        });

        // ── the base-finding half ────────────────────────────────────────────
        run("storageEsp", () -> {
            if (modules.storageEsp != null && modules.storageEsp.isEnabled()) {
                StorageEspRenderer.render(immediate, matrices, camera, modules.storageEsp);
            }
        });
        run("blockEsp", () -> {
            if (modules.blockEsp != null && modules.blockEsp.isEnabled()) {
                BlockEspRenderer.render(immediate, matrices, camera, modules.blockEsp);
            }
        });
        run("blockEntityEsp", () -> {
            if (modules.blockEntityEsp != null && modules.blockEntityEsp.isEnabled()) {
                BlockEntityEspRenderer.render(immediate, matrices, camera, modules.blockEntityEsp);
            }
        });
        run("chunkFinder", () -> {
            if (modules.chunkFinder != null && modules.chunkFinder.isEnabled()) {
                ChunkFinderRenderer.render(immediate, matrices, camera, modules.chunkFinder);
            }
        });
        run("susChunkFinder", () -> {
            if (modules.susChunkFinder != null && modules.susChunkFinder.isEnabled()) {
                SusChunkRenderer.render(immediate, matrices, camera, modules.susChunkFinder);
            }
        });
        run("holeEsp", () -> {
            if (modules.debugHoleEsp != null && modules.debugHoleEsp.isEnabled()) {
                HoleEspRenderer.render(immediate, matrices, camera, modules.debugHoleEsp);
            }
        });
        run("spawnerNametags", () -> {
            if (modules.spawnerNametags != null && modules.spawnerNametags.isEnabled()) {
                MiscBlockEspRenderer.renderSpawners(immediate, matrices, camera, modules.spawnerNametags);
            }
        });
        run("playerEsp", () -> {
            if (modules.playerEsp != null && modules.playerEsp.isEnabled()) {
                EntityEspRenderer.renderPlayers(immediate, matrices, camera, modules.playerEsp);
            }
        });
        run("mobEsp", () -> {
            if (modules.mobEsp != null && modules.mobEsp.isEnabled()) {
                EntityEspRenderer.renderMobs(immediate, matrices, camera, modules.mobEsp);
            }
        });

        // Anything still buffered when the frame ends is dropped, so draw now.
        run("flush", () -> FlatOverlay.flush(immediate));
    }

    private static void run(String name, Runnable body) {
        if (failed.contains(name)) return;
        try {
            body.run();
        } catch (Throwable error) {
            failed.add(name);
            SixSevenClient.LOGGER.error(
                    "World renderer '{}' failed; leaving it out for the rest of this session", name, error);
        }
    }
}
