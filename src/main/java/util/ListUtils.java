package util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

public final class ListUtils {
   private static final Map<Long, WorldChunk> map = new ConcurrentHashMap<>();

   private ListUtils() {
   }

   public static void run(WorldChunk var0) {
      map.put(var0.getPos().toLong(), var0);
   }

   public static void run2(WorldChunk var0) {
      map.remove(var0.getPos().toLong());
   }

   public static List<Chunk> getList() {
      return List.copyOf(map.values());
   }

   public static void run3() {
      map.clear();
   }
}
