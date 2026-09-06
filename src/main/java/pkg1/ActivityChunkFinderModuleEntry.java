package pkg1;

import java.util.ArrayList;
import java.util.List;

public final class ActivityChunkFinderModuleEntry {
   public String string;
   private final List<Setting<?>> list = new ArrayList<>();

   ActivityChunkFinderModuleEntry(String var1) {
      this.string = var1;
   }

   public <T> Setting<T> addSetting(Setting<T> var1) {
      this.list.add(var1);
      return var1;
   }

   public List<Setting<?>> getList() {
      return List.copyOf(this.list);
   }
}
