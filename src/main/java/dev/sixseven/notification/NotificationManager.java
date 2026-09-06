package dev.sixseven.notification;

import dev.sixseven.hud.HudComponent;
import dev.sixseven.hud.HudDragController;
import dev.sixseven.render.anim.Animation;
import dev.sixseven.render.anim.Easing;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dev.sixseven.module.client.HudModule;
public class NotificationManager extends HudComponent {
   private static final float WIDTH_PAD = 12.0F;
   private static final float HEIGHT = 30.0F;
   private static final float HEIGHT_WEATHER = 42.0F;
   private static final float GAP = 6.0F;
   private static final float FONT = 13.5F;
   private static final float FONT_SUB = 11.0F;
   private static final float GLYPH = 26.0F;
   private static final float ANCHOR_W = 168.0F;
   private static final float ANCHOR_H = 30.0F;
   private final ThemeManager themes;
   private final HudModule hud;
   private final List<NotificationManager.Toast> toasts = new ArrayList<>();

   public NotificationManager(ThemeManager themeManager, HudModule hudModule) {
      super("notifications", 0.99F, 0.71F, () -> hudModule.isEnabled() && hudModule.notifications.get());
      this.themes = themeManager;
      this.hud = hudModule;
   }

   private boolean enabled() {
      return this.hud.isEnabled() && this.hud.notifications.get();
   }

   private float lifeSeconds() {
      return this.hud.notifyDuration.getFloat();
   }

   public void push(String str, boolean value) {
      this.add(
         new NotificationManager.Toast(
            StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","r-">(str, value ? " enabled" : " disabled"),
            (String)null,
            (NotificationManager.Weather)null,
            value
         )
      );
   }

   public void pushInfo(String str) {
      this.add(new NotificationManager.Toast(str, (String)null, (NotificationManager.Weather)null, true));
   }

   public void pushWeather(String str, String str3, NotificationManager.Weather weather, boolean value) {
      this.add(new NotificationManager.Toast(str, str3, weather, value));
   }

   private void add(NotificationManager.Toast toast) {
      if (this.enabled()) {
         synchronized (this.toasts) {
            this.toasts.add(toast);
            if (this.toasts.size() > 6) {
               this.toasts.removeFirst();
            }
         }
      }
   }

   @Override
   public float measureWidth(NVGRenderer nVGRenderer) {
      return 168.0F;
   }

   @Override
   public float measureHeight(NVGRenderer nVGRenderer) {
      return 30.0F;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
   }

   public void renderToasts(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2) {
      Theme theme = this.themes.current();
      float f = this.lifeSeconds();
      float f14 = f * 1.0E9F;
      synchronized (this.toasts) {
         Iterator it = this.toasts.iterator();
         ArrayList list = new ArrayList();

         while (it.hasNext()) {
            NotificationManager.Toast toast = (NotificationManager.Toast)it.next();
            if ((float)(System.nanoTime() - toast.bornNanos) > f14 + 3.0E8F) {
               it.remove();
            } else {
               list.add(toast);
            }
         }

         boolean visible = HudDragController.isEditing();
         if (!list.isEmpty() || visible) {
            float f15 = this.getScale();
            float f16 = 168.0F * f15;
            float f17 = 30.0F * f15;
            float f18 = this.getFx() * (tickDelta - f16);
            float f19 = this.getFy() * (tickDelta2 - f17);
            nVGRenderer.save();
            nVGRenderer.translate((float)Math.round(f18), (float)Math.round(f19));
            nVGRenderer.scale(f15);
            float f20 = 168.0F;
            float f21 = 30.0F;

            for (int n = list.size() - 1; n >= 0; n--) {
               NotificationManager.Toast toast2 = (NotificationManager.Toast)list.get(n);
               float f22 = (float)(System.nanoTime() - toast2.bornNanos) / 1.0E9F;
               float f23 = Math.clamp((f + 0.3F - f22) / 0.3F, 0.0F, 1.0F);
               float f24 = toast2.slide.value();
               float f25 = this.drawToast(nVGRenderer, theme, f20, f21, f24, f23, toast2.title, toast2.subtitle, toast2.weather, toast2.accent);
               f21 -= f25 + 6.0F;
            }

            if (list.isEmpty() && visible) {
               this.drawToast(nVGRenderer, theme, f20, f21, 1.0F, 0.5F, "Notification", (String)null, (NotificationManager.Weather)null, true);
            }

            nVGRenderer.restore();
         }
      }
   }

