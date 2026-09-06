package dev.sixseven.render;

import dev.sixseven.util.Colors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import net.minecraft.client.render.VertexFormat;

public final class FlatOverlay {

    // 1.21.5-compatible render layers using the stable RenderLayer API
    public static final RenderLayer FILL = RenderLayer.of(
        "sixsevenclient:flat_fill",
        VertexFormats.POSITION_COLOR,
        VertexFormat.DrawMode.QUADS,
        1536,
        RenderLayer.MultiPhaseParameters.builder()
            .program(RenderPhase.COLOR_PROGRAM)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
            .cull(RenderPhase.DISABLE_CULLING)
            .writeMaskState(RenderPhase.ALL_MASK)
            .build(false)
    );

    public static final RenderLayer LINES = RenderLayer.of(
        "sixsevenclient:flat_lines",
        VertexFormats.LINES,
        VertexFormat.DrawMode.LINES,
        1536,
        RenderLayer.MultiPhaseParameters.builder()
            .program(RenderPhase.RENDERTYPE_LINES_PROGRAM)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
            .writeMaskState(RenderPhase.COLOR_MASK)
            .build(false)
    );

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
            .color(color).normal(entry, normal).lineWidth(lineWidth);
        consumer.vertex(entry, (float)(x2 - vec.x), (float)(y - vec.y), (float)(z2 - vec.z))
            .color(color).normal(entry, normal).lineWidth(lineWidth);
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
}
