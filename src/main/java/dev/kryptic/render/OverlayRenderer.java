package dev.kryptic.render;

import dev.kryptic.KrypticClient;
import dev.kryptic.hud.HudDragController;
import dev.kryptic.hud.HudManager;
import dev.kryptic.module.misc.CustomCrosshairModule;
import dev.kryptic.module.visuals.MotionBlurModule;
import dev.kryptic.notification.NotificationManager;
import dev.kryptic.render.nanovg.GlStateSnapshot;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.theme.Theme;
import dev.kryptic.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.opengl.GL33C;

public final class OverlayRenderer {
   private static HudManager hudManager;
   private static NotificationManager notifications;
   private static boolean crashed;

   private OverlayRenderer() {
   }

   public static void init(HudManager hudManager2, NotificationManager notificationManager) {
      hudManager = hudManager2;
      notifications = notificationManager;
   }

   public static float uiScale() {
      MinecraftClient client = MinecraftClient.getInstance();
      return Math.max(1.0F, (float)client.getWindow().getFramebufferHeight() / 1080.0F);
   }

   public static void render() {
      try {
         GlintTextureTinter.onFrame();
      } catch (Throwable ex) {
      }

      if (!crashed && hudManager != null) {
         MinecraftClient client = MinecraftClient.getInstance();
         Framebuffer framebuffer = client.getFramebuffer();
         if (framebuffer != null) {
            boolean found = client.currentScreen instanceof NvgDrawable;
            boolean found2 = client.currentScreen instanceof ChatScreen;
            boolean found3 = !client.options.hudHidden && client.world != null && !found;
            boolean enabled = client.world != null && !client.options.hudHidden;
            MotionBlurModule motionBlurModule = KrypticClient.modules() != null ? KrypticClient.modules().motionBlur : null;
            boolean enabled2 = motionBlurModule != null && motionBlurModule.isEnabled() && client.world != null && !found;
            if (!found && !found3 && !enabled && !enabled2) {
               return;
            }

            try {
               GlStateSnapshot glStateSnapshot = GlStateSnapshot.capture();

               try {
                  prepareOverlayTarget(framebuffer.textureWidth, framebuffer.textureHeight);

                  NVGRenderer nVGRenderer = NVGRenderer.get();
                  if (enabled2) {
                     MotionBlurRenderer.render(nVGRenderer, framebuffer.textureWidth, framebuffer.textureHeight, motionBlurModule);
                  }

                  nVGRenderer.setFontMode(KrypticClient.modules().clickGui.font.get());
                  if (nVGRenderer.hasFont()) {
                     float f = uiScale();
                     float f4 = (float)framebuffer.textureWidth / f;
                     float f5 = (float)framebuffer.textureHeight / f;
                     nVGRenderer.beginFrame((float)framebuffer.textureWidth, (float)framebuffer.textureHeight, 1.0F);
                     nVGRenderer.save();
                     nVGRenderer.scale(f);
                     if (found3) {
                        if (found2 && HudDragController.isDragging()) {
                           HudDragController.updateDrag(nVGRenderer);
                        }

                        CustomCrosshairModule customCrosshairModule = KrypticClient.modules().customCrosshair;
                        if (customCrosshairModule.isEnabled() && client.currentScreen == null) {
                           customCrosshairModule.render(nVGRenderer, f4 / 2.0F, f5 / 2.0F);
                        }

                        if (client.currentScreen == null) {
                           WorldNametagRenderer.render(nVGRenderer);
                        }

                        hudManager.render(nVGRenderer, f4, f5);
                        if (found2) {
                           renderChatEditOverlay(nVGRenderer, f4, f5);
                        }
                     }

                     if (enabled) {
                        notifications.renderToasts(nVGRenderer, f4, f5);
                     }

                     if (found) {
                        ((NvgDrawable)client.currentScreen).renderNvg(nVGRenderer, uiMouseX(), uiMouseY(), f4, f5);
                     }

                     nVGRenderer.restore();
                     nVGRenderer.endFrame();
                     return;
                  }
               } finally {
                  glStateSnapshot.restore();
               }

               return;
            } catch (Throwable ex2) {
               crashed = true;
               KrypticClient.LOGGER.error("Kryptic Client overlay renderer crashed; disabling overlay", ex2);
               return;
            }
         }
      }
   }

