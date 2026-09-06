package dev.sixseven.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringSetting extends Setting<String> {
   private final int maxLength;
   private final String placeholder;

   public StringSetting(String json, String str4, String str5) {
      this(json, str4, str5, 32, "");
   }

   public StringSetting(String json, String str5, String str6, int n, String str7) {
      super(json, str5, str6);
      this.maxLength = n;
      this.placeholder = str7;
   }

   public int getMaxLength() {
      return this.maxLength;
   }

   public String getPlaceholder() {
      return this.placeholder;
   }

   @Override
   public JsonElement toJson() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void fromJson(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonPrimitive()) {
         String json = jsonElement.getAsString();
         this.value = json.length() > this.maxLength ? json.substring(0, this.maxLength) : json;
      }
   }
}
