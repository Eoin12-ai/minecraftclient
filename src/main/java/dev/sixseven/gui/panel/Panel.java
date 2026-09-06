package dev.sixseven.gui.panel;

import dev.sixseven.gui.ClickGuiState;
import dev.sixseven.render.anim.Animation;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import dev.sixseven.util.UiSounds;

/**
 * Redesigned Panel — flat/minimal aesthetic.
 *
 * Visual changes vs original:
 *  - WIDTH 210 → 220  (slightly wider for breathing room)
 *  - HEADER_H 38 → 36 (tighter — content starts sooner)
 *  - RADIUS  12 → 10  (less bubbly, cleaner corners)
 *  - Accent strip under header replaced by a 1 px hairline — half the weight
 *  - Shadow glow sigma reduced: 14 → 8 (subtle, not muddy)
 *  - Header gradient kept but flattened (headerTop == headerBottom in flat themes)
 *  - Chevron rotates 90° on expand (unchanged) — still crisp
 *  - Content background is a single flat rect, no gradient (cleaner on dark themes)
 *  - Scroll target snaps to nearest 36 px (one row) — feels intentional
 */
public abstract class Panel {

    // ── geometry ─────────────────────────────────────────────────────────────
    public  static final float WIDTH    = 220.0f;
    public  static final float HEADER_H = 36.0f;
    public  static final float RADIUS   = 10.0f;
    protected static final float CONTENT_PAD = 6.0f;
    private  static final float FADE_ZONE    = 18.0f;

    // ── accent strip ─────────────────────────────────────────────────────────
    private static final float STRIP_H       = 1.0f;   // hairline under header
    private static final float STRIP_ALPHA   = 0.55f;

    // ── shadow ───────────────────────────────────────────────────────────────
    private static final float GLOW_SIGMA_IDLE  = 8.0f;
    private static final float GLOW_SIGMA_HOVER = 12.0f;
    private static final float GLOW_ALPHA       = 0.32f;

    // ── state ─────────────────────────────────────────────────────────────────
    protected final ThemeManager         themes;
    protected final ClickGuiState.PanelState panelState;
    private   final Animation            open;
    private   final Animation            scroll = new Animation(200.0f, 0.0f);
    private         float                maxScroll;
    private         boolean              headerHovered;

    // ─────────────────────────────────────────────────────────────────────────

    protected Panel(ThemeManager themes, ClickGuiState.PanelState panelState) {
        this.themes     = themes;
        this.panelState = panelState;
        this.open       = new Animation(200.0f, panelState.collapsed ? 0.0f : 1.0f);
    }

    // ── subclass contract ─────────────────────────────────────────────────────

    protected abstract String title();

    protected int icon() { return -1; }

    protected abstract float contentHeight(NVGRenderer nvg);

    protected abstract void renderContent(NVGRenderer nvg,
                                          float contentTop,
                                          float mouseX, float mouseY,
                                          float clipTop, float clipBot,
                                          float edgeFade);

    // ── layout helpers ────────────────────────────────────────────────────────

    protected Theme theme() { return themes.current(); }

    public float getX() { return panelState.x; }
    public float getY() { return panelState.y; }

    public void moveTo(float x, float y) {
        panelState.x = x;
        panelState.y = y;
    }

    public void toggleCollapsed() {
        panelState.collapsed = !panelState.collapsed;
        open.setTarget(panelState.collapsed ? 0.0f : 1.0f);
    }

    protected float maxViewHeight(float screenH) {
        return Math.max(60.0f, screenH - panelState.y - HEADER_H - 24.0f);
    }

    protected float viewHeight(NVGRenderer nvg, float screenH) {
        return Math.min(contentHeight(nvg), maxViewHeight(screenH)) * open.value();
    }

    public float totalHeight(NVGRenderer nvg, float screenH) {
        return HEADER_H + viewHeight(nvg, screenH);
    }

    /** Returns [0,1] edge-fade factor so content dissolves near clip boundary. */
    protected float edgeFade(float y, float yBot, float clipTop, float clipBot) {
        float fadeIn  = Math.clamp((y    - clipTop) / FADE_ZONE, 0.0f, 1.0f);
        float fadeOut = Math.clamp((clipBot - yBot) / FADE_ZONE, 0.0f, 1.0f);
        return Math.min(fadeIn, fadeOut);
    }

    // ── render ────────────────────────────────────────────────────────────────

