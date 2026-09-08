package dev.sixseven.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sixseven.module.Category;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * ClickGuiState — layout, expansion and favourites for the ClickGUI.
 *
 * The layout is now a locked column grid: one full-height column per
 * {@link Category}, five of them today, sized and centred by
 * {@link #applyColumnLayout}. Panels are no longer draggable, so stored x/y
 * coordinates are recomputed every frame and only the collapsed flag and the
 * favourites set carry over between sessions.
 *
 * Layout version bumped 4 → 5; older configs still load (v>=3 check kept) and
 * their stored coordinates are simply overwritten by the column layout.
 */
public class ClickGuiState {

    public static final String THEMES_PANEL   = "__themes__";
    private static final int   LAYOUT_VERSION = 5;
    private static final float PANEL_W        = 220.0f;   // fallback column width

    // ── locked column grid ───────────────────────────────────────────────────

    /** One column per category — five with the current category set. */
    public static final  int   COLUMNS     = Category.values().length;
    private static final float COL_GAP     = 12.0f;
    private static final float SIDE_MARGIN = 20.0f;
    private static final float COL_MIN_W   = 160.0f;
    private static final float COL_MAX_W   = 300.0f;
    private static final float COL_TOP     = 68.0f;   // clears the search bar
    private static final float COL_BOTTOM  = 58.0f;   // clears the bottom pill bar

    private final Map<String, PanelState> panels           = new LinkedHashMap<>();
    private final Set<String>             expandedModules  = new HashSet<>();
    private final Set<String>             favouriteModules = new HashSet<>();

    private float colW    = PANEL_W;
    private float colX0   = SIDE_MARGIN;
    private float colTop  = COL_TOP;
    private float colH    = 0.0f;

    // ─────────────────────────────────────────────────────────────────────────

    public ClickGuiState() {
        float x = SIDE_MARGIN;
        for (Category c : Category.values()) {
            panels.put(c.name(), new PanelState(x, COL_TOP));
            x += PANEL_W + COL_GAP;
        }
        panels.put(THEMES_PANEL, new PanelState(x, 320.0f));
    }

    // ── layout ────────────────────────────────────────────────────────────────

    /**
     * Recomputes the locked column grid for the given UI size. Cheap enough to
     * call every frame, which is what keeps the layout pinned on resize.
     */
    public void applyColumnLayout(float sw, float sh) {
        float avail = Math.max(COL_MIN_W * COLUMNS, sw - SIDE_MARGIN * 2.0f);
        float raw   = (avail - COL_GAP * (COLUMNS - 1)) / COLUMNS;
        colW  = Math.clamp(raw, COL_MIN_W, COL_MAX_W);
        colX0 = Math.max(4.0f, (sw - columnsWidth()) / 2.0f);
        colTop = COL_TOP;
        colH   = Math.max(120.0f, sh - COL_TOP - COL_BOTTOM);

        int i = 0;
        for (Category c : Category.values()) {
            PanelState ps = panel(c.name());
            ps.x = colX0 + i * (colW + COL_GAP);
            ps.y = colTop;
            i++;
        }
    }

    public float columnWidth()  { return colW; }
    public float columnGap()    { return COL_GAP; }
    public float columnsLeft()  { return colX0; }
    public float columnsWidth() { return colW * COLUMNS + COL_GAP * (COLUMNS - 1); }
    public float columnTop()    { return colTop; }
    public float columnHeight() { return colH; }

    public PanelState panel(String key) {
        return panels.computeIfAbsent(key, k -> new PanelState(SIDE_MARGIN, COL_TOP));
    }

    // ── expanded ─────────────────────────────────────────────────────────────

    public boolean isExpanded(String key) { return expandedModules.contains(key); }

    public void setExpanded(String key, boolean val) {
        if (val) expandedModules.add(key); else expandedModules.remove(key);
    }

    // ── favourites ────────────────────────────────────────────────────────────

    public boolean isFavourite(String key) { return favouriteModules.contains(key); }

    public void setFavourite(String key, boolean val) {
        if (val) favouriteModules.add(key); else favouriteModules.remove(key);
    }

    // ── serialisation ─────────────────────────────────────────────────────────

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("v", LAYOUT_VERSION);
        root.addProperty("custom", true);

        for (Map.Entry<String, PanelState> e : panels.entrySet()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("collapsed", e.getValue().collapsed);
            root.add(e.getKey(), obj);
        }

        JsonArray favArr = new JsonArray();
        for (String fk : favouriteModules) favArr.add(fk);
        root.add("favourites", favArr);

        return root;
    }

    public void fromJson(JsonObject root) {
        if (!root.has("v") || root.get("v").getAsInt() < 3) return;

        for (Map.Entry<String, PanelState> e : panels.entrySet()) {
            JsonObject obj = root.getAsJsonObject(e.getKey());
            if (obj == null) continue;
            if (obj.has("collapsed")) e.getValue().collapsed = obj.get("collapsed").getAsBoolean();
        }

        favouriteModules.clear();
        if (root.has("favourites")) {
            for (JsonElement el : root.getAsJsonArray("favourites")) {
                favouriteModules.add(el.getAsString());
            }
        }
    }

    // ── inner ─────────────────────────────────────────────────────────────────

    public static class PanelState {
        public float   x;
        public float   y;
        public boolean collapsed;

        PanelState(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
