package dev.kryptic.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.module.misc.FreecamModule;
import dev.kryptic.render.BlurHook;
import dev.kryptic.render.WorldProjection;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class GameRendererMixin {
   @Inject(
      method = {"renderWorld"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/systems/ProjectionType;)V"
      )}
   )
   private void kryptic$captureProjection(RenderTickCounter renderTickCounter, CallbackInfo callbackInfo, @Local(ordinal = 0) Matrix4f matrix) {
      WorldProjection.capture(matrix, renderTickCounter.getTickProgress(false));
   }

   @ModifyArg(
      method = {"render"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gl/GlobalSettings;set(IIDJLnet/minecraft/client/render/RenderTickCounter;ILnet/minecraft/client/render/Camera;Z)V"
      ),
      index = 5
   )
   private int kryptic$overrideBlurRadius(int n) {
      return BlurHook.apply(n);
   }

   @ModifyReturnValue(
      method = {"getFov"},
      at = {@At("RETURN")}
   )
   private float kryptic$zoomFov(float f) {
      ModuleManager moduleManager = KrypticClient.modules();
      if (moduleManager != null && moduleManager.zoom != null) {
         double d = moduleManager.zoom.currentFactor();
         return d > 1.0001 ? (float)((double)f / d) : f;
      } else {
         return f;
      }
   }

   @ModifyExpressionValue(
      method = {"getFov"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F",
         ordinal = 0
      )}
   )
   private float kryptic$customFov(float f) {
      ModuleManager moduleManager = KrypticClient.modules();
      return moduleManager != null && moduleManager.customFov != null ? moduleManager.customFov.fovMultiplier(f) : f;
   }

   @ModifyExpressionValue(
      method = {"renderHand"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z"
      )}
   )
   private boolean kryptic$freecamRenderFirstPersonHands(boolean flag) {
      FreecamModule freecamModule = FreecamModule.get();
      return freecamModule != null && freecamModule.isActive() && freecamModule.renderHands() ? true : flag;
   }
}
