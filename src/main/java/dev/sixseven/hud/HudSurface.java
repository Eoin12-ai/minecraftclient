package dev.sixseven.hud;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.render.Glass;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.theme.Theme;

/**
 * The material every HUD panel is drawn on.
 *
 * Each component used to paint its own background with a flat two-stop
 * gradient, which meant the HUD and the ClickGUI — sitting on screen at the
 * same time — were visibly made of different stuff. Routing them all through
 * here lets the whole client share one surface, and lets that surface be
 * swapped for the whole HUD at once.
 *
 * Glass is the same five-layer material the menu panes use: a drop shadow to
 * ground the panel, accent bloom bleeding past the edge, a translucent body
 * that lightens toward the top, a specular sheen over the upper half, and a
 * hairline rim. Flat is the original pill, kept because the glass costs more
 * draw calls per panel and some people want their readouts plain.
 */
public final class HudSurface {

    private HudSurface() {
    }

    /** A rectangular panel — cards, lists, the arraylist. */
    public static void panel(NVGRenderer nvg, float x, float y, float w, float h,
                             float radius, Theme theme) {
        if (glass()) {
            Glass.surface(nvg, x, y, w, h, radius, theme, 0.0f);
        } else {
            nvg.rectGradient(x, y, w, h, radius, theme.background(), theme.backgroundTo(), true);
        }
    }

    /** A capsule — the single-value readouts along the bottom. */
    public static void pill(NVGRenderer nvg, float x, float y, float w, float h, Theme theme) {
        if (glass()) {
            Glass.pill(nvg, x, y, w, h, theme, 0.0f);
        } else {
            nvg.rectGradient(x, y, w, h, h / 2.0f, theme.background(), theme.backgroundTo(), true);
        }
    }

    /**
     * Whether the glass material is selected.
     *
     * Read live rather than cached: the setting is a switch in the menu, and a
     * HUD that only changed material after a restart would read as broken.
     * Defaults to glass when the module manager is not up yet, which is the
     * same answer the setting's own default gives.
     */
    private static boolean glass() {
        ModuleManager modules = SixSevenClient.modules();
        if (modules == null || modules.hud == null) return true;
        return modules.hud.style.is("Glass");
    }
}
