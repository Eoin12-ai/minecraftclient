package dev.kryptic.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.List;

public class ModeSetting extends Setting<String> {
   private final List<String> modes;

   public ModeSetting(String str, String str4, String str5, String... temp) {
      super(str, str4, str5);
      this.modes = List.of(temp);
      if (!this.modes.contains(str5)) {
         throw new IllegalArgumentException("Default mode '" + str5 + "' not in modes for " + str);
      }
   }

   public List<String> getModes() {
      return this.modes;
   }

   public boolean is(String str) {
      return this.get().equals(str);
   }

   public void cycle() {
      int n = (this.modes.indexOf(this.get()) + 1) % this.modes.size();
      this.set(this.modes.get(n));
   }

   @Override
   public JsonElement toJson() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void fromJson(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonPrimitive() && this.modes.contains(jsonElement.getAsString())) {
         this.value = jsonElement.getAsString();
      }
   }
}
