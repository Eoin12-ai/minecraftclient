package dev.sixseven.stats;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Session and per-server play statistics, measured client-side.
 *
 * Everything here is derived from what the client can see for itself — wall
 * clock time, the player's position each tick, and whether the player is alive
 * — so it stays correct on servers that keep no vanilla statistics at all.
 * Lifetime numbers are a separate concern and live in {@link VanillaStats}.
 *
 * Two scopes are kept:
 *
 *   session — reset when the client starts, or when the player changes server
 *   server  — accumulated across every visit to one address, saved in config
 *
 * Time is accumulated from wall-clock deltas rather than by counting ticks, so
 * a stuttering or throttled client does not quietly lose minutes. A tick gap
 * longer than {@link #MAX_TICK_GAP_MS} is treated as the client having been
 * paused or unfocused and is not counted at all.
 */
public final class StatsTracker {

    /** Ignore gaps longer than this; the client was paused, not playing. */
    private static final long MAX_TICK_GAP_MS = 5_000L;

    /** A single-tick move further than this is a teleport, not travel. */
    private static final double MAX_STEP_BLOCKS = 16.0;

    private static final String SINGLEPLAYER = "singleplayer";

    // ── session scope ────────────────────────────────────────────────────────
    private long   sessionMillis;
    private int    sessionDeaths;
    private double sessionBlocks;
    private long   sessionStartedAt = System.currentTimeMillis();

    // ── per-server scope ─────────────────────────────────────────────────────
    private final Map<String, ServerRecord> servers = new LinkedHashMap<>();

    // ── tick bookkeeping ─────────────────────────────────────────────────────
    private String  currentKey = "";
    private long    lastTickAt;
    private boolean wasAlive = true;
    private double  lastX, lastZ;
    private boolean hasLastPos;

    // ─────────────────────────────────────────────────────────────────────────

    /** Called every client tick while the stats module is enabled. */
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        long now = System.currentTimeMillis();

        if (player == null || client.world == null) {
            // out of a world: drop the anchors so re-entry does not book a huge
            // time gap or count the spawn jump as travel
            currentKey = "";
            lastTickAt = 0L;
            hasLastPos = false;
            return;
        }

        String key = serverKey(client);
        if (!key.equals(currentKey)) {
            onEnterServer(key, now);
            currentKey = key;
        }

        ServerRecord record = servers.computeIfAbsent(key, k -> new ServerRecord(now));

        // ── time ─────────────────────────────────────────────────────────────
        if (lastTickAt > 0L) {
            long delta = now - lastTickAt;
            if (delta > 0L && delta <= MAX_TICK_GAP_MS) {
                sessionMillis += delta;
                record.playMillis += delta;
            }
        }
        lastTickAt = now;
        record.lastSeen = now;

        // ── deaths ───────────────────────────────────────────────────────────
        boolean alive = player.isAlive();
        if (wasAlive && !alive) {
            sessionDeaths++;
            record.deaths++;
        }
        wasAlive = alive;

        // ── distance ─────────────────────────────────────────────────────────
        double x = player.getX();
        double z = player.getZ();
        if (hasLastPos) {
            double dx = x - lastX;
            double dz = z - lastZ;
            double step = Math.sqrt(dx * dx + dz * dz);
            if (step > 0.0 && step < MAX_STEP_BLOCKS) {
                sessionBlocks += step;
                record.blocksTravelled += step;
            }
        }
        lastX = x;
        lastZ = z;
        hasLastPos = true;
    }

    private void onEnterServer(String key, long now) {
        // a new address means a new session's worth of numbers
        sessionMillis = 0L;
        sessionDeaths = 0;
        sessionBlocks = 0.0;
        sessionStartedAt = now;
        lastTickAt = 0L;
        hasLastPos = false;
        wasAlive = true;

        ServerRecord record = servers.computeIfAbsent(key, k -> new ServerRecord(now));
        record.joins++;
        record.lastSeen = now;
    }

    /** Address of the current server, or a fixed key for singleplayer. */
    public static String serverKey(MinecraftClient client) {
        ServerInfo entry = client.getCurrentServerEntry();
        if (entry == null || entry.address == null || entry.address.isBlank()) return SINGLEPLAYER;
        return entry.address.toLowerCase(Locale.ROOT);
    }

    /** Human-readable label for the current server. */
    public static String serverLabel(MinecraftClient client) {
        String key = serverKey(client);
        return key.equals(SINGLEPLAYER) ? "Singleplayer" : key;
    }

    // ── reads ────────────────────────────────────────────────────────────────

    public long   sessionMillis()   { return sessionMillis; }
    public int    sessionDeaths()   { return sessionDeaths; }
    public double sessionBlocks()   { return sessionBlocks; }
    public long   sessionStarted()  { return sessionStartedAt; }

    public ServerRecord current() {
        return servers.getOrDefault(currentKey, ServerRecord.EMPTY);
    }

    public ServerRecord forKey(String key) {
        return servers.getOrDefault(key, ServerRecord.EMPTY);
    }

    public int trackedServers() {
        return servers.size();
    }

    /** Everything across every server ever tracked, summed on demand. */
    public ServerRecord allServers() {
        ServerRecord total = new ServerRecord(0L);
        for (ServerRecord r : servers.values()) {
            total.playMillis += r.playMillis;
            total.deaths += r.deaths;
            total.joins += r.joins;
            total.blocksTravelled += r.blocksTravelled;
        }
        return total;
    }

    /** Key of the server with the most recorded time, or "" when none. */
    public String mostPlayedKey() {
        String best = "";
        long bestMillis = -1L;
        for (Map.Entry<String, ServerRecord> e : servers.entrySet()) {
            if (e.getValue().playMillis > bestMillis) {
                bestMillis = e.getValue().playMillis;
                best = e.getKey();
            }
        }
        return best;
    }

    /** Wipes both scopes. Bound to the module's Reset switch. */
    public void resetAll() {
        servers.clear();
        sessionMillis = 0L;
        sessionDeaths = 0;
        sessionBlocks = 0.0;
        sessionStartedAt = System.currentTimeMillis();
        currentKey = "";
        lastTickAt = 0L;
        hasLastPos = false;
    }

    // ── persistence ──────────────────────────────────────────────────────────

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("v", 1);
        JsonObject byServer = new JsonObject();
        for (Map.Entry<String, ServerRecord> e : servers.entrySet()) {
            byServer.add(e.getKey(), e.getValue().toJson());
        }
        root.add("servers", byServer);
        return root;
    }

    public void fromJson(JsonObject root) {
        if (root == null || !root.has("servers") || !root.get("servers").isJsonObject()) return;
        servers.clear();
        JsonObject byServer = root.getAsJsonObject("servers");
        for (Map.Entry<String, JsonElement> e : byServer.entrySet()) {
            if (e.getValue() != null && e.getValue().isJsonObject()) {
                servers.put(e.getKey(), ServerRecord.fromJson(e.getValue().getAsJsonObject()));
            }
        }
    }

    // ── record ───────────────────────────────────────────────────────────────

    /** Everything remembered about one server address. */
    public static final class ServerRecord {
        public static final ServerRecord EMPTY = new ServerRecord(0L);

        public long   playMillis;
        public int    deaths;
        public int    joins;
        public double blocksTravelled;
        public long   firstSeen;
        public long   lastSeen;

        ServerRecord(long now) {
            this.firstSeen = now;
            this.lastSeen = now;
        }

        JsonObject toJson() {
            JsonObject o = new JsonObject();
            o.addProperty("playMillis", playMillis);
            o.addProperty("deaths", deaths);
            o.addProperty("joins", joins);
            o.addProperty("blocks", blocksTravelled);
            o.addProperty("firstSeen", firstSeen);
            o.addProperty("lastSeen", lastSeen);
            return o;
        }

        static ServerRecord fromJson(JsonObject o) {
            ServerRecord r = new ServerRecord(0L);
            if (o.has("playMillis")) r.playMillis = o.get("playMillis").getAsLong();
            if (o.has("deaths")) r.deaths = o.get("deaths").getAsInt();
            if (o.has("joins")) r.joins = o.get("joins").getAsInt();
            if (o.has("blocks")) r.blocksTravelled = o.get("blocks").getAsDouble();
            if (o.has("firstSeen")) r.firstSeen = o.get("firstSeen").getAsLong();
            if (o.has("lastSeen")) r.lastSeen = o.get("lastSeen").getAsLong();
            return r;
        }
    }
}
