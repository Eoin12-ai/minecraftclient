package dev.kryptic.gui;

import java.util.Locale;
import dev.kryptic.gui.panel.ModuleEntry;
import dev.kryptic.module.Module;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.gui.Click;
import dev.kryptic.KrypticClient;
import dev.kryptic.gui.config.ConfigPanel;
import dev.kryptic.config.ConfigStore;
import dev.kryptic.config.ServerConfigs;
import dev.kryptic.gui.panel.CategoryPanel;
import dev.kryptic.gui.panel.Panel;
import dev.kryptic.gui.panel.StatsPanel;
import dev.kryptic.render.nanovg.NVGImages;
import net.minecraft.util.Identifier;
import dev.kryptic.gui.panel.ThemesPanel;
import dev.kryptic.gui.picker.BlockGridModel;
import dev.kryptic.gui.picker.IconListGridModel;
import dev.kryptic.module.Category;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.module.render.BlockEspModule;
import dev.kryptic.render.BlurHook;
import dev.kryptic.render.Surface;
import dev.kryptic.render.NvgDrawable;
import dev.kryptic.render.OverlayRenderer;
import dev.kryptic.render.anim.Animation;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.settings.BlockListSetting;
import dev.kryptic.settings.IconListSetting;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import dev.kryptic.util.UiSounds;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import dev.kryptic.module.client.ClickGuiModule;
import dev.kryptic.module.client.DiscordPresenceModule;

/**
 * The module menu: five locked columns under one row of controls.
 *
 * One column per {@link Category}, centred as a group and recomputed every
 * frame from the UI size. Columns cannot be dragged or reordered — the grid is
 * the layout, not a starting arrangement — and each is as tall as its content
 * up to the space available, with longer lists scrolling inside their own pane.
 *
 * The controls sit in one row along the top with nothing behind them: the mark,
 * the search field, and the three panes. Themes, Configs and Stats open as
 * floating panes over the grid rather than as extra columns, which is what
 * keeps the count at five.
 *
 * Every surface comes from {@link Surface} so the whole menu is one material.
 */
public class ClickGuiScreen extends Screen implements NvgDrawable {

    private static final ClickGuiState STATE = new ClickGuiState();

    // ── the top bar ──────────────────────────────────────────────────────────
    //
    // The search used to float in the middle of the sky and the three buttons
    // sat in a second bar pinned to the bottom, which put the menu's own
    // controls at two opposite edges with the modules stranded between them.
    // One row along the top holds all of it, on nothing: the mark, the search,
    // the three panes. The columns keep their size and position, because
    // COL_TOP already cleared the old floating search.
    /** The client mark, drawn in the bar in place of a set wordmark. */
    private static final Identifier LOGO =
            Identifier.of("krypticclient", "textures/logo.png");
    private static final float LOGO_ASPECT   = 319.0f / 512.0f;

    private static final float BAR_Y         = 10.0f;
    private static final float BAR_H         = 44.0f;

    private static final float SEARCH_W      = 260.0f;
    private static final float SEARCH_H      = 28.0f;
    private static final float MARK_W        = 62.0f;   // the KP mark, drawn to scale
    private static final float MARK_GAP      = 18.0f;

    private static final float PILL_W        = 96.0f;
    private static final float PILL_H        = 28.0f;
    private static final float PILL_GAP      = 8.0f;
    private static final float BAR_MID_GAP   = 18.0f;  // search to buttons

    private static final float THEMES_W      = 300.0f;
    private static final float STATS_W       = 400.0f;

    private static final float HINT_FONT     = 11.0f;
    private static final float HINT_MARGIN   = 6.0f;

    private final List<CategoryPanel> columns     = new ArrayList<>();
    private final ThemesPanel         themesPanel;
    private final StatsPanel          statsPanel;
    private final Animation           openAnim    = new Animation(180.0f, 0.0f);
    private boolean                   closing;
    private final ConfigPanel         configPanel = new ConfigPanel();
    private boolean                   configHovered;
    private boolean                   themesHovered;
    private boolean                   themesOpen;
    private boolean                   statsHovered;
    private boolean                   statsOpen;
    private final StringBuilder       search      = new StringBuilder();
    private boolean                   searchFocused;
    private Panel                     pressedContentPanel;
    private final Screen              parent;

