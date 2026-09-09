package dev.kryptic.theme;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
   /** #F5A524 — the client's own accent, and the default theme. */
   public static final int AMBER = -678620;
   private final List<Theme> themes = new ArrayList<>();
   private Theme current;

   public ThemeManager() {
      this.themes.add(new Theme("Amber", AMBER, false));
      this.themes.add(new Theme("Ember", -45715, false));      // was "!I," — decompiler garbage
      this.themes.add(new Theme("Emerald", -12654960, false));
      this.themes.add(new Theme("Ice", -11689985, false));
      this.themes.add(new Theme("Violet", -5743361, false));
      this.themes.add(new RainbowTheme());
      this.current = this.themes.getFirst();
   }

   public Theme current() {
      return this.current;
   }

   public List<Theme> getThemes() {
      return this.themes;
   }

   public void select(Theme theme) {
      if (this.themes.contains(theme)) {
         this.current = theme;
      }
   }

   public Theme addCustom(int n) {
      int offset = 1;

      for (Theme theme2 : this.themes) {
         if (theme2.isCustom()) {
            offset++;
         }
      }

      Theme theme = new Theme("Custom " + offset, n, true);
      this.themes.add(theme);
      return theme;
   }

   public void removeCustom(Theme theme) {
      if (theme.isCustom() && this.themes.remove(theme) && this.current == theme) {
         this.current = this.themes.getFirst();
      }
   }

   public JsonObject toJson() {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("current", this.current.getName());
      JsonArray jsonArray = new JsonArray();

      for (Theme theme : this.themes) {
         if (theme.isCustom()) {
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("name", theme.getName());
            jsonObject2.addProperty("accent", theme.accent());
            jsonArray.add(jsonObject2);
         }
      }

      jsonObject.add("custom", jsonArray);
      return jsonObject;
   }

   public void fromJson(JsonObject jsonObject) {
      if (jsonObject != null) {
         this.themes.removeIf(Theme::isCustom);
         if (jsonObject.has("custom")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("custom")) {
               JsonObject jsonObject2 = jsonElement.getAsJsonObject();
               this.themes.add(new Theme(jsonObject2.get("name").getAsString(), jsonObject2.get("accent").getAsInt(), true));
            }
         }

         if (jsonObject.has("current")) {
            String json = jsonObject.get("current").getAsString();

            for (Theme theme : this.themes) {
               if (theme.getName().equals(json)) {
                  this.current = theme;
                  break;
               }
            }
         }
      }
   }
}
