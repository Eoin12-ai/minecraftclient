package dev.sixseven.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.lang.invoke.StringConcatFactory;
import org.lwjgl.glfw.GLFW;

public class KeybindSetting extends Setting<Integer> {
   public static final int NONE = -1;

   public KeybindSetting(String text, String str3, int bind) {
      super(text, str3, bind);
   }

   public boolean isBound() {
      return this.get() != -1;
   }

   public boolean matches(int n) {
      return this.isBound() && this.get() == n;
   }

   public String keyName() {
      int n = this.get();
      if (n == -1) {
         return "None";
      } else if (n >= 0 && n <= 7) {
         return switch (n) {
            case 0 -> "LMB";
            case 1 -> "RMB";
            case 2 -> "MMB";
            default -> n + 1 + "Qv";
         };
      } else {
         String text = GLFW.glfwGetKeyName(n, 0);
         if (text != null) {
            return text.toUpperCase();
         } else {
            return switch (n) {
               case 32 -> "SPACE";
               case 257 -> "ENTER";
               case 258 -> "TAB";
               case 259 -> "BACK";
               case 260 -> "INSERT";
               case 261 -> "DELETE";
               case 262 -> "RIGHT";
               case 263 -> "LEFT";
               case 264 -> "DOWN";
               case 265 -> "&|";
               case 266 -> "PGUP";
               case 267 -> "PGDN";
               case 268 -> "HOME";
               case 269 -> "END";
               case 280 -> "CAPS";
               case 340 -> "LSHIFT";
               case 341 -> "LCTRL";
               case 342 -> "LALT";
               case 344 -> "RSHIFT";
               case 345 -> "RCTRL";
               case 346 -> "RALT";
               default -> n >= 290 && n <= 314
               ? StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","5-">(n - 290 + 1)
               : "KEY" + n;
            };
         }
      }
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
