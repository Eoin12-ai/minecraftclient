package dev.kryptic.render;

import dev.kryptic.module.visuals.BlockOutlineModule;
import dev.kryptic.util.Colors;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * The outline on the block you are looking at.
 *
 * This was a stub — the method body was a comment and nothing called it — so
 * the module has been sitting in the menu doing nothing. It draws now, against
 * the block's real collision shape rather than a plain cube, so a slab, a stair
 * or a fence outlines its actual silhouette the way vanilla's does.
 *
 * The animation runs off wall-clock time rather than tick count. A pulse tied
 * to ticks stutters whenever the server does, and this is a cosmetic that
 * should not tell you about your connection.
 */
public final class BlockOutlineRenderer {

    private BlockOutlineRenderer() {
    }

    public static void render(Immediate immediate, MatrixStack matrices, Vec3d camera,
                              BlockOutlineModule module) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.crosshairTarget == null) return;
        if (client.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) client.crosshairTarget).getBlockPos();
        BlockState state = client.world.getBlockState(pos);
        if (state.isAir()) return;

        List<Box> boxes = shapeOf(client, state, pos);
        if (boxes.isEmpty()) return;

        float phase = Surface.phase(2.4f / module.speedScale());
        int rgb = module.currentRgb();
        float energy = animationEnergy(module, phase);

        float lineAlpha = 0.35f + 0.65f * energy;
        float fill = module.fillOpacity.getFloat() / 100.0f;
        float glow = module.glowStrength();
        float width = module.thickness.getFloat();
        double grow = module.expansion();
        boolean corners = module.corners.get();

        // the outline sits a hair proud of the block, otherwise it fights the
        // block's own faces for depth and flickers as the camera moves
        for (Box box : boxes) {
            double x1 = pos.getX() + box.minX - grow, y1 = pos.getY() + box.minY - grow, z1 = pos.getZ() + box.minZ - grow;
            double x2 = pos.getX() + box.maxX + grow, y2 = pos.getY() + box.maxY + grow, z2 = pos.getZ() + box.maxZ + grow;

            if (fill > 0.001f) {
                int fillColour = Colors.withAlpha(rgb, fill * (0.6f + 0.4f * energy));
                FlatOverlay.box(immediate, matrices, camera, x1, y1, z1, x2, y2, z2, fillColour, 0, 1.0f);
                if (module.fillSides.get()) {
                    FlatOverlay.wall(immediate, matrices, camera, x1, z1, x2, z1, y1, y2, fillColour, fillColour);
                    FlatOverlay.wall(immediate, matrices, camera, x2, z1, x2, z2, y1, y2, fillColour, fillColour);
                    FlatOverlay.wall(immediate, matrices, camera, x2, z2, x1, z2, y1, y2, fillColour, fillColour);
                    FlatOverlay.wall(immediate, matrices, camera, x1, z2, x1, z1, y1, y2, fillColour, fillColour);
                }
            }

            // a wider, fainter pass under the core line reads as a glow without
            // needing a shader or a second render target
            if (glow > 0.01f) {
                cage(immediate, matrices, camera, x1, y1, z1, x2, y2, z2,
                        Colors.withAlpha(Colors.lighten(rgb, 0.45f), 0.16f * glow * energy),
                        width * 2.6f, corners);
            }
            cage(immediate, matrices, camera, x1, y1, z1, x2, y2, z2,
                    Colors.withAlpha(Colors.lighten(rgb, 0.25f), lineAlpha),
                    width, corners);
        }
    }

    /**
     * How bright the outline is this frame, 0..1.
     *
     * Gradient Flow rises and falls twice as often as Pulse and never dims all
     * the way, so it reads as something travelling rather than something
     * breathing.
     */
    private static float animationEnergy(BlockOutlineModule module, float phase) {
        String mode = module.animation.get();
        if (mode.equals("Static")) return 1.0f;
        if (mode.equals("Gradient Flow")) {
            return 0.45f + 0.55f * (float) Math.abs(Math.sin(phase * Math.PI * 2.0));
        }
        // Pulse: a soft breath, never all the way off
        return 0.5f + 0.5f * (float) Math.sin(phase * Math.PI * 2.0);
    }

    /**
     * The block's outline shape, falling back to a full cube.
     *
     * An empty outline shape is normal — plants and torches have none — and a
     * cube is a better answer there than drawing nothing at all when the player
     * is clearly pointing at something.
     */
    private static List<Box> shapeOf(MinecraftClient client, BlockState state, BlockPos pos) {
        try {
            List<Box> boxes = state.getOutlineShape(client.world, pos).getBoundingBoxes();
            if (!boxes.isEmpty()) return boxes;
        } catch (Throwable ignored) {
            // a block that refuses to describe itself still gets an outline
        }
        return List.of(new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
    }

    /**
     * The twelve edges of a box, or just the corners of each.
     *
     * Corner brackets leave the middle of every edge open, so the block face
     * stays readable while mining instead of being fenced in by a solid frame.
     */
    private static void cage(Immediate immediate, MatrixStack matrices, Vec3d camera,
                             double x1, double y1, double z1, double x2, double y2, double z2,
                             int colour, float width, boolean cornersOnly) {
        double cut = cornersOnly ? 0.28 : 0.5;
        edge(immediate, matrices, camera, x1, y1, z1, x2, y1, z1, colour, width, cut);
        edge(immediate, matrices, camera, x2, y1, z1, x2, y1, z2, colour, width, cut);
        edge(immediate, matrices, camera, x2, y1, z2, x1, y1, z2, colour, width, cut);
        edge(immediate, matrices, camera, x1, y1, z2, x1, y1, z1, colour, width, cut);

        edge(immediate, matrices, camera, x1, y2, z1, x2, y2, z1, colour, width, cut);
        edge(immediate, matrices, camera, x2, y2, z1, x2, y2, z2, colour, width, cut);
        edge(immediate, matrices, camera, x2, y2, z2, x1, y2, z2, colour, width, cut);
        edge(immediate, matrices, camera, x1, y2, z2, x1, y2, z1, colour, width, cut);

        edge(immediate, matrices, camera, x1, y1, z1, x1, y2, z1, colour, width, cut);
        edge(immediate, matrices, camera, x2, y1, z1, x2, y2, z1, colour, width, cut);
        edge(immediate, matrices, camera, x2, y1, z2, x2, y2, z2, colour, width, cut);
        edge(immediate, matrices, camera, x1, y1, z2, x1, y2, z2, colour, width, cut);
    }

    /** One edge, drawn as its two end stubs when {@code cut} is under a half. */
    private static void edge(Immediate immediate, MatrixStack matrices, Vec3d camera,
                             double x1, double y1, double z1, double x2, double y2, double z2,
                             int colour, float width, double cut) {
        if (cut >= 0.5) {
            thickLine(immediate, matrices, camera, x1, y1, z1, x2, y2, z2, colour, width);
            return;
        }
        double ax = x1 + (x2 - x1) * cut, ay = y1 + (y2 - y1) * cut, az = z1 + (z2 - z1) * cut;
        double bx = x2 - (x2 - x1) * cut, by = y2 - (y2 - y1) * cut, bz = z2 - (z2 - z1) * cut;
        thickLine(immediate, matrices, camera, x1, y1, z1, ax, ay, az, colour, width);
        thickLine(immediate, matrices, camera, bx, by, bz, x2, y2, z2, colour, width);
    }

    /**
     * A line drawn wide, as a small bundle of parallel lines.
     *
     * The line render layer sets its width once for the whole pipeline, so a
     * per-line thickness cannot come from the vertex data. Offsetting a few
     * copies in the two directions across the line gets there instead, and the
     * offset scales with camera distance so a thickness of 3 looks the same
     * from one block away as from ten.
     */
    private static void thickLine(Immediate immediate, MatrixStack matrices, Vec3d camera,
                                  double x1, double y1, double z1, double x2, double y2, double z2,
                                  int colour, float width) {
        FlatOverlay.line3d(immediate, matrices, camera, x1, y1, z1, x2, y2, z2, colour);
        if (width <= 1.25f) return;

        double midX = (x1 + x2) * 0.5, midY = (y1 + y2) * 0.5, midZ = (z1 + z2) * 0.5;
        double distance = Math.sqrt(camera.squaredDistanceTo(midX, midY, midZ));
        double step = 0.0016 * Math.max(1.0, distance);

        // the two axes the line does not run along; offsetting along the line
        // itself would only make it longer
        double dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), dz = Math.abs(z2 - z1);
        double ox = dx > dy && dx > dz ? 0.0 : 1.0;
        double oy = dy > dx && dy > dz ? 0.0 : 1.0;
        double oz = dz > dx && dz > dy ? 0.0 : 1.0;

        int passes = Math.min(5, (int) Math.round(width) - 1);
        for (int i = 1; i <= passes; i++) {
            double d = step * i;
            for (int sign = -1; sign <= 1; sign += 2) {
                FlatOverlay.line3d(immediate, matrices, camera,
                        x1 + ox * d * sign, y1 + oy * d * sign, z1 + oz * d * sign,
                        x2 + ox * d * sign, y2 + oy * d * sign, z2 + oz * d * sign, colour);
            }
        }
    }
}