   private float drawToast(
      NVGRenderer nVGRenderer, Theme theme, float f, float f12, float f13, float f14, String str, String str3, NotificationManager.Weather weather, boolean value
   ) {
      boolean visible = str3 != null;
      float f15 = visible ? 42.0F : 30.0F;
      float f16 = weather != null ? 26.0F : 0.0F;
      float f17 = 16.0F + f16;
      float f18 = Math.max(nVGRenderer.textWidth(str, 13.5F), visible ? nVGRenderer.textWidth(str3, 11.0F) : 0.0F);
      float f19 = f17 + f18 + 12.0F;
      float f20 = f - f19 + (1.0F - f13) * (f19 + 10.0F);
      float f21 = f12 - f15;
      int n = value ? theme.accent() : theme.textDisabled();
      nVGRenderer.save();
      nVGRenderer.alpha(f14);
      nVGRenderer.rectGradient(f20, f21, f19, f15, 9.0F, theme.background(), theme.backgroundTo(), true);
      nVGRenderer.rect(f20 + 3.0F, f21 + 5.0F, 3.0F, f15 - 10.0F, 1.5F, n);
      nVGRenderer.glow(f20, f21, f19, f15, 9.0F, 5.0F, Colors.withAlpha(n, 0.2F * f14));
      if (weather != null) {
         this.drawWeatherGlyph(nVGRenderer, f20 + 12.0F + 2.0F + f16 / 2.0F - 4.0F, f21 + f15 / 2.0F, weather, n, theme.accentBright());
      }

      if (visible) {
         if (value) {
            nVGRenderer.textGradient(str, f20 + f17, f21 + f15 / 2.0F - 8.0F, 13.5F, theme.accentBright(), theme.accent());
         } else {
            nVGRenderer.text(str, f20 + f17, f21 + f15 / 2.0F - 8.0F, 13.5F, theme.textPrimary());
         }

         nVGRenderer.text(str3, f20 + f17, f21 + f15 / 2.0F + 8.0F, 11.0F, theme.textMuted());
      } else if (value) {
         nVGRenderer.textGradient(str, f20 + f17, f21 + f15 / 2.0F, 13.5F, theme.accentBright(), theme.accent());
      } else {
         nVGRenderer.text(str, f20 + f17, f21 + f15 / 2.0F, 13.5F, theme.textMuted());
      }

      nVGRenderer.restore();
      return f15;
   }

   private void drawWeatherGlyph(NVGRenderer nVGRenderer, float f, float f6, NotificationManager.Weather weather, int n, int localX) {
      switch (weather) {
         case RAIN:
            this.drawCloud(nVGRenderer, f, f6 - 3.0F, n);

            for (int bestSlot = -1; bestSlot <= 1; bestSlot++) {
               float f7 = f + (float)bestSlot * 4.0F;
               nVGRenderer.line(f7 + 1.0F, f6 + 4.0F, f7 - 1.0F, f6 + 9.0F, 1.6F, n);
            }
            break;
         case THUNDER:
            this.drawCloud(nVGRenderer, f, f6 - 3.0F, n);
            nVGRenderer.line(f + 1.5F, f6 + 2.0F, f - 2.5F, f6 + 6.0F, 1.9F, localX);
            nVGRenderer.line(f - 2.5F, f6 + 6.0F, f + 1.5F, f6 + 6.0F, 1.9F, localX);
            nVGRenderer.line(f + 1.5F, f6 + 6.0F, f - 2.5F, f6 + 11.0F, 1.9F, localX);
            break;
         case CLEAR:
            nVGRenderer.circle(f, f6, 4.5F, localX);

            for (int localZ = 0; localZ < 8; localZ++) {
               double d = (double)localZ * Math.PI / 4.0;
               float f8 = (float)Math.cos(d);
               float f9 = (float)Math.sin(d);
               nVGRenderer.line(f + f8 * 6.5F, f6 + f9 * 6.5F, f + f8 * 9.0F, f6 + f9 * 9.0F, 1.6F, n);
            }
      }
   }

   private void drawCloud(NVGRenderer nVGRenderer, float f, float f3, int n) {
      nVGRenderer.rect(f - 7.0F, f3 - 1.0F, 14.0F, 5.0F, 2.5F, n);
      nVGRenderer.circle(f - 4.5F, f3 + 1.0F, 4.0F, n);
      nVGRenderer.circle(f + 4.5F, f3 + 1.0F, 4.0F, n);
      nVGRenderer.circle(f, f3 - 2.5F, 5.0F, n);
   }

   private static class Toast {
      final String title;
      final String subtitle;
      final NotificationManager.Weather weather;
      final boolean accent;
      final long bornNanos = System.nanoTime();
      final Animation slide = new Animation(220.0F, 0.0F, Easing.EASE_OUT_CUBIC);

      Toast(String str2, String str, NotificationManager.Weather weather, boolean visible) {
         this.title = str2;
         this.subtitle = str;
         this.weather = weather;
         this.accent = visible;
         this.slide.setTarget(1.0F);
      }
   }

   public static enum Weather {
      RAIN,
      THUNDER,
      CLEAR;

      private static NotificationManager.Weather[] $values() {
         return new NotificationManager.Weather[]{RAIN, THUNDER, CLEAR};
      }
   }
}
