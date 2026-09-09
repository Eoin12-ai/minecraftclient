package dev.kryptic.gui;

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
import dev.kryptic.gui.panel.ThemesPanel;
import dev.kryptic.gui.picker.BlockGridModel;
import dev.kryptic.gui.picker.IconListGridModel;
import dev.kryptic.module.Category;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.module.render.BlockEspModule;
import dev.kryptic.render.BlurHook;
import dev.kryptic.render.Glass;
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
 * ClickGuiScreen — locked five-column liquid-glass menu.
 *
 * Layout: one full-height column per {@link Category}, five of them, centred as
 * a group and recomputed every frame from the UI size. Columns cannot be
 * dragged or reordered; clicking a header collapses or expands that column and
 * long lists scroll inside their own pane.
 *
 * Chrome: a glass search capsule at the top, and a bottom bar with two glass
 * pills — Configs and Themes. Themes is a floating pane centred over the
 * columns rather than a sixth column, which is what keeps the grid at five.
 *
 * Every surface comes from {@link Glass} so the whole menu is one material.
 */
public class ClickGuiScreen extends Screen implements NvgDrawable {

    private static final ClickGuiState STATE = new ClickGuiState();

    private static final float SEARCH_W      = 320.0f;
    private static final float SEARCH_H      = 34.0f;
    private static final float SEARCH_Y      = 16.0f;

    private static final float PILL_W        = 128.0f;
    private static final float PILL_H        = 30.0f;
    private static final float PILL_GAP      = 10.0f;
    private static final float PILL_MARGIN_B = 14.0f;

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
        finishCloseIfDone();
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
        if (!nvg.hasFont()) return;
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

        renderSearchBar(nvg, mouseX, mouseY, screenW);
        renderPillBar(nvg, mouseX, mouseY, screenW, screenH);
        renderHint(nvg, screenW, screenH);

        if (themesOpen) {
            // scrim so the floating pane reads as being above the grid
            nvg.rect(0.0f, 0.0f, screenW, screenH, 0.0f, Colors.withAlpha(0xFF05060A, 0.42f));
            themesPanel.render(nvg, mouseX, mouseY, screenW, screenH);
        }

        if (statsOpen) {
            nvg.rect(0.0f, 0.0f, screenW, screenH, 0.0f, Colors.withAlpha(0xFF05060A, 0.42f));
            statsPanel.render(nvg, mouseX, mouseY, screenW, screenH);
        }

