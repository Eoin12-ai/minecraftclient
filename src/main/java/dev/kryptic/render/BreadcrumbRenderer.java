package dev.kryptic.render;

import dev.kryptic.theme.ThemeColors;
import dev.kryptic.module.render.BreadcrumbsModule;
import dev.kryptic.util.Colors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.Deque;

/** The recorded path, drawn as one continuous line. */
public final class BreadcrumbRenderer {

   private BreadcrumbRenderer() {
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d camera,
                             BreadcrumbsModule module) {
      Deque<Vec3d> points = module.points();
      if (points.size() < 2) return;

      VertexConsumer consumer = immediate.getBuffer(FlatOverlay.LINES);
      Entry entry = matrices.peek();

      int colour = ThemeColors.resolve(module.color.get());
      int baseAlpha = Colors.alpha(colour);
      boolean fade = module.fade.get();
      float lift = module.eyeOffset.getFloat();
      int total = points.size();

      Vec3d previous = null;
      int index = 0;
      for (Vec3d point : points) {
         if (previous != null) {
            int drawn = colour;
            if (fade) {
               // oldest end of the path is faintest, so the direction you came
               // from reads at a glance
               float age = (float) index / (float) total;
               drawn = Colors.withAlpha(colour, (0.15f + 0.85f * age) * (baseAlpha / 255.0f));
            }
            line(consumer, entry,
                 (float) (previous.x - camera.x), (float) (previous.y + lift - camera.y), (float) (previous.z - camera.z),
                 (float) (point.x - camera.x), (float) (point.y + lift - camera.y), (float) (point.z - camera.z),
                 drawn);
         }
         previous = point;
         index++;
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
