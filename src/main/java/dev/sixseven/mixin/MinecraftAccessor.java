package dev.sixseven.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({MinecraftClient.class})
public interface MinecraftAccessor {
   @Accessor("attackCooldown")
   void setItemUseCooldown(int n);

   @Accessor("attackCooldown")
   int getItemUseCooldown();

   @Accessor("itemUseCooldown")
   void sixsevenclient$setRightClickDelay(int n);

   @Invoker("doAttack")
   boolean sixsevenclient$startAttack();

   @Invoker("doItemUse")
   void sixsevenclient$startUseItem();
}
