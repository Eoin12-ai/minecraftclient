package dev.sixseven.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({PlayerListHud.class})
public class PlayerTabOverlayMixin {
   @ModifyReturnValue(
      method = {"getPlayerName"},
      at = {@At("RETURN")}
   )
   private Text sixsevenclient$protectTabName(Text text, PlayerListEntry playerListEntry) {
      ModuleManager moduleManager = SixSevenClient.modules();
      if (moduleManager == null) {
         return text;
      } else {
         GameProfile gameProfile = playerListEntry.getProfile();
         String name2 = gameProfile == null ? null : gameProfile.getName();
         Object value = text;
         if (moduleManager.nameProtect != null && moduleManager.nameProtect.isEnabled() && name2 != null && !name2.isEmpty()) {
            String name2r = moduleManager.nameProtect.replacementForDisplay(playerListEntry);
            if (name2r != null) {
               text = Text.literal(name2r);
            }
         }

         if (moduleManager.fakeRoles != null && text != null) {
            text = moduleManager.fakeRoles.decorateTab((Text)text, playerListEntry);
         }

         return (Text)text;
      }
   }
}