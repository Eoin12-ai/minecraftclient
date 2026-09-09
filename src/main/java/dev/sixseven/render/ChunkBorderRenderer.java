package dev.sixseven.render;

import dev.sixseven.module.render.ChunkBordersModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

/**
 * The chunk grid.
 *
 * Everything is drawn as world-space lines relative to the camera, in a band
 * reaching a fixed distance above and below the player rather than the whole
 * world height — full-height lines turn into a wall of noise the moment the
 * grid radius goes past a chunk or two.
 */
public final class ChunkBorderRenderer {

   private static final int CHUNK = 16;
   private static final int REGION = 512;

   private ChunkBorderRenderer() {
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d camera,
                             ChunkBordersModule module) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (player == null) return;

      VertexConsumer consumer = immediate.getBuffer(FlatOverlay.LINES);
      Entry entry = matrices.peek();

      double span = module.span.getFloat();
      double lowY = player.getY() - span;
      double highY = player.getY() + span;

      int chunkX = Math.floorDiv((int) Math.floor(player.getX()), CHUNK);
      int chunkZ = Math.floorDiv((int) Math.floor(player.getZ()), CHUNK);

      if (module.grid.get()) {
         int radius = (int) module.radius.getFloat();
         int colour = module.chunkColor.get();
         int minX = (chunkX - radius) * CHUNK;
         int maxX = (chunkX + radius + 1) * CHUNK;
         int minZ = (chunkZ - radius) * CHUNK;
         int maxZ = (chunkZ + radius + 1) * CHUNK;

         for (int gx = chunkX - radius; gx <= chunkX + radius + 1; gx++) {
            double x = gx * CHUNK;
            flatLine(consumer, entry, camera, x, minZ, x, maxZ, lowY, colour);
            flatLine(consumer, entry, camera, x, minZ, x, maxZ, highY, colour);
         }
         for (int gz = chunkZ - radius; gz <= chunkZ + radius + 1; gz++) {
            double z = gz * CHUNK;
            flatLine(consumer, entry, camera, minX, z, maxX, z, lowY, colour);
            flatLine(consumer, entry, camera, minX, z, maxX, z, highY, colour);
         }
      }

      if (module.current.get()) {
         int colour = module.currentColor.get();
         double x0 = chunkX * CHUNK;
         double z0 = chunkZ * CHUNK;
         double x1 = x0 + CHUNK;
         double z1 = z0 + CHUNK;
         outline(consumer, entry, camera, x0, z0, x1, z1, lowY, colour);
         outline(consumer, entry, camera, x0, z0, x1, z1, highY, colour);
         if (module.verticals.get()) {
            upright(consumer, entry, camera, x0, z0, lowY, highY, colour);
            upright(consumer, entry, camera, x1, z0, lowY, highY, colour);
            upright(consumer, entry, camera, x1, z1, lowY, highY, colour);
            upright(consumer, entry, camera, x0, z1, lowY, highY, colour);
         }
      }

      if (module.regions.get()) {
         int colour = module.regionColor.get();
         int regionX = Math.floorDiv((int) Math.floor(player.getX()), REGION);
         int regionZ = Math.floorDiv((int) Math.floor(player.getZ()), REGION);
         double x0 = (double) regionX * REGION;
         double z0 = (double) regionZ * REGION;
         double x1 = x0 + REGION;
         double z1 = z0 + REGION;
         outline(consumer, entry, camera, x0, z0, x1, z1, lowY, colour);
         outline(consumer, entry, camera, x0, z0, x1, z1, highY, colour);
         upright(consumer, entry, camera, x0, z0, lowY, highY, colour);
         upright(consumer, entry, camera, x1, z0, lowY, highY, colour);
         upright(consumer, entry, camera, x1, z1, lowY, highY, colour);
         upright(consumer, entry, camera, x0, z1, lowY, highY, colour);
      }
   }

   private static void outline(VertexConsumer consumer, Entry entry, Vec3d camera,
                               double x0, double z0, double x1, double z1, double y, int colour) {
      flatLine(consumer, entry, camera, x0, z0, x1, z0, y, colour);
      flatLine(consumer, entry, camera, x1, z0, x1, z1, y, colour);
      flatLine(consumer, entry, camera, x1, z1, x0, z1, y, colour);
      flatLine(consumer, entry, camera, x0, z1, x0, z0, y, colour);
   }

   private static void flatLine(VertexConsumer consumer, Entry entry, Vec3d camera,
                                double x0, double z0, double x1, double z1, double y, int colour) {
      line(consumer, entry,
           (float) (x0 - camera.x), (float) (y - camera.y), (float) (z0 - camera.z),
           (float) (x1 - camera.x), (float) (y - camera.y), (float) (z1 - camera.z),
           colour);
   }

   private static void upright(VertexConsumer consumer, Entry entry, Vec3d camera,
                               double x, double z, double lowY, double highY, int colour) {
      line(consumer, entry,
           (float) (x - camera.x), (float) (lowY - camera.y), (float) (z - camera.z),
           (float) (x - camera.x), (float) (highY - camera.y), (float) (z - camera.z),
           colour);
   }

   private static void line(VertexConsumer consumer, Entry entry,
                            float x0, float y0, float z0, float x1, float y1, float z1, int colour) {
      Vector3f normal = new Vector3f(x1 - x0, y1 - y0, z1 - z0);
      if (normal.lengthSquared() > 1.0e-9f) {
         normal.normalize();
      } else {
         normal.set(0.0f, 1.0f, 0.0f);
      }
      consumer.vertex(entry, x0, y0, z0).color(colour).normal(entry, normal);
      consumer.vertex(entry, x1, y1, z1).color(colour).normal(entry, normal);
   }
}
