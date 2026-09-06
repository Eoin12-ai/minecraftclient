package dev.sixseven.module.misc;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class CustomFovModule extends Module {
   private static final double EASE_SPEED = 12.0;
   private static final double SPRINT_SPEED = 0.2825;
   public final SliderSetting fov = this.addSetting(
      new SliderSetting("FOV", "Target field of view", 95.0, 30.0, 140.0, 1.0, "Ã").withLabel(arg -> (int)arg + "° · " + tag((int)arg))
   );
   public final BooleanSetting smooth = this.addSetting(new BooleanSetting("Smooth", "Ease FOV changes in and out", true));
   public final BooleanSetting noSprintZoom = this.addSetting(new BooleanSetting("No Sprint Zoom", "Cancel the vanilla sprint / speed FOV punch", true));
   public final BooleanSetting speedFov = this.addSetting(new BooleanSetting("Speed FOV", "Widen the view with your movement speed", false));
   public final SliderSetting speedStrength = this.addSetting(new SliderSetting("Speed Strength", "Extra degrees at full sprint", 12.0, 0.0, 30.0, 1.0, "Ã"));
   private double current = -1.0;
   private long lastNanos = 0L;

   public CustomFovModule() {
      super("CustomFOV", "Overrides the field of view", Category.MISC);
      SliderSetting sliderSetting = this.speedStrength;
      BooleanSetting booleanSetting = this.speedFov;
      sliderSetting.visibleWhen(booleanSetting::get);
   }

   public float fovMultiplier(float temp) {
      MinecraftClient client = MinecraftClient.getInstance();
      int n = (Integer)client.options.getFov().getValue();
      if (n <= 0) {
         return temp;
      } else {
         double d = this.isEnabled() ? this.fov.get() + this.speedBonus(client) : (double)n;
         long l = System.nanoTime();
         double coord = this.lastNanos == 0L ? 0.0 : (double)(l - this.lastNanos) / 1.0E9;
         this.lastNanos = l;
         if (this.current < 0.0) {
            this.current = (double)n;
         }

         if (!this.smooth.get()) {
            this.current = d;
         } else {
            double currentScore = 1.0 - Math.exp(-12.0 * Math.max(0.0, coord));
            this.current = this.current + (d - this.current) * currentScore;
            if (Math.abs(this.current - d) < 0.05) {
               this.current = d;
            }
         }

         if (!this.isEnabled() && this.current == (double)n) {
            return temp;
         } else {
            float f3 = this.isEnabled() && this.noSprintZoom.get() ? 1.0F : temp;
            return (float)(this.current / (double)n) * f3;
         }
      }
   }

   public double currentFov() {
      return this.current;
   }

   private double speedBonus(MinecraftClient client) {
      if (!this.speedFov.get()) {
         return 0.0;
      } else {
         ClientPlayerEntity player = client.player;
         if (player == null) {
            return 0.0;
         } else {
            Vec3d vec = player.getVelocity();
            double d = Math.sqrt(vec.x * vec.x + vec.z * vec.z);
            double coord = Math.min(1.0, d / 0.2825);
            return (double)this.speedStrength.getFloat() * coord;
         }
      }
   }

   private static String tag(int n) {
      if (n <= 45) {
         return "Tunnel";
      } else if (n <= 65) {
         return "Focused";
      } else if (n <= 80) {
         return "Normal";
      } else if (n <= 100) {
         return "Wide";
      } else {
         return n <= 118 ? "Ultra" : "Fisheye";
      }
   }
}
