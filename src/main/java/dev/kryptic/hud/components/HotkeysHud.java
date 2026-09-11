package dev.kryptic.hud.components;

import dev.kryptic.hud.HudComponent;
import dev.kryptic.hud.HudSurface;
import dev.kryptic.module.Module;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Every module you have bound a key to, and what that key is.
 *
 * The point is not the list, it is the recall: a client accumulates binds and
 * nobody remembers all of them a week later. Only bound modules appear, so it
 * stays as short as your own setup and shows nothing at all until you have
 * bound something.
 *
 * A module that is switched off is dimmed rather than hidden. Hiding it would
 * make the panel jump every time you toggled anything, and the whole value of
 * the list is that a given module sits in the same place each time you look.
 */
public class HotkeysHud extends HudComponent {

    private static final float PAD = 8.0f;
    private static final float HEADER_H = 17.0f;
    private static final float ROW_H = 15.0f;
    private static final float FONT = 11.0f;
    private static final float GAP = 22.0f;      // between a name and its key
    private static final float MIN_W = 128.0f;

    private final ModuleManager modules;
    private final ThemeManager themes;

    public HotkeysHud(ModuleManager modules, ThemeManager themes, BooleanSupplier visible) {
        super("hotkeys", 0.006f, 0.20f, visible);
        this.modules = modules;
        this.themes = themes;
    }

    /** Bound modules, alphabetically — the order has to be stable to be learnable. */
    private List<Module> bound() {
        List<Module> out = new ArrayList<>();
        for (Module m : modules.all()) {
            if (m.getKeybind() != null && m.getKeybind().isBound()) out.add(m);
        }
        out.sort(Comparator.comparing(Module::getName));
        return out;
    }

    @Override
    public float measureWidth(NVGRenderer nvg) {
        float widest = nvg.textWidth("HOTKEYS", HudSurface.TITLE_FONT);
        for (Module m : bound()) {
            widest = Math.max(widest,
                    nvg.textWidth(m.getName().toUpperCase(), FONT)
                            + GAP + nvg.textWidth(m.getKeybind().keyName(), FONT));
        }
        return Math.max(MIN_W, widest + PAD * 2.0f);
    }

    @Override
    public float measureHeight(NVGRenderer nvg) {
        int rows = bound().size();
        if (rows == 0) return 0.0f;
        return HEADER_H + rows * ROW_H + 5.0f;
    }

    @Override
    public void render(NVGRenderer nvg, float x, float y, float w, float h) {
        List<Module> rows = bound();
        if (rows.isEmpty()) return;          // nothing bound, nothing to say

        Theme th = themes.current();
        float rowY = HudSurface.titledPanel(nvg, x, y, w, h, HEADER_H, "HOTKEYS", th) + 2.0f;

        for (Module m : rows) {
            boolean on = m.isEnabled();
            float mid = rowY + ROW_H / 2.0f;

            if (on) {
                // a faint band behind what is running, so the list doubles as a
                // read of current state without a second column of ticks
                nvg.rect(x + 2.0f, rowY, w - 4.0f, ROW_H, 2.0f,
                        Colors.withAlpha(th.accent(), 0.10f));
            }

            String name = m.getName().toUpperCase();
            nvg.text(name, x + PAD, mid, FONT, on ? th.textPrimary() : th.textDisabled());

            String key = m.getKeybind().keyName();
            float kw = nvg.textWidth(key, FONT);
            nvg.text(key, x + w - PAD - kw, mid, FONT,
                    on ? th.accentBright() : th.textDisabled());

            rowY += ROW_H;
        }
    }
}
