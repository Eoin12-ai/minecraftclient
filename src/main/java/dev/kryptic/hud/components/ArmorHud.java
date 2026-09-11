package dev.kryptic.hud.components;

import dev.kryptic.hud.HudComponent;
import dev.kryptic.hud.HudSurface;
import dev.kryptic.render.nanovg.NVGImages;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import java.util.function.BooleanSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ArmorHud extends HudComponent {
   private static final EquipmentSlot[] SLOTS = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private static final float ICON = 22.0F;
   private static final float PAD = 7.0F;
   private static final float GAP = 6.0F;
   private static final float BAR_H = 3.0F;
   private final ThemeManager themes;

   public ArmorHud(ThemeManager themeManager, BooleanSupplier booleanSupplier) {
      super("armor", 0.5F, 0.8F, booleanSupplier);
      this.themes = themeManager;
   }

   @Override
   public float measureWidth(NVGRenderer nVGRenderer) {
      return 14.0F + (float)SLOTS.length * 22.0F + (float)(SLOTS.length - 1) * 6.0F;
   }

   @Override
   public float measureHeight(NVGRenderer nVGRenderer) {
      return 38.0F;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      Theme theme = this.themes.current();
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null) {
         HudSurface.panel(nVGRenderer, tickDelta, tickDelta2, tickDelta3, tickDelta4, 9.0F, theme);
         float f = tickDelta + 7.0F;

         for (EquipmentSlot equipmentSlot : SLOTS) {
            ItemStack stack = player.getEquippedStack(equipmentSlot);
            float f5 = tickDelta2 + 4.0F;
            if (stack.isEmpty()) {
               nVGRenderer.rectOutline(f, f5, 22.0F, 22.0F, 5.0F, 1.0F, Colors.withAlpha(theme.textDisabled(), 0.5F));
            } else {
               Identifier id = Registries.ITEM.getId(stack.getItem());
               int id2 = NVGImages.fromResource(Identifier.of(id.getNamespace(), "textures/item/" + id.getPath() + ".png"));
               if (id2 > 0) {
                  nVGRenderer.imagePattern(id2, f, f5, 22.0F, 22.0F, f, f5, 22.0F, 22.0F, 1.0F);
               } else {
                  nVGRenderer.rect(f, f5, 22.0F, 22.0F, 5.0F, Colors.withAlpha(theme.accent(), 0.3F));
               }

               if (stack.isDamageable()) {
                  float f6 = 1.0F - (float)stack.getDamage() / (float)stack.getMaxDamage();
                  int n = Colors.healthRamp(f6);
                  float f7 = f5 + 22.0F + 3.0F;
                  nVGRenderer.rect(f, f7, 22.0F, 3.0F, 1.5F, Colors.withAlpha(-16777216, 0.45F));
                  nVGRenderer.rect(f, f7, Math.max(3.0F, 22.0F * f6), 3.0F, 1.5F, n);
               }
            }

            f += 28.0F;
         }
      }
   }
}
