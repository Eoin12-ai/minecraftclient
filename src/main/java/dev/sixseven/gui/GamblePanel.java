package dev.sixseven.gui;

import dev.sixseven.mixin.AbstractContainerScreenAccessor;
import dev.sixseven.module.misc.GambleRiggerModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class GamblePanel {
   private static final int PAD = 8;
   private static final int CELL = 22;
   private static final int GAP = 4;
   private static final int TITLE_H = 12;
   private static final int BTN_H = 16;
   private static final int RESET_H = 12;
   private static final int PANEL_FILL = -3750202;
   private static final int PANEL_HOVER = -2697514;
   private static final int BTN_DISABLED = -5263441;
   private static final int HL_LIGHT = -1;
   private static final int HL_DARK = -11184811;
   private static final int SLOT_FILL = -7631989;
   private static final int SLOT_DARK = -13158601;
   private static final int OUTLINE = -16777216;
   private static final int LABEL = -12566464;
   private static final int TEXT_ON_BTN = -1;
   private static final int TEXT_HOVER = -96;
   private static final int TEXT_MUTED = -6250336;
   private final HandledScreen<?> screen;
   private final AbstractContainerScreenAccessor access;
   private final GambleRiggerModule mod;

   public GamblePanel(HandledScreen<?> handledScreen, GambleRiggerModule gambleRiggerModule) {
      this.screen = handledScreen;
      this.access = (AbstractContainerScreenAccessor)handledScreen;
      this.mod = gambleRiggerModule;
   }

   private int gridW() {
      return 74;
   }

   private int panelW() {
      return this.gridW() + 16;
   }

   private int panelX() {
      int temp = this.access.getLeftPos() + this.access.getImageWidth() + 6;
      if (temp + this.panelW() > this.screen.width) {
         int offset = this.access.getLeftPos() - this.panelW() - 6;
         if (offset >= 2) {
            return offset;
         }
      }

      return temp;
   }

   private int panelY() {
      return this.access.getTopPos();
   }

   private int gridTop() {
      return this.panelY() + 8 + 12 + 4;
   }

   private int gridH() {
      return 74;
   }

   private int restoreTop() {
      return this.gridTop() + this.gridH() + 6;
   }

   private int resetTop() {
      return this.restoreTop() + 16 + 3;
   }

   private int statusTop() {
      return this.resetTop() + 12 + 5;
   }

   private int panelH() {
      return this.statusTop() + 16 + 7 - this.panelY();
   }

   private int cellX(int n) {
      return this.panelX() + 8 + n % 3 * 26;
   }

   private int cellY(int n) {
      return this.gridTop() + n / 3 * 26;
   }

   private int controlX() {
      return this.panelX() + 8;
   }

   private static boolean in(double d, double coord, int n, int localZ, int localY, int step) {
      return d >= (double)n && d < (double)(n + localY) && coord >= (double)localZ && coord < (double)(localZ + step);
   }

   public void render(DrawContext context, int n, int step2) {
      if (this.mod.isEnabled()) {
         TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
         int n9 = this.panelX();
         int n10 = this.panelY();
         int n11 = this.panelW();
         int n12 = this.panelH();
         context.fill(n9 - 1, n10 - 1, n9 + n11 + 1, n10 + n12 + 1, -16777216);
         raised(context, n9, n10, n11, n12, -3750202);
         this.centered(context, textRenderer, Text.literal("Gamble Rigger"), n9 + n11 / 2, n10 + 8, -12566464, false);
         boolean ok = this.mod.canExtract();

         for (int n13 = 0; n13 < 9; n13++) {
            this.drawCell(context, textRenderer, n13, n, step2, ok);
         }

         this.drawButton(context, textRenderer, this.controlX(), this.restoreTop(), this.gridW(), 16, "Restore", this.mod.canRestore(), n, step2);
         boolean ok2 = this.mod.phase() != GambleRiggerModule.Phase.IDLE || this.mod.keepSlot() >= 0;
         this.drawButton(context, textRenderer, this.controlX(), this.resetTop(), this.gridW(), 12, "Reset", ok2, n, step2);

         this.centered(context, textRenderer, Text.literal(switch (this.mod.phase()) {
            case EXTRACTING -> "Taking... " + this.mod.queued();
            case EXTRACTED -> "Kept #" + (this.mod.keepSlot() + 1);
            case RESTORING -> "Restoring... " + this.mod.queued();
            default -> "Pick a slot";
         }), n9 + n11 / 2, this.statusTop(), -12566464, false);
      }
   }

   private void drawCell(DrawContext context, TextRenderer textRenderer, int n, int n9, int n10, boolean value) {
      int n11 = this.cellX(n);
      int n12 = this.cellY(n);
      boolean ok = this.mod.keepSlot() == n && this.mod.phase() != GambleRiggerModule.Phase.IDLE;
      boolean ok2 = value && in((double)n9, (double)n10, n11, n12, 22, 22);
      MutableText text = Text.literal(Integer.toString(n + 1));
      int n13 = n11 + 11;
      int n14 = n12 + 7;
      if (ok) {
         sunken(context, n11, n12, 22, 22, -7631989);
         this.centered(context, textRenderer, text, n13, n14, -1, true);
      } else {
         raised(context, n11, n12, 22, 22, ok2 ? -2697514 : -3750202);
         int n15 = !value ? -6250336 : (ok2 ? -96 : -1);
         this.centered(context, textRenderer, text, n13, n14, n15, true);
      }
   }

   private void drawButton(DrawContext context, TextRenderer textRenderer, int n, int n9, int n10, int n11, String str, boolean value, int n12, int n13) {
      boolean ok = value && in((double)n12, (double)n13, n, n9, n10, n11);
      int n14 = !value ? -5263441 : (ok ? -2697514 : -3750202);
      raised(context, n, n9, n10, n11, n14);
      int n15 = !value ? -6250336 : (ok ? -96 : -1);
      this.centered(context, textRenderer, Text.literal(str), n + n10 / 2, n9 + (n11 - 8) / 2, n15, true);
   }

   private static void raised(DrawContext context, int n, int localY, int step, int step2, int n9) {
      context.fill(n, localY, n + step, localY + step2, n9);
      context.fill(n, localY, n + step, localY + 1, -1);
      context.fill(n, localY, n + 1, localY + step2, -1);
      context.fill(n, localY + step2 - 1, n + step, localY + step2, -11184811);
      context.fill(n + step - 1, localY, n + step, localY + step2, -11184811);
   }

   private static void sunken(DrawContext context, int n, int localY, int step, int step2, int n9) {
      context.fill(n, localY, n + step, localY + step2, n9);
      context.fill(n, localY, n + step, localY + 1, -13158601);
      context.fill(n, localY, n + 1, localY + step2, -13158601);
      context.fill(n, localY + step2 - 1, n + step, localY + step2, -1);
      context.fill(n + step - 1, localY, n + step, localY + step2, -1);
   }

   private void centered(DrawContext context, TextRenderer textRenderer, Text text, int n, int localX, int localZ, boolean value) {
      context.drawText(textRenderer, text, n - textRenderer.getWidth(text) / 2, localX, localZ, value);
   }

   public boolean handleClick(double d, double coord, int n) {
      if (!this.mod.isEnabled()) {
         return false;
      } else {
         int localZ = this.panelX();
         int localY = this.panelY();
         if (!in(d, coord, localZ, localY, this.panelW(), this.panelH())) {
            return false;
         } else {
            if (n == 0) {
               if (this.mod.canExtract()) {
                  for (int step = 0; step < 9; step++) {
                     if (in(d, coord, this.cellX(step), this.cellY(step), 22, 22)) {
                        this.mod.requestKeep(step);
                        return true;
                     }
                  }
               }

               if (this.mod.canRestore() && in(d, coord, this.controlX(), this.restoreTop(), this.gridW(), 16)) {
                  this.mod.requestRestore();
                  return true;
               }

               if (in(d, coord, this.controlX(), this.resetTop(), this.gridW(), 12)) {
                  this.mod.reset();
                  return true;
               }
            }

            return true;
         }
      }
   }
}
