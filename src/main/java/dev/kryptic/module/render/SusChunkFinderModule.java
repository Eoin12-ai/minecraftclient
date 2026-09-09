package dev.kryptic.module.render;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.render.SusChunkRenderer;
import dev.kryptic.suschunk.SusChunkScanner;

public class SusChunkFinderModule extends Module {
   public final SliderSetting sensitivity = this.addSetting(
      new SliderSetting("Sensitivity", "Higher = stricter: more weighted evidence before a chunk flags", 3.0, 1.0, 10.0, 1.0)
         .withLabel(arg -> (int)arg + " (" + (int)arg * 5 + ")")
   );
   public final BooleanSetting amethyst = this.addSetting(
      new BooleanSetting("Amethyst", "Hidden grown clusters via server block light 5 — the strongest signal", true)
   );
   public final BooleanSetting kelp = this.addSetting(new BooleanSetting("Kelp", "Fully grown / unusually tall kelp columns", true));
   public final BooleanSetting bamboo = this.addSetting(new BooleanSetting("Bamboo", "Fully grown, max-height bamboo", true));
   public final BooleanSetting berries = this.addSetting(new BooleanSetting("Berries", "Sweet berry bushes at max growth stage", true));
   public final BooleanSetting vines = this.addSetting(new BooleanSetting("Vines", "Vines grown far down from their support", true));
   public final BooleanSetting dripstone = this.addSetting(new BooleanSetting("Dripstone", "Dripstone spikes longer than natural generation", true));
   public final SliderSetting scanSpeed = this.addSetting(new SliderSetting("Scan Speed", "Chunks scanned per tick", 10.0, 2.0, 24.0, 1.0));
   public final ModeSetting style = this.addSetting(
      new ModeSetting(
         "Style", "How a flagged chunk is drawn in the world",
         "Layer", "Layer", "Beam", "Cage", "Flat", "Corners", "Pulse"
      )
   );
   public final ColorSetting color = this.addSetting(
      new ColorSetting("Color", "Marker colour", 0xFFFF3B30)
   );
   public final BooleanSetting themeColor = this.addSetting(
      new BooleanSetting("Theme Color", "Follow the client theme's accent instead of the colour above", false)
   );
   public final BooleanSetting cellEsp = this.addSetting(
      new BooleanSetting("Cell ESP", "Box the individual amethyst cells that triggered the flag", false)
   );
   public final ColorSetting cellColor = this.addSetting(
      new ColorSetting("Cell Color", "Colour of the individual cell boxes", 0xFFA569FF)
   );
   public final SliderSetting layerThickness = this.addSetting(
      new SliderSetting("Layer Thickness", "How deep the floating slab is", 1.5, 0.2, 8.0, 0.1, "m")
   );
   public final BooleanSetting followPlayer = this.addSetting(
      new BooleanSetting("Follow You", "Float the layer above your own height instead of a fixed Y", true)
   );
   public final SliderSetting layerOffset = this.addSetting(
      new SliderSetting("Layer Height", "How far above you the layer floats", 12.0, -32.0, 96.0, 1.0, "m")
   );
   public final SliderSetting height = this.addSetting(
      new SliderSetting("Height", "How tall the beam or cage stands", 96.0, 8.0, 320.0, 8.0, "m")
   );
   public final SliderSetting renderY = this.addSetting(
      new SliderSetting("Render Y", "Height the marker is anchored at", 100.0, -64.0, 320.0, 1.0)
   );
   public final SliderSetting fillOpacity = this.addSetting(new SliderSetting("Fill Opacity", "Fill opacity of the chunk quads", 90.0, 0.0, 255.0, 1.0));
   public final BooleanSetting outline = this.addSetting(
      new BooleanSetting("Outline", "Crisp border along the chunk edges", true)
   );
   public final SliderSetting outlineOpacity = this.addSetting(
      new SliderSetting("Outline Opacity", "Border opacity", 200.0, 0.0, 255.0, 1.0)
   );
   public final BooleanSetting smartMode = this.addSetting(
      new BooleanSetting("Smart Mode", "Merge nearby flags into zones with a centroid marker", true)
   );
   public final SliderSetting mergeRadius = this.addSetting(
      new SliderSetting(
         "Merge Radius", "Flags within this many chunks merge into one zone", 3.0, 1.0, 8.0, 1.0, "ch"
      )
   );
   public final BooleanSetting centroidMarker = this.addSetting(
      new BooleanSetting("Centroid Marker", "Mark each zone's weighted centre — the likely base spot", true)
   );
   public final BooleanSetting showOnRadar = this.addSetting(
      new BooleanSetting("Show on Radar", "Pulse sus zones on the Radar; far zones clamp to the edge", true)
   );
   public final ModeSetting notifications = this.addSetting(
      new ModeSetting(
         "Notifications",
         "Announce each new zone once, with coords and distance",
         "Toast",
         "Toast",
         "Chat",
         "Off"
      )
   );
   public final SusChunkScanner scanner = new SusChunkScanner(this);

   public SusChunkFinderModule() {
      super("Sus Chunk Finder", "Finds long-loaded chunks — bases — via amethyst light & growth", Category.RENDER);
      SliderSetting sliderSetting = this.outlineOpacity;
      BooleanSetting booleanSetting = this.outline;
      sliderSetting.visibleWhen(booleanSetting::get);
      // Height only means anything to the styles that stand up off the ground
      this.height.visibleWhen(() -> this.style.is("Beam") || this.style.is("Cage")
            || this.style.is("Corners"));
      this.outline.visibleWhen(() -> this.style.is("Flat") || this.style.is("Cage"));
      this.color.visibleWhen(() -> !this.themeColor.get());
      this.layerThickness.visibleWhen(() -> this.style.is("Layer"));
      this.cellColor.visibleWhen(this.cellEsp::get);
      this.layerOffset.visibleWhen(this.followPlayer::get);
      // a fixed altitude only means anything when the marker is not tracking you
      this.renderY.visibleWhen(() -> !this.followPlayer.get());
   }

   /** True while the selected style draws something standing off the ground. */
   public boolean isUpright() {
      return this.style.is("Beam") || this.style.is("Cage") || this.style.is("Corners");
   }

   @Override
   public void onTick() {
      this.scanner.tick();
   }

   @Override
   protected void onDisable() {
      this.scanner.clear();
      SusChunkRenderer.reset();
   }
}
