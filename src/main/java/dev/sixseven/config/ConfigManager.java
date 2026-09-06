package dev.sixseven.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sixseven.SixSevenClient;
import dev.sixseven.module.Module;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.settings.Setting;
import dev.sixseven.theme.ThemeManager;
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
   private final Path file = FabricLoader.getInstance().getConfigDir().resolve("sixsevenclient.json");
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
         jsonObject2.add(String.valueOf(module.getName(), module.getCategory().name()), jsonObject3);
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
                  name2 + module.getCategory(.name())
               );
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

   public synchronized void save() {
      try {
         Files.createDirectories(this.file.getParent());
         Files.writeString(this.file, GSON.toJson(this.captureState()));
      } catch (IOException ex) {
         SixSevenClient.LOGGER.error("Failed to save config", ex);
      }
   }

   public synchronized void load() {
      if (Files.exists(this.file)) {
         JsonObject jsonObject;
         try {
            jsonObject = JsonParser.parseString(Files.readString(this.file)).getAsJsonObject();
         } catch (Exception ex) {
            SixSevenClient.LOGGER.error("Failed to read config, using defaults", ex);
            return;
         }

         this.applyState(jsonObject);
      }
   }

   public static record Section(Supplier<JsonObject> save, Consumer<JsonObject> load) {
   }
}
