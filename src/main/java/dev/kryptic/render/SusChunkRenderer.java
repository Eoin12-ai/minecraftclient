package dev.kryptic.render;

import dev.kryptic.KrypticClient;
import dev.kryptic.suschunk.SusChunkScanner;
import dev.kryptic.util.Colors;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import dev.kryptic.module.render.SusChunkFinderModule;
public final class SusChunkRenderer {
   private static final float FADE_IN_SECONDS = 0.3F;
   private static final float FADE_OUT_SECONDS = 0.45F;
   private static final float MOVE_RATE = 7.0F;
   private static final Map<Long, SusChunkRenderer.ChunkFade> chunkFades = new HashMap<>();
   private static final Map<Long, SusChunkRenderer.ZoneFade> zoneFades = new HashMap<>();
   private static long lastFrameNanos;

   private SusChunkRenderer() {
   }

   public static void reset() {
      chunkFades.clear();
      zoneFades.clear();
   }

   public static String debugState() {
      StringBuilder sb = new StringBuilder();

      for (Entry entry : chunkFades.entrySet()) {
         SusChunkRenderer.ChunkFade chunkFade = (SusChunkRenderer.ChunkFade)entry.getValue();
         sb.append(new ChunkPos((Long)entry.getKey()))
            .append("{a=")
            .append(chunkFade.alpha)
            .append("_Xu")
            .append(chunkFade.tier)
            .append("_Ju")
            .append(chunkFade.flagged)
            .append("} ");
      }

      for (Entry entry2 : zoneFades.entrySet()) {
         SusChunkRenderer.ZoneFade zoneFade = (SusChunkRenderer.ZoneFade)entry2.getValue();
         sb.append("zone")
            .append(new ChunkPos((Long)entry2.getKey()))
            .append("{a=")
            .append(zoneFade.alpha)
            .append("__u")
            .append(zoneFade.size)
            .append("_Ju")
            .append(zoneFade.flagged)
            .append("} ");
      }

      return sb.toString();
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d vec, SusChunkFinderModule susChunkFinderModule) {
      SusChunkScanner susChunkScanner = susChunkFinderModule.scanner;
      double d = anchorY(susChunkFinderModule);
      int n = markerColour(susChunkFinderModule);
      float f = susChunkFinderModule.fillOpacity.getFloat() / 255.0F;
      float f5 = susChunkFinderModule.outlineOpacity.getFloat() / 255.0F;
      boolean ok = susChunkFinderModule.outline.get();
      boolean ok2 = susChunkFinderModule.smartMode.get();
      int offset = susChunkScanner.threshold();
      updateTargets(susChunkScanner, ok2, offset);
      float f6 = frameDelta();
      float f7 = 1.0F - (float)Math.exp((double)(-f6 * 7.0F));
      Iterator it = chunkFades.entrySet().iterator();

      while (it.hasNext()) {
         Entry entry = (Entry)it.next();
         SusChunkRenderer.ChunkFade chunkFade = (SusChunkRenderer.ChunkFade)entry.getValue();
         chunkFade.alpha = chunkFade.alpha + (chunkFade.flagged ? f6 / 0.3F : -f6 / 0.45F);
         chunkFade.alpha = Math.clamp(chunkFade.alpha, 0.0F, 1.0F);
         if (!chunkFade.flagged && chunkFade.alpha <= 0.0F) {
            it.remove();
         } else {
            double coord = (double)ChunkPos.getPackedX((Long)entry.getKey()) * 16.0;
            double currentScore = (double)ChunkPos.getPackedZ((Long)entry.getKey()) * 16.0;
            drawMarker(immediate, matrices, vec, susChunkFinderModule,
                  coord, currentScore, coord + 16.0, currentScore + 16.0, d, n,
                  f * chunkFade.tier * chunkFade.alpha,
                  ok ? f5 * chunkFade.alpha : 0.0F,
                  chunkFade.alpha, chunkFade.tier);
         }
      }

      Iterator it2 = zoneFades.entrySet().iterator();

      while (it2.hasNext()) {
         SusChunkRenderer.ZoneFade zoneFade = (SusChunkRenderer.ZoneFade)((Entry)it2.next()).getValue();
         zoneFade.alpha = zoneFade.alpha + (zoneFade.flagged ? f6 / 0.3F : -f6 / 0.45F);
         zoneFade.alpha = Math.clamp(zoneFade.alpha, 0.0F, 1.0F);
         if (!zoneFade.flagged && zoneFade.alpha <= 0.0F) {
            it2.remove();
         } else {
            zoneFade.centerX = zoneFade.centerX + (zoneFade.targetX - zoneFade.centerX) * (double)f7;
            zoneFade.centerZ = zoneFade.centerZ + (zoneFade.targetZ - zoneFade.centerZ) * (double)f7;
            zoneFade.size = zoneFade.size + (zoneFade.targetSize - zoneFade.size) * f7;
            double coord3 = (double)zoneFade.size / 2.0;
            drawMarker(
               immediate, matrices, vec, susChunkFinderModule,
               zoneFade.centerX - coord3, zoneFade.centerZ - coord3,
               zoneFade.centerX + coord3, zoneFade.centerZ + coord3,
               d, n,
               f * zoneFade.tier * zoneFade.alpha,
               ok ? f5 * zoneFade.alpha : 0.0F,
               zoneFade.alpha, zoneFade.tier
            );
            if (susChunkFinderModule.centroidMarker.get()) {
               FlatOverlay.marker(immediate, matrices, vec, zoneFade.centerX, zoneFade.centerZ, d + 0.05, 2.0, Colors.withAlpha(n, 0.95F * zoneFade.alpha));
            }
         }
      }

      if (susChunkFinderModule.cellEsp.get()) {
         drawCells(immediate, matrices, vec, susChunkScanner, susChunkFinderModule);
      }

      FlatOverlay.flush(immediate);
   }

