package dev.sixseven.hud.components;

import dev.sixseven.hud.HudComponent;
import dev.sixseven.module.misc.StaffListModule;
import dev.sixseven.render.anim.Animation;
import dev.sixseven.render.anim.Easing;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.staff.StaffEntry;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StaffListHud extends HudComponent {
   private static final float PAD = 8.0F;
   private static final float HEADER_H = 15.0F;
   private static final float GAP_HEADER = 5.0F;
   private static final float ROW_H = 18.0F;
   private static final float EMPTY_H = 16.0F;
   private static final float OVERFLOW_H = 13.0F;
   private static final float MARK_W = 15.0F;
   private static final float PING_W = 11.0F;
   private static final float FONT_TITLE = 9.0F;
   private static final float FONT_NAME = 12.5F;
   private static final float FONT_RANK = 9.0F;
   private static final float FONT_EMPTY = 11.0F;
   private static final float MIN_CONTENT_W = 92.0F;
   private static final float MAX_NAME_W = 128.0F;
   private static final int GREEN = -11870592;
   private static final int YELLOW = -340971;
   private static final int RED = -495247;
   private final StaffListModule module;
   private final ThemeManager themes;
   private final Map<String, StaffListHud.RowAnim> rows = new LinkedHashMap<>();

   public StaffListHud(StaffListModule staffListModule, ThemeManager themeManager) {
      super("staffList", 0.008F, 0.27F, staffListModule::isEnabled);
      this.module = staffListModule;
      this.themes = themeManager;
   }

   private int shownCount(List<StaffEntry> list) {
      return Math.min(list.size(), Math.max(1, this.module.maxRows.getInt()));
   }

   private List<StaffListHud.RowAnim> layoutRows(List<StaffEntry> list) {
      int n = this.shownCount(list);
      HashSet set = new HashSet();

      for (int localX = 0; localX < n && localX < list.size(); localX++) {
         StaffEntry staffEntry = (StaffEntry)list.get(localX);
         set.add(staffEntry.name());
         StaffListHud.RowAnim rowAnim = this.rows.get(staffEntry.name());
         if (rowAnim == null) {
            rowAnim = new StaffListHud.RowAnim(staffEntry);
            this.rows.put(staffEntry.name(), rowAnim);
         } else {
            rowAnim.entry = staffEntry;
         }

         rowAnim.anim.setTarget(1.0F);
      }

      ArrayList list2 = new ArrayList();

      for (int localZ = 0; localZ < n && localZ < list.size(); localZ++) {
         list2.add(this.rows.get(((StaffEntry)list.get(localZ)).name()));
      }

      Iterator it = this.rows.values().iterator();

      while (it.hasNext()) {
         StaffListHud.RowAnim rowAnim2 = (StaffListHud.RowAnim)it.next();
         if (!set.contains(rowAnim2.entry.name())) {
            rowAnim2.anim.setTarget(0.0F);
            if (rowAnim2.anim.value() <= 0.01F) {
               it.remove();
            } else {
               list2.add(rowAnim2);
            }
         }
      }

      return list2;
   }

   private float rowWidth(NVGRenderer nVGRenderer, StaffEntry staffEntry) {
      float temp = 15.0F + Math.min(nVGRenderer.textWidth(staffEntry.name(), 12.5F), 128.0F);
      if (this.module.showRank.get() && !staffEntry.rankLabel().isEmpty()) {
         temp += 6.0F + nVGRenderer.textWidth(staffEntry.rankLabel(), 9.0F);
      }

      if (this.module.showPing.get()) {
         temp += 19.0F;
      }

      return temp;
   }

   @Override
   public float measureWidth(NVGRenderer nVGRenderer) {
      List list = this.module.staff();
      float f = 92.0F;
      String text2 = Integer.toString(list.size());
      f = Math.max(f, nVGRenderer.textWidth("STAFF", 9.0F) + 10.0F + nVGRenderer.textWidth(text2, 8.5F) + 9.0F);

      for (StaffListHud.RowAnim rowAnim : this.layoutRows(list)) {
         f = Math.max(f, this.rowWidth(nVGRenderer, rowAnim.entry));
      }

      if (list.isEmpty()) {
         f = Math.max(f, 14.0F + nVGRenderer.textWidth("No staff online", 11.0F));
      }

      return 16.0F + f;
   }

   @Override
   public float measureHeight(NVGRenderer nVGRenderer) {
      List list = this.module.staff();
      float f = 0.0F;

      for (StaffListHud.RowAnim rowAnim : this.layoutRows(list)) {
         f += 18.0F * rowAnim.anim.value();
      }

      if (list.isEmpty() && f < 0.5F) {
         f = 16.0F;
      }

      float f3 = 28.0F + f + 8.0F;
      if (list.size() > this.shownCount(list)) {
         f3 += 13.0F;
      }

      return f3;
   }

   @Override
   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      Theme theme = this.themes.current();
      List list = this.module.staff();
      nVGRenderer.glow(tickDelta, tickDelta2, tickDelta3, tickDelta4, 13.0F, 8.0F, Colors.withAlpha(-16777216, 0.3F));
      nVGRenderer.rectGradient(tickDelta, tickDelta2, tickDelta3, tickDelta4, 10.0F, theme.background(), theme.backgroundTo(), true);
      this.drawHeader(nVGRenderer, theme, tickDelta, tickDelta2, tickDelta3, list.size());
      float f = tickDelta2 + 8.0F + 15.0F + 5.0F;
      if (list.isEmpty()) {
         float f4 = f + 8.0F;
         nVGRenderer.checkmark(tickDelta + 8.0F, f4 - 4.0F, 8.0F, 1.7F, -11870592);
         nVGRenderer.text("No staff online", tickDelta + 8.0F + 14.0F, f4, 11.0F, theme.textMuted());
      } else {
         for (StaffListHud.RowAnim rowAnim : this.layoutRows(list)) {
            float f5 = Math.clamp(rowAnim.anim.value(), 0.0F, 1.0F);
            if (!(f5 <= 0.01F)) {
               this.drawRow(nVGRenderer, theme, rowAnim.entry, tickDelta, f, tickDelta3, f5);
               f += 18.0F * f5;
            }
         }

         int n = list.size() - this.shownCount(list);
         if (n > 0) {
            nVGRenderer.text("+" + n + " more", tickDelta + 8.0F + 15.0F, f + 6.5F, 9.0F, theme.textDisabled());
         }
      }
   }

   private void drawHeader(NVGRenderer nVGRenderer, Theme theme, float f, float f10, float f11, int n) {
      float f12 = f10 + 8.0F + 7.5F;
      nVGRenderer.text("STAFF", f + 8.0F, f12, 9.0F, theme.textPrimary());
      String text2 = Integer.toString(n);
      float f13 = nVGRenderer.textWidth(text2, 8.5F);
      float f14 = f13 + 9.0F;
      float f15 = 12.5F;
      float f16 = f + f11 - 8.0F - f14;
      float f17 = f12 - f15 / 2.0F;
      boolean ok = n > 0;
      nVGRenderer.rect(f16, f17, f14, f15, 6.0F, Colors.withAlpha(theme.accent(), ok ? 0.2F : 0.1F));
      nVGRenderer.rectOutline(f16, f17, f14, f15, 6.0F, 1.0F, Colors.withAlpha(theme.accent(), ok ? 0.5F : 0.22F));
      nVGRenderer.text(text2, f16 + 4.5F, f12, 8.5F, ok ? theme.accentBright() : theme.textMuted());
   }

   private void drawRow(NVGRenderer nVGRenderer, Theme theme, StaffEntry staffEntry, float f, float f8, float f9, float f10) {
      float f11 = f8 + 9.0F;
      int n = staffEntry.hasColor() ? staffEntry.color() : theme.accent();
      nVGRenderer.save();
      nVGRenderer.alpha(f10);
      nVGRenderer.translate((1.0F - f10) * -10.0F, 0.0F);
      this.drawStar(nVGRenderer, f + 8.0F + 7.5F - 2.0F, f11, 4.2F, n, staffEntry.vanished());
      float f12 = f + 8.0F + 15.0F;
      int localX = staffEntry.vanished() ? theme.textMuted() : theme.textPrimary();
      float f13 = nVGRenderer.textTruncated(staffEntry.name(), f12, f11, 12.5F, localX, 128.0F);
      if (this.module.showRank.get() && !staffEntry.rankLabel().isEmpty()) {
         int localZ = staffEntry.hasColor() ? Colors.lighten(n, 0.25F) : theme.accent();
         nVGRenderer.text(staffEntry.rankLabel(), f12 + f13 + 6.0F, f11 + 0.5F, 9.0F, Colors.withAlpha(localZ, staffEntry.vanished() ? 0.6F : 0.95F));
      }

      if (this.module.showPing.get()) {
         this.drawPingBars(nVGRenderer, f + f9 - 8.0F - 11.0F, f11, staffEntry.latency(), theme);
      }

      nVGRenderer.restore();
   }

   private void drawStar(NVGRenderer nVGRenderer, float f, float f4, float f5, int n, boolean value) {
      if (value) {
         nVGRenderer.save();
         nVGRenderer.translate(f, f4);
         nVGRenderer.rotate((float) (Math.PI / 4));
         nVGRenderer.rectOutline(-f5 * 0.55F, -f5 * 0.55F, f5 * 1.1F, f5 * 1.1F, f5 * 0.25F, 1.2F, Colors.withAlpha(n, 0.55F));
         nVGRenderer.restore();
      } else {
         int localX = Colors.lighten(n, 0.4F);
         nVGRenderer.circleGlow(f, f4, f5 * 0.5F, f5 * 1.3F, Colors.withAlpha(n, 0.5F));
         int localZ = Colors.withAlpha(localX, 0.9F);
         nVGRenderer.line(f, f4 - f5, f, f4 + f5, 1.2F, localZ);
         nVGRenderer.line(f - f5, f4, f + f5, f4, 1.2F, localZ);
         nVGRenderer.save();
         nVGRenderer.translate(f, f4);
         nVGRenderer.rotate((float) (Math.PI / 4));
         nVGRenderer.rect(-f5 * 0.5F, -f5 * 0.5F, f5, f5, f5 * 0.22F, n);
         nVGRenderer.restore();
      }
   }

   private void drawPingBars(NVGRenderer nVGRenderer, float f, float f6, int n, Theme theme) {
      int localY = n < 0 ? 0 : (n <= 80 ? 4 : (n <= 150 ? 3 : (n <= 300 ? 2 : (n <= 600 ? 1 : 0))));
      int step = localY >= 3 ? -11870592 : (localY == 2 ? -340971 : -495247);

      for (int step2 = 0; step2 < 4; step2++) {
         float f7 = 2.0F + (float)step2 * 2.0F;
         float f8 = f + (float)step2 * 3.0F;
         float f9 = f6 + 4.0F - f7;
         int n9 = step2 < localY ? step : Colors.withAlpha(theme.textDisabled(), 0.45F);
         nVGRenderer.rect(f8, f9, 2.0F, f7, 0.5F, n9);
      }
   }

   private static final class RowAnim {
      StaffEntry entry;
      final Animation anim = new Animation(220.0F, 0.0F, Easing.EASE_OUT_CUBIC);

      RowAnim(StaffEntry staffEntry) {
         this.entry = staffEntry;
      }
   }
}
