package dev.sixseven.module.misc;

import dev.sixseven.mixin.MinecraftAccessor;
import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class FastUseModule extends Module {
   public final ModeSetting items = this.addSetting(new ModeSetting("Items", "What to speed up", "2@$", "2@$", "Pearls", "XP Bottles"));

   public FastUseModule() {
      super("FastUse", "Removes item use cooldowns", Category.MISC);
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && this.appliesTo(client)) {
         ((MinecraftAccessor)client).sixsevenclient$setRightClickDelay(0);
      }
   }

   private boolean appliesTo(MinecraftClient client) {
      if (this.items.is("2@$")) {
         return true;
      } else {
         Item item = this.items.is("Pearls") ? Items.ENDER_PEARL : Items.EXPERIENCE_BOTTLE;
         return client.player.getMainHandStack().isOf(item) || client.player.getOffHandStack().isOf(item);
      }
   }
}
