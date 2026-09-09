package dev.kryptic.module.client;

import dev.kryptic.KrypticClient;
import dev.kryptic.discord.DiscordPresenceService;
import dev.kryptic.discord.PresenceState;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientWorld;

import java.util.Locale;

/**
 * Discord Rich Presence.
 *
 * The two visible lines are templates, so the player decides what shows rather
 * than picking from a fixed list. Supported placeholders:
 *
 * <pre>
 *   {server}     server address, or Singleplayer
 *   {players}    players currently online
 *   {dimension}  overworld / the_nether / the_end, prettified
 *   {health}     current hearts, rounded
 *   {coords}     block position
 *   {fps}        frames per second
 *   {username}   the local player's name
 *   {client}     the client name
 *   {version}    the client version
 * </pre>
 *
 * "Hide Server IP" defaults to on. A presence line is visible to everyone in
 * the player's Discord, and a server address is the kind of thing people
 * usually do not mean to broadcast, so opting in is the safer default.
 *
 * The module only assembles a {@link PresenceState} on the game thread; the
 * socket work happens on {@link DiscordPresenceService}'s own thread.
 */
public class DiscordPresenceModule extends Module {

    private static final int UPDATE_INTERVAL_TICKS = 20;   // once a second

    public final StringSetting line1 = this.addSetting(new StringSetting(
            "Line 1", "Top line. Placeholders: {server} {players} {dimension} {health} {coords} {fps} {username} {client} {version}",
            "{server}", 96, "{server}"));

    public final StringSetting line2 = this.addSetting(new StringSetting(
            "Line 2", "Second line, same placeholders as Line 1",
            "{dimension} · {players} online", 96, "{dimension}"));

    public final BooleanSetting hideServerIp = this.addSetting(new BooleanSetting(
            "Hide Server IP", "Show \"Multiplayer\" instead of the real address", true));

    public final BooleanSetting elapsed = this.addSetting(new BooleanSetting(
            "Elapsed Time", "Count up from when the presence was switched on", true));

    public final BooleanSetting menuPresence = this.addSetting(new BooleanSetting(
            "Show In Menus", "Keep the presence up while not in a world", true));

    public final StringSetting largeImage = this.addSetting(new StringSetting(
            "Large Image", "Asset key from the Discord application", "default", 64, "default"));

    public final StringSetting appId = this.addSetting(new StringSetting(
            "Application ID", "Discord application the presence is published as",
            DiscordPresenceService.DEFAULT_APP_ID, 32, DiscordPresenceService.DEFAULT_APP_ID));

    private long startedAt;
    private int tickCounter;
    private String lastAppId = "";

    public DiscordPresenceModule() {
        super("DiscordRPC", "Show what you are playing in your Discord profile", Category.CLIENT);
    }

    // ── lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onEnable() {
        startedAt = System.currentTimeMillis();
        tickCounter = 0;
        lastAppId = appId.get();
        DiscordPresenceService svc = service();
        if (svc == null) return;
        svc.start(lastAppId);
        svc.publish(build());
    }

    @Override
    protected void onDisable() {
        DiscordPresenceService svc = service();
        if (svc == null) return;
        svc.publish(PresenceState.EMPTY);
        svc.stop();
    }

    @Override
    public void onTick() {
        DiscordPresenceService svc = service();
        if (svc == null) return;

        // an edited application id needs a fresh connection
        String id = appId.get();
        if (id != null && !id.equals(lastAppId)) {
            lastAppId = id;
            svc.stop();
            svc.start(id);
        }

        if (++tickCounter < UPDATE_INTERVAL_TICKS) return;
        tickCounter = 0;
        svc.publish(build());
    }

    /**
     * The service is created during client init. A module can in principle be
     * enabled by a config load before that field is assigned, so every call
     * site tolerates null rather than assuming an order of construction.
     */
    private DiscordPresenceService service() {
        return KrypticClient.discord();
    }

    /** True when the worker currently holds a live Discord connection. */
    public boolean isConnected() {
        DiscordPresenceService svc = service();
        return svc != null && svc.isConnected();
    }

    // ── presence assembly ────────────────────────────────────────────────────

    private PresenceState build() {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean inWorld = client.world != null && client.player != null;
        if (!inWorld && !menuPresence.get()) return PresenceState.EMPTY;

        String details = inWorld ? expand(line1.get(), client) : "In the menus";
        String state   = inWorld ? expand(line2.get(), client) : "";
        long start     = elapsed.get() ? startedAt : 0L;

        return new PresenceState(
                details,
                state,
                start,
                largeImage.get(),
                KrypticClient.NAME + " " + KrypticClient.VERSION,
                "",
                "");
    }

    private String expand(String template, MinecraftClient client) {
        if (template == null || template.isEmpty()) return "";
        String out = template;
        if (out.contains("{server}"))    out = out.replace("{server}", serverLabel(client));
        if (out.contains("{players}"))   out = out.replace("{players}", playerCount(client));
        if (out.contains("{dimension}")) out = out.replace("{dimension}", dimensionLabel(client));
        if (out.contains("{health}"))    out = out.replace("{health}", health(client));
        if (out.contains("{coords}"))    out = out.replace("{coords}", coords(client));
        if (out.contains("{fps}"))       out = out.replace("{fps}", Integer.toString(client.getCurrentFps()));
        if (out.contains("{username}"))  out = out.replace("{username}", username(client));
        if (out.contains("{client}"))    out = out.replace("{client}", KrypticClient.NAME);
        if (out.contains("{version}"))   out = out.replace("{version}", KrypticClient.VERSION);
        return out;
    }

    private String serverLabel(MinecraftClient client) {
        ServerInfo entry = client.getCurrentServerEntry();
        if (entry == null) return "Singleplayer";
        if (hideServerIp.get()) return "Multiplayer";
        return entry.address == null ? "Multiplayer" : entry.address;
    }

    private String playerCount(MinecraftClient client) {
        try {
            if (client.getNetworkHandler() == null) return "0";
            return Integer.toString(client.getNetworkHandler().getPlayerList().size());
        } catch (Exception e) {
            return "0";
        }
    }

    private String dimensionLabel(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) return "Menus";
        String path = world.getRegistryKey().getValue().getPath();
        return switch (path) {
            case "overworld"  -> "Overworld";
            case "the_nether" -> "Nether";
            case "the_end"    -> "The End";
            default -> prettify(path);
        };
    }

    private static String prettify(String raw) {
        String cleaned = raw.replace('_', ' ').strip();
        if (cleaned.isEmpty()) return "Unknown";
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1).toLowerCase(Locale.ROOT);
    }

    private String health(MinecraftClient client) {
        if (client.player == null) return "0";
        return Integer.toString(Math.round(client.player.getHealth()));
    }

    private String coords(MinecraftClient client) {
        if (client.player == null) return "?";
        return client.player.getBlockPos().getX()
                + ", " + client.player.getBlockPos().getY()
                + ", " + client.player.getBlockPos().getZ();
    }

    private String username(MinecraftClient client) {
        try {
            return client.player == null ? "" : client.player.getGameProfile().name();
        } catch (Exception e) {
            return "";
        }
    }
}
