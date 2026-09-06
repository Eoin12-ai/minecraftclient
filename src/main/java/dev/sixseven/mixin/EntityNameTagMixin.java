package dev.sixseven.mixin;

import com.mojang.authlib.GameProfile;
import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.module.misc.FakeRolesModule;
import dev.sixseven.module.misc.NameTagsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public class EntityNameTagMixin {
   @Inject(
      method = {"renderLabelIfPresent"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sixsevenclient$nameTag(EntityRenderState entityRenderState, MatrixStack matrices, Object orderedRenderCommandQueue, Object cameraRenderState, CallbackInfo callbackInfo) {
      ModuleManager moduleManager = SixSevenClient.modules();
      if (moduleManager != null && temp.displayName != null) {
         NameTagsModule nameTagsModule = moduleManager.nameTags;
         boolean enabled = nameTagsModule != null && nameTagsModule.isEnabled();
         if (enabled) {
            String text = temp.displayName.getString();
            if (isLocalPlayer(text)) {
               if (nameTagsModule.hideOwnTag.get() || nameTagsModule.players.get() && nameTagsModule.self.get()) {
                  temp2.cancel();
                  return;
               }
            } else if (isOnlinePlayer(text)) {
               if (nameTagsModule.players.get() || nameTagsModule.hidePlayerTags.get()) {
                  temp2.cancel();
                  return;
               }
            } else if (nameTagsModule.hideOtherTags.get()) {
               temp2.cancel();
               return;
            }
         }

         if (moduleManager.nameProtect != null && moduleManager.nameProtect.isEnabled()) {
            String name2 = moduleManager.nameProtect.replacementForDisplay(temp.displayName.getString());
            if (name2 != null) {
               temp.displayName = Text.literal(name2);
            }
         }

         FakeRolesModule fakeRolesModule = moduleManager.fakeRoles;
         if (fakeRolesModule != null && isLocalPlayer(temp.displayName.getString())) {
            temp.displayName = fakeRolesModule.decorateNametag(temp.displayName);
         }
      }
   }

   private static boolean isLocalPlayer(String text) {
      if (text != null && !text.isEmpty()) {
         ClientPlayerEntity player = MinecraftClient.getInstance().player;
         if (player == null) {
            return false;
         } else {
            String name3 = player.getGameProfile().name();
            return name3 != null && !name3.isEmpty() && text.contains(name3);
         }
      } else {
         return false;
      }
   }

   private static boolean isOnlinePlayer(String text) {
      if (text != null && !text.isEmpty()) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.getNetworkHandler() == null) {
            return false;
         } else {
            for (PlayerListEntry playerListEntry : client.getNetworkHandler().getPlayerList()) {
               GameProfile gameProfile = playerListEntry.getProfile();
               String name3 = gameProfile == null ? null : gameProfile.name();
               if (name3 != null && !name3.isEmpty() && text.contains(name3)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }
}
