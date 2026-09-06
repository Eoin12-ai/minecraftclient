package dev.sixseven.hud.components;

import dev.sixseven.hud.HudComponent;
import dev.sixseven.render.nanovg.NVGImages;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

public class PotionsHud extends HudComponent {
   private static final float ROW = 24.0F;
   private static final float ICON = 18.0F;
   private static final float PAD = 7.0F;
   private static final float FONT = 12.5F;
   private final ThemeManager themes;

   public PotionsHud(ThemeManager themeManager, BooleanSupplier booleanSupplier) {
      super("potions", 0.995F, 0.6F, booleanSupplier);
      this.themes = themeManager;
   }

   private List<StatusEffectInstance> effects() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null) {
         return List.of();
      } else {
         ArrayList list = new ArrayList(player.getStatusEffects());
         list.sort(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed());
         return list;
      }
   }

   private static String label(StatusEffectInstance statusEffectInstance) {
      String name = ((StatusEffect)statusEffectInstance.getEffectType().value()).getName().getString();
      int n = statusEffectInstance.getAmplifier();
      return n > 0 ? name + " " + (n + 1) : name;
   }

   private static String timer(StatusEffectInstance statusEffectInstance) {
      if (statusEffectInstance.isInfinite()) {
         return "≭";
      } else {
         int n = statusEffectInstance.getDuration() / 20;
         return String.format("%d:%02d", n / 60, n % 60);
      }
   }

   @Override
   public float measureWidth(NVGRenderer nVGRenderer) {
      float temp = 110.0F;

      for (StatusEffectInstance statusEffectInstance : this.effects()) {
         temp = Math.max(temp, 31.0F + nVGRenderer.textWidth(label(statusEffectInstance), 12.5F) + 10.0F + nVGRenderer.textWidth(timer(statusEffectInstance), 12.5F) + 7.0F);
      }

      return temp;
   }

   @Override
   public float measureHeight(NVGRenderer nVGRenderer) {
      return Math.max(24.0F, (float)this.effects().size() * 24.0F);
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      Theme theme = this.themes.current();
      List list = this.effects();
      if (!list.isEmpty()) {
         boolean ok = this.rightAnchored();
         float f = tickDelta2;

         for (StatusEffectInstance statusEffectInstance : list) {
            float f7 = nVGRenderer.textWidth(label(statusEffectInstance), 12.5F);
            float f8 = nVGRenderer.textWidth(timer(statusEffectInstance), 12.5F);
            float f9 = 31.0F + f7 + 10.0F + f8 + 7.0F;
            float f10 = ok ? tickDelta + tickDelta3 - f9 : tickDelta;
            float f11 = f + 12.0F;
            nVGRenderer.rect(f10, f + 1.0F, f9, 22.0F, 7.0F, Colors.withAlpha(-15462118, 0.78F));
            Identifier id = statusEffectInstance.getEffectType().getKey().map(arg -> arg.getValue()).orElse(null);
            if (id != null) {
               int id2 = NVGImages.fromResource(Identifier.of(id.getNamespace(), "textures/mob_effect/" + id.getPath() + ".png"));
               if (id2 > 0) {
                  nVGRenderer.imagePattern(id2, f10 + 7.0F, f11 - 9.0F, 18.0F, 18.0F, f10 + 7.0F, f11 - 9.0F, 18.0F, 18.0F, 1.0F);
               }
            }

            nVGRenderer.text(label(statusEffectInstance), f10 + 7.0F + 18.0F + 6.0F, f11, 12.5F, theme.textPrimary());
            nVGRenderer.textGradient(timer(statusEffectInstance), f10 + f9 - 7.0F - f8, f11, 12.5F, theme.accentBright(), theme.accent());
            f += 24.0F;
         }
      }
   }
}