   private static void renderChatEditOverlay(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2) {
      Theme theme = KrypticClient.themes().current();
      float f = uiMouseX();
      float f4 = uiMouseY();
      String text2 = "Drag to move  ·  scroll to resize";
      nVGRenderer.text(text2, (tickDelta - nVGRenderer.textWidth(text2, 14.0F)) / 2.0F, 22.0F, 14.0F, Colors.withAlpha(theme.textMuted(), 0.9F));

      for (HudManager.Placement placement : hudManager.layout(nVGRenderer, tickDelta, tickDelta2, true)) {
         boolean found = !placement.component().visible();
         boolean found2 = placement.contains(f, f4) || placement.component() == HudDragController.draggedComponent();
         if (found) {
            nVGRenderer.save();
            nVGRenderer.alpha(0.35F);
            hudManager.renderPlacement(nVGRenderer, placement);
            nVGRenderer.restore();
         }

         int n = found2 ? theme.accentBright() : Colors.withAlpha(theme.accent(), 0.5F);
         nVGRenderer.rectOutline(placement.x() - 3.0F, placement.y() - 3.0F, placement.w() + 6.0F, placement.h() + 6.0F, 6.0F, found2 ? 1.6F : 1.0F, n);
         nVGRenderer.rect(placement.x() + placement.w() - 2.0F, placement.y() + placement.h() - 2.0F, 6.0F, 6.0F, 2.0F, n);
         if (found2) {
            float f5 = placement.component().getScale();
            String text3 = Math.round(f5 * 100.0F) + "%";
            nVGRenderer.text(text3, placement.x() + placement.w() + 6.0F, placement.y() + placement.h() / 2.0F, 11.0F, theme.accentBright());
         }
      }
   }

   /**
    * Points the overlay at the framebuffer Minecraft already has bound.
    *
    * This used to build its own framebuffer object and attach the game's colour
    * texture to it. That cannot work on this version: Minecraft's framebuffer
    * hands back a GpuTexture, an abstraction over the graphics backend that
    * exposes no OpenGL handle, and there is no supported way to get one. What
    * the code actually did was attach texture 0 — which detaches the colour
    * attachment — so the completeness check that followed could never pass and
    * the whole overlay returned early every single frame. The HUD, the
    * ClickGUI, the watermark and the notifications have not drawn on any
    * platform since.
    *
    * Drawing into the bound framebuffer needs none of that. It is the one the
    * game is about to present, NanoVG never changes the binding, and the state
    * snapshot puts the viewport and scissor back afterwards.
    */
   private static void prepareOverlayTarget(int width, int height) {
      GL33C.glViewport(0, 0, width, height);
      GL33C.glDisable(GL33C.GL_SCISSOR_TEST);
   }

   public static float uiMouseX() {
      MinecraftClient client = MinecraftClient.getInstance();
      return (float)(client.mouse.getX() * (double)client.getWindow().getFramebufferWidth() / (double)Math.max(1, client.getWindow().getWidth()))
         / uiScale();
   }

   public static float uiMouseY() {
      MinecraftClient client = MinecraftClient.getInstance();
      return (float)(client.mouse.getY() * (double)client.getWindow().getFramebufferHeight() / (double)Math.max(1, client.getWindow().getHeight()))
         / uiScale();
   }

   public static float guiToUi(double d) {
      MinecraftClient client = MinecraftClient.getInstance();
      float f = (float)(d * (double)client.getWindow().getScaleFactor());
      return f / uiScale();
   }

   public static float uiWidth() {
      MinecraftClient client = MinecraftClient.getInstance();
      return (float)client.getWindow().getFramebufferWidth() / uiScale();
   }

   public static float uiHeight() {
      MinecraftClient client = MinecraftClient.getInstance();
      return (float)client.getWindow().getFramebufferHeight() / uiScale();
   }
}
