package dev.sixseven.mixin;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({ChatHud.class})
public class ChatComponentMixin {
   @ModifyVariable(
      method = {"addMessage"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private Text sixsevenclient$censorChat(Text text) {
      ModuleManager moduleManager = SixSevenClient.modules();
      if (moduleManager == null) {
         return text;
      } else {
         Text modified = text;
         if (moduleManager.fakeRoles != null) {
            modified = moduleManager.fakeRoles.decorateChat(text);
         }

         if (moduleManager.nameProtect != null && moduleManager.nameProtect.isEnabled()) {
            modified = moduleManager.nameProtect.censorChat(modified);
         }

         return modified;
      }
   }
}