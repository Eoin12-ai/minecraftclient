package dev.sixseven.gui.panel;

import dev.sixseven.gui.ClickGuiState;
import dev.sixseven.gui.widget.*;
import dev.sixseven.module.Module;
import dev.sixseven.render.anim.Animation;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.settings.*;
import dev.sixseven.theme.Theme;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.Colors;
import dev.sixseven.util.UiSounds;

import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Redesigned ModuleEntry — flat/minimal, no clutter.
 *
 * Row layout (32 px tall):
 *  [8px pad] [module name 14.5px] ··· [dot 5px] [chevron 12px] [8px pad]
 *
 * Active state  : accent-filled row bg + name rendered with accentBright gradient
 * Hover state   : subtle accent tint over row
 * Dot indicator : 4 px, green (enabled) / muted (disabled) — same as original
 * Expand        : right-click — settings drawer slides in below (unchanged behaviour)
 *
 * Settings drawer changes:
 *  - Each setting row has a label above it rendered at 10.5 px in textDisabled
 *  - Drawer background is a flat semi-transparent rect, no gradient
 *  - 1 px separator line between drawer and row
 *
 * Favourites:
 *  - Middle-click (btn==2) toggles favourite flag in ClickGuiState
 *  - Favourite modules get a small star (★) rendered at the left edge of the name
 */
public class ModuleEntry {

    // ── row geometry ──────────────────────────────────────────────────────────
    public  static final float ROW_H          = 32.0f;
    private static final float RADIUS         = 8.0f;
    private static final float SETTING_INDENT = 14.0f;
    private static final float SETTING_GAP    = 3.0f;
    private static final float DRAWER_PAD_TOP = 8.0f;
    private static final float DRAWER_PAD_BOT = 6.0f;

    // ── dot indicator ─────────────────────────────────────────────────────────
    private static final float DOT_R          = 3.5f;
    private static final float DOT_GLOW_R     = 5.0f;
    private static final float DOT_GLOW_ALPHA = 0.45f;

    // ── favourite star ────────────────────────────────────────────────────────
    private static final float STAR_SIZE      = 9.0f;
    private static final String STAR_CHAR     = "★";

    // ── state ─────────────────────────────────────────────────────────────────
    private final Module             module;
    private final ThemeManager       themes;
    private final ClickGuiState      state;
    private final String             key;
    private final List<SettingWidget> widgets = new ArrayList<>();

    private final Animation hover  = new Animation(140.0f, 0.0f);
    private final Animation enable = new Animation(160.0f, 0.0f);
    private final Animation expand;

    private float x, y, width;

    // ─────────────────────────────────────────────────────────────────────────

    public ModuleEntry(Module module, ThemeManager themes, ClickGuiState state) {
        this.module = module;
        this.themes = themes;
        this.state  = state;
        this.key    = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","rlI">(
                module.getName(), module.getCategory().name());
        this.expand = new Animation(190.0f, state.isExpanded(key) ? 1.0f : 0.0f);
        this.enable.snapTo(module.isEnabled() ? 1.0f : 0.0f);