   /**
    * Boxes the individual amethyst cells behind a flag.
    *
    * The chunk marker tells you where to go; this tells you what it found once
    * you are there. It reads the cells the scan already collected -- no second
    * pass over the world, and nothing here influences whether a chunk flags.
    *
    * Cells are drawn at their real block position rather than at the marker
    * height, so they sit on the thing itself.
    */
   private static void drawCells(
      Immediate immediate, MatrixStack matrices, Vec3d camera,
      SusChunkScanner scanner, SusChunkFinderModule module
   ) {
      java.util.List<net.minecraft.util.math.BlockPos> cells = scanner.amethystCells();
      if (cells.isEmpty()) return;

      int base = module.cellColor.get() & 0xFFFFFF;
      int fill = Colors.withAlpha(base, 0.28F);
      int line = Colors.withAlpha(Colors.lighten(base, 0.35F), 0.9F);

      for (net.minecraft.util.math.BlockPos pos : cells) {
         FlatOverlay.box(immediate, matrices, camera,
               pos.getX(), pos.getY(), pos.getZ(),
               pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0,
               fill, line, 1.5F);
      }
   }

   /**
    * The colour every marker is drawn in.
    *
    * This used to be the theme accent with no way to change it, which meant a
    * flagged chunk was the same colour as the HUD, the menu and every other
    * highlight on screen. Following the theme is still available, but it is
    * no longer the only option.
    */
   private static int markerColour(SusChunkFinderModule module) {
      if (module.themeColor.get()) {
         return KrypticClient.themes().current().accent();
      }
      return module.color.get() & 0xFFFFFF;
   }

   /**
    * The height the marker is anchored at.
    *
    * A fixed altitude is fine on the surface and useless underground: at Y=100
    * every marker sits far overhead while you are in a cave at Y=-20. Tracking
    * the player keeps the layer where it can actually be seen, whatever depth
    * the search takes you to.
    */
   private static double anchorY(SusChunkFinderModule module) {
      if (!module.followPlayer.get()) {
         return module.renderY.get();
      }
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      double base = player == null ? module.renderY.get() : player.getY();
      return base + module.layerOffset.getFloat();
   }

   /**
    * Draws one flagged area in whichever style is selected.
    *
    * A flat tile on the ground is invisible edge-on, which is the exact
    * situation you are in whenever a candidate is far away and slightly below
    * you -- so the default now stands up off the ground instead.
    *
    * {@code confidence} is how far past the threshold the evidence went; the
    * upright styles spend it on height, so a stronger candidate is taller as
    * well as brighter and can be picked out of a field of weak ones.
    */
   private static void drawMarker(
      Immediate immediate, MatrixStack matrices, Vec3d camera, SusChunkFinderModule module,
      double x1, double z1, double x2, double z2, double y, int colour,
      float fillAlpha, float outlineAlpha, float fade, float confidence
   ) {
      String style = module.style.get();
      double tall = module.height.getFloat() * (0.55 + 0.45 * confidence);

      switch (style) {
         case "Layer" -> drawLayer(immediate, matrices, camera, x1, z1, x2, z2, y,
               module.layerThickness.getFloat(), colour, fillAlpha, fade);
         case "Cage" -> drawCage(immediate, matrices, camera, x1, z1, x2, z2, y, tall,
               colour, fillAlpha, outlineAlpha);
         case "Corners" -> drawCorners(immediate, matrices, camera, x1, z1, x2, z2, y, tall,
               colour, Math.max(outlineAlpha, fade * 0.85F));
         case "Pulse" -> drawPulse(immediate, matrices, camera, x1, z1, x2, z2, y,
               colour, fillAlpha, fade);
         case "Flat" -> drawQuad(immediate, matrices, camera, x1, z1, x2, z2, y, colour,
               fillAlpha, outlineAlpha);
         default -> drawBeam(immediate, matrices, camera, x1, z1, x2, z2, y, tall,
               colour, fillAlpha, fade);   // "Beam"
      }
   }