    /**
     * How many frames the NanoVG pass has completed, and what it read last
     * time the vanilla pass ran.
     *
     * The vanilla render() runs before the overlay each frame, so comparing
     * these tells the fallback whether NanoVG produced anything at all last
     * frame. A one-frame lag is invisible and the check needs no knowledge of
     * why NanoVG is silent -- a missing font, a latched crash, a framebuffer
     * that will not bind. Any of them, the menu still draws.
     */
    private int nvgFrames;
    private int nvgFramesSeen = -1;

    public ClickGuiScreen()              { this(null); }

    public ClickGuiScreen(Screen parent) {
        super(Text.literal("Kryptic Client ClickGUI"));
        this.parent = parent;
        ModuleManager mm = KrypticClient.modules();
        ThemeManager  tm = KrypticClient.themes();
        for (Category c : Category.values()) columns.add(new CategoryPanel(c, mm, tm, STATE));
        themesPanel = new ThemesPanel(tm, STATE);
        themesPanel.setWidth(THEMES_W);
        statsPanel = new StatsPanel(tm, STATE);
        statsPanel.setWidth(STATS_W);
        layoutColumns();
        openAnim.setTarget(1.0f);
    }

    @Override public void onDisplayed() { super.onDisplayed(); UiSounds.guiOpen(); }
    public static ClickGuiState state()     { return STATE; }
    private ClickGuiModule guiModule()      { return KrypticClient.modules().clickGui; }
    @Override public boolean shouldPause()  { return false; }

    @Override
    public void close() {
        if (!closing) { closing = true; openAnim.setTarget(0.0f); UiSounds.guiClose(); }
    }

    @Override
    public void removed() { BlurHook.clear(); KrypticClient.config().save(); }

    // ── locked layout ─────────────────────────────────────────────────────────

    private void layoutColumns() {
        layoutColumns(OverlayRenderer.uiWidth(), OverlayRenderer.uiHeight());
    }

    /** Recomputes the five-column grid and pins every pane to its slot. */
    private void layoutColumns(float sw, float sh) {
        STATE.applyColumnLayout(sw, sh);
        float colW = STATE.columnWidth();
        float bodyH = Math.max(80.0f, STATE.columnHeight() - Panel.HEADER_H);
        for (CategoryPanel p : columns) {
            p.setWidth(colW);
            p.setViewportHeight(bodyH);
        }
        float tw = Math.min(THEMES_W, Math.max(220.0f, sw - 40.0f));
        themesPanel.setWidth(tw);
        themesPanel.moveTo((sw - tw) / 2.0f, STATE.columnTop() + 24.0f);
        float stw = Math.min(STATS_W, Math.max(260.0f, sw - 40.0f));
        statsPanel.setWidth(stw);
        statsPanel.moveTo((sw - stw) / 2.0f, STATE.columnTop() + 24.0f);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int dimAlpha = (int)(48.0f * openAnim.value());
        ctx.fill(0, 0, width, height, dimAlpha << 24 | 0x060608);

        boolean nvgSilent = nvgFrames == nvgFramesSeen;
        nvgFramesSeen = nvgFrames;
        if (nvgSilent) {
            renderVanillaFallback(ctx, mx, my);
        }

        finishCloseIfDone();
    }

