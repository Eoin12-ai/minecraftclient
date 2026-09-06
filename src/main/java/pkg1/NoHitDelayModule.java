package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import com.swyzzyaddon.mixin.MinecraftClientAccessor;

public final class NoHitDelayModule extends Module {
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Integer> val2_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("cooldown")
            .valOf2("Ticks the client still waits between attacks. Zero removes the delay completely.")
            .valOf3(0)
            .valOf5(0, 10)
            .getVal()
      );

   public NoHitDelayModule() {
      super(SwyzzyAddon.val, "no-hit-delay", "Removes the client side delay between attacks.");
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null) {
         MinecraftClientAccessor var2 = (MinecraftClientAccessor)class310;
         if (var2.swyzzy$getAttackCooldown() > this.val2_2.getObject()) {
            var2.swyzzy$setAttackCooldown(this.val2_2.getObject());
         }
      }
   }

   @Override
   public String getString2() {
      return this.val2_2.getObject() == 0 ? null : this.val2_2.getObject() + "t";
   }
}
