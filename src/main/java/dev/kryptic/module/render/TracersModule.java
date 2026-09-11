package dev.kryptic.module.render;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;

/** Lines drawn from in front of the camera out to whatever is worth knowing about. */
public class TracersModule extends Module {

   public final BooleanSetting players = this.addSetting(new BooleanSetting(
         "Players", "Trace other players", true));
   public final BooleanSetting mobs = this.addSetting(new BooleanSetting(
         "Mobs", "Trace hostile and passive mobs", false));
   public final BooleanSetting items = this.addSetting(new BooleanSetting(
         "Items", "Trace dropped items", false));

   public final ModeSetting target = this.addSetting(new ModeSetting(
         "Target", "Where on the entity the line lands", "Body", "Feet", "Body", "Head"));
   public final SliderSetting range = this.addSetting(new SliderSetting(
         "Range", "How far out to trace", 128.0, 16.0, 512.0, 8.0, "m"));
   public final SliderSetting thickness = this.addSetting(new SliderSetting(
         "Thickness", "Line width", 1.6, 0.5, 4.0, 0.1, ""));
   public final BooleanSetting fade = this.addSetting(new BooleanSetting(
         "Fade", "Fade the line out with distance", true));

   public final ColorSetting playerColor = this.addSetting(new ColorSetting(
         "Player Color", "Colour for players", 0xFFFF4C6B));
   public final ColorSetting mobColor = this.addSetting(new ColorSetting(
         "Mob Color", "Colour for mobs", 0xFFFFA24C));
   public final ColorSetting itemColor = this.addSetting(new ColorSetting(
         "Item Color", "Colour for dropped items", 0xFF6BE675));

   public TracersModule() {
      super("Tracers", "Lines to players, mobs and dropped items", Category.RENDER);
      this.playerColor.visibleWhen(this.players::get);
      this.mobColor.visibleWhen(this.mobs::get);
      this.itemColor.visibleWhen(this.items::get);
   }
}
