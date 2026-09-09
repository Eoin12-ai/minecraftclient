package dev.kryptic.hud.components;

import dev.kryptic.hud.HudComponent;
import dev.kryptic.hud.HudSurface;
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

import java.util.Locale;

/**
 * The stats card.
 *
 * Rows are assembled first and measured second, so the card is exactly as tall
 * as the sections that are switched on and nothing has to be kept in sync by
 * hand when a section is toggled.
 *
 * A value of {@code null} renders as a dash. That is deliberate and load
 * bearing: lifetime figures are unavailable until the server sends them, and a
 * dash says so honestly where a zero would read as a real count.
 */
public class StatsHud extends HudComponent {

    private static final float WIDTH        = 214.0f;
    private static final float PAD          = 11.0f;
    private static final float HEAD         = 34.0f;
    private static final float BUST_H       = 56.0f;
    private static final float ROW_H        = 15.0f;
    private static final float SECTION_H    = 19.0f;
    private static final float HEADER_GAP   = 8.0f;
    private static final float ROW_FONT     = 11.5f;
    private static final float VALUE_FONT   = 11.5f;
    private static final float NAME_FONT    = 14.0f;
    private static final float SERVER_FONT  = 10.5f;
    private static final float SECTION_FONT = 9.5f;

    private final StatsModule  module;
    private final ThemeManager themes;

    public StatsHud(StatsModule module, ThemeManager themes) {
        super("stats", 0.006f, 0.06f, module::isEnabled);
        this.module = module;
        this.themes = themes;
    }

    private float headerHeight() {
        if (!module.skin.get()) return NAME_FONT + SERVER_FONT + 6.0f;
        return module.skinStyle.is("Bust") ? BUST_H : HEAD;
    }

    // ── measurement ──────────────────────────────────────────────────────────

    @Override
    public float measureWidth(NVGRenderer nvg) {
        return WIDTH;
    }

    @Override
    public float measureHeight(NVGRenderer nvg) {
        MinecraftClient client = MinecraftClient.getInstance();
        float h = PAD + headerHeight() + HEADER_GAP;
        for (StatsCard.Section s : StatsCard.sections(client, module)) {
            h += SECTION_H + s.rows().size() * ROW_H;
        }
        return h + PAD - 2.0f;
    }

    // ── render ───────────────────────────────────────────────────────────────

    @Override
    public void render(NVGRenderer nvg, float x, float y, float w, float h) {
        MinecraftClient client = MinecraftClient.getInstance();
        Theme th = themes.current();

        HudSurface.panel(nvg, x, y, w, h, 10.0f, th);
        nvg.rectOutline(x, y, w, h, 10.0f, 1.0f, Colors.withAlpha(th.accent(), 0.35f));

        float cursorY = y + PAD;
        cursorY = renderHeader(nvg, th, client, x, cursorY, w);
        cursorY += HEADER_GAP;

        for (StatsCard.Section s : StatsCard.sections(client, module)) {
            cursorY = renderSection(nvg, th, s, x, cursorY, w);
        }
    }

    private float renderHeader(NVGRenderer nvg, Theme th, MinecraftClient client,
                               float x, float y, float w) {
        ClientPlayerEntity player = client.player;
        String name = player == null ? "Not in a world" : player.getGameProfile().name();
        String where = player == null ? "—" : StatsTracker.serverLabel(client);

        float textX = x + PAD;
        float blockH = headerHeight();

        if (module.skin.get() && player != null) {
            boolean bust = module.skinStyle.is("Bust");
            float size = HEAD;
            drawSkin(nvg, player, x + PAD, y, size, bust);
            textX = x + PAD + size + 10.0f;
        }

        float nameY = y + (module.skin.get() ? HEAD / 2.0f - 7.0f : 0.0f) + NAME_FONT / 2.0f;
        nvg.textGradient(name, textX, nameY, NAME_FONT, th.accentBright(), th.accent());
        nvg.textTruncated(where, textX, nameY + 13.0f, SERVER_FONT, th.textMuted(),
                Math.max(20.0f, x + w - PAD - textX));

        return y + blockH;
    }

    /**
     * Draws the player's own skin straight from the texture Minecraft already
     * has bound, so it follows skin changes and anything SkinProtect swaps in.
     * Both passes matter: the base layer, then the hat or jacket overlay.
     */
    private void drawSkin(NVGRenderer nvg, ClientPlayerEntity player,
                          float x, float y, float size, boolean bust) {
        int tex;
        try {
            tex = NVGImages.wrapGlTexture(player.getSkin().body().texturePath(), 64, 64);
        } catch (Throwable t) {
            tex = -1;
        }
        if (tex <= 0) {
            // no texture yet: a themed placeholder rather than a hole
            nvg.rect(x, y, size, size, 4.0f, Colors.withAlpha(themes.current().accent(), 0.18f));
            return;
        }

        // head: base at (8,8)-(16,16), hat overlay at (40,8)-(48,16)
        NVGImages.drawSubImage(nvg, tex, 64.0f, 64.0f, 8.0f, 8.0f, 16.0f, 16.0f, x, y, size, size, 1.0f);
        NVGImages.drawSubImage(nvg, tex, 64.0f, 64.0f, 40.0f, 8.0f, 48.0f, 16.0f, x, y, size, size, 1.0f);

        if (bust) {
            // torso: base at (20,20)-(28,32), jacket overlay at (20,36)-(28,48)
            float torsoY = y + size;
            float torsoH = size * 1.5f;
            NVGImages.drawSubImage(nvg, tex, 64.0f, 64.0f, 20.0f, 20.0f, 28.0f, 32.0f,
                    x, torsoY, size, torsoH, 1.0f);
            NVGImages.drawSubImage(nvg, tex, 64.0f, 64.0f, 20.0f, 36.0f, 28.0f, 48.0f,
                    x, torsoY, size, torsoH, 1.0f);
        }

        nvg.rectOutline(x - 1.0f, y - 1.0f, size + 2.0f,
                (bust ? size * 2.5f : size) + 2.0f, 3.0f, 1.0f,
                Colors.withAlpha(0xFFFFFFFF, 0.28f));
    }

    private float renderSection(NVGRenderer nvg, Theme th, StatsCard.Section s,
                                float x, float y, float w) {
        String title = s.title().toUpperCase(Locale.ROOT);
        float titleY = y + SECTION_H / 2.0f;
        nvg.textGradient(title, x + PAD, titleY, SECTION_FONT, th.accentBright(), th.accent());

        float lineX = x + PAD + nvg.textWidth(title, SECTION_FONT) + 7.0f;
        float lineW = Math.max(0.0f, x + w - PAD - lineX);
        nvg.rect(lineX, titleY - 0.5f, lineW, 1.0f, 0.5f, Colors.withAlpha(th.accent(), 0.28f));

        float rowY = y + SECTION_H;
        for (StatsCard.Row r : s.rows()) {
            float centre = rowY + ROW_H / 2.0f;
            nvg.text(r.label(), x + PAD, centre, ROW_FONT, th.textMuted());

            String value = r.value() == null ? "—" : r.value();
            int colour = r.value() == null ? th.textDisabled() : th.textPrimary();
            float vw = nvg.textWidth(value, VALUE_FONT);
            nvg.text(value, x + w - PAD - vw, centre, VALUE_FONT, colour);

            rowY += ROW_H;
        }
        return rowY;
    }
}
