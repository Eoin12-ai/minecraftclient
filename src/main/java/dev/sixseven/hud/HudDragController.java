package dev.sixseven.hud;

import dev.sixseven.render.OverlayRenderer;
import dev.sixseven.render.nanovg.NVGRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;

public final class HudDragController {
   private static HudComponent dragging;
   private static float grabDx;
   private static float grabDy;

   private HudDragController() {
   }

   public static boolean isEditing() {
      return MinecraftClient.getInstance().currentScreen instanceof ChatScreen;
   }

   public static boolean isDragging() {
      return dragging != null;
   }

   public static boolean tryStartDrag(HudManager hudManager) {
      NVGRenderer nVGRenderer = NVGRenderer.get();
      float f = OverlayRenderer.uiMouseX();
      float f4 = OverlayRenderer.uiMouseY();

      for (HudManager.Placement placement : hudManager.layout(nVGRenderer, OverlayRenderer.uiWidth(), OverlayRenderer.uiHeight(), true)) {
         if (placement.contains(f, f4)) {
            float f5 = placement.component().getScale();
            if (placement.component().onEditClick((f - placement.x()) / f5, (f4 - placement.y()) / f5)) {
               return true;
            }

            dragging = placement.component();
            grabDx = f - placement.x();
            grabDy = f4 - placement.y();
            return true;
         }
      }

      return false;
   }

   public static boolean tryResize(HudManager hudManager, double d) {
      NVGRenderer nVGRenderer = NVGRenderer.get();
      float f = OverlayRenderer.uiMouseX();
      float f3 = OverlayRenderer.uiMouseY();

      for (HudManager.Placement placement : hudManager.layout(nVGRenderer, OverlayRenderer.uiWidth(), OverlayRenderer.uiHeight(), true)) {
         if (placement.contains(f, f3)) {
            HudComponent hudComponent = placement.component();
            hudComponent.setScale(hudComponent.getScale() + (float)d * 0.06F);
            return true;
         }
      }

      return false;
   }

   public static void updateDrag(NVGRenderer nVGRenderer) {
      if (dragging != null) {
         float f = OverlayRenderer.uiWidth();
         float f8 = OverlayRenderer.uiHeight();
         float f9 = dragging.getScale();
         float f10 = dragging.measureWidth(nVGRenderer) * f9;
         float f11 = dragging.measureHeight(nVGRenderer) * f9;
         float f12 = Math.max(1.0F, f - f10);
         float f13 = Math.max(1.0F, f8 - f11);
         dragging.setPosition((OverlayRenderer.uiMouseX() - grabDx) / f12, (OverlayRenderer.uiMouseY() - grabDy) / f13);
      }
   }

   public static void stopDrag() {
      dragging = null;
   }

   public static HudComponent draggedComponent() {
      return dragging;
   }
}
