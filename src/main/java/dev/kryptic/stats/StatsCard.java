package dev.kryptic.stats;

import dev.kryptic.module.client.StatsModule;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * What the stats card says, with nothing about how it looks.
 *
 * The rows used to be built inside the HUD component, which meant the card
 * could only ever exist in one place. Both the HUD and the menu panel read
 * from here now, so the two can never drift apart or need keeping in sync by
 * hand when a section is added.
 *
 * A {@code null} value renders as a dash wherever it is drawn. That is
 * deliberate and load bearing: lifetime figures are unavailable until the
 * server sends them, and a dash says so honestly where a zero would read as a
 * real count.
 */
public final class StatsCard {

    private StatsCard() {
    }

    /** One line of the card; a null {@code value} renders as a dash. */
    public record Row(String label, String value) {
    }

    /** A titled group of rows. */
    public record Section(String title, List<Row> rows) {
    }

    public static List<Section> sections(MinecraftClient client, StatsModule module) {
        List<Section> out = new ArrayList<>();
        StatsTracker tracker = module.tracker();

        if (module.world.get()) {
            List<Row> rows = new ArrayList<>();
            rows.add(new Row("Day", WorldClock.day(client)));
            rows.add(new Row("Time", WorldClock.clock(client)));
            rows.add(new Row("Phase", WorldClock.phase(client)));
            out.add(new Section("World", rows));
        }

        if (module.money.get()) {
            List<Row> rows = new ArrayList<>();
            rows.add(new Row("Balance", module.balance().formatted()));
            rows.add(new Row("Checked", module.balance().age()));
            out.add(new Section("Money", rows));
        }

        if (module.session.get()) {
            List<Row> rows = new ArrayList<>();
            rows.add(new Row("Time", duration(tracker.sessionMillis())));
            rows.add(new Row("Deaths", Integer.toString(tracker.sessionDeaths())));
            rows.add(new Row("Travelled", distance(tracker.sessionBlocks())));
            out.add(new Section("Session", rows));
        }

        if (module.server.get()) {
            StatsTracker.ServerRecord rec = tracker.current();
            List<Row> rows = new ArrayList<>();
            rows.add(new Row("Time", duration(rec.playMillis)));
            rows.add(new Row("Visits", Integer.toString(rec.joins)));
            rows.add(new Row("Deaths", Integer.toString(rec.deaths)));
            rows.add(new Row("Travelled", distance(rec.blocksTravelled)));
            out.add(new Section("This Server", rows));
        }

        if (module.allServers.get()) {
            StatsTracker.ServerRecord all = tracker.allServers();
            List<Row> rows = new ArrayList<>();
            rows.add(new Row("Time", duration(all.playMillis)));
            rows.add(new Row("Servers", Integer.toString(tracker.trackedServers())));
            rows.add(new Row("Deaths", Integer.toString(all.deaths)));
            rows.add(new Row("Travelled", distance(all.blocksTravelled)));
            String most = tracker.mostPlayedKey();
            if (!most.isEmpty()) rows.add(new Row("Most played", most));
            out.add(new Section("All Servers", rows));
        }

        if (module.lifetime.get()) {
            List<Row> rows = new ArrayList<>();
            if (!VanillaStats.available(client)) {
                rows.add(new Row("Waiting on server", null));
            } else {
                rows.add(new Row("Played", ticks(VanillaStats.playTimeTicks(client))));
                rows.add(new Row("Days", count(VanillaStats.daysPlayed(client))));
                rows.add(new Row("Deaths", count(VanillaStats.deaths(client))));
                rows.add(new Row("Mob kills", count(VanillaStats.mobKills(client))));
                rows.add(new Row("Player kills", count(VanillaStats.playerKills(client))));
                rows.add(new Row("K/D", kd(VanillaStats.lifetimeKd(client))));
                rows.add(new Row("Walked", centimetres(VanillaStats.walkedCm(client))));
                rows.add(new Row("Jumps", count(VanillaStats.jumps(client))));
                rows.add(new Row("Damage out", halfHearts(VanillaStats.damageDealt(client))));
                rows.add(new Row("Damage in", halfHearts(VanillaStats.damageTaken(client))));
            }
            out.add(new Section("Lifetime", rows));
        }

        return out;
    }

    // ── formatting ───────────────────────────────────────────────────────────

    /** "6d 4h", "1h 23m", "45m", "12s". Never a bare zero-padded clock. */
    public static String duration(long millis) {
        if (millis < 0L) return null;
        long seconds = millis / 1000L;
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        if (days > 0L) return days + "d " + hours + "h";
        if (hours > 0L) return hours + "h " + minutes + "m";
        if (minutes > 0L) return minutes + "m";
        return seconds + "s";
    }

    public static String ticks(int t) {
        return t < 0 ? null : duration((long) t * 1000L / VanillaStats.TICKS_PER_SECOND);
    }

    /** Blocks travelled, shown in km once it stops being readable in blocks. */
    public static String distance(double blocks) {
        if (blocks < 0.0) return null;
        if (blocks >= 1000.0) return String.format(Locale.ROOT, "%.1f km", blocks / 1000.0);
        return Math.round(blocks) + " m";
    }

    public static String centimetres(int cm) {
        return cm < 0 ? null : distance(cm / 100.0);
    }

    /** Minecraft records damage in tenths of a heart. */
    public static String halfHearts(int tenths) {
        if (tenths < 0) return null;
        return count(Math.round(tenths / 10.0f));
    }

    public static String count(int value) {
        if (value < 0) return null;
        return String.format(Locale.ROOT, "%,d", value);
    }

    public static String kd(float value) {
        if (value < 0.0f) return null;
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
