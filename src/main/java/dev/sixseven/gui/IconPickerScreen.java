package dev.sixseven.gui;

import dev.sixseven.SixSevenClient;
import dev.sixseven.gui.picker.PickerGrid;
import dev.sixseven.gui.widget.ColorWidget;
import dev.sixseven.render.NvgDrawable;
import dev.sixseven.render.OverlayRenderer;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.settings.ColorSetting;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import dev.sixseven.util.UiSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class IconPickerScreen extends Screen implements NvgDrawable {
   private static final float HEADER_H = 42.0F;
   private static final float SEARCH_H = 34.0F;
   private static final float PAD = 12.0F;
   private static final float CELL = 34.0F;
   private static final float SEL_CHIP_W = 116.0F;
   private static final float PANEL_RADIUS = 14.0F;
   private final Screen parent;
   private final PickerGrid model;
   private final ThemeManager themes;
   private final StringBuilder search = new StringBuilder();
   private boolean searchFocused;
   private boolean selectedOnly;
   private List<PickerGrid.Cell> filtered = List.of();
   private String lastQuery = null;
   private boolean lastSelectedOnly;
   private boolean filterDirty = true;
   private float scroll;
   private float maxScroll;
   private ColorSetting colorSetting;
   private String colorTitle;
   private ColorWidget colorWidget;
   private final float[] popupRect = new float[4];
   private final float[] closeRect = new float[4];
   private final float[] searchRect = new float[4];
   private final float[] selChipRect = new float[4];

   public IconPickerScreen(Screen screen, PickerGrid pickerGrid, ThemeManager themeManager) {
      super(Text.literal(pickerGrid.title()));
      this.parent = screen;
      this.model = pickerGrid;
      this.themes = themeManager;
   }

   private void refreshFilter() {
      String text2 = this.search.toString().trim().toLowerCase(Locale.ROOT);
      if (this.filterDirty || !text2.equals(this.lastQuery) || this.selectedOnly != this.lastSelectedOnly) {
         this.lastQuery = text2;
         this.lastSelectedOnly = this.selectedOnly;
         this.filterDirty = false;
         ArrayList list = new ArrayList();

         for (PickerGrid.Cell cell : this.model.cells()) {
            if ((!this.selectedOnly || cell.selected()) && (text2.isEmpty() || cell.matches(text2))) {
               list.add(cell);
            }
         }

         this.filtered = list;
         this.scroll = 0.0F;
      }
   }

   private IconPickerScreen.Layout layout() {
      float f = OverlayRenderer.uiWidth();
      float f12 = OverlayRenderer.uiHeight();
      float f13 = Math.min(f * 0.82F, 940.0F);
      float f14 = Math.min(f12 * 0.84F, 660.0F);
      float f15 = (f - f13) / 2.0F;
      float f16 = (f12 - f14) / 2.0F;
      float f17 = f15 + 12.0F;
      float f18 = f16 + 42.0F + 34.0F;
      float f19 = f13 - 24.0F;
      float f20 = f14 - 42.0F - 34.0F - 12.0F;
      int n = Math.max(1, (int)(f19 / 34.0F));
      float f21 = f19 / (float)n;
      return new IconPickerScreen.Layout(f15, f16, f13, f14, f17, f18, f19, f20, n, f21, f21 - 11.0F);
   }

   private float contentHeight(IconPickerScreen.Layout layout2) {
      int n = (this.filtered.size() + layout2.cols() - 1) / layout2.cols();
      return (float)n * layout2.cell();
   }

   public void render(DrawContext context, int n, int n9, float tickDelta) {
      this.refreshFilter();
      IconPickerScreen.Layout layout2 = this.layout();
      this.maxScroll = Math.max(0.0F, this.contentHeight(layout2) - layout2.gridH());
      this.scroll = Math.clamp(this.scroll, 0.0F, this.maxScroll);
      float f = OverlayRenderer.uiScale();
      double d = (double)this.client.getWindow().getScaleFactor();
      float f6 = (float)((double)f / d);
      context.fill(0, 0, this.width, this.height, -670694391);
      fillRoundedRectV(context, layout2.px() * f6, layout2.py() * f6, layout2.panelW() * f6, layout2.panelH() * f6, 14.0F * f6, -132771297, -133560304);
      context.enableScissor(gi(layout2.gridX() * f6), gi(layout2.gridY() * f6), gi((layout2.gridX() + layout2.gridW()) * f6), gi((layout2.gridY() + layout2.gridH()) * f6));
      int n10 = Math.max(0, (int)(this.scroll / layout2.cell()) * layout2.cols());
      int n11 = layout2.cols() * ((int)(layout2.gridH() / layout2.cell()) + 3);
      int n12 = Math.min(this.filtered.size(), n10 + n11);
      MatrixStack matrix3x2fStack = context.getMatrices();

      for (int n13 = n10; n13 < n12; n13++) {
         PickerGrid.Cell cell = this.filtered.get(n13);
         int n14 = n13 % layout2.cols();
         int n15 = n13 / layout2.cols();
         float f7 = layout2.gridX() + (float)n14 * layout2.cell() + (layout2.cell() - layout2.icon()) / 2.0F;
         float f8 = layout2.gridY() - this.scroll + (float)n15 * layout2.cell() + (layout2.cell() - layout2.icon()) / 2.0F;
         float f9 = layout2.icon() * f6 / 16.0F;
         matrix3x2fStack.push();
         matrix3x2fStack.translate(f7 * f6, f8 * f6, 0.0f);
         matrix3x2fStack.scale(f9, f9, f9);
         context.drawItem(cell.icon(), 0, 0);
         matrix3x2fStack.pop();
      }

      context.disableScissor();
   }

   private static int gi(float f) {
      return Math.round(f);
   }

   private static void fillRoundedRectV(DrawContext context, float f, float f6, float f7, float f8, float f9, int n, int n14) {
      int n15 = Math.round(f);
      int n16 = Math.round(f6);
      int n17 = Math.round(f + f7);
      int n18 = Math.round(f6 + f8);
      int n19 = n18 - n16;
      int n20 = n17 - n15;
      if (n19 > 0 && n20 > 0) {
         int n21 = Math.min(Math.round(f9), Math.min(n20, n19) / 2);

         for (int n22 = 0; n22 < n19; n22++) {
            int n23 = 0;
            if (n21 > 0) {
               int n24 = n22 < n21 ? n22 : (n22 >= n19 - n21 ? n19 - 1 - n22 : -1);
               if (n24 >= 0) {
                  double d = (double)(n21 - n24) - 0.5;
                  n23 = (int)Math.round((double)n21 - Math.sqrt(Math.max(0.0, (double)n21 * (double)n21 - d * d)));
               }
            }

            int n25 = Colors.lerp(n, n14, n19 <= 1 ? 0.0F : (float)n22 / (float)(n19 - 1));
            context.fill(n15 + n23, n16 + n22, n17 - n23, n16 + n22 + 1, n25);
         }
      }
   }

   @Override
   public void renderNvg(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      if (nVGRenderer.hasFont()) {
         Theme theme = this.themes.current();
         IconPickerScreen.Layout layout2 = this.layout();
         boolean ok = System.nanoTime() / 400000000L % 2L == 0L;
         nVGRenderer.glow(layout2.px(), layout2.py(), layout2.panelW(), layout2.panelH(), 14.0F, 17.0F, Colors.withAlpha(-16777216, 0.45F));
         nVGRenderer.glow(layout2.px(), layout2.py(), layout2.panelW(), layout2.panelH(), 14.0F, 8.0F, Colors.withAlpha(theme.accent(), 0.28F));
         nVGRenderer.rectOutline(layout2.px(), layout2.py(), layout2.panelW(), layout2.panelH(), 14.0F, 1.4F, Colors.withAlpha(theme.accentBright(), 0.75F));
         nVGRenderer.rectOutline(
            layout2.px() + 2.2F, layout2.py() + 2.2F, layout2.panelW() - 4.4F, layout2.panelH() - 4.4F, 11.8F, 1.0F, Colors.withAlpha(theme.accentBright(), 0.13F)
         );
         nVGRenderer.textGradient(this.model.title(), layout2.px() + 12.0F, layout2.py() + 16.0F, 16.0F, theme.accentBright(), theme.accent());
         String text2 = "Left-click: toggle   ·   Right-click: color   ·   " + this.model.activeCount() + " active";
         nVGRenderer.text(text2, layout2.px() + 12.0F, layout2.py() + 31.0F, 11.0F, theme.textDisabled());
         float f = layout2.px() + layout2.panelW() - 12.0F - 3.0F;
         float f18 = layout2.py() + 18.0F;
         boolean ok2 = Math.abs(tickDelta - f) < 10.0F && Math.abs(tickDelta2 - f18) < 10.0F;
         nVGRenderer.cross(f - 6.0F, f18 - 6.0F, 12.0F, 1.8F, ok2 ? theme.accentBright() : theme.textMuted());
         this.closeRect[0] = f - 10.0F;
         this.closeRect[1] = f18 - 10.0F;
         this.closeRect[2] = f + 10.0F;
         this.closeRect[3] = f18 + 10.0F;
         float f19 = layout2.px() + 12.0F;
         float f20 = layout2.py() + 42.0F + 4.0F;
         float f21 = 22.0F;
         float f22 = layout2.panelW() - 24.0F - 116.0F - 8.0F;
         this.searchRect[0] = f19;
         this.searchRect[1] = f20;
         this.searchRect[2] = f19 + f22;
         this.searchRect[3] = f20 + f21;
         nVGRenderer.rect(f19, f20, f22, f21, f21 / 2.0F, this.searchFocused ? Colors.withAlpha(theme.accent(), 0.16F) : Colors.withAlpha(-16777216, 0.4F));
         nVGRenderer.rectOutline(
            f19,
            f20,
            f22,
            f21,
            f21 / 2.0F,
            1.1F,
            Colors.withAlpha(this.searchFocused ? theme.accentBright() : theme.accent(), this.searchFocused ? 0.9F : 0.35F)
         );
         float f23 = f19 + 12.0F;
         float f24 = f20 + f21 / 2.0F;
         nVGRenderer.circleOutline(f23, f24 - 1.0F, 4.0F, 1.4F, theme.textMuted());
         nVGRenderer.line(f23 + 3.0F, f24 + 2.0F, f23 + 6.0F, f24 + 5.0F, 1.4F, theme.textMuted());
         if (this.search.length() == 0 && !this.searchFocused) {
            nVGRenderer.text("Search…  (" + this.filtered.size() + " shown)", f19 + 24.0F, f24, 12.5F, theme.textDisabled());
         } else {
            float f25 = nVGRenderer.text(this.search.toString(), f19 + 24.0F, f24, 12.5F, theme.textPrimary());
            if (this.searchFocused && ok) {
               nVGRenderer.rect(f19 + 24.0F + f25 + 1.5F, f24 - 6.0F, 1.4F, 12.0F, 0.7F, theme.accentBright());
            }
         }

         float f26 = f19 + f22 + 8.0F;
         this.selChipRect[0] = f26;
         this.selChipRect[1] = f20;
         this.selChipRect[2] = f26 + 116.0F;
         this.selChipRect[3] = f20 + f21;
         boolean ok3 = inRect(tickDelta, tickDelta2, this.selChipRect);
         nVGRenderer.rect(f26, f20, 116.0F, f21, f21 / 2.0F, this.selectedOnly ? Colors.withAlpha(theme.accent(), 0.22F) : Colors.withAlpha(-16777216, 0.4F));
         nVGRenderer.rectOutline(
            f26,
            f20,
            116.0F,
            f21,
            f21 / 2.0F,
            1.1F,
            Colors.withAlpha(!this.selectedOnly && !ok3 ? theme.accent() : theme.accentBright(), this.selectedOnly ? 0.9F : 0.4F)
         );
         float f27 = f26 + 13.0F;
         float f28 = f20 + f21 / 2.0F;
         if (this.selectedOnly) {
            nVGRenderer.circle(f27, f28, 4.0F, theme.statusEnabled());
            nVGRenderer.circleGlow(f27, f28, 4.0F, 4.0F, Colors.withAlpha(theme.statusEnabled(), 0.5F));
         } else {
            nVGRenderer.circleOutline(f27, f28, 4.0F, 1.4F, theme.textMuted());
         }

         nVGRenderer.text("Selected only", f26 + 24.0F, f28, 11.5F, this.selectedOnly ? theme.textPrimary() : theme.textMuted());
         nVGRenderer.save();
         nVGRenderer.scissor(layout2.gridX(), layout2.gridY(), layout2.gridW(), layout2.gridH());
         int n = Math.max(0, (int)(this.scroll / layout2.cell()) * layout2.cols());
         int n10 = layout2.cols() * ((int)(layout2.gridH() / layout2.cell()) + 3);
         int n11 = Math.min(this.filtered.size(), n + n10);
         int n12 = this.cellAt(tickDelta, tickDelta2, layout2);

         for (int n13 = n; n13 < n11; n13++) {
            PickerGrid.Cell cell = this.filtered.get(n13);
            int n14 = n13 % layout2.cols();
            int n15 = n13 / layout2.cols();
            float f29 = layout2.gridX() + (float)n14 * layout2.cell();
            float f30 = layout2.gridY() - this.scroll + (float)n15 * layout2.cell();
            boolean enabled2 = cell.tracked();
            boolean enabled3 = cell.enabled();
            if (n13 == n12) {
               nVGRenderer.rect(f29 + 1.0F, f30 + 1.0F, layout2.cell() - 2.0F, layout2.cell() - 2.0F, 6.0F, Colors.withAlpha(theme.accent(), 0.12F));
            }

            if (enabled2) {
               int n16 = cell.color();
               int n17 = enabled3 ? n16 : Colors.withAlpha(n16, 0.35F);
               nVGRenderer.rectOutline(f29 + 1.5F, f30 + 1.5F, layout2.cell() - 3.0F, layout2.cell() - 3.0F, 6.0F, enabled3 ? 1.8F : 1.0F, n17 | (enabled3 ? -16777216 : 0));
               nVGRenderer.rect(f29 + 4.0F, f30 + layout2.cell() - 5.0F, layout2.cell() - 8.0F, 2.5F, 1.0F, n16 | 0xFF000000);
               if (enabled3) {
                  nVGRenderer.circle(f29 + layout2.cell() - 6.0F, f30 + 6.0F, 2.6F, theme.statusEnabled());
               }
            }
         }

         nVGRenderer.restore();
         if (this.filtered.isEmpty() && this.selectedOnly) {
            nVGRenderer.text("Nothing selected yet — turn off \"Selected only\" to browse.", layout2.gridX() + 4.0F, layout2.gridY() + 16.0F, 12.5F, theme.textDisabled());
         }

         if (this.maxScroll > 0.0F) {
            float f31 = layout2.px() + layout2.panelW() - 6.0F;
            float f32 = Math.max(24.0F, layout2.gridH() * (layout2.gridH() / this.contentHeight(layout2)));
            float f33 = layout2.gridY() + (layout2.gridH() - f32) * (this.scroll / this.maxScroll);
            nVGRenderer.rect(f31, f33, 3.0F, f32, 1.5F, Colors.withAlpha(theme.accent(), 0.55F));
         }

         if (this.colorSetting != null && this.colorWidget != null) {
            this.renderColorPopup(nVGRenderer, theme);
         } else {
            this.popupRect[0] = this.popupRect[1] = this.popupRect[2] = this.popupRect[3] = 0.0F;
         }
      }
   }

   private void renderColorPopup(NVGRenderer nVGRenderer, Theme theme) {
      this.colorWidget.setExpanded(true);
      float f = 220.0F;
      float f10 = 26.0F;
      float f11 = OverlayRenderer.uiWidth();
      float f12 = OverlayRenderer.uiHeight();
      float f13 = (f11 - f) / 2.0F + 12.0F;
      float f14 = this.colorWidget.height(nVGRenderer);
      float f15 = f10 + f14 + 12.0F;
      float f16 = (f11 - f) / 2.0F;
      float f17 = (f12 - f15) / 2.0F;
      this.popupRect[0] = f16;
      this.popupRect[1] = f17;
      this.popupRect[2] = f16 + f;
      this.popupRect[3] = f17 + f15;
      nVGRenderer.glow(f16, f17, f, f15, 12.0F, 14.0F, Colors.withAlpha(theme.accent(), 0.3F));
      nVGRenderer.rectGradient(f16, f17, f, f15, 12.0F, theme.background(), theme.backgroundTo(), true);
      nVGRenderer.rectOutline(f16, f17, f, f15, 12.0F, 1.4F, Colors.withAlpha(theme.accentBright(), 0.7F));
      nVGRenderer.textGradient(this.colorTitle, f16 + 12.0F, f17 + 14.0F, 12.5F, theme.accentBright(), theme.accent());
      this.colorWidget.setBounds(f13, f17 + f10, f - 24.0F);
      this.colorWidget.render(nVGRenderer, OverlayRenderer.uiMouseX(), OverlayRenderer.uiMouseY());
   }

   private int cellAt(float f, float f3, IconPickerScreen.Layout layout2) {
      if (!(f < layout2.gridX()) && !(f > layout2.gridX() + layout2.gridW()) && !(f3 < layout2.gridY()) && !(f3 > layout2.gridY() + layout2.gridH())) {
         int n = (int)((f - layout2.gridX()) / layout2.cell());
         int localX = (int)((f3 - (layout2.gridY() - this.scroll)) / layout2.cell());
         if (n >= 0 && n < layout2.cols() && localX >= 0) {
            int localZ = localX * layout2.cols() + n;
            return localZ >= 0 && localZ < this.filtered.size() ? localZ : -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private float ux(double d) {
      return OverlayRenderer.guiToUi(d);
   }

   public boolean mouseClicked(double _cx, double _cy, int value) {
      float f = this.ux(_cx);
      float f3 = this.ux(_cy);
      IconPickerScreen.Layout layout2 = this.layout();
      if (this.colorSetting != null) {
         if (inRect(f, f3, this.popupRect)) {
            this.colorWidget.mouseClicked(f, f3, _cb);
         } else {
            this.closeColor();
            UiSounds.select();
         }

         return true;
      } else if (inRect(f, f3, this.closeRect)) {
         this.close();
         return true;
      } else if (inRect(f, f3, this.selChipRect)) {
         this.selectedOnly = !this.selectedOnly;
         this.filterDirty = true;
         this.searchFocused = false;
         UiSounds.toggle(this.selectedOnly);
         return true;
      } else if (inRect(f, f3, this.searchRect)) {
         this.searchFocused = true;
         UiSounds.select();
         return true;
      } else {
         this.searchFocused = false;
         if (!(f < layout2.px()) && !(f > layout2.px() + layout2.panelW()) && !(f3 < layout2.py()) && !(f3 > layout2.py() + layout2.panelH())) {
            int n = this.cellAt(f, f3, layout2);
            if (n >= 0) {
               PickerGrid.Cell cell = this.filtered.get(n);
               if (_cb == 1) {
                  this.openColor(cell.colorTarget(), cell.label());
               } else if (_cb == 0) {
                  boolean ok = cell.selected();
                  cell.toggle();
                  UiSounds.toggle(cell.enabled());
                  if (this.selectedOnly && ok && !cell.selected()) {
                     this.filterDirty = true;
                  }
               }

               return true;
            } else {
               return true;
            }
         } else {
            this.close();
            return true;
         }
      }
   }

   private void openColor(ColorSetting colorSetting2, String text2) {
      if (colorSetting2 != null) {
         this.colorSetting = colorSetting2;
         this.colorTitle = text2;
         this.colorWidget = new ColorWidget(this.themes, colorSetting2);
         this.colorWidget.setExpanded(true);
         this.searchFocused = false;
         UiSounds.select();
      }
   }

   private void closeColor() {
      this.colorSetting = null;
      this.colorTitle = null;
      this.colorWidget = null;
   }

   public boolean mouseDragged(double _cx, double _cy, int _cb, double d, double coord) {
      if (this.colorSetting != null && this.colorWidget != null) {
         this.colorWidget.mouseDragged(this.ux(_cx), this.ux(_cy));
      }

      return true;
   }

   public boolean mouseReleased(double _cx, double _cy, int _cb) {
      if (this.colorWidget != null) {
         this.colorWidget.mouseReleased();
      }

      return true;
   }

   public boolean mouseScrolled(double _sx, double _sy, double d, double coord, double currentScore, double coord3) {
      if (this.colorSetting == null) {
         this.scroll = Math.clamp(this.scroll - (float)(coord3 * (double)this.layout().cell()), 0.0F, this.maxScroll);
      }

      return true;
   }

   public boolean keyPressed(int _key, int _scan, int keyInput) {
      int n = _key;
      if (this.colorSetting != null) {
         if (this.colorWidget != null && this.colorWidget.isListening()) {
            this.colorWidget.keyPressed(_key);
            return true;
         } else if (n == 256) {
            this.closeColor();
            return true;
         } else {
            return true;
         }
      } else if (this.searchFocused) {
         switch (n) {
            case 256:
            case 257:
            case 335:
               this.searchFocused = false;
               break;
            case 259:
               if (this.search.length() > 0) {
                  this.search.deleteCharAt(this.search.length() - 1);
                  this.filterDirty = true;
               }
         }

         return true;
      } else if (n == 256) {
         this.close();
         return true;
      } else {
         return super.keyPressed(_key, _scan, _mods);
      }
   }

   public boolean charTyped(char _c, int charInput) {
      if (this.colorSetting != null) {
         return true;
      } else if (!this.searchFocused) {
         return true;
      } else if (this.search.length() >= 48) {
         return true;
      } else {
         char ch = (char)_c;
         if (ch == ' ' || ch == '_' || ch == ':' || ch == '/' || ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z'
            )
          {
            this.search.append(Character.toLowerCase(ch));
            this.filterDirty = true;
         }

         return true;
      }
   }

   public void close() {
      SixSevenClient.config().save();
      this.client.setScreen(this.parent);
   }

   public boolean shouldPause() {
      return false;
   }

   public void debugSetSearch(String text2) {
      this.search.setLength(0);
      this.search.append(text2);
      this.searchFocused = true;
      this.filterDirty = true;
      this.refreshFilter();
   }

   public void debugSetSelectedOnly(boolean value) {
      this.selectedOnly = value;
      this.filterDirty = true;
      this.refreshFilter();
   }

   public void debugOpenColor(int n) {
      if (n >= 0 && n < this.filtered.size()) {
         PickerGrid.Cell cell = this.filtered.get(n);
         this.openColor(cell.colorTarget(), cell.label());
      }
   }

   public int debugFilteredCount() {
      return this.filtered.size();
   }

   private static boolean inRect(float f, float f4, float[] f5) {
      return f >= f5[0] && f <= f5[2] && f4 >= f5[1] && f4 <= f5[3];
   }

   private static record Layout(
      float px, float py, float panelW, float panelH, float gridX, float gridY, float gridW, float gridH, int cols, float cell, float icon
   ) {
   }
}