        for (Setting<?> s : module.getSettings()) {
            if      (s instanceof BooleanSetting   b) widgets.add(new BooleanWidget(themes, b));
            else if (s instanceof SliderSetting    sl) widgets.add(new SliderWidget(themes, sl));
            else if (s instanceof ModeSetting      m)  widgets.add(new ModeWidget(themes, m));
            else if (s instanceof ColorSetting     c)  widgets.add(new ColorWidget(themes, c));
            else if (s instanceof BlockListSetting bl) widgets.add(new BlockListWidget(themes, bl));
            else if (s instanceof IconListSetting  il) widgets.add(new IconListWidget(themes, il));
            else if (s instanceof StringSetting    st) widgets.add(new StringWidget(themes, st));
            else if (s instanceof KeybindSetting   kb) widgets.add(new KeybindWidget(themes, kb));
        }
        // module keybind always at end
        widgets.add(new KeybindWidget(themes, module.getKeybind()));
    }

    // ── public API ───────────────────────────────────────────────────────────

    public Module getModule() { return module; }

    public void setBounds(float x, float y, float width) {
        this.x     = x;
        this.y     = y;
        this.width = width;
    }

    // ── layout ───────────────────────────────────────────────────────────────

    private float drawerHeight(NVGRenderer nvg) {
        float h = DRAWER_PAD_TOP;
        for (SettingWidget w : widgets) {
            if (w.isVisible()) h += w.height(nvg) + SETTING_GAP;
        }
        return h + DRAWER_PAD_BOT;
    }

    public float height(NVGRenderer nvg) {
        float exp = expand.value();
        return ROW_H + (exp <= 0.005f ? 0.0f : exp * drawerHeight(nvg));
    }

    // ── render ───────────────────────────────────────────────────────────────

    public void render(NVGRenderer nvg, float mouseX, float mouseY, float edgeFade) {
        Theme   th      = theme();
        boolean rowHit  = mouseX >= x && mouseX <= x + width
                       && mouseY >= y && mouseY <= y + ROW_H;
        boolean isFav   = state.isFavourite(key);

        // hover sound
        if (rowHit && hover.getTarget() < 0.5f) UiSounds.hover();
        hover.setTarget(rowHit ? 1.0f : 0.0f);
        enable.setTarget(module.isEnabled() ? 1.0f : 0.0f);

        float hv  = hover.value();
        float ev  = enable.value();
        float xpv = expand.value();
        float midY = y + ROW_H / 2.0f;

        nvg.save();
        nvg.alpha(edgeFade);

        // ── active fill ──────────────────────────────────────────────────────
        if (ev > 0.01f) {
            nvg.save();
            nvg.alpha(ev);
            nvg.rect(x, y, width, ROW_H, RADIUS, th.moduleActiveFill());
            nvg.glow(x, y, width, ROW_H, RADIUS, 5.0f,
                    Colors.withAlpha(th.accent(), 0.18f * ev));
            nvg.restore();
        }

        // ── hover tint ───────────────────────────────────────────────────────
        if (hv > 0.01f) {
            nvg.rect(x, y, width, ROW_H, RADIUS,
                    Colors.withAlpha(th.accent(), 0.09f * hv));
        }

        // ── module name ───────────────────────────────────────────────────────
        float nameX = x + 10.0f;

        // favourite star
        if (isFav) {
            nvg.text(STAR_CHAR, nameX, midY, STAR_SIZE,
                    Colors.withAlpha(th.accentBright(), 0.85f));
            nameX += STAR_SIZE + 4.0f;
        }

        if (ev > 0.01f) {
            // active: gradient name + soft glow
            nvg.save();
            nvg.alpha(ev);
            nvg.textGlow(module.getName(), nameX, midY, 14.5f,
                    Colors.withAlpha(th.accent(), 0.7f));
            nvg.textGradient(module.getName(), nameX, midY, 14.5f,
                    th.accentBright(), th.accent());
            nvg.restore();
        }
        if (ev < 0.99f) {
            nvg.save();
            nvg.alpha(1.0f - ev);
            nvg.text(module.getName(), nameX, midY, 14.5f,
                    Colors.lerp(th.textMuted(), th.textPrimary(), hv));
            nvg.restore();
        }

        // ── right-side indicators: chevron then dot ───────────────────────────
        float rightEdge = x + width;

        // dot
        float dotX = rightEdge - 14.0f;
        int   dotC = Colors.lerp(th.statusDisabled(), th.statusEnabled(), ev);
        if (ev > 0.3f) {
            nvg.circleGlow(dotX, midY, DOT_R, DOT_GLOW_R,
                    Colors.withAlpha(dotC, DOT_GLOW_ALPHA * ev));
        }
        nvg.circle(dotX, midY, DOT_R, dotC);

        // expand chevron (only when module has settings)
        if (!widgets.isEmpty()) {
            float chX = rightEdge - 28.0f;
            nvg.save();
            nvg.translate(chX, midY);
            nvg.rotate((float)(xpv * Math.PI / 2.0));
            nvg.chevron(0, 0, 3.5f, 1.4f, th.textMuted(), false);
            nvg.restore();
        }

        // ── settings drawer ───────────────────────────────────────────────────
        if (xpv > 0.005f) {
            float dH = drawerHeight(nvg) * xpv;

            nvg.save();
            nvg.scissor(x, y + ROW_H, width, dH);
            nvg.alpha(xpv * edgeFade);

            // drawer background
            nvg.rect(x + 4.0f, y + ROW_H - 4.0f, width - 8.0f, dH + 4.0f,
                    6.0f, Colors.withAlpha(0xFF0A0A0A, 0.58f));

            // 1 px separator line
            nvg.rectGradient(x + 8.0f, y + ROW_H,
                    width - 16.0f, 1.0f, 0.5f,
                    Colors.withAlpha(th.accent(), 0.35f),
                    Colors.withAlpha(th.accent(), 0.0f),
                    false);

            float wy = y + ROW_H + DRAWER_PAD_TOP;
            for (SettingWidget w : widgets) {
                if (!w.isVisible()) continue;
                float wH = w.height(nvg);
                w.setBounds(x + SETTING_INDENT, wy, width - SETTING_INDENT * 2.0f);
                w.render(nvg, mouseX, mouseY);
                wy += wH + SETTING_GAP;
            }

            nvg.restore();
        }

        nvg.restore();
    }

    private Theme theme() { return themes.current(); }

    // ── input ─────────────────────────────────────────────────────────────────

    public boolean mouseClicked(float mx, float my, int btn) {
        if (mx >= x && mx <= x + width && my >= y && my <= y + ROW_H) {
            if (btn == 0) {
                module.toggle();
                UiSounds.toggle(module.isEnabled());
            } else if (btn == 1) {
                boolean exp = !(expand.getTarget() > 0.5f);
                expand.setTarget(exp ? 1.0f : 0.0f);
                state.setExpanded(key, exp);
                UiSounds.select();
            } else if (btn == 2) {
                // middle-click: toggle favourite
                state.setFavourite(key, !state.isFavourite(key));
                UiSounds.select();
            }
            return true;
        }

        // click inside open settings drawer
        if (expand.getTarget() > 0.5f
                && my >= y + ROW_H
                && my <= y + height(null)) {
            for (SettingWidget w : widgets) {
                if (w.isVisible() && w.mouseClicked(mx, my, btn)) return true;
            }
            return mx >= x && mx <= x + width;
        }

        return false;
    }

    public void    mouseDragged(float mx, float my)    { widgets.forEach(w -> w.mouseDragged(mx, my)); }
    public void    mouseReleased()                      { widgets.forEach(SettingWidget::mouseReleased); }
    public boolean keyPressed(int key)                  { return widgets.stream().anyMatch(w -> w.keyPressed(key)); }
    public boolean charTyped(int cp)                    { return widgets.stream().anyMatch(w -> w.charTyped(cp)); }
    public boolean isListening()                        { return widgets.stream().anyMatch(SettingWidget::isListening); }
}
