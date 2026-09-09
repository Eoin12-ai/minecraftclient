package dev.kryptic.mixin;

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
   void kryptic$setRightClickDelay(int n);

   @Invoker("doAttack")
   boolean kryptic$startAttack();

   @Invoker("doItemUse")
   void kryptic$startUseItem();
}
