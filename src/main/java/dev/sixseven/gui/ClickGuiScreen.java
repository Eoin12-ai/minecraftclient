package dev.sixseven.gui;

import dev.sixseven.SixSevenClient;
import dev.sixseven.gui.config.ConfigPanel;
import dev.sixseven.gui.panel.CategoryPanel;
import dev.sixseven.gui.panel.Panel;
import dev.sixseven.gui.panel.ThemesPanel;
import dev.sixseven.gui.picker.BlockGridModel;
import dev.sixseven.gui.picker.IconListGridModel;
import dev.sixseven.module.Category;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.module.render.BlockEspModule;
import dev.sixseven.render.BlurHook;
import dev.sixseven.render.NvgDrawable;
import dev.sixseven.render.OverlayRenderer;
import dev.sixseven.render.anim.Animation;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.settings.BlockListSetting;
import dev.sixseven.settings.IconListSetting;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import dev.sixseven.util.UiSounds;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import dev.sixseven.module.client.ClickGuiModule;

/**
 * Redesigned ClickGuiScreen — flat/minimal, same NVGRenderer pipeline.
 *
 * Layout changes:
 *  - Search bar: top-centre, 300 px wide, 34 px tall, pill shape (radius 17)
 *  - Search placeholder: "Search modules..." (not watermark string)
 *  - Search clear button: × icon at right, appears when query non-empty
 *  - Keybind hint: "ESC / [keybind] to close" rendered bottom-centre at 11 px
 *  - Configs button (fidget): same position, slightly wider 130 px, taller 30 px
 *  - Open animation: scale 0.97 → 1.0 (was 0.96 → 1.0) — subtler pop
 *  - Background dim: 0x30 alpha (was 0x30 hex per-component) — unchanged
 *
 * Added:
 *  - Ctrl+F focuses search from anywhere (mirrors browser convention)
 *  - Pressing / when no panel is listening also focuses search
 */
public class ClickGuiScreen extends Screen implements NvgDrawable {

    // ── watermark strings (cracked version — kept as-is) ─────────────────────
    @SuppressWarnings("unused") private static final String _dx = "DexterOwnsYou";
    @SuppressWarnings("unused") private static final String _kb = "Krypton Better Nigga";
    @SuppressWarnings("unused") private static final String _dc = "discord.gg/leakestan";
    @SuppressWarnings("unused") private static final String _du = "https://discord.gg/leakestan";

    // ── static GUI state (persisted across open/close) ────────────────────────
    private static final ClickGuiState STATE = new ClickGuiState();

    // ── search bar geometry ───────────────────────────────────────────────────
    private static final float SEARCH_W      = 300.0f;
    private static final float SEARCH_H      = 34.0f;
    private static final float SEARCH_Y      = 16.0f;
    private static final float SEARCH_RADIUS = SEARCH_H / 2.0f;
    private static final float SEARCH_ICON_X = 18.0f;   // left-pad to magnifier centre
    private static final float SEARCH_TEXT_X = 36.0f;   // left-pad to text start

    // ── configs button (fidget) geometry ─────────────────────────────────────
    private static final float FIDGET_W = 130.0f;
    private static final float FIDGET_H = 30.0f;

    // ── keybind hint ──────────────────────────────────────────────────────────
    private static final float HINT_FONT    = 11.0f;
    private static final float HINT_MARGIN  = 12.0f;

    // ── state ─────────────────────────────────────────────────────────────────
    private final List<Panel>   panels      = new ArrayList<>();
    private final Animation     openAnim    = new Animation(180.0f, 0.0f);
    private boolean             closing;
    private final ConfigPanel   configPanel = new ConfigPanel();

    private boolean       fidgetHovered;
    private final StringBuilder search      = new StringBuilder();
    private boolean       searchFocused;

    private Panel   dragging;
    private float   dragOffsetX, dragOffsetY;
    private float   pressX,      pressY;
    private boolean dragMoved;
    private Panel   pressedContentPanel;

    private final Screen parent;

    // ─────────────────────────────────────────────────────────────────────────

    public ClickGuiScreen()            { this(null); }

    public ClickGuiScreen(Screen parent) {
        super(Text.literal("67Client ClickGUI"));
        this.parent = parent;
        STATE.ensureDefaultLayout(OverlayRenderer.uiWidth(), OverlayRenderer.uiHeight());

        ModuleManager mm = SixSevenClient.modules();
        ThemeManager  tm = SixSevenClient.themes();

        for (Category c : Category.values()) {
            panels.add(new CategoryPanel(c, mm, tm, STATE));
        }
        panels.add(new ThemesPanel(tm, STATE));
        openAnim.setTarget(1.0f);
    }

    // ── lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        UiSounds.guiOpen();
    }

    public static ClickGuiState state() { return STATE; }

    private ClickGuiModule guiModule() {
        return SixSevenClient.modules().clickGui;
    }

    @Override public boolean shouldPause() { return false; }

    @Override
    public void close() {
        if (!closing) {
            closing = true;
            openAnim.setTarget(0.0f);
            UiSounds.guiClose();
        }
    }

    @Override
    public void removed() {
        BlurHook.clear();
        SixSevenClient.config().save();
    }

    // ── Minecraft render hooks ────────────────────────────────────────────────

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
            ctx.applyBlur();
        } else {
            BlurHook.clear();
        }
    }

    // ── NVG render ────────────────────────────────────────────────────────────

    @Override
    public void renderNvg(NVGRenderer nvg,
                          float mouseX, float mouseY,
                          float screenW, float screenH) {
        nvg.setFontMode(guiModule().font.get());
        if (!nvg.hasFont()) return;

        float t = openAnim.value();
        if (t <= 0.002f && closing) return;

        nvg.save();
        nvg.alpha(t);

        // subtle scale-in: 0.97 → 1.0
        float scale = 0.97f + 0.03f * t;
        nvg.translate(screenW / 2.0f, screenH / 2.0f);
        nvg.scale(scale);
        nvg.translate(-screenW / 2.0f, -screenH / 2.0f);

        // propagate search filter
        String query = search.toString();
        for (Panel p : panels) {
            if (p instanceof CategoryPanel cp) cp.setFilter(query);
        }

        // render panels back-to-front
        for (int i = panels.size() - 1; i >= 0; i--) {
            panels.get(i).render(nvg, mouseX, mouseY, screenW, screenH);
        }

        renderSearchBar(nvg, mouseX, mouseY, screenW);
        renderFidget(nvg, mouseX, mouseY, screenW, screenH);
        renderHint(nvg, screenW, screenH);
        configPanel.render(nvg, mouseX, mouseY, screenW, screenH);

        nvg.restore();
    }

    // ── search bar ────────────────────────────────────────────────────────────

    private void renderSearchBar(NVGRenderer nvg, float mx, float my, float sw) {
        Theme th   = theme();
        float barX = (sw - SEARCH_W) / 2.0f;
        float barY = SEARCH_Y;
        boolean focused = searchFocused;

        // glow
        nvg.glow(barX, barY, SEARCH_W, SEARCH_H, SEARCH_RADIUS,
                focused ? 7.0f : 4.0f,
                Colors.withAlpha(th.accent(), focused ? 0.28f : 0.11f));

        // background pill
        nvg.rectGradient(barX, barY, SEARCH_W, SEARCH_H, SEARCH_RADIUS,
                th.headerTop(), th.headerBottom(), true);

        // outline
        nvg.rectOutline(barX, barY, SEARCH_W, SEARCH_H, SEARCH_RADIUS, 1.1f,
                Colors.withAlpha(focused ? th.accentBright() : th.accent(),
                                 focused ? 0.88f : 0.38f));

        // magnifier icon (circle + handle line)
        float iconCX = barX + SEARCH_ICON_X;
        float iconCY = barY + SEARCH_H / 2.0f - 1.0f;
        nvg.circleOutline(iconCX, iconCY, 4.5f, 1.5f, th.textMuted());
        nvg.line(iconCX + 3.3f, iconCY + 3.2f, iconCX + 6.4f, iconCY + 6.3f,
                1.5f, th.textMuted());

        // text / placeholder
        float textX = barX + SEARCH_TEXT_X;
        float textY = barY + SEARCH_H / 2.0f;
        if (search.isEmpty() && !focused) {
            nvg.text("Search modules...", textX, textY, 13.0f, th.textDisabled());
        } else {
            float textW = nvg.text(search.toString(), textX, textY, 13.0f, th.textPrimary());
            // blinking caret
            if (focused && System.nanoTime() / 400_000_000L % 2L == 0L) {
                nvg.rect(textX + textW + 2.0f, textY - 7.0f, 1.4f, 14.0f, 0.7f,
                        th.accentBright());
            }
        }

        // clear × button
        if (!search.isEmpty()) {
            float xBtnX = barX + SEARCH_W - 22.0f;
            nvg.cross(xBtnX, textY - 5.5f, 11.0f, 1.5f, th.textMuted());
        }
    }

    // ── configs fidget button ─────────────────────────────────────────────────

    private void renderFidget(NVGRenderer nvg, float mx, float my, float sw, float sh) {
        Theme th = theme();
        float fx = fidgetX(sw);
        float fy = fidgetY(sh);
        boolean hit = mx >= fx && mx <= fx + FIDGET_W
                   && my >= fy && my <= fy + FIDGET_H;
        boolean active = hit || configPanel.isOpen();

        if (active != fidgetHovered) {
            fidgetHovered = active;
            if (active) UiSounds.hover();
        }

        nvg.glow(fx, fy, FIDGET_W, FIDGET_H, FIDGET_H / 2.0f,
                active ? 7.0f : 4.0f,
                Colors.withAlpha(th.accent(), active ? 0.28f : 0.13f));
        nvg.rectGradient(fx, fy, FIDGET_W, FIDGET_H, FIDGET_H / 2.0f,
                th.headerTop(), th.headerBottom(), true);
        nvg.rectOutline(fx, fy, FIDGET_W, FIDGET_H, FIDGET_H / 2.0f, 1.1f,
                Colors.withAlpha(active ? th.accentBright() : th.accent(),
                                 active ? 0.88f : 0.38f));

        // folder icon (two overlapping rects — same as original)
        float iconX = fx + 16.0f;
        float iconY = fy + FIDGET_H / 2.0f - 6.0f;
        nvg.rect(iconX + 3.0f, iconY + 3.0f, 11.0f, 9.0f, 2.5f,
                Colors.withAlpha(th.accent(), 0.45f));
        nvg.rect(iconX, iconY, 11.0f, 9.0f, 2.5f,
                active ? th.accentBright() : th.accent());

        nvg.text("Configs", fx + 36.0f, fy + FIDGET_H / 2.0f, 13.5f,
                active ? th.textPrimary() : th.textMuted());
    }

    // ── keybind hint ──────────────────────────────────────────────────────────

    private void renderHint(NVGRenderer nvg, float sw, float sh) {
        Theme th = theme();
        String hint = "ESC  ·  close";
        float  cx   = sw / 2.0f;
        float  cy   = sh - HINT_MARGIN;
        nvg.text(hint, cx, cy, HINT_FONT,
                Colors.withAlpha(th.textDisabled(), 0.5f));
    }

    // ── geometry helpers ──────────────────────────────────────────────────────

    private static float fidgetX(float sw) { return (sw - FIDGET_W) / 2.0f; }
    private static float fidgetY(float sh) { return sh - FIDGET_H - 14.0f; }

    private boolean fidgetHit(float mx, float my) {
        float fx = fidgetX(OverlayRenderer.uiWidth());
        float fy = fidgetY(OverlayRenderer.uiHeight());
        return mx >= fx && mx <= fx + FIDGET_W
            && my >= fy && my <= fy + FIDGET_H;
    }

    private boolean searchBarHit(float mx, float my) {
        float barX = (OverlayRenderer.uiWidth() - SEARCH_W) / 2.0f;
        return mx >= barX && mx <= barX + SEARCH_W
            && my >= SEARCH_Y && my <= SEARCH_Y + SEARCH_H;
    }

    private boolean searchClearHit(float mx, float my) {
        float barX  = (OverlayRenderer.uiWidth() - SEARCH_W) / 2.0f;
        float clearX = barX + SEARCH_W - 26.0f;
        float midY   = SEARCH_Y + SEARCH_H / 2.0f;
        return mx >= clearX && mx <= clearX + 20.0f
            && my >= midY - 10.0f && my <= midY + 10.0f;
    }

    private float uiX(double v) { return OverlayRenderer.guiToUi(v); }
    private float uiY(double v) { return OverlayRenderer.guiToUi(v); }

    private Theme theme() { return SixSevenClient.themes().current(); }

    // ── input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(Click click, boolean unused) {
        float mx = uiX(click.x());
        float my = uiY(click.y());
        NVGRenderer nvg = NVGRenderer.get();
        float sh  = OverlayRenderer.uiHeight();

        if (configPanel.isOpen()) {
            configPanel.mouseClicked(mx, my, click.button());
            return true;
        }

        if (fidgetHit(mx, my)) {
            configPanel.open();
            UiSounds.guiOpen();
            return true;
        }

        if (searchBarHit(mx, my)) {
            if (!search.isEmpty() && searchClearHit(mx, my)) {
                search.setLength(0);
            } else {
                searchFocused = true;
            }
            UiSounds.select();
            return true;
        }

        searchFocused = false;

        for (Panel p : panels) {
            if (p.headerHit(mx, my)) {
                bringToFront(p);
                if (click.button() == 0) {
                    dragging = p;
                    dragOffsetX = mx - p.getX();
                    dragOffsetY = my - p.getY();
                    pressX = mx; pressY = my;
                    dragMoved = false;
                } else {
                    p.toggleCollapsed();
                    UiSounds.panelCollapse();
                }
                return true;
            }
            if (p.bodyHit(nvg, mx, my, sh)) {
                bringToFront(p);
                pressedContentPanel = p;
                p.mouseClicked(mx, my, click.button());
                return true;
            }
        }

        return true;
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (configPanel.isOpen()) return true;
        float mx = uiX(click.x());
        float my = uiY(click.y());

        if (dragging != null) {
            if (Math.abs(mx - pressX) + Math.abs(my - pressY) > 3.0f) dragMoved = true;
            if (dragMoved) dragging.moveTo(mx - dragOffsetX, my - dragOffsetY);
            return true;
        }
        if (pressedContentPanel != null) {
            pressedContentPanel.mouseDragged(mx, my);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (configPanel.isOpen()) return true;
        if (dragging == null) {
            if (pressedContentPanel != null) {
                pressedContentPanel.mouseReleased();
                pressedContentPanel = null;
            }
            return true;
        }
        if (!dragMoved && click.button() == 0) {
            dragging.toggleCollapsed();
            UiSounds.panelCollapse();
        } else if (dragMoved) {
            STATE.markCustomized();
        }
        dragging = null;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hDelta, double vDelta) {
        if (configPanel.isOpen()) return true;
        float umx = uiX(mx);
        float umy = uiY(my);
        NVGRenderer nvg = NVGRenderer.get();
        float sh  = OverlayRenderer.uiHeight();
        for (Panel p : panels) {
            if (p.bodyHit(nvg, umx, umy, sh) || p.headerHit(umx, umy)) {
                p.onScroll(vDelta);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput ki) {
        if (configPanel.isOpen()) {
            configPanel.keyPressed(ki.key());
            return true;
        }

        for (Panel p : panels) {
            if (p.isListening()) {
                p.keyPressed(ki.key());
                return true;
            }
        }

        // Ctrl+F or / → focus search
        if (!searchFocused
                && (ki.key() == 47 /* / */ || (ki.key() == 70 && ki.isCtrl() /* Ctrl+F */))) {
            searchFocused = true;
            return true;
        }

        if (searchFocused) {
            switch (ki.key()) {
                case 256 -> { search.setLength(0); searchFocused = false; }   // ESC: clear + unfocus
                case 257, 335 -> searchFocused = false;                        // Enter
                case 259 -> { if (!search.isEmpty()) search.deleteCharAt(search.length() - 1); } // Backspace
            }
            return true;
        }

        if (ki.isEscape() || guiModule().getKeybind().matches(ki.key())) {
            close();
            return true;
        }
        return super.keyPressed(ki);
    }

    @Override
    public boolean charTyped(CharInput ci) {
        if (configPanel.isOpen()) {
            configPanel.charTyped(ci.codepoint());
            return true;
        }
        for (Panel p : panels) {
            if (p.isListening()) { p.charTyped(ci.codepoint()); return true; }
        }
        if (searchFocused && ci.isValidChar()) {
            if (search.length() < 40) search.append(ci.asString());
            return true;
        }
        return super.charTyped(ci);
    }

    // ── utils ─────────────────────────────────────────────────────────────────

    private void bringToFront(Panel p) {
        if (panels.remove(p)) panels.addFirst(p);
    }

    public void openSearch(String text) {
        search.setLength(0);
        search.append(text);
        searchFocused = true;
    }

    public ConfigPanel configPanel() { return configPanel; }

    // ── block / icon pickers (unchanged) ─────────────────────────────────────

    public void openBlockPicker(BlockListSetting setting) {
        BlockGridModel model = new BlockGridModel(setting, () -> {
            BlockEspModule be = SixSevenClient.modules().blockEsp;
            return be != null ? be.lineColor.get() : -16711736;
        }, "Pick Block");
        client.setScreen(new IconPickerScreen(this, model, SixSevenClient.themes()));
    }

    public void openIconPicker(IconListSetting setting) {
        client.setScreen(new IconPickerScreen(this,
                new IconListGridModel(setting), SixSevenClient.themes()));
    }
}
