package dev.sixseven.gui;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.misc.GambleRiggerModule;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterInit;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterRender;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.AllowMouseClick;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;

public final class GambleRiggerOverlay {
   private GambleRiggerOverlay() {
   }

   public static void register() {
      ScreenEvents.AFTER_INIT
         .register(
            (AfterInit)(arg, arg2, arg3, arg4) -> {
               if (arg2 instanceof Generic3x3ContainerScreen generic3x3ContainerScreen) {
                  GambleRiggerModule gambleRiggerModule = SixSevenClient.modules() == null ? null : SixSevenClient.modules().gambleRigger;
                  if (gambleRiggerModule != null && gambleRiggerModule.isEnabled()) {
                     GamblePanel gamblePanel = new GamblePanel(generic3x3ContainerScreen, gambleRiggerModule);
                     ScreenEvents.afterRender(arg2).register((AfterRender)(arg5, arg6, arg7, arg8, arg9) -> gamblePanel.render(arg6, arg7, arg8));
                     ScreenMouseEvents.allowMouseClick(arg2)
                        .register((AllowMouseClick)(arg5, arg6) -> !gamblePanel.handleClick(arg6.x(), arg6.y(), arg6.button()));
                  }
               }
            }
         );
   }
}
