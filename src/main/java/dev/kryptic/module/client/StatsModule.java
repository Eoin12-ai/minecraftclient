package dev.kryptic.module.client;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.stats.StatsTracker;
import dev.kryptic.stats.VanillaStats;
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

    public final BooleanSetting world = this.addSetting(new BooleanSetting(
            "World", "The in-game day, clock and whether it is safe out", true));

    public final BooleanSetting money = this.addSetting(new BooleanSetting(
            "Money", "Your balance on this server, as the server last reported it", true));

    public final BooleanSetting askBalance = this.addSetting(new BooleanSetting(
            "Ask For Balance", "Run the balance command periodically to keep the figure fresh", false));

    public final SliderSetting balanceInterval = this.addSetting(new SliderSetting(
            "Ask Every", "Minutes between balance checks", 5.0, 1.0, 30.0, 1.0, "m"));

    public final dev.kryptic.settings.StringSetting balanceCommand = this.addSetting(
            new dev.kryptic.settings.StringSetting(
            "Balance Command", "The command that asks the server for your balance",
            "bal", 32, "bal"));

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
            "Refresh Every", "Seconds between statistics requests", 30.0, 10.0, 300.0, 5.0, "s"));

    public final BooleanSetting reset = this.addSetting(new BooleanSetting(
            "Reset All", "Switch on to wipe every recorded figure, then it switches back off", false));

    private final StatsTracker tracker = new StatsTracker();
    private final dev.kryptic.stats.BalanceWatcher balance = new dev.kryptic.stats.BalanceWatcher();
    private long lastRequestAt;
    private long lastBalanceAskAt;

    public StatsModule() {
        super("Stats", "Your skin, session, server and lifetime statistics", Category.MISC);
    }

    public StatsTracker tracker() {
        return tracker;
    }

    public dev.kryptic.stats.BalanceWatcher balance() {
        return balance;
    }

    /**
     * Offers a chat line to the balance watcher.
     *
     * Fake Stats answers the balance command itself with a made-up figure, so
     * while that is switched on the real balance is left alone rather than
     * being overwritten by the fake one.
     */
    public void onChatMessage(String plain) {
        if (!money.get()) return;
        var modules = KrypticClient.modules();
        if (modules != null && modules.fakeStats != null
                && modules.fakeStats.isEnabled() && modules.fakeStats.balanceCommand.get()) {
            return;
        }
        balance.onChat(plain);
    }

    @Override
    protected void onEnable() {
        lastRequestAt = 0L;
        lastBalanceAskAt = 0L;
    }

    @Override
    public void onTick() {
        // the reset switch acts as a button: it fires once and pops back up
        if (reset.get()) {
            tracker.resetAll();
            balance.clear();
            reset.set(Boolean.FALSE);
            KrypticClient.LOGGER.info("Stats: all recorded figures cleared");
        }

        tracker.tick();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) return;

        long now = System.currentTimeMillis();
        askForBalance(client, now);

        if (!requestStats.get() || !lifetime.get()) return;
        long interval = (long) (requestInterval.getFloat() * 1000.0f);
        if (now - lastRequestAt < interval) return;
        lastRequestAt = now;
        VanillaStats.request(client);
    }

    /**
     * Runs the balance command on a timer, when asked to.
     *
     * Off by default, and never faster than a minute: this sends a real command
     * as you, and a client that spams one at a server is a client that gets its
     * user muted. The first ask waits a full interval after joining rather than
     * firing the moment the world loads.
     */
    private void askForBalance(MinecraftClient client, long now) {
        if (!money.get() || !askBalance.get()) return;

        String command = balanceCommand.get().trim();
        if (command.isEmpty()) return;
        if (command.startsWith("/")) command = command.substring(1);

        long interval = (long) (balanceInterval.getFloat() * 60_000.0f);
        if (lastBalanceAskAt == 0L) {
            lastBalanceAskAt = now;
            return;
        }
        if (now - lastBalanceAskAt < interval) return;
        lastBalanceAskAt = now;

        try {
            client.getNetworkHandler().sendChatCommand(command);
        } catch (Exception ignored) {
            // a server that refuses the command is not worth a crash
        }
    }
}
