package dev.sixseven.gui.panel;

import dev.sixseven.gui.ClickGuiState;
import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.render.nanovg.NVGIcons;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Redesigned CategoryPanel.
 *
 * New features vs original:
 *  - Active-count badge in header: shows "3" (accent pill) when ≥1 module enabled
 *  - setFilter() now searches description too, not just name
 *  - Entry gap reduced 3 → 2 px (slightly denser — cleaner at 220 px wide)
 *  - Empty-state message when search yields no results
 */
public class CategoryPanel extends Panel {

    private static final float ENTRY_GAP     = 2.0f;
    private static final float BADGE_H       = 16.0f;
    private static final float BADGE_PAD_X   = 7.0f;
    private static final float BADGE_FONT    = 10.5f;
    private static final float EMPTY_FONT    = 12.5f;
    private static final float EMPTY_PAD     = 28.0f;

    private final Category          category;
    private final List<ModuleEntry> entries = new ArrayList<>();
    private       String            filter  = "";

    public CategoryPanel(Category category,
                         ModuleManager modules,
                         ThemeManager themes,
                         ClickGuiState state) {
        super(themes, state.panel(category.name()));
        this.category = category;
        for (Module m : modules.inCategory(category)) {
            entries.add(new ModuleEntry(m, themes, state));
        }
    }

    // ── filter ───────────────────────────────────────────────────────────────

    public void setFilter(String raw) {
        filter = raw == null ? "" : raw.toLowerCase(Locale.ROOT).trim();
    }

    private List<ModuleEntry> visibleEntries() {
        if (filter.isEmpty()) return entries;
        return entries.stream()
                .filter(e -> e.getModule().getName().toLowerCase(Locale.ROOT).contains(filter)
                          || e.getModule().getDescription().toLowerCase(Locale.ROOT).contains(filter))
                .toList();
    }

    // ── Panel contract ────────────────────────────────────────────────────────

    @Override
    protected String title() { return category.getDisplayName(); }

    @Override
    protected int icon() { return NVGIcons.get(category); }

    @Override
    protected float contentHeight(NVGRenderer nvg) {
        List<ModuleEntry> vis = visibleEntries();
        if (vis.isEmpty()) return EMPTY_PAD * 2.0f;

        float h = CONTENT_PAD;
        for (ModuleEntry e : vis) h += e.height(nvg) + ENTRY_GAP;
        return h - ENTRY_GAP + CONTENT_PAD;
    }

    // ── active-count badge ─────────────────────────────────────────────────
    // Drawn inside render() in Panel after title; we hook via renderContent calling
    // back, so we override the full Panel#render to append badge, then let super do content.
    //
    // Alternative: override nothing — badge position is derived from super render's
    // title text width which we can't get cheaply. Simpler: render badge in a fixed
    // position relative to panel right edge, same row as title.

    @Override
    public void render(NVGRenderer nvg,
                       float mouseX, float mouseY,
                       float screenW, float screenH) {
        super.render(nvg, mouseX, mouseY, screenW, screenH);
        renderBadge(nvg);
    }

    private void renderBadge(NVGRenderer nvg) {
        long activeCount = entries.stream()
                .filter(e -> e.getModule().isEnabled())
                .count();
        if (activeCount == 0) return;

        Theme th    = theme();
        String text = String.valueOf(activeCount);
        float  bW   = nvg.textWidth(text, BADGE_FONT) + BADGE_PAD_X * 2.0f;
        float  bH   = BADGE_H;
        // position: header right, vertically centred
        float  bX   = panelState.x + width() - 28.0f - bW;
        float  bY   = panelState.y + (Panel.HEADER_H - bH) / 2.0f;

        nvg.rect(bX, bY, bW, bH, bH / 2.0f,
                Colors.withAlpha(th.accent(), 0.22f));
        nvg.text(text, bX + BADGE_PAD_X, bY + bH / 2.0f, BADGE_FONT,
                th.accentBright());
    }

    // ── content ───────────────────────────────────────────────────────────────

    @Override
    protected void renderContent(NVGRenderer nvg,
                                 float contentTop,
                                 float mouseX, float mouseY,
                                 float clipTop, float clipBot,
                                 float edgeFade) {
        List<ModuleEntry> vis = visibleEntries();

        if (vis.isEmpty()) {
            // empty-state label
            Theme  th  = theme();
            float  cx  = panelState.x + width() / 2.0f;
            float  cy  = contentTop + EMPTY_PAD;
            nvg.text("No results", cx, cy, EMPTY_FONT, th.textDisabled());
            return;
        }

        float y = contentTop;
        for (ModuleEntry e : vis) {
            float eH = e.height(nvg);
            e.setBounds(panelState.x + CONTENT_PAD, y, width() - CONTENT_PAD * 2.0f);

            // only render rows touching the clip window
            if (y + eH >= clipTop - 20.0f && y <= clipBot + 20.0f) {
                float fade = edgeFade(y, y + eH, clipTop, clipBot);
                e.render(nvg, mouseX, mouseY, fade * edgeFade);
            }
            y += eH + ENTRY_GAP;
        }
    }

    // ── input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(float mx, float my, int btn) {
        for (ModuleEntry e : visibleEntries()) {
            if (e.mouseClicked(mx, my, btn)) return true;
        }
        return false;
    }

    @Override
    public void mouseDragged(float mx, float my) {
        visibleEntries().forEach(e -> e.mouseDragged(mx, my));
    }

    @Override
    public void mouseReleased() {
        visibleEntries().forEach(ModuleEntry::mouseReleased);
    }

    @Override
    public boolean keyPressed(int key) {
        return visibleEntries().stream().anyMatch(e -> e.keyPressed(key));
    }

    @Override
    public boolean charTyped(int cp) {
        return visibleEntries().stream().anyMatch(e -> e.charTyped(cp));
    }

    @Override
    public boolean isListening() {
        return visibleEntries().stream().anyMatch(ModuleEntry::isListening);
    }
}
