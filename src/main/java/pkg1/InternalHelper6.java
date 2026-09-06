package pkg1;

final class InternalHelper6 {
   private HEADER kind;
   private int intVal;
   private int intVal2;
   private int width;
   private int height;
   private Module module;
   private final Setting<?> setting;

   InternalHelper6(HEADER var1, int var2, int var3, int var4, int var5, Module var6, Setting<?> var7) {
      this.kind = var1;
      this.intVal = var2;
      this.intVal2 = var3;
      this.width = var4;
      this.height = var5;
      this.module = var6;
      this.setting = var7;
   }

   boolean check(double var1, double var3) {
      return var1 >= this.intVal && var1 <= this.intVal + this.width && var3 >= this.intVal2 && var3 < this.intVal2 + this.height;
   }

   public HEADER kind() {
      return this.kind;
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

   public Module module() {
      return this.module;
   }

   public Setting<?> setting() {
      return this.setting;
   }
}
