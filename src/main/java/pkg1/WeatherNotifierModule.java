package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public final class WeatherNotifierModule extends Module {
   private boolean bool_2;
   private boolean bool2;
   private Object object;
   private float floatVal;

   public WeatherNotifierModule() {
      super(SwyzzyAddon.val2, "weather-notifier", "Notifies you about weather changes with a toast and sound.");
   }

   @Override
   public void run6() {
      this.object = class310.world;
      this.bool_2 = false;
      this.bool2 = false;
      this.floatVal = 0.0F;
   }

   @Override
   public void run7() {
      this.object = null;
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.world != null && class310.player != null) {
         if (class310.world != this.object) {
            this.object = class310.world;
            this.bool_2 = false;
            this.bool2 = false;
            this.floatVal = 0.0F;
         }

         boolean var2 = class310.world.isRaining();
         boolean var3 = class310.world.isThundering();
         if (var2 && !this.bool_2) {
            this.run2("Rain Started", "The sky is getting wet.", Items.WATER_BUCKET, SoundEvents.WEATHER_RAIN);
         } else if (!var2 && this.bool_2) {
            this.run2("Rain Cleared", "The sky is clear again.", Items.SUNFLOWER, SoundEvents.WEATHER_RAIN_ABOVE);
         }

         if (var3 && !this.bool2) {
            this.run2("Thunderstorm", "Lightning nearby - stay safe.", Items.LIGHTNING_ROD, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER);
         } else if (!var3 && this.bool2) {
            this.run2("Storm Ended", "Thunder has faded.", Items.SUNFLOWER, (SoundEvent)SoundEvents.AMBIENT_CAVE.value());
         }

         this.bool_2 = var2;
         this.bool2 = var3;
      }
   }

   private void run2(String var1, String var2, Item var3, SoundEvent var4) {
      float var5 = 0.85F + this.floatVal % 3.0F * 0.05F;
      this.floatVal++;
      class310.player.playSound(var4, 0.35F, var5);
      class310.player.playSound(SoundEvents.UI_TOAST_IN, 0.5F, 1.05F);
      this.run4("[%s] %s", new Object[]{var1, var2});
   }
}
