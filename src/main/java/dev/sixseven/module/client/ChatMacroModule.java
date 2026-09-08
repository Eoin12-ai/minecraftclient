package dev.sixseven.module.client;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.KeybindSetting;
import dev.sixseven.settings.ModeSetting;
import dev.sixseven.settings.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;

public class ChatMacroModule extends Module {
   private static final int SLOTS = 3;
   public final ModeSetting slot = this.addSetting(new ModeSetting("Slot", "Macro slot to edit.", "B", "B", "A", "@"));
   public final BooleanSetting sendInstantly = this.addSetting(new BooleanSetting("Send Instantly", "Skip the chat preview and send immediately.", true));
   private final StringSetting[] messages = new StringSetting[3];
   private final KeybindSetting[] keys = new KeybindSetting[3];

   public ChatMacroModule() {
      super("ChatMacro", "Bindable chat command macros.", Category.CLIENT);

      for (int n = 0; n < 3; n++) {
         String text = Integer.toString(n + 1);
         this.messages[n] = this.addSetting(new StringSetting("Message " + text, "Message or /command for slot " + text, "", 256, "/say hi"));
         this.keys[n] = this.addSetting(new KeybindSetting("Key " + text, "Key that runs slot " + text, -1));
         this.messages[n].visibleWhen(() -> this.slot.is(text));
         this.keys[n].visibleWhen(() -> this.slot.is(text));
      }
   }

   @Override
   public boolean onKeyPress(int n) {
      boolean matches2 = false;

      for (int offset = 0; offset < 3; offset++) {
         if (this.keys[offset].matches(n)) {
            this.run(this.messages[offset].get());
            matches2 = true;
         }
      }

      return matches2;
   }

   private void run(String text) {
      if (text != null && !text.isBlank()) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null && client.player.networkHandler != null) {
            if (!this.sendInstantly.get()) {
               client.setScreen(new ChatScreen(text, false));
            } else if (text.startsWith("\\")) {
               client.player.networkHandler.sendChatCommand(text.substring(1));
            } else {
               client.player.networkHandler.sendChatMessage(text);
            }
         }
      }
   }
}
