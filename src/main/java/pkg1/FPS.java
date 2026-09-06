package pkg1;

public enum FPS {
   ACTIVE_MODULES("active-modules", "Active Modules", -1, 35),
   WATERMARK("watermark", "Watermark", -2, 2),
   COORDINATES("coordinates", "Coordinates", -2, 16),
   FPS("fps", "FPS", -1, 16),
   CLOCK("clock", "Clock", -1, 30);

   private String key;
   private String label;
   private int defaultX;
   private int defaultY;

   private FPS(String var3, String var4, int var5, int var6) {
      this.key = var3;
      this.label = var4;
      this.defaultX = var5;
      this.defaultY = var6;
   }

   public String getString() {
      return this.key;
   }

   public String getString2() {
      return this.label;
   }

   public int getInt() {
      return this.defaultX;
   }

   public int getInt2() {
      return this.defaultY;
   }

   private static FPS[] getValArray() {
      return new FPS[]{ACTIVE_MODULES, WATERMARK, COORDINATES, FPS, CLOCK};
   }
}
