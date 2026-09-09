package dev.kryptic.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.module.misc.ArmorTrimHiderModule;
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
   private Object kryptic$armorTrim(Object value3, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) Object value2) {
      ModuleManager moduleManager = KrypticClient.modules();
      if (moduleManager == null) {
         return value3;
      } else {
         ArmorTrimHiderModule armorTrimHiderModule = moduleManager.armorTrimHider;
         if (armorTrimHiderModule != null && armorTrimHiderModule.isEnabled()) {
            return !armorTrimHiderModule.affectsOwn() && kryptic$isLocalPlayer(value3) ? value3 : armorTrimHiderModule.mapTrim((ItemStack)value2, (ArmorTrim)value3);
         } else {
            return value3;
         }
      }
   }

   private static boolean kryptic$isLocalPlayer(Object value3) {
      if (!(value3 instanceof PlayerEntityRenderState playerEntityRenderState)) {
         return false;
      } else {
         MinecraftClient client = MinecraftClient.getInstance();
         return client.player != null && playerEntityRenderState.id == client.player.getId();
      }
   }
}
