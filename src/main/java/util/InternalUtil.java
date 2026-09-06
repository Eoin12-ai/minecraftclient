package util;

import com.swyzzyaddon.SwyzzyAddon;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import pkg1.ActivityChunkFinderModuleEntry;
import pkg1.ActivityChunkFinderModuleHelper4;
import pkg1.Module;
import pkg1.Setting;
import pkg1.Tab;

public final class InternalUtil {
   private static final Set<String> set = Set.of("License Key");
   private static final Set<String> set2 = Set.of("license-key", "config-manager");

   private InternalUtil() {
   }

   public static String stringOf(Module var0, Setting<?> var1) {
      return var0.string + ".setting." + var1.string;
   }

   public static String stringOf2(Object var0) {
      if (var0 instanceof ActivityChunkFinderModuleHelper4 var5) {
         return var5.intVal + "," + var5.intVal2 + "," + var5.intVal3 + "," + var5.intVal4;
      } else if (var0 instanceof Tab var4) {
         return Integer.toString(var4.getInt2());
      } else if (var0 instanceof Enum var3) {
         return var3.name();
      } else if (var0 instanceof List var1 && !var1.isEmpty() && var1.stream().allMatch(EntityType.class::isInstance)) {
         return ((java.util.List<EntityType>)var1).stream()
            .map(EntityType.class::cast)
            .<Identifier>map((EntityType var913) -> Registries.ENTITY_TYPE.getId(var913))
            .filter(Objects::nonNull)
            .<CharSequence>map(Identifier::toString)
            .collect(Collectors.joining(","));
      } else {
         return var0 instanceof List var2 && var2.stream().allMatch(Block.class::isInstance)
            ? ((java.util.List<Block>)var2).stream()
               .map(Block.class::cast)
               .<Identifier>map((Block var913) -> Registries.BLOCK.getId(var913))
               .filter(Objects::nonNull)
               .<CharSequence>map(Identifier::toString)
               .collect(Collectors.joining(","))
            : String.valueOf(var0);
      }
   }

   public static void run(Setting var0, String var1) {
      try {
         Object var2 = var0.getObject();
         if (var2 instanceof Boolean) {
            var0.run2(Boolean.parseBoolean(var1));
         } else if (var2 instanceof Integer) {
            var0.run2(Integer.parseInt(var1));
         } else if (var2 instanceof Double) {
            var0.run2(Double.parseDouble(var1));
         } else if (var2 instanceof Float) {
            var0.run2(Float.parseFloat(var1));
         } else if (var2 instanceof String) {
            var0.run2(var1);
         } else if (var2 instanceof Tab) {
            var0.run2(Tab.valOf2(var1));
         } else if (var2 instanceof Enum var3) {
            var0.run2(Enum.valueOf(var3.getDeclaringClass(), var1));
         } else if (var2 instanceof ActivityChunkFinderModuleHelper4) {
            String[] var5 = var1.split(",");
            if (var5.length == 4) {
               var0.run2(
                  new ActivityChunkFinderModuleHelper4(
                     Integer.parseInt(var5[0]), Integer.parseInt(var5[1]), Integer.parseInt(var5[2]), Integer.parseInt(var5[3])
                  )
               );
            }
         } else if (var2 instanceof List var4 && !var4.isEmpty() && var4.get(0) instanceof EntityType) {
            ArrayList var13 = new ArrayList();
            if (!var1.isBlank()) {
               for (String var17 : var1.split(",")) {
                  Identifier var18 = Identifier.tryParse(var17.trim());
                  if (var18 != null && Registries.ENTITY_TYPE.containsId(var18)) {
                     var13.add((EntityType)Registries.ENTITY_TYPE.get(var18));
                  }
               }
            }

            var0.run2(var13);
         } else if (var2 instanceof List) {
            ArrayList var12 = new ArrayList();
            if (!var1.isBlank()) {
               for (String var9 : var1.split(",")) {
                  Identifier var10 = Identifier.tryParse(var9.trim());
                  if (var10 != null && Registries.BLOCK.containsId(var10)) {
                     var12.add((Block)Registries.BLOCK.get(var10));
                  }
               }
            }

            var0.run2(var12);
         }
      } catch (RuntimeException var11) {
         SwyzzyAddon.logger.warn("Ignoring invalid saved setting {}={}", var0.string, var1);
      }
   }

   public static boolean check(Module var0, Setting<?> var1) {
      return !set.contains(var0.string) && !set2.contains(var1.string);
   }

   public static boolean check2(Module var0) {
      return !set.contains(var0.string);
   }

   public static String stringOf3(List<Module> var0) {
      Properties var1 = new Properties();

      for (Module var3 : var0) {
         if (check2(var3)) {
            var1.setProperty(var3.string, Boolean.toString(var3.isEnabled()));

            for (ActivityChunkFinderModuleEntry var5 : var3.val2.getList()) {
               for (Setting var7 : var5.getList()) {
                  if (check(var3, var7)) {
                     var1.setProperty(stringOf(var3, var7), stringOf2(var7.getObject()));
                  }
               }
            }
         }
      }

      StringWriter var9 = new StringWriter();

      try {
         var1.store(var9, "Swyzzy Client shared config");
      } catch (IOException var8) {
         SwyzzyAddon.logger.warn("Could not serialize the current config.", var8);
      }

      return var9.toString();
   }

   public static int intOf(List<Module> var0, String var1) throws IOException {
      Properties var2 = new Properties();
      var2.load(new StringReader(var1));
      int var3 = 0;

      for (Module var5 : var0) {
         if (check2(var5)) {
            boolean var6 = false;

            for (ActivityChunkFinderModuleEntry var8 : var5.val2.getList()) {
               for (Setting var10 : var8.getList()) {
                  if (check(var5, var10)) {
                     String var11 = var2.getProperty(stringOf(var5, var10));
                     if (var11 != null) {
                        run(var10, var11);
                        var6 = true;
                     }
                  }
               }
            }

            String var12 = var2.getProperty(var5.string);
            if (var12 != null) {
               var5.run(Boolean.parseBoolean(var12));
               var6 = true;
            }

            if (var6) {
               var3++;
            }
         }
      }

      return var3;
   }
}
