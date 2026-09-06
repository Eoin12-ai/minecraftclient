package dev.sixseven.module.misc;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.ColorSetting;
import dev.sixseven.settings.ModeSetting;
import dev.sixseven.settings.StringSetting;
import dev.sixseven.util.Amounts;
import java.lang.invoke.StringConcatFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class FakePayModule extends Module {
   private static final int WHITE = 16777215;
   private static final int RED = 16733525;
   public final StringSetting command = this.addSetting(new StringSetting("Command", "Command name to hijack, without the slash.", "pay", 32, "pay"));
   public final ModeSetting feedback = this.addSetting(new ModeSetting("Feedback", "Where the confirmation shows.", "Both", "Both", "Action Bar", "Chat"));
   public final StringSetting currency = this.addSetting(new StringSetting("Currency", "Symbol before the amount (colored).", "W", 4, "W"));
   public final ColorSetting currencyColor = this.addSetting(
      new ColorSetting("Currency Color", "Color of the currency symbol (DonutSMP blue by default).", -16740609)
   );
   public final BooleanSetting sounds = this.addSetting(new BooleanSetting("Sounds", "Level-up on success, villager 'no' when it fails.", true));
   public final BooleanSetting checkBalance = this.addSetting(new BooleanSetting("Check Balance", "With FakeStats on, refuse pays you can't afford.", true));
   public final BooleanSetting selfGuard = this.addSetting(new BooleanSetting("Self-Pay Guard", "Block paying your own name, like the real command.", true));

   public FakePayModule() {
      super("FakePay", "Fakes a /pay for clips — blocks the real command", Category.MISC);
   }

   public boolean tryIntercept(String text2) {
      if (this.isEnabled() && text2 != null) {
         String[] text5 = text2.trim().split("/_c");
         if (text5.length == 0) {
            return false;
         } else {
            String text6 = text5[0];
            if (text6.startsWith("\\")) {
               text6 = text6.substring(1);
            }

            String trimmed = this.command.get().trim();
            if (!trimmed.isEmpty() && text6.equalsIgnoreCase(trimmed)) {
               this.handle(text5);
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private void handle(String[] text2) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         if (text2.length < 3) {
            this.show(client, Text.literal("Usage: /" + text2[0] + " <player> <amount>").withColor(16733525));
            this.fail(client);
         } else {
            String text4 = text2[1];
            double d = Amounts.parse(text2[2]);
            if (Double.isNaN(d) || d <= 0.0) {
               this.show(client, Text.literal("Invalid amount: " + text2[2]).withColor(16733525));
               this.fail(client);
            } else if (this.selfGuard.get() && text4.equalsIgnoreCase(client.player.getGameProfile().name())) {
               this.show(client, Text.literal("You can't pay yourself!").withColor(16733525));
               this.fail(client);
            } else {
               FakeStatsModule fakeStatsModule = this.stats();
               boolean enabled = fakeStatsModule != null && fakeStatsModule.isEnabled() && fakeStatsModule.deductOnPay.get();
               if (enabled && this.checkBalance.get() && fakeStatsModule.getLiveBalance() < d) {
                  this.show(client, Text.literal("You don't have enough money!").withColor(16733525));
                  this.fail(client);
               } else {
                  if (enabled) {
                     fakeStatsModule.deduct(d);
                  }

                  MutableText text = Text.empty().append(Text.literal("You paid " + text4).withColor(16777215));
                  String text5 = this.currency.get();
                  if (!text5.isEmpty()) {
                     text.append(
                           Text.literal(String.valueOf(text5))
                              .withColor(this.currencyColor.get() & 16777215)
                        )
                        .append(Text.literal(Amounts.shortForm(d)).withColor(16777215));
                  } else {
                     text.append(
                        Text.literal(String.valueOf(Amounts.shortForm(d)))
                           .withColor(16777215)
                     );
                  }

                  this.show(client, text);
                  this.succeed(client);
               }
            }
         }
      }
   }

   private void show(MinecraftClient client, Text text) {
      if (client.player != null) {
         String text2 = this.feedback.get();
         if (text2.equals("Action Bar") || text2.equals("Both")) {
            client.player.sendMessage(text, true);
         }

         if (text2.equals("Chat") || text2.equals("Both")) {
            client.player.sendMessage(text, false);
         }
      }
   }

   private void succeed(MinecraftClient client) {
      if (this.sounds.get()) {
         this.play(client, SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0F);
      }
   }

   private void fail(MinecraftClient client) {
      if (this.sounds.get()) {
         this.play(client, SoundEvents.ENTITY_VILLAGER_NO, 1.0F);
      }
   }

   private void play(MinecraftClient client, SoundEvent sound, float f) {
      client.getSoundManager().play(PositionedSoundInstance.ui(sound, f, 1.0F));
   }

   private FakeStatsModule stats() {
      ModuleManager moduleManager = SixSevenClient.modules();
      return moduleManager == null ? null : moduleManager.fakeStats;
   }
}
