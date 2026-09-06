package pkg1;

public class ActivityChunkFinderModuleEntry2 {
   public int intVal;
   public int intVal2;
   public int intVal3;
   public int intVal4;

   public ActivityChunkFinderModuleEntry2(int var1, int var2, int var3, int var4) {
      this.intVal = intOf(var1);
      this.intVal2 = intOf(var2);
      this.intVal3 = intOf(var3);
      this.intVal4 = intOf(var4);
   }

   public ActivityChunkFinderModuleEntry2(ActivityChunkFinderModuleEntry2 var1) {
      this(var1.intVal, var1.intVal2, var1.intVal3, var1.intVal4);
   }

   private static int intOf(int var0) {
      return Math.max(0, Math.min(255, var0));
   }

   public int getInt() {
      return (this.intVal4 & 0xFF) << 24 | (this.intVal & 0xFF) << 16 | (this.intVal2 & 0xFF) << 8 | this.intVal3 & 0xFF;
   }
}
