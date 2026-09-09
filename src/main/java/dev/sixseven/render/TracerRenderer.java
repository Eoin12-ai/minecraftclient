package dev.sixseven.render;

import dev.sixseven.module.render.TracersModule;
import dev.sixseven.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

/**
 * Lines from the camera out to entities worth knowing about.
 *
 * The line starts a little way in front of the camera rather than at the camera
 * itself: a line beginning exactly at the near plane is clipped into a smear
 * across the middle of the screen, whereas one starting a metre out converges
 * on the crosshair the way a tracer is supposed to.
 */
public final class TracerRenderer {

   /** How far in front of the camera the lines converge. */
   private static final double ORIGIN_DISTANCE = 1.0;

   private TracerRenderer() {
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d cameraPos,
                             TracersModule module) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity self = client.player;
      if (self == null || client.world == null) return;

      Camera camera = client.gameRenderer.getCamera();
      Vector3f facing = new Vector3f(camera.getHorizontalPlane());
      // camera-relative, so the origin is just the view direction scaled out
      float ox = facing.x * (float) ORIGIN_DISTANCE;
      float oy = facing.y * (float) ORIGIN_DISTANCE;
      float oz = facing.z * (float) ORIGIN_DISTANCE;

      VertexConsumer consumer = immediate.getBuffer(FlatOverlay.LINES);
      Entry entry = matrices.peek();

      double range = module.range.getFloat();
      double rangeSq = range * range;
      float tickDelta = client.getRenderTickCounter().getTickProgress(false);
      String target = module.target.get();
      boolean fade = module.fade.get();

      for (Entity entity : client.world.getEntities()) {
         if (entity == self) continue;

         int colour;
         if (entity instanceof PlayerEntity) {
            if (!module.players.get()) continue;
            colour = module.playerColor.get();
         } else if (entity instanceof ItemEntity) {
            if (!module.items.get()) continue;
            colour = module.itemColor.get();
         } else if (entity instanceof LivingEntity) {
            if (!module.mobs.get()) continue;
            colour = module.mobColor.get();
         } else {
            continue;
         }

         double distSq = entity.squaredDistanceTo(self);
         if (distSq > rangeSq) continue;

         // interpolated so the line does not lag a moving target by a tick
         double ex = MathHelper.lerp((double) tickDelta, entity.lastRenderX, entity.getX());
         double ey = MathHelper.lerp((double) tickDelta, entity.lastRenderY, entity.getY());
         double ez = MathHelper.lerp((double) tickDelta, entity.lastRenderZ, entity.getZ());

         float height = entity.getHeight();
         if (target.equals("Body")) {
            ey += height * 0.5;
         } else if (target.equals("Head")) {
            ey += height;
         }

         int drawn = colour;
         if (fade) {
            float alpha = 1.0f - (float) Math.min(1.0, Math.sqrt(distSq) / range);
            drawn = Colors.withAlpha(colour, Math.max(0.15f, alpha) * (Colors.alpha(colour) / 255.0f));
         }

         line(consumer, entry, ox, oy, oz,
              (float) (ex - cameraPos.x), (float) (ey - cameraPos.y), (float) (ez - cameraPos.z),
              drawn);
      }
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
