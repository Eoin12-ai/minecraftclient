package dev.kryptic.render;

import dev.kryptic.module.render.ItemEspModule;
import dev.kryptic.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3fc;

/**
 * Draws the markers for {@link ItemEspModule}.
 *
 * A dropped item's own bounding box is about a quarter of a block and sits on
 * the floor, so an outline drawn on it exactly is nearly invisible from any
 * distance — which is the whole point of an ESP. The box is inflated to a
 * readable minimum and lifted clear of the ground instead.
 */
public final class ItemEspRenderer {

   /** Smallest a marker may be drawn, in blocks. Below this it stops reading. */
   private static final double MIN_SIZE = 0.45;

   /** How far the beacon runs above a rare drop. */
   private static final double BEAM_HEIGHT = 48.0;

   private static final float LINE_WIDTH = 2.0F;
   private static final float TRACER_WIDTH = 1.15F;

   /** Beyond this many markers in one frame, stop — the rest would be a smear. */
   private static final int MAX_MARKERS = 400;

   private ItemEspRenderer() {
   }

   public static void render(Immediate immediate, MatrixStack matrices, Vec3d camera, ItemEspModule module) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientWorld world = client.world;
      ClientPlayerEntity player = client.player;
      if (world == null || player == null) {
         return;
      }

      double reach = module.range.get();
      double reachSquared = reach * reach;
      boolean tracers = module.tracers.get();
      boolean beams = module.beamsRare();
      Vector3fc look = tracers ? client.gameRenderer.getCamera().getHorizontalPlane() : null;

      int drawn = 0;

      for (Entity entity : world.getEntities()) {
         if (drawn >= MAX_MARKERS) {
            break;
         }

         if (!(entity instanceof ItemEntity item) || !item.isAlive()) {
            continue;
         }

         if (player.squaredDistanceTo(item) > reachSquared) {
            continue;
         }

         ItemStack stack = item.getStack();
         if (stack.isEmpty()) {
            continue;
         }

         ItemEspModule.Tier tier = ItemEspModule.tierOf(stack);
         if (!module.shows(tier)) {
            continue;
         }

         int tint = module.tintFor(tier);
         Box box = inflate(item.getBoundingBox());

         EspBoxRenderer.outline(immediate, matrices, camera,
               box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, tint, LINE_WIDTH);

         int shade = module.shadeFor(tier);
         if (shade != 0) {
            EspBoxRenderer.fill(immediate, matrices, camera,
                  box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, shade);
         }

         if (beams && tier == ItemEspModule.Tier.RARE) {
            beam(immediate, matrices, camera, box, tint);
         }

         if (tracers && look != null) {
            EspBoxRenderer.tracer(immediate, matrices, camera,
                  new Vec3d(look.x(), look.y(), look.z()),
                  (box.minX + box.maxX) / 2.0,
                  (box.minY + box.maxY) / 2.0,
                  (box.minZ + box.maxZ) / 2.0,
                  Colors.withAlpha(tint, 0.72F), TRACER_WIDTH);
         }

         drawn++;
      }

      EspBoxRenderer.flush(immediate);
   }

   /**
    * Grow a drop's box to something you can see, keeping it centred on the item
    * so the marker does not drift off what it is marking.
    */
   private static Box inflate(Box box) {
      double growX = Math.max(0.0, (MIN_SIZE - (box.maxX - box.minX)) / 2.0);
      double growY = Math.max(0.0, (MIN_SIZE - (box.maxY - box.minY)) / 2.0);
      double growZ = Math.max(0.0, (MIN_SIZE - (box.maxZ - box.minZ)) / 2.0);
      return box.expand(growX, growY, growZ);
   }

   /**
    * A column standing on the drop, fading out with height.
    *
    * Drawn as four uprights on the box's corners rather than one line through
    * the middle: a single line disappears when you are stood directly over it,
    * which is exactly when you are trying to find the thing.
    */
   private static void beam(Immediate immediate, MatrixStack matrices, Vec3d camera, Box box, int tint) {
      int core = Colors.withAlpha(tint, 0.55F);
      double top = box.maxY + BEAM_HEIGHT;
      FlatOverlay.upright(immediate, matrices, camera, box.minX, box.minZ, box.maxY, top, core);
      FlatOverlay.upright(immediate, matrices, camera, box.maxX, box.minZ, box.maxY, top, core);
      FlatOverlay.upright(immediate, matrices, camera, box.maxX, box.maxZ, box.maxY, top, core);
      FlatOverlay.upright(immediate, matrices, camera, box.minX, box.maxZ, box.maxY, top, core);
   }
}
