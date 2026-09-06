package dev.sixseven.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.sixseven.SixSevenClient;
import dev.sixseven.hud.HudDragController;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.util.CpsTracker;
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

   @Inject(
      method = {"updateMouse"},
      at = {@At("HEAD")}
   )
   private void sixsevenclient$aimAssist(double d, CallbackInfo callbackInfo) {
      ModuleManager moduleManager = SixSevenClient.modules();
      if (moduleManager != null && moduleManager.aimAssist != null && moduleManager.aimAssist.isEnabled()) {
         double[] d = temp.aimAssist.computePixels(temp2, this.cursorDeltaX, this.cursorDeltaY);
         if (d != null) {
            this.cursorDeltaX = this.cursorDeltaX + d[0];
            this.cursorDeltaY = this.cursorDeltaY + d[1];
         }
      }
   }

   @Inject(
      method = {"onMouseButton(JLnet/minecraft/MouseInput;I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sixsevenclient$onButton(long l, MouseInput mouseInput, int n, CallbackInfo callbackInfo) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (temp == client.getWindow().getHandle()) {
         if (temp4 == 1 && client.currentScreen == null) {
            CpsTracker.onClick(temp3.button());
         }

         if (client.currentScreen instanceof ChatScreen && temp3.button() == 0 && SixSevenClient.hud() != null) {
            if (temp4 == 1) {
               if (HudDragController.tryStartDrag(SixSevenClient.hud())) {
                  temp2.cancel();
               }
            } else if (temp4 == 0 && HudDragController.isDragging()) {
               HudDragController.stopDrag();
               SixSevenClient.config().save();
               temp2.cancel();
            }
         }
      }
   }

   @ModifyExpressionValue(
      method = {"updateMouse"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/SimpleOption;getValue()Ljava/lang/Object;"
      )},
      slice = {@Slice(
         from = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/GameOptions;getMouseSensitivity()Lnet/minecraft/SimpleOption;"
         )
      )}
   )
   private Object sixsevenclient$zoomSensitivity(Object value2) {
      ModuleManager moduleManager = SixSevenClient.modules();
      if (moduleManager != null && moduleManager.zoom != null && temp instanceof Double value) {
         double d = moduleManager.zoom.currentFactor();
         if (d <= 1.0001) {
            return temp;
         } else {
            double coord = value * 0.6 + 0.2;
            double currentScore = coord / Math.cbrt(d);
            double coord3 = (currentScore - 0.2) / 0.6;
            return Math.max(0.0, coord3);
         }
      } else {
         return temp;
      }
   }

   @Inject(
      method = {"onMouseScroll(JDD)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sixsevenclient$onScroll(long l, double coord, double d, CallbackInfo callbackInfo) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (temp2 == client.getWindow().getHandle()
         && client.currentScreen instanceof ChatScreen
         && SixSevenClient.hud() != null
         && HudDragController.tryResize(SixSevenClient.hud(), temp)) {
         temp3.cancel();
      }
   }
}