    public void render(NVGRenderer nvg,
                       float mouseX, float mouseY,
                       float screenW, float screenH) {

        // keep on screen
        panelState.x = Math.clamp(panelState.x, -WIDTH + 40.0f, screenW - 40.0f);
        panelState.y = Math.clamp(panelState.y, 0.0f, screenH - HEADER_H);

        Theme  th      = theme();
        float  vH      = viewHeight(nvg, screenH);
        boolean open   = vH > 0.5f;
        boolean hHover = headerHit(mouseX, mouseY);

        // hover sound
        if (hHover && !headerHovered) UiSounds.hover();
        headerHovered = hHover;

        float px = panelState.x;
        float py = panelState.y;
        float totalH = HEADER_H + vH;

        // ── drop shadow ──────────────────────────────────────────────────────
        float glowSigma = hHover ? GLOW_SIGMA_HOVER : GLOW_SIGMA_IDLE;
        nvg.glow(px, py, WIDTH, totalH, RADIUS, glowSigma,
                Colors.withAlpha(0xFF000000, GLOW_ALPHA));

        // ── content background (flat, single colour) ─────────────────────────
        if (open) {
            nvg.rect(px, py + HEADER_H, WIDTH, vH,
                    new float[]{0, 0, RADIUS, RADIUS},
                    th.background());
        }

        // ── header background ────────────────────────────────────────────────
        float[] headerRadii = open
                ? new float[]{RADIUS, RADIUS, 0, 0}
                : new float[]{RADIUS, RADIUS, RADIUS, RADIUS};
        nvg.rectVaryingGradient(px, py, WIDTH, HEADER_H,
                headerRadii[0], headerRadii[1], headerRadii[2], headerRadii[3],
                th.headerTop(), th.headerBottom());

        // ── hairline accent strip ────────────────────────────────────────────
        if (open) {
            nvg.rectGradient(
                    px + 1.0f, py + HEADER_H - STRIP_H,
                    WIDTH - 2.0f, STRIP_H, 0.5f,
                    Colors.withAlpha(th.accent(),      STRIP_ALPHA),
                    Colors.withAlpha(th.accentBright(), STRIP_ALPHA * 0.6f),
                    false);
        }

        // ── thin outer border — entire panel ────────────────────────────────
        nvg.rectOutline(px, py, WIDTH, totalH, RADIUS, 1.0f,
                Colors.withAlpha(th.accent(), open ? 0.22f : 0.14f));

        // ── icon ─────────────────────────────────────────────────────────────
        float titleX = px + 12.0f;
        int iconHandle = icon();
        if (iconHandle > 0) {
            float iconY = py + HEADER_H / 2.0f - 9.0f;
            nvg.image(iconHandle, px + 10.0f, iconY, 18.0f, 18.0f,
                    th.accentBright());
            titleX = px + 34.0f;
        }

        // ── title ─────────────────────────────────────────────────────────────
        nvg.text(title(), titleX, py + HEADER_H / 2.0f, 15.5f, th.textPrimary());

        // ── chevron (rotates 90° when expanded) ──────────────────────────────
        float chX = px + WIDTH - 15.0f;
        float chY = py + HEADER_H / 2.0f;
        nvg.save();
        nvg.translate(chX, chY);
        nvg.rotate((float)(this.open.value() * Math.PI / 2.0));
        nvg.chevron(0, 0, 4.0f, 1.6f, th.textMuted(), false);
        nvg.restore();

        // ── content ───────────────────────────────────────────────────────────
        if (open) {
            float clipTop = py + HEADER_H;
            float clipBot = clipTop + vH;

            maxScroll = Math.max(0.0f, contentHeight(nvg) - maxViewHeight(screenH));
            scroll.setTarget(Math.clamp(scroll.getTarget(), 0.0f, maxScroll));

            nvg.save();
            nvg.scissor(px, clipTop, WIDTH, vH);
            renderContent(nvg,
                    clipTop + CONTENT_PAD - scroll.value(),
                    mouseX, mouseY,
                    clipTop, clipBot,
                    1.0f /* outer fade handled per-entry */);
            nvg.restore();
        }
    }

    // ── hit tests ─────────────────────────────────────────────────────────────

    public boolean headerHit(float mx, float my) {
        return mx >= panelState.x && mx <= panelState.x + WIDTH
                && my >= panelState.y && my <= panelState.y + HEADER_H;
    }

    public boolean bodyHit(NVGRenderer nvg, float mx, float my, float screenH) {
        float vH = viewHeight(nvg, screenH);
        return mx >= panelState.x && mx <= panelState.x + WIDTH
                && my >= panelState.y + HEADER_H
                && my <= panelState.y + HEADER_H + vH;
    }

    // ── input ─────────────────────────────────────────────────────────────────

    public void onScroll(double delta) {
        if (maxScroll <= 0.0f) return;
        scroll.setTarget(Math.clamp(scroll.getTarget() - (float)delta * HEADER_H,
                0.0f, maxScroll));
    }

    public boolean mouseClicked(float mx, float my, int btn) { return false; }
    public void    mouseDragged(float mx, float my)          {}
    public void    mouseReleased()                           {}
    public boolean keyPressed(int key)                       { return false; }
    public boolean charTyped(int cp)                         { return false; }
    public boolean isListening()                             { return false; }
}
