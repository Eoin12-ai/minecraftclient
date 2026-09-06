package dev.sixseven.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.module.misc.FreecamModule;
import dev.sixseven.render.BlurHook;
import dev.sixseven.render.OverlayRenderer;
import dev.sixseven.render.WorldProjection;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class GameRendererMixin {
   @Inject(
      method = {"render(Lnet/minecraft/RenderTickCounter;Z)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/GuiRenderer;incrementFrame()V",
         shift = Shift.AFTER
      )}
   )
   private void sixsevenclient$renderOverlay(RenderTickCounter renderTickCounter, boolean flag, CallbackInfo callbackInfo) {
      OverlayRenderer.render();
   }

   @Inject(
      method = {"renderWorld(Lnet/minecraft/RenderTickCounter;)V"},
      at = {@At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lnet/minecraft/ProjectionType;)V"
      )}
   )
   private void sixsevenclient$captureProjection(RenderTickCounter renderTickCounter, CallbackInfo callbackInfo, @Local(ordinal = 0) Matrix4f matrix) {
      WorldProjection.capture(temp2, temp.getTickProgress(false));
   }

   @ModifyArg(
      method = {"render(Lnet/minecraft/RenderTickCounter;Z)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/GlobalSettings;set(IIDJLnet/minecraft/RenderTickCounter;ILnet/minecraft/Camera;Z)V"
      ),
      index = 5
   )
   private int sixsevenclient$overrideBlurRadius(int n) {
      return BlurHook.apply(temp);
   }

   @ModifyReturnValue(
      method = {"getFov(Lnet/minecraft/Camera;FZ)F"},
      at = {@At("RETURN")}
   )
   private float sixsevenclient$zoomFov(float f) {
      ModuleManager moduleManager = SixSevenClient.modules();
      if (moduleManager != null && moduleManager.zoom != null) {
         double d = temp.zoom.currentFactor();
         return d > 1.0001 ? (float)((double)temp2 / d) : temp2;
      } else {
         return temp;
      }
   }

   @ModifyExpressionValue(
      method = {"getFov(Lnet/minecraft/Camera;FZ)F"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/MathHelper;lerp(FFF)F",
         ordinal = 0
      )}
   )
   private float sixsevenclient$customFov(float f) {
      ModuleManager moduleManager = SixSevenClient.modules();
      return moduleManager != null && moduleManager.customFov != null ? moduleManager.customFov.fovMultiplier(temp) : temp;
   }

   @ModifyExpressionValue(
      method = {"renderHand"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/Perspective;isFirstPerson()Z"
      )}
   )
   private boolean sixsevenclient$freecamRenderFirstPersonHands(boolean flag) {
      FreecamModule freecamModule = FreecamModule.get();
      return freecamModule != null && freecamModule.isActive() && freecamModule.renderHands() ? true : temp;
   }
}
