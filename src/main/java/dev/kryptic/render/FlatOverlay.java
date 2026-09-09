package dev.kryptic.render;

import dev.kryptic.util.Colors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public final class FlatOverlay {

    // Use existing MC RenderLayer presets — no custom pipeline needed
    public static final RenderLayer FILL  = RenderLayers.debugQuads();
    public static final RenderLayer LINES = RenderLayers.lines();

    private FlatOverlay() {}

    public static void fillQuad(Immediate immediate, MatrixStack matrices, Vec3d vec,
                                 double x1, double z1, double x2, double z2, double y, int color) {
        VertexConsumer consumer = immediate.getBuffer(FILL);
        Entry entry = matrices.peek();
        float fx1 = (float)(x1 - vec.x), fz1 = (float)(z1 - vec.z);
        float fx2 = (float)(x2 - vec.x), fz2 = (float)(z2 - vec.z);
        float fy  = (float)(y  - vec.y);
        consumer.vertex(entry, fx1, fy, fz1).color(color);
        consumer.vertex(entry, fx1, fy, fz2).color(color);
        consumer.vertex(entry, fx2, fy, fz2).color(color);
        consumer.vertex(entry, fx2, fy, fz1).color(color);
    }

    public static void edge(Immediate immediate, MatrixStack matrices, Vec3d vec,
                             double x1, double z1, double x2, double z2, double y, int color, float lineWidth) {
        VertexConsumer consumer = immediate.getBuffer(LINES);
        Entry entry = matrices.peek();
        Vector3f normal = new Vector3f((float)(x2 - x1), 0.0f, (float)(z2 - z1)).normalize();
        consumer.vertex(entry, (float)(x1 - vec.x), (float)(y - vec.y), (float)(z1 - vec.z))
            .color(color).normal(entry, normal);
        consumer.vertex(entry, (float)(x2 - vec.x), (float)(y - vec.y), (float)(z2 - vec.z))
            .color(color).normal(entry, normal);
    }

    public static void box(Immediate immediate, MatrixStack matrices, Vec3d camera,
                            double x1, double y1, double z1, double x2, double y2, double z2,
                            int fill, int line, float lineWidth) {
        if (Colors.alpha(fill) > 0) {
            fillQuad(immediate, matrices, camera, x1, z1, x2, z2, y1, fill);
            fillQuad(immediate, matrices, camera, x1, z1, x2, z2, y2, fill);
        }
        if (Colors.alpha(line) > 0) {
            edge(immediate, matrices, camera, x1, z1, x2, z1, y1, line, lineWidth);
            edge(immediate, matrices, camera, x2, z1, x2, z2, y1, line, lineWidth);
            edge(immediate, matrices, camera, x2, z2, x1, z2, y1, line, lineWidth);
            edge(immediate, matrices, camera, x1, z2, x1, z1, y1, line, lineWidth);
            edge(immediate, matrices, camera, x1, z1, x2, z1, y2, line, lineWidth);
            edge(immediate, matrices, camera, x2, z1, x2, z2, y2, line, lineWidth);
            edge(immediate, matrices, camera, x2, z2, x1, z2, y2, line, lineWidth);
            edge(immediate, matrices, camera, x1, z2, x1, z1, y2, line, lineWidth);
        }
    }

    /**
     * A vertical quad spanning two heights, for columns and cages.
     *
     * The horizontal fill above is invisible edge-on, which is exactly what
     * happens to a marker on the ground once you are any distance from it.
     * Walls stay readable from every angle.
     */
    public static void wall(Immediate immediate, MatrixStack matrices, Vec3d camera,
                            double x1, double z1, double x2, double z2,
                            double yLow, double yHigh, int colorLow, int colorHigh) {
        VertexConsumer consumer = immediate.getBuffer(FILL);
        Entry entry = matrices.peek();
        float fx1 = (float)(x1 - camera.x), fz1 = (float)(z1 - camera.z);
        float fx2 = (float)(x2 - camera.x), fz2 = (float)(z2 - camera.z);
        float lo = (float)(yLow - camera.y), hi = (float)(yHigh - camera.y);
        consumer.vertex(entry, fx1, lo, fz1).color(colorLow);
        consumer.vertex(entry, fx2, lo, fz2).color(colorLow);
        consumer.vertex(entry, fx2, hi, fz2).color(colorHigh);
        consumer.vertex(entry, fx1, hi, fz1).color(colorHigh);
    }

    /** A vertical line, for the uprights on a cage or a corner bracket. */
    public static void upright(Immediate immediate, MatrixStack matrices, Vec3d camera,
                               double x, double z, double yLow, double yHigh, int color) {
        VertexConsumer consumer = immediate.getBuffer(LINES);
        Entry entry = matrices.peek();
        Vector3f normal = new Vector3f(0.0f, 1.0f, 0.0f);
        consumer.vertex(entry, (float)(x - camera.x), (float)(yLow - camera.y), (float)(z - camera.z))
            .color(color).normal(entry, normal);
        consumer.vertex(entry, (float)(x - camera.x), (float)(yHigh - camera.y), (float)(z - camera.z))
            .color(color).normal(entry, normal);
    }

    /**
     * A line between two arbitrary points.
     *
     * {@link #edge} is flat and {@link #upright} is vertical; a block outline
     * needs neither restriction, because a slab or a stair edge can run in any
     * direction once the collision shape is the thing being traced.
     *
     * A zero-length segment is skipped rather than normalised: normalising it
     * yields NaN, and a NaN normal poisons the whole buffered layer, not just
     * the one line.
     */
    public static void line3d(Immediate immediate, MatrixStack matrices, Vec3d camera,
                              double x1, double y1, double z1,
                              double x2, double y2, double z2, int color) {
        float dx = (float)(x2 - x1), dy = (float)(y2 - y1), dz = (float)(z2 - z1);
        if (dx * dx + dy * dy + dz * dz < 1.0e-9f) return;

        VertexConsumer consumer = immediate.getBuffer(LINES);
        Entry entry = matrices.peek();
        Vector3f normal = new Vector3f(dx, dy, dz).normalize();
        consumer.vertex(entry, (float)(x1 - camera.x), (float)(y1 - camera.y), (float)(z1 - camera.z))
            .color(color).normal(entry, normal);
        consumer.vertex(entry, (float)(x2 - camera.x), (float)(y2 - camera.y), (float)(z2 - camera.z))
            .color(color).normal(entry, normal);
    }

    /** A small diamond laid flat on the ground, used to pin a point on the map. */
    public static void marker(Immediate immediate, MatrixStack matrices, Vec3d camera,
                              double x, double z, double y, double size, int color) {
        VertexConsumer consumer = immediate.getBuffer(FILL);
        Entry entry = matrices.peek();
        float fx = (float)(x - camera.x), fz = (float)(z - camera.z), fy = (float)(y - camera.y);
        float r = (float) size;
        consumer.vertex(entry, fx,     fy, fz - r).color(color);
        consumer.vertex(entry, fx - r, fy, fz    ).color(color);
        consumer.vertex(entry, fx,     fy, fz + r).color(color);
        consumer.vertex(entry, fx + r, fy, fz    ).color(color);
    }

    /**
     * Draws what has been buffered into this overlay's two layers.
     *
     * Both layers have to be drawn explicitly: the world renderer flushes its
     * own layers on its own schedule, and anything still buffered when the
     * frame ends is simply dropped.
     */
    public static void flush(Immediate immediate) {
        immediate.draw(FILL);
        immediate.draw(LINES);
    }
}