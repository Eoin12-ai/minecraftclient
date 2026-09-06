package util;

import java.lang.reflect.Field;
import pkg1.Module;

public final class Utils_2 {
   private Utils_2() {
   }

   public static void run(Module var0, String var1) {
      Class var2 = var0.getClass();

      while (var2 != null) {
         try {
            Field var3 = var2.getDeclaredField("title");
            var3.setAccessible(true);
            var3.set(var0, var1);
            return;
         } catch (NoSuchFieldException var4) {
            var2 = var2.getSuperclass();
         } catch (Throwable var5) {
            return;
         }
      }
   }
}
