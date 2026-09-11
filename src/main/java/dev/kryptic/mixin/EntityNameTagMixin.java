package dev.kryptic.mixin;

import com.mojang.authlib.GameProfile;
import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.module.misc.FakeRolesModule;
import dev.kryptic.module.misc.NameTagsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Nametag control: hiding them, censoring them, and decorating your own.
 *
 * The last two parameters have to be spelled out. They were typed {@code
 * Object}, which compiles and looks harmless, but Mixin matches an @Inject
 * handler against the target by raw descriptor and does not treat Object as a
 * wildcard — so it threw InvalidInjectionException while applying, which is
 * fatal whatever {@code defaultRequire} says, and the client died before the
 * window opened. Both types came off the remapped jar rather than a guess.
 */
@Mixin({EntityRenderer.class})
public class EntityNameTagMixin {
   @Inject(
      method = {"renderLabelIfPresent"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void kryptic$nameTag(EntityRenderState entityRenderState, MatrixStack matrices,
                                OrderedRenderCommandQueue queue, CameraRenderState camera,
                                CallbackInfo callbackInfo) {
      ModuleManager moduleManager = KrypticClient.modules();
      if (moduleManager != null && entityRenderState.displayName != null) {
         NameTagsModule nameTagsModule = moduleManager.nameTags;
         boolean enabled = nameTagsModule != null && nameTagsModule.isEnabled();
         if (enabled) {
            String text = entityRenderState.displayName.getString();
            if (isLocalPlayer(text)) {
               if (nameTagsModule.hideOwnTag.get() || nameTagsModule.players.get() && nameTagsModule.self.get()) {
                  callbackInfo.cancel();
                  return;
               }
            } else if (isOnlinePlayer(text)) {
               if (nameTagsModule.players.get() || nameTagsModule.hidePlayerTags.get()) {
                  callbackInfo.cancel();
                  return;
               }
            } else if (nameTagsModule.hideOtherTags.get()) {
               callbackInfo.cancel();
               return;
            }
         }

         if (moduleManager.nameProtect != null && moduleManager.nameProtect.isEnabled()) {
            String name2 = moduleManager.nameProtect.replacementForDisplay(entityRenderState.displayName.getString());
            if (name2 != null) {
               entityRenderState.displayName = Text.literal(name2);
            }
         }

         FakeRolesModule fakeRolesModule = moduleManager.fakeRoles;
         if (fakeRolesModule != null && isLocalPlayer(entityRenderState.displayName.getString())) {
            entityRenderState.displayName = fakeRolesModule.decorateNametag(entityRenderState.displayName);
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
