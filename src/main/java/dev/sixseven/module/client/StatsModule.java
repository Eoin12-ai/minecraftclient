package dev.sixseven.module.client;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.ModeSetting;
import dev.sixseven.settings.SliderSetting;
import dev.sixseven.stats.StatsTracker;
import dev.sixseven.stats.VanillaStats;
import net.minecraft.client.MinecraftClient;

/**
 * The stats card: who you are, how long you have played here, and what
 * Minecraft's own records say about your account.
 *
 * Three scopes, each switchable:
 *
 *   Session   this sitting on this server, measured client-side
 *   Server    every visit to this address, kept across restarts
 *   Lifetime  Minecraft's own statistics for the account
 *
 * Lifetime figures need the server to send them. "Request Stats" asks for them
 * on a timer using the same packet the vanilla Statistics screen sends when it
 * opens. Some servers keep no statistics and will never answer; the card shows
 * a dash rather than a misleading zero when that happens.
 */
public class StatsModule extends Module {

    public final BooleanSetting skin = this.addSetting(new BooleanSetting(
            "Skin", "Show your head from your current skin", true));

    public final ModeSetting skinStyle = this.addSetting(new ModeSetting(
            "Skin Style", "How the skin is drawn", "Head", "Head", "Bust"));

    public final BooleanSetting session = this.addSetting(new BooleanSetting(
            "Session", "Time, deaths and distance for this sitting", true));

    public final BooleanSetting server = this.addSetting(new BooleanSetting(
            "Server", "Totals for the server you are on, across all visits", true));

    public final BooleanSetting lifetime = this.addSetting(new BooleanSetting(
            "Lifetime", "Minecraft's own statistics for your account", true));

    public final BooleanSetting allServers = this.addSetting(new BooleanSetting(
            "All Servers", "Totals across every server the client has tracked", true));

    public final BooleanSetting requestStats = this.addSetting(new BooleanSetting(
            "Request Stats", "Ask the server for your statistics periodically", true));

    public final SliderSetting requestInterval = this.addSetting(new SliderSetting(
            "Request Every", "Seconds between statistics requests", 30.0, 10.0, 300.0, 5.0, "s"));

    public final BooleanSetting reset = this.addSetting(new BooleanSetting(
            "Reset All", "Switch on to wipe every recorded figure, then it switches back off", false));

    private final StatsTracker tracker = new StatsTracker();
    private long lastRequestAt;

    public StatsModule() {
        super("Stats", "Your skin, session, server and lifetime statistics", Category.CLIENT);
    }

    public StatsTracker tracker() {
        return tracker;
    }

    @Override
    protected void onEnable() {
        lastRequestAt = 0L;
    }

    @Override
    public void onTick() {
        // the reset switch acts as a button: it fires once and pops back up
        if (reset.get()) {
            tracker.resetAll();
            reset.set(Boolean.FALSE);
            SixSevenClient.LOGGER.info("Stats: all recorded figures cleared");
        }

        tracker.tick();

        if (!requestStats.get() || !lifetime.get()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) return;

        long now = System.currentTimeMillis();
        long interval = (long) (requestInterval.getFloat() * 1000.0f);
        if (now - lastRequestAt < interval) return;
        lastRequestAt = now;
        VanillaStats.request(client);
    }
}
