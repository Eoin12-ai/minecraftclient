package com.swyzzyaddon.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({MinecraftClient.class})
public interface MinecraftClientAccessor {
   @Accessor("attackCooldown")
   int swyzzy$getAttackCooldown();

   @Accessor("attackCooldown")
   void swyzzy$setAttackCooldown(int var1);
}
