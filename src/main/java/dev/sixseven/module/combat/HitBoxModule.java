package dev.sixseven.module.combat;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.SliderSetting;

public class HitBoxModule extends Module {
   public final SliderSetting expand = this.addSetting(
      new SliderSetting("Expand", "Disabled vs prediction anticheats — kept for config compatibility", 0.5, 0.0, 2.0, 0.05)
   );
   public final BooleanSetting enableRender = this.addSetting(new BooleanSetting("Enable Render", "", true));

   public HitBoxModule() {
      super("HitBox", "Hitbox expander (neutered: undetectable = does not widen server hitboxes)", Category.COMBAT);
   }

   public float getHitboxExpansion() {
      return this.isEnabled() ? this.expand.getFloat() : 0.0F;
   }

   public boolean shouldRender() {
      return this.isEnabled() && this.enableRender.get();
   }
}
