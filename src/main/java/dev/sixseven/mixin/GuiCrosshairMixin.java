package dev.sixseven.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.module.misc.FreecamModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameHud.class})
public class GuiCrosshairMixin {
   @Inject(
      method = {"renderCrosshair"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sixsevenclient$hideVanillaCrosshair(DrawContext context, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
      ModuleManager moduleManager = SixSevenClient.modules();
      if (moduleManager != null && moduleManager.customCrosshair != null && moduleManager.customCrosshair.shouldHideVanilla()) {
         temp.cancel();
      }
   }

   @ModifyExpressionValue(
      method = {"renderCrosshair"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/Perspective;isFirstPerson()Z"
      )}
   )
   private boolean sixsevenclient$freecamCrosshairInThirdPerson(boolean flag) {
      FreecamModule freecamModule = FreecamModule.get();
      return freecamModule != null && freecamModule.isActive() && freecamModule.isShowPlayerModel() && !temp ? true : temp;
   }
}
