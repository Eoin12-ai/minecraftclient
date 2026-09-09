package dev.sixseven.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sixseven.SixSevenClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigStore {
   public static final int SLOT_COUNT = 5;
   public static final int CONFIG_VERSION = 1;
   public static final String FORMAT = "epsteinclient-config";
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   public static volatile boolean applying;
   private final ConfigManager config;
   private final Path dir;
   private final ConfigStore.Slot[] slots = new ConfigStore.Slot[5];
   private int active = -1;

   public ConfigStore(ConfigManager configManager) {
      this.config = configManager;
      this.dir = FabricLoader.getInstance().getConfigDir().resolve("sixsevenclient-configs");

      for (int n = 0; n < 5; n++) {
         this.slots[n] = new ConfigStore.Slot(n);
      }
   }

   public static String defaultName(int n) {
      return "Config " + (n + 1);
   }

   public ConfigStore.Slot slot(int slot2) {
      return this.slots[slot2];
   }

   public ConfigStore.Slot[] slots() {
      return this.slots;
   }

   public int activeIndex() {
      return this.active;
   }

   public Path directory() {
      return this.dir;
   }

   public void loadAll() {
      for (int n = 0; n < 5; n++) {
         this.refreshSlot(n);
      }

      JsonObject jsonObject = this.readJson(this.dir.resolve("index.json"));
      if (jsonObject != null && jsonObject.has("active")) {
         int offset = jsonObject.get("active").getAsInt();
         if (offset >= 0 && offset < 5 && this.slots[offset].filled) {
            this.active = offset;
         }
      }
   }

   private void refreshSlot(int slot2) {
      ConfigStore.Slot slot3 = this.slots[slot2];
      JsonObject jsonObject = this.readJson(this.slotPath(slot2));
      JsonObject jsonObject2 = this.extractState(jsonObject);
      if (jsonObject2 == null) {
         slot3.filled = false;
         slot3.savedAt = 0L;
         slot3.name = defaultName(slot2);
      } else {
         slot3.filled = true;
         slot3.name = jsonObject.has("name") && !jsonObject.get("name").getAsString().isBlank() ? jsonObject.get("name").getAsString() : defaultName(slot2);
         slot3.savedAt = jsonObject.has("savedAt") ? jsonObject.get("savedAt").getAsLong() : 0L;
      }
   }

   public boolean save(int n) {
      ConfigStore.Slot slot2 = this.slots[n];
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("format", "epsteinclient-config");
      jsonObject.addProperty("version", 1);
      jsonObject.addProperty("name", slot2.name);
      jsonObject.addProperty("savedAt", System.currentTimeMillis());
      jsonObject.addProperty("client", "1.6.2");
      jsonObject.add("state", this.config.captureState());
      if (!this.write(this.slotPath(n), jsonObject)) {
         return false;
      } else {
         this.refreshSlot(n);
         return true;
      }
   }

   public boolean activate(int n) {
      JsonObject jsonObject = this.extractState(this.readJson(this.slotPath(n)));
      if (jsonObject == null) {
         return false;
      } else {
         applying = true;

         try {
            this.config.applyState(jsonObject);
         } finally {
            applying = false;
         }

         this.active = n;
         this.writeIndex();
         return true;
      }
   }

   public boolean delete(int n) {
      try {
         Files.deleteIfExists(this.slotPath(n));
      } catch (IOException ex) {
         SixSevenClient.LOGGER.error("Failed to delete config file {}", this.slotPath(n).getFileName(), ex);
         return false;
      }

      if (this.active == n) {
         this.active = -1;
         this.writeIndex();
      }

      this.refreshSlot(n);
      return true;
   }

   public boolean rename(int n, String json) {
      ConfigStore.Slot slot2 = this.slots[n];
      String text3 = this.sanitizeName(json);
      if (text3.isEmpty()) {
         text3 = defaultName(n);
      }

      slot2.name = text3;
      if (!slot2.filled) {
         return true;
      } else {
         JsonObject jsonObject = this.readJson(this.slotPath(n));
         if (jsonObject == null) {
            return false;
         } else {
            jsonObject.addProperty("name", text3);
            return this.write(this.slotPath(n), jsonObject);
         }
      }
   }

   public String export(int n) {
      JsonObject jsonObject = this.readJson(this.slotPath(n));
      if (this.extractState(jsonObject) == null) {
         return null;
      } else {
         String json = GSON.toJson(jsonObject);
         String text3 = this.sanitizeFileName(this.slots[n].name);
         this.write(this.dir.resolve(text3 + ".json"), jsonObject);
         return json;
      }
   }

   public ConfigStore.ImportResult importInto(int n, String json) {
      if (json != null && !json.isBlank()) {
         JsonObject jsonObject;
         try {
            jsonObject = JsonParser.parseString(json).getAsJsonObject();
         } catch (Exception ex) {
            return new ConfigStore.ImportResult(false, "Not valid config JSON");
         }

         if (jsonObject.has("version") && jsonObject.get("version").isJsonPrimitive() && jsonObject.get("version").getAsInt() > 1) {
            return new ConfigStore.ImportResult(false, "Config is from a newer client");
         } else {
            JsonObject jsonObject2 = this.extractState(jsonObject);
            if (jsonObject2 == null) {
               return new ConfigStore.ImportResult(false, "No config data found");
            } else {
               ConfigStore.Slot slot2 = this.slots[n];
               String text3 = jsonObject.has("name") && !jsonObject.get("name").getAsString().isBlank() ? this.sanitizeName(jsonObject.get("name").getAsString()) : slot2.name;
               JsonObject jsonObject3 = new JsonObject();
               jsonObject3.addProperty("format", "epsteinclient-config");
               jsonObject3.addProperty("version", 1);
               jsonObject3.addProperty("name", text3);
               jsonObject3.addProperty("savedAt", jsonObject.has("savedAt") ? jsonObject.get("savedAt").getAsLong() : System.currentTimeMillis());
               if (jsonObject.has("client")) {
                  jsonObject3.addProperty("client", jsonObject.get("client").getAsString());
               }

               jsonObject3.add("state", jsonObject2);
               if (!this.write(this.slotPath(n), jsonObject3)) {
                  return new ConfigStore.ImportResult(false, "Couldn't write the slot file");
               } else {
                  this.refreshSlot(n);
                  return new ConfigStore.ImportResult(true, "Imported \"" + slot2.name + "\"");
               }
            }
         }
      } else {
         return new ConfigStore.ImportResult(false, "Clipboard is empty");
      }
   }

   private JsonObject extractState(JsonObject jsonObject) {
      if (jsonObject == null) {
         return null;
      } else if (jsonObject.has("state") && jsonObject.get("state").isJsonObject()) {
         return jsonObject.getAsJsonObject("state");
      } else {
         return jsonObject.has("modules") && jsonObject.get("modules").isJsonObject() ? jsonObject : null;
      }
   }

   private Path slotPath(int slot2) {
      return this.dir.resolve("slot" + (slot2 + 1) + ".json");
   }

   private void writeIndex() {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("active", this.active);
      this.write(this.dir.resolve("index.json"), jsonObject);
   }

   private JsonObject readJson(Path path) {
      if (!Files.exists(path)) {
         return null;
      } else {
         try {
            return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
         } catch (Exception ex) {
            SixSevenClient.LOGGER.warn("Ignoring unreadable config file {}", path.getFileName(), ex);
            return null;
         }
      }
   }

   private boolean write(Path path, JsonObject jsonObject) {
      try {
         Files.createDirectories(this.dir);
         Files.writeString(path, GSON.toJson(jsonObject));
         return true;
      } catch (IOException ex) {
         SixSevenClient.LOGGER.error("Failed to write config file {}", path.getFileName(), ex);
         return false;
      }
   }

   /**
    * Trims a user-typed config name down to what is safe to show and store.
    *
    * The pattern here was decompiler wreckage with an unterminated group, so
    * this threw PatternSyntaxException the moment anyone named a config.
    */
   private String sanitizeName(String json) {
      if (json == null) {
         return "";
      } else {
         String text3 = json.replaceAll("[^A-Za-z0-9 ._-]", "").trim();
         return text3.length() > 24 ? text3.substring(0, 24) : text3;
      }
   }

   /**
    * Turns a config name into a filename.
    *
    * The pattern here matched a fixed run of mojibake, so it stripped nothing:
    * a name carrying a slash or a run of dots went straight into the path.
    * Anything outside the allowlist is dropped, and a name that is all dots
    * cannot survive as one.
    */
   private String sanitizeFileName(String json) {
      if (json == null) {
         return "config";
      }
      String text3 = json.replaceAll("[^A-Za-z0-9 ._-]", "").trim().replace(' ', '_');
      while (text3.startsWith(".")) {
         text3 = text3.substring(1);
      }
      return text3.isEmpty() ? "config" : text3;
   }

   public static record ImportResult(boolean ok, String message) {
   }

   public static final class Slot {
      private final int index;
      private String name;
      private boolean filled;
      private long savedAt;

      Slot(int n) {
         this.index = n;
         this.name = ConfigStore.defaultName(n);
      }

      public int index() {
         return this.index;
      }

      public String name() {
         return this.name;
      }

      public boolean filled() {
         return this.filled;
      }

      public long savedAt() {
         return this.savedAt;
      }
   }
}
