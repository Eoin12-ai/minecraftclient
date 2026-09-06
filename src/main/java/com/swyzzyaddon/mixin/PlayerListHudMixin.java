package com.swyzzyaddon.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pkg1.FakeRankModule;

@Mixin({PlayerListHud.class})
public abstract class PlayerListHudMixin {
   @Inject(
      method = {"method_1918"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void swyzzy$fakeRank(PlayerListEntry var1, CallbackInfoReturnable<Text> var2) {
      Text var3 = (Text)var2.getReturnValue();
      Text var4 = FakeRankModule.class2561Of(var1.getProfile(), var3);
      if (var4 != var3) {
         var2.setReturnValue(var4);
      }
   }
}
