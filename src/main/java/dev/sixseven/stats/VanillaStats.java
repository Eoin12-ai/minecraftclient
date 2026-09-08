package dev.sixseven.stats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

/**
 * Reads Minecraft's own lifetime statistics — the numbers behind the vanilla
 * Statistics screen.
 *
 * There is a real caveat worth understanding: in multiplayer the client holds
 * no statistics until the server sends them, which vanilla only does in reply
 * to a request. Opening the Statistics screen is what normally triggers that.
 * {@link #request} sends the same packet so the HUD can fill in without the
 * player opening a screen, and {@link #available} reports honestly whether
 * anything has arrived yet rather than showing a confident zero.
 *
 * Servers that keep no statistics, and proxies that drop the reply, will leave
 * these permanently unavailable. That is a property of the server, not a bug
 * here, and the HUD renders a dash for it.
 *
 * Every read is wrapped: a stat missing from a modded or trimmed registry
 * returns -1 rather than propagating out into the render loop.
 */
public final class VanillaStats {

    private VanillaStats() {
    }

    /** Ticks per real-world second, for turning PLAY_TIME into a duration. */
    public static final int TICKS_PER_SECOND = 20;

    // ── access ───────────────────────────────────────────────────────────────

    private static StatHandler handler(MinecraftClient client) {
        return client.player == null ? null : client.player.getStatHandler();
    }

    /** True once the client actually holds statistics worth showing. */
    public static boolean available(MinecraftClient client) {
        return playTimeTicks(client) > 0 || deaths(client) > 0;
    }

    /**
     * Asks the server to send this player's statistics, the same request the
     * vanilla Statistics screen makes when it opens.
     */
    public static void request(MinecraftClient client) {
        try {
            if (client.getNetworkHandler() == null) return;
            client.getNetworkHandler().sendPacket(
                    new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
        } catch (Throwable ignored) {
            // a server that refuses the request simply leaves stats unavailable
        }
    }

    private static int custom(MinecraftClient client, Identifier id) {
        try {
            StatHandler h = handler(client);
            if (h == null) return -1;
            return h.getStat(Stats.CUSTOM.getOrCreateStat(id));
        } catch (Throwable t) {
            return -1;
        }
    }

    // ── individual figures ───────────────────────────────────────────────────

    public static int playTimeTicks(MinecraftClient client) {
        return custom(client, Stats.PLAY_TIME);
    }

    public static int deaths(MinecraftClient client) {
        return custom(client, Stats.DEATHS);
    }

    public static int mobKills(MinecraftClient client) {
        return custom(client, Stats.MOB_KILLS);
    }

    public static int playerKills(MinecraftClient client) {
        return custom(client, Stats.PLAYER_KILLS);
    }

    public static int jumps(MinecraftClient client) {
        return custom(client, Stats.JUMP);
    }

    /** Distance walked, in centimetres, as Minecraft stores it. */
    public static int walkedCm(MinecraftClient client) {
        return custom(client, Stats.WALK_ONE_CM);
    }

    /** Damage dealt, in tenths of a heart. */
    public static int damageDealt(MinecraftClient client) {
        return custom(client, Stats.DAMAGE_DEALT);
    }

    /** Damage taken, in tenths of a heart. */
    public static int damageTaken(MinecraftClient client) {
        return custom(client, Stats.DAMAGE_TAKEN);
    }

    public static int timeSinceDeathTicks(MinecraftClient client) {
        return custom(client, Stats.TIME_SINCE_DEATH);
    }

    // ── derived ──────────────────────────────────────────────────────────────

    /** Whole days of recorded play time, or -1 when unavailable. */
    public static int daysPlayed(MinecraftClient client) {
        int ticks = playTimeTicks(client);
        if (ticks < 0) return -1;
        return ticks / (TICKS_PER_SECOND * 60 * 60 * 24);
    }

    /** Kills per death across the account's lifetime, or -1 when unavailable. */
    public static float lifetimeKd(MinecraftClient client) {
        int kills = playerKills(client);
        int deaths = deaths(client);
        if (kills < 0 || deaths < 0) return -1.0f;
        return deaths == 0 ? kills : (float) kills / (float) deaths;
    }
}
