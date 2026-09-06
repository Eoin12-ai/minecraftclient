package pkg1;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ActivityChunkFinderModuleHelper8 {
   private String string = "setting";
   private String string2 = "";
   private boolean bool;
   private Consumer<Boolean> consumer;
   private Supplier<Boolean> supplier;

   public ActivityChunkFinderModuleHelper8 valOf(String var1) {
      this.string = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper8 valOf2(String var1) {
      this.string2 = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper8 valOf3(boolean var1) {
      this.bool = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper8 valOf4(Consumer<Boolean> var1) {
      this.consumer = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper8 valOf5(Supplier<Boolean> var1) {
      this.supplier = var1;
      return this;
   }

   public Setting<Boolean> getVal() {
      return new Setting<>(this.string, this.string2, this.bool, this.consumer, this.supplier);
   }
}
