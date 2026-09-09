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
   public final ModeSetting mode = this.addSetting(new ModeSetting("Mode", "Which entity the mouse rotates.", "Player", "Player", "Camera"));
   public final BooleanSetting togglePerspective = this.addSetting(new BooleanSetting("Toggle Perspective", "Switch to third person on toggle.", true));
   public final BooleanSetting throughWalls = this.addSetting(
      new BooleanSetting("Through Walls", "See through walls — the third-person camera ignores wall collision.", false)
   );
   public final SliderSetting sensitivity = this.addSetting(
      new SliderSetting("Camera Sensitivity", "How fast the camera moves in Camera mode.", 8.0, 0.0, 10.0, 0.1)
   );
   public final BooleanSetting arrows = this.addSetting(
      new BooleanSetting("Arrows Control Opposite", "Control the other entity's rotation with the arrow keys.", true)
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
               if (this.mode.is("Player")) {
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

   public boolean cameraMode() {
      return this.isActive() && this.mode.is("Camera");
   }

   public boolean playerMode() {
      return this.isActive() && MinecraftClient.getInstance().options.getPerspective() == Perspective.THIRD_PERSON_BACK && this.mode.is("Player");
   }

   public void addCameraLook(double d, double coord) {
      float f = this.sensitivity.getFloat();
      if (f <= 0.0F) {
         f = 1.0F;
      }

      this.cameraYaw += (float)(d / (double)f);
      this.cameraPitch += (float)(coord / (double)f);
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
