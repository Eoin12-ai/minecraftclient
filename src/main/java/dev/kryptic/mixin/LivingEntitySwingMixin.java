package dev.kryptic.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({LivingEntity.class})
public class LivingEntitySwingMixin {
   @ModifyReturnValue(
      method = {"getHandSwingDuration"},
      at = {@At("RETURN")}
   )
   private int kryptic$swingSpeed(int n) {
      LivingEntity entity = (LivingEntity)(Object)this;
      if (MinecraftClient.getInstance().player != entity) {
         return n;
      } else {
         ModuleManager moduleManager = KrypticClient.modules();
         if (moduleManager != null && moduleManager.swingSpeed != null && moduleManager.swingSpeed.isEnabled()) {
            float f = moduleManager.swingSpeed.multiplier();
            return f <= 0.01F ? n : Math.max(1, Math.round((float)n / f));
         } else {
            return n;
         }
      }
   }
}
