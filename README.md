# Kryptic Client

A Fabric client mod for **Minecraft 1.21.11**, built around a NanoVG menu and HUD.

---

## Building

```bash
./gradlew build
```

The jar lands in `build/libs/`. Ignore `-sources` and `-dev` jars — the one to
install is `krypticclient-<version>.jar`.

Requirements: JDK 21. Everything else is fetched by Gradle. The one vendored
dependency is `libs/lwjgl-nanovg-3.3.3.jar`, which is `compileOnly` — LWJGL's
NanoVG bindings ship with Minecraft at runtime, so it is a compile stub only.

## Two constraints that are not obvious

Both of these cost a crash to learn, so they are enforced by the build rather
than left as lore.

### The Fabric Loader version is pinned on purpose

`gradle.properties` pins `loader_version` deliberately. Newer Loader releases
ship a Mixin in which `@Redirect`, `@ModifyArg`, `@ModifyArgs` and
`@ModifyVariable` declare `at()` as an **array** rather than a single `@At`.

Java lets one value stand in for a one-element array, so a perfectly correct
`at = @At(...)` silently compiles to `at = [@At(...)]` against such a Mixin —
and MixinExtras reads that attribute as a single `AnnotationNode`, throws
`ClassCastException` during apply, and the client dies before the window opens.
The stack trace names neither the mixin nor the mod.

`checkMixinAnnotationShape` fails the build if the Mixin on the compile
classpath declares any of those four as an array. It inspects the **classpath**,
not the source, because the source is not what goes wrong here.

If you bump the loader, expect to have to re-pin.

### `@Inject` parameters must be the exact types

Mixin matches an `@Inject` handler to its target by raw descriptor and does not
treat `Object` as a wildcard. An `Object` parameter compiles clean, looks
harmless, and then throws `InvalidInjectionException` at apply time — fatal
regardless of `defaultRequire: 0`.

`checkMixinDescriptors` rejects an `@Inject` handler with an `Object` parameter.
It is a lint, not a verifier: it does not resolve the target method, it refuses
the one shape that has actually shipped broken. `@ModifyExpressionValue` on an
erased generic such as `SimpleOption.getValue()` legitimately takes `Object` and
is not affected.

When a signature is unknown, read it off the remapped jar rather than guessing.
A throwaway Gradle task that opens `configurations.compileClasspath` in a
`URLClassLoader` and reflects over the target class answers it in one build.

## Layout

| Path | What lives there |
| --- | --- |
| `module/` | Every module, one per file, grouped by category |
| `module/ModuleOrder.java` | The curated order each column is listed in |
| `gui/` | The menu: panels, widgets, the click-GUI screen |
| `hud/` | HUD components |
| `render/` | World renderers and the NanoVG layer |
| `render/WorldRenderHook.java` | The single place anything draws into the world |
| `mixin/` | Mixins, one target apiece |
| `config/` | Saving, loading, and the rename-migration tables |

### Adding a module

Extend `Module`, call `super(name, description, category)`, register it in
`ModuleManager`, and add its name to `ModuleOrder.CURATED`. Anything left out of
the curated list still appears — sorted by name at the bottom of its column —
so a new module can never vanish because someone forgot this step.

### Renaming anything

Config state is keyed by display name. Renaming a module or a setting orphans
its saved value: it stays in the file under a key nothing looks for any more,
and the user sees a silent reset. `ConfigManager` holds `LEGACY_NAMES` (modules)
and `LEGACY_SETTINGS` (settings, keyed by module and name) for exactly this.
Add an entry whenever you rename.

### The UI sounds are cut, not collected

The ten sounds under `assets/krypticclient/sounds/ui/` are all derived from
three source recordings, cut and pitched by `tools/mksounds.py`. Keep that
script as the source of truth: it holds the segment boundaries, the pitch
ratios and the per-role peak targets, so a sound can be re-cut or rebalanced
without anyone guessing at what the last one was.

Two things it does that are easy to leave out by hand. Pairs (open/close,
on/off) are the *same* segment played in opposite directions, which is what
makes them read as a pair. And every clip is faded to land on real silence a
few milliseconds before its nominal end — a fade-out aimed at the exact end
gets truncated by the resampler and that truncation is an audible click.

Peaks are targeted per role rather than normalised flat, because these do not
all play equally often: `hover` fires every time the cursor crosses a row and
has to sit under everything else, while a notification has to carry over the
game.

## Licence

Bundled fonts keep their own licences, next to them in
`assets/krypticclient/fonts/`. Inter, JetBrains Mono, Monocraft and Xuong are
SIL OFL; Minecraft Ten carries its own notice.

The UI sounds are cut from Mixkit sound effects, used under the Mixkit Sound
Effects Free License.
