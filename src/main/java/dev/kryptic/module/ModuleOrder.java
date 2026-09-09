package dev.kryptic.module;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

/**
 * A hand-picked order for each column.
 *
 * Alphabetical is predictable but it is not useful: it puts BlockEntityESP
 * above BlockESP, buries SusChunkFinder in the middle of the Render column,
 * and scatters the three totem modules across the Combat one. This client is
 * for finding bases, so the modules that find things come first, the ones that
 * support that come next, and the situational ones settle to the bottom —
 * within each column, in the order you would actually reach for them.
 *
 * Anything not named here keeps its place after everything that is, sorted by
 * name, so a new module never disappears from view just because nobody
 * remembered to add it to this list.
 */
public final class ModuleOrder {

    private ModuleOrder() {
    }

    /** Column order, most-reached-for first. Matched case-insensitively. */
    private static final String[] CURATED = {
        // ── Render: what finds a base, then what helps you read it ───────────
        "SusChunkFinder",       // the flagship signal
        "Chunk Finder",
        "StorageESP",           // chests and shulkers, the payoff
        "BlockEntityESP",
        "BlockESP",
        "SpawnerNametags",
        "PlayerESP",            // who else is here
        "MobESP",
        "Tracers",
        "ChunkBorders",         // navigation aids
        "Breadcrumbs",
        "RegionMap",
        "DebugHoleESP",
        "FullBright",

        // ── Combat: keep-yourself-alive first, then offence ──────────────────
        "Auto Totem",
        "Hover Totem",
        "Inv Totem",
        "Elytra Swap",
        "Auto Crystal",
        "Anchor Macro",
        "Double Anchor",
        "Mace Bomber",
        "Mace Swap",
        "Shield Breaker",
        "Triggerbot",
        "AimAssist",
        "HitBox",

        // ── Misc: the things that keep a clip or a stash safe come first ─────
        "SpawnerProtect",
        "NameProtect",
        "SkinProtect",
        "StaffList",
        "AutoTPA",
        "CoordSnapper",
        "Freecam",
        "FreeLook",
        "Zoom",
        "CustomFOV",
        "CustomCrosshair",
        "NameTags",
        "KeySounds",
        "CustomGlint",
        "ArmorTrimHider",
        "FastUse",
        "AutoWalk",
        "WeatherNotifier",
        "FakePay",
        "FakeStats",
        "FakeRoles",
        "Media/StaffNames/Icons",

        // ── Client: the things you open and configure ────────────────────────
        "ClickGUI",
        "HUD",
        "Stats",
        "ServerConfigs",
        "ConfigShare",
        "DiscordRPC",
        "SpotifyHUD",
        "ChatMacro",
        "KrypticJumpCircles",
        "SwingSpeed",

        // ── Visuals ──────────────────────────────────────────────────────────
        "CustomAccessories",
        "CustomBlockOutline",
        "HitParticles",
        "MotionBlur",
    };

    private static final Map<String, Integer> RANK = buildRanks();

    private static Map<String, Integer> buildRanks() {
        Map<String, Integer> out = new HashMap<>();
        for (int i = 0; i < CURATED.length; i++) {
            out.put(CURATED[i].toLowerCase(Locale.ROOT), i);
        }
        return Map.copyOf(out);
    }

    /**
     * Where a module sits in the curated order.
     *
     * Anything unlisted returns a rank past every listed one, so new modules
     * land at the bottom of their column rather than silently at the top.
     */
    public static int rankOf(Module module) {
        Integer rank = RANK.get(module.getName().toLowerCase(Locale.ROOT));
        return rank == null ? Integer.MAX_VALUE : rank;
    }

    /** Names in the curated list that no longer match a registered module. */
    public static List<String> stale(List<Module> registered) {
        java.util.Set<String> live = new java.util.HashSet<>();
        for (Module m : registered) live.add(m.getName().toLowerCase(Locale.ROOT));
        List<String> out = new java.util.ArrayList<>();
        for (String name : CURATED) {
            if (!live.contains(name.toLowerCase(Locale.ROOT))) out.add(name);
        }
        return out;
    }
}
