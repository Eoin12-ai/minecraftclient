package dev.kryptic.discord;

import com.google.gson.JsonObject;
import dev.kryptic.KrypticClient;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Owns the single background thread that talks to Discord.
 *
 * The game thread only ever calls {@link #publish}, which swaps an immutable
 * snapshot into an atomic reference. The worker wakes on a fixed cadence,
 * connects when Discord is available, and writes a SET_ACTIVITY frame only when
 * the snapshot differs from the last one it sent.
 *
 * Discord rate-limits presence updates to roughly five per twenty seconds, so
 * {@link #MIN_SEND_INTERVAL_MS} is the floor between writes even if the
 * snapshot changes every tick. Connection attempts back off up to
 * {@link #MAX_RETRY_MS} so a closed Discord costs almost nothing.
 */
public final class DiscordPresenceService {

    /** Discord's own Minecraft application, so the art assets already exist. */
    public static final String DEFAULT_APP_ID = "1096504064027787305";

    private static final long POLL_INTERVAL_MS     = 1_000L;
    private static final long MIN_SEND_INTERVAL_MS = 4_000L;
    private static final long BASE_RETRY_MS        = 5_000L;
    private static final long MAX_RETRY_MS         = 60_000L;

    private final AtomicReference<PresenceState> pending = new AtomicReference<>(PresenceState.EMPTY);

    private volatile String appId = DEFAULT_APP_ID;
    private volatile boolean running;
    private volatile boolean connected;
    private Thread worker;

    private DiscordIpc ipc;
    private PresenceState lastSent;
    private long lastSendAt;
    private long retryDelay = BASE_RETRY_MS;
    private long nextAttemptAt;
    private boolean loggedFailure;

    // ── lifecycle, called from the game thread ───────────────────────────────

    public synchronized void start(String applicationId) {
        if (applicationId != null && !applicationId.isBlank()) {
            String trimmed = applicationId.strip();
            if (!trimmed.equals(appId)) {
                appId = trimmed;
                // a different application means a different connection
                dropConnection();
            }
        }
        if (running) return;
        running = true;
        worker = new Thread(this::run, "krypticclient-discord-rpc");
        worker.setDaemon(true);
        worker.start();
    }

    public synchronized void stop() {
        running = false;
        pending.set(PresenceState.EMPTY);
        if (worker != null) worker.interrupt();
        worker = null;
    }

    /** Hands the worker the presence it should show next. Never blocks. */
    public void publish(PresenceState state) {
        pending.set(state == null ? PresenceState.EMPTY : state);
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isRunning() {
        return running;
    }

    // ── worker ───────────────────────────────────────────────────────────────

    private void run() {
        try {
            while (running) {
                try {
                    tickConnection();
                } catch (Exception e) {
                    dropConnection();
                }
                Thread.sleep(POLL_INTERVAL_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            dropConnection();
            connected = false;
        }
    }

    private void tickConnection() throws Exception {
        long now = System.currentTimeMillis();

        if (ipc == null) {
            if (now < nextAttemptAt) return;
            ipc = DiscordIpc.connect(appId);
            if (ipc == null) {
                connected = false;
                nextAttemptAt = now + retryDelay;
                retryDelay = Math.min(MAX_RETRY_MS, retryDelay * 2L);
                if (!loggedFailure) {
                    KrypticClient.LOGGER.info("Discord presence: no Discord client responding, will keep retrying");
                    loggedFailure = true;
                }
                return;
            }
            connected = true;
            retryDelay = BASE_RETRY_MS;
            loggedFailure = false;
            lastSent = null;
            KrypticClient.LOGGER.info("Discord presence: connected");
        }

        PresenceState want = pending.get();
        if (want.sameAs(lastSent)) return;
        if (now - lastSendAt < MIN_SEND_INTERVAL_MS) return;

        ipc.send(DiscordIpc.OP_FRAME, setActivityFrame(want));
        lastSent = want;
        lastSendAt = now;
    }

    private static JsonObject setActivityFrame(PresenceState s) {
        JsonObject args = new JsonObject();
        args.addProperty("pid", ProcessHandle.current().pid());

        if (s.isBlank()) {
            // a null activity is how Discord is told to clear the presence
            args.add("activity", null);
        } else {
            JsonObject activity = new JsonObject();
            if (!s.details().isEmpty()) activity.addProperty("details", s.details());
            if (!s.state().isEmpty()) activity.addProperty("state", s.state());

            if (s.startEpochMillis() > 0L) {
                JsonObject ts = new JsonObject();
                ts.addProperty("start", s.startEpochMillis());
                activity.add("timestamps", ts);
            }

            JsonObject assets = new JsonObject();
            if (!s.largeImage().isEmpty()) assets.addProperty("large_image", s.largeImage());
            if (!s.largeText().isEmpty()) assets.addProperty("large_text", s.largeText());
            if (!s.smallImage().isEmpty()) assets.addProperty("small_image", s.smallImage());
            if (!s.smallText().isEmpty()) assets.addProperty("small_text", s.smallText());
            if (assets.size() > 0) activity.add("assets", assets);

            args.add("activity", activity);
        }

        JsonObject frame = new JsonObject();
        frame.addProperty("cmd", "SET_ACTIVITY");
        frame.add("args", args);
        frame.addProperty("nonce", UUID.randomUUID().toString());
        return frame;
    }

    private void dropConnection() {
        if (ipc != null) {
            ipc.closeQuietly();
            ipc = null;
        }
        connected = false;
        lastSent = null;
    }
}
