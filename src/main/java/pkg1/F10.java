package pkg1;

public enum F10 {
   H_2("H", 72),
   RIGHT_SHIFT("Right Shift", 344),
   INSERT("Insert", 260),
   HOME("Home", 268),
   DELETE("Delete", 261),
   F8_2("F8", 297),
   F9_2("F9", 298),
   F10("F10", 299),
   F11("F11", 300),
   F12("F12", 301);

   private String label;
   private int code;

   private F10(String var3, int var4) {
      this.label = var3;
      this.code = var4;
   }

   public String getString() {
      return this.label;
   }

   public int getInt() {
      return this.code;
   }

   private static F10[] getValArray() {
      return new F10[]{H_2, RIGHT_SHIFT, INSERT, HOME, DELETE, F8_2, F9_2, F10, F11, F12};
   }
}
