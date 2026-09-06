package util;

import com.swyzzyaddon.SwyzzyAddon;
import java.lang.reflect.Method;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class ThrowableUtils {
   private static Method method;
   private static boolean bool;
   private static boolean bool2;

   private ThrowableUtils() {
   }

   public static void run(DrawContext var0, TextRenderer var1, Text var2, int var3, int var4, int var5) {
      Method var6 = methodOf(var0.getClass());
      if (var6 != null) {
         try {
            var6.invoke(var0, var1, var2, var3, var4, var5, true);
         } catch (ReflectiveOperationException var8) {
            run3(var8);
         }
      }
   }

   public static void run2(DrawContext var0, TextRenderer var1, Text var2, int var3, int var4, int var5) {
      run(var0, var1, var2, var3 - var1.getWidth(var2) / 2, var4, var5);
   }

   private static synchronized Method methodOf(Class<?> var0) {
      if (bool) {
         return method;
      } else {
         bool = true;

         for (Method var4 : var0.getMethods()) {
            String var5 = var4.getName();
            if ((var5.equals("drawText") || var5.equals("method_51439")) && var4.getParameterCount() == 6) {
               Class[] var6 = var4.getParameterTypes();
               if (TextRenderer.class.isAssignableFrom(var6[0]) && Text.class.isAssignableFrom(var6[1]) && var6[5] == boolean.class) {
                  method = var4;
                  return method;
               }
            }
         }

         run3(new NoSuchMethodException("DrawContext.drawText"));
         return null;
      }
   }

   private static void run3(Throwable var0) {
      if (!bool2) {
         bool2 = true;
         SwyzzyAddon.logger.warn("Could not resolve the cross-version text renderer.", var0);
      }
   }
}
