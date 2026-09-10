package dev.kryptic.theme;

import com.google.gson.JsonArray;
import dev.kryptic.settings.BooleanSetting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
   /** Plain white — the mark is white, so the client is. */
   public static final int MONO = -1;

   /**
    * Whether world visuals follow the theme accent instead of their own colour.
    *
    * Off by default: turning it on is a deliberate choice to trade per-module
    * colours for one look, and it should never happen to someone who has spent
    * time setting those colours. Only modules whose colour is decoration
    * follow it — see ThemeColors.
    */
   public final BooleanSetting moduleColors = new BooleanSetting("Theme Drives Module Colors",
         "Single-colour modules use the theme accent. Modules whose colours mean something keep theirs.",
         false);
   private final List<Theme> themes = new ArrayList<>();
   private Theme current;

   public ThemeManager() {
      this.themes.add(new Theme("Mono", MONO, false));
      this.themes.add(new Theme("Steel", -3618608, false));    // #C8C8D0
      this.themes.add(new Theme("Amber", -678620, false));
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
            if (theme.hasCustomSurface()) {
               jsonObject2.addProperty("surface", theme.surface());
            }

            jsonArray.add(jsonObject2);
         }
      }

      jsonObject.add("custom", jsonArray);
      jsonObject.add("moduleColors", this.moduleColors.toJson());
      return jsonObject;
   }

   public void fromJson(JsonObject jsonObject) {
      if (jsonObject != null) {
         this.themes.removeIf(Theme::isCustom);
         if (jsonObject.has("custom")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("custom")) {
               JsonObject jsonObject2 = jsonElement.getAsJsonObject();
               Theme restored = new Theme(
                     jsonObject2.get("name").getAsString(), jsonObject2.get("accent").getAsInt(), true);
               // Written only when it was set, so a theme saved before surfaces
               // existed loads on the stock one rather than on black.
               if (jsonObject2.has("surface")) {
                  restored.setSurface(jsonObject2.get("surface").getAsInt());
               }

               this.themes.add(restored);
            }
         }

         if (jsonObject.has("moduleColors")) {
            this.moduleColors.fromJson(jsonObject.get("moduleColors"));
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
