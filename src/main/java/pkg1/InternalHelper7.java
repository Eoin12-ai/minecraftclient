package pkg1;

final class InternalHelper7 {
   private Module module;
   private String label;
   private Setting<Integer> xSetting;
   private Setting<Integer> ySetting;
   private int intVal;
   private int intVal2;
   private int width;
   private int height;
   private int rawWidth;
   private int rawHeight;
   private boolean windowPixels;

   InternalHelper7(
      Module var1, String var2, Setting<Integer> var3, Setting<Integer> var4, int var5, int var6, int var7, int var8, int var9, int var10, boolean var11
   ) {
      this.module = var1;
      this.label = var2;
      this.xSetting = var3;
      this.ySetting = var4;
      this.intVal = var5;
      this.intVal2 = var6;
      this.width = var7;
      this.height = var8;
      this.rawWidth = var9;
      this.rawHeight = var10;
      this.windowPixels = var11;
   }

   boolean check(double var1, double var3) {
      return var1 >= this.intVal && var1 <= this.intVal + this.width && var3 >= this.intVal2 - 14 && var3 <= this.intVal2 + this.height;
   }

   boolean check2(InternalHelper7 var1) {
      return this.module == var1.module;
   }

   public Module module() {
      return this.module;
   }

   public String label() {
      return this.label;
   }

   public Setting<Integer> xSetting() {
      return this.xSetting;
   }

   public Setting<Integer> ySetting() {
      return this.ySetting;
   }

   public int getInt() {
      return this.intVal;
   }

   public int getInt2() {
      return this.intVal2;
   }

   public int width() {
      return this.width;
   }

   public int height() {
      return this.height;
   }

   public int rawWidth() {
      return this.rawWidth;
   }

   public int rawHeight() {
      return this.rawHeight;
   }

   public boolean windowPixels() {
      return this.windowPixels;
   }
}
