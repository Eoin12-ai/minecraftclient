package pkg1;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AdminDetectorModuleHelper2 {
   private String string = "setting";
   private String string2 = "";
   private String string3 = "";
   private Consumer<String> consumer;
   private Supplier<Boolean> supplier;

   public AdminDetectorModuleHelper2 valOf(String var1) {
      this.string = var1;
      return this;
   }

   public AdminDetectorModuleHelper2 valOf2(String var1) {
      this.string2 = var1;
      return this;
   }

   public AdminDetectorModuleHelper2 valOf3(String var1) {
      this.string3 = var1;
      return this;
   }

   public AdminDetectorModuleHelper2 valOf4(Consumer<String> var1) {
      this.consumer = var1;
      return this;
   }

   public AdminDetectorModuleHelper2 valOf5(Supplier<Boolean> var1) {
      this.supplier = var1;
      return this;
   }

   public Setting<String> getVal() {
      return new Setting<>(this.string, this.string2, this.string3, this.consumer, this.supplier);
   }
}
