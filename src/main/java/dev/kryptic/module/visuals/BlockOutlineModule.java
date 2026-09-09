package dev.kryptic.module.visuals;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.util.Colors;

/**
 * The outline drawn on the block you are looking at.
 *
 * The outline traces the block's real collision shape, so a slab, a stair or a
 * fence gets its own silhouette rather than a cube drawn around it.
 */
public class BlockOutlineModule extends Module {

   public final ColorSetting color = this.addSetting(new ColorSetting(
      "Color", "Outline color", -678620));
   public final BooleanSetting rainbow = this.addSetting(new BooleanSetting(
      "Rainbow", "Cycle the outline through the rainbow", false));
   public final ModeSetting animation = this.addSetting(new ModeSetting(
      "Animation", "Outline animation style", "Pulse", "Pulse", "Gradient Flow", "Static"));
   public final SliderSetting speed = this.addSetting(new SliderSetting(
      "Speed", "How fast the animation and rainbow run", 100.0, 20.0, 300.0, 10.0, "%"));
   public final BooleanSetting corners = this.addSetting(new BooleanSetting(
      "Corners", "Draw corner brackets instead of a closed frame", false));
   public final SliderSetting thickness = this.addSetting(new SliderSetting(
      "Thickness", "Core line width", 2.5, 1.0, 6.0, 0.5, "px"));
   public final SliderSetting glow = this.addSetting(new SliderSetting(
      "Glow", "Outer glow intensity", 60.0, 0.0, 100.0, 5.0, "%"));
   public final SliderSetting fillOpacity = this.addSetting(new SliderSetting(
      "Fill", "Transparent fill inside the block", 12.0, 0.0, 60.0, 2.0, "%"));
   public final BooleanSetting fillSides = this.addSetting(new BooleanSetting(
      "Fill Sides", "Fill the four side faces too, not just top and bottom", true));
   public final SliderSetting expand = this.addSetting(new SliderSetting(
      "Expand", "Push the outline off the block face, to stop it flickering", 2.0, 0.0, 12.0, 0.5, "%"));

   public BlockOutlineModule() {
      super("Block Outline", "Glowing animated outline on the targeted block", Category.VISUALS);
      this.setEnabled(true);
   }

   /**
    * The outline colour this frame, with no alpha set.
    *
    * The rainbow runs off wall-clock time rather than tick count, so it keeps
    * its rhythm when the server stutters — a cosmetic should not be a latency
    * readout.
    */
   public int currentRgb() {
      if (this.rainbow.get()) {
         long period = (long)(4000.0F / this.speedScale());
         float hue = (float)(System.currentTimeMillis() % period) / period * 360.0F;
         return Colors.hsvToRgb(hue, 0.8F, 1.0F) & 16777215;
      }
      return this.color.get() & 16777215;
   }

   public float glowStrength() {
      return this.glow.getFloat() / 100.0F;
   }

   /** Animation rate as a multiplier, so 100% is the tuned default. */
   public float speedScale() {
      return Math.max(0.2F, this.speed.getFloat() / 100.0F);
   }

   /** How far, in blocks, to push the outline off the block's faces. */
   public double expansion() {
      return this.expand.getFloat() / 100.0 * 0.5;
   }
}
