package pkg1;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.entity.EntityType;

public final class PrimeChunkFinderModuleHelper2 {
   private String string = "setting";
   private String string2 = "";
   private List<EntityType<?>> list = new ArrayList<>();
   private Consumer<List<EntityType<?>>> consumer;
   private Supplier<Boolean> supplier;

   public PrimeChunkFinderModuleHelper2 valOf(String var1) {
      this.string = var1;
      return this;
   }

   public PrimeChunkFinderModuleHelper2 valOf2(String var1) {
      this.string2 = var1;
      return this;
   }

   public PrimeChunkFinderModuleHelper2 valOf3(List<EntityType<?>> var1) {
      this.list = var1;
      return this;
   }

   public PrimeChunkFinderModuleHelper2 valOf4(EntityType<?>[] var1) {
      this.list = new ArrayList<>(List.of(var1));
      return this;
   }

   public PrimeChunkFinderModuleHelper2 valOf5(Predicate<EntityType<?>> var1) {
      return this;
   }

   public PrimeChunkFinderModuleHelper2 valOf6(Consumer<List<EntityType<?>>> var1) {
      this.consumer = var1;
      return this;
   }

   public PrimeChunkFinderModuleHelper2 valOf7(Supplier<Boolean> var1) {
      this.supplier = var1;
      return this;
   }

   public Setting<List<EntityType<?>>> getVal() {
      return new Setting<>(this.string, this.string2, this.list, this.consumer, this.supplier);
   }
}
