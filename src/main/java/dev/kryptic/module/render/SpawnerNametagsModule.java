package dev.kryptic.module.render;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.render.BlockScanCache;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.Colors;
import net.minecraft.block.Blocks;

/**
 * Spawners, marked by a beam falling out of the sky.
 *
 * This was a box, a ring and a floating label — the same three things every ESP
 * in the client already draws, which meant a spawner looked like a chest looked
 * like a marked block, and you had to be close enough to read the text to tell
 * them apart. A beam is visible from across a render distance, tells you the
 * direction before you can see the block itself, and is the one shape nothing
 * else here uses.
 *
 * It falls rather than rises: brightest at the spawner and fading out toward
 * the sky, so the eye is pulled down to the thing that matters instead of up
 * into the clouds.
 */
public class SpawnerNametagsModule extends Module {

   public final BooleanSetting beam = this.addSetting(new BooleanSetting(
      "Sky Beam", "Drop a beam from the sky onto every spawner found", true));

   public final ColorSetting color = this.addSetting(new ColorSetting(
      "Color", "Beam color", 0xFFE01B1B));

   public final SliderSetting beamWidth = this.addSetting(new SliderSetting(
      "Beam Width", "How thick the beam's core is", 30.0, 10.0, 100.0, 5.0, "%"));

   public final SliderSetting beamOpacity = this.addSetting(new SliderSetting(
      "Beam Opacity", "How solid the beam is", 70.0, 10.0, 100.0, 5.0, "%"));

   public final ModeSetting beamTop = this.addSetting(new ModeSetting(
      "Beam Reach", "How far up the beam goes", "Sky", "Sky", "High", "Short"));

   public final BooleanSetting pulse = this.addSetting(new BooleanSetting(
      "Pulse", "Let the beam breathe rather than sitting still", true));

   public final BooleanSetting groundRing = this.addSetting(new BooleanSetting(
      "Ground Ring", "Ring of light where the beam lands", true));

   public final BooleanSetting nametag = this.addSetting(new BooleanSetting(
      "Nametag", "Floating type label above the spawner", true));

   public final BooleanSetting distance = this.addSetting(new BooleanSetting(
      "Distance", "Append the distance in blocks", true));

   public final BooleanSetting rangeRing = this.addSetting(new BooleanSetting(
      "Range Ring", "Draw the 16-block activation ring", false));

   public final BooleanSetting box = this.addSetting(new BooleanSetting(
      "Highlight Box", "Through-wall box on the spawner itself", false));

   public final BooleanSetting tracers = this.addSetting(new BooleanSetting(
      "Tracers", "Draw lines from the crosshair to each spawner", false));

   public final SliderSetting opacity = this.addSetting(new SliderSetting(
      "Opacity", "Nametag transparency", 100.0, 10.0, 100.0, 5.0, "%"));

   public final BlockScanCache scan = new BlockScanCache(
      arg -> arg.isOf(Blocks.SPAWNER) || arg.isOf(Blocks.TRIAL_SPAWNER), 12, 8, 400, 96.0
   );

   public SpawnerNametagsModule() {
      super("Spawner Nametags", "Drops a beam from the sky onto every spawner found", Category.RENDER);
   }

   /** The beam colour with no alpha set. */
   public int rgb() {
      return this.color.get() & 0xFFFFFF;
   }

   /** Core half-width in blocks. */
   public float coreWidth() {
      return 0.06f + this.beamWidth.getFloat() / 100.0f * 0.24f;
   }

   /** How high above the spawner the beam reaches, in blocks. */
   public double reach() {
      return switch (this.beamTop.get()) {
         case "Short" -> 40.0;
         case "High" -> 160.0;
         default -> 400.0;
      };
   }

   /**
    * Brightness this frame, 0..1.
    *
    * Off wall-clock time rather than tick count, so the beam keeps its rhythm
    * when the server stutters — a marker should not double as a lag readout.
    */
   public float energy() {
      if (!this.pulse.get()) return 1.0f;
      double seconds = (System.nanoTime() % 1_000_000_000_000L) / 1.0e9;
      return 0.72f + 0.28f * (float) Math.sin(seconds * 2.0);
   }

   public int beamColour(float alphaScale) {
      float a = this.beamOpacity.getFloat() / 100.0f * alphaScale * energy();
      return Colors.withAlpha(rgb(), Math.clamp(a, 0.0f, 1.0f));
   }

   @Override
   public void onTick() {
      this.scan.scan();
   }

   @Override
   protected void onDisable() {
      this.scan.clear();
   }
}
