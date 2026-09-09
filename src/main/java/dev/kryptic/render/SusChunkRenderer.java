package dev.kryptic.render;

import dev.kryptic.KrypticClient;
import dev.kryptic.suschunk.SusChunkScanner;
import dev.kryptic.util.Colors;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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
      double d = susChunkFinderModule.renderY.get();
      int n = KrypticClient.themes().current().accent();
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
            drawQuad(immediate, matrices, vec, coord, currentScore, coord + 16.0, currentScore + 16.0, d, n, f * chunkFade.tier * chunkFade.alpha, ok ? f5 * chunkFade.alpha : 0.0F);
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
            drawQuad(
               immediate,
               matrices,
               vec,
               zoneFade.centerX - coord3,
               zoneFade.centerZ - coord3,
               zoneFade.centerX + coord3,
               zoneFade.centerZ + coord3,
               d,
               n,
               f * zoneFade.tier * zoneFade.alpha,
               ok ? f5 * zoneFade.alpha : 0.0F
            );
            if (susChunkFinderModule.centroidMarker.get()) {
               FlatOverlay.marker(immediate, matrices, vec, zoneFade.centerX, zoneFade.centerZ, d + 0.05, 2.0, Colors.withAlpha(n, 0.95F * zoneFade.alpha));
            }
         }
      }

      FlatOverlay.flush(immediate);
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