        configPanel.render(nvg, mouseX, mouseY, screenW, screenH);
        nvg.restore();
    }

    // ── chrome ────────────────────────────────────────────────────────────────

    private float searchX(float sw) { return (sw - SEARCH_W) / 2.0f; }

    private void renderSearchBar(NVGRenderer nvg, float mx, float my, float sw) {
        Theme th   = theme();
        float barX = searchX(sw);
        float barY = SEARCH_Y;
        boolean focused = searchFocused;
        boolean hover   = searchBarHit(mx, my);
        float lift = focused ? 1.0f : (hover ? 0.45f : 0.0f);

        Glass.pill(nvg, barX, barY, SEARCH_W, SEARCH_H, th, lift);
        Glass.caustic(nvg, barX + 2.0f, barY + 2.0f, SEARCH_W - 4.0f, SEARCH_H - 4.0f, 11.0f, 1.0f);

        float iconCX = barX + 19.0f, iconCY = barY + SEARCH_H / 2.0f - 1.0f;
        nvg.circleOutline(iconCX, iconCY, 4.5f, 1.5f, th.textMuted());
        nvg.line(iconCX + 3.3f, iconCY + 3.2f, iconCX + 6.4f, iconCY + 6.3f, 1.5f, th.textMuted());

        float textX = barX + 37.0f, textY = barY + SEARCH_H / 2.0f;
        if (search.isEmpty() && !focused) {
            nvg.text("Search modules...", textX, textY, 13.0f, th.textDisabled());
        } else {
            float textW = nvg.text(search.toString(), textX, textY, 13.0f, th.textPrimary());
            if (focused && System.nanoTime() / 400_000_000L % 2L == 0L)
                nvg.rect(textX + textW + 2.0f, textY - 7.0f, 1.4f, 14.0f, 0.7f, th.accentBright());
        }
        if (!search.isEmpty())
            nvg.cross(barX + SEARCH_W - 22.0f, textY - 5.5f, 11.0f, 1.5f, th.textMuted());
    }

    private float pillBarX(float sw) { return (sw - (PILL_W * 3.0f + PILL_GAP * 2.0f)) / 2.0f; }
    private float pillBarY(float sh) { return sh - PILL_H - PILL_MARGIN_B; }

    private void renderPillBar(NVGRenderer nvg, float mx, float my, float sw, float sh) {
        Theme th = theme();
        float x0 = pillBarX(sw);
        float y  = pillBarY(sh);

        boolean cfgActive = configHit(mx, my) || configPanel.isOpen();
        if (cfgActive != configHovered) { configHovered = cfgActive; if (cfgActive) UiSounds.hover(); }
        Glass.pill(nvg, x0, y, PILL_W, PILL_H, th, cfgActive ? 1.0f : 0.0f);
        float iconX = x0 + 16.0f, iconY = y + PILL_H / 2.0f - 6.0f;
        nvg.rect(iconX + 3.0f, iconY + 3.0f, 11.0f, 9.0f, 2.5f, Colors.withAlpha(th.accent(), 0.45f));
        nvg.rect(iconX, iconY, 11.0f, 9.0f, 2.5f, cfgActive ? th.accentBright() : th.accent());
        nvg.text("Configs", x0 + 36.0f, y + PILL_H / 2.0f, 13.5f,
                cfgActive ? th.textPrimary() : th.textMuted());

        float x1 = x0 + PILL_W + PILL_GAP;
        boolean thActive = themesHit(mx, my) || themesOpen;
        if (thActive != themesHovered) { themesHovered = thActive; if (thActive) UiSounds.hover(); }
        Glass.pill(nvg, x1, y, PILL_W, PILL_H, th, thActive ? 1.0f : 0.0f);
        float dotX = x1 + 21.0f, dotY = y + PILL_H / 2.0f;
        nvg.circle(dotX, dotY, 6.0f, th.accent());
        nvg.circleOutline(dotX, dotY, 8.0f, 1.2f,
                Colors.withAlpha(th.accentBright(), thActive ? 0.9f : 0.45f));
        nvg.text("Themes", x1 + 36.0f, y + PILL_H / 2.0f, 13.5f,
                thActive ? th.textPrimary() : th.textMuted());

        float x2 = x1 + PILL_W + PILL_GAP;
        boolean stActive = statsHit(mx, my) || statsOpen;
        if (stActive != statsHovered) { statsHovered = stActive; if (stActive) UiSounds.hover(); }
        Glass.pill(nvg, x2, y, PILL_W, PILL_H, th, stActive ? 1.0f : 0.0f);
        // three ascending bars, the shortest first, so the icon reads as a chart
        float barX = x2 + 16.0f, barBase = y + PILL_H / 2.0f + 6.0f;
        for (int i = 0; i < 3; i++) {
            float barH = 4.0f + i * 4.0f;
            nvg.rect(barX + i * 5.0f, barBase - barH, 3.0f, barH, 1.5f,
                    stActive ? th.accentBright() : th.accent());
        }
        nvg.text("Stats", x2 + 36.0f, y + PILL_H / 2.0f, 13.5f,
                stActive ? th.textPrimary() : th.textMuted());
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

    private boolean configHit(float mx, float my) {
        float x0 = pillBarX(OverlayRenderer.uiWidth());
        float y  = pillBarY(OverlayRenderer.uiHeight());
        return mx >= x0 && mx <= x0 + PILL_W && my >= y && my <= y + PILL_H;
    }

    private boolean themesHit(float mx, float my) {
        float x1 = pillBarX(OverlayRenderer.uiWidth()) + PILL_W + PILL_GAP;
        float y  = pillBarY(OverlayRenderer.uiHeight());
        return mx >= x1 && mx <= x1 + PILL_W && my >= y && my <= y + PILL_H;
    }

    private boolean statsHit(float mx, float my) {
        float x2 = pillBarX(OverlayRenderer.uiWidth()) + (PILL_W + PILL_GAP) * 2.0f;
        float y  = pillBarY(OverlayRenderer.uiHeight());
        return mx >= x2 && mx <= x2 + PILL_W && my >= y && my <= y + PILL_H;
    }

    private boolean searchBarHit(float mx, float my) {
        float barX = searchX(OverlayRenderer.uiWidth());
        return mx >= barX && mx <= barX + SEARCH_W && my >= SEARCH_Y && my <= SEARCH_Y + SEARCH_H;
    }

    private boolean searchClearHit(float mx, float my) {
        float barX   = searchX(OverlayRenderer.uiWidth());
        float clearX = barX + SEARCH_W - 26.0f;
        float midY   = SEARCH_Y + SEARCH_H / 2.0f;
        return mx >= clearX && mx <= clearX + 20.0f && my >= midY - 10.0f && my <= midY + 10.0f;
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