    /**
     * The menu, drawn with Minecraft's own renderer.
     *
     * This exists because the NanoVG overlay has more than one way to produce
     * nothing -- no font loaded, the overlay latched off after a throw, a
     * framebuffer that would not bind -- and every one of them looked the same
     * from the outside: an invisible menu that still accepts clicks, because
     * the input handlers never depended on drawing.
     *
     * It is not meant to be pretty. It uses the same layout and the same hit
     * boxes as the real thing, so anything you can click here does what it
     * would have done, and it says plainly that it is the fallback so nobody
     * mistakes it for the design.
     */
    private void renderVanillaFallback(DrawContext ctx, int mx, int my) {
        ModuleManager modules = KrypticClient.modules();
        if (modules == null) return;

        // layoutColumns normally runs from renderNvg. With NanoVG silent it
        // never would, so a window resize would leave this drawing to stale
        // geometry -- and to hit boxes that no longer match where you click.
        layoutColumns();

        int rowH   = Math.round(OverlayRenderer.uiToGui(ModuleEntry.ROW_H));
        int headH  = Math.round(OverlayRenderer.uiToGui(Panel.HEADER_H));
        int colW   = Math.round(OverlayRenderer.uiToGui(STATE.columnWidth()));
        int gap    = Math.round(OverlayRenderer.uiToGui(STATE.columnGap()));
        int left   = Math.round(OverlayRenderer.uiToGui(STATE.columnsLeft()));
        int top    = Math.round(OverlayRenderer.uiToGui(STATE.columnTop()));

        ctx.fill(0, 0, width, height, 0xE0090909);
        // Say why, on screen. The styled menu failing is not something the user
        // can diagnose from a log file they have to go and find, and the reason
        // is the one piece of information that makes the next report useful.
        String reason = OverlayRenderer.lastError();
        ctx.drawText(this.client.textRenderer,
                Text.literal("Kryptic - fallback view: " + (reason == null
                        ? "the styled menu drew nothing (no overlay error; likely no font loaded)"
                        : reason)),
                left, Math.max(4, top - 14), 0xFFE08A8A, true);

        String query = search.toString().toLowerCase(Locale.ROOT).trim();
        int cx = left;
        for (Category category : Category.values()) {
            List<Module> inColumn = new ArrayList<>();
            for (Module m : modules.inCategory(category)) {
                if (query.isEmpty() || m.getName().toLowerCase(Locale.ROOT).contains(query)) {
                    inColumn.add(m);
                }
            }

            int colH = headH + inColumn.size() * rowH + 4;
            ctx.fill(cx, top, cx + colW, top + colH, 0xF00E0E11);
            ctx.fill(cx, top, cx + colW, top + headH, 0xFF17171A);
            ctx.drawText(this.client.textRenderer, Text.literal(category.name()),
                    cx + 6, top + headH / 2 - 4, 0xFFEDEDEF, true);

            int ry = top + headH + 2;
            for (Module m : inColumn) {
                boolean on    = m.isEnabled();
                boolean hover = mx >= cx && mx <= cx + colW && my >= ry && my <= ry + rowH;
                if (on || hover) {
                    ctx.fill(cx + 2, ry, cx + colW - 2, ry + rowH, on ? 0x33FFFFFF : 0x18FFFFFF);
                }

                if (on) {
                    ctx.fill(cx + 3, ry + 2, cx + 5, ry + rowH - 2, 0xFFFFFFFF);
                }

                ctx.drawText(this.client.textRenderer, Text.literal(m.getName()),
                        cx + 9, ry + rowH / 2 - 4, on ? 0xFFFFFFFF : 0xFF8E8E96, true);
                ry += rowH;
            }

            cx += colW + gap;
        }
    }

    @Override
    public void tick() { finishCloseIfDone(); }

    private void finishCloseIfDone() {
        if (closing && openAnim.isDone()) client.setScreen(parent);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        if (client.world == null) renderPanoramaBackground(ctx, delta);
        if (guiModule().blur.get()) {
            BlurHook.set(guiModule().blurStrength.getFloat() * openAnim.value());
            // ctx.applyBlur() not available in 1.21.5
        } else {
            BlurHook.clear();
        }
    }

