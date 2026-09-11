package dev.kryptic.gui.panel;

import dev.kryptic.KrypticClient;
import dev.kryptic.gui.ClickGuiState;
import dev.kryptic.module.client.StatsModule;
import dev.kryptic.render.nanovg.NVGImages;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.stats.StatsCard;
import dev.kryptic.stats.StatsTracker;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.List;
import java.util.Locale;

/**
 * The stats card, in the menu.
 *
 * The card only existed as a HUD element before, which meant reading it needed
 * the HUD switched on and nothing covering that corner of the screen. Both
 * views draw from {@link StatsCard}, so they can never disagree about what your
 * numbers are.
 *
 * Two columns rather than the HUD's one. The menu has room the HUD does not,
 * and the sections are short enough that a single column would be a stripe of
 * whitespace with a list down the left of it.
 */
public class StatsPanel extends Panel {

    private static final float PAD = 14.0f;
    private static final float HEAD = 44.0f;
    private static final float HEADER_GAP = 12.0f;
    private static final float SECTION_H = 22.0f;
    private static final float ROW_H = 17.0f;
    private static final float COL_GAP = 16.0f;
    private static final float SECTION_GAP = 8.0f;

    private static final float SECTION_FONT = 10.0f;
    private static final float ROW_FONT = 12.0f;
    private static final float NAME_FONT = 16.0f;
    private static final float SERVER_FONT = 11.0f;

    public StatsPanel(ThemeManager themes, ClickGuiState state) {
        super(themes, state.panel("__stats__"));
    }

    @Override
    protected String title() {
        return "Stats";
    }

    private StatsModule module() {
        return KrypticClient.modules() == null ? null : KrypticClient.modules().stats;
    }

    // ── layout ───────────────────────────────────────────────────────────────

    /**
     * Sections are dealt into two columns shortest-first by running total, so
     * the two sides end up close in height whatever combination is switched on.
     * Returns the per-section column assignment alongside the two heights.
     */
    private int[] columnise(List<StatsCard.Section> sections) {
        int[] which = new int[sections.size()];
        float left = 0.0f;
        float right = 0.0f;
        for (int i = 0; i < sections.size(); i++) {
            float h = SECTION_H + sections.get(i).rows().size() * ROW_H + SECTION_GAP;
            if (left <= right) {
                which[i] = 0;
                left += h;
            } else {
                which[i] = 1;
                right += h;
            }
        }
        return which;
    }

    private float columnHeight(List<StatsCard.Section> sections, int[] which, int column) {
        float h = 0.0f;
        for (int i = 0; i < sections.size(); i++) {
            if (which[i] != column) continue;
            h += SECTION_H + sections.get(i).rows().size() * ROW_H + SECTION_GAP;
        }
        return h;
    }

    @Override
    protected float contentHeight(NVGRenderer nvg) {
        StatsModule module = module();
        if (module == null) return HEAD + PAD * 2.0f;

        List<StatsCard.Section> sections = StatsCard.sections(MinecraftClient.getInstance(), module);
        if (sections.isEmpty()) return HEAD + HEADER_GAP + 28.0f + PAD;

        int[] which = columnise(sections);
        float tallest = Math.max(columnHeight(sections, which, 0), columnHeight(sections, which, 1));
        return HEAD + HEADER_GAP + tallest + PAD;
    }

    // ── render ───────────────────────────────────────────────────────────────

