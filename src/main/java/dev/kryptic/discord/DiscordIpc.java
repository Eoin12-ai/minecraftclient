package dev.kryptic.discord;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Minimal Discord IPC client — enough to hold a connection and push Rich
 * Presence updates, with no third-party dependency.
 *
 * Discord's local IPC is a length-prefixed JSON protocol over a per-user pipe:
 * a named pipe on Windows, a Unix domain socket everywhere else. Each frame is
 * a little-endian int32 opcode, a little-endian int32 payload length, then that
 * many bytes of UTF-8 JSON. The client opens the pipe, sends a HANDSHAKE naming
 * the application id, and from then on sends FRAME payloads.
 *
 * Discord numbers its pipes 0-9 so several clients (stable, PTB, canary) can run
 * at once, so {@link #connect} walks the candidate directories and indices and
 * takes the first that answers.
 *
 * Nothing here touches the game thread; {@link DiscordPresenceService} owns the
 * one thread that calls into this class.
 */
final class DiscordIpc implements Closeable {

    // ── protocol ─────────────────────────────────────────────────────────────
    static final int OP_HANDSHAKE = 0;
    static final int OP_FRAME     = 1;
    static final int OP_CLOSE     = 2;
    static final int OP_PING      = 3;
    static final int OP_PONG      = 4;

    /** Refuse absurd frames rather than trying to allocate them. */
    private static final int MAX_FRAME = 1 << 20;

    private final Transport transport;

    private DiscordIpc(Transport transport) {
        this.transport = transport;
    }

    // ── connect ──────────────────────────────────────────────────────────────

    /**
     * Opens the first Discord pipe that accepts a handshake for {@code appId}.
     *
     * @return a connected client, or null when Discord is not running
     */
    static DiscordIpc connect(String appId) {
        boolean windows = System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT).contains("win");
        for (int i = 0; i < 10; i++) {
            Transport t = windows ? openWindows(i) : openUnix(i);
            if (t == null) continue;
            DiscordIpc ipc = new DiscordIpc(t);
            if (ipc.handshake(appId)) return ipc;
            ipc.closeQuietly();
        }
        return null;
    }

    private static Transport openWindows(int index) {
        try {
            RandomAccessFile pipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + index, "rw");
            return new WindowsTransport(pipe);
        } catch (Exception e) {
            return null;
        }
    }

    private static Transport openUnix(int index) {
        for (Path dir : unixDirs()) {
            Path sock = dir.resolve("discord-ipc-" + index);
            try {
                if (!Files.exists(sock)) continue;
                SocketChannel ch = SocketChannel.open(StandardProtocolFamily.UNIX);
                ch.connect(UnixDomainSocketAddress.of(sock));
                return new UnixTransport(ch);
            } catch (Exception e) {
                // next candidate
            }
        }
        return null;
    }

    /** Runtime dir, plus the subdirectories Flatpak and Snap installs use. */
    private static List<Path> unixDirs() {
        List<Path> roots = new ArrayList<>();
        for (String key : new String[]{"XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP"}) {
            String v = System.getenv(key);
            if (v != null && !v.isBlank()) roots.add(Path.of(v));
        }
        roots.add(Path.of("/tmp"));

        List<Path> out = new ArrayList<>();
        for (Path root : roots) {
            out.add(root);
            out.add(root.resolve("app").resolve("com.discordapp.Discord"));
            out.add(root.resolve("snap.discord"));
        }
        return out;
    }

    // ── handshake ────────────────────────────────────────────────────────────

    private boolean handshake(String appId) {
        try {
            JsonObject hello = new JsonObject();
            hello.addProperty("v", 1);
            hello.addProperty("client_id", appId);
            send(OP_HANDSHAKE, hello);

            Frame reply = read();
            // Discord answers READY on op 1; op 2 is a refusal carrying a reason.
            return reply != null && reply.op == OP_FRAME;
        } catch (Exception e) {
            return false;
        }
    }

    // ── frames ───────────────────────────────────────────────────────────────

    void send(int op, JsonObject payload) throws IOException {
        byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(8 + body.length).order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(op);
        buf.putInt(body.length);
        buf.put(body);
        transport.write(buf.array());
    }

    /** Reads one frame, or null if the peer closed or sent something malformed. */
    Frame read() throws IOException {
        byte[] header = transport.readFully(8);
        if (header == null) return null;
        ByteBuffer hb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        int op  = hb.getInt();
        int len = hb.getInt();
        if (len < 0 || len > MAX_FRAME) return null;
        byte[] body = len == 0 ? new byte[0] : transport.readFully(len);
        if (body == null) return null;
        JsonObject json = null;
        try {
            json = JsonParser.parseString(new String(body, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            // a frame we cannot parse is still a frame; op alone is useful
        }
        return new Frame(op, json);
    }

    @Override
    public void close() throws IOException {
        try {
            send(OP_CLOSE, new JsonObject());
        } catch (Exception ignored) {
            // the pipe may already be gone; closing is what matters
        }
        transport.close();
    }

    void closeQuietly() {
        try {
            close();
        } catch (Exception ignored) {
        }
    }

    record Frame(int op, JsonObject json) {
    }

    // ── transports ───────────────────────────────────────────────────────────

    private interface Transport extends Closeable {
        void write(byte[] data) throws IOException;

        /** Reads exactly {@code n} bytes, or returns null at end of stream. */
        byte[] readFully(int n) throws IOException;
    }

    private static final class WindowsTransport implements Transport {
        private final RandomAccessFile pipe;

        WindowsTransport(RandomAccessFile pipe) {
            this.pipe = pipe;
        }

        @Override
        public void write(byte[] data) throws IOException {
            pipe.write(data);
        }

        @Override
        public byte[] readFully(int n) throws IOException {
            byte[] buf = new byte[n];
            int read = 0;
            while (read < n) {
                int r = pipe.read(buf, read, n - read);
                if (r < 0) return null;
                read += r;
            }
            return buf;
        }

        @Override
        public void close() throws IOException {
            pipe.close();
        }
    }

    private static final class UnixTransport implements Transport {
        private final SocketChannel channel;

        UnixTransport(SocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public void write(byte[] data) throws IOException {
            ByteBuffer buf = ByteBuffer.wrap(data);
            while (buf.hasRemaining()) {
                if (channel.write(buf) < 0) throw new IOException("discord pipe closed");
            }
        }

        @Override
        public byte[] readFully(int n) throws IOException {
            ByteBuffer buf = ByteBuffer.allocate(n);
            while (buf.hasRemaining()) {
                if (channel.read(buf) < 0) return null;
            }
            return buf.array();
        }

        @Override
        public void close() throws IOException {
            channel.close();
        }
    }
}