    @Override
    public void renderNvg(NVGRenderer nvg, float mouseX, float mouseY, float screenW, float screenH) {
        nvg.setFontMode(guiModule().font.get());
        // No hasFont() gate here. It used to return early, which turned "one
        // font failed to load" into "the entire menu is invisible" -- panels,
        // rows and toggles included, while the dim and the blur still drew
        // through the vanilla path and clicks still landed. That is exactly
        // the bug that was fixed in OverlayRenderer, and this was a second
        // copy of it that the fix missed. NanoVG draws shapes without a font;
        // only text needs one.
        float t = openAnim.value();
        if (t <= 0.002f && closing) return;

        layoutColumns(screenW, screenH);

        nvg.save();
        nvg.alpha(t);
        float scale = 0.97f + 0.03f * t;
        nvg.translate(screenW / 2.0f, screenH / 2.0f);
        nvg.scale(scale);
        nvg.translate(-screenW / 2.0f, -screenH / 2.0f);

        String query = search.toString();
        for (CategoryPanel cp : columns) cp.setFilter(query);

        // columns render left to right; the pane under the cursor is unaffected
        // by order because nothing overlaps in a locked grid
        for (CategoryPanel cp : columns) cp.render(nvg, mouseX, mouseY, screenW, screenH);

        renderTopBar(nvg, mouseX, mouseY, screenW);
        renderHint(nvg, screenW, screenH);

        if (themesOpen) {
            // scrim so the floating pane reads as being above the grid
            nvg.rect(0.0f, 0.0f, screenW, screenH, 0.0f, Colors.withAlpha(0xFF050506, 0.46f));
            themesPanel.render(nvg, mouseX, mouseY, screenW, screenH);
        }

        if (statsOpen) {
            nvg.rect(0.0f, 0.0f, screenW, screenH, 0.0f, Colors.withAlpha(0xFF050506, 0.46f));
            statsPanel.render(nvg, mouseX, mouseY, screenW, screenH);
        }

        configPanel.render(nvg, mouseX, mouseY, screenW, screenH);
        nvg.restore();
        nvgFrames++;
    }

    // ── chrome ────────────────────────────────────────────────────────────────

    /**
     * Where each piece of the bar sits, so hit tests and drawing agree.
     *
     * The row is exactly as wide as what it holds, and there is no plate behind
     * it. Stretched to the window it had a few hundred pixels of empty card
     * between the search and the buttons, which is the one thing a strip of
     * controls should never have — the eye reads that gap as a missing control
     * rather than as spacing.
     */
    private float barW() {
        return MARK_W + MARK_GAP + SEARCH_W + BAR_MID_GAP
                + PILL_W * 3.0f + PILL_GAP * 2.0f;
    }

    private float barX(float sw)      { return (sw - barW()) / 2.0f; }
    private float barMidY()           { return BAR_Y + BAR_H / 2.0f; }
    private float searchX(float sw)   { return barX(sw) + MARK_W + MARK_GAP; }
    private float searchY()           { return barMidY() - SEARCH_H / 2.0f; }
    private float buttonY()           { return barMidY() - PILL_H / 2.0f; }

    /** Buttons sit at the right end, so the row reads Stats, Configs, Themes. */
    private float buttonX(float sw, int index) {
        float first = barX(sw) + barW() - (PILL_W * 3.0f + PILL_GAP * 2.0f);
        return first + index * (PILL_W + PILL_GAP);
    }

