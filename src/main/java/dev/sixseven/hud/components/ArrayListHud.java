package dev.sixseven.hud.components;

import dev.sixseven.hud.HudComponent;
import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.render.anim.Animation;
import dev.sixseven.render.anim.Easing;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import dev.sixseven.module.client.HudModule;
public class ArrayListHud extends HudComponent {
   private static final float ENTRY_HEIGHT = 20.0F;
   private static final float FONT_SIZE = 13.5F;
   private static final float PAD_X = 8.0F;
   private static final float STRIP_W = 2.5F;
   private final ModuleManager modules;
   private final HudModule hudModule;
   private final ThemeManager themes;
   private final Map<Module, Animation> slide = new HashMap<>();

   public ArrayListHud(ModuleManager moduleManager, HudModule hudModule2, ThemeManager themeManager, BooleanSupplier booleanSupplier) {
      super("arraylist", 1.0F, 0.008F, booleanSupplier);
      this.modules = moduleManager;
      this.hudModule = hudModule2;
      this.themes = themeManager;
   }

   private List<Module> animatedEntries(NVGRenderer nVGRenderer) {
      ArrayList list = new ArrayList();

      for (Module module : this.modules.all()) {
         if (module.getCategory() != Category.CLIENT) {
            Animation animation = this.slide.computeIfAbsent(module, arg -> new Animation(240.0F, arg.isEnabled() ? 1.0F : 0.0F, Easing.EASE_OUT_CUBIC));
            animation.setTarget(module.isEnabled() ? 1.0F : 0.0F);
            if (module.isEnabled() || animation.value() > 0.01F) {
               list.add(module);
            }
         }
      }

      list.sort(Comparator.<Module>comparingDouble(arg2 -> (double)nVGRenderer.textWidth(arg2.getName(), 13.5F)).reversed());
      return list;
   }

   @Override
   public float measureWidth(NVGRenderer nVGRenderer) {
      float temp = 40.0F;

      for (Module module : this.animatedEntries(nVGRenderer)) {
         temp = Math.max(temp, nVGRenderer.textWidth(module.getName(), 13.5F) + 16.0F + 2.5F + 2.0F);
      }

      return temp;
   }

   @Override
   public float measureHeight(NVGRenderer nVGRenderer) {
      float f = 0.0F;

      for (Module module : this.animatedEntries(nVGRenderer)) {
         f += 20.0F * this.slide.get(module).value();
      }

      return Math.max(20.0F, f);
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      Theme theme = this.themes.current();
      boolean ok = this.hudModule.themeSync.get();
      boolean ok2 = this.rightAnchored();
      float f = tickDelta2;

      for (Module module : this.animatedEntries(nVGRenderer)) {
         float f10 = this.slide.get(module).value();
         if (!(f10 <= 0.01F)) {
            int n = ok ? theme.accent() : this.hudModule.listColor.get();
            int offset = ok ? theme.accentBright() : Colors.lighten(n, 0.35F);
            float f11 = nVGRenderer.textWidth(module.getName(), 13.5F);
            float f12 = f11 + 16.0F + 2.5F + 2.0F;
            float f13 = (1.0F - f10) * (f12 + 12.0F) * (float)(ok2 ? 1 : -1);
            float f14 = (float)Math.round((ok2 ? tickDelta + tickDelta3 - f12 : tickDelta) + f13);
            float f15 = (float)Math.round(f);
            nVGRenderer.save();
            nVGRenderer.alpha(f10);
            nVGRenderer.rectGradient(f14, f15, f12, 20.0F, 5.0F, Colors.withAlpha(-15330788, 0.86F), Colors.withAlpha(-15791084, 0.86F), true);
            float f16 = ok2 ? f14 + f12 - 2.5F : f14;
            nVGRenderer.glow(f16 - 1.0F, f15 + 2.0F, 4.5F, 16.0F, 2.0F, 3.0F, Colors.withAlpha(n, 0.35F));
            nVGRenderer.rect(f16, f15 + 2.0F, 2.5F, 16.0F, 1.25F, n);
            float f17 = (float)Math.round(ok2 ? f14 + 8.0F : f14 + 2.5F + 2.0F + 8.0F - 2.0F);
            nVGRenderer.textGradient(module.getName(), f17, f15 + 10.0F, 13.5F, offset, n);
            nVGRenderer.restore();
            f += 20.0F * f10;
         }
      }
   }
}