   /**
    * A slab floating over the chunk.
    *
    * Given real thickness rather than drawn as a single plane: a
    * zero-thickness quad disappears completely when your eye is level with it,
    * and a marker that vanishes at one specific pitch is worse than one that
    * is merely faint. The sides are what you see from level, the faces from
    * above and below.
    *
    * The top face is brighter than the bottom so the slab reads as lit from
    * above and sits in the world rather than floating on the screen.
    */
   private static void drawLayer(
      Immediate immediate, MatrixStack matrices, Vec3d camera,
      double x1, double z1, double x2, double z2, double y, double thickness,
      int colour, float fillAlpha, float fade
   ) {
      double half = Math.max(0.05, thickness / 2.0);
      double lo = y - half;
      double hi = y + half;

      int top = Colors.withAlpha(Colors.lighten(colour, 0.30F), fillAlpha);
      int bottom = Colors.withAlpha(colour, fillAlpha * 0.75F);
      int side = Colors.withAlpha(Colors.lighten(colour, 0.12F), Math.min(1.0F, fillAlpha * 1.6F));

      FlatOverlay.fillQuad(immediate, matrices, camera, x1, z1, x2, z2, hi, top);
      FlatOverlay.fillQuad(immediate, matrices, camera, x1, z1, x2, z2, lo, bottom);

      FlatOverlay.wall(immediate, matrices, camera, x1, z1, x2, z1, lo, hi, side, side);
      FlatOverlay.wall(immediate, matrices, camera, x2, z1, x2, z2, lo, hi, side, side);
      FlatOverlay.wall(immediate, matrices, camera, x2, z2, x1, z2, lo, hi, side, side);
      FlatOverlay.wall(immediate, matrices, camera, x1, z2, x1, z1, lo, hi, side, side);

      // a crisp edge along the top so the outline of the chunk stays legible
      // once several slabs overlap
      int edge = Colors.withAlpha(Colors.lighten(colour, 0.55F), fade * 0.9F);
      outlineFloor(immediate, matrices, camera, x1, z1, x2, z2, hi + 0.01, edge, 2.5F);
   }

   /**
    * A column of light rising out of the chunk, brightest at the base and
    * fading out at the top. Narrower than the chunk so a cluster of flags
    * reads as several separate beams rather than one solid wall.
    */
   private static void drawBeam(
      Immediate immediate, MatrixStack matrices, Vec3d camera,
      double x1, double z1, double x2, double z2, double y, double tall,
      int colour, float fillAlpha, float fade
   ) {
      double cx = (x1 + x2) / 2.0;
      double cz = (z1 + z2) / 2.0;
      double half = Math.max(1.5, Math.min(x2 - x1, z2 - z1) * 0.18);

      int low = Colors.withAlpha(Colors.lighten(colour, 0.25F), Math.min(1.0F, fillAlpha * 3.2F));
      int high = Colors.withAlpha(colour, 0.0F);

      // two crossed walls rather than four sides: a hollow box seen from
      // outside shows only its far face, which halves the apparent brightness
      FlatOverlay.wall(immediate, matrices, camera, cx - half, cz, cx + half, cz,
            y, y + tall, low, high);
      FlatOverlay.wall(immediate, matrices, camera, cx, cz - half, cx, cz + half,
            y, y + tall, low, high);

      // a footprint so the beam is anchored to something on the ground
      FlatOverlay.fillQuad(immediate, matrices, camera, x1, z1, x2, z2, y + 0.02,
            Colors.withAlpha(colour, fillAlpha * 0.8F));
      int edge = Colors.withAlpha(Colors.lighten(colour, 0.4F), fade * 0.9F);
      outlineFloor(immediate, matrices, camera, x1, z1, x2, z2, y + 0.03, edge, 2.5F);
   }

