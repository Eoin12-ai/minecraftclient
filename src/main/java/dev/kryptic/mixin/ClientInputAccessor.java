package dev.kryptic.mixin;

import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Input.class})
public interface ClientInputAccessor {
   @Accessor("playerInput")
   void kryptic$setKeyPresses(PlayerInput playerInput);

   @Accessor("movementVector")
   void kryptic$setMoveVector(Vec2f playerInput);
}
