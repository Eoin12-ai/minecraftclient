package pkg1;

public enum Red {
   Purple("Purple", 11822335),
   Blue("Blue", 4886015),
   Cyan("Cyan", 2742760),
   Green("Green", 4381050),
   Red("Red", 15749714),
   Orange("Orange", 16750141),
   Pink("Pink", 15817653),
   White("White", 15265012);

   private String label;
   private int rgb;

   private Red(String var3, int var4) {
      this.label = var3;
      this.rgb = var4;
   }

   public String getString() {
      return this.label;
   }

   public int getInt() {
      return this.rgb;
   }

   public int intOf(int var1) {
      return (var1 & 0xFF) << 24 | this.rgb;
   }

   private static Red[] getValArray() {
      return new Red[]{Purple, Blue, Cyan, Green, Red, Orange, Pink, White};
   }
}
