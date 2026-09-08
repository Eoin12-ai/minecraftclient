package dev.sixseven.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.module.misc.ArmorTrimHiderModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({EquipmentRenderer.class})
public class EquipmentLayerRendererMixin {
   @ModifyExpressionValue(
      method = {"render"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"
      )}
   )
   private Object sixsevenclient$armorTrim(Object value3, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) Object value2) {
      ModuleManager moduleManager = SixSevenClient.modules();
      if (moduleManager == null) {
         return value3;
      } else {
         ArmorTrimHiderModule armorTrimHiderModule = moduleManager.armorTrimHider;
         if (armorTrimHiderModule != null && armorTrimHiderModule.isEnabled()) {
            return !armorTrimHiderModule.affectsOwn() && sixsevenclient$isLocalPlayer(value3) ? value3 : armorTrimHiderModule.mapTrim((ItemStack)value2, (ArmorTrim)value3);
         } else {
            return value3;
         }
      }
   }

   private static boolean sixsevenclient$isLocalPlayer(Object value3) {
      if (!(value3 instanceof PlayerEntityRenderState playerEntityRenderState)) {
         return false;
      } else {
         MinecraftClient client = MinecraftClient.getInstance();
         return client.player != null && playerEntityRenderState.id == client.player.getId();
      }
   }
}
