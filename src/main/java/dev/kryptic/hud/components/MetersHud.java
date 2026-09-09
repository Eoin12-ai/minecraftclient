package dev.kryptic.hud.components;

import dev.kryptic.hud.HudComponent;
import dev.kryptic.hud.HudSurface;
import dev.kryptic.module.client.HudModule;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.theme.Theme;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The readouts that do not deserve a pill of their own.
 *
 * The existing readouts are one component each, pinned to a hardcoded fraction
 * along the bottom edge. That works for six of them and stops working at the
 * seventh: the fractions are fixed, so a wider value pushes into its
 * neighbour and everything past 0.45 runs under the hotbar. Rather than keep
 * threading new pills through gaps, everything added here shares one card that
 * measures itself, so rows can be switched on and off without anything
 * colliding and the whole card is a single drag target.
 *
 * Rows are assembled before they are measured, so the card is exactly as tall
 * as what is switched on.
 */
public class MetersHud extends HudComponent {

    private static final float PAD        = 10.0f;
    private static final float ROW_H      = 15.0f;
    private static final float LABEL_FONT = 11.0f;
    private static final float VALUE_FONT = 11.5f;
    private static final float MIN_WIDTH  = 132.0f;

    /** Ticks per second, for turning a per-tick delta into a per-second one. */
    private static final double TPS = 20.0;

    /** The nether is eight overworld blocks to one of its own. */
    private static final double NETHER_RATIO = 8.0;

    private final HudModule     hud;
    private final ThemeManager  themes;

    /** Horizontal speed, smoothed so the number is readable rather than jittery. */
    private double smoothedSpeed;
    private double lastX = Double.NaN;
    private double lastZ = Double.NaN;
    private long   sessionStart = System.currentTimeMillis();

    public MetersHud(HudModule hud, ThemeManager themes) {
        super("meters", 0.006f, 0.86f, () -> hud.isEnabled() && hud.meters.get());
        this.hud = hud;
        this.themes = themes;
    }

    private record Row(String label, String value) {
    }

    // ── model ────────────────────────────────────────────────────────────────

    private List<Row> rows(MinecraftClient client) {
        List<Row> out = new ArrayList<>();
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;

        if (hud.meterSpeed.get()) {
            out.add(new Row("Speed", speed(player)));
        }
        if (hud.meterBiome.get()) {
            out.add(new Row("Biome", biome(world, player)));
        }
        if (hud.meterLight.get()) {
            out.add(new Row("Light", light(world, player)));
        }
        if (hud.meterTime.get()) {
            out.add(new Row("Time", timeOfDay(world)));
        }
        if (hud.meterSession.get()) {
            out.add(new Row("Session", elapsed(System.currentTimeMillis() - sessionStart)));
        }
        if (hud.meterPortal.get()) {
            out.add(new Row(portalLabel(world), portal(world, player)));
        }
        return out;
    }

    // ── readouts ─────────────────────────────────────────────────────────────

    /**
     * Horizontal speed in blocks per second.
     *
     * Taken from how far the player actually moved between frames rather than
     * from the velocity vector, which on a server-driven client reads zero for
     * anything that is not self-propelled — riding a boat, being pushed, or
     * flying an elytra all report honestly this way.
     */
    private String speed(ClientPlayerEntity player) {
        if (player == null) return null;
        double x = player.getX();
        double z = player.getZ();
        if (!Double.isNaN(lastX)) {
            double dx = x - lastX;
            double dz = z - lastZ;
            double step = Math.sqrt(dx * dx + dz * dz);
            // a teleport is not travel
            double perSecond = step > 16.0 ? smoothedSpeed : step * TPS;
            smoothedSpeed += (perSecond - smoothedSpeed) * 0.18;
        }
        lastX = x;
        lastZ = z;
        return String.format(Locale.ROOT, "%.1f b/s", smoothedSpeed);
    }

