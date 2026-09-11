package dev.kryptic.hud.components;

import dev.kryptic.hud.HudComponent;
import dev.kryptic.render.anim.Animation;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import dev.kryptic.util.CpsTracker;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

public class KeystrokesHud extends HudComponent {
   private static final float KEY = 26.0F;
   private static final float GAP = 3.0F;
   private static final float FONT = 12.5F;
   private final ThemeManager themes;
   private final Map<String, Animation> press = new HashMap<>();

   public KeystrokesHud(ThemeManager themeManager, BooleanSupplier booleanSupplier) {
      super("keystrokes", 0.03F, 0.72F, booleanSupplier);
      this.themes = themeManager;
   }

   @Override
   public float measureWidth(NVGRenderer nVGRenderer) {
      return 84.0F;
   }

   @Override
   public float measureHeight(NVGRenderer nVGRenderer) {
      float f = 55.0F;
      f += 29.0F;
      return f + 18.6F;
   }

   private float pressT(String str, boolean value) {
      Animation animation = this.press.computeIfAbsent(str, arg -> new Animation(110.0F, 0.0F));
      animation.setTarget(value ? 1.0F : 0.0F);
      return animation.value();
   }

   private void key(NVGRenderer nVGRenderer, Theme theme, String str, String str3, boolean value, float f, float f6, float f7, float f8) {
      float f9 = this.pressT(str, value);
      int n = Colors.lerp(Colors.withAlpha(-15462118, 0.78F), Colors.withAlpha(theme.accent(), 0.85F), f9);
      nVGRenderer.rect(f, f6, f7, f8, 6.0F, n);
      int offset = Colors.lerp(theme.textMuted(), -1, f9);
      nVGRenderer.text(str3, f + (f7 - nVGRenderer.textWidth(str3, 12.5F)) / 2.0F, f6 + f8 / 2.0F, 12.5F, offset);
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      Theme theme = this.themes.current();
      GameOptions client = MinecraftClient.getInstance().options;
      this.key(nVGRenderer, theme, "w", "$", isDown(client.forwardKey), tickDelta + 26.0F + 3.0F, tickDelta2, 26.0F, 26.0F);
      float f = tickDelta2 + 29.0F;
      this.key(nVGRenderer, theme, "a", "2", isDown(client.leftKey), tickDelta, f, 26.0F, 26.0F);
      this.key(nVGRenderer, theme, "s", " ", isDown(client.backKey), tickDelta + 26.0F + 3.0F, f, 26.0F, 26.0F);
      this.key(nVGRenderer, theme, "d", "7", isDown(client.rightKey), tickDelta + 58.0F, f, 26.0F, 26.0F);
      f += 29.0F;
      float f4 = (tickDelta3 - 3.0F) / 2.0F;
      this.key(nVGRenderer, theme, "lmb", "LMB " + CpsTracker.get(0), isDown(client.attackKey), tickDelta, f, f4, 26.0F);
      this.key(nVGRenderer, theme, "rmb", "RMB " + CpsTracker.get(1), isDown(client.useKey), tickDelta + f4 + 3.0F, f, f4, 26.0F);
      f += 29.0F;
      f4 = this.pressT("space", isDown(client.jumpKey));
      float f5 = 15.6F;
      int n = Colors.lerp(Colors.withAlpha(-15462118, 0.78F), Colors.withAlpha(theme.accent(), 0.85F), f4);
      nVGRenderer.rect(tickDelta, f, tickDelta3, f5, 6.0F, n);
      nVGRenderer.rect(tickDelta + tickDelta3 * 0.25F, f + f5 / 2.0F - 1.25F, tickDelta3 * 0.5F, 2.5F, 1.25F, Colors.lerp(theme.textMuted(), -1, f4));
   }

   private static boolean isDown(KeyBinding keyBinding) {
      return keyBinding.isPressed();
   }
}
