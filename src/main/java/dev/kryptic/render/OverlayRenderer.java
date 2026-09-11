package dev.kryptic.render;

import dev.kryptic.KrypticClient;
import dev.kryptic.hud.HudDragController;
import dev.kryptic.hud.HudManager;
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
   /**
    * The last failure from the overlay frame, and how many there have been.
    *
    * This used to be a boolean latch: one throw set it and the overlay never
    * drew again for the rest of the session. That is why the menu appeared for
    * a split second and then vanished -- the first frame drew, the second
    * threw, and everything after was skipped in silence.
    *
    * A failure is now recorded and retried. Most causes are transient (a
    * resize, a resource pack reload, a frame where a texture is not ready) and
    * cost one dropped frame instead of the whole session. The message is kept
    * so the ClickGUI's fallback can show it, because a log file the user has to
    * find is a diagnosis nobody makes.
    */
   private static volatile String lastError;
   private static int errorCount;
   private static long lastErrorLogged;
   private static boolean warnedNoFont;

   /**
    * How many overlay frames have actually completed, and how many times a
    * screen has been asked to draw itself.
    *
    * These exist to tell apart the failures that look identical from the
    * outside. An overlay that never runs, one that runs and throws, and one
    * that runs cleanly and still puts no pixels on the screen all present as
    * the same blank menu, and they have nothing in common as causes. A count
    * separates them in one glance.
    */
   private static volatile int overlayFrames;
   private static volatile int screenDraws;
   private static volatile boolean fontLoaded;

   /** Said once: every frame would repeat it sixty times a second. */
   private static void warnNoFontOnce() {
      if (warnedNoFont) return;
      warnedNoFont = true;
      KrypticClient.LOGGER.error(
            "No NanoVG font loaded - the menu and HUD will draw without any text. "
          + "Check that assets/krypticclient/fonts/ survived in the jar.");
   }

   /**
    * A screen that threw while drawing.
    *
    * This used to log once and swallow the rest, and never touched lastError.
    * That is the worst of both: the screen's own fallback asks lastError why it
    * is being shown, got null, and told the user "NanoVG drew nothing and
    * reported no error" -- while the real exception was being thrown sixty
    * times a second and discarded. The one message that would have identified
    * the fault was the one the code went out of its way to hide.
    */
   private static void recordScreenFailure(Object screen, Throwable error) {
      String name = screen == null ? "?" : screen.getClass().getSimpleName();
      recordFailure("screen " + name, error);
   }

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
      if (hudManager != null) {
         MinecraftClient client = MinecraftClient.getInstance();
         Framebuffer framebuffer = client.getFramebuffer();
         if (framebuffer != null) {
            boolean found = client.currentScreen instanceof NvgDrawable;
            boolean found2 = client.currentScreen instanceof ChatScreen;
            boolean found3 = !client.options.hudHidden && client.world != null && !found;
            boolean enabled = client.world != null && !client.options.hudHidden;
            if (!found && !found3 && !enabled) {
               return;
            }

            try {
               GlStateSnapshot glStateSnapshot = GlStateSnapshot.capture();

               try {
                  prepareOverlayTarget(framebuffer.textureWidth, framebuffer.textureHeight);

                  NVGRenderer nVGRenderer = NVGRenderer.get();
                  nVGRenderer.setFontMode(KrypticClient.modules().clickGui.font.get());
                  // Draw whether or not a face loaded. This used to be wrapped
                  // in `if (hasFont())`, so a font that failed to load silently
                  // skipped the entire frame -- no menu, no HUD, and not one
                  // line in the log to say why. NanoVG draws shapes without a
                  // font; only text needs one, and losing the labels is a far
                  // better failure than losing everything.
                  fontLoaded = nVGRenderer.hasFont();
                  if (!fontLoaded) warnNoFontOnce();
                  {
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
                        // Guarded on its own: a screen that throws should lose
                        // itself, not latch `crashed` and take the HUD down for
                        // the rest of the session.
                        screenDraws++;
                        try {
                           ((NvgDrawable)client.currentScreen).renderNvg(nVGRenderer, uiMouseX(), uiMouseY(), f4, f5);
                        } catch (Throwable screenError) {
                           recordScreenFailure(client.currentScreen, screenError);
                        }
                     }

                     nVGRenderer.restore();
                     nVGRenderer.endFrame();
                     overlayFrames++;
                     return;
                  }
               } finally {
                  glStateSnapshot.restore();
               }
            } catch (Throwable ex2) {
               recordFailure(ex2);
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

   /**
    * The inverse of {@link #guiToUi}: this client's UI space back to vanilla's.
    *
    * Needed by anything that has laid itself out in UI coordinates but has to
    * draw through a vanilla DrawContext -- the ClickGUI's fallback pass, which
    * runs when NanoVG has produced nothing.
    */
   public static float uiToGui(double d) {
      MinecraftClient client = MinecraftClient.getInstance();
      float f = (float)(d * (double)uiScale());
      return f / (float)client.getWindow().getScaleFactor();
   }

   /**
    * The last overlay failure, formatted for display, or null if it has never
    * failed. Shown by the ClickGUI fallback.
    */
   public static String lastError() {
      return lastError;
   }

   /**
    * Records a failed frame and logs it, throttled.
    *
    * Throttled rather than once-only: an error that starts happening later is
    * worth a line, but sixty a second is worth none.
    */
   private static void recordFailure(Throwable error) {
      recordFailure("overlay", error);
   }

   private static void recordFailure(String where, Throwable error) {
      errorCount++;
      String name = error.getClass().getSimpleName();
      String message = error.getMessage();

      // The class name alone is rarely enough -- a NullPointerException says
      // nothing without the frame it came from -- so the top line of our own
      // code goes in too. That is the line somebody has to open.
      String at = "";
      for (StackTraceElement frame : error.getStackTrace()) {
         if (frame.getClassName().startsWith("dev.kryptic.")) {
            at = " at " + frame.getFileName() + ":" + frame.getLineNumber();
            break;
         }
      }

      lastError = errorCount + "x " + where + " " + name
            + (message == null ? "" : ": " + message) + at;

      long now = System.currentTimeMillis();
      if (now - lastErrorLogged > 5000L) {
         lastErrorLogged = now;
         KrypticClient.LOGGER.error(
               "Kryptic {} frame failed ({} so far); retrying next frame", where, errorCount, error);
      }
   }

   /**
    * One line saying what the overlay is actually doing, for the fallback to
    * show.
    *
    * Ordered by how early the failure is: a hook that never fires, a frame that
    * throws, a missing font, and finally the case where everything reports
    * success and nothing appears -- which is the only one that points at GL
    * state rather than at our own code.
    */
   public static String status() {
      if (overlayFrames == 0 && errorCount == 0) {
         return "the overlay render hook has never fired";
      }
      if (lastError != null) {
         return lastError;
      }
      if (screenDraws == 0) {
         return "overlay ran " + overlayFrames + "x but this screen was never asked to draw";
      }
      if (!fontLoaded) {
         return "overlay ran " + overlayFrames + "x, no font loaded - check assets/krypticclient/fonts/";
      }
      return "overlay ran " + overlayFrames + "x and drew " + screenDraws
           + "x with no error - NanoVG is producing no pixels (GL state)";
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
