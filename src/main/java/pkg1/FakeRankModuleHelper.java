package pkg1;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class FakeRankModuleHelper {
   private static final FakeRankModuleHelper val = new FakeRankModuleHelper();
   private final Map<Class<? extends Module>, Module> map = new HashMap<>();

   public static FakeRankModuleHelper getVal() {
      return val;
   }

   public void run(Module var1) {
      this.map.put((Class<? extends Module>)var1.getClass(), var1);
   }

   public <T extends Module> T valOf(Class<T> var1) {
      return (T)this.map.get(var1);
   }

   public Collection<Module> getCollection() {
      return this.map.values().stream().filter(Module::isEnabled).toList();
   }
}
