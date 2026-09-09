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
   public final BooleanSetting startup67 = new BooleanSetting("Kryptic Theme", "Startup track candidate", true);
   public final BooleanSetting startupSad = new BooleanSetting("Slow Fade", "Startup track candidate", false);
   public final BooleanSetting startupSong = new BooleanSetting("Anthem", "Startup track candidate", false);
   public final BooleanSetting startupTiki = new BooleanSetting("Tiki Phonk", "Startup track candidate", false);
   private final List<Setting<?>> all = List.of(
      this.masterVolume,
      this.guiSounds,
      this.hoverSounds,
      this.clickSounds,
      this.notificationSounds,
      this.startup67,
      this.startupSad,
      this.startupSong,
      this.startupTiki
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

   /** Startup tracks saved under the names they carried before the rename. */
   private static final java.util.Map<String, String> LEGACY = java.util.Map.of(
      "Kryptic Theme", "Kryptic",
      "Slow Fade", "67 Sad Song",
      "Anthem", "67 Song",
      "Tiki Phonk", "67 Tiki Phonk");

   public void fromJson(JsonObject jsonObject) {
      for (Setting setting : this.all) {
         String saved = setting.getName();
         if (!jsonObject.has(saved)) {
            saved = LEGACY.get(saved);
         }
         if (saved != null && jsonObject.has(saved)) {
            setting.fromJson(jsonObject.get(saved));
         }
      }
   }
}
