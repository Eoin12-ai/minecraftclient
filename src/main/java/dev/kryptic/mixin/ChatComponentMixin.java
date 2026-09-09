package dev.kryptic.mixin;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
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
   private Text kryptic$censorChat(Text text) {
      ModuleManager moduleManager = KrypticClient.modules();
      if (moduleManager == null) {
         return text;
      } else {
         // read before anything rewrites it, so a censored name or a fake role
         // cannot change what the balance parser sees
         if (moduleManager.stats != null && moduleManager.stats.isEnabled()) {
            try {
               moduleManager.stats.onChatMessage(text.getString());
            } catch (Exception ignored) {
            }
         }

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