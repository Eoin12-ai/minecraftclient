package dev.kryptic.render;

import dev.kryptic.theme.ThemeColors;
import dev.kryptic.module.render.MobEspModule;
import dev.kryptic.module.render.PlayerEspModule;
import dev.kryptic.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3fc;

public final class EntityEspRenderer {
   private static final float TRACER_WIDTH = 1.2F;

   private EntityEspRenderer() {
   }

   public static void renderPlayers(Immediate immediate, MatrixStack matrices, Vec3d vec, PlayerEspModule playerEspModule) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientWorld world = client.world;
      if (world != null && client.player != null) {
         int n = ThemeColors.resolve(playerEspModule.color.get());
         boolean found = playerEspModule.style.is("Glow");
         boolean found2 = playerEspModule.tracers.get();
         Vector3fc camera = found2 ? client.gameRenderer.getCamera().getHorizontalPlane() : null;

         for (AbstractClientPlayerEntity abstractClientPlayerEntity : world.getPlayers()) {
            if (abstractClientPlayerEntity != client.player && abstractClientPlayerEntity.isAlive() && !abstractClientPlayerEntity.isSpectator()) {
               box(immediate, matrices, vec, abstractClientPlayerEntity.getBoundingBox(), n, found);
               if (found2) {
                  tracer(immediate, matrices, vec, camera, abstractClientPlayerEntity.getBoundingBox(), n);
               }
            }
         }

         EspBoxRenderer.flush(immediate);
      }
   }

   public static void renderMobs(Immediate immediate, MatrixStack matrices, Vec3d vec, MobEspModule mobEspModule) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientWorld world = client.world;
      if (world != null) {
         int n = mobEspModule.hostile.get();
         int localX = mobEspModule.passive.get();
         boolean found = mobEspModule.passiveToo.get();
         boolean found2 = mobEspModule.tracers.get();
         Vector3fc camera = found2 ? client.gameRenderer.getCamera().getHorizontalPlane() : null;

         for (Entity entity : world.getEntities()) {
            if (entity instanceof MobEntity && entity.isAlive()) {
               boolean ok = entity instanceof Monster;
               if (ok || found) {
                  int localZ = ok ? n : localX;
                  box(immediate, matrices, vec, entity.getBoundingBox(), localZ, false);
                  if (found2) {
                     tracer(immediate, matrices, vec, camera, entity.getBoundingBox(), localZ);
                  }
               }
            }
         }

         EspBoxRenderer.flush(immediate);
      }
   }

   private static void box(Immediate immediate, MatrixStack matrices, Vec3d vec, Box box2, int n, boolean value) {
      EspBoxRenderer.outline(immediate, matrices, vec, box2.minX, box2.minY, box2.minZ, box2.maxX, box2.maxY, box2.maxZ, n, 2.0F);
      if (value) {
         EspBoxRenderer.fill(
            immediate,
            matrices,
            vec,
            box2.minX,
            box2.minY,
            box2.minZ,
            box2.maxX,
            box2.maxY,
            box2.maxZ,
            Colors.withAlpha(n, 0.18F)
         );
      }
   }

   private static void tracer(Immediate immediate, MatrixStack matrices, Vec3d vec, Vector3fc vector3fc, Box box2, int n) {
      EspBoxRenderer.tracer(
         immediate,
         matrices,
         vec,
         new net.minecraft.util.math.Vec3d(vector3fc.x(), vector3fc.y(), vector3fc.z()),
         (box2.minX + box2.maxX) / 2.0,
         (box2.minY + box2.maxY) / 2.0,
         (box2.minZ + box2.maxZ) / 2.0,
         Colors.withAlpha(n, 0.72F),
         1.2F
      );
   }
}
