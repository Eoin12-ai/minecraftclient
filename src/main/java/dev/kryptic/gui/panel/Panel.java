package dev.kryptic.gui.panel;

import dev.kryptic.gui.ClickGuiState;
import dev.kryptic.render.Surface;
import dev.kryptic.render.anim.Animation;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import dev.kryptic.util.UiSounds;

/**
 * Panel — a single liquid-glass column of the ClickGUI.
 *
 * The pane is drawn entirely from {@link Surface} parts so it shares one
 * material with the search bar and the bottom pills: translucent frosted body,
 * specular sheen over the top, inner white hairline, accent rim, soft drop
 * shadow, and a slow travelling highlight across the header.
 *
 * Geometry is driven by the screen rather than by the panel: the ClickGUI hands
 * each column its width via {@link #setWidth} and a fixed body height via
 * {@link #setViewportHeight}, so all five columns line up exactly. A panel with
 * no fixed viewport (the floating Themes pane) still sizes itself to content.
 *
 * Panels are locked in place — {@link #moveTo} exists for programmatic
 * positioning only; there is no drag handling anywhere in the GUI.
 */
public abstract class Panel {

    // ── geometry ─────────────────────────────────────────────────────────────
    public  static final float WIDTH    = 220.0f;   // fallback when no width is set
    public  static final float HEADER_H = 38.0f;
    public  static final float RADIUS   = Surface.RADIUS;
    protected static final float CONTENT_PAD = 6.0f;
    private  static final float FADE_ZONE    = 18.0f;

    // ── header hairline ──────────────────────────────────────────────────────
    private static final float STRIP_H     = 1.0f;
    private static final float STRIP_ALPHA = 0.55f;

    // ── scrollbar ────────────────────────────────────────────────────────────
    private static final float BAR_W        = 3.0f;
    private static final float BAR_INSET    = 3.0f;
    private static final float BAR_MIN_H    = 24.0f;

    // ── state ─────────────────────────────────────────────────────────────────
    protected final ThemeManager             themes;
    protected final ClickGuiState.PanelState panelState;
    private   final Animation                open;
    private   final Animation                scroll;
    private   final Animation                lift   = new Animation(150.0f, 0.0f);
    private         float                    maxScroll;
    private         boolean                  headerHovered;
    private         float                    width     = WIDTH;
    private         float                    viewportH = -1.0f;

    // ─────────────────────────────────────────────────────────────────────────

    protected Panel(ThemeManager themes, ClickGuiState.PanelState panelState) {
        this.themes     = themes;
        this.panelState = panelState;
        this.open       = new Animation(200.0f, panelState.collapsed ? 0.0f : 1.0f);
        // restore the scroll offset the GUI was left at, without animating to it
        this.scroll     = new Animation(200.0f, Math.max(0.0f, panelState.scroll));
    }

    // ── subclass contract ─────────────────────────────────────────────────────

    protected abstract String title();

    protected int icon() { return -1; }

    protected abstract float contentHeight(NVGRenderer nvg);

    protected abstract void renderContent(NVGRenderer nvg,
                                          float contentTop,
                                          float tickDelta2, float tickDelta3,
                                          float tickDelta4, float tickDelta5,
                                          float edgeFade);

    // ── layout helpers ────────────────────────────────────────────────────────

    protected Theme theme() { return themes.current(); }

    public float getX() { return panelState.x; }
    public float getY() { return panelState.y; }

    /** Width of this pane in UI units. */
    public float width() { return width; }

    public void setWidth(float w) { this.width = Math.max(120.0f, w); }

    /** Pins the body height so sibling columns align; pass a value &lt;= 0 to size to content. */
    public void setViewportHeight(float h) { this.viewportH = h; }

    public void moveTo(float x, float y) {
        panelState.x = x;
        panelState.y = y;
    }

    public boolean isCollapsed() { return panelState.collapsed; }

    public void toggleCollapsed() {
        panelState.collapsed = !panelState.collapsed;
        open.setTarget(panelState.collapsed ? 0.0f : 1.0f);
    }

    protected float maxViewHeight(float screenH) {
        if (viewportH > 0.0f) return viewportH;
        return Math.max(60.0f, screenH - panelState.y - HEADER_H - 24.0f);
    }

    protected float viewHeight(NVGRenderer nvg, float screenH) {
        float max = maxViewHeight(screenH);
        float h   = viewportH > 0.0f ? max : Math.min(contentHeight(nvg), max);
        return h * open.value();
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
        panelState.x = Math.clamp(panelState.x, -width + 40.0f, screenW - 40.0f);
        panelState.y = Math.clamp(panelState.y, 0.0f, Math.max(0.0f, screenH - HEADER_H));

        Theme   th     = theme();
        float   vH     = viewHeight(nvg, screenH);
        boolean isOpen = vH > 0.5f;
        boolean hHover = headerHit(mouseX, mouseY);

        // hover sound
        if (hHover && !headerHovered) UiSounds.hover();
        headerHovered = hHover;
        lift.setTarget(hHover ? 1.0f : 0.0f);
        float lv = lift.value();

        float px     = panelState.x;
        float py     = panelState.y;
        float w      = width;
        float totalH = HEADER_H + vH;

        // ── glass pane ───────────────────────────────────────────────────────
        Surface.surface(nvg, px, py, w, totalH, RADIUS, th, lv);

        // travelling highlight over the header only — reads as liquid, stays cheap

        // ── hairline under the header ────────────────────────────────────────
        if (isOpen) {
            nvg.rectGradient(
                    px + 1.0f, py + HEADER_H - STRIP_H,
                    w - 2.0f, STRIP_H, 0.5f,
                    Colors.withAlpha(th.accent(),       STRIP_ALPHA),
                    Colors.withAlpha(th.accentBright(), STRIP_ALPHA * 0.6f),
                    false);
        }

        // ── icon ─────────────────────────────────────────────────────────────
        float titleX = px + 13.0f;
        int iconHandle = icon();
        if (iconHandle > 0) {
            float iconY = py + HEADER_H / 2.0f - 9.0f;
            nvg.image(iconHandle, px + 11.0f, iconY, 18.0f, 18.0f, th.accentBright());
            titleX = px + 35.0f;
        }

        // ── title ─────────────────────────────────────────────────────────────
        nvg.text(title(), titleX, py + HEADER_H / 2.0f, 15.5f, th.textPrimary());

        // ── chevron (rotates 90° when expanded) ──────────────────────────────
        float chX = px + w - 15.0f;
        float chY = py + HEADER_H / 2.0f;
        nvg.save();
        nvg.translate(chX, chY);
        nvg.rotate((float)(this.open.value() * Math.PI / 2.0));
        nvg.chevron(0, 0, 4.0f, 1.6f, th.textMuted(), false);
        nvg.restore();

        // ── content ───────────────────────────────────────────────────────────
        if (isOpen) {
            float clipTop = py + HEADER_H;
            float clipBot = clipTop + vH;

            maxScroll = Math.max(0.0f, contentHeight(nvg) - maxViewHeight(screenH));
            scroll.setTarget(Math.clamp(scroll.getTarget(), 0.0f, maxScroll));
            panelState.scroll = scroll.getTarget();

            nvg.save();
            nvg.scissor(px, clipTop, w, vH);
            renderContent(nvg,
                    clipTop + CONTENT_PAD - scroll.value(),
                    mouseX, mouseY,
                    clipTop, clipBot,
                    1.0f);
            nvg.restore();

            renderScrollbar(nvg, th, px, clipTop, w, vH);
        }
    }

    private void renderScrollbar(NVGRenderer nvg, Theme th,
                                 float px, float top, float w, float vH) {
        if (maxScroll <= 0.5f || vH < BAR_MIN_H + 8.0f) return;
        float track   = vH - BAR_INSET * 2.0f;
        float frac    = track / (track + maxScroll);
        float thumbH  = Math.max(BAR_MIN_H, track * frac);
        float travel  = track - thumbH;
        float pos     = maxScroll <= 0.0f ? 0.0f : Math.clamp(scroll.value() / maxScroll, 0.0f, 1.0f);
        float thumbY  = top + BAR_INSET + travel * pos;
        float barX    = px + w - BAR_W - BAR_INSET;
        nvg.rect(barX, top + BAR_INSET, BAR_W, track, BAR_W / 2.0f,
                Colors.withAlpha(0xFFFFFFFF, 0.06f));
        nvg.rect(barX, thumbY, BAR_W, thumbH, BAR_W / 2.0f,
                Colors.withAlpha(th.accentBright(), 0.55f));
    }

    // ── hit tests ─────────────────────────────────────────────────────────────

    public boolean headerHit(float mx, float my) {
        return mx >= panelState.x && mx <= panelState.x + width
                && my >= panelState.y && my <= panelState.y + HEADER_H;
    }

    public boolean bodyHit(NVGRenderer nvg, float mx, float my, float screenH) {
        float vH = viewHeight(nvg, screenH);
        return mx >= panelState.x && mx <= panelState.x + width
                && my >= panelState.y + HEADER_H
                && my <= panelState.y + HEADER_H + vH;
    }

    // ── input ─────────────────────────────────────────────────────────────────

    /**
     * Scrolls so the given content offset sits at the top of the viewport.
     *
     * Clamped by render, which is the only place the content height is known
     * cheaply; an out-of-range target here corrects itself on the next frame
     * rather than needing the panel to measure itself twice.
     */
    protected void scrollTo(float offset) {
        scroll.setTarget(Math.max(0.0f, offset));
        panelState.scroll = scroll.getTarget();
    }

    public void onScroll(double delta) {
        if (maxScroll <= 0.0f) return;
        scroll.setTarget(Math.clamp(scroll.getTarget() - (float)delta * HEADER_H,
                0.0f, maxScroll));
        panelState.scroll = scroll.getTarget();
    }

    public boolean mouseClicked(float mx, float my, int btn) { return false; }
    public void    mouseDragged(float mx, float my)          {}
    public void    mouseReleased()                           {}
    public boolean keyPressed(int key)                       { return false; }
    public boolean charTyped(int cp)                         { return false; }
    public boolean isListening()                             { return false; }
}
