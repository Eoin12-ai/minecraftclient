package dev.sixseven.module.render;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.SliderSetting;
import dev.sixseven.settings.ModeSetting;
import dev.sixseven.render.SusChunkRenderer;
import dev.sixseven.suschunk.SusChunkScanner;

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
   public final SliderSetting renderY = this.addSetting(
      new SliderSetting("Render Y", "Height the flat chunk highlights render at", 100.0, -64.0, 320.0, 1.0)
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
         "Merge Radius", "Flags within this many chunks merge into one zone", 3.0, 1.0, 8.0, 1.0, "SO "
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
         "<J."
      )
   );
   public final SusChunkScanner scanner = new SusChunkScanner(this);

   public SusChunkFinderModule() {
      super("SusChunkFinder", "Finds long-loaded chunks — bases — via amethyst light & growth", Category.RENDER);
      SliderSetting sliderSetting = this.outlineOpacity;
      BooleanSetting booleanSetting = this.outline;
      sliderSetting.visibleWhen(booleanSetting::get);
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
