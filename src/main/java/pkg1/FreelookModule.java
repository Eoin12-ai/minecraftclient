package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;

public final class FreelookModule extends Module {
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Double> val2_2 = this.val_2
      .addSetting(
         new AutoTotemModuleHelper2()
            .valOf("distance")
            .valOf2("How far behind the player the camera sits, in blocks.")
            .valOf3(4.0)
            .valOf4(0.0, 32.0)
            .valOf5(0.0, 16.0)
            .getVal()
      );
   private final Setting<Boolean> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("reset-on-deactivate")
            .valOf2("Snaps the view back to where the body is facing when freelook ends.")
            .valOf3(true)
            .getVal()
      );
   private float floatVal;
   private float floatVal2;
   private Perspective class5498;

   public FreelookModule() {
      super(SwyzzyAddon.val4, "freelook", "Look around without turning your body.");
   }

   @Override
   public void run6() {
      if (class310.player == null) {
         this.run(false);
      } else {
         this.floatVal = class310.player.getYaw();
         this.floatVal2 = class310.player.getPitch();
         this.class5498 = class310.options.getPerspective();
         class310.options.setPerspective(Perspective.THIRD_PERSON_BACK);
      }
   }

   @Override
   public void run7() {
      if (this.class5498 != null) {
         class310.options.setPerspective(this.class5498);
      }

      this.class5498 = null;
      if (this.val3_2.getObject() && class310.player != null) {
         this.floatVal = class310.player.getYaw();
         this.floatVal2 = class310.player.getPitch();
      }
   }

   public static FreelookModule getVal() {
      FreelookModule var0 = FakeRankModuleHelper.getVal().valOf(FreelookModule.class);
      return var0 != null && var0.isEnabled() ? var0 : null;
   }

   public float getFloat() {
      return this.floatVal;
   }

   public float getFloat2() {
      return this.floatVal2;
   }

   public float getFloat3() {
      return this.val2_2.getObject().floatValue();
   }

   public void run(double var1, double var3) {
      this.floatVal += (float)var1 * 0.15F;
      this.floatVal2 = MathHelper.clamp(this.floatVal2 + (float)var3 * 0.15F, -90.0F, 90.0F);
   }
}
