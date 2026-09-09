package dev.kryptic.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kryptic.KrypticClient;
import dev.kryptic.stats.StatsTracker;
import net.minecraft.client.MinecraftClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Binds config slots to servers, so joining an address restores the setup you
 * last used there.
 *
 * Bindings are keyed by the same server key {@link StatsTracker} uses, which
 * keeps "this server" meaning one thing across the whole client rather than two
 * subsystems disagreeing about what counts as the same place.
 *
 * The binding stores the slot index rather than a copy of the settings. A slot
 * edited later is therefore picked up by every server bound to it, which is
 * usually what people want — one "anarchy" setup shared by several addresses —
 * and it means this class never has to duplicate config data.
 */
public final class ServerConfigs {

    /** Server key -> config slot index. */
    private final Map<String, Integer> bindings = new LinkedHashMap<>();

    private String lastAppliedKey = "";

    // ── bindings ─────────────────────────────────────────────────────────────

    /** Slot bound to this key, or -1 when the server has no binding. */
    public int slotFor(String key) {
        Integer slot = bindings.get(key);
        return slot == null ? -1 : slot;
    }

    public void bind(String key, int slot) {
        if (key == null || key.isBlank()) return;
        if (slot < 0 || slot >= ConfigStore.SLOT_COUNT) return;
        bindings.put(key, slot);
    }

    public void unbind(String key) {
        bindings.remove(key);
    }

    public int count() {
        return bindings.size();
    }

    public Map<String, Integer> all() {
        return java.util.Collections.unmodifiableMap(bindings);
    }

    // ── the current server ───────────────────────────────────────────────────

    public static String currentKey() {
        return StatsTracker.serverKey(MinecraftClient.getInstance());
    }

    public int slotForCurrent() {
        return slotFor(currentKey());
    }

    /**
     * Applies the binding for the server just joined.
     *
     * Called on the join event, where the client is connected but the server
     * entry is already populated. Does nothing when there is no binding, when
     * the bound slot is empty, or when that slot is already active — activating
     * a config reloads every module, so it is not something to do redundantly.
     *
     * @return true when a config was actually switched
     */
    public boolean applyForCurrentServer() {
        ConfigStore store = KrypticClient.configStore();
        if (store == null) return false;

        String key = currentKey();
        int slot = slotFor(key);
        if (slot < 0) {
            lastAppliedKey = key;
            return false;
        }

        ConfigStore.Slot target = store.slot(slot);
        if (target == null || !target.filled()) return false;
        if (store.activeIndex() == slot) {
            lastAppliedKey = key;
            return false;
        }

        boolean ok = store.activate(slot);
        if (ok) {
            lastAppliedKey = key;
            KrypticClient.LOGGER.info("Config '{}' applied for {}", target.name(), key);
        }
        return ok;
    }

    public String lastAppliedKey() {
        return lastAppliedKey;
    }

    // ── persistence ──────────────────────────────────────────────────────────

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("v", 1);
        JsonObject map = new JsonObject();
        for (Map.Entry<String, Integer> e : bindings.entrySet()) {
            map.addProperty(e.getKey(), e.getValue());
        }
        root.add("bindings", map);
        return root;
    }

    public void fromJson(JsonObject root) {
        if (root == null || !root.has("bindings") || !root.get("bindings").isJsonObject()) return;
        bindings.clear();
        for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("bindings").entrySet()) {
            try {
                int slot = e.getValue().getAsInt();
                if (slot >= 0 && slot < ConfigStore.SLOT_COUNT) bindings.put(e.getKey(), slot);
            } catch (Exception ignored) {
                // a malformed entry is dropped rather than failing the load
            }
        }
    }
}
