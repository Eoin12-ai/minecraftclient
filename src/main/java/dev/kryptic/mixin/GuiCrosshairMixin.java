package dev.kryptic.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.kryptic.module.misc.FreecamModule;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({InGameHud.class})
public class GuiCrosshairMixin {
   @ModifyExpressionValue(
      method = {"renderCrosshair"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z"
      )}
   )
   private boolean kryptic$freecamCrosshairInThirdPerson(boolean flag) {
      FreecamModule freecamModule = FreecamModule.get();
      return freecamModule != null && freecamModule.isActive() && freecamModule.isShowPlayerModel() && !flag ? true : flag;
   }
}
