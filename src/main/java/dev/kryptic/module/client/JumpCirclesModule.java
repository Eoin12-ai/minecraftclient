package dev.kryptic.module.client;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.SliderSetting;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.random.Random;

public class JumpCirclesModule extends Module {
   public final SliderSetting size = this.addSetting(new SliderSetting("Size", "Decal scale (1.0 = one block wide)", 1.0, 0.5, 3.0, 0.1, "x"));
   public final ColorSetting color = this.addSetting(new ColorSetting("Color", "Decal color (alpha is animated)", -38476));
   public final SliderSetting lifetime = this.addSetting(new SliderSetting("Lifetime", "How long a decal stays visible", 1.5, 0.5, 4.0, 0.1, "s"));
   public final BooleanSetting rainbow = this.addSetting(new BooleanSetting("Rainbow", "Cycle the color through the rainbow", false));
   public final BooleanSetting shockwave = this.addSetting(new BooleanSetting("Shockwave", "Expanding ring on spawn", true));
   public final BooleanSetting particles = this.addSetting(new BooleanSetting("Particles", "Drifting dust motes on jump", true));
   public final SliderSetting maxCircles = this.addSetting(new SliderSetting(">M0", "Max simultaneous decals (oldest drops first)", 10.0, 1.0, 30.0, 1.0));
   private final Deque<JumpCirclesModule.JumpCircle> circles = new ArrayDeque<>();
   private int spawnCounter;

   public JumpCirclesModule() {
      super("KrypticJumpCircles", "Stamps a glowing Kryptic decal on the ground when you jump", Category.CLIENT);
   }

   public Deque<JumpCirclesModule.JumpCircle> circles() {
      return this.circles;
   }

   @Override
   protected void onDisable() {
      this.clear();
   }

   public void clear() {
      this.circles.clear();
   }

   public int baseRgb() {
      return this.color.get() & 16777215;
   }

   public void onPlayerJump(ClientPlayerEntity player) {
      if (this.isEnabled()) {
         double d = player.getX();
         double coord = player.getY();
         double currentScore = player.getZ();
         float f = 0.01F + (float)(this.spawnCounter % 8) * 0.001F;
         this.spawnCounter++;
         this.circles.addLast(new JumpCirclesModule.JumpCircle(d, coord, currentScore, player.getYaw(), f, System.nanoTime()));
         int n = Math.max(1, this.maxCircles.getInt());

         while (this.circles.size() > n) {
            this.circles.removeFirst();
         }

         if (this.particles.get()) {
            this.spawnParticles(player);
         }
      }
   }

   private void spawnParticles(ClientPlayerEntity player) {
      Random random = player.getRandom();
      int n = this.baseRgb();

      for (int offset = 0; offset < 10; offset++) {
         double d = random.nextDouble() * Math.PI * 2.0;
         double coord = 0.15 + random.nextDouble() * 0.45;
         player.getEntityWorld()
            .addParticleClient(
               new DustParticleEffect(n, 0.9F),
               player.getX() + Math.cos(d) * coord,
               player.getY() + 0.05,
               player.getZ() + Math.sin(d) * coord,
               0.0,
               0.6 + random.nextDouble() * 0.4,
               0.0
            );
      }
   }

   public static final class JumpCircle {
      public final double x;
      public final double y;
      public final double z;
      public final float yawDegrees;
      public final float yLift;
      public final long spawnNanos;

      JumpCircle(double d, double d3, double d2, float f2, float f, long l) {
         this.x = d;
         this.y = d3;
         this.z = d2;
         this.yawDegrees = f2;
         this.yLift = f;
         this.spawnNanos = l;
      }

      public float ageSeconds(long l) {
         return (float)(l - this.spawnNanos) / 1.0E9F;
      }
   }
}
