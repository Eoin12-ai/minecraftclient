package pkg1;

import com.swyzzyaddon.SwyzzyAddon;

public final class SwingSpeedModule extends Module {
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Double> val2_2 = this.val_2
      .addSetting(
         new AutoTotemModuleHelper2()
            .valOf("speed")
            .valOf2("Client-side swing speed. 0.5 is half speed and 2.0 is double speed.")
            .valOf3(1.0)
            .valOf4(0.1, 2.0)
            .valOf5(0.1, 2.0)
            .getVal()
      );

   public SwingSpeedModule() {
      super(SwyzzyAddon.val2, "swing-speed", "Changes only your client-side mining and attack swing animation speed.");
   }

   public static double getDouble() {
      SwingSpeedModule var0 = FakeRankModuleHelper.getVal().valOf(SwingSpeedModule.class);
      return var0 != null && var0.isEnabled() ? var0.val2_2.getObject() : 1.0;
   }
}
