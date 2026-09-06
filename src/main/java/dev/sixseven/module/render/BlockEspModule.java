package dev.sixseven.module.render;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.render.BlockEspRenderer;
import dev.sixseven.settings.BlockListSetting;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.ColorSetting;
import dev.sixseven.settings.ModeSetting;
import dev.sixseven.settings.SliderSetting;

public class BlockEspModule extends Module {
   public final BlockListSetting targets = this.addSetting(new BlockListSetting("Target Blocks", "Pick which blocks to highlight"));
   public final ModeSetting shapeMode = this.addSetting(new ModeSetting("Shape Mode", "How highlights are drawn", "Both", "Both", "Lines", "Sides"));
   public final ColorSetting lineColor = this.addSetting(new ColorSetting("Default Outline Color", "Color used for newly added blocks", -16711736));
   public final ColorSetting sideColor = this.addSetting(new ColorSetting("Fill Overlay", "Default fill tint", 419495880));
   public final BooleanSetting tracers = this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each block", false));
   public final BooleanSetting tracer = this.addSetting(new BooleanSetting("Tracer", "Secondary tracer enable", true));
   public final ColorSetting tracerColor = this.addSetting(new ColorSetting("Default Tracer Tint", "Tracer line color / alpha", 2097217480));
   public final SliderSetting highlightAlpha = this.addSetting(new SliderSetting("Highlight Alpha", "Box opacity", 255.0, 0.0, 255.0, 5.0));
   public final SliderSetting rangeExtraChunks = this.addSetting(
      new SliderSetting("Range Extra Chunks", "Extra scan radius beyond render distance", 1.0, 0.0, 4.0, 1.0)
   );

   public BlockEspModule() {
      super("BlockESP", "Highlights chosen blocks through walls", Category.RENDER);
      this.targets.seedDefaults();
      BooleanSetting booleanSetting = this.tracer;
      BooleanSetting booleanSetting2 = this.tracers;
      booleanSetting.visibleWhen(booleanSetting2::get);
      ColorSetting colorSetting = this.tracerColor;
      booleanSetting2 = this.tracers;
      colorSetting.visibleWhen(booleanSetting2::get);
   }

   @Override
   public void onTick() {
      BlockEspRenderer.scan(this);
   }

   @Override
   protected void onDisable() {
      BlockEspRenderer.clear();
   }
}
