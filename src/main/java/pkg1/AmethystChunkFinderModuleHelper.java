package pkg1;

import net.minecraft.world.chunk.WorldChunk;

public final class AmethystChunkFinderModuleHelper {
   private WorldChunk chunk;

   public AmethystChunkFinderModuleHelper(WorldChunk var1) {
      this.chunk = var1;
   }

   public WorldChunk chunk() {
      return this.chunk;
   }
}
