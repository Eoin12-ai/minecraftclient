package dev.kryptic.module.misc;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.settings.StringSetting;
import java.lang.invoke.StringConcatFactory;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;

public class AutoTpaModule extends Module {
   public final ModeSetting mode = this.addSetting(new ModeSetting("Mode", "Which request to send.", "TPA", "TPA", "TPAHere"));
   public final StringSetting target = this.addSetting(new StringSetting("Target", "Player to send the request to.", "", 32, "Steve"));
   public final SliderSetting delay = this.addSetting(
      new SliderSetting("Delay", "Time between requests — lower is faster.", 2000.0, 250.0, 10000.0, 50.0, "ms")
   );
   public final SliderSetting humanize = this.addSetting(
      new SliderSetting("Humanize", "Random +/- swing on each delay so the timing isn't a fixed, bot-like interval. 0 = off.", 25.0, 0.0, 60.0, 5.0, "V")
   );
   public final BooleanSetting notify = this.addSetting(new BooleanSetting("Notify", "Show a notification each time a request is sent.", false));
   private long nextSendAtMs = -1L;
   private String lastSent;

   public AutoTpaModule() {
      super("AutoTPA", "Spams TPA requests at a target on a humanized timer.", Category.MISC);
   }

   @Override
   protected void onEnable() {
      if (this.target.get().trim().isEmpty()) {
         KrypticClient.notifications().pushInfo("AutoTPA · set a Target first");
      }

      this.lastSent = null;
      this.nextSendAtMs = 0L;
   }

   @Override
   protected void onDisable() {
      this.nextSendAtMs = -1L;
   }

   @Override
   public void onTick() {
      if (this.nextSendAtMs >= 0L) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null && client.player.networkHandler != null && System.currentTimeMillis() >= this.nextSendAtMs) {
            String trimmed = this.target.get().trim();
            if (trimmed.isEmpty()) {
               this.nextSendAtMs = this.scheduleNext();
            } else {
               String text4 = this.mode.is("TPAHere") ? "tpahere " : "tpa ";
               String text = text4 + trimmed;
               client.player.networkHandler.sendChatCommand(text);
               this.lastSent = text;
               if (this.notify.get()) {
                  KrypticClient.notifications().pushInfo("AutoTPA · /" + text);
               }

               this.nextSendAtMs = this.scheduleNext();
            }
         }
      }
   }

   public String lastSent() {
      return this.lastSent;
   }

   private long scheduleNext() {
      double d = this.delay.get();
      double coord = this.humanize.get() / 100.0;
      double currentScore = coord <= 0.0 ? 1.0 : 1.0 + (ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0) * coord;
      long l = Math.max(0L, Math.round(d * currentScore));
      return System.currentTimeMillis() + l;
   }
}
