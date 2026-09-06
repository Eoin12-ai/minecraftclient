package pkg1;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ModuleHelper2 {
   private String string = "setting";
   private String string2 = "";
   private Tab val = Tab.getVal();
   private Consumer<Tab> consumer;
   private Supplier<Boolean> supplier;

   public ModuleHelper2 valOf(String var1) {
      this.string = var1;
      return this;
   }

   public ModuleHelper2 valOf2(String var1) {
      this.string2 = var1;
      return this;
   }

   public ModuleHelper2 valOf3(Tab var1) {
      this.val = var1;
      return this;
   }

   public ModuleHelper2 valOf4(Consumer<Tab> var1) {
      this.consumer = var1;
      return this;
   }

   public ModuleHelper2 valOf5(Supplier<Boolean> var1) {
      this.supplier = var1;
      return this;
   }

   public Setting<Tab> getVal() {
      return new Setting<>(this.string, this.string2, this.val, this.consumer, this.supplier);
   }
}
