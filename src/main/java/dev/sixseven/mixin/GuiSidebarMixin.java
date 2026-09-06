package dev.sixseven.mixin;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.module.misc.FakeStatsModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameHud.class})
public class GuiSidebarMixin {
   @Inject(
      method = {"renderScoreboardSidebar"},
      at = {@At("HEAD")}
   )
   private void sixsevenclient$beginSidebar(DrawContext context, ScoreboardObjective scoreboardObjective, CallbackInfo callbackInfo) {
      FakeStatsModule fakeStatsModule = fakeStats();
      if (fakeStatsModule != null) {
         temp.beginSidebar();
      }
   }

   @Redirect(
      method = {"renderScoreboardSidebar"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/TextRenderer;getWidth(Lnet/minecraft/StringVisitable;)I",
         ordinal = 1
      )
   )
   private int sixsevenclient$lineWidth(TextRenderer textRenderer, StringVisitable stringVisitable) {
      FakeStatsModule fakeStatsModule = fakeStats();
      if (fakeStatsModule != null && temp2 instanceof Text text) {
         try {
            return temp.getWidth(fakeStatsModule.rewriteForWidth(text));
         } catch (Exception ex) {
         }
      }

      return temp.getWidth(temp2);
   }

   @ModifyArg(
      method = {"renderScoreboardSidebar"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/DrawContext;drawText(Lnet/minecraft/TextRenderer;Lnet/minecraft/Text;IIIZ)V",
         ordinal = 1
      ),
      index = 1
   )
   private Text sixsevenclient$lineText(Text text) {
      FakeStatsModule fakeStatsModule = fakeStats();
      if (fakeStatsModule != null) {
         try {
            return fakeStatsModule.rewriteForDraw(temp);
         } catch (Exception ex) {
         }
      }

      return temp;
   }

   private static FakeStatsModule fakeStats() {
      ModuleManager moduleManager = SixSevenClient.modules();
      return moduleManager != null && moduleManager.fakeStats != null && moduleManager.fakeStats.isEnabled() ? moduleManager.fakeStats : null;
   }
}
