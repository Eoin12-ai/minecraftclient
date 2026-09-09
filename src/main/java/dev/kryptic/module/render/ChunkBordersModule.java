package dev.kryptic.module.render;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.SliderSetting;

/**
 * The chunk grid, drawn in the world.
 *
 * Useful well beyond looking tidy: chunk edges are where a stash's loaded
 * radius stops, where a mob farm's spawning area ends, and what the 512-block
 * region grid is measured in — which is what a region file, and therefore a
 * seed-cracked or old-anvil scan, is keyed on.
 */
public class ChunkBordersModule extends Module {

   public final BooleanSetting current = this.addSetting(new BooleanSetting(
         "Current Chunk", "Outline the chunk you are standing in", true));
   public final BooleanSetting grid = this.addSetting(new BooleanSetting(
         "Grid", "The chunk grid around you", true));
   public final SliderSetting radius = this.addSetting(new SliderSetting(
         "Grid Radius", "How many chunks out the grid reaches", 3.0, 1.0, 8.0, 1.0, "ch"));
   public final BooleanSetting regions = this.addSetting(new BooleanSetting(
         "Regions", "The 512-block region grid, the unit a region file covers", false));
   public final BooleanSetting verticals = this.addSetting(new BooleanSetting(
         "Verticals", "Uprights at the corners of your chunk", true));
   public final SliderSetting span = this.addSetting(new SliderSetting(
         "Height Span", "How far above and below you the lines run", 24.0, 4.0, 128.0, 4.0, "m"));

   public final ColorSetting chunkColor = this.addSetting(new ColorSetting(
         "Chunk Color", "Colour of the chunk grid", 0x7F4CC2FF));
   public final ColorSetting currentColor = this.addSetting(new ColorSetting(
         "Current Color", "Colour of the chunk you are in", 0xFFFFD166));
   public final ColorSetting regionColor = this.addSetting(new ColorSetting(
         "Region Color", "Colour of the region grid", 0xB3FF6BD6));

   public ChunkBordersModule() {
      super("ChunkBorders", "Chunk and region grid drawn in the world", Category.RENDER);
      this.radius.visibleWhen(this.grid::get);
      this.chunkColor.visibleWhen(this.grid::get);
      this.currentColor.visibleWhen(this.current::get);
      this.regionColor.visibleWhen(this.regions::get);
   }
}