    /**
     * Title, search and the three buttons, on one strip.
     *
     * The order left to right is what you reach for in that order: what the
     * client is, what you are looking for, and then the three panes you open
     * occasionally. Putting the buttons hard right keeps them clear of the
     * search field as the window narrows.
     */
    private void renderTopBar(NVGRenderer nvg, float mx, float my, float sw) {
        Theme th = theme();
        float bx = barX(sw);

        // No plate behind the row. A card here was a slab of black across the
        // top of the screen holding three things that already have their own
        // edges — the search field and the buttons are surfaces in their own
        // right, and the mark needs no backing at all.

        // ── the mark ─────────────────────────────────────────────────────────
        int mark = NVGImages.fromResource(LOGO);
        if (mark > 0) {
            float mh = MARK_W * LOGO_ASPECT;
            nvg.image(mark, bx, barMidY() - mh / 2.0f, MARK_W, mh, -1);
        } else {
            // the mark is an asset; if it will not load, the name still has to
            // appear rather than leaving a hole where the brand goes
            nvg.text("KRYPTIC", bx, barMidY(), 15.0f, th.textPrimary());
        }

        // ── search ───────────────────────────────────────────────────────────
        float sx = searchX(sw), sy = searchY();
        boolean focused = searchFocused;
        float lift = focused ? 1.0f : (searchBarHit(mx, my) ? 0.45f : 0.0f);
        Surface.button(nvg, sx, sy, SEARCH_W, SEARCH_H, th, lift);

        float iconCX = sx + 16.0f, iconCY = sy + SEARCH_H / 2.0f - 1.0f;
        nvg.circleOutline(iconCX, iconCY, 4.0f, 1.4f, th.textMuted());
        nvg.line(iconCX + 3.0f, iconCY + 2.9f, iconCX + 5.8f, iconCY + 5.7f, 1.4f, th.textMuted());

        float textX = sx + 30.0f, textY = sy + SEARCH_H / 2.0f;
        if (search.isEmpty() && !focused) {
            nvg.text("Search", textX, textY, 12.5f, th.textDisabled());
        } else {
            float tw = nvg.textTruncated(search.toString(), textX, textY, 12.5f,
                    th.textPrimary(), SEARCH_W - 52.0f);
            if (focused && System.nanoTime() / 400_000_000L % 2L == 0L) {
                nvg.rect(textX + tw + 2.0f, textY - 6.0f, 1.4f, 12.0f, 0.7f, th.accentBright());
            }
        }
        if (!search.isEmpty()) {
            nvg.cross(sx + SEARCH_W - 19.0f, textY - 5.0f, 10.0f, 1.4f, th.textMuted());
        }

        // ── the three panes ──────────────────────────────────────────────────
        boolean stActive = statsHit(mx, my) || statsOpen;
        if (stActive != statsHovered) { statsHovered = stActive; if (stActive) UiSounds.hover(); }
        float x0 = buttonX(sw, 0);
        button(nvg, th, x0, "Stats", stActive);
        // three ascending bars, shortest first, so the icon reads as a chart
        float base = buttonY() + PILL_H / 2.0f + 5.0f;
        for (int i = 0; i < 3; i++) {
            float bh = 3.0f + i * 3.5f;
            nvg.rect(x0 + 13.0f + i * 4.5f, base - bh, 2.5f, bh, 1.2f,
                    stActive ? th.accentBright() : th.accent());
        }

        boolean cfgActive = configHit(mx, my) || configPanel.isOpen();
        if (cfgActive != configHovered) { configHovered = cfgActive; if (cfgActive) UiSounds.hover(); }
        float x1 = buttonX(sw, 1);
        button(nvg, th, x1, "Configs", cfgActive);
        float iconY = buttonY() + PILL_H / 2.0f - 5.0f;
        nvg.rect(x1 + 15.0f, iconY + 2.5f, 9.0f, 7.5f, 2.0f, Colors.withAlpha(th.accent(), 0.45f));
        nvg.rect(x1 + 13.0f, iconY, 9.0f, 7.5f, 2.0f, cfgActive ? th.accentBright() : th.accent());

        boolean thActive = themesHit(mx, my) || themesOpen;
        if (thActive != themesHovered) { themesHovered = thActive; if (thActive) UiSounds.hover(); }
        float x2 = buttonX(sw, 2);
        button(nvg, th, x2, "Themes", thActive);
        float dotX = x2 + 17.0f, dotY = buttonY() + PILL_H / 2.0f;
        nvg.circle(dotX, dotY, 5.0f, th.accent());
        nvg.circleOutline(dotX, dotY, 6.8f, 1.1f,
                Colors.withAlpha(th.accentBright(), thActive ? 0.9f : 0.4f));
    }

    /** One bar button: the surface and its label. The icon is drawn by the caller. */
    private void button(NVGRenderer nvg, Theme th, float x, String label, boolean active) {
        Surface.button(nvg, x, buttonY(), PILL_W, PILL_H, th, active ? 1.0f : 0.0f);
        nvg.text(label, x + 29.0f, buttonY() + PILL_H / 2.0f, 12.5f,
                active ? th.textPrimary() : th.textMuted());
    }

    private void renderHint(NVGRenderer nvg, float sw, float sh) {
        // nvgTextAlign is LEFT|MIDDLE throughout, so centre by hand
        String hint = "ESC  ·  close      ·      " + statusLine();
        float  hw   = nvg.textWidth(hint, HINT_FONT);
        nvg.text(hint, (sw - hw) / 2.0f, sh - HINT_MARGIN, HINT_FONT,
                Colors.withAlpha(theme().textDisabled(), 0.5f));
    }

