package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.Locale;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class FreecamModule extends Module {
   private static final double doubleVal = 0.25;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Scroll");
   private final Setting<Double> val3_2 = this.val_2
      .addSetting(
         new AutoTotemModuleHelper2()
            .valOf("speed")
            .valOf2("Base camera speed in blocks per second.")
            .valOf3(12.0)
            .valOf4(0.5, 150.0)
            .valOf5(1.0, 60.0)
            .getVal()
      );
   private final Setting<Double> val4 = this.val_2
      .addSetting(
         new AutoTotemModuleHelper2()
            .valOf("sprint-multiplier")
            .valOf2("Speed factor while the sprint key is held.")
            .valOf3(2.5)
            .valOf4(1.0, 10.0)
            .valOf5(1.0, 5.0)
            .getVal()
      );
   private final Setting<Boolean> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("keep-momentum")
            .valOf2("Your body carries on with the movement it had when freecam started. Off makes it stand still.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Boolean> val6 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("scroll-speed")
            .valOf2("Scroll wheel raises and lowers the camera speed instead of switching hotbar slot.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Double> val7 = this.val2_2
      .addSetting(
         new AutoTotemModuleHelper2()
            .valOf("scroll-step")
            .valOf2("How much one scroll notch changes the speed factor.")
            .valOf3(0.15)
            .valOf4(0.01, 2.0)
            .valOf5(0.05, 1.0)
            .valOf7(this.val6::getObject)
            .getVal()
      );
   private final Setting<Double> val8 = this.val2_2
      .addSetting(
         new AutoTotemModuleHelper2()
            .valOf("max-scroll-factor")
            .valOf2("Upper limit the scroll wheel can push the speed factor to.")
            .valOf3(8.0)
            .valOf4(1.0, 50.0)
            .valOf5(1.0, 20.0)
            .valOf7(this.val6::getObject)
            .getVal()
      );
   private Vec3d class243 = Vec3d.ZERO;
   private float floatVal;
   private float floatVal2;
   private double doubleVal2 = 1.0;
   private long longVal;
   private PlayerInput class10185 = PlayerInput.DEFAULT;
   private Perspective class5498;

   public FreecamModule() {
      super(SwyzzyAddon.val4, "freecam", "Flies the camera away from your body.");
   }

   @Override
   public void run6() {
      if (class310.player == null) {
         this.run(false);
      } else {
         this.class243 = class310.player.getEyePos();
         this.floatVal = class310.player.getYaw();
         this.floatVal2 = class310.player.getPitch();
         this.doubleVal2 = 1.0;
         this.longVal = 0L;
         this.class10185 = ((Boolean)this.val5.getObject()) ? class310.player.input.playerInput : PlayerInput.DEFAULT;
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
      this.class10185 = PlayerInput.DEFAULT;
      this.longVal = 0L;
   }

   @Override
   public String getString2() {
      return String.format(Locale.ROOT, "%.1fx", this.doubleVal2);
   }

   public static FreecamModule getVal() {
      FreecamModule var0 = FakeRankModuleHelper.getVal().valOf(FreecamModule.class);
      return var0 != null && var0.isEnabled() ? var0 : null;
   }

   public static PlayerInput getclass10185() {
      FreecamModule var0 = getVal();
      return var0 == null ? null : var0.class10185;
   }

   public float getFloat() {
      return this.floatVal;
   }

   public float getFloat2() {
      return this.floatVal2;
   }

   public Vec3d getclass243() {
      long var1 = System.nanoTime();
      double var3 = this.longVal == 0L ? 0.0 : (var1 - this.longVal) / 1.0E9;
      this.longVal = var1;
      if (var3 <= 0.0) {
         return this.class243;
      } else {
         double var5 = 0.0;
         double var7 = 0.0;
         double var9 = 0.0;
         if (class310.options.forwardKey.isPressed()) {
            var5++;
         }

         if (class310.options.backKey.isPressed()) {
            var5--;
         }

         if (class310.options.leftKey.isPressed()) {
            var7++;
         }

         if (class310.options.rightKey.isPressed()) {
            var7--;
         }

         if (class310.options.jumpKey.isPressed()) {
            var9++;
         }

         if (class310.options.sneakKey.isPressed()) {
            var9--;
         }

         if (var5 == 0.0 && var7 == 0.0 && var9 == 0.0) {
            return this.class243;
         } else {
            double var11 = Math.sqrt(var5 * var5 + var7 * var7);
            if (var11 > 1.0) {
               var5 /= var11;
               var7 /= var11;
            }

            double var13 = this.val3_2.getObject() * this.doubleVal2 * Math.min(var3, 0.25);
            if (class310.options.sprintKey.isPressed()) {
               var13 *= this.val4.getObject();
            }

            double var15 = Math.toRadians(this.floatVal);
            double var17 = Math.sin(var15);
            double var19 = Math.cos(var15);
            this.class243 = this.class243.add((var7 * var19 - var5 * var17) * var13, var9 * var13, (var5 * var19 + var7 * var17) * var13);
            return this.class243;
         }
      }
   }

   public void run(double var1, double var3) {
      this.floatVal += (float)var1 * 0.15F;
      this.floatVal2 = MathHelper.clamp(this.floatVal2 + (float)var3 * 0.15F, -90.0F, 90.0F);
   }

   public boolean check2(double var1) {
      if (!this.val6.getObject()) {
         return false;
      } else {
         this.doubleVal2 = MathHelper.clamp(this.doubleVal2 + var1 * this.val7.getObject(), 0.1, this.val8.getObject());
         return true;
      }
   }
}
