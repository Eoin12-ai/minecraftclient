package dev.kryptic.render;

import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.theme.Theme;
import dev.kryptic.util.Colors;

/**
 * Liquid-glass surface primitives.
 *
 * Every surface in the ClickGUI is built from the same five layers so the whole
 * menu reads as one material:
 *
 *   1. shadow   — soft drop shadow, offset down, grounds the pane
 *   2. bloom    — accent-tinted light bleeding out past the edge
 *   3. body     — translucent frosted fill, lighter at the top
 *   4. sheen    — specular falloff over the upper half
 *   5. rim      — inner white hairline + outer accent edge
 *
 * {@link #caustic} adds the slow travelling highlight that makes the material
 * feel wet rather than merely transparent. The world behind stays visible
 * because every fill is alpha-blended; the ClickGUI's background blur supplies
 * the frosting.
 */
public final class Glass {

    private Glass() {}

    /** Corner radius used by the large panes. */
    public static final float RADIUS = 14.0f;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    // ── timing ────────────────────────────────────────────────────────────────

    /** Rising 0..1 sawtooth with the given period, in seconds. */
    public static float phase(float periodSeconds) {
        double seconds = (System.nanoTime() % 1_000_000_000_000L) / 1.0e9;
        double p = seconds % periodSeconds;
        return (float) (p / periodSeconds);
    }

    // ── layers ────────────────────────────────────────────────────────────────

    public static void shadow(NVGRenderer nvg, float x, float y, float w, float h,
                              float r, float sigma, float alpha) {
        nvg.glow(x, y + sigma * 0.25f, w, h, r, sigma, Colors.withAlpha(BLACK, alpha));
    }

    public static void bloom(NVGRenderer nvg, float x, float y, float w, float h,
                             float r, Theme th, float lift) {
        nvg.glow(x, y, w, h, r, 10.0f + 8.0f * lift,
                Colors.withAlpha(th.accent(), 0.07f + 0.15f * lift));
    }

    /** Translucent frosted fill — lighter at the top, denser at the bottom. */
    public static void body(NVGRenderer nvg, float x, float y, float w, float h,
                            float r, Theme th, float lift) {
        int base = th.background();
        int top  = Colors.withAlpha(Colors.lighten(base, 0.17f + 0.09f * lift), 0.47f + 0.07f * lift);
        int bot  = Colors.withAlpha(Colors.darken(base, 0.20f), 0.68f);
        nvg.rectGradient(x, y, w, h, r, top, bot, true);
    }

    /** Specular falloff over the upper part of the surface. */
    public static void sheen(NVGRenderer nvg, float x, float y, float w, float h,
                             float r, float strength) {
        float sh = Math.min(h * 0.46f, 90.0f);
        if (sh < 2.0f || w < 4.0f) return;
        float rr = Math.max(0.0f, r - 1.0f);
        nvg.rectVaryingGradient(x + 1.0f, y + 1.0f, w - 2.0f, sh, rr, rr, 0.0f, 0.0f,
                Colors.withAlpha(WHITE, 0.13f * strength),
                Colors.withAlpha(WHITE, 0.0f));
    }

    /** Inner white hairline plus the outer accent edge. */
    public static void rim(NVGRenderer nvg, float x, float y, float w, float h,
                           float r, Theme th, float lift) {
        nvg.rectOutline(x + 0.9f, y + 0.9f, w - 1.8f, h - 1.8f, Math.max(0.0f, r - 0.9f), 1.0f,
                Colors.withAlpha(WHITE, 0.11f + 0.08f * lift));
        nvg.rectOutline(x, y, w, h, r, 1.25f,
                Colors.withAlpha(th.accent(), 0.26f + 0.34f * lift));
    }

    /** Travelling highlight band, clipped to the surface. Cheap and subtle. */
    public static void caustic(NVGRenderer nvg, float x, float y, float w, float h,
                               float seconds, float strength) {
        if (w < 8.0f || h < 2.0f) return;
        float p     = phase(seconds);
        float bandW = Math.max(40.0f, w * 0.34f);
        float bx    = x - bandW + (w + bandW * 2.0f) * p;
        int   peak  = Colors.withAlpha(WHITE, 0.055f * strength);
        int   clear = Colors.withAlpha(WHITE, 0.0f);
        nvg.save();
        nvg.scissor(x, y, w, h);
        nvg.rectGradient(bx, y, bandW * 0.5f, h, 0.0f, clear, peak, false);
        nvg.rectGradient(bx + bandW * 0.5f, y, bandW * 0.5f, h, 0.0f, peak, clear, false);
        nvg.restore();
    }

    // ── composites ────────────────────────────────────────────────────────────

    /** Full glass pane: shadow, bloom, body, sheen, rim. */
    public static void surface(NVGRenderer nvg, float x, float y, float w, float h,
                               float r, Theme th, float lift) {
        if (w <= 0.0f || h <= 0.0f) return;
        shadow(nvg, x, y, w, h, r, 15.0f + 7.0f * lift, 0.33f);
        bloom(nvg, x, y, w, h, r, th, lift);
        body(nvg, x, y, w, h, r, th, lift);
        sheen(nvg, x, y, w, h, r, 1.0f);
        rim(nvg, x, y, w, h, r, th, lift);
    }

    /** Capsule variant for search fields and buttons. */
    public static void pill(NVGRenderer nvg, float x, float y, float w, float h,
                            Theme th, float lift) {
        if (w <= 0.0f || h <= 0.0f) return;
        float r = h / 2.0f;
        shadow(nvg, x, y, w, h, r, 9.0f, 0.26f);
        bloom(nvg, x, y, w, h, r, th, lift);
        body(nvg, x, y, w, h, r, th, lift);
        sheen(nvg, x, y, w, h, r, 1.15f);
        rim(nvg, x, y, w, h, r, th, lift);
    }
}
