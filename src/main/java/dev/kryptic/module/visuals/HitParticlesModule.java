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
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class HitParticlesModule extends Module {
   public static final int STYLE_SPARKS = 0;
   public static final int STYLE_HEARTS = 1;
   public static final int STYLE_LIGHTNING = 2;
   public static final int STYLE_67 = 3;
   public final ModeSetting style = this.addSetting(new ModeSetting("Style", "Burst style", "Sparks", "Sparks", "Hearts", "Lightning", "67"));
   public final SliderSetting amount = this.addSetting(new SliderSetting("Amount", "Particles spawned per hit", 14.0, 4.0, 40.0, 1.0));
   public final SliderSetting size = this.addSetting(new SliderSetting("Size", "Particle scale", 1.0, 0.3, 3.0, 0.1, "x"));
   public final SliderSetting lifetime = this.addSetting(new SliderSetting("Lifetime", "How long the burst lingers", 0.7, 0.3, 2.0, 0.1, "s"));
   public final SliderSetting spread = this.addSetting(new SliderSetting("Spread", "How far particles fly out", 1.0, 0.3, 2.5, 0.1, "x"));
   public final ColorSetting color = this.addSetting(new ColorSetting("Color", "Particle tint", -49508));
   public final BooleanSetting rainbow = this.addSetting(new BooleanSetting("Rainbow", "Cycle each burst through the rainbow", false));
   public final SliderSetting glow = this.addSetting(new SliderSetting("Glow", "Soft outer halo intensity", 65.0, 0.0, 100.0, 5.0, "V"));
   public final BooleanSetting shockwave = this.addSetting(new BooleanSetting("Shockwave", "Expanding ring on each hit", true));
   private final Deque<HitParticlesModule.HitParticle> particles = new ArrayDeque<>();
   private final Deque<HitParticlesModule.Shock> shocks = new ArrayDeque<>();
   private static final int MAX_PARTICLES = 600;
   private static final int MAX_SHOCKS = 24;

   public HitParticlesModule() {
      super("HitParticles", "Themed particle bursts when you hit an entity", Category.VISUALS);
   }

   public Deque<HitParticlesModule.HitParticle> particles() {
      return this.particles;
   }

   public Deque<HitParticlesModule.Shock> shocks() {
      return this.shocks;
   }

   @Override
   protected void onDisable() {
      this.clear();
   }

   public void clear() {
      this.particles.clear();
      this.shocks.clear();
   }

   private int currentRgb() {
      if (this.rainbow.get()) {
         float f = (float)(System.currentTimeMillis() % 3500L) / 3500.0F * 360.0F;
         return Colors.hsvToRgb(f, 0.85F, 1.0F) & 16777215;
      } else {
         return this.color.get() & 16777215;
      }
   }

   public void onHit(Entity entity) {
      if (this.isEnabled() && entity != null) {
         double d = entity.getX();
         double coord = entity.getY() + (double)entity.getHeight() * 0.6;
         double currentScore = entity.getZ();
         this.spawnAt(d, coord, currentScore);
      }
   }

   public void spawnAt(double d, double coord, double currentScore) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world != null) {
         Random random = client.world.random;
         int n = this.styleId();
         int localZ = this.currentRgb();
         int localY = this.amount.getInt();
         float f = this.lifetime.getFloat();
         float f4 = this.spread.getFloat();
         float f5 = this.size.getFloat();
         long l = System.nanoTime();

         for (int step = 0; step < localY; step++) {
            this.spawnOne(random, n, localZ, d, coord, currentScore, f, f4, f5, l);
         }

         if (this.shockwave.get()) {
            this.shocks.addLast(new HitParticlesModule.Shock(d, coord, currentScore, localZ, n, l));

            while (this.shocks.size() > 24) {
               this.shocks.removeFirst();
            }
         }

         while (this.particles.size() > 600) {
            this.particles.removeFirst();
         }
      }
   }

   private void spawnOne(Random random, int n, int offset, double d, double coord, double currentScore, float f, float f17, float f18, long l) {
      double coord3 = random.nextDouble() * Math.PI * 2.0;
      double coord4 = 2.0 * random.nextDouble() - 1.0;
      double coord5 = Math.sqrt(Math.max(0.0, 1.0 - coord4 * coord4));
      float f19 = (float)(coord5 * Math.cos(coord3));
      float f20 = (float)coord4;
      float f21 = (float)(coord5 * Math.sin(coord3));
      float f22 = f * (0.75F + random.nextFloat() * 0.25F);
      float f23 = f18 * (0.7F + random.nextFloat() * 0.6F);
      float f24;
      float f25;
      switch (n) {
         case 1:
            f19 *= 0.5F;
            f21 *= 0.5F;
            f20 = 0.4F + Math.abs(f20) * 0.5F;
            f24 = (1.6F + random.nextFloat() * 1.0F) * f17;
            f25 = 1.2F;
            f22 = f * (1.0F + random.nextFloat() * 0.3F);
            break;
         case 2:
            f20 *= 0.25F;
            float f26 = MathHelper.sqrt(Math.max(1.0E-4F, f19 * f19 + f21 * f21));
            f19 /= f26;
            f21 /= f26;
            f24 = (5.0F + random.nextFloat() * 3.0F) * f17;
            f25 = 0.6F;
            f22 = f * (0.45F + random.nextFloat() * 0.25F);
            break;
         case 3:
            f20 = 0.12F + f20 * 0.35F;
            f24 = (1.9F + random.nextFloat() * 1.4F) * f17;
            f25 = 1.3F;
            f22 = f * (1.0F + random.nextFloat() * 0.25F);
            break;
         default:
            f20 = f20 * 0.7F + 0.3F;
            f24 = (4.0F + random.nextFloat() * 3.5F) * f17;
            f25 = 6.5F;
      }

      float f27 = f19 * f24;
      float f28 = f20 * f24;
      float f29 = f21 * f24;
      float f30 = random.nextFloat() * (float) (Math.PI * 2);
      float f31 = (random.nextFloat() - 0.5F) * 8.0F;
      this.particles.addLast(new HitParticlesModule.HitParticle(d, coord, currentScore, f27, f28, f29, offset, n, f23, f30, f31, f25, f22, l));
   }

   private int styleId() {
      if (this.style.is("Hearts")) {
         return 1;
      } else if (this.style.is("Lightning")) {
         return 2;
      } else {
         return this.style.is("67") ? 3 : 0;
      }
   }

   public float glowStrength() {
      return this.glow.getFloat() / 100.0F;
   }

   public static final class HitParticle {
      public final double ox;
      public final double oy;
      public final double oz;
      public final float vx;
      public final float vy;
      public final float vz;
      public final int rgb;
      public final int styleId;
      public final float size;
      public final float rot;
      public final float rotSpeed;
      public final float gravity;
      public final float lifetime;
      public final long spawnNanos;

      HitParticle(
         double d,
         double d3,
         double d2,
         float f8,
         float f6,
         float f7,
         int index,
         int n,
         float f5,
         float f,
         float f3,
         float f4,
         float f2,
         long l
      ) {
         this.ox = d;
         this.oy = d3;
         this.oz = d2;
         this.vx = f8;
         this.vy = f6;
         this.vz = f7;
         this.rgb = index;
         this.styleId = n;
         this.size = f5;
         this.rot = f;
         this.rotSpeed = f3;
         this.gravity = f4;
         this.lifetime = f2;
         this.spawnNanos = l;
      }

      public float ageSeconds(long l) {
         return (float)(l - this.spawnNanos) / 1.0E9F;
      }

      public double getX(float f) {
         return this.ox + (double)(this.vx * f);
      }

      public double getY(float f) {
         return this.oy + (double)(this.vy * f) - 0.5 * (double)this.gravity * (double)f * (double)f;
      }

      public double getZ(float f) {
         return this.oz + (double)(this.vz * f);
      }
   }

   public static final class Shock {
      public final double x;
      public final double y;
      public final double z;
      public final int rgb;
      public final int styleId;
      public final long spawnNanos;

      Shock(double d, double d3, double d2, int index, int n, long l) {
         this.x = d;
         this.y = d3;
         this.z = d2;
         this.rgb = index;
         this.styleId = n;
         this.spawnNanos = l;
      }

      public float ageSeconds(long l) {
         return (float)(l - this.spawnNanos) / 1.0E9F;
      }
   }
}