    /**
     * The right half of the hint line: which config is live, whether this
     * server is bound to one, and whether Discord is connected. All read-only,
     * and each piece drops out silently when its subsystem is unavailable.
     */
    private String statusLine() {
        StringBuilder out = new StringBuilder();
        try {
            ConfigStore store = KrypticClient.configStore();
            if (store != null) {
                int active = store.activeIndex();
                ConfigStore.Slot slot = active >= 0 ? store.slot(active) : null;
                if (slot != null) out.append(slot.name());
            }
            ServerConfigs bindings = KrypticClient.serverConfigs();
            if (bindings != null && bindings.slotForCurrent() >= 0) {
                if (out.length() > 0) out.append("  ·  ");
                out.append("bound");
            }
            DiscordPresenceModule rpc = KrypticClient.modules().discordRpc;
            if (rpc != null && rpc.isEnabled() && rpc.isConnected()) {
                if (out.length() > 0) out.append("  ·  ");
                out.append("discord");
            }
        } catch (Throwable ignored) {
            // the hint line is decoration; never let it break the GUI
        }
        if (out.length() == 0) out.append("layout locked");
        return out.toString();
    }

    // ── hit tests ─────────────────────────────────────────────────────────────

    /** All three buttons share one row, so only the x differs. */
    private boolean buttonHit(float mx, float my, int index) {
        float x = buttonX(OverlayRenderer.uiWidth(), index);
        float y = buttonY();
        return mx >= x && mx <= x + PILL_W && my >= y && my <= y + PILL_H;
    }

    private boolean statsHit(float mx, float my)   { return buttonHit(mx, my, 0); }
    private boolean configHit(float mx, float my)  { return buttonHit(mx, my, 1); }
    private boolean themesHit(float mx, float my)  { return buttonHit(mx, my, 2); }

    private boolean searchBarHit(float mx, float my) {
        float x = searchX(OverlayRenderer.uiWidth()), y = searchY();
        return mx >= x && mx <= x + SEARCH_W && my >= y && my <= y + SEARCH_H;
    }

    private boolean searchClearHit(float mx, float my) {
        float clearX = searchX(OverlayRenderer.uiWidth()) + SEARCH_W - 26.0f;
        float midY   = searchY() + SEARCH_H / 2.0f;
        return mx >= clearX && mx <= clearX + 20.0f && my >= midY - 9.0f && my <= midY + 9.0f;
    }

    private float uiX(double v) { return OverlayRenderer.guiToUi(v); }
    private float uiY(double v) { return OverlayRenderer.guiToUi(v); }
    private Theme theme()        { return KrypticClient.themes().current(); }

    private void closeThemes() { themesOpen = false; UiSounds.guiClose(); }
    private void closeStats()  { statsOpen  = false; UiSounds.guiClose(); }

    // ── Screen API: 1.21.11 passes input as records ───────────────────────────
    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        double px = click.x(), py = click.y();
        int btn = click.button();
        float mx = uiX(px), my = uiY(py);
        NVGRenderer nvg = NVGRenderer.get();
        float sh  = OverlayRenderer.uiHeight();

        if (configPanel.isOpen()) { configPanel.mouseClicked(mx, my, btn); return true; }

        if (configHit(mx, my)) { configPanel.open(); UiSounds.guiOpen(); return true; }
        if (themesHit(mx, my)) {
            themesOpen = !themesOpen;
            if (themesOpen) { statsOpen = false; UiSounds.guiOpen(); } else UiSounds.guiClose();
            return true;
        }
        if (statsHit(mx, my)) {
            statsOpen = !statsOpen;
            if (statsOpen) { themesOpen = false; UiSounds.guiOpen(); } else UiSounds.guiClose();
            return true;
        }

        if (statsOpen) {
            if (statsPanel.headerHit(mx, my)) {
                if (btn == 0) { statsPanel.toggleCollapsed(); UiSounds.panelCollapse(); }
                else          closeStats();
                return true;
            }
            if (statsPanel.bodyHit(nvg, mx, my, sh)) {
                pressedContentPanel = statsPanel;
                statsPanel.mouseClicked(mx, my, btn);
                return true;
            }
            closeStats();
            return true;
        }

        if (themesOpen) {
            if (themesPanel.headerHit(mx, my)) {
                if (btn == 0) { themesPanel.toggleCollapsed(); UiSounds.panelCollapse(); }
                else          closeThemes();
                return true;
            }
            if (themesPanel.bodyHit(nvg, mx, my, sh)) {
                pressedContentPanel = themesPanel;
                themesPanel.mouseClicked(mx, my, btn);
                return true;
            }
            closeThemes();
            return true;
        }

