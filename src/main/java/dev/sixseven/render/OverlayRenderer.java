package dev.sixseven.render;

import dev.sixseven.SixSevenClient;
import dev.sixseven.hud.HudDragController;
import dev.sixseven.hud.HudManager;
import dev.sixseven.module.misc.CustomCrosshairModule;
import dev.sixseven.module.visuals.MotionBlurModule;
import dev.sixseven.notification.NotificationManager;
import dev.sixseven.render.nanovg.GlStateSnapshot;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.theme.Theme;
import dev.sixseven.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.opengl.GL33C;

public final class OverlayRenderer {
   private static HudManager hudManager;
   private static NotificationManager notifications;
   private static int fbo = -1;
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
         GlintTextureTinter.tick();
      } catch (Throwable ex) {
      }

      if (!crashed && hudManager != null) {
         MinecraftClient client = MinecraftClient.getInstance();
         Framebuffer framebuffer = client.getFramebuffer();
         if (framebuffer != null) {
            int glTexId = framebuffer.getColorAttachment();
            boolean found = client.currentScreen instanceof NvgDrawable;
            boolean found2 = client.currentScreen instanceof ChatScreen;
            boolean found3 = !client.options.hudHidden && client.world != null && !found;
            boolean enabled = client.world != null && !client.options.hudHidden;
            MotionBlurModule motionBlurModule = SixSevenClient.modules() != null ? SixSevenClient.modules().motionBlur : null;
            boolean enabled2 = motionBlurModule != null && motionBlurModule.isEnabled() && client.world != null && !found;
            if (!found && !found3 && !enabled && !enabled2) {
               return;
            }

            try {
               GlStateSnapshot glStateSnapshot = GlStateSnapshot.capture();

               try {
                  if (!bindOverlayFbo(glTexture, framebuffer.textureWidth, framebuffer.textureHeight)) {
                     return;
                  }

                  NVGRenderer nVGRenderer = NVGRenderer.get();
                  if (enabled2) {
                     MotionBlurRenderer.render(nVGRenderer, framebuffer.textureWidth, framebuffer.textureHeight, motionBlurModule);
                  }

                  nVGRenderer.setFontMode(SixSevenClient.modules().clickGui.font.get());
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

                        CustomCrosshairModule customCrosshairModule = SixSevenClient.modules().customCrosshair;
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
               SixSevenClient.LOGGER.error("Epstein Client overlay renderer crashed; disabling overlay", ex2);
               return;
            }
         }
      }
   }

   private static void renderChatEditOverlay(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2) {
      Theme theme = SixSevenClient.themes().current();
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

   private static boolean bindOverlayFbo(AbstractTexture glTexture, int bind, int bind2) {
      if (fbo == -1) {
         fbo = GL33C.glGenFramebuffers();
      }

      GL33C.glBindFramebuffer(36160, fbo);
      GL33C.glFramebufferTexture2D(36160, 36064, 3553, glTexId, 0);
      if (GL33C.glCheckFramebufferStatus(36160) != 36053) {
         return false;
      } else {
         GL33C.glViewport(0, 0, bind, bind2);
         GL33C.glDisable(3089);
         return true;
      }
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
