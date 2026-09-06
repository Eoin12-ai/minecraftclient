package dev.sixseven.mixin;

import dev.sixseven.SixSevenClient;
import dev.sixseven.gui.ClickGuiScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Keyboard.class})
public class KeyboardHandlerMixin {
   @Inject(
      method = {"onKey(JIII)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sixsevenclient$dispatchModuleKeybinds(long l, int n, int keyCode, int scanCode, CallbackInfo callbackInfo) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (SixSevenClient.modules() != null && temp3 == 1) {
         if (client.currentScreen == null && client.world != null) {
            if (SixSevenClient.modules().onKeyPressed(keyCode)) {
               temp2.cancel();
            }
         } else if ((keyCode == 340 || keyCode == 344) && client.currentScreen != null && shiftOpensGui(client.currentScreen)) {
            client.setScreen(new ClickGuiScreen(client.currentScreen));
            temp2.cancel();
         }
      }
   }

   private static boolean shiftOpensGui(Screen screen) {
      if (!(screen instanceof ClickGuiScreen)
         && !(screen instanceof ChatScreen)
         && !(screen instanceof HandledScreen)
         && !(screen instanceof AbstractSignEditScreen)
         && !(screen instanceof BookEditScreen)) {
         if (screen.getFocused() instanceof TextFieldWidget textFieldWidget && textFieldWidget.isFocused()) {
            return false;
         }

         return true;
      } else {
         return false;
      }
   }
}