   /** The chunk as a wireframe volume: floor, ceiling and four uprights. */
   private static void drawCage(
      Immediate immediate, MatrixStack matrices, Vec3d camera,
      double x1, double z1, double x2, double z2, double y, double tall,
      int colour, float fillAlpha, float outlineAlpha
   ) {
      int edge = Colors.withAlpha(colour, Math.max(outlineAlpha, 0.25F));
      double top = y + tall;

      FlatOverlay.fillQuad(immediate, matrices, camera, x1, z1, x2, z2, y + 0.02,
            Colors.withAlpha(colour, fillAlpha));
      outlineFloor(immediate, matrices, camera, x1, z1, x2, z2, y, edge, 2.5F);
      outlineFloor(immediate, matrices, camera, x1, z1, x2, z2, top, edge, 2.0F);
      FlatOverlay.upright(immediate, matrices, camera, x1, z1, y, top, edge);
      FlatOverlay.upright(immediate, matrices, camera, x2, z1, y, top, edge);
      FlatOverlay.upright(immediate, matrices, camera, x2, z2, y, top, edge);
      FlatOverlay.upright(immediate, matrices, camera, x1, z2, y, top, edge);
   }

   /**
    * Just the corners -- an L bracket on the ground at each one plus a short
    * upright. Least ink on screen, which matters once a scan has flagged
    * thirty chunks at once and a solid fill would hide the terrain.
    */
   private static void drawCorners(
      Immediate immediate, MatrixStack matrices, Vec3d camera,
      double x1, double z1, double x2, double z2, double y, double tall,
      int colour, float alpha
   ) {
      if (alpha <= 0.02F) return;
      int edge = Colors.withAlpha(Colors.lighten(colour, 0.3F), alpha);
      double arm = Math.min(4.0, (x2 - x1) * 0.28);
      double tick = Math.min(tall, 6.0 + tall * 0.08);

      double[][] corners = {
         {x1, z1,  1,  1}, {x2, z1, -1,  1},
         {x2, z2, -1, -1}, {x1, z2,  1, -1},
      };
      for (double[] c : corners) {
         double cx = c[0], cz = c[1], sx = c[2], sz = c[3];
         FlatOverlay.edge(immediate, matrices, camera, cx, cz, cx + arm * sx, cz, y, edge, 3.0F);
         FlatOverlay.edge(immediate, matrices, camera, cx, cz, cx, cz + arm * sz, y, edge, 3.0F);
         FlatOverlay.upright(immediate, matrices, camera, cx, cz, y, y + tick, edge);
      }
   }

   /**
    * A ring travelling outward from the centre, on a loop. Motion is the one
    * thing that reads at the edge of vision, so this is the style to use when
    * you are sweeping terrain rather than studying one spot.
    */
   private static void drawPulse(
      Immediate immediate, MatrixStack matrices, Vec3d camera,
      double x1, double z1, double x2, double z2, double y,
      int colour, float fillAlpha, float fade
   ) {
      double cx = (x1 + x2) / 2.0;
      double cz = (z1 + z2) / 2.0;
      double maxR = Math.max(x2 - x1, z2 - z1) * 0.72;

      FlatOverlay.fillQuad(immediate, matrices, camera, x1, z1, x2, z2, y + 0.02,
            Colors.withAlpha(colour, fillAlpha * 0.55F));

      float phase = (float)((System.nanoTime() % 2_000_000_000L) / 2.0e9);
      for (int ring = 0; ring < 2; ring++) {
         float t = (phase + ring * 0.5F) % 1.0F;
         float ringAlpha = (1.0F - t) * fade * 0.95F;
         if (ringAlpha <= 0.02F) continue;
         double r = maxR * t;
         int edge = Colors.withAlpha(Colors.lighten(colour, 0.45F), ringAlpha);
         ring(immediate, matrices, camera, cx, cz, r, y + 0.05, edge);
      }
   }

   private static void ring(
      Immediate immediate, MatrixStack matrices, Vec3d camera,
      double cx, double cz, double radius, double y, int colour
   ) {
      int segments = 28;
      double px = cx + radius, pz = cz;
      for (int i = 1; i <= segments; i++) {
         double a = i / (double)segments * Math.PI * 2.0;
         double nx = cx + Math.cos(a) * radius;
         double nz = cz + Math.sin(a) * radius;
         FlatOverlay.edge(immediate, matrices, camera, px, pz, nx, nz, y, colour, 2.0F);
         px = nx;
         pz = nz;
      }
   }

