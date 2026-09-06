package dev.sixseven.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
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
   private int sixsevenclient$swingSpeed(int n) {
      LivingEntity entity = (LivingEntity)this;
      if (MinecraftClient.getInstance().player != entity) {
         return temp;
      } else {
         ModuleManager moduleManager = SixSevenClient.modules();
         if (moduleManager != null && moduleManager.swingSpeed != null && moduleManager.swingSpeed.isEnabled()) {
            float f = temp2.swingSpeed.multiplier();
            return f <= 0.01F ? temp : Math.max(1, Math.round((float)temp / f));
         } else {
            return temp;
         }
      }
   }
}
