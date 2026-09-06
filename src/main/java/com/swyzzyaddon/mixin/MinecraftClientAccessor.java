package com.swyzzyaddon.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({MinecraftClient.class})
public interface MinecraftClientAccessor {
   @Accessor("field_1771")
   int swyzzy$getAttackCooldown();

   @Accessor("field_1771")
   void swyzzy$setAttackCooldown(int var1);
}
