package com.swyzzyaddon.mixin;

import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pkg1.StatsChangerModule;

@Mixin({TextVisitFactory.class})
public abstract class TextVisitFactoryMixin {
   @ModifyVariable(
      method = {"method_27473(Ljava/lang/String;ILnet/minecraft/class_2583;Lnet/minecraft/class_2583;Lnet/minecraft/class_5224;)Z"},
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private static String swyzzy$rewriteText(String var0) {
      StatsChangerModule var1 = StatsChangerModule.getVal();
      return var1 == null ? var0 : var1.addSetting(var0);
   }
}