        if (searchBarHit(mx, my)) {
            if (!search.isEmpty() && searchClearHit(mx, my)) search.setLength(0);
            else searchFocused = true;
            UiSounds.select(); return true;
        }
        searchFocused = false;

        for (CategoryPanel p : columns) {
            if (p.headerHit(mx, my)) {
                // columns are locked in place — a header click only collapses
                p.toggleCollapsed();
                UiSounds.panelCollapse();
                return true;
            }
            if (p.bodyHit(nvg, mx, my, sh)) {
                pressedContentPanel = p;
                p.mouseClicked(mx, my, btn);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (configPanel.isOpen()) return true;
        float mx = uiX(click.x()), my = uiY(click.y());
        if (pressedContentPanel != null) pressedContentPanel.mouseDragged(mx, my);
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (configPanel.isOpen()) return true;
        if (pressedContentPanel != null) { pressedContentPanel.mouseReleased(); pressedContentPanel = null; }
        return true;
    }

    @Override
    public boolean mouseScrolled(double px, double py, double hDelta, double vDelta) {
        if (configPanel.isOpen()) return true;
        float mx = uiX(px), my = uiY(py);
        NVGRenderer nvg = NVGRenderer.get();
        float sh = OverlayRenderer.uiHeight();
        if (themesOpen) {
            themesPanel.onScroll(vDelta);
            return true;
        }
        if (statsOpen) {
            statsPanel.onScroll(vDelta);
            return true;
        }
        for (CategoryPanel p : columns) {
            if (p.bodyHit(nvg, mx, my, sh) || p.headerHit(mx, my)) { p.onScroll(vDelta); return true; }
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key(), mods = input.modifiers();
        if (configPanel.isOpen()) { configPanel.keyPressed(key); return true; }
        if (themesOpen && themesPanel.isListening()) { themesPanel.keyPressed(key); return true; }
        for (CategoryPanel p : columns) { if (p.isListening()) { p.keyPressed(key); return true; } }
        if (themesOpen && key == 256) { closeThemes(); return true; }
        // Ctrl+F, or Cmd+F, since Ctrl is not the search idiom on macOS
        boolean commandHeld = (mods & GLFW.GLFW_MOD_CONTROL) != 0 || (mods & GLFW.GLFW_MOD_SUPER) != 0;
        if (!searchFocused && (key == GLFW.GLFW_KEY_SLASH || (key == GLFW.GLFW_KEY_F && commandHeld))) {
            searchFocused = true;
            return true;
        }
        if (searchFocused) {
            switch (key) {
                case 256 -> { search.setLength(0); searchFocused = false; }
                case 257, 335 -> searchFocused = false;
                case 259 -> { if (!search.isEmpty()) search.deleteCharAt(search.length() - 1); }
            }
            return true;
        }
        if (key == 256 && statsOpen)  { closeStats();  return true; }
        if (key == 256 && themesOpen) { closeThemes(); return true; }
        if (key == 256 || guiModule().getKeybind().matches(key)) { close(); return true; }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        char c = (char) input.codepoint();
        if (configPanel.isOpen()) { configPanel.charTyped(c); return true; }
        if (themesOpen && themesPanel.isListening()) { themesPanel.charTyped(c); return true; }
        for (CategoryPanel p : columns) { if (p.isListening()) { p.charTyped(c); return true; } }
        if (searchFocused && input.isValidChar()) {
            if (search.length() < 40) search.append(input.asString());
            return true;
        }
        return super.charTyped(input);
    }

    public void openSearch(String text) { search.setLength(0); search.append(text); searchFocused = true; }
    public ConfigPanel configPanel()    { return configPanel; }

    public void openBlockPicker(BlockListSetting setting) {
        BlockGridModel model = new BlockGridModel(setting, () -> {
            BlockEspModule be = KrypticClient.modules().blockEsp;
            return be != null ? be.lineColor.get() : -16711736;
        }, "Pick Block");
        client.setScreen(new IconPickerScreen(this, model, KrypticClient.themes()));
    }
    public void openIconPicker(IconListSetting setting) {
        client.setScreen(new IconPickerScreen(this, new IconListGridModel(setting), KrypticClient.themes()));
    }
}
