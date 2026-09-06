package dev.sixseven.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanSetting extends Setting<Boolean> {
   public BooleanSetting(String str, String str3, boolean value) {
      super(str, str3, value);
   }

   public void toggle() {
      this.set(Boolean.valueOf(!this.get()));
   }

   @Override
   public JsonElement toJson() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void fromJson(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isBoolean()) {
         this.value = jsonElement.getAsBoolean();
      }
   }
}
