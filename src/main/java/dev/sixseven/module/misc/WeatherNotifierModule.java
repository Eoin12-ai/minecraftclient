package dev.sixseven.module.misc;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.notification.NotificationManager;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class WeatherNotifierModule extends Module {
   public final BooleanSetting rain = this.addSetting(new BooleanSetting("Rain", "Announce when rain starts/stops", true));
   public final BooleanSetting thunder = this.addSetting(new BooleanSetting("Thunder", "Announce when a thunderstorm starts/stops", true));
   public final BooleanSetting sound = this.addSetting(new BooleanSetting("Sound", "Play a ping on each change", true));
   public final ModeSetting output = this.addSetting(new ModeSetting("Output", "How the change is shown", "Notification", "Notification", "Chat", "Action Bar"));
   private Boolean lastRaining;
   private Boolean lastThundering;

   public WeatherNotifierModule() {
      super("WeatherNotifier", "Themed toast when the weather changes", Category.MISC);
   }

   @Override
   protected void onEnable() {
      this.lastRaining = null;
      this.lastThundering = null;
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientWorld world = client.world;
      if (world != null && client.player != null) {
         boolean found = world.isRaining();
         boolean found2 = world.isThundering();
         if (this.lastRaining != null && this.lastThundering != null) {
            if (found2 != this.lastThundering) {
               this.lastThundering = found2;
               if (this.thunder.get()) {
                  if (found2) {
                     this.notify(client, "Thunderstorm", "A storm rolls in", NotificationManager.Weather.THUNDER, true);
                  } else {
                     this.notify(client, "Storm cleared", "The thunder has passed", NotificationManager.Weather.CLEAR, false);
                  }
               }
            }

            if (found != this.lastRaining) {
               this.lastRaining = found;
               if (this.rain.get() && !found2) {
                  if (found) {
                     this.notify(client, "Rain", "Rain starts to fall", NotificationManager.Weather.RAIN, true);
                  } else {
                     this.notify(client, "Skies cleared", "The rain has stopped", NotificationManager.Weather.CLEAR, false);
                  }
               }
            }
         } else {
            this.lastRaining = found;
            this.lastThundering = found2;
         }
      }
   }

   private void notify(MinecraftClient client, String str, String str3, NotificationManager.Weather weather, boolean value) {
      if (client.player != null) {
         if (this.output.is("Notification")) {
            if (SixSevenClient.notifications() != null) {
               SixSevenClient.notifications().pushWeather(str, str3, weather, value);
            }
         } else {
            boolean found = this.output.is("Action Bar");
            client.player.sendMessage(Text.literal("§d[67] §f" + str + " — " + str3), found);
         }

         if (this.sound.get()) {
            float f = value ? 1.2F : 0.8F;
            client.getSoundManager().play(PositionedSoundInstance.ui(RegistryEntry.of(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value()), f));
         }
      }
   }
}
