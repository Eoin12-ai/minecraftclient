package dev.kryptic.module.render;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.render.HoleEspRenderer;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.ModeSetting;

public class DebugHoleEspModule extends Module {
   public final ModeSetting depth = this.addSetting(new ModeSetting("Depth", "Hole depth check", "A", "A", "@", "2B1"));
   public final ColorSetting safe = this.addSetting(new ColorSetting("Safe", "Safe hole color", -12654960));
   public final ColorSetting unsafe = this.addSetting(new ColorSetting("Unsafe", "Unsafe hole color", -45715));
   public final BooleanSetting tracers = this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each hole", false));

   public DebugHoleEspModule() {
      super("DebugHoleESP", "Marks safe crystal-pvp holes", Category.RENDER);
   }

   @Override
   public void onTick() {
      HoleEspRenderer.scan(this);
   }

   @Override
   protected void onDisable() {
      HoleEspRenderer.clear();
   }
}
