package pkg1;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AutoTotemModuleHelper2 {
   private String string = "setting";
   private String string2 = "";
   private double doubleVal;
   private Consumer<Double> consumer;
   private Supplier<Boolean> supplier;
   private double doubleVal2 = -Double.MAX_VALUE;
   private double doubleVal3 = Double.MAX_VALUE;

   public AutoTotemModuleHelper2 valOf(String var1) {
      this.string = var1;
      return this;
   }

   public AutoTotemModuleHelper2 valOf2(String var1) {
      this.string2 = var1;
      return this;
   }

   public AutoTotemModuleHelper2 valOf3(double var1) {
      this.doubleVal = var1;
      return this;
   }

   public AutoTotemModuleHelper2 valOf4(double var1, double var3) {
      this.doubleVal2 = var1;
      this.doubleVal3 = var3;
      return this;
   }

   public AutoTotemModuleHelper2 valOf5(double var1, double var3) {
      this.doubleVal2 = var1;
      this.doubleVal3 = var3;
      return this;
   }

   public AutoTotemModuleHelper2 valOf6(Consumer<Double> var1) {
      this.consumer = var1;
      return this;
   }

   public AutoTotemModuleHelper2 valOf7(Supplier<Boolean> var1) {
      this.supplier = var1;
      return this;
   }

   public Setting<Double> getVal() {
      return new Setting<>(this.string, this.string2, this.doubleVal, this.consumer, this.supplier).valOf(this.doubleVal2, this.doubleVal3);
   }
}
