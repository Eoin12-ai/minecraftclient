package dev.kryptic.mixin;

import dev.kryptic.module.render.ChamsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The gate for Chams.
 *
 * {@code hasOutline} is what decides whether an entity goes through vanilla's
 * outline pass — the same pass that makes a glowing entity readable through a
 * wall. Forcing it true for the entities Chams wants gets the effect without
 * touching the entity render pipeline at all.
 *
 * <p>Only ever widened, never narrowed: an entity vanilla already wants to
 * outline (a spectral-arrow hit, a team glow) keeps its outline, because this
 * returns early rather than returning false.
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientOutlineMixin {

   @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
   private void kryptic$chamsOutline(Entity entity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
      ChamsModule chams = ChamsModule.get();
      if (chams != null && chams.outlines(entity)) {
         callbackInfoReturnable.setReturnValue(Boolean.TRUE);
      }
   }
}
