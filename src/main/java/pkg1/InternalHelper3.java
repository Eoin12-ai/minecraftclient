package pkg1;

import java.util.List;

final class InternalHelper3 {
   private FPS element;
   private int intVal;
   private int intVal2;
   private int width;
   private int height;
   private List<String> lines;

   InternalHelper3(FPS var1, int var2, int var3, int var4, int var5, List<String> var6) {
      this.element = var1;
      this.intVal = var2;
      this.intVal2 = var3;
      this.width = var4;
      this.height = var5;
      this.lines = var6;
   }

   boolean check(double var1, double var3) {
      return var1 >= this.intVal && var1 <= this.intVal + this.width && var3 >= this.intVal2 && var3 <= this.intVal2 + this.height;
   }

   public FPS element() {
      return this.element;
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

   public List<String> lines() {
      return this.lines;
   }
}
