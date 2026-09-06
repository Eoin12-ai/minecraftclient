package pkg1;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ActivityChunkFinderModuleHelper10 {
   private String string = "setting";
   private String string2 = "";
   private ActivityChunkFinderModuleHelper4 val = new ActivityChunkFinderModuleHelper4(255, 255, 255, 255);
   private Consumer<ActivityChunkFinderModuleHelper4> consumer;
   private Supplier<Boolean> supplier;

   public ActivityChunkFinderModuleHelper10 valOf(String var1) {
      this.string = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper10 valOf2(String var1) {
      this.string2 = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper10 valOf3(ActivityChunkFinderModuleHelper4 var1) {
      this.val = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper10 valOf4(Consumer<ActivityChunkFinderModuleHelper4> var1) {
      this.consumer = var1;
      return this;
   }

   public ActivityChunkFinderModuleHelper10 valOf5(Supplier<Boolean> var1) {
      this.supplier = var1;
      return this;
   }

   public Setting<ActivityChunkFinderModuleHelper4> getVal() {
      return new Setting<>(this.string, this.string2, this.val, this.consumer, this.supplier);
   }
}
