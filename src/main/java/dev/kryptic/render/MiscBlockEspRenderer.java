package dev.kryptic.render;

import dev.kryptic.module.render.SpawnerNametagsModule;
import dev.kryptic.util.Colors;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * The spawner marker: a beam of light falling out of the sky.
 *
 * The beam is two quads crossed at right angles rather than one quad turned to
 * face the camera. Crossed quads have no orientation to keep up to date, read
 * as a solid column from every angle including directly overhead, and cost the
 * same four triangles a billboard would.
 *
 * Each beam is drawn twice: a narrow bright core inside a wider, fainter
 * shell. That is what makes it read as light rather than as a red pole — a
 * single quad at any opacity looks like a painted plank.
 */
public final class MiscBlockEspRenderer {

   private static final double SPAWNER_RANGE = 16.0;
   private static final float TRACER_WIDTH = 1.2F;

   /** How much wider the outer shell is than the core. */
   private static final float SHELL_SCALE = 3.4F;

   private MiscBlockEspRenderer() {
   }

   public static void renderSpawners(Immediate immediate, MatrixStack matrices, Vec3d camera,
                                     SpawnerNametagsModule module) {
      List<BlockPos> found = module.scan.get();
      if (found.isEmpty()) return;

      boolean beam = module.beam.get();
      boolean ring = module.groundRing.get();
      boolean activation = module.rangeRing.get();
      boolean outline = module.box.get();
      boolean tracers = module.tracers.get();
      if (!beam && !ring && !activation && !outline && !tracers) return;

      Vec3d eye = tracers
            ? MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos()
            : null;

      int rgb = module.rgb();
      float energy = module.energy();

      for (BlockPos pos : found) {
         double cx = pos.getX() + 0.5;
         double cy = pos.getY();
         double cz = pos.getZ() + 0.5;

         if (beam) {
            drawBeam(immediate, matrices, camera, module, cx, cy, cz);
         }

         if (ring) {
            // a tight ring where the beam lands, so the exact block is readable
            // once you are close enough for the beam itself to be overhead
            EspBoxRenderer.ring(immediate, matrices, camera, cx, cy + 0.03, cz,
                  0.9, 24, Colors.withAlpha(rgb, 0.55f * energy), 2.0F);
         }

         if (outline) {
            EspBoxRenderer.outline(immediate, matrices, camera,
                  pos.getX(), pos.getY(), pos.getZ(),
                  pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                  Colors.withAlpha(rgb, 0.9f), 2.0F);
         }

         if (activation) {
            EspBoxRenderer.ring(immediate, matrices, camera, cx, cy + 0.5, cz,
                  SPAWNER_RANGE, 48, Colors.withAlpha(rgb, 0.35f), 2.0F);
         }

         if (tracers) {
            EspBoxRenderer.tracer(immediate, matrices, camera, eye, cx, cy + 0.5, cz,
                  Colors.withAlpha(rgb, 0.7f), TRACER_WIDTH);
         }
      }

      EspBoxRenderer.flush(immediate);
      FlatOverlay.flush(immediate);
   }

   /**
    * One beam: a bright core inside a fainter shell, both fading upward.
    *
    * The fade runs bottom-to-top rather than the other way about. A beacon
    * rises and is meant to be seen from far off; this one is meant to point at
    * a block, so it is brightest where the block is and thins out as it climbs.
    */
   private static void drawBeam(Immediate immediate, MatrixStack matrices, Vec3d camera,
                                SpawnerNametagsModule module,
                                double cx, double cy, double cz) {
      double top = cy + module.reach();
      float core = module.coreWidth();

      // the shell first, so the core draws over it rather than through it
      column(immediate, matrices, camera, cx, cz, cy, top, core * SHELL_SCALE,
            module.beamColour(0.22f), module.beamColour(0.0f));
      column(immediate, matrices, camera, cx, cz, cy, top, core,
            module.beamColour(1.0f), module.beamColour(0.05f));
   }

   /** Two quads crossed at right angles, so the column reads from any angle. */
   private static void column(Immediate immediate, MatrixStack matrices, Vec3d camera,
                              double cx, double cz, double yLow, double yHigh,
                              float halfWidth, int low, int high) {
      FlatOverlay.wall(immediate, matrices, camera,
            cx - halfWidth, cz, cx + halfWidth, cz, yLow, yHigh, low, high);
      FlatOverlay.wall(immediate, matrices, camera,
            cx, cz - halfWidth, cx, cz + halfWidth, yLow, yHigh, low, high);
   }
}
