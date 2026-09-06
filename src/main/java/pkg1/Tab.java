package pkg1;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.lwjgl.glfw.GLFW;

public final class Tab {
   public static final int intVal = -1;
   private static final int intVal2 = 10000;
   private static final Map<Integer, String> map = Map.ofEntries(
      Map.entry(32, "Space"),
      Map.entry(256, "Escape"),
      Map.entry(257, "Enter"),
      Map.entry(258, "Tab"),
      Map.entry(259, "Backspace"),
      Map.entry(260, "Insert"),
      Map.entry(261, "Delete"),
      Map.entry(262, "Right"),
      Map.entry(263, "Left"),
      Map.entry(264, "Down"),
      Map.entry(265, "Up"),
      Map.entry(266, "Page Up"),
      Map.entry(267, "Page Down"),
      Map.entry(268, "Home"),
      Map.entry(269, "End"),
      Map.entry(280, "Caps Lock"),
      Map.entry(281, "Scroll Lock"),
      Map.entry(282, "Num Lock"),
      Map.entry(283, "Print Screen"),
      Map.entry(284, "Pause"),
      Map.entry(340, "Left Shift"),
      Map.entry(341, "Left Control"),
      Map.entry(342, "Left Alt"),
      Map.entry(343, "Left Super"),
      Map.entry(344, "Right Shift"),
      Map.entry(345, "Right Control"),
      Map.entry(346, "Right Alt"),
      Map.entry(347, "Right Super"),
      Map.entry(348, "Menu")
   );
   private int intVal3;

   public static Tab valOf(int var0) {
      return new Tab(10000 + var0);
   }

   public boolean isEnabled() {
      return this.intVal3 >= 10000;
   }

   public int getInt() {
      return this.intVal3 - 10000;
   }

   public Tab(int var1) {
      this.intVal3 = var1;
   }

   public static Tab getVal() {
      return new Tab(-1);
   }

   public int getInt2() {
      return this.intVal3;
   }

   public boolean isEnabled2() {
      return this.intVal3 != -1;
   }

   public String getString3() {
      if (this.intVal3 == -1) {
         return "None";
      } else if (this.isEnabled()) {
         return stringOf(this.getInt());
      } else {
         String var1 = map.get(this.intVal3);
         if (var1 != null) {
            return var1;
         } else if (this.intVal3 >= 290 && this.intVal3 <= 314) {
            return "F" + (this.intVal3 - 290 + 1);
         } else if (this.intVal3 >= 320 && this.intVal3 <= 329) {
            return "Numpad " + (this.intVal3 - 320);
         } else {
            String var2 = this.getString();
            return var2 != null ? var2 : "Key " + this.intVal3;
         }
      }
   }

   private static String stringOf(int var0) {
      return "Mouse " + (var0 + 1);
   }

   private String getString() {
      try {
         String var1 = GLFW.glfwGetKeyName(this.intVal3, 0);
         return var1 != null && !var1.isBlank() ? var1.toUpperCase(Locale.ROOT) : null;
      } catch (Throwable var2) {
         return null;
      }
   }

   public static Tab valOf2(String var0) {
      if (var0 == null) {
         return getVal();
      } else {
         try {
            return new Tab(Integer.parseInt(var0.trim()));
         } catch (NumberFormatException var2) {
            return getVal();
         }
      }
   }

   @Override
   public boolean equals(Object var1) {
      return var1 instanceof Tab var2 && var2.intVal3 == this.intVal3;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.intVal3);
   }

   @Override
   public String toString() {
      return Integer.toString(this.intVal3);
   }
}
