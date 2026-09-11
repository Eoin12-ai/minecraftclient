package dev.kryptic.mixin;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.module.misc.FakeStatsModule;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
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
   private void kryptic$beginSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
      FakeStatsModule fakeStatsModule = fakeStats();
      if (fakeStatsModule != null) {
         fakeStatsModule.beginSidebar();
      }
   }

   @Redirect(
      method = {"renderScoreboardSidebar"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/StringVisitable;)I",
         ordinal = 1
      )
   )
   private int kryptic$lineWidth(TextRenderer textRenderer, StringVisitable stringVisitable) {
      FakeStatsModule fakeStatsModule = fakeStats();
      if (fakeStatsModule != null && stringVisitable instanceof Text text) {
         try {
            return textRenderer.getWidth(fakeStatsModule.rewriteForWidth(text));
         } catch (Exception ex) {
         }
      }

      return textRenderer.getWidth(stringVisitable);
   }

   @ModifyArg(
      method = {"renderScoreboardSidebar"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V",
         ordinal = 1
      ),
      index = 1
   )
   private Text kryptic$lineText(Text text) {
      FakeStatsModule fakeStatsModule = fakeStats();
      if (fakeStatsModule != null) {
         try {
            return fakeStatsModule.rewriteForDraw(text);
         } catch (Exception ex) {
         }
      }

      return text;
   }

   private static FakeStatsModule fakeStats() {
      ModuleManager moduleManager = KrypticClient.modules();
      return moduleManager != null && moduleManager.fakeStats != null && moduleManager.fakeStats.isEnabled() ? moduleManager.fakeStats : null;
   }
}
