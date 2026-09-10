package dev.kryptic.hud.components;

import dev.kryptic.hud.HudComponent;
import dev.kryptic.hud.HudSurface;
import dev.kryptic.render.nanovg.NVGImages;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import dev.kryptic.util.CombatTarget;
import java.util.function.BooleanSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Who you are fighting, while you are fighting them.
 *
 * The information is all on screen already — their nametag carries a health
 * bar, their armour is on their model — but not where you can read it mid-fight.
 * Reading a health bar means looking at the thing you are trying to hit, and by
 * the time you have parsed it the number has changed. This puts it in one fixed
 * place at a size you can take in without moving your eyes off the crosshair.
 *
 * The bar carries a second, slower marker behind the live one. A bar that only
 * shows the current value tells you what their health is; the trailing marker
 * tells you which way it is going and how fast, which is the thing you actually
 * make decisions on.
 */
public class EnemyHud extends HudComponent {

   private static final float PAD = 8.0F;
   private static final float FACE = 30.0F;
   private static final float GAP = 9.0F;
   private static final float BAR_H = 7.0F;
   private static final float ARMOUR = 13.0F;
   private static final float NAME_FONT = 12.5F;
   private static final float SMALL_FONT = 10.0F;
   private static final float WIDTH = 168.0F;
   private static final float HEIGHT = 56.0F;

   private static final EquipmentSlot[] SLOTS = {
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
      EquipmentSlot.FEET, EquipmentSlot.MAINHAND
   };

   /** How fast the trailing marker catches up, as a fraction per frame. */
   private static final float TRAIL_RATE = 0.06F;

   private final ThemeManager themes;

   /** The trailing health marker, and who it belongs to. */
   private float trail = -1.0F;
   private int trailOwner = Integer.MIN_VALUE;

   public EnemyHud(ThemeManager themes, BooleanSupplier visible) {
      super("enemy", 0.5F, 0.26F, visible);
      this.themes = themes;
   }

   @Override
   public float measureWidth(NVGRenderer nvg) {
      return WIDTH;
   }

   @Override
   public float measureHeight(NVGRenderer nvg) {
      // Nothing to fight, nothing to draw. Returning zero keeps the panel out
      // of the HUD's layout entirely rather than leaving a hole where it sits.
      return CombatTarget.current() == null ? 0.0F : HEIGHT;
   }

   @Override
   public void render(NVGRenderer nvg, float x, float y, float w, float h) {
      LivingEntity target = CombatTarget.current();
      if (target == null) {
         this.trail = -1.0F;
         return;
      }

      Theme theme = this.themes.current();
      HudSurface.panel(nvg, x, y, w, h, 9.0F, theme);

      float max = Math.max(1.0F, target.getMaxHealth());
      float health = Math.clamp(target.getHealth(), 0.0F, max);
      float fraction = health / max;

      // The trail belongs to one entity. Switching targets has to snap, not
      // slide across from the last one's health.
      if (this.trailOwner != target.getId() || this.trail < 0.0F) {
         this.trailOwner = target.getId();
         this.trail = fraction;
      } else if (this.trail > fraction) {
         this.trail += (fraction - this.trail) * TRAIL_RATE;
      } else {
         this.trail = fraction;               // healing catches up at once
      }

      float faceX = x + PAD;
      float faceY = y + (h - FACE) / 2.0F;
      drawFace(nvg, target, theme, faceX, faceY);

      float textX = faceX + FACE + GAP;
      float textW = x + w - PAD - textX;

      nvg.textTruncated(displayName(target), textX, y + PAD + 6.0F, NAME_FONT,
            theme.textPrimary(), textW - 34.0F);

      // Distance sits right-aligned on the name's line: it is the one number
      // you glance at without needing to read a label.
      ClientPlayerEntity self = MinecraftClient.getInstance().player;
      if (self != null) {
         String dist = (int) self.distanceTo(target) + "m";
         float dw = nvg.textWidth(dist, SMALL_FONT);
         nvg.text(dist, x + w - PAD - dw, y + PAD + 6.0F, SMALL_FONT, theme.textMuted());
      }

      float barY = y + PAD + 17.0F;
      drawHealthBar(nvg, theme, textX, barY, textW, fraction, health);

      drawArmour(nvg, target, theme, textX, barY + BAR_H + 6.0F);
   }

