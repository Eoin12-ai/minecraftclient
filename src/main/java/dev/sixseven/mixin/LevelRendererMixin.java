package dev.sixseven.mixin;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.render.AccessoryRenderer;
import dev.sixseven.render.BlockEntityEspRenderer;
import dev.sixseven.render.BlockEspRenderer;
import dev.sixseven.render.BlockOutlineRenderer;
import dev.sixseven.render.ChunkFinderRenderer;
import dev.sixseven.render.EntityEspRenderer;
import dev.sixseven.render.HitParticleRenderer;
import dev.sixseven.render.HoleEspRenderer;
import dev.sixseven.render.JumpCircleRenderer;
import dev.sixseven.render.MiscBlockEspRenderer;
import dev.sixseven.render.StorageEspRenderer;
import dev.sixseven.render.SusChunkRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({WorldRenderer.class})
public class LevelRendererMixin {
   @Inject(
      method = {"renderTargetBlockOutline(Lnet/minecraft/VertexConsumerProvider$Immediate;Lnet/minecraft/MatrixStack;ZLnet/minecraft/Object;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sixsevenclient$customBlockOutline(Immediate immediate, MatrixStack matrices, boolean flag, Object worldRenderState, CallbackInfo callbackInfo) {
      ModuleManager moduleManager = SixSevenClient.modules();
      if (moduleManager != null) {
         if (temp4 && moduleManager.susChunkFinder.isEnabled()) {
            SusChunkRenderer.render(temp, temp2, tmp5.cameraRenderState.pos, moduleManager.susChunkFinder);
         }

         if (temp4 && moduleManager.chunkFinder != null && moduleManager.chunkFinder.isEnabled()) {
            ChunkFinderRenderer.render(temp, temp2, tmp5.cameraRenderState.pos, moduleManager.chunkFinder);
         }

         if (temp4 && moduleManager.storageEsp != null && moduleManager.storageEsp.isEnabled()) {
            StorageEspRenderer.render(temp, temp2, tmp5.cameraRenderState.pos, moduleManager.storageEsp);
         }

         if (temp4 && moduleManager.blockEsp != null && moduleManager.blockEsp.isEnabled()) {
            BlockEspRenderer.render(temp, temp2, tmp5.cameraRenderState.pos, moduleManager.blockEsp);
         }

         if (temp4 && moduleManager.blockEntityEsp != null && moduleManager.blockEntityEsp.isEnabled()) {
            BlockEntityEspRenderer.render(temp, temp2, tmp5.cameraRenderState.pos, moduleManager.blockEntityEsp);
         }

         if (temp4 && moduleManager.spawnerNametags != null && moduleManager.spawnerNametags.isEnabled()) {
            MiscBlockEspRenderer.renderSpawners(temp, temp2, tmp5.cameraRenderState.pos, moduleManager.spawnerNametags);
         }

         if (temp4 && moduleManager.debugHoleEsp != null && moduleManager.debugHoleEsp.isEnabled()) {
            HoleEspRenderer.render(temp, temp2, tmp5.cameraRenderState.pos, moduleManager.debugHoleEsp);
         }

         if (temp4 && moduleManager.playerEsp != null && moduleManager.playerEsp.isEnabled()) {
            EntityEspRenderer.renderPlayers(temp, temp2, tmp5.cameraRenderState.pos, moduleManager.playerEsp);
         }

         if (temp4 && moduleManager.mobEsp != null && moduleManager.mobEsp.isEnabled()) {
            EntityEspRenderer.renderMobs(temp, temp2, tmp5.cameraRenderState.pos, moduleManager.mobEsp);
         }

         if (temp4 && moduleManager.jumpCircles != null && moduleManager.jumpCircles.isEnabled()) {
            JumpCircleRenderer.render(temp, temp2, tmp5.cameraRenderState.pos, moduleManager.jumpCircles);
         }

         if (temp4 && moduleManager.hitParticles != null && moduleManager.hitParticles.isEnabled()) {
            HitParticleRenderer.render(temp, temp2, tmp5.cameraRenderState.pos, moduleManager.hitParticles);
         }

         if (temp4 && moduleManager.customAccessories != null && moduleManager.customAccessories.isEnabled()) {
            AccessoryRenderer.render(temp, temp2, tmp5.cameraRenderState.pos, moduleManager.customAccessories);
         }

         if (moduleManager.freecam != null && moduleManager.freecam.isActive()) {
            temp3.cancel();
         } else if (moduleManager.blockOutline.isEnabled()) {
            temp3.cancel();
            Object outlineRenderState = tmp5.outlineRenderState;
            if (outlineRenderState != null && outlineRenderState.isTranslucent() == temp4) {
               BlockOutlineRenderer.render(temp, temp2, outlineRenderState, tmp5.cameraRenderState.pos, moduleManager.blockOutline);
            }
         }
      }
   }
}
