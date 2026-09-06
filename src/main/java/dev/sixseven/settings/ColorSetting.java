package dev.sixseven.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ColorSetting extends Setting<Integer> {
   public ColorSetting(String str, String str3, int n) {
      super(str, str3, n);
   }

   public int red() {
      return this.get() >> 16 & 0xFF;
   }

   public int green() {
      return this.get() >> 8 & 0xFF;
   }

   public int blue() {
      return this.get() & 0xFF;
   }

   public int alpha() {
      return this.get() >>> 24 & 0xFF;
   }

   public void setRed(int n) {
      this.set(Integer.valueOf(this.get() & -16711681 | (n & 0xFF) << 16));
   }

   public void setGreen(int n) {
      this.set(Integer.valueOf(this.get() & -65281 | (n & 0xFF) << 8));
   }

   public void setBlue(int n) {
      this.set(Integer.valueOf(this.get() & -256 | n & 0xFF));
   }

   public String hex() {
      return String.format("#%06X", this.get() & 16777215);
   }

   @Override
   public JsonElement toJson() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void fromJson(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
         this.value = jsonElement.getAsInt();
      }
   }
}
