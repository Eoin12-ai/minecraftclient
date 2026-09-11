package dev.kryptic.stats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import java.util.Locale;

/**
 * The in-game date and time.
 *
 * Minecraft keeps two clocks. {@code getTime} counts every tick the world has
 * ever run and is what a day number comes from; {@code getTimeOfDay} is that
 * value modulo a day, and is what the sun goes by. A server can set the second
 * without touching the first, so the day number has to come from the first —
 * reading the day out of the time of day would reset it every dawn.
 *
 * Tick 0 is 06:00, not midnight, which is the offset every conversion here
 * carries.
 */
public final class WorldClock {

    private static final long TICKS_PER_DAY = 24000L;
    private static final long DAWN_OFFSET = 6000L;

    private WorldClock() {
    }

    /** Which in-game day it is, counting the first as day 1. */
    public static String day(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) return null;
        long day = Math.floorDiv(world.getTime(), TICKS_PER_DAY) + 1L;
        return String.format(Locale.ROOT, "%,d", day);
    }

    /** The time of day as a 24-hour clock. */
    public static String clock(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) return null;
        long tick = Math.floorMod(world.getTimeOfDay() + DAWN_OFFSET, TICKS_PER_DAY);
        long minutesTotal = tick * 24L * 60L / TICKS_PER_DAY;
        return String.format(Locale.ROOT, "%02d:%02d", minutesTotal / 60L, minutesTotal % 60L);
    }

    /**
     * What that time means: whether it is safe out.
     *
     * The boundaries are the ones the game actually uses — mobs spawn from
     * 13000 and burn from 23000 — rather than an even split of the day.
     */
    public static String phase(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) return null;
        long tick = Math.floorMod(world.getTimeOfDay(), TICKS_PER_DAY);
        if (tick < 1000L) return "Sunrise";
        if (tick < 11000L) return "Day";
        if (tick < 13000L) return "Sunset";
        if (tick < 23000L) return "Night";
        return "Sunrise";
    }
}
