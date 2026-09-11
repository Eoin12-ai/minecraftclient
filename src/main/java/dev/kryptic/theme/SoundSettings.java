package dev.kryptic.theme;

import com.google.gson.JsonObject;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.Setting;
import dev.kryptic.settings.SliderSetting;
import java.util.List;

public class SoundSettings {
   public final SliderSetting masterVolume = new SliderSetting("Master Volume", "Volume for all client sounds", 60.0, 0.0, 100.0, 5.0, "V");
   public final BooleanSetting guiSounds = new BooleanSetting("GUI Open/Close", "Whoosh when the menu opens and closes", true);
   public final BooleanSetting hoverSounds = new BooleanSetting("Hover", "Soft ticks when hovering elements", true);
   public final BooleanSetting clickSounds = new BooleanSetting("Clicks & Toggles", "Pops for toggles, sliders and keybinds", true);
   public final BooleanSetting notificationSounds = new BooleanSetting("Notifications", "Chimes with toggle toasts", true);
   private final List<Setting<?>> all = List.of(
      this.masterVolume,
      this.guiSounds,
      this.hoverSounds,
      this.clickSounds,
      this.notificationSounds
   );

   public List<Setting<?>> all() {
      return this.all;
   }

   public float volume() {
      return this.masterVolume.getFloat() / 100.0F;
   }

   public JsonObject toJson() {
      JsonObject jsonObject = new JsonObject();

      for (Setting setting : this.all) {
         jsonObject.add(setting.getName(), setting.toJson());
      }

      return jsonObject;
   }


   public void fromJson(JsonObject jsonObject) {
      // The rename table that used to sit here only ever held startup-track
      // names, and there are no startup tracks any more.
      for (Setting setting : this.all) {
         String saved = setting.getName();
         if (jsonObject.has(saved)) {
            setting.fromJson(jsonObject.get(saved));
         }
      }
   }
}
