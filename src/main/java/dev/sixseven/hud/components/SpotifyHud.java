package dev.sixseven.hud.components;

import dev.sixseven.hud.HudComponent;
import dev.sixseven.render.nanovg.NVGImages;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.spotify.SpotifyService;
import dev.sixseven.spotify.SpotifyState;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryStack;

import dev.sixseven.module.client.SpotifyModule;
public class SpotifyHud extends HudComponent {
   public static final float WIDTH = 252.0F;
   public static final float HEIGHT = 74.0F;
   private static final float ART = 54.0F;
   private static final float TEXT_X = 76.0F;
   private static final float VOL_ROW = 18.0F;
   private static final float VBAR_X = 36.0F;
   private static final float VBAR_TRIM = 46.0F;
   private static final float VOL_CY = 80.0F;
   private final SpotifyModule module;
   private final SpotifyService service;
   private final ThemeManager themes;
   private static final long DEMO_START = System.nanoTime();

   public SpotifyHud(SpotifyModule spotifyModule, SpotifyService spotifyService, ThemeManager themeManager) {
      super("spotify", 0.5F, 0.965F, spotifyModule::isEnabled);
      this.module = spotifyModule;
      this.service = spotifyService;
      this.themes = themeManager;
   }

   private boolean demo() {
      return this.module.source.is("Demo");
   }

   private SpotifyState state() {
      if (this.demo()) {
         long l = (System.nanoTime() - DEMO_START) / 1000000L % 227000L;
         return new SpotifyState(true, "Neon Nights", "67 Sound", l, 227000L, true, true, 0, 70, System.nanoTime());
      } else {
         return this.service.state();
      }
   }

   private boolean showVolume() {
      return this.module.volume.get() && this.state().active();
   }

   @Override
   public float measureWidth(NVGRenderer nVGRenderer) {
      return 252.0F;
   }

   @Override
   public float measureHeight(NVGRenderer nVGRenderer) {
      return this.showVolume() ? 92.0F : 74.0F;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      SpotifyState spotifyState = this.state();
      Theme theme = this.themes.current();
      if (spotifyState.active() || !this.module.hideWhenIdle.get()) {
         nVGRenderer.glow(tickDelta, tickDelta2, tickDelta3, tickDelta4, 13.0F, 8.0F, Colors.withAlpha(-16777216, 0.3F));
         nVGRenderer.rectGradient(tickDelta, tickDelta2, tickDelta3, tickDelta4, 13.0F, Colors.withAlpha(-15264995, 0.92F), Colors.withAlpha(-15856878, 0.92F), true);
         float f = tickDelta + 10.0F;
         float f13 = tickDelta2 + 10.0F;
         int n = this.demo() ? -1 : NVGImages.fromFile(this.service.artPath(), spotifyState.artVersion());
         if (!this.demo() && n > 0) {
            this.drawRoundedImage(nVGRenderer, n, f, f13, 54.0F, 8.0F);
         } else {
            this.drawVinyl(nVGRenderer, theme, f, f13, 54.0F);
         }

         if (!spotifyState.active()) {
            nVGRenderer.text("Nothing playing", tickDelta + 76.0F, tickDelta2 + tickDelta4 / 2.0F, 13.5F, theme.textMuted());
         } else {
            float f14 = this.module.controls.get() ? tickDelta + tickDelta3 - 84.0F : tickDelta + tickDelta3 - 12.0F;
            nVGRenderer.textTruncated(spotifyState.title(), tickDelta + 76.0F, tickDelta2 + 20.0F, 14.0F, theme.textPrimary(), f14 - (tickDelta + 76.0F) - 6.0F);
            nVGRenderer.textTruncated(spotifyState.artist(), tickDelta + 76.0F, tickDelta2 + 38.0F, 11.5F, theme.textMuted(), f14 - (tickDelta + 76.0F) - 6.0F);
            if (this.module.controls.get()) {
               float f15 = tickDelta2 + 22.0F;
               int localX = theme.textMuted();
               this.drawPrev(nVGRenderer, tickDelta + tickDelta3 - 76.0F, f15, localX);
               this.drawPlayPause(nVGRenderer, tickDelta + tickDelta3 - 52.0F, f15, theme.accentBright(), spotifyState.playing());
               this.drawNext(nVGRenderer, tickDelta + tickDelta3 - 28.0F, f15, localX);
            }

            float f16 = tickDelta + 76.0F;
            float f17 = tickDelta3 - 76.0F - 12.0F;
            float f18 = tickDelta2 + 58.0F;
            float f19 = spotifyState.durMs() > 0L ? Math.clamp((float)spotifyState.livePosMs() / (float)spotifyState.durMs(), 0.0F, 1.0F) : 0.0F;
            nVGRenderer.text(time(spotifyState.livePosMs()), f16, tickDelta2 + 49.0F, 9.5F, theme.textDisabled());
            String text2 = time(spotifyState.durMs());
            nVGRenderer.text(text2, f16 + f17 - nVGRenderer.textWidth(text2, 9.5F), tickDelta2 + 49.0F, 9.5F, theme.textDisabled());
            nVGRenderer.rect(f16, f18, f17, 4.0F, 2.0F, Colors.withAlpha(-16777216, 0.5F));
            nVGRenderer.rectGradient(f16, f18, Math.max(4.0F, f17 * f19), 4.0F, 2.0F, theme.accent(), theme.accentBright(), false);
            nVGRenderer.circle(f16 + f17 * f19, f18 + 2.0F, 4.0F, -1);
            if (this.module.volume.get()) {
               int localZ = spotifyState.volume();
               float f20 = Math.clamp((float)(localZ < 0 ? 0 : localZ) / 100.0F, 0.0F, 1.0F);
               float f21 = tickDelta + 36.0F;
               float f22 = tickDelta3 - 36.0F - 46.0F;
               float f23 = tickDelta2 + 80.0F - 2.0F;
               this.drawSpeaker(nVGRenderer, tickDelta + 18.0F, tickDelta2 + 80.0F, theme.textMuted(), localZ == 0);
               nVGRenderer.rect(f21, f23, f22, 4.0F, 2.0F, Colors.withAlpha(-16777216, 0.5F));
               nVGRenderer.rectGradient(f21, f23, Math.max(4.0F, f22 * f20), 4.0F, 2.0F, theme.accent(), theme.accentBright(), false);
               nVGRenderer.circle(f21 + f22 * f20, tickDelta2 + 80.0F, 4.0F, -1);
               String text3 = localZ < 0 ? "--" : localZ + "%";
               nVGRenderer.text(text3, tickDelta + tickDelta3 - nVGRenderer.textWidth(text3, 9.5F) - 12.0F, tickDelta2 + 80.0F, 9.5F, theme.textDisabled());
            }
         }
      }
   }

