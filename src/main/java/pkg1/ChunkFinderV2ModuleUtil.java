package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;

public final class ChunkFinderV2ModuleUtil {
   private ChunkFinderV2ModuleUtil() {
   }

   public static void run10(String var0, String var1) {
      ToastsModule.run10(var0, var1);
      if (MinecraftClient.getInstance().player == null) {
         SwyzzyAddon.logger.info("[{}] {}", var0, var1);
      }
   }

   public static void run(MinecraftClient var0, Item var1, String var2, String var3) {
      run10(var2, var3);
   }
}
