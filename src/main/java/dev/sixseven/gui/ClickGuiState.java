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
 * ClickGuiState — unchanged layout logic; favourites Set added.
 *
 * favouriteModules: Set<String> keyed by same module key as expandedModules.
 * Persisted in config JSON under "favourites" array.
 * Layout version bumped 3 → 4 so old configs still load (v>=3 check kept).
 */
public class ClickGuiState {

    public static final String THEMES_PANEL   = "__themes__";
    private static final int   LAYOUT_VERSION = 4;
    private static final float PANEL_W        = 220.0f;   // matches new Panel.WIDTH
    private static final float SEARCH_W       = 300.0f;   // matches new search bar

    private final Map<String, PanelState> panels          = new LinkedHashMap<>();
    private final Set<String>             expandedModules  = new HashSet<>();
    private final Set<String>             favouriteModules = new HashSet<>();  // NEW

    private boolean laidOut;
    private boolean customized;
    private float   lastLayoutWidth  = -1.0f;
    private float   lastLayoutHeight = -1.0f;

    // ─────────────────────────────────────────────────────────────────────────

    public ClickGuiState() {
        float x = 16.0f;
        for (Category c : Category.values()) {
            panels.put(c.name(), new PanelState(x, 16.0f));
            x += PANEL_W + 8.0f;
        }
        panels.put(THEMES_PANEL, new PanelState(x, 320.0f));
    }

    // ── layout ────────────────────────────────────────────────────────────────

    public void markCustomized() { customized = true; }

    public void ensureDefaultLayout(float sw, float sh) {
        if (!customized && (!laidOut || sw != lastLayoutWidth || sh != lastLayoutHeight)) {
            laidOut          = true;
            lastLayoutWidth  = sw;
            lastLayoutHeight = sh;

            String[] order = {
                Category.COMBAT.name(), Category.MISC.name(),
                Category.RENDER.name(), Category.VISUALS.name(),
                Category.CLIENT.name(), THEMES_PANEL
            };

            float needed = PANEL_W * order.length + 8.0f * (order.length - 1) + SEARCH_W + 24.0f;
            if (sw >= needed) {
                // wide layout — single row, evenly spaced
                float gap = (sw - PANEL_W * order.length - SEARCH_W) / (order.length + 1);
                float cx  = gap;
                for (int i = 0; i < order.length; i++) {
                    PanelState ps = panel(order[i]);
                    ps.x         = cx;
                    ps.y         = 66.0f;  // below search bar
                    ps.collapsed = false;
                    cx += PANEL_W + gap;
                    if (i == 2) cx += SEARCH_W + gap;  // gap around search centre
                }
            } else {
                // narrow layout — wrap into columns
                float colW   = PANEL_W + 8.0f;
                int   cols   = Math.max(1, (int)((sw - 24.0f) / colW));
                for (int i = 0; i < order.length; i++) {
                    PanelState ps = panel(order[i]);
                    ps.x         = 12.0f + (i % cols) * colW;
                    ps.y         = 66.0f + (i / cols) * (sh * 0.44f);
                    ps.collapsed = (i / cols) > 0;
                }
            }
        }
    }

    public PanelState panel(String key) {
        return panels.computeIfAbsent(key, k -> new PanelState(16.0f, 66.0f));
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
        root.addProperty("custom", customized);

        for (Map.Entry<String, PanelState> e : panels.entrySet()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x",         e.getValue().x);
            obj.addProperty("y",         e.getValue().y);
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
        if (!root.has("custom") || !root.get("custom").getAsBoolean()) return;

        laidOut   = true;
        customized = true;

        for (Map.Entry<String, PanelState> e : panels.entrySet()) {
            JsonObject obj = root.getAsJsonObject(e.getKey());
            if (obj == null) continue;
            if (obj.has("x"))         e.getValue().x         = obj.get("x").getAsFloat();
            if (obj.has("y"))         e.getValue().y         = obj.get("y").getAsFloat();
            if (obj.has("collapsed")) e.getValue().collapsed  = obj.get("collapsed").getAsBoolean();
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
