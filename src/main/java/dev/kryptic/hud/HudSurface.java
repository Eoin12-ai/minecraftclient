package dev.kryptic.hud;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.render.Surface;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.theme.Theme;
import dev.kryptic.util.Colors;

/**
 * The material every HUD panel is drawn on.
 *
 * Each component used to paint its own background with a flat two-stop
 * gradient, which meant the HUD and the ClickGUI — sitting on screen at the
 * same time — were visibly made of different stuff. Routing them all through
 * here lets the whole client share one surface, and lets that surface be
 * swapped for the whole HUD at once.
 *
 * Card is the same flat material the menu panes use: a drop shadow to ground
 * the panel, an opaque body, and a hairline edge. Flat is a plain gradient
 * fill, kept for people who want their readouts with no chrome at all.
 */
public final class HudSurface {

    private HudSurface() {
    }

    /** A rectangular panel — cards, lists, the arraylist. */
    public static void panel(NVGRenderer nvg, float x, float y, float w, float h,
                             float radius, Theme theme) {
        if (card()) {
            Surface.surface(nvg, x, y, w, h, radius, theme, 0.0f);
        } else {
            nvg.rectGradient(x, y, w, h, radius, theme.background(), theme.backgroundTo(), true);
        }
    }

    /**
     * A panel with a title bar across the top.
     *
     * The header is a lighter band with the title in it and a hairline under,
     * so a list reads as a labelled group rather than as loose rows floating on
     * a rectangle. Returns where the body starts, so callers lay rows out from
     * one number instead of repeating the header height.
     */
    public static float titledPanel(NVGRenderer nvg, float x, float y, float w, float h,
                                    float headerH, String title, Theme theme) {
        panel(nvg, x, y, w, h, 4.0f, theme);
        nvg.rect(x + 1.0f, y + 1.0f, w - 2.0f, headerH - 1.0f, 3.0f,
                Colors.withAlpha(theme.headerTop(), 0.95f));
        nvg.rect(x + 1.0f, y + headerH - 1.0f, w - 2.0f, 1.0f, 0.0f,
                Colors.withAlpha(theme.accent(), 0.45f));
        // centred, because a title bar reads as a heading rather than a row
        float tw = nvg.textWidth(title, TITLE_FONT);
        nvg.text(title, x + (w - tw) / 2.0f, y + headerH / 2.0f, TITLE_FONT, theme.textPrimary());
        return y + headerH;
    }

    /** Size of a panel title. */
    public static final float TITLE_FONT = 11.5f;

    /** A capsule — the single-value readouts along the bottom. */
    public static void pill(NVGRenderer nvg, float x, float y, float w, float h, Theme theme) {
        if (card()) {
            Surface.pill(nvg, x, y, w, h, theme, 0.0f);
        } else {
            nvg.rectGradient(x, y, w, h, h / 2.0f, theme.background(), theme.backgroundTo(), true);
        }
    }

    /**
     * Whether the card backing is selected.
     *
     * Read live rather than cached: the setting is a switch in the menu, and a
     * HUD that only changed material after a restart would read as broken.
     * Defaults to the card when the module manager is not up yet, which is the
     * same answer the setting's own default gives.
     */
    private static boolean card() {
        ModuleManager modules = KrypticClient.modules();
        if (modules == null || modules.hud == null) return true;
        return modules.hud.style.is("Card");
    }
}
