package dev.kryptic.module.misc;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class FreeLookModule extends Module {
   private static FreeLookModule instance;
   public final ModeSetting mode = this.addSetting(new ModeSetting("Mode",
      "Free Camera: your body holds still and the mouse swings the camera. "
    + "Locked Camera: the camera holds still and the mouse turns your body.",
      "Free Camera", "Free Camera", "Locked Camera"));
   public final BooleanSetting togglePerspective = this.addSetting(new BooleanSetting("Toggle Perspective", "Switch to third person on toggle.", true));
   public final BooleanSetting throughWalls = this.addSetting(
      new BooleanSetting("Through Walls", "See through walls — the third-person camera ignores wall collision.", false)
   );
   public final SliderSetting sensitivity = this.addSetting(
      new SliderSetting("Camera Sensitivity",
         "How fast the mouse swings the camera in Free Camera mode. 5 matches your normal look speed.",
         5.0, 0.5, 10.0, 0.1)
   );
   public final BooleanSetting arrows = this.addSetting(
      new BooleanSetting("Arrows Turn Other View", "Control the other entity's rotation with the arrow keys.", true)
   );
   public final SliderSetting arrowSpeed = this.addSetting(new SliderSetting("Arrow Speed", "Rotation speed with the arrow keys.", 4.0, 0.0, 10.0, 0.5));
   private float cameraYaw;
   private float cameraPitch;
   private Perspective prePers;

   public FreeLookModule() {
      super("Free Look", "Allows more rotation options in third person.", Category.MISC);
      instance = this;
   }

   public static FreeLookModule get() {
      return instance;
   }

   @Override
   protected void onEnable() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         this.cameraYaw = client.player.getYaw();
         this.cameraPitch = client.player.getPitch();
         this.prePers = client.options.getPerspective();
         if (this.prePers != Perspective.THIRD_PERSON_BACK && this.togglePerspective.get()) {
            client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
         }
      }
   }

   @Override
   protected void onDisable() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (this.prePers != null && client.options.getPerspective() != this.prePers && this.togglePerspective.get()) {
         client.options.setPerspective(this.prePers);
      }
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         if (this.arrows.get()) {
            long l = client.getWindow().getHandle();
            boolean pressed = GLFW.glfwGetKey(l, 263) == 1;
            boolean pressed2 = GLFW.glfwGetKey(l, 262) == 1;
            boolean pressed3 = GLFW.glfwGetKey(l, 265) == 1;
            boolean pressed4 = GLFW.glfwGetKey(l, 264) == 1;
            int n = (int)(this.arrowSpeed.get() * 2.0);

            for (int offset = 0; offset < n; offset++) {
               if (!this.mode.is("Free Camera")) {
                  if (pressed) {
                     this.cameraYaw -= 0.5F;
                  }

                  if (pressed2) {
                     this.cameraYaw += 0.5F;
                  }

                  if (pressed3) {
                     this.cameraPitch -= 0.5F;
                  }

                  if (pressed4) {
                     this.cameraPitch += 0.5F;
                  }
               } else {
                  float f = client.player.getYaw();
                  float f3 = client.player.getPitch();
                  if (pressed) {
                     f -= 0.5F;
                  }

                  if (pressed2) {
                     f += 0.5F;
                  }

                  if (pressed3) {
                     f3 -= 0.5F;
                  }

                  if (pressed4) {
                     f3 += 0.5F;
                  }

                  client.player.setYaw(f);
                  client.player.setPitch(f3);
               }
            }
         }

         client.player.setPitch(MathHelper.clamp(client.player.getPitch(), -90.0F, 90.0F));
         this.cameraPitch = MathHelper.clamp(this.cameraPitch, -90.0F, 90.0F);
      }
   }

   public boolean isActive() {
      return this.isEnabled() && MinecraftClient.getInstance().player != null;
   }

   public boolean seeThroughWalls() {
      return this.isActive() && this.throughWalls.get();
   }

   /**
    * Whether the mouse drives the camera rather than the player.
    *
    * ModeSetting refuses to load a value that is not in its list, so the old
    * "Player" and "Camera" both fall back to the new default. That is the
    * outcome we want here rather than one to migrate around: Free Camera is
    * what the old default should have been.
    */
   public boolean cameraMode() {
      return this.isActive() && this.mode.is("Free Camera");
   }

   public void addCameraLook(double d, double coord) {
      // The arguments arrive raw, the way Entity.changeLookDirection receives
      // them, so this has to apply vanilla's own 0.15 itself. The slider used
      // to be a divisor, which made "sensitivity 10" the slowest setting on
      // the bar -- it scales the speed now, so the number reads the way the
      // label promises. 5 lands on vanilla's factor exactly.
      float f = this.sensitivity.getFloat();
      double scale = 0.15 * (f / 5.0);

      this.cameraYaw += (float)(d * scale);
      this.cameraPitch += (float)(coord * scale);
      if (Math.abs(this.cameraPitch) > 90.0F) {
         this.cameraPitch = this.cameraPitch > 0.0F ? 90.0F : -90.0F;
      }
   }

   public float getCameraYaw() {
      return this.cameraYaw;
   }

   public float getCameraPitch() {
      return this.cameraPitch;
   }
}
