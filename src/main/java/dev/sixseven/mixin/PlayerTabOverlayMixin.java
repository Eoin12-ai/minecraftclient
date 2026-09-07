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
         Object value = temp;
         if (moduleManager.nameProtect != null && moduleManager.nameProtect.isEnabled() && name2 != null && !name2.isEmpty()) {
            String name2 = moduleManager.nameProtect.replacementForDisplay(temp2);
            if (name2 != null) {
               text = Text.literal(name2);
            }
         }

         if (moduleManager.fakeRoles != null && temp != null) {
            temp = temp3.fakeRoles.decorateTab((Text)temp, temp2);
         }

         return (Text)temp;
      }
   }
}
