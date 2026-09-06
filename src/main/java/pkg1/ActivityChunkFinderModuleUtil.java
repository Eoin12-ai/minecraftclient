package pkg1;

import net.minecraft.world.chunk.Chunk;
import util.ListUtils;

public final class ActivityChunkFinderModuleUtil {
   private ActivityChunkFinderModuleUtil() {
   }

   public static Iterable<Chunk> getIterable() {
      return ListUtils.getList();
   }
}
