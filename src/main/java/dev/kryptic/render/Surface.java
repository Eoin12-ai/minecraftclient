package dev.kryptic.render;

import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.theme.Theme;
import dev.kryptic.util.Colors;

/**
 * The panels, cards and buttons everything else is drawn on.
 *
 * This was five stacked layers per surface — a drop shadow, an accent bloom, a
 * translucent gradient body, a specular sheen, an inner and outer rim — plus a
 * highlight band that travelled across every header on a timer. That is the
 * frosted-glass look every cracked client has shipped since about 2019, and it
 * is most of why this menu was recognisable as one.
 *
 * A card here is opaque, square-shouldered and flat: one fill, one hairline,
 * one shadow to lift it off the world. Three consequences, all of them wanted:
 *
 * <ul>
 *   <li>Text sits on a known colour. The old bodies were translucent over a
 *       blurred world, so label contrast changed as the player turned around.</li>
 *   <li>The accent means something. When it is not also the border, the bloom
 *       and the rim of every surface, an accent-coloured thing on screen is
 *       reliably a thing that is switched on.</li>
 *   <li>It costs four draw calls a panel instead of a dozen.</li>
 * </ul>
 *
 * {@code lift} runs 0..1 for rest..focused and now only moves the border and
 * the shadow, rather than brightening the whole surface.
 */
public final class Surface {

    /** Corner radius. Tighter than the old 14, which read as a lozenge. */
    public static final float RADIUS = 8.0f;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private Surface() {
    }

    // ── timing ────────────────────────────────────────────────────────────────

    /** Rising 0..1 sawtooth with the given period, in seconds. */
    public static float phase(float periodSeconds) {
        double seconds = (System.nanoTime() % 1_000_000_000_000L) / 1.0e9;
        double p = seconds % periodSeconds;
        return (float) (p / periodSeconds);
    }

    // ── parts ─────────────────────────────────────────────────────────────────

    /** A soft drop shadow, so a card reads as sitting above the world. */
    public static void shadow(NVGRenderer nvg, float x, float y, float w, float h,
                              float r, float sigma, float alpha) {
        nvg.glow(x, y + sigma * 0.3f, w, h, r, sigma, Colors.withAlpha(BLACK, alpha));
    }

    /** The opaque body. */
    public static void body(NVGRenderer nvg, float x, float y, float w, float h,
                            float r, Theme th) {
        nvg.rect(x, y, w, h, r, th.background());
    }

    /**
     * One hairline border.
     *
     * Warm grey at rest and the accent when focused, so focus is legible
     * without the surface itself changing brightness.
     */
    public static void edge(NVGRenderer nvg, float x, float y, float w, float h,
                            float r, Theme th, float lift) {
        int rest = Colors.withAlpha(WHITE, 0.09f);
        int active = Colors.withAlpha(th.accent(), 0.72f);
        nvg.rectOutline(x, y, w, h, r, 1.0f, Colors.lerp(rest, active, lift));
    }

    // ── composites ────────────────────────────────────────────────────────────

    /** A panel: shadow, opaque body, hairline. */
    public static void card(NVGRenderer nvg, float x, float y, float w, float h,
                            float r, Theme th, float lift) {
        if (w <= 0.0f || h <= 0.0f) return;
        shadow(nvg, x, y, w, h, r, 14.0f, 0.38f);
        body(nvg, x, y, w, h, r, th);
        edge(nvg, x, y, w, h, r, th, lift);
    }

    /** Kept under the old name so panel and HUD callers read the same. */
    public static void surface(NVGRenderer nvg, float x, float y, float w, float h,
                               float r, Theme th, float lift) {
        card(nvg, x, y, w, h, r, th, lift);
    }

    /**
     * A button or a field.
     *
     * A rounded rectangle rather than a capsule: at this size a full capsule
     * wastes its ends on curve and centres the label badly, and a row of them
     * reads as a toolbar of lozenges rather than of buttons.
     */
    public static void button(NVGRenderer nvg, float x, float y, float w, float h,
                              Theme th, float lift) {
        if (w <= 0.0f || h <= 0.0f) return;
        float r = 6.0f;
        shadow(nvg, x, y, w, h, r, 8.0f, 0.30f);
        // a focused button fills faintly with the accent as well as edging it
        nvg.rect(x, y, w, h, r, th.background());
        if (lift > 0.01f) {
            nvg.rect(x, y, w, h, r, Colors.withAlpha(th.accent(), 0.14f * lift));
        }
        edge(nvg, x, y, w, h, r, th, lift);
    }

    /** Kept under the old name for the HUD's rounded readouts. */
    public static void pill(NVGRenderer nvg, float x, float y, float w, float h,
                            Theme th, float lift) {
        button(nvg, x, y, w, h, th, lift);
    }
}