    @Override
    protected void renderContent(NVGRenderer nvg, float contentTop,
                                 float mouseX, float mouseY,
                                 float clipTop, float clipBot, float edgeFade) {
        StatsModule module = module();
        if (module == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        Theme th = theme();
        float x = this.panelState.x;
        float w = width();

        float y = renderHeader(nvg, th, client, module, x, contentTop, w) + HEADER_GAP;

        List<StatsCard.Section> sections = StatsCard.sections(client, module);
        if (sections.isEmpty()) {
            nvg.text("Every section is switched off", x + PAD, y + 12.0f, ROW_FONT, th.textDisabled());
            return;
        }

        float colW = (w - PAD * 2.0f - COL_GAP) / 2.0f;
        int[] which = columnise(sections);
        float[] cursor = {y, y};

        for (int i = 0; i < sections.size(); i++) {
            int column = which[i];
            float colX = x + PAD + column * (colW + COL_GAP);
            cursor[column] = renderSection(nvg, th, sections.get(i), colX, cursor[column], colW)
                    + SECTION_GAP;
        }
    }

    private float renderHeader(NVGRenderer nvg, Theme th, MinecraftClient client,
                               StatsModule module, float x, float y, float w) {
        ClientPlayerEntity player = client.player;
        String name = player == null ? "Not in a world" : player.getGameProfile().name();
        String where = player == null ? "—" : StatsTracker.serverLabel(client);

        float textX = x + PAD;
        if (module.skin.get() && player != null) {
            drawHead(nvg, th, player, x + PAD, y, HEAD);
            textX = x + PAD + HEAD + 12.0f;
        }

        nvg.textGradient(name, textX, y + 15.0f, NAME_FONT, th.accentBright(), th.accent());
        nvg.textTruncated(where, textX, y + 32.0f, SERVER_FONT, th.textMuted(),
                Math.max(20.0f, x + w - PAD - textX));

        return y + HEAD;
    }

    /**
     * The player's head, from the texture Minecraft already has bound, so it
     * follows skin changes and anything Skin Protect swaps in. Both passes
     * matter: the base layer, then the hat overlay.
     */
    private void drawHead(NVGRenderer nvg, Theme th, ClientPlayerEntity player, float x, float y, float size) {
        int tex;
        try {
            tex = NVGImages.wrapGlTexture(player.getSkin().body().texturePath(), 64, 64);
        } catch (Throwable t) {
            tex = -1;
        }
        if (tex <= 0) {
            nvg.rect(x, y, size, size, 5.0f, Colors.withAlpha(th.accent(), 0.18f));
            return;
        }
        NVGImages.drawSubImage(nvg, tex, 64.0f, 64.0f, 8.0f, 8.0f, 16.0f, 16.0f, x, y, size, size, 1.0f);
        NVGImages.drawSubImage(nvg, tex, 64.0f, 64.0f, 40.0f, 8.0f, 48.0f, 16.0f, x, y, size, size, 1.0f);
        nvg.rectOutline(x - 1.0f, y - 1.0f, size + 2.0f, size + 2.0f, 4.0f, 1.0f,
                Colors.withAlpha(0xFFFFFFFF, 0.28f));
    }

    private float renderSection(NVGRenderer nvg, Theme th, StatsCard.Section s,
                                float x, float y, float w) {
        String title = s.title().toUpperCase(Locale.ROOT);
        float titleY = y + SECTION_H / 2.0f;
        nvg.textGradient(title, x, titleY, SECTION_FONT, th.accentBright(), th.accent());

        float lineX = x + nvg.textWidth(title, SECTION_FONT) + 7.0f;
        float lineW = Math.max(0.0f, x + w - lineX);
        nvg.rect(lineX, titleY - 0.5f, lineW, 1.0f, 0.5f, Colors.withAlpha(th.accent(), 0.28f));

        float rowY = y + SECTION_H;
        boolean shade = false;
        for (StatsCard.Row r : s.rows()) {
            // every other row on a faint band, so the eye tracks across the gap
            // between a label and a value that is a whole column away
            if (shade) {
                nvg.rect(x - 4.0f, rowY, w + 8.0f, ROW_H, 4.0f, Colors.withAlpha(0xFFFFFFFF, 0.028f));
            }
            shade = !shade;

            float centre = rowY + ROW_H / 2.0f;
            nvg.text(r.label(), x, centre, ROW_FONT, th.textMuted());

            String value = r.value() == null ? "—" : r.value();
            int colour = r.value() == null ? th.textDisabled() : th.textPrimary();
            nvg.text(value, x + w - nvg.textWidth(value, ROW_FONT), centre, ROW_FONT, colour);

            rowY += ROW_H;
        }
        return rowY;
    }
}