    private static String biome(ClientWorld world, ClientPlayerEntity player) {
        if (world == null || player == null) return null;
        try {
            Identifier id = world.getRegistryManager()
                    .getOrThrow(RegistryKeys.BIOME)
                    .getId(world.getBiome(player.getBlockPos()).value());
            return id == null ? null : prettify(id.getPath());
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Block light where the player is standing, which is what decides whether
     * something can spawn there.
     */
    private static String light(ClientWorld world, ClientPlayerEntity player) {
        if (world == null || player == null) return null;
        BlockPos pos = player.getBlockPos();
        int block = world.getLightLevel(net.minecraft.world.LightType.BLOCK, pos);
        int sky = world.getLightLevel(net.minecraft.world.LightType.SKY, pos);
        return block + " blk / " + sky + " sky";
    }

    private static String timeOfDay(ClientWorld world) {
        if (world == null) return null;
        long ticks = world.getTimeOfDay() % 24000L;
        // Minecraft's day starts at 06:00, not midnight
        long minutes = (ticks * 60L / 1000L + 360L) % 1440L;
        return String.format(Locale.ROOT, "%02d:%02d", minutes / 60L, minutes % 60L);
    }

    /** Which way the conversion runs depends on the dimension you are in. */
    private static String portalLabel(ClientWorld world) {
        if (world != null && world.getRegistryKey() == World.NETHER) return "Overworld";
        return "Nether";
    }

    /**
     * The matching coordinate in the other dimension — the single most useful
     * arithmetic on an anarchy server, and the easiest to get wrong in your
     * head at four in the morning.
     */
    private static String portal(ClientWorld world, ClientPlayerEntity player) {
        if (world == null || player == null) return null;
        BlockPos pos = player.getBlockPos();
        boolean inNether = world.getRegistryKey() == World.NETHER;
        double factor = inNether ? NETHER_RATIO : 1.0 / NETHER_RATIO;
        long x = Math.round(pos.getX() * factor);
        long z = Math.round(pos.getZ() * factor);
        return x + ", " + z;
    }

    private static String elapsed(long millis) {
        long seconds = millis / 1000L;
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        if (hours > 0L) return hours + "h " + minutes + "m";
        if (minutes > 0L) return minutes + "m";
        return seconds + "s";
    }

    /** "deep_dark" reads better as "Deep Dark". */
    private static String prettify(String path) {
        String[] parts = path.split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    // ── measurement ──────────────────────────────────────────────────────────

    @Override
    public float measureWidth(NVGRenderer nvg) {
        MinecraftClient client = MinecraftClient.getInstance();
        float widest = 0.0f;
        for (Row row : rows(client)) {
            float w = nvg.textWidth(row.label(), LABEL_FONT)
                    + 14.0f
                    + nvg.textWidth(row.value() == null ? "—" : row.value(), VALUE_FONT);
            widest = Math.max(widest, w);
        }
        return Math.max(MIN_WIDTH, widest + PAD * 2.0f);
    }

    @Override
    public float measureHeight(NVGRenderer nvg) {
        int count = rows(MinecraftClient.getInstance()).size();
        if (count == 0) return 0.0f;
        return PAD * 2.0f + count * ROW_H;
    }

    // ── render ───────────────────────────────────────────────────────────────

    @Override
    public void render(NVGRenderer nvg, float x, float y, float w, float h) {
        MinecraftClient client = MinecraftClient.getInstance();
        List<Row> rows = rows(client);
        if (rows.isEmpty()) return;

        Theme th = themes.current();
        HudSurface.panel(nvg, x, y, w, h, 8.0f, th);
        nvg.rectOutline(x, y, w, h, 8.0f, 1.0f, Colors.withAlpha(th.accent(), 0.30f));

        float rowY = y + PAD;
        for (Row row : rows) {
            float centre = rowY + ROW_H / 2.0f;
            nvg.text(row.label(), x + PAD, centre, LABEL_FONT, th.textMuted());

            String value = row.value() == null ? "—" : row.value();
            int colour = row.value() == null ? th.textDisabled() : th.textPrimary();
            float vw = nvg.textWidth(value, VALUE_FONT);
            nvg.text(value, x + w - PAD - vw, centre, VALUE_FONT, colour);

            rowY += ROW_H;
        }
    }
}
