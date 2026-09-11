package dev.kryptic.hud.components;

import dev.kryptic.hud.HudComponent;
import dev.kryptic.render.nanovg.NVGImages;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.suschunk.SusChunkScanner;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import java.util.function.BooleanSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;

import dev.kryptic.module.client.HudModule;
import dev.kryptic.module.render.SusChunkFinderModule;
public class RadarHud extends HudComponent {
   private static final float SIZE = 110.0F;
   private final HudModule module;
   private final SusChunkFinderModule susFinder;
   private final ThemeManager themes;

   public RadarHud(HudModule hudModule, SusChunkFinderModule susChunkFinderModule, ThemeManager themeManager, BooleanSupplier booleanSupplier) {
      super("radar", 0.006F, 0.45F, booleanSupplier);
      this.module = hudModule;
      this.susFinder = susChunkFinderModule;
      this.themes = themeManager;
   }

   @Override
   public float measureWidth(NVGRenderer nVGRenderer) {
      return 110.0F;
   }

   @Override
   public float measureHeight(NVGRenderer nVGRenderer) {
      return 110.0F;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      Theme theme = this.themes.current();
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null) {
         float f = tickDelta3 / 2.0F;
         float f22 = tickDelta + f;
         float f23 = tickDelta2 + f;
         float f24 = 0.55F;
         nVGRenderer.circle(f22, f23, f, Colors.withAlpha(-15856621, f24));
         nVGRenderer.circleOutline(f22, f23, f * 0.5F, 1.0F, Colors.withAlpha(theme.accent(), 0.22F));
         int n = Colors.withAlpha(theme.accent(), 0.18F);
         nVGRenderer.line(f22 - f, f23, f22 + f, f23, 1.0F, n);
         nVGRenderer.line(f22, f23 - f, f22, f23 + f, 1.0F, n);
         float f25 = player.getYaw();
         float f26 = (float)Math.toRadians((double)(f25 + 90.0F));
         String[] text2 = new String[]{"=", "6", " ", "$"};
         float[][] f27 = new float[][]{{0.0F, -1.0F}, {1.0F, 0.0F}, {0.0F, 1.0F}, {-1.0F, 0.0F}};

         for (int localZ = 0; localZ < 4; localZ++) {
            float f28 = screenX(f27[localZ][0], f27[localZ][1], f26);
            float f29 = screenY(f27[localZ][0], f27[localZ][1], f26);
            float f30 = f22 + f28 * (f - 9.0F);
            float f31 = f23 + f29 * (f - 9.0F);
            boolean ok = localZ == 0;
            nVGRenderer.text(text2[localZ], f30 - nVGRenderer.textWidth(text2[localZ], 11.0F) / 2.0F, f31, 11.0F, ok ? theme.accentBright() : theme.textMuted());
         }

         float f32 = this.module.radarRange.getFloat();
         if (this.susFinder.isEnabled() && this.susFinder.showOnRadar.get()) {
            int localY = this.susFinder.scanner.threshold();

            for (SusChunkScanner.Zone zone : this.susFinder.scanner.zones()) {
               this.drawZoneBlip(
                  nVGRenderer,
                  theme,
                  f22,
                  f23,
                  f,
                  f32,
                  f26,
                  (float)(zone.centroidX() - player.getX()),
                  (float)(zone.centroidZ() - player.getZ()),
                  strength(zone.maxScore(), localY)
               );
            }
         }

         nVGRenderer.circleGlow(f22, f23, 3.0F, 4.0F, Colors.withAlpha(theme.accent(), 0.6F));
         nVGRenderer.circle(f22, f23, 3.0F, theme.accentBright());
         float f33 = Math.max(10.0F, f * 0.17F);

         for (AbstractClientPlayerEntity abstractClientPlayerEntity : MinecraftClient.getInstance().world.getPlayers()) {
            if (abstractClientPlayerEntity != player) {
               float f34 = (float)(abstractClientPlayerEntity.getX() - player.getX());
               float f35 = (float)(abstractClientPlayerEntity.getZ() - player.getZ());
               float f36 = (float)Math.sqrt((double)(f34 * f34 + f35 * f35));
               if (!(f36 > f32)) {
                  float f37 = f36 / f32;
                  float f38 = f22 + screenX(f34, f35, f26) / Math.max(f36, 0.001F) * f37 * (f - f33 / 2.0F - 3.0F);
                  float f39 = f23 + screenY(f34, f35, f26) / Math.max(f36, 0.001F) * f37 * (f - f33 / 2.0F - 3.0F);
                  if (this.module.radarHeads.get()) {
                     int step = NVGImages.wrapGlTexture(abstractClientPlayerEntity.getSkin().body().texturePath(), 64, 64);
                     if (step > 0) {
                        float f40 = f38 - f33 / 2.0F;
                        float f41 = f39 - f33 / 2.0F;
                        NVGImages.drawSubImage(nVGRenderer, step, 64.0F, 64.0F, 8.0F, 8.0F, 16.0F, 16.0F, f40, f41, f33, f33, 1.0F);
                        NVGImages.drawSubImage(nVGRenderer, step, 64.0F, 64.0F, 40.0F, 8.0F, 48.0F, 16.0F, f40, f41, f33, f33, 1.0F);
                        nVGRenderer.rectOutline(f40 - 1.0F, f41 - 1.0F, f33 + 2.0F, f33 + 2.0F, 3.0F, 1.0F, Colors.withAlpha(-1, 0.35F));
                        continue;
                     }
                  }

                  nVGRenderer.circleGlow(f38, f39, 2.5F, 3.0F, Colors.withAlpha(-1, 0.4F));
                  nVGRenderer.circle(f38, f39, 2.5F, -1);
               }
            }
         }
      }
   }

   private static float strength(double d, int n) {
      return n <= 0 ? 1.0F : Math.clamp((float)((d - (double)n) / ((double)n * 2.0)), 0.0F, 1.0F);
   }

   private void drawZoneBlip(NVGRenderer nVGRenderer, Theme theme, float f, float f20, float f21, float f22, float f23, float f24, float f25, float f26) {
      float f27 = (float)Math.sqrt((double)(f24 * f24 + f25 * f25));
      if (!(f27 < 0.5F)) {
         boolean ok = f27 > f22;
         float f28 = Math.min(f27 / f22, 1.0F);
         float f29 = ok ? 6.0F : 9.0F;
         float f30 = f + screenX(f24, f25, f23) / f27 * f28 * (f21 - f29);
         float f31 = f20 + screenY(f24, f25, f23) / f27 * f28 * (f21 - f29);
         double d = (double)(System.nanoTime() % 1000000000000L) / 1.0E9;
         float f32 = (float)(0.5 + 0.5 * Math.sin(d * Math.PI * 2.0 / 1.8));
         float f33 = (ok ? 2.4F : 3.0F + 1.5F * f26) * (1.0F + 0.1F * f32);
         float f34 = (0.72F + 0.28F * f26) * (0.88F + 0.12F * f32);
         int n = theme.accent();
         nVGRenderer.glow(
            f30 - f33,
            f31 - f33,
            f33 * 2.0F,
            f33 * 2.0F,
            f33 * 0.6F,
            3.5F + 2.5F * f32,
            Colors.withAlpha(n, (0.3F + 0.25F * f26) * f34)
         );
         nVGRenderer.rect(f30 - f33, f31 - f33, f33 * 2.0F, f33 * 2.0F, f33 * 0.6F, Colors.withAlpha(n, f34));
         nVGRenderer.rectOutline(f30 - f33, f31 - f33, f33 * 2.0F, f33 * 2.0F, f33 * 0.6F, 1.0F, Colors.withAlpha(theme.accentBright(), 0.55F * f34));
         if (ok && f27 <= f22 * 2.5F) {
            String text2 = "2" + (int)f27;
            float f35 = nVGRenderer.textWidth(text2, 8.5F);
            float f36 = f30 + (f - f30) * 0.24F;
            float f37 = f31 + (f20 - f31) * 0.24F;
            nVGRenderer.rect(f36 - f35 / 2.0F - 3.0F, f37 - 5.5F, f35 + 6.0F, 11.0F, 5.5F, Colors.withAlpha(-15856621, 0.72F));
            nVGRenderer.text(text2, f36 - f35 / 2.0F, f37, 8.5F, Colors.withAlpha(theme.accentBright(), 0.95F));
         }
      }
   }

   private static float screenX(float f, float f4, float f5) {
      return (float)((double)(-f) * Math.sin((double)f5) + (double)f4 * Math.cos((double)f5));
   }

   private static float screenY(float f, float f4, float f5) {
      return (float)(-((double)f * Math.cos((double)f5) + (double)f4 * Math.sin((double)f5)));
   }
}
