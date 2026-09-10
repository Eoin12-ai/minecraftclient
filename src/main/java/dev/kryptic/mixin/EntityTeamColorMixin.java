package dev.kryptic.mixin;

import dev.kryptic.module.render.ChamsModule;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The colour half of Chams.
 *
 * The outline pass does not take a colour of its own — it reads the entity's
 * team colour. So tinting an outline means answering this question differently
 * for the entities Chams is drawing, which is why the hook is here rather than
 * anywhere in the renderer.
 *
 * <p>Guarded on the same predicate as the outline itself. Without that, an
 * entity that is not being outlined would still have its team colour rewritten,
 * and team colour is also what tints a player's name in chat and on the
 * scoreboard — a module about seeing through walls has no business changing
 * who looks like they are on your team.
 */
@Mixin(Entity.class)
public class EntityTeamColorMixin {

   @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
   private void kryptic$chamsColor(CallbackInfoReturnable<Integer> callbackInfoReturnable) {
      ChamsModule chams = ChamsModule.get();
      Entity self = (Entity)(Object)this;
      if (chams != null && chams.outlines(self)) {
         callbackInfoReturnable.setReturnValue(Integer.valueOf(chams.tint(self)));
      }
   }
}
