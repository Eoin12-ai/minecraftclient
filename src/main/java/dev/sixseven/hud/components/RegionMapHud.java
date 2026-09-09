package dev.sixseven.hud.components;

import dev.sixseven.hud.HudComponent;
import dev.sixseven.hud.HudSurface;
import dev.sixseven.module.render.RegionMapModule;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class RegionMapHud extends HudComponent {
   private static final float PAD = 8.0F;
   private static final float CELL = 12.0F;
   private static final float GRID = 108.0F;
   private static final float HEADER_H = 15.0F;
   private static final float GAP_HEADER = 5.0F;
   private static final float GAP_LEGEND = 8.0F;
   private static final float LEGEND_ROW = 12.0F;
   private static final int LEGEND_COLS = 2;
   private final RegionMapModule module;
   private final ThemeManager themes;

   public RegionMapHud(RegionMapModule regionMapModule, ThemeManager themeManager) {
      super("regionMap", 0.008F, 0.05F, regionMapModule::isEnabled);
      this.module = regionMapModule;
      this.themes = themeManager;
   }

   private float legendRows() {
      return (float)Math.ceil((double)this.module.regionTypeCount() / 2.0);
   }

   @Override
   public float measureWidth(NVGRenderer nVGRenderer) {
      return 124.0F;
   }

   @Override
   public float measureHeight(NVGRenderer nVGRenderer) {
      float temp = 144.0F;
      if (this.module.legend.get()) {
         temp += 8.0F + this.legendRows() * 12.0F;
      }

      return temp;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      Theme theme = this.themes.current();
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player != null) {
         float f = 10.0F;
         nVGRenderer.glow(tickDelta, tickDelta2, tickDelta3, tickDelta4, 13.0F, 8.0F, Colors.withAlpha(-16777216, 0.3F));
         HudSurface.panel(nVGRenderer, tickDelta, tickDelta2, tickDelta3, tickDelta4, f, theme);
         int n = this.module.currentRegionId();
         int offset = this.module.regionTypeAtWorld(player.getX(), player.getZ());
         this.drawHeader(nVGRenderer, theme, tickDelta, tickDelta2, tickDelta3, n);
         float f4 = tickDelta + 8.0F;
         float f5 = tickDelta2 + 8.0F + 15.0F + 5.0F;
         this.drawGrid(nVGRenderer, theme, player, f4, f5);
         if (this.module.legend.get()) {
            this.drawLegend(nVGRenderer, theme, f4, f5 + 108.0F + 8.0F, offset);
         }
      }
   }

   private void drawHeader(NVGRenderer nVGRenderer, Theme theme, float f, float f10, float f11, int n) {
      float f12 = f10 + 8.0F + 7.5F;
      nVGRenderer.text("REGION MAP", f + 8.0F, f12, 9.0F, theme.textPrimary());
      String text2 = n >= 0 ? "|" + n : "N/A";
      float f13 = nVGRenderer.textWidth(text2, 8.5F);
      float f14 = f13 + 9.0F;
      float f15 = 12.5F;
      float f16 = f + f11 - 8.0F - f14;
      float f17 = f12 - f15 / 2.0F;
      boolean ok = n >= 0;
      nVGRenderer.rect(f16, f17, f14, f15, 6.0F, Colors.withAlpha(theme.accent(), ok ? 0.18F : 0.1F));
      nVGRenderer.rectOutline(f16, f17, f14, f15, 6.0F, 1.0F, Colors.withAlpha(theme.accent(), ok ? 0.45F : 0.2F));
      nVGRenderer.text(text2, f16 + 4.5F, f12, 8.5F, ok ? theme.accentBright() : theme.textMuted());
   }

   private void drawGrid(NVGRenderer nVGRenderer, Theme theme, ClientPlayerEntity player, float f, float f14) {
      int n = this.module.mapSize();
      int n13 = (int)(Math.clamp(this.module.opacity.getFloat() / 100.0F, 0.0F, 1.0F) * 255.0F);
      nVGRenderer.rect(f, f14, 108.0F, 108.0F, 4.0F, Colors.withAlpha(-16054000, 0.92F));
      nVGRenderer.save();
      nVGRenderer.scissor(f, f14, 108.0F, 108.0F);

      for (int n14 = 0; n14 < n; n14++) {
         for (int n15 = 0; n15 < n; n15++) {
            int n16 = this.module.regionTypeAt(n14 * n + n15);
            if (n16 >= 0) {
               int n17 = Colors.withAlpha(0xFF000000 | this.module.regionTypeRgb(n16), n13);
               float f15 = f + (float)n15 * 12.0F;
               float f16 = f14 + (float)n14 * 12.0F;
               nVGRenderer.rect(f15 + 1.0F, f16 + 1.0F, 10.0F, 10.0F, 1.5F, n17);
            }
         }
      }

      if (this.module.gridLines.get()) {
         int n18 = Colors.withAlpha(theme.accent(), 0.14F);

         for (int n19 = 0; n19 <= n; n19++) {
            float f17 = (float)n19 * 12.0F;
            nVGRenderer.line(f + f17, f14, f + f17, f14 + 108.0F, 1.0F, n18);
            nVGRenderer.line(f, f14 + f17, f + 108.0F, f14 + f17, 1.0F, n18);
         }
      }

      int[] n20 = this.module.worldToGrid(player.getX(), player.getZ());
      boolean ok = n20[0] >= 0 && n20[0] < n && n20[1] >= 0 && n20[1] < n;
      if (ok) {
         float f18 = f + (float)n20[0] * 12.0F;
         float f19 = f14 + (float)n20[1] * 12.0F;
         nVGRenderer.glow(f18, f19, 12.0F, 12.0F, 2.0F, 3.0F, Colors.withAlpha(theme.accent(), 0.35F));
         nVGRenderer.rectOutline(f18 + 0.5F, f19 + 0.5F, 11.0F, 11.0F, 2.0F, 1.2F, theme.accentBright());
      }

      if (this.module.cellNumbers.get()) {
         for (int n21 = 0; n21 < n; n21++) {
            for (int n22 = 0; n22 < n; n22++) {
               int n23 = this.module.regionTypeAt(n21 * n + n22);
               if (n23 >= 0) {
                  String text2 = String.valueOf(this.module.regionIdAt(n21 * n + n22));
                  float f20 = text2.length() >= 3 ? 5.5F : 6.5F;
                  float f21 = nVGRenderer.textWidth(text2, f20);
                  float f22 = f + (float)n22 * 12.0F + (12.0F - f21) / 2.0F;
                  float f23 = f14 + (float)n21 * 12.0F + 6.0F;
                  nVGRenderer.text(text2, f22 + 0.5F, f23 + 0.5F, f20, Colors.withAlpha(-16777216, 0.55F));
                  nVGRenderer.text(text2, f22, f23, f20, -790280);
               }
            }
         }
      }

      if (ok) {
         double[] d = this.module.worldToCellPosition(player.getX(), player.getZ());
         float f24 = f + (float)(((double)n20[0] + d[0]) * 12.0);
         float f25 = f14 + (float)(((double)n20[1] + d[1]) * 12.0);
         nVGRenderer.circleGlow(f24, f25, 2.2F, 3.5F, Colors.withAlpha(theme.accent(), 0.75F));
         nVGRenderer.save();
         nVGRenderer.translate(f24, f25);
         nVGRenderer.rotate((float)Math.toRadians((double)(player.getYaw() + 180.0F)));
         nVGRenderer.triangle(0.0F, -4.9F, 3.3F, 2.9F, -3.3F, 2.9F, Colors.withAlpha(-16777216, 0.55F));
         nVGRenderer.triangle(0.0F, -4.0F, 2.6F, 2.2F, -2.6F, 2.2F, theme.accentBright());
         nVGRenderer.restore();
      }

      nVGRenderer.restore();
      nVGRenderer.rectOutline(f, f14, 108.0F, 108.0F, 4.0F, 1.0F, Colors.withAlpha(theme.accent(), 0.5F));
   }

   private void drawLegend(NVGRenderer nVGRenderer, Theme theme, float f, float f6, int n) {
      float f7 = 54.0F;

      for (int localZ = 0; localZ < this.module.regionTypeCount(); localZ++) {
         int localY = localZ % 2;
         int step = localZ / 2;
         float f8 = f + (float)localY * f7;
         float f9 = f6 + (float)step * 12.0F + 6.0F;
         nVGRenderer.rect(f8, f9 - 2.5F, 5.0F, 5.0F, 1.2F, 0xFF000000 | this.module.regionTypeRgb(localZ));
         boolean ok = localZ == n;
         nVGRenderer.text(this.module.regionTypeName(localZ), f8 + 8.0F, f9, 7.5F, ok ? theme.accentBright() : theme.textMuted());
      }
   }
}
