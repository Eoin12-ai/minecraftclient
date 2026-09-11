package dev.kryptic.module.combat;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.AnchorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;

/**
 * Refuses anchor detonations that would kill you.
 *
 * This adds nothing you could not do yourself — it is the check you already
 * make in your head before clicking, made every tick instead of when you
 * remember. It does not place, charge or detonate anything. It only answers
 * "would this one kill me", and the anchor modules ask it before they fire.
 *
 * <p>The damage figure is an estimate: a faithful copy of vanilla's explosion
 * would need ray casts through every block between you and the anchor. It errs
 * high, which is the safe direction for a check whose only job is to refuse.
 * Treat the margin as a margin, not a guarantee.
 */
public class SafeAnchorModule extends Module {

   public final SliderSetting margin = this.addSetting(new SliderSetting("Health Margin",
      "Refuse if the blast would leave you below this much health", 6.0, 0.0, 20.0, 0.5));

   public final BooleanSetting requireTotem = this.addSetting(new BooleanSetting("Require A Totem",
      "Also refuse when there is no totem in your offhand", false));

   public final BooleanSetting warnOnly = this.addSetting(new BooleanSetting("Warn Only",
      "Let the shot through anyway -- this becomes advice rather than a veto", false));

   public SafeAnchorModule() {
      super("Safe Anchor", "Blocks anchor detonations that would kill you", Category.COMBAT);
   }

   /**
    * Whether setting off the anchor under the crosshair is survivable.
    *
    * Returns true when the module is off, so callers can ask unconditionally.
    */
   public boolean allows() {
      if (!this.isEnabled() || this.warnOnly.get()) {
         return true;
      }

      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (player == null) {
         return true;
      }

      BlockHitResult hit = AnchorHelper.anchorUnderCrosshair();
      if (hit == null) {
         return true;
      }

      if (this.requireTotem.get()
            && !player.getOffHandStack().isOf(net.minecraft.item.Items.TOTEM_OF_UNDYING)) {
         return false;
      }

      // Health only. Absorption would raise this figure, so ignoring it makes
      // the check refuse slightly more often than strictly necessary -- the
      // safe direction for something whose only job is to say no.
      float pool = player.getHealth();

      float damage = AnchorHelper.selfDamageEstimate(hit.getBlockPos());
      return pool - damage >= this.margin.getFloat();
   }
}
