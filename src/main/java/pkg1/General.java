package pkg1;

import java.util.ArrayList;
import java.util.List;

public final class General {
   private final ActivityChunkFinderModuleEntry val = new ActivityChunkFinderModuleEntry("General");
   private final List<ActivityChunkFinderModuleEntry> list = new ArrayList<>(List.of(this.val));

   public ActivityChunkFinderModuleEntry getVal() {
      return this.val;
   }

   public ActivityChunkFinderModuleEntry valOf(String var1) {
      ActivityChunkFinderModuleEntry var2 = new ActivityChunkFinderModuleEntry(var1);
      this.list.add(var2);
      return var2;
   }

   public List<ActivityChunkFinderModuleEntry> getList() {
      return List.copyOf(this.list);
   }

   public Setting<?> valOf2(String var1) {
      for (ActivityChunkFinderModuleEntry var3 : this.list) {
         for (Setting var5 : var3.getList()) {
            if (var5.string.equals(var1)) {
               return var5;
            }
         }
      }

      return null;
   }
}
