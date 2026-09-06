package pkg1;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.block.Block;

public final class BlockEspPlusModuleHelper2 {
   private String string = "setting";
   private String string2 = "";
   private List<Block> list = new ArrayList<>();
   private Consumer<List<Block>> consumer;
   private Supplier<Boolean> supplier;

   public BlockEspPlusModuleHelper2 valOf(String var1) {
      this.string = var1;
      return this;
   }

   public BlockEspPlusModuleHelper2 valOf2(String var1) {
      this.string2 = var1;
      return this;
   }

   public BlockEspPlusModuleHelper2 valOf3(List<Block> var1) {
      this.list = var1;
      return this;
   }

   public BlockEspPlusModuleHelper2 valOf4(Consumer<List<Block>> var1) {
      this.consumer = var1;
      return this;
   }

   public BlockEspPlusModuleHelper2 valOf5(Supplier<Boolean> var1) {
      this.supplier = var1;
      return this;
   }

   public Setting<List<Block>> getVal() {
      return new Setting<>(this.string, this.string2, this.list, this.consumer, this.supplier).valOf2(Block.class);
   }
}
