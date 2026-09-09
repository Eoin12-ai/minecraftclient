package dev.kryptic.module.combat;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;

public class HitBoxModule extends Module {
   public final SliderSetting expand = this.addSetting(
      new SliderSetting("Expand", "Disabled vs prediction anticheats — kept for config compatibility", 0.5, 0.0, 2.0, 0.05)
   );
   public final BooleanSetting enableRender = this.addSetting(new BooleanSetting("Show Hitboxes", "Draw the box around each entity", true));

   public HitBoxModule() {
      super("Hit Box", "Hitbox expander (neutered: undetectable = does not widen server hitboxes)", Category.COMBAT);
   }

   public float getHitboxExpansion() {
      return this.isEnabled() ? this.expand.getFloat() : 0.0F;
   }

   public boolean shouldRender() {
      return this.isEnabled() && this.enableRender.get();
   }
}
