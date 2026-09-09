package dev.kryptic.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kryptic.KrypticClient;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.settings.Setting;
import dev.kryptic.theme.ThemeManager;
import java.io.IOException;
import java.lang.invoke.StringConcatFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigManager {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final Path file = FabricLoader.getInstance().getConfigDir().resolve("krypticclient.json");
   /** Where settings lived before the rename; read once, if the new file is absent. */
   private final Path legacyFile = FabricLoader.getInstance().getConfigDir().resolve("sixsevenclient.json");
   private final ModuleManager modules;
   private final ThemeManager themes;
   private final Map<String, ConfigManager.Section> sections = new LinkedHashMap<>();

   public ConfigManager(ModuleManager moduleManager, ThemeManager themeManager) {
      this.modules = moduleManager;
      this.themes = themeManager;
   }

   public void addSection(String name2, Supplier<JsonObject> supplier, Consumer<JsonObject> consumer) {
      this.sections.put(name2, new ConfigManager.Section(supplier, consumer));
   }

   public JsonObject captureState() {
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("theme", this.themes.toJson());
      JsonObject jsonObject2 = new JsonObject();

      for (Module module : this.modules.all()) {
         JsonObject jsonObject3 = new JsonObject();
         jsonObject3.addProperty("enabled", module.isEnabled());
         jsonObject3.add("keybind", module.getKeybind().toJson());
         JsonObject jsonObject4 = new JsonObject();

         for (Setting setting : module.getSettings()) {
            jsonObject4.add(setting.getName(), setting.toJson());
         }

         jsonObject3.add("settings", jsonObject4);
         jsonObject2.add(module.getName() + module.getCategory().name(), jsonObject3);
      }

      jsonObject.add("modules", jsonObject2);

      for (Entry entry : this.sections.entrySet()) {
         jsonObject.add((String)entry.getKey(), (JsonElement)((ConfigManager.Section)entry.getValue()).save().get());
      }

      return jsonObject;
   }

   public void applyState(JsonObject jsonObject) {
      if (jsonObject != null) {
         if (jsonObject.has("theme") && jsonObject.get("theme").isJsonObject()) {
            this.themes.fromJson(jsonObject.getAsJsonObject("theme"));
         }

         if (jsonObject.has("modules") && jsonObject.get("modules").isJsonObject()) {
            JsonObject jsonObject2 = jsonObject.getAsJsonObject("modules");

            for (Module module : this.modules.all()) {
               String name2 = module.getName();
               JsonObject jsonObject3 = jsonObject2.getAsJsonObject(
                  name2 + module.getCategory().name()
               );
               if (jsonObject3 == null) {
                  jsonObject3 = legacyEntry(jsonObject2, name2);
               }
               if (jsonObject3 != null) {
                  if (jsonObject3.has("enabled") && jsonObject3.get("enabled").getAsBoolean() != module.isEnabled()) {
                     module.setEnabled(jsonObject3.get("enabled").getAsBoolean());
                  }

                  if (jsonObject3.has("keybind")) {
                     module.getKeybind().fromJson(jsonObject3.get("keybind"));
                  }

                  JsonObject jsonObject4 = jsonObject3.getAsJsonObject("settings");
                  if (jsonObject4 != null) {
                     for (Setting setting : module.getSettings()) {
                        if (jsonObject4.has(setting.getName())) {
                           setting.fromJson(jsonObject4.get(setting.getName()));
                        }
                     }
                  }
               }
            }
         }

         for (Entry entry : this.sections.entrySet()) {
            if (jsonObject.has((String)entry.getKey()) && jsonObject.get((String)entry.getKey()).isJsonObject()) {
               ((ConfigManager.Section)entry.getValue()).load().accept(jsonObject.getAsJsonObject((String)entry.getKey()));
            }
         }
      }
   }

   /**
    * Module state saved under a previous name or category.
    *
    * The key is the display name and the category glued together, so both
    * renaming a module and moving it between columns orphan everything it had
    * saved. Rather than let a rename silently reset a module to defaults, the
    * old keys are looked up as a fallback -- and the category is not assumed,
    * since several of these moved column as well as changing name.
    */
   private static final Map<String, String> LEGACY_NAMES = Map.ofEntries(
         Map.entry("Aim Assist", "AimAssist"),
         Map.entry("Hit Box", "HitBox"),
         Map.entry("Trigger Bot", "Triggerbot"),
         Map.entry("Block Entity ESP", "BlockEntityESP"),
         Map.entry("Block ESP", "BlockESP"),
         Map.entry("Chunk Borders", "ChunkBorders"),
         Map.entry("Debug Hole ESP", "DebugHoleESP"),
         Map.entry("Full Bright", "FullBright"),
         Map.entry("Mob ESP", "MobESP"),
         Map.entry("Player ESP", "PlayerESP"),
         Map.entry("Region Map", "RegionMap"),
         Map.entry("Spawner Nametags", "SpawnerNametags"),
         Map.entry("Storage ESP", "StorageESP"),
         Map.entry("Sus Chunk Finder", "SusChunkFinder"),
         Map.entry("Armor Trim Hider", "ArmorTrimHider"),
         Map.entry("Auto TPA", "AutoTPA"),
         Map.entry("Auto Walk", "AutoWalk"),
         Map.entry("Coord Snapper", "CoordSnapper"),
         Map.entry("Custom Crosshair", "CustomCrosshair"),
         Map.entry("Custom FOV", "CustomFOV"),
         Map.entry("Custom Glint", "CustomGlint"),
         Map.entry("Fake Pay", "FakePay"),
         Map.entry("Fake Roles", "FakeRoles"),
         Map.entry("Fake Stats", "FakeStats"),
         Map.entry("Fast Use", "FastUse"),
         Map.entry("Free Look", "FreeLook"),
         Map.entry("Key Sounds", "KeySounds"),
         Map.entry("Name Protect", "NameProtect"),
         Map.entry("Name Tags", "NameTags"),
         Map.entry("Skin Protect", "SkinProtect"),
         Map.entry("Spawner Protect", "SpawnerProtect"),
         Map.entry("Staff List", "StaffList"),
         Map.entry("Weather Notifier", "WeatherNotifier"),
         Map.entry("Click GUI", "ClickGUI"),
         Map.entry("Discord RPC", "DiscordRPC"),
         Map.entry("Spotify HUD", "SpotifyHUD"),
         Map.entry("Chat Macro", "ChatMacro"),
         Map.entry("Server Configs", "ServerConfigs"),
         Map.entry("Jump Circles", "KrypticJumpCircles"),
         Map.entry("Swing Speed", "SwingSpeed"),
         Map.entry("Accessories", "CustomAccessories"),
         Map.entry("Block Outline", "CustomBlockOutline"),
         Map.entry("Motion Blur", "MotionBlur"),
         Map.entry("Config Share", "ConfigShare"),
         Map.entry("Media Icons", "Media/StaffNames/Icons"));

   private static JsonObject legacyEntry(JsonObject modules, String currentName) {
      String old = LEGACY_NAMES.get(currentName);
      if (old == null) return null;
      for (Category category : Category.values()) {
         JsonObject found = modules.getAsJsonObject(old + category.name());
         if (found != null) return found;
      }
      return null;
   }

   public synchronized void save() {
      try {
         Files.createDirectories(this.file.getParent());
         Files.writeString(this.file, GSON.toJson(this.captureState()));
      } catch (IOException ex) {
         KrypticClient.LOGGER.error("Failed to save config", ex);
      }
   }

   /**
    * Loads settings, falling back to the pre-rename file the first time.
    *
    * The config path is derived from the mod id, so renaming the mod moved it
    * and every saved setting would silently revert to defaults. The old file is
    * read when the new one does not exist yet and is left where it is rather
    * than deleted, so downgrading back to a previous build still finds it.
    */
   public synchronized void load() {
      Path source = Files.exists(this.file) ? this.file
            : (Files.exists(this.legacyFile) ? this.legacyFile : null);
      if (source == null) return;

      JsonObject jsonObject;
      try {
         jsonObject = JsonParser.parseString(Files.readString(source)).getAsJsonObject();
      } catch (Exception ex) {
         KrypticClient.LOGGER.error("Failed to read config, using defaults", ex);
         return;
      }

      if (source == this.legacyFile) {
         KrypticClient.LOGGER.info("Carried settings over from {}", this.legacyFile.getFileName());
      }
      this.applyState(jsonObject);
   }

   public static record Section(Supplier<JsonObject> save, Consumer<JsonObject> load) {
   }
}