   private void drawSpeaker(NVGRenderer nVGRenderer, float f, float f3, int n, boolean value) {
      long l = nVGRenderer.ctx();
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NVGColor nVGColor = NanoVG.nvgRGBA(
            (byte)Colors.red(n), (byte)Colors.green(n), (byte)Colors.blue(n), (byte)Colors.alpha(n), NVGColor.malloc(memoryStack)
         );
         nVGRenderer.rect(f - 7.0F, f3 - 3.0F, 5.0F, 6.0F, 1.0F, n);
         NanoVG.nvgFillColor(l, nVGColor);
         NanoVG.nvgBeginPath(l);
         NanoVG.nvgMoveTo(l, f - 6.0F, f3);
         NanoVG.nvgLineTo(l, f + 2.0F, f3 - 7.0F);
         NanoVG.nvgLineTo(l, f + 2.0F, f3 + 7.0F);
         NanoVG.nvgClosePath(l);
         NanoVG.nvgFill(l);
         NanoVG.nvgStrokeColor(l, nVGColor);
         NanoVG.nvgStrokeWidth(l, 1.7F);
         NanoVG.nvgLineCap(l, 1);
         if (value) {
            NanoVG.nvgBeginPath(l);
            NanoVG.nvgMoveTo(l, f + 5.0F, f3 - 3.5F);
            NanoVG.nvgLineTo(l, f + 10.0F, f3 + 3.5F);
            NanoVG.nvgMoveTo(l, f + 10.0F, f3 - 3.5F);
            NanoVG.nvgLineTo(l, f + 5.0F, f3 + 3.5F);
            NanoVG.nvgStroke(l);
         } else {
            for (float item : new float[]{4.5F, 8.0F}) {
               NanoVG.nvgBeginPath(l);
               NanoVG.nvgArc(l, f + 2.0F, f3, item, -0.6F, 0.6F, 2);
               NanoVG.nvgStroke(l);
            }
         }
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   private static String time(long l) {
      long l3 = l / 1000L;
      return String.format("%d:%02d", l3 / 60L, l3 % 60L);
   }

   private void drawRoundedImage(NVGRenderer nVGRenderer, int n, float f, float f5, float f6, float f7) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         long l = nVGRenderer.ctx();
         NVGPaint nVGPaint = NVGPaint.malloc(memoryStack);
         NanoVG.nvgImagePattern(l, f, f5, f6, f6, 0.0F, n, 1.0F, nVGPaint);
         NanoVG.nvgBeginPath(l);
         NanoVG.nvgRoundedRect(l, f, f5, f6, f6, f7);
         NanoVG.nvgFillPaint(l, nVGPaint);
         NanoVG.nvgFill(l);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   private void drawVinyl(NVGRenderer nVGRenderer, Theme theme, float f, float f6, float f7) {
      float f8 = f + f7 / 2.0F;
      float f9 = f6 + f7 / 2.0F;
      nVGRenderer.rect(f, f6, f7, f7, 8.0F, Colors.withAlpha(-16119795, 0.9F));
      nVGRenderer.circle(f8, f9, f7 * 0.4F, -15330789);
      nVGRenderer.circleOutline(f8, f9, f7 * 0.3F, 1.0F, Colors.withAlpha(theme.accent(), 0.35F));
      nVGRenderer.circleOutline(f8, f9, f7 * 0.22F, 1.0F, Colors.withAlpha(theme.accent(), 0.25F));
      nVGRenderer.circle(f8, f9, f7 * 0.12F, theme.accent());
      nVGRenderer.textGradient("67", f8 - nVGRenderer.textWidth("67", 9.0F) / 2.0F, f9, 9.0F, -1, -1122834);
   }

   private void drawPrev(NVGRenderer nVGRenderer, float f, float f3, int n) {
      this.triangle(nVGRenderer, f + 4.0F, f3, -7.0F, n);
      nVGRenderer.rect(f - 7.0F, f3 - 5.5F, 2.0F, 11.0F, 1.0F, n);
   }

   private void drawNext(NVGRenderer nVGRenderer, float f, float f3, int n) {
      this.triangle(nVGRenderer, f - 4.0F, f3, 7.0F, n);
      nVGRenderer.rect(f + 5.0F, f3 - 5.5F, 2.0F, 11.0F, 1.0F, n);
   }

   private void drawPlayPause(NVGRenderer nVGRenderer, float f, float f3, int n, boolean value) {
      nVGRenderer.circleOutline(f, f3, 10.0F, 1.4F, n);
      if (value) {
         nVGRenderer.rect(f - 3.5F, f3 - 4.5F, 2.4F, 9.0F, 1.2F, n);
         nVGRenderer.rect(f + 1.1F, f3 - 4.5F, 2.4F, 9.0F, 1.2F, n);
      } else {
         this.triangle(nVGRenderer, f - 2.5F, f3, 7.0F, n);
      }
   }

   private void triangle(NVGRenderer nVGRenderer, float f, float f4, float f5, int n) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         long l = nVGRenderer.ctx();
         NanoVG.nvgBeginPath(l);
         NanoVG.nvgMoveTo(l, f, f4 - 5.5F);
         NanoVG.nvgLineTo(l, f, f4 + 5.5F);
         NanoVG.nvgLineTo(l, f + f5, f4);
         NanoVG.nvgClosePath(l);
         NanoVG.nvgFillColor(
            l, NanoVG.nvgRGBA((byte)Colors.red(n), (byte)Colors.green(n), (byte)Colors.blue(n), (byte)Colors.alpha(n), NVGColor.malloc(memoryStack))
         );
         NanoVG.nvgFill(l);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   @Override
   public boolean onEditClick(float f, float f7) {
      SpotifyState spotifyState = this.state();
      if (!spotifyState.active()) {
         return false;
      } else {
         if (this.module.controls.get() && f7 >= 10.0F && f7 <= 34.0F) {
            if (hit(f, 176.0F)) {
               if (this.demo()) {
                  return true;
               }

               this.service.previous();
               return true;
            }

            if (hit(f, 200.0F)) {
               if (this.demo()) {
                  return true;
               }

               this.service.togglePlay();
               return true;
            }

            if (hit(f, 224.0F)) {
               if (this.demo()) {
                  return true;
               }

               this.service.next();
               return true;
            }
         }

         float f8 = 76.0F;
         float f9 = 164.0F;
         if (f7 >= 52.0F && f7 <= 66.0F && f >= f8 && f <= f8 + f9 && spotifyState.canSeek()) {
            long l = (long)((f - f8) / f9 * (float)spotifyState.durMs());
            if (!this.demo()) {
               this.service.seekTo(l);
            }

            return true;
         } else {
            float f10 = 36.0F;
            float f11 = 170.0F;
            if (this.module.volume.get() && f7 >= 73.0F && f7 <= 87.0F && f >= f10 && f <= f10 + f11) {
               int n = Math.round((f - f10) / f11 * 100.0F);
               if (!this.demo()) {
                  this.service.setVolume(n);
               }

               return true;
            } else {
               return false;
            }
         }
      }
   }

   private static boolean hit(float f, float f3) {
      return Math.abs(f - f3) <= 11.0F;
   }
}