   /** The head from their skin, or a plain plate for anything that is not a player. */
   private void drawFace(NVGRenderer nvg, LivingEntity target, Theme theme, float fx, float fy) {
      if (target instanceof AbstractClientPlayerEntity playerTarget) {
         int tex = NVGImages.wrapGlTexture(playerTarget.getSkin().body().texturePath(), 64, 64);
         if (tex > 0) {
            // the face, then the hat layer over it — the same two draws the
            // radar uses, so a player looks the same in both places
            NVGImages.drawSubImage(nvg, tex, 64.0F, 64.0F, 8.0F, 8.0F, 16.0F, 16.0F, fx, fy, FACE, FACE, 1.0F);
            NVGImages.drawSubImage(nvg, tex, 64.0F, 64.0F, 40.0F, 8.0F, 48.0F, 16.0F, fx, fy, FACE, FACE, 1.0F);
            nvg.rectOutline(fx - 1.0F, fy - 1.0F, FACE + 2.0F, FACE + 2.0F, 4.0F, 1.0F,
                  Colors.withAlpha(theme.accent(), 0.45F));
            return;
         }
      }

      nvg.rect(fx, fy, FACE, FACE, 4.0F, Colors.withAlpha(theme.accent(), 0.22F));
      String initial = displayName(target).substring(0, 1).toUpperCase();
      float iw = nvg.textWidth(initial, 15.0F);
      nvg.text(initial, fx + (FACE - iw) / 2.0F, fy + FACE / 2.0F, 15.0F, theme.textPrimary());
   }

   private void drawHealthBar(NVGRenderer nvg, Theme theme, float bx, float by, float bw,
                              float fraction, float health) {
      nvg.rect(bx, by, bw, BAR_H, BAR_H / 2.0F, Colors.withAlpha(-16777216, 0.45F));

      // The trail sits behind the live bar, so the gap between them is the
      // damage that just landed.
      if (this.trail > fraction) {
         nvg.rect(bx, by, Math.max(BAR_H, bw * this.trail), BAR_H, BAR_H / 2.0F,
               Colors.withAlpha(-1684147, 0.55F));
      }

      // Red when they are nearly down, green when they are not. The colour is
      // the read you get from peripheral vision, before you parse the number.
      int fill = Colors.lerp(-1684147, -11671924, fraction);
      nvg.rect(bx, by, Math.max(BAR_H, bw * fraction), BAR_H, BAR_H / 2.0F, fill);

      String value = String.valueOf(Math.round(health));
      float vw = nvg.textWidth(value, SMALL_FONT);
      // Sit the number just inside the bar's right end, in whichever of black
      // or white actually reads against the fill behind it.
      float valueX = bx + bw - vw - 4.0F;
      boolean overFill = valueX < bx + bw * fraction;
      nvg.text(value, valueX, by + BAR_H / 2.0F, SMALL_FONT,
            overFill ? Colors.contrastOn(fill) : theme.textMuted());
   }

   private void drawArmour(NVGRenderer nvg, LivingEntity target, Theme theme, float ax, float ay) {
      float cx = ax;
      for (EquipmentSlot slot : SLOTS) {
         ItemStack stack = target.getEquippedStack(slot);
         if (stack.isEmpty()) {
            continue;
         }

         Identifier id = Registries.ITEM.getId(stack.getItem());
         int tex = NVGImages.fromResource(
               Identifier.of(id.getNamespace(), "textures/item/" + id.getPath() + ".png"));
         if (tex > 0) {
            nvg.imagePattern(tex, cx, ay, ARMOUR, ARMOUR, cx, ay, ARMOUR, ARMOUR, 1.0F);
         } else {
            nvg.rect(cx, ay, ARMOUR, ARMOUR, 3.0F, Colors.withAlpha(theme.accent(), 0.28F));
         }

         cx += ARMOUR + 3.0F;
      }
   }

   private static String displayName(LivingEntity target) {
      String name = target.getName().getString();
      return name == null || name.isEmpty() ? "?" : name;
   }
}
