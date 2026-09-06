package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;
import util.InternalUtil;

public final class SwyzzyAddonHelper2 {
   private static final Path path = FabricLoader.getInstance().getConfigDir().resolve("swyzzy-client-modules.properties");
   private final List<Module> list = new ArrayList<>();
   private final Map<Class<?>, List<InternalHelper>> map = new HashMap<>();
   private Set<Integer> set = new HashSet<>();
   private boolean bool;
   private boolean bool2;

   public void run(Module var1) {
      this.list.add(var1);
      FakeRankModuleHelper.getVal().run(var1);
      var1.run2(this::run5);

      for (ActivityChunkFinderModuleEntry var3 : var1.val2.getList()) {
         for (Setting var5 : var3.getList()) {
            var5.run(this::run5);
         }
      }

      for (Method var11 : var1.getClass().getDeclaredMethods()) {
         if (var11.isAnnotationPresent(InternalHelper5.class) && var11.getParameterCount() == 1) {
            var11.setAccessible(true);
            Class var6 = var11.getParameterTypes()[0];
            int var7 = var11.getAnnotation(InternalHelper5.class).getInt();
            this.map.computeIfAbsent(var6, SwyzzyAddonHelper2::listOf).add(new InternalHelper(var1, var11, var7));
            this.map.get(var6).sort(Comparator.comparingInt(InternalHelper::priority).reversed());
         }
      }
   }

   public List<Module> getList() {
      return List.copyOf(this.list);
   }

   public void run2(long var1) {
      HashSet var3 = new HashSet();

      for (Module var5 : this.list) {
         Tab var6 = var5.val3.getObject();
         if (var6.isEnabled2() && check(var1, var6)) {
            var3.add(var6.getInt2());
         }
      }

      if (!var3.isEmpty()) {
         for (Module var8 : this.list) {
            Tab var9 = var8.val3.getObject();
            if (var9.isEnabled2() && var3.contains(var9.getInt2()) && !this.set.contains(var9.getInt2())) {
               var8.run3();
            }
         }
      }

      this.set = var3;
   }

   private static boolean check(long var0, Tab var2) {
      int var3 = var2.isEnabled() ? GLFW.glfwGetMouseButton(var0, var2.getInt()) : GLFW.glfwGetKey(var0, var2.getInt2());
      return var3 == 1;
   }

   public <T extends Module> T valOf(Class<T> var1) {
      for (Module var3 : this.list) {
         if (var1.isInstance(var3)) {
            return (T)var3;
         }
      }

      return null;
   }

   public void run3(Object var1) {
      List var2 = this.map.get(var1.getClass());
      if (var2 != null) {
         for (InternalHelper var4 : (Iterable<InternalHelper>)(Object)(var2)) {
            if (var4.module().isEnabled()) {
               try {
                  var4.method().invoke(var4.module(), var1);
               } catch (Throwable var6) {
                  SwyzzyAddon.logger
                     .warn("Swyzzy module {} failed while handling {}.", new Object[]{var4.module().string, var1.getClass().getSimpleName(), var6});
               }
            }
         }
      }
   }

   public void run4() {
      Properties var1 = new Properties();
      if (Files.exists(path)) {
         try (InputStream var2 = Files.newInputStream(path)) {
            var1.load(var2);
         } catch (IOException var19) {
            SwyzzyAddon.logger.warn("Could not load Swyzzy module states.", var19);
         }
      }

      this.bool = true;

      try {
         for (Module var3 : this.list) {
            for (ActivityChunkFinderModuleEntry var5 : var3.val2.getList()) {
               for (Setting var7 : var5.getList()) {
                  String var8 = var1.getProperty(InternalUtil.stringOf(var3, var7));
                  if (var8 != null) {
                     InternalUtil.run(var7, var8);
                  }
               }
            }

            try {
               var3.run(Boolean.parseBoolean(var1.getProperty(var3.string, "false")));
            } catch (RuntimeException var15) {
               SwyzzyAddon.logger.warn("Swyzzy module {} failed to activate on load.", var3.string, var15);
            }
         }
      } finally {
         this.bool = false;
         this.bool2 = false;
      }
   }

   private void run5() {
      if (!this.bool) {
         this.bool2 = true;
      }
   }

   public void run6() {
      if (this.bool2) {
         this.run7();
      }
   }

   public void run7() {
      Properties var1 = new Properties();

      for (Module var3 : this.list) {
         var1.setProperty(var3.string, Boolean.toString(var3.isEnabled()));

         for (ActivityChunkFinderModuleEntry var5 : var3.val2.getList()) {
            for (Setting var7 : var5.getList()) {
               var1.setProperty(InternalUtil.stringOf(var3, var7), InternalUtil.stringOf2(var7.getObject()));
            }
         }
      }

      try {
         Files.createDirectories(path.getParent());
         Path var12 = Files.createTempFile(path.getParent(), "swyzzy-modules-", ".tmp");

         try (OutputStream var13 = Files.newOutputStream(var12)) {
            var1.store(var13, "Swyzzy Addon module states");
         }

         try {
            Files.move(var12, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException var9) {
            Files.move(var12, path, StandardCopyOption.REPLACE_EXISTING);
         }

         this.bool2 = false;
      } catch (IOException var11) {
         SwyzzyAddon.logger.warn("Could not save Swyzzy module states.", var11);
      }
   }

   private static List listOf(Class var0) {
      return new ArrayList<>();
   }
}
