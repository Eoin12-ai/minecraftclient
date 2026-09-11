package dev.kryptic.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.kryptic.KrypticClient;
import dev.kryptic.hud.HudDragController;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.util.CpsTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Mouse.class})
public class MouseHandlerMixin {
   @Shadow
   private double cursorDeltaX;
   @Shadow
   private double cursorDeltaY;

   /**
    * Aim Assist scales the movement you are already making.
    *
    * It adds to the deltas rather than writing a rotation, so every angle the
    * server sees still originated from your hand. A still mouse produces no
    * deltas and therefore no assistance at all.
    */
   @Inject(method = "updateMouse", at = @At("HEAD"))
   private void kryptic$aimAssist(double d, CallbackInfo callbackInfo) {
      ModuleManager modules = KrypticClient.modules();
      if (modules == null || modules.aimAssist == null) {
         return;
      }

      double[] extra = modules.aimAssist.assist(this.cursorDeltaX, this.cursorDeltaY);
      if (extra != null) {
         this.cursorDeltaX += extra[0];
         this.cursorDeltaY += extra[1];
      }
   }

   @Inject(
      method = {"onMouseButton(JLnet/minecraft/client/input/MouseInput;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void kryptic$onButton(long l, MouseInput input, int mouseInputAction, CallbackInfo callbackInfo) {
      MinecraftClient client = MinecraftClient.getInstance();
      int mouseInputButton = input.button();
      if (l == client.getWindow().getHandle()) {
         if (mouseInputAction == 1 && client.currentScreen == null) {
            CpsTracker.onClick(mouseInputButton);
         }

         if (client.currentScreen instanceof ChatScreen && mouseInputButton == 0 && KrypticClient.hud() != null) {
            if (mouseInputAction == 1) {
               if (HudDragController.tryStartDrag(KrypticClient.hud())) {
                  callbackInfo.cancel();
               }
            } else if (mouseInputAction == 0 && HudDragController.isDragging()) {
               HudDragController.stopDrag();
               KrypticClient.config().save();
               callbackInfo.cancel();
            }
         }
      }
   }

   @ModifyExpressionValue(
      method = {"updateMouse"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"
      )},
      slice = {@Slice(
         from = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/GameOptions;getMouseSensitivity()Lnet/minecraft/client/option/SimpleOption;"
         )
      )}
   )
   private Object kryptic$zoomSensitivity(Object value2) {
      ModuleManager moduleManager = KrypticClient.modules();
      if (moduleManager != null && moduleManager.zoom != null && value2 instanceof Double value) {
         double d = moduleManager.zoom.currentFactor();
         if (d <= 1.0001) {
            return value2;
         } else {
            double coord = value * 0.6 + 0.2;
            double currentScore = coord / Math.cbrt(d);
            double coord3 = (currentScore - 0.2) / 0.6;
            return Math.max(0.0, coord3);
         }
      } else {
         return value2;
      }
   }

   @Inject(
      method = {"onMouseScroll(JDD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void kryptic$onScroll(long l, double coord, double d, CallbackInfo callbackInfo) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (l == client.getWindow().getHandle()
         && client.currentScreen instanceof ChatScreen
         && KrypticClient.hud() != null
         && HudDragController.tryResize(KrypticClient.hud(), coord)) {
         callbackInfo.cancel();
      }
   }
}
