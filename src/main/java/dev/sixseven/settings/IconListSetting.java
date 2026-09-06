package dev.sixseven.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.item.Item;

public class IconListSetting extends Setting<List<IconListSetting.Entry>> {
   private final Map<String, IconListSetting.Entry> byKey = new LinkedHashMap<>();

   public IconListSetting(String str, String str3) {
      super(str, str3, new ArrayList<>());
   }

   public IconListSetting.Entry add(String str, String str3, Item item, boolean value, int n) {
      IconListSetting.Entry entry = new IconListSetting.Entry(str, str3, item, value, n);
      this.value.add(entry);
      this.byKey.put(str, entry);
      return entry;
   }

   public List<IconListSetting.Entry> entries() {
      return this.value;
   }

   public IconListSetting.Entry get(String str) {
      return this.byKey.get(str);
   }

   public boolean isEnabled(String str) {
      IconListSetting.Entry entry = this.byKey.get(str);
      return entry != null && entry.enabled.get();
   }

   public int color(String str) {
      IconListSetting.Entry entry = this.byKey.get(str);
      return entry != null ? entry.color.get() : -1;
   }

   public int size() {
      return this.value.size();
   }

   public long enabledCount() {
      return this.value.stream().filter(arg -> arg.enabled.get()).count();
   }

   @Override
   public JsonElement toJson() {
      JsonArray jsonArray = new JsonArray();

      for (IconListSetting.Entry entry : this.value) {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("key", entry.key);
         jsonObject.addProperty("enabled", entry.enabled.get());
         jsonObject.addProperty("color", entry.color.get());
         jsonArray.add(jsonObject);
      }

      return jsonArray;
   }

   @Override
   public void fromJson(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonArray()) {
         for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
            if (jsonElement2.isJsonObject()) {
               JsonObject jsonObject = jsonElement2.getAsJsonObject();
               if (jsonObject.has("key")) {
                  IconListSetting.Entry entry = this.byKey.get(jsonObject.get("key").getAsString());
                  if (entry != null) {
                     if (jsonObject.has("enabled")) {
                        entry.enabled.set(Boolean.valueOf(jsonObject.get("enabled").getAsBoolean()));
                     }

                     if (jsonObject.has("color")) {
                        entry.color.set(Integer.valueOf(jsonObject.get("color").getAsInt()));
                     }
                  }
               }
            }
         }
      }
   }

   public static final class Entry {
      private final String key;
      private final String label;
      private final Item icon;
      public final BooleanSetting enabled;
      public final ColorSetting color;

      Entry(String str, String str2, Item item, boolean flag, int n) {
         this.key = str;
         this.label = str2;
         this.icon = item;
         this.enabled = new BooleanSetting("Enabled", "Highlight this type", flag);
         this.color = new ColorSetting(str2, "Highlight color", n);
      }

      public String key() {
         return this.key;
      }

      public String label() {
         return this.label;
      }

      public Item icon() {
         return this.icon;
      }

      public boolean matches(String str) {
         return this.label.toLowerCase(Locale.ROOT).contains(str) || this.key.contains(str);
      }
   }
}
