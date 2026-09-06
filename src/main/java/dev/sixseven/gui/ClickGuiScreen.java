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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import dev.sixseven.module.client.ClickGuiModule;

public class ClickGuiScreen extends Screen implements NvgDrawable {

    @SuppressWarnings("unused") private static final String _dx = "DexterOwnsYou";
    @SuppressWarnings("unused") private static final String _kb = "Krypton Better Nigga";
    @SuppressWarnings("unused") private static final String _dc = "discord.gg/leakestan";
    @SuppressWarnings("unused") private static final String _du = "https://discord.gg/leakestan";

    private static final ClickGuiState STATE = new ClickGuiState();

    private static final float SEARCH_W      = 300.0f;
    private static final float SEARCH_H      = 34.0f;
    private static final float SEARCH_Y      = 16.0f;
    private static final float SEARCH_RADIUS = SEARCH_H / 2.0f;
    private static final float FIDGET_W      = 130.0f;
    private static final float FIDGET_H      = 30.0f;
    private static final float HINT_FONT     = 11.0f;
    private static final float HINT_MARGIN   = 12.0f;

    private final List<Panel>    panels      = new ArrayList<>();
    private final Animation      openAnim    = new Animation(180.0f, 0.0f);
    private boolean              closing;
    private final ConfigPanel    configPanel = new ConfigPanel();
    private boolean              fidgetHovered;
    private final StringBuilder  search      = new StringBuilder();
    private boolean              searchFocused;
    private Panel                dragging;
    private float                dragOffsetX, dragOffsetY;
    private float                pressX,      pressY;
    private boolean              dragMoved;
    private Panel                pressedContentPanel;
    private final Screen         parent;

    public ClickGuiScreen()              { this(null); }

    public ClickGuiScreen(Screen parent) {
        super(Text.literal("67Client ClickGUI"));
        this.parent = parent;
        STATE.ensureDefaultLayout(OverlayRenderer.uiWidth(), OverlayRenderer.uiHeight());
        ModuleManager mm = SixSevenClient.modules();
        ThemeManager  tm = SixSevenClient.themes();
        for (Category c : Category.values()) panels.add(new CategoryPanel(c, mm, tm, STATE));
        panels.add(new ThemesPanel(tm, STATE));
        openAnim.setTarget(1.0f);
    }

    @Override public void onDisplayed() { super.onDisplayed(); UiSounds.guiOpen(); }
    public static ClickGuiState state()     { return STATE; }
    private ClickGuiModule guiModule()      { return SixSevenClient.modules().clickGui; }
    @Override public boolean shouldPause()  { return false; }

    @Override
    public void close() {
        if (!closing) { closing = true; openAnim.setTarget(0.0f); UiSounds.guiClose(); }
    }

