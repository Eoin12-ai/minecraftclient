package pkg1;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BaseEspModuleHelper2<T extends Enum<T>> {
   private String string = "setting";
   private String string2 = "";
   private T enumVal;
   private Consumer<T> consumer;
   private Supplier<Boolean> supplier;

   public BaseEspModuleHelper2<T> valOf(String var1) {
      this.string = var1;
      return this;
   }

   public BaseEspModuleHelper2<T> valOf2(String var1) {
      this.string2 = var1;
      return this;
   }

   public BaseEspModuleHelper2<T> valOf3(T var1) {
      this.enumVal = (T)var1;
      return this;
   }

   public BaseEspModuleHelper2<T> valOf4(Consumer<T> var1) {
      this.consumer = var1;
      return this;
   }

   public BaseEspModuleHelper2<T> valOf5(Supplier<Boolean> var1) {
      this.supplier = var1;
      return this;
   }

   public Setting<T> getVal() {
      return new Setting<>(this.string, this.string2, this.enumVal, this.consumer, this.supplier);
   }
}
