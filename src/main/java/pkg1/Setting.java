package pkg1;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Setting<T> {
   public String string;
   public String string2;
   private T object;
   private Consumer<T> consumer;
   private Supplier<Boolean> supplier;
   private Runnable runnable;
   private Double doubleVal;
   private Double doubleVal2;
   private Class<?> classVal;

   Setting(String var1, String var2, T var3, Consumer<T> var4, Supplier<Boolean> var5) {
      this.string = var1;
      this.string2 = var2;
      this.object = (T)var3;
      this.consumer = var4;
      this.supplier = var5;
   }

   public T getObject() {
      return this.object;
   }

   public void run2(T var1) {
      boolean var2 = !Objects.equals(this.object, var1);
      this.object = (T)var1;
      if (this.consumer != null) {
         this.consumer.accept((T)var1);
      }

      if (var2 && this.runnable != null) {
         this.runnable.run();
      }
   }

   public void run(Runnable var1) {
      this.runnable = var1;
   }

   public boolean isEnabled() {
      return this.supplier == null || this.supplier.get();
   }

   Setting<T> valOf(double var1, double var3) {
      this.doubleVal = var1;
      this.doubleVal2 = var3;
      return this;
   }

   public Double getDouble() {
      return this.doubleVal;
   }

   public Double getDouble2() {
      return this.doubleVal2;
   }

   Setting<T> valOf2(Class<?> var1) {
      this.classVal = var1;
      return this;
   }

   public Class<?> getClass0() {
      return this.classVal;
   }
}
