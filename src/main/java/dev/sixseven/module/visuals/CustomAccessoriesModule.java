package dev.sixseven.module.visuals;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.ColorSetting;
import dev.sixseven.settings.ModeSetting;
import dev.sixseven.settings.SliderSetting;
import dev.sixseven.util.Colors;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class CustomAccessoriesModule extends Module {
   public final ColorSetting color = this.addSetting(new ColorSetting("Color", "Base tint for every accessory", -49508));
   public final BooleanSetting rainbow = this.addSetting(new BooleanSetting("Rainbow", "Cycle every accessory through the rainbow", false));
   public final SliderSetting glow = this.addSetting(new SliderSetting("Glow", "Soft outer-halo intensity", 70.0, 0.0, 100.0, 5.0, "V"));
   public final BooleanSetting firstPerson = this.addSetting(new BooleanSetting("First Person", "Also show your accessories in first-person view", false));
   public final BooleanSetting cape = this.addSetting(new BooleanSetting("Cape", "A flowing cloth cape down your back", true));
   public final ModeSetting capeStyle = this.addSetting(new ModeSetting("Cape Style", "Cape look", "67", "67", "Wave", "Grid", "Solid"));
   public final BooleanSetting capePhysics = this.addSetting(new BooleanSetting("Cape Physics", "Sway & billow with your movement", true));
   public final BooleanSetting trail = this.addSetting(new BooleanSetting("Trail", "A glowing trail left behind as you move", true));
   public final ModeSetting trailStyle = this.addSetting(new ModeSetting("Trail Style", "Trail look", "Ribbon", "Ribbon", "Sparkle", "Echo"));
   public final SliderSetting trailLength = this.addSetting(new SliderSetting("Trail Length", "How long the trail lingers", 1.2, 0.2, 4.0, 0.1, "s"));
   public final BooleanSetting aura = this.addSetting(new BooleanSetting("Aura", "An orbiting aura around your feet", false));
   public final ModeSetting auraStyle = this.addSetting(new ModeSetting("Aura Style", "Aura look", "Orbit", "Orbit", "Ring"));
   public final BooleanSetting crown = this.addSetting(new BooleanSetting("Crown", "A floating, spinning 67 above your head", false));
   private final Deque<CustomAccessoriesModule.TrailNode> trailNodes = new ArrayDeque<>();
   private static final int MAX_TRAIL = 256;

   public CustomAccessoriesModule() {
      super("CustomAccessories", "Client-side cosmetics — cape, trail, aura & crown", Category.VISUALS);
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
   }

   public Deque<CustomAccessoriesModule.TrailNode> trailNodes() {
      return this.trailNodes;
   }

   @Override
   public void onTick() {
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