   private static void outlineFloor(
      Immediate immediate, MatrixStack matrices, Vec3d camera,
      double x1, double z1, double x2, double z2, double y, int colour, float width
   ) {
      FlatOverlay.edge(immediate, matrices, camera, x1, z1, x2, z1, y, colour, width);
      FlatOverlay.edge(immediate, matrices, camera, x2, z1, x2, z2, y, colour, width);
      FlatOverlay.edge(immediate, matrices, camera, x2, z2, x1, z2, y, colour, width);
      FlatOverlay.edge(immediate, matrices, camera, x1, z2, x1, z1, y, colour, width);
   }

   private static void drawQuad(
      Immediate immediate, MatrixStack matrices, Vec3d vec, double d, double coord, double currentScore, double coord3, double coord4, int n, float tickDelta, float tickDelta2
   ) {
      FlatOverlay.fillQuad(immediate, matrices, vec, d, coord, currentScore, coord3, coord4, Colors.withAlpha(n, tickDelta));
      if (tickDelta2 > 0.004F) {
         int offset = Colors.withAlpha(n, tickDelta2);
         FlatOverlay.edge(immediate, matrices, vec, d, coord, currentScore, coord, coord4, offset, 2.5F);
         FlatOverlay.edge(immediate, matrices, vec, d, coord3, currentScore, coord3, coord4, offset, 2.5F);
         FlatOverlay.edge(immediate, matrices, vec, d, coord, d, coord3, coord4, offset, 2.5F);
         FlatOverlay.edge(immediate, matrices, vec, currentScore, coord, currentScore, coord3, coord4, offset, 2.5F);
      }
   }

   private static void updateTargets(SusChunkScanner susChunkScanner, boolean value, int n) {
      for (SusChunkRenderer.ChunkFade chunkFade3 : chunkFades.values()) {
         chunkFade3.flagged = false;
      }

      for (SusChunkRenderer.ZoneFade zoneFade2 : zoneFades.values()) {
         zoneFade2.flagged = false;
      }

      if (value) {
         for (SusChunkScanner.Zone zone : susChunkScanner.zones()) {
            if (zone.members().size() == 1) {
               long l = zone.members().iterator().next();
               SusChunkRenderer.ChunkFade chunkFade = chunkFades.computeIfAbsent(l, arg -> new SusChunkRenderer.ChunkFade());
               chunkFade.flagged = true;
               chunkFade.tier = confidence(zone.maxScore(), n);
            } else {
               long l4 = Long.MAX_VALUE;

               for (long l3 : zone.members()) {
                  l4 = Math.min(l4, l3);
               }

               SusChunkRenderer.ZoneFade zoneFade = zoneFades.get(l4);
               if (zoneFade == null) {
                  zoneFade = new SusChunkRenderer.ZoneFade(zone.centroidX(), zone.centroidZ());
                  zoneFades.put(l4, zoneFade);
               }

               zoneFade.flagged = true;
               zoneFade.targetX = zone.centroidX();
               zoneFade.targetZ = zone.centroidZ();
               zoneFade.targetSize = Math.min(48.0F, 16.0F + (float)(zone.members().size() - 1) * 8.0F);
               zoneFade.tier = confidence(zone.maxScore(), n);
            }
         }
      } else {
         for (SusChunkScanner.Flag item : susChunkScanner.flags()) {
            SusChunkRenderer.ChunkFade chunkFade2 = chunkFades.computeIfAbsent(item.chunkKey(), arg -> new SusChunkRenderer.ChunkFade());
            chunkFade2.flagged = true;
            chunkFade2.tier = 1.0F;
         }
      }
   }

   private static float confidence(double d, int n) {
      if (n <= 0) {
         return 1.0F;
      } else {
         float f = (float)((d - (double)n) / ((double)n * 2.0));
         return 0.55F + 0.45F * Math.clamp(f, 0.0F, 1.0F);
      }
   }

   private static float frameDelta() {
      long l = System.nanoTime();
      float f = lastFrameNanos == 0L ? 0.016F : (float)(l - lastFrameNanos) / 1.0E9F;
      lastFrameNanos = l;
      return Math.min(f, 0.1F);
   }

   private static final class ChunkFade {
      float alpha;
      float tier = 1.0F;
      boolean flagged;
   }

   private static final class ZoneFade {
      double centerX;
      double centerZ;
      double targetX;
      double targetZ;
      float size = 16.0F;
      float targetSize = 16.0F;
      float alpha;
      float tier = 1.0F;
      boolean flagged;

      ZoneFade(double d, double d2) {
         this.centerX = this.targetX = d;
         this.centerZ = this.targetZ = d2;
      }
   }
}
