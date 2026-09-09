package dev.kryptic.module.misc;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ModeSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

public class ArmorTrimHiderModule extends Module {
   public final ModeSetting mode = this.addSetting(
      new ModeSetting("Mode", "Hide wipes worn trims; Random gives every piece a random one", "Hide", "Hide", "Random")
   );
   public final BooleanSetting ownArmor = this.addSetting(new BooleanSetting("Own Armor", "Also affect your own worn armor (F5 / inventory)", true));
   private DynamicRegistryManager cachedAccess;
   private final List<RegistryEntry<ArmorTrimMaterial>> materials = new ArrayList<>();
   private final List<RegistryEntry<ArmorTrimPattern>> patterns = new ArrayList<>();

   public ArmorTrimHiderModule() {
      super("ArmorTrimHider", "Hides or randomizes worn armor trims", Category.MISC);
   }

   public boolean affectsOwn() {
      return this.ownArmor.get();
   }

   @Nullable
   public ArmorTrim mapTrim(ItemStack stack, @Nullable ArmorTrim armorTrim) {
      if (this.mode.is("Random")) {
         ArmorTrim armorTrim2 = this.randomTrim(stack);
         return armorTrim2 != null ? armorTrim2 : armorTrim;
      } else {
         return null;
      }
   }

   @Nullable
   private ArmorTrim randomTrim(ItemStack stack) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world == null) {
         return null;
      } else {
         DynamicRegistryManager dynamicRegistryManager = client.world.getRegistryManager();
         if (dynamicRegistryManager != this.cachedAccess) {
            this.rebuildCache(dynamicRegistryManager);
         }

         if (!this.materials.isEmpty() && !this.patterns.isEmpty()) {
            Random random = new Random((long)stack.getItem().getTranslationKey().hashCode());
            RegistryEntry registryEntry = this.materials.get(random.nextInt(this.materials.size()));
            RegistryEntry registryEntry2 = this.patterns.get(random.nextInt(this.patterns.size()));
            return new ArmorTrim(registryEntry, registryEntry2);
         } else {
            return null;
         }
      }
   }

   private void rebuildCache(DynamicRegistryManager dynamicRegistryManager) {
      this.cachedAccess = dynamicRegistryManager;
      this.materials.clear();
      this.patterns.clear();

      try {
         Stream stream = dynamicRegistryManager.getOrThrow(RegistryKeys.TRIM_MATERIAL).streamEntries();
         List list = this.materials;
         stream.forEach(list::add);
         stream = dynamicRegistryManager.getOrThrow(RegistryKeys.TRIM_PATTERN).streamEntries();
         list = this.patterns;
         stream.forEach(list::add);
      } catch (Exception ex) {
         this.materials.clear();
         this.patterns.clear();
      }
   }

   @Override
   protected void onDisable() {
      this.cachedAccess = null;
      this.materials.clear();
      this.patterns.clear();
   }
}
