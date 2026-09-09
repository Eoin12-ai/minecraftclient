package dev.sixseven.module.render;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.ColorSetting;
import dev.sixseven.settings.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A line showing where you have been.
 *
 * This is not the cosmetic trail — that one is a few seconds long and exists to
 * look good. This is a navigation aid: it holds thousands of points, survives
 * for as long as you are in the world, and is meant for getting back out of a
 * cave system or a stash tunnel you have been branching around in for an hour.
 *
 * Points are recorded by distance rather than by time, so standing still does
 * not pile up thousands of identical points, and the path is dropped when the
 * dimension changes because the coordinates would not mean anything there.
 */
public class BreadcrumbsModule extends Module {

   public final SliderSetting spacing = this.addSetting(new SliderSetting(
         "Spacing", "How far you travel before another point is recorded", 1.0, 0.25, 5.0, 0.25, "m"));
   public final SliderSetting maxPoints = this.addSetting(new SliderSetting(
         "Max Points", "How much path to keep before the oldest is dropped", 2000.0, 100.0, 10000.0, 100.0, ""));
   public final SliderSetting eyeOffset = this.addSetting(new SliderSetting(
         "Height", "How far above your feet the line is drawn", 0.4, 0.0, 2.0, 0.1, "m"));
   public final BooleanSetting fade = this.addSetting(new BooleanSetting(
         "Fade", "Fade the oldest part of the path out", true));
   public final BooleanSetting clear = this.addSetting(new BooleanSetting(
         "Clear", "Switch on to wipe the path, then it switches back off", false));
   public final ColorSetting color = this.addSetting(new ColorSetting(
         "Color", "Path colour", 0xFF7CE0FF));

   private final Deque<Vec3d> points = new ArrayDeque<>();

   /** The dimension the current path belongs to; a change invalidates it. */
   private String dimension = "";

   public BreadcrumbsModule() {
      super("Breadcrumbs", "A trail of where you have walked, for finding your way back",
            Category.RENDER);
   }

   public Deque<Vec3d> points() {
      return this.points;
   }

   @Override
   public void onTick() {
      if (this.clear.get()) {
         this.points.clear();
         this.clear.set(Boolean.FALSE);
      }

      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      ClientWorld world = client.world;
      if (player == null || world == null) return;

      String key = world.getRegistryKey().getValue().toString();
      if (!key.equals(this.dimension)) {
         // coordinates from another dimension would draw a path through nothing
         this.points.clear();
         this.dimension = key;
      }

      Vec3d here = new Vec3d(player.getX(), player.getY(), player.getZ());
      Vec3d last = this.points.peekLast();
      double step = Math.max(0.25, this.spacing.getFloat());
      if (last == null || last.squaredDistanceTo(here) >= step * step) {
         this.points.addLast(here);
      }

      int cap = (int) this.maxPoints.getFloat();
      while (this.points.size() > cap) {
         this.points.removeFirst();
      }
   }

   @Override
   protected void onDisable() {
      this.points.clear();
   }
}