    @Override
    public void removed() { BlurHook.clear(); SixSevenClient.config().save(); }

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
        nvg.save();
        nvg.alpha(t);
        float scale = 0.97f + 0.03f * t;
        nvg.translate(screenW / 2.0f, screenH / 2.0f);
        nvg.scale(scale);
        nvg.translate(-screenW / 2.0f, -screenH / 2.0f);
        String query = search.toString();
        for (Panel p : panels) if (p instanceof CategoryPanel cp) cp.setFilter(query);
        for (int i = panels.size() - 1; i >= 0; i--) panels.get(i).render(nvg, mouseX, mouseY, screenW, screenH);
        renderSearchBar(nvg, mouseX, mouseY, screenW);
        renderFidget(nvg, mouseX, mouseY, screenW, screenH);
        renderHint(nvg, screenW, screenH);
        configPanel.render(nvg, mouseX, mouseY, screenW, screenH);
        nvg.restore();
    }

    private void renderSearchBar(NVGRenderer nvg, float mx, float my, float sw) {
        Theme th   = theme();
        float barX = (sw - SEARCH_W) / 2.0f;
        float barY = SEARCH_Y;
        boolean focused = searchFocused;
        nvg.glow(barX, barY, SEARCH_W, SEARCH_H, SEARCH_RADIUS, focused ? 7.0f : 4.0f,
                Colors.withAlpha(th.accent(), focused ? 0.28f : 0.11f));
        nvg.rectGradient(barX, barY, SEARCH_W, SEARCH_H, SEARCH_RADIUS,
                th.headerTop(), th.headerBottom(), true);
        nvg.rectOutline(barX, barY, SEARCH_W, SEARCH_H, SEARCH_RADIUS, 1.1f,
                Colors.withAlpha(focused ? th.accentBright() : th.accent(), focused ? 0.88f : 0.38f));
        float iconCX = barX + 18.0f, iconCY = barY + SEARCH_H / 2.0f - 1.0f;
        nvg.circleOutline(iconCX, iconCY, 4.5f, 1.5f, th.textMuted());
        nvg.line(iconCX + 3.3f, iconCY + 3.2f, iconCX + 6.4f, iconCY + 6.3f, 1.5f, th.textMuted());
        float textX = barX + 36.0f, textY = barY + SEARCH_H / 2.0f;
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

    private void renderFidget(NVGRenderer nvg, float mx, float my, float sw, float sh) {
        Theme th = theme();
        float fx = (sw - FIDGET_W) / 2.0f, fy = sh - FIDGET_H - 14.0f;
        boolean hit = mx >= fx && mx <= fx + FIDGET_W && my >= fy && my <= fy + FIDGET_H;
        boolean active = hit || configPanel.isOpen();
        if (active != fidgetHovered) { fidgetHovered = active; if (active) UiSounds.hover(); }
        nvg.glow(fx, fy, FIDGET_W, FIDGET_H, FIDGET_H / 2.0f, active ? 7.0f : 4.0f,
                Colors.withAlpha(th.accent(), active ? 0.28f : 0.13f));
        nvg.rectGradient(fx, fy, FIDGET_W, FIDGET_H, FIDGET_H / 2.0f, th.headerTop(), th.headerBottom(), true);
        nvg.rectOutline(fx, fy, FIDGET_W, FIDGET_H, FIDGET_H / 2.0f, 1.1f,
                Colors.withAlpha(active ? th.accentBright() : th.accent(), active ? 0.88f : 0.38f));
        float iconX = fx + 16.0f, iconY = fy + FIDGET_H / 2.0f - 6.0f;
        nvg.rect(iconX + 3.0f, iconY + 3.0f, 11.0f, 9.0f, 2.5f, Colors.withAlpha(th.accent(), 0.45f));
        nvg.rect(iconX, iconY, 11.0f, 9.0f, 2.5f, active ? th.accentBright() : th.accent());
        nvg.text("Configs", fx + 36.0f, fy + FIDGET_H / 2.0f, 13.5f, active ? th.textPrimary() : th.textMuted());
    }

    private void renderHint(NVGRenderer nvg, float sw, float sh) {
        nvg.text("ESC  ·  close", sw / 2.0f, sh - HINT_MARGIN, HINT_FONT,
                Colors.withAlpha(theme().textDisabled(), 0.5f));
    }

    private boolean fidgetHit(float mx, float my) {
        float sw = OverlayRenderer.uiWidth(), sh = OverlayRenderer.uiHeight();
        float fx = (sw - FIDGET_W) / 2.0f, fy = sh - FIDGET_H - 14.0f;
        return mx >= fx && mx <= fx + FIDGET_W && my >= fy && my <= fy + FIDGET_H;
    }

    private boolean searchBarHit(float mx, float my) {
        float barX = (OverlayRenderer.uiWidth() - SEARCH_W) / 2.0f;
        return mx >= barX && mx <= barX + SEARCH_W && my >= SEARCH_Y && my <= SEARCH_Y + SEARCH_H;
    }

    private boolean searchClearHit(float mx, float my) {
        float barX  = (OverlayRenderer.uiWidth() - SEARCH_W) / 2.0f;
        float clearX = barX + SEARCH_W - 26.0f;
        float midY   = SEARCH_Y + SEARCH_H / 2.0f;
        return mx >= clearX && mx <= clearX + 20.0f && my >= midY - 10.0f && my <= midY + 10.0f;
    }

    private float uiX(double v) { return OverlayRenderer.guiToUi(v); }
    private float uiY(double v) { return OverlayRenderer.guiToUi(v); }
    private Theme theme()        { return SixSevenClient.themes().current(); }

    // ── 1.21.5 Screen API signatures ──────────────────────────────────────────
    @Override
    public boolean mouseClicked(double px, double py, int btn) {
        float mx = uiX(px), my = uiY(py);
        NVGRenderer nvg = NVGRenderer.get();
        float sh  = OverlayRenderer.uiHeight();
        if (configPanel.isOpen()) { configPanel.mouseClicked(mx, my, btn); return true; }
        if (fidgetHit(mx, my))   { configPanel.open(); UiSounds.guiOpen(); return true; }
        if (searchBarHit(mx, my)) {
            if (!search.isEmpty() && searchClearHit(mx, my)) search.setLength(0);
            else searchFocused = true;
            UiSounds.select(); return true;
        }
        searchFocused = false;
        for (Panel p : panels) {
            if (p.headerHit(mx, my)) {
                bringToFront(p);
                if (btn == 0) { dragging = p; dragOffsetX = mx - p.getX(); dragOffsetY = my - p.getY(); pressX = mx; pressY = my; dragMoved = false; }
                else          { p.toggleCollapsed(); UiSounds.panelCollapse(); }
                return true;
            }
            if (p.bodyHit(nvg, mx, my, sh)) {
                bringToFront(p); pressedContentPanel = p; p.mouseClicked(mx, my, btn); return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double px, double py, int btn, double dx, double dy) {
        if (configPanel.isOpen()) return true;
        float mx = uiX(px), my = uiY(py);
        if (dragging != null) {
            if (Math.abs(mx - pressX) + Math.abs(my - pressY) > 3.0f) dragMoved = true;
            if (dragMoved) dragging.moveTo(mx - dragOffsetX, my - dragOffsetY);
            return true;
        }
        if (pressedContentPanel != null) pressedContentPanel.mouseDragged(mx, my);
        return true;
    }

    @Override
    public boolean mouseReleased(double px, double py, int btn) {
        if (configPanel.isOpen()) return true;
        if (dragging == null) {
            if (pressedContentPanel != null) { pressedContentPanel.mouseReleased(); pressedContentPanel = null; }
            return true;
        }
        if (!dragMoved && btn == 0) { dragging.toggleCollapsed(); UiSounds.panelCollapse(); }
        else if (dragMoved)         { STATE.markCustomized(); }
        dragging = null; return true;
    }

    @Override
    public boolean mouseScrolled(double px, double py, double hDelta, double vDelta) {
        if (configPanel.isOpen()) return true;
        float mx = uiX(px), my = uiY(py);
        NVGRenderer nvg = NVGRenderer.get();
        float sh = OverlayRenderer.uiHeight();
        for (Panel p : panels) {
            if (p.bodyHit(nvg, mx, my, sh) || p.headerHit(mx, my)) { p.onScroll(vDelta); return true; }
        }
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (configPanel.isOpen()) { configPanel.keyPressed(key); return true; }
        for (Panel p : panels) { if (p.isListening()) { p.keyPressed(key); return true; } }
        if (!searchFocused && (key == 47 || (key == 70 && (mods & 2) != 0))) { searchFocused = true; return true; }
        if (searchFocused) {
            switch (key) {
                case 256 -> { search.setLength(0); searchFocused = false; }
                case 257, 335 -> searchFocused = false;
                case 259 -> { if (!search.isEmpty()) search.deleteCharAt(search.length() - 1); }
            }
            return true;
        }
        if (key == 256 || guiModule().getKeybind().matches(key)) { close(); return true; }
        return super.keyPressed(key, scancode, mods);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        if (configPanel.isOpen()) { configPanel.charTyped(c); return true; }
        for (Panel p : panels) { if (p.isListening()) { p.charTyped(c); return true; } }
        if (searchFocused && !Character.isISOControl(c)) {
            if (search.length() < 40) search.append(c);
            return true;
        }
        return super.charTyped(c, mods);
    }

    private void bringToFront(Panel p) { if (panels.remove(p)) panels.addFirst(p); }

    public void openSearch(String text) { search.setLength(0); search.append(text); searchFocused = true; }
    public ConfigPanel configPanel()    { return configPanel; }

    public void openBlockPicker(BlockListSetting setting) {
        BlockGridModel model = new BlockGridModel(setting, () -> {
            BlockEspModule be = SixSevenClient.modules().blockEsp;
            return be != null ? be.lineColor.get() : -16711736;
        }, "Pick Block");
        client.setScreen(new IconPickerScreen(this, model, SixSevenClient.themes()));
    }
    public void openIconPicker(IconListSetting setting) {
        client.setScreen(new IconPickerScreen(this, new IconListGridModel(setting), SixSevenClient.themes()));
    }
}
