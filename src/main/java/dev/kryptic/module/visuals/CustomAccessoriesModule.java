package dev.kryptic.module.visuals;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.Colors;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class CustomAccessoriesModule extends Module {
   public final ColorSetting color = this.addSetting(new ColorSetting("Color", "Base tint for every accessory", -1));
   public final BooleanSetting rainbow = this.addSetting(new BooleanSetting("Rainbow", "Cycle every accessory through the rainbow", false));
   public final SliderSetting glow = this.addSetting(new SliderSetting("Glow", "Soft outer-halo intensity", 70.0, 0.0, 100.0, 5.0, "V"));
   public final BooleanSetting firstPerson = this.addSetting(new BooleanSetting("First Person", "Also show your accessories in first-person view", false));
   public final BooleanSetting cape = this.addSetting(new BooleanSetting("Cape", "A flowing cloth cape down your back", true));
   public final ModeSetting capeStyle = this.addSetting(new ModeSetting("Cape Style", "Cape look", "Kryptic", "Kryptic", "Wave", "Grid", "Solid"));
   public final BooleanSetting capePhysics = this.addSetting(new BooleanSetting("Cape Physics", "Sway & billow with your movement", true));
   public final BooleanSetting trail = this.addSetting(new BooleanSetting("Trail", "A glowing trail left behind as you move", true));
   public final ModeSetting trailStyle = this.addSetting(new ModeSetting("Trail Style", "Trail look", "Ribbon", "Ribbon", "Sparkle", "Echo"));
   public final SliderSetting trailLength = this.addSetting(new SliderSetting("Trail Length", "How long the trail lingers", 1.2, 0.2, 4.0, 0.1, "s"));
   public final BooleanSetting aura = this.addSetting(new BooleanSetting("Aura", "An orbiting aura around your feet", false));
   public final ModeSetting auraStyle = this.addSetting(new ModeSetting("Aura Style", "Aura look", "Orbit", "Orbit", "Ring"));
   public final BooleanSetting crown = this.addSetting(new BooleanSetting("Crown", "A floating, spinning 67 above your head", false));
   public final BooleanSetting wings = this.addSetting(new BooleanSetting("Wings", "A pair of wings that beat as you move", false));
   public final ModeSetting wingStyle = this.addSetting(new ModeSetting("Wing Style", "Wing look", "Membrane", "Membrane", "Feathered", "Shard"));
   public final SliderSetting wingSpan = this.addSetting(new SliderSetting("Wing Span", "How far the wings reach out", 1.15, 0.5, 2.2, 0.05, "m"));
   public final BooleanSetting halo = this.addSetting(new BooleanSetting("Halo", "A ring of light above your head", false));
   public final ModeSetting haloStyle = this.addSetting(new ModeSetting("Halo Style", "Halo look", "Ring", "Ring", "Runes", "Double"));
   public final BooleanSetting footsteps = this.addSetting(new BooleanSetting("Footsteps", "Glowing prints left where you walk", false));
   public final SliderSetting footstepLife = this.addSetting(new SliderSetting("Footstep Life", "How long a print lingers", 3.0, 0.5, 10.0, 0.5, "s"));
   public final BooleanSetting groundGlow = this.addSetting(new BooleanSetting("Ground Glow", "A pulsing disc of light under your feet", false));
   private final Deque<CustomAccessoriesModule.TrailNode> trailNodes = new ArrayDeque<>();
   private static final int MAX_TRAIL = 256;
   private final Deque<CustomAccessoriesModule.Footstep> footprints = new ArrayDeque<>();
   private static final int MAX_FOOTPRINTS = 64;
   /** Prints alternate feet, and only land once the player has covered a stride. */
   private static final double STRIDE = 0.55;
   private boolean leftFoot;
   private double lastPrintX;
   private double lastPrintZ;
   private boolean hasPrinted;

   public CustomAccessoriesModule() {
      super("Accessories", "Client-side cosmetics — cape, trail, aura & crown", Category.VISUALS);
      ModeSetting modeSetting = this.capeStyle;
      BooleanSetting booleanSetting = this.cape;
      modeSetting.visibleWhen(booleanSetting::get);
      BooleanSetting booleanSetting2 = this.capePhysics;
      booleanSetting = this.cape;
      booleanSetting2.visibleWhen(booleanSetting::get);
      ModeSetting modeSetting2 = this.trailStyle;
      booleanSetting = this.trail;
      modeSetting2.visibleWhen(booleanSetting::get);
      SliderSetting sliderSetting = this.trailLength;
      booleanSetting = this.trail;
      sliderSetting.visibleWhen(booleanSetting::get);
      ModeSetting modeSetting3 = this.auraStyle;
      booleanSetting = this.aura;
      modeSetting3.visibleWhen(booleanSetting::get);
      this.wingStyle.visibleWhen(this.wings::get);
      this.wingSpan.visibleWhen(this.wings::get);
      this.haloStyle.visibleWhen(this.halo::get);
      this.footstepLife.visibleWhen(this.footsteps::get);
   }

   public Deque<CustomAccessoriesModule.TrailNode> trailNodes() {
      return this.trailNodes;
   }

   public Deque<CustomAccessoriesModule.Footstep> footprints() {
      return this.footprints;
   }

   /**
    * Drops a print once the player has covered a stride on the ground,
    * alternating feet. Distance rather than time is what makes the spacing
    * look like walking: a stationary player leaves no prints, and a sprinting
    * one leaves them no closer together than a walking one.
    */
   private void tickFootsteps(ClientPlayerEntity player) {
      long now = System.nanoTime();
      long cutoff = now - (long)((double)Math.max(0.5F, this.footstepLife.getFloat()) * 1.0E9);

      while (!this.footprints.isEmpty() && this.footprints.peekFirst().nanos < cutoff) {
         this.footprints.removeFirst();
      }

      if (!player.isOnGround()) {
         // airborne: the next landing starts a fresh stride
         this.hasPrinted = false;
         return;
      }

      double x = player.getX();
      double z = player.getZ();
      if (this.hasPrinted) {
         double dx = x - this.lastPrintX;
         double dz = z - this.lastPrintZ;
         if (dx * dx + dz * dz < STRIDE * STRIDE) return;
      }

      this.leftFoot = !this.leftFoot;
      this.footprints.addLast(new CustomAccessoriesModule.Footstep(
            x, player.getY() + 0.02, z, player.bodyYaw, this.leftFoot, now));
      this.lastPrintX = x;
      this.lastPrintZ = z;
      this.hasPrinted = true;

      while (this.footprints.size() > MAX_FOOTPRINTS) {
         this.footprints.removeFirst();
      }
   }

   @Override
   public void onTick() {
      ClientPlayerEntity self = MinecraftClient.getInstance().player;
      if (!this.footsteps.get()) {
         if (!this.footprints.isEmpty()) {
            this.footprints.clear();
         }
      } else if (self != null) {
         this.tickFootsteps(self);
      }

      if (!this.trail.get()) {
         if (!this.trailNodes.isEmpty()) {
            this.trailNodes.clear();
         }
      } else {
         ClientPlayerEntity player = MinecraftClient.getInstance().player;
         if (player != null) {
            double d = player.getX();
            double coord = player.getY() + (double)player.getHeight() * 0.5;
            double currentScore = player.getZ();
            this.trailNodes.addLast(new CustomAccessoriesModule.TrailNode(d, coord, currentScore, System.nanoTime()));
            long l = System.nanoTime() - (long)((double)Math.max(0.2F, this.trailLength.getFloat()) * 1.4E9);

            while (!this.trailNodes.isEmpty() && this.trailNodes.peekFirst().nanos < l) {
               this.trailNodes.removeFirst();
            }

            while (this.trailNodes.size() > 256) {
               this.trailNodes.removeFirst();
            }
         }
      }
   }

   @Override
   protected void onDisable() {
      this.clear();
   }

   public void clear() {
      this.trailNodes.clear();
      this.footprints.clear();
      this.hasPrinted = false;
   }

   public int currentRgb() {
      if (this.rainbow.get()) {
         float f = (float)(System.currentTimeMillis() % 4000L) / 4000.0F * 360.0F;
         return Colors.hsvToRgb(f, 0.8F, 1.0F) & 16777215;
      } else {
         return this.color.get() & 16777215;
      }
   }

   public float glowStrength() {
      return this.glow.getFloat() / 100.0F;
   }

   /** One print on the ground: where, which way it faced, and which foot. */
   public static final class Footstep {
      public final double x;
      public final double y;
      public final double z;
      public final float yaw;
      public final boolean left;
      public final long nanos;

      Footstep(double x, double y, double z, float yaw, boolean left, long nanos) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.yaw = yaw;
         this.left = left;
         this.nanos = nanos;
      }

      public float ageSeconds(long now) {
         return (float)(now - this.nanos) / 1.0E9F;
      }
   }

   public static final class TrailNode {
      public final double x;
      public final double y;
      public final double z;
      public final long nanos;

      TrailNode(double d2, double d3, double d, long l) {
         this.x = d2;
         this.y = d3;
         this.z = d;
         this.nanos = l;
      }

      public float ageSeconds(long l) {
         return (float)(l - this.nanos) / 1.0E9F;
      }
   }
}
