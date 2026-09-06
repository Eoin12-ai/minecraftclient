package util;

import com.swyzzyaddon.SwyzzyAddon;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Type;

public final class MISC {
   private MISC() {
   }

   public static KeyBinding class304Of(int var0) {
      try {
         KeyBinding var1 = class304Of2(var0);
         return KeyBindingHelper.registerKeyBinding(var1);
      } catch (ReflectiveOperationException var2) {
         SwyzzyAddon.logger.error("Could not register the Swyzzy GUI keybind.", var2);
         return null;
      }
   }

   public static void run(KeyBinding var0, int var1) {
      if (var0 != null) {
         var0.setBoundKey(Type.KEYSYM.createFromCode(var1));
         KeyBinding.updateKeysByCode();
      }
   }

   private static KeyBinding class304Of2(int var0) throws ReflectiveOperationException {
      for (Constructor var4 : KeyBinding.class.getConstructors()) {
         Class[] var5 = var4.getParameterTypes();
         if (var5.length == 3 && var5[0] == String.class && var5[1] == int.class) {
            if (var5[2] == String.class) {
               return (KeyBinding)var4.newInstance("key.swyzzy-client.open-gui", var0, "key.categories.misc");
            }

            Object var6 = objectOf(var5[2]);
            if (var6 != null) {
               return (KeyBinding)var4.newInstance("key.swyzzy-client.open-gui", var0, var6);
            }
         }
      }

      throw new NoSuchMethodException("compatible KeyBinding constructor");
   }

   private static Object objectOf(Class<?> var0) throws IllegalAccessException {
      Object var1 = null;

      for (Field var5 : var0.getFields()) {
         if (Modifier.isStatic(var5.getModifiers()) && var5.getType() == var0) {
            Object var6 = var5.get(null);
            if (var1 == null) {
               var1 = var6;
            }

            if (var5.getName().equals("MISC") || String.valueOf(var6).toLowerCase().contains("misc")) {
               return var6;
            }
         }
      }

      return var1;
   }
}
