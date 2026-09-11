package dev.kryptic.gui;

import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
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
    /**
     * Inter, drawn by Minecraft rather than by NanoVG.
     *
     * The fallback used the default font and came out pixelated, because
     * Minecraft's own font is a bitmap. Minecraft will render a TTF though --
     * that is what the ttf font provider is for -- so registering Inter as a
     * font and asking for it by name gives the fallback the same typeface as
     * the styled menu, anti-aliased, with no NanoVG involved at all.
     *
     * If the provider fails to load, Minecraft falls back to the default font
     * on its own, so the worst case is what this looked like before.
     */
    /**
     * The typeface the fallback draws in, following the client's Font setting.
     *
     * Every bundled face is registered as a Minecraft font, so this honours the
     * same setting the NanoVG path uses rather than hard-coding one. Picking a
     * typeface is a matter of taste and changing it should not need a rebuild.
     *
     * On this version Style.withFont takes a StyleSpriteSource rather than an
     * Identifier, and the concrete type is the record nested inside it -- read
     * off the remapped jar rather than guessed, after guessing cost a build.
     * If a provider fails to load, Minecraft falls back to its default font on
     * its own.
     */
    private static Style uiStyle() {
        ClickGuiModule gui = KrypticClient.modules() == null ? null : KrypticClient.modules().clickGui;
        String face = switch (gui == null ? "Bold" : gui.font.get()) {
            case "Xuong"   -> "xuong";
            case "Vanilla" -> "vanilla";
            case "Mono"    -> "mono";
            case "Ten"     -> "ten";
            case "Bold"    -> "bold";
            default        -> "kryptic";
        };

        return Style.EMPTY.withFont(new StyleSpriteSource.Font(
                Identifier.of("krypticclient", smallText() ? face + "_small" : face)));
    }

    /**
     * Whether the fallback needs the half-size twin of the chosen face.
     *
     * The fallback converts a layout measured in UI space into Minecraft's
     * scaled space by dividing by the GUI scale. The panels shrink with it;
     * Minecraft's text does not. At GUI scale 3 a column that is 300 units wide
     * becomes 100 pixels while the text stays the size it always was, so the
     * proportions come out roughly two and a half times heavier than the
     * NanoVG path and fit() eats the labels -- CONFIGS renders as "CON..",
     * CRYSTAL OPTIMISER as "CRYSTAL O..".
     *
     * <p>Halving the face restores the ratio. It is chosen from the converted
     * width rather than from the GUI scale directly, because what actually
     * matters is how many pixels a column ends up with, and that depends on the
     * window as well as the setting. At GUI scale 1 and 2 there is room for the
     * full-size face and it keeps it.
     *
     * <p>Only the fallback goes through here. NanoVG sets its own point size
     * and was never affected.
     */
    private static boolean smallText() {
        return OverlayRenderer.uiToGui(300.0) < 140.0f;
    }

    /** Half the cap height of whichever face is in use, to centre a line on a row. */
    private static int textDy() {
        return smallText() ? 3 : 4;
    }

    /** Text in whichever typeface the Font setting names. */
    private static Text ui(String s) {
        return Text.literal(s).setStyle(uiStyle());
    }

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

        int rowH  = Math.round(OverlayRenderer.uiToGui(ModuleEntry.ROW_H));
        int headH = Math.round(OverlayRenderer.uiToGui(Panel.HEADER_H));
        int colW  = Math.round(OverlayRenderer.uiToGui(STATE.columnWidth()));
        int gap   = Math.round(OverlayRenderer.uiToGui(STATE.columnGap()));
        int left  = Math.round(OverlayRenderer.uiToGui(STATE.columnsLeft()));
        int top   = Math.round(OverlayRenderer.uiToGui(STATE.columnTop()));

        // A near-opaque sheet here threw away the blur that renderBackground
        // had already applied, which is what made the menu read as a flat black
        // screen rather than as glass over the world. Just enough tint to sit
        // the panels on.
        ctx.fill(0, 0, width, height, 0x2E06060A);

        drawFallbackTopBar(ctx);

        String query = search.toString().toLowerCase(Locale.ROOT).trim();
        int cx = left;
        for (Category category : Category.values()) {
            List<Module> inColumn = new ArrayList<>();
            for (Module m : modules.inCategory(category)) {
                if (query.isEmpty() || m.getName().toLowerCase(Locale.ROOT).contains(query)) {
                    inColumn.add(m);
                }
            }

            int on = 0;
            for (Module m : inColumn) if (m.isEnabled()) on++;

            int colH = headH + inColumn.size() * rowH + 6;
            glass(ctx, cx, top, cx + colW, top + colH, 0x7A2E3038, 0x6E101218);
            glass(ctx, cx + 1, top + 1, cx + colW - 1, top + headH, 0x5E3E4250, 0x52202430);
            ctx.fill(cx + 3, top + headH, cx + colW - 3, top + headH + 1, 0x55FFFFFF);
            // a small square standing in for the category glyph, then the name
            ctx.fill(cx + 8, top + headH / 2 - 3, cx + 14, top + headH / 2 + 3, 0xFFFFFFFF);
            ctx.drawText(this.client.textRenderer, ui(category.name().toUpperCase(Locale.ROOT)),
                    cx + 19, top + headH / 2 - textDy(), 0xFFEDEDEF, false);

            // the collapse mark sits hard right, where the reference puts it
            ctx.fill(cx + colW - 15, top + headH / 2 - 1, cx + colW - 8, top + headH / 2, 0xFF8E8E96);

            String count = on + "/" + inColumn.size();
            int cw = this.client.textRenderer.getWidth(ui(count));
            ctx.drawText(this.client.textRenderer, ui(count),
                    cx + colW - 21 - cw, top + headH / 2 - textDy(), 0xFF5A5A62, false);

            int ry = top + headH + 3;
            for (Module m : inColumn) {
                boolean enabled = m.isEnabled();
                boolean hover   = mx >= cx && mx <= cx + colW && my >= ry && my <= ry + rowH;
                if (enabled || hover) {
                    // Rounded, because a square highlight inside a rounded
                    // panel is the one shape that gives the whole thing away.
                    roundFill(ctx, cx + 4, ry, cx + colW - 4, ry + rowH, 5,
                            enabled ? 0x30FFFFFF : 0x18FFFFFF);
                }

                if (enabled) {
                    ctx.fill(cx + 6, ry + 4, cx + 8, ry + rowH - 4, 0xFFFFFFFF);
                }

                // The switch owns the right of the row, so the name has to stop
                // short of it. Without this, long names run underneath the
                // switch and the two become unreadable together.
                int nameRoom = colW - 13 - SWITCH_W - 12;
                ctx.drawText(this.client.textRenderer,
                        ui(fit(m.getName().toUpperCase(Locale.ROOT), nameRoom)),
                        cx + 13, ry + rowH / 2 - textDy(), enabled ? 0xFFFFFFFF : 0xFF8E8E96, false);

                toggle(ctx, cx + colW - 9 - SWITCH_W, ry + rowH / 2 - SWITCH_H / 2, enabled);
                ry += rowH;
            }

            cx += colW + gap;
        }

        drawFallbackPanes(ctx);

        String hint = "RIGHT SHIFT TO CLOSE   -   RIGHT-CLICK A MODULE FOR ITS SETTINGS";
        ctx.drawText(this.client.textRenderer, ui(hint),
                (width - this.client.textRenderer.getWidth(ui(hint))) / 2, height - 14, 0xFF5A5A62, false);

        // Say why, on screen. The styled menu failing is not something the user
        // can diagnose from a log file they have to go and find, and the reason
        // is the one piece of information that makes the next report useful.
        // The status leads and the label follows, because this line gets cut off
        // at the window edge and the half worth reading is the diagnosis.
        String banner = fit(OverlayRenderer.status() + "  (Kryptic fallback view)", width - 8);
        ctx.fill(0, 0, width, 12, 0xC0301010);
        ctx.drawText(this.client.textRenderer, ui(banner), 4, 2, 0xFFE08A8A, false);
    }

    /**
     * The bar, drawn where the click handler says it is.
     *
     * This used to lay itself out independently -- title at the column margin,
     * pills sized to their labels -- which looked fine and was wrong: every hit
     * test goes through buttonX, searchX and buttonY in UI space, so the
     * buttons you saw were not the buttons you were clicking. Converting those
     * same functions is the only way the two can agree.
     *
     * Labels are cut to the converted pill rather than the pill being widened,
     * for the same reason: the width is not this method's to choose.
     */
    private void drawFallbackTopBar(DrawContext ctx) {
        float sw = OverlayRenderer.uiWidth();
        int h = Math.round(OverlayRenderer.uiToGui(SEARCH_H));
        int searchW = Math.round(OverlayRenderer.uiToGui(SEARCH_W));
        int sx = Math.round(OverlayRenderer.uiToGui(searchX(sw)));
        int sy = Math.round(OverlayRenderer.uiToGui(searchY()));
        int pillW = Math.round(OverlayRenderer.uiToGui(PILL_W));
        int pillH = Math.round(OverlayRenderer.uiToGui(PILL_H));
        int by = Math.round(OverlayRenderer.uiToGui(buttonY()));

        int markX = Math.round(OverlayRenderer.uiToGui(barX(sw)));
        // The mark's room is its own width plus the gap that follows it. In the
        // NanoVG path that width holds a logo image; here it holds the word,
        // and the word is wider than the image at any size that stays legible.
        // Lending it the gap costs nothing -- the search box starts after it,
        // and two pixels are held back so the two never touch.
        int markRoom = Math.round(OverlayRenderer.uiToGui(MARK_W + MARK_GAP)) - 2;
        ctx.drawText(this.client.textRenderer, ui(fit("KRYPTIC", markRoom)),
                markX, sy + h / 2 - textDy(), 0xFFEDEDEF, false);

        panel(ctx, sx, sy, sx + searchW, sy + h, 0x30202430, 0x2EFFFFFF);
        String typed = search.length() == 0
                ? "SEARCH MODULES" : search.toString().toUpperCase(Locale.ROOT);
        ctx.drawText(this.client.textRenderer, ui(fit(typed, searchW - 12)),
                sx + 6, sy + h / 2 - textDy(), search.length() == 0 ? 0xFF5A5A62 : 0xFFEDEDEF, false);

        String[] labels = {"STATS", "CONFIGS", "THEMES"};
        boolean[] active = {statsOpen, false, themesOpen};
        for (int i = 0; i < labels.length; i++) {
            int x0 = Math.round(OverlayRenderer.uiToGui(buttonX(sw, i)));
            panel(ctx, x0, by, x0 + pillW, by + pillH,
                    active[i] ? 0x5C323744 : 0x30222630, active[i] ? 0x66FFFFFF : 0x2AFFFFFF);
            String label = fit(labels[i], pillW - 6);
            int lw = this.client.textRenderer.getWidth(ui(label));
            ctx.drawText(this.client.textRenderer, ui(label),
                    x0 + (pillW - lw) / 2, by + pillH / 2 - textDy(),
                    active[i] ? 0xFFFFFFFF : 0xFF8E8E96, false);
        }
    }

    /**
     * The Themes and Stats panes, which otherwise opened invisibly.
     *
     * The fallback drew the columns and the bar and nothing else, so pressing
     * Themes set the flag, the NanoVG pane never drew, and the menu looked
     * broken -- a button that swallows a click and does nothing is worse than a
     * button that is missing. These are drawn at the panes' own coordinates so
     * what is on screen lines up with what is clickable.
     */
    private void drawFallbackPanes(DrawContext ctx) {
        if (themesOpen) {
            pane(ctx, themesPanel, "THEMES", themeLines());
        }

        if (statsOpen) {
            pane(ctx, statsPanel, "STATS", List.of("Open the pane in the styled menu for the full read-out."));
        }
    }

    private List<String> themeLines() {
        List<String> lines = new ArrayList<>();
        ThemeManager themes = KrypticClient.themes();
        if (themes == null) {
            return lines;
        }

        for (Theme theme : themes.getThemes()) {
            lines.add((theme == themes.current() ? "> " : "  ") + theme.getName().toUpperCase(Locale.ROOT));
        }

        lines.add("  + ADD CUSTOM");
        return lines;
    }

    private void pane(DrawContext ctx, Panel panel, String title, List<String> lines) {
        int x = Math.round(OverlayRenderer.uiToGui(panel.getX()));
        int y = Math.round(OverlayRenderer.uiToGui(panel.getY()));
        int w = Math.round(OverlayRenderer.uiToGui(panel.width()));
        int headH = Math.round(OverlayRenderer.uiToGui(Panel.HEADER_H));
        int rowH = Math.round(OverlayRenderer.uiToGui(26.0f));
        int h = headH + lines.size() * rowH + 6;

        // a scrim, so the pane reads as sitting above the grid
        ctx.fill(0, 0, width, height, 0x66050506);
        glass(ctx, x, y, x + w, y + h, 0x8A2E3038, 0x7E101218);
        glass(ctx, x + 1, y + 1, x + w - 1, y + headH, 0x5E3E4250, 0x52202430);
        ctx.drawText(this.client.textRenderer, ui(title), x + 8, y + headH / 2 - textDy(), 0xFFEDEDEF, false);

        int ry = y + headH + 3;
        for (String line : lines) {
            boolean current = line.startsWith(">");
            if (current) {
                ctx.fill(x + 4, ry, x + w - 4, ry + rowH, 0x2EFFFFFF);
            }

            ctx.drawText(this.client.textRenderer, ui(fit(line, w - 16)),
                    x + 8, ry + rowH / 2 - textDy(), current ? 0xFFFFFFFF : 0xFF9CA0AC, false);
            ry += rowH;
        }
    }

    /**
     * The longest prefix of {@code text} that fits in {@code room} pixels.
     *
     * The column is about a hundred pixels wide once the UI-space layout is
     * converted to vanilla's, and plenty of module names are wider than what
     * is left after the switch takes its share. Measuring against the styled
     * text matters: the width of the glyphs actually drawn is not the width of
     * the same string in the default font.
     */
    private String fit(String text, int room) {
        if (room <= 0 || this.client.textRenderer.getWidth(ui(text)) <= room) {
            return text;
        }

        String ellipsis = "..";
        int budget = room - this.client.textRenderer.getWidth(ui(ellipsis));
        StringBuilder kept = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (this.client.textRenderer.getWidth(ui(kept.toString() + text.charAt(i))) > budget) {
                break;
            }

            kept.append(text.charAt(i));
        }

        return kept + ellipsis;
    }

    private static final int SWITCH_W = 14;
    private static final int SWITCH_H = 8;

    /**
     * A toggle switch, the way the reference client draws one.
     *
     * A dot says "this is on" and nothing else. A switch says which way it
     * moves, so a column of them reads as a set of controls rather than as a
     * column of indicator lights -- and it makes the hit target obvious, which
     * a three-pixel dot never did.
     */
    private static void toggle(DrawContext ctx, int x, int y, boolean on) {
        int track = on ? 0xFFFFFFFF : 0x33FFFFFF;
        // the track, with its end pixels clipped so it reads as a rounded pill
        ctx.fill(x + 1, y, x + SWITCH_W - 1, y + SWITCH_H, track);
        ctx.fill(x, y + 1, x + 1, y + SWITCH_H - 1, track);
        ctx.fill(x + SWITCH_W - 1, y + 1, x + SWITCH_W, y + SWITCH_H - 1, track);

        int knobX = on ? x + SWITCH_W - SWITCH_H + 1 : x + 1;
        int knob = on ? 0xFF15151A : 0xFFB0B0B8;
        ctx.fill(knobX + 1, y + 1, knobX + SWITCH_H - 2, y + SWITCH_H - 1, knob);
        ctx.fill(knobX, y + 2, knobX + 1, y + SWITCH_H - 2, knob);
        ctx.fill(knobX + SWITCH_H - 2, y + 2, knobX + SWITCH_H - 1, y + SWITCH_H - 2, knob);
    }

    /**
     * How far a row is pulled in, to round a corner.
     *
     * The old version inset the first row by two pixels and the second by one,
     * which is what you do when you have square fills and have given up. A
     * corner is a quarter circle, and a row's inset is just the horizontal
     * distance from the arc to the edge at that height, so a radius of eight
     * gives five pixels on the top row and nothing by the eighth. That is a
     * real curve, drawn one row at a time.
     */
    private static int cornerInset(int row, int height, int radius) {
        int fromEnd = Math.min(row, height - 1 - row);
        if (fromEnd >= radius) {
            return 0;
        }

        double dy = radius - 0.5 - fromEnd;
        return (int) Math.round(radius - Math.sqrt(Math.max(0.0, radius * radius - dy * dy)));
    }

    /** The radius a box of this size can carry without the curves meeting. */
    private static int radiusFor(int w, int h) {
        return Math.max(0, Math.min(PANEL_RADIUS, Math.min(w, h) / 2 - 1));
    }

    private static final int PANEL_RADIUS = 12;

    /**
     * A pane of liquid glass.
     *
     * The blur behind it is already there -- renderBackground blurs the world
     * before any of this draws -- and the single biggest thing standing between
     * that and glass was opacity. A panel at 76% alpha is a dark card with a
     * blurred photograph faintly behind it. Dropping it lets the blur become
     * the material instead of the backdrop, which is the whole effect: what you
     * see through the panel is the world, smeared, not a picture of it.
     *
     * On top of that go the four things that make a sheet of glass read as one
     * rather than as a translucent rectangle:
     *
     * <ul>
     * <li>a sheen across the top third, strongest at the very top and gone by
     *     the middle -- light entering the face of the pane, not a border;
     * <li>a bounce along the bottom, much fainter, from light coming back up
     *     off whatever it is sitting on;
     * <li>a rim that is bright where the pane is lit and dark where it is not,
     *     so the edge turns with the curve instead of tracing it evenly;
     * <li>a wide, weak shadow, which is what puts it above the background
     *     rather than in it.
     * </ul>
     */
    private static void glass(DrawContext ctx, int x0, int y0, int x1, int y1,
                              int topArgb, int bottomArgb) {
        int height = Math.max(1, y1 - y0);
        int radius = radiusFor(x1 - x0, height);

        // Five rings rather than three, each weaker. A tight dark shadow reads
        // as a border; a wide faint one reads as height.
        for (int ring = 5; ring >= 1; ring--) {
            shadowRing(ctx, x0 - ring, y0 - ring + 2, x1 + ring, y1 + ring + 2,
                    radius + ring, ((6 - ring) * 5) << 24);
        }

        for (int i = 0; i < height; i++) {
            int inset = cornerInset(i, height, radius);
            int argb = Colors.lerpArgb(topArgb, bottomArgb, (float) i / height);
            ctx.fill(x0 + inset, y0 + i, x1 - inset, y0 + i + 1, argb);
        }

        // The sheen. Quadratic rather than linear so it falls away quickly and
        // then lingers, which is how light through a curved face behaves; a
        // straight ramp looks like a gradient someone applied.
        int sheen = Math.max(1, (int) (height * 0.42f));
        for (int i = 0; i < sheen; i++) {
            float k = 1.0f - (float) i / sheen;
            int alpha = (int) (0x2E * k * k);
            if (alpha <= 0) {
                continue;
            }

            int inset = cornerInset(i, height, radius);
            ctx.fill(x0 + inset, y0 + i, x1 - inset, y0 + i + 1, alpha << 24 | 0xFFFFFF);
        }

        int bounce = Math.max(1, (int) (height * 0.22f));
        for (int i = 0; i < bounce; i++) {
            float k = (float) i / bounce;
            int alpha = (int) (0x12 * k * k);
            if (alpha <= 0) {
                continue;
            }

            int row = height - bounce + i;
            int inset = cornerInset(row, height, radius);
            ctx.fill(x0 + inset, y0 + row, x1 - inset, y0 + row + 1, alpha << 24 | 0xFFFFFF);
        }

        // The rim, turning with the curve: bright along the top, neutral at the
        // waist, dark underneath.
        for (int i = 0; i < height; i++) {
            int inset = cornerInset(i, height, radius);
            float t = (float) i / Math.max(1, height - 1);
            int edge = t < 0.5f
                    ? Colors.lerpArgb(0x66FFFFFF, 0x14FFFFFF, t * 2.0f)
                    : Colors.lerpArgb(0x14FFFFFF, 0x3C000000, (t - 0.5f) * 2.0f);
            ctx.fill(x0 + inset, y0 + i, x0 + inset + 1, y0 + i + 1, edge);
            ctx.fill(x1 - inset - 1, y0 + i, x1 - inset, y0 + i + 1, edge);
        }

        int cap = cornerInset(0, height, radius);
        ctx.fill(x0 + cap, y0, x1 - cap, y0 + 1, 0x72FFFFFF);
        ctx.fill(x0 + cap, y1 - 1, x1 - cap, y1, 0x3C000000);

        int inner = cornerInset(1, height, radius);
        ctx.fill(x0 + inner + 1, y0 + 1, x1 - inner - 1, y0 + 2, 0x2CFFFFFF);
    }

    /** A solid rounded rectangle, for anything sitting inside a panel. */
    private static void roundFill(DrawContext ctx, int x0, int y0, int x1, int y1,
                                  int radius, int argb) {
        int height = Math.max(1, y1 - y0);
        int r = Math.max(0, Math.min(radius, Math.min(x1 - x0, height) / 2 - 1));
        for (int i = 0; i < height; i++) {
            int inset = cornerInset(i, height, r);
            ctx.fill(x0 + inset, y0 + i, x1 - inset, y0 + i + 1, argb);
        }
    }

    /** One row-by-row rounded outline, used to build the shadow. */
    private static void shadowRing(DrawContext ctx, int x0, int y0, int x1, int y1,
                                   int radius, int argb) {
        int height = Math.max(1, y1 - y0);
        int r = Math.max(0, Math.min(radius, Math.min(x1 - x0, height) / 2 - 1));
        for (int i = 0; i < height; i++) {
            int inset = cornerInset(i, height, r);
            if (i == 0 || i == height - 1) {
                ctx.fill(x0 + inset, y0 + i, x1 - inset, y0 + i + 1, argb);
            } else {
                ctx.fill(x0 + inset, y0 + i, x0 + inset + 1, y0 + i + 1, argb);
                ctx.fill(x1 - inset - 1, y0 + i, x1 - inset, y0 + i + 1, argb);
            }
        }
    }

    /** A small glass surface -- the search field and the three buttons. */
    private static void panel(DrawContext ctx, int x0, int y0, int x1, int y1, int fill, int edge) {
        glass(ctx, x0, y0, x1, y1, fill, fill);
        ctx.fill(x0 + 2, y0, x1 - 2, y0 + 1, edge);
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
