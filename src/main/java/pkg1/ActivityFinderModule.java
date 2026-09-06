package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public final class ActivityFinderModule extends Module {
   private final Set<ChunkPos> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private final Map<Long, Long> map = new ConcurrentHashMap<>();

   public ActivityFinderModule() {
      super(SwyzzyAddon.val2, "activity-finder", "Detects suspicious chunk activity at low Y levels.");
   }

   @Override
   public void run7() {
      this.set.clear();
      this.map.clear();
   }

   public void run(BlockPos var1) {
      if (this.isEnabled() && class310.player != null && class310.world != null) {
         if (var1.getY() <= 16) {
            ChunkPos var2 = new ChunkPos(var1);
            if (this.set.add(var2)) {
               long var3 = var2.toLong();
               long var5 = System.currentTimeMillis();
               Long var7 = this.map.put(var3, var5);
               if (var7 != null && var5 - var7 > 1250L) {
               }
            }
         }
      }
   }
}
