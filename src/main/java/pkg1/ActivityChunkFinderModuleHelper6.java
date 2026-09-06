package pkg1;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ActivityChunkFinderModuleHelper6 {
   private String string = "setting";
   private String string2 = "";
   private int intVal;
   private Consumer<Integer> consumer;
   private Supplier<Boolean> supplier;
   private int intVal2 = Integer.MIN_VALUE;
   private int intVal3 = Integer.MAX_VALUE;

   public ActivityChunkFinderModuleHelper6 valOf(String var1) {
      this.string = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper6 valOf2(String var1) {
      this.string2 = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper6 valOf3(int var1) {
      this.intVal = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper6 valOf4(int var1, int var2) {
      this.intVal2 = var1;
      this.intVal3 = var2;
      return this;
   }

   public ActivityChunkFinderModuleHelper6 valOf5(int var1, int var2) {
      this.intVal2 = var1;
      this.intVal3 = var2;
      return this;
   }

   public ActivityChunkFinderModuleHelper6 valOf6(int var1) {
      this.intVal2 = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper6 valOf7(int var1) {
      this.intVal3 = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper6 valOf8(Consumer<Integer> var1) {
      this.consumer = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper6 valOf9(Supplier<Boolean> var1) {
      this.supplier = var1;
      return this;
   }

   public Setting<Integer> getVal() {
      return new Setting<>(this.string, this.string2, this.intVal, this.consumer, this.supplier).valOf(this.intVal2, this.intVal3);
   }
}
