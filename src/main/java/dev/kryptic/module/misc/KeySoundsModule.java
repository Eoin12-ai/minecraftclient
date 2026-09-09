package dev.kryptic.module.misc;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.UiSoundEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

/**
 * Mechanical keyboard sounds for typing and clicking.
 *
 * Thirty-six samples of the same switch rather than one played over and over,
 * because a real keyboard never sounds identical twice — a single looped
 * sample is the thing that makes this sort of effect grating within a minute.
 * A key keeps its own sample for as long as it is held down, so holding W does
 * not machine-gun, and the modifier, space, enter and backspace keys have their
 * own deeper samples the way a real board does.
 */
public class KeySoundsModule extends Module {

    public final BooleanSetting keys = this.addSetting(new BooleanSetting(
            "Keys", "Play a click when you press a key", true));
    public final BooleanSetting mouse = this.addSetting(new BooleanSetting(
            "Mouse", "Play a click for mouse buttons", true));
    public final BooleanSetting scroll = this.addSetting(new BooleanSetting(
            "Scroll", "Play a tick when you scroll", true));
    public final BooleanSetting inChat = this.addSetting(new BooleanSetting(
            "In Menus", "Also play while a screen is open, so typing is audible", true));
    public final BooleanSetting releases = this.addSetting(new BooleanSetting(
            "Key Up", "Also play the quieter sound when a key is released", false));
    public final SliderSetting volume = this.addSetting(new SliderSetting(
            "Volume", "How loud the switches are", 45.0, 0.0, 100.0, 5.0, "%"));
    public final SliderSetting pitchVary = this.addSetting(new SliderSetting(
            "Pitch Variance", "Random pitch spread, so repeated keys differ", 6.0, 0.0, 25.0, 1.0, "%"));

    private final Random random = new Random();

    /** Which sample each held key drew, so a held key does not retrigger. */
    private final java.util.Map<Integer, SoundEvent> held = new java.util.HashMap<>();

    public KeySoundsModule() {
        super("KeySounds", "Mechanical keyboard sounds when you type and click", Category.MISC);
    }

    // ── events, called from the keyboard and mouse mixins ─────────────────────

    /** @param action one of GLFW's PRESS / RELEASE / REPEAT */
    public void onKey(int key, int action) {
        if (!this.isEnabled() || !this.keys.get()) return;
        if (!allowedHere()) return;

        if (action == GLFW.GLFW_PRESS) {
            SoundEvent sample = sampleFor(key);
            held.put(key, sample);
            play(sample, 1.0f);
        } else if (action == GLFW.GLFW_RELEASE) {
            SoundEvent sample = held.remove(key);
            // the up-stroke of a switch is quieter and higher than the down
            if (this.releases.get() && sample != null) play(sample, 1.18f);
        }
        // REPEAT is deliberately silent: a held key is one press, not fifty
    }

    public void onMouseButton(int button, int action) {
        if (!this.isEnabled() || !this.mouse.get()) return;
        if (!allowedHere() || action != GLFW.GLFW_PRESS) return;

        SoundEvent sample = switch (button) {
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> UiSoundEvents.MOUSE_RIGHT;
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> UiSoundEvents.MOUSE_MIDDLE;
            default -> UiSoundEvents.MOUSE_LEFT;
        };
        play(sample, 1.0f);
    }

    public void onScroll(double amount) {
        if (!this.isEnabled() || !this.scroll.get()) return;
        if (!allowedHere() || amount == 0.0) return;
        play(amount > 0.0 ? UiSoundEvents.SCROLL_UP : UiSoundEvents.SCROLL_DOWN, 1.0f);
    }

    // ── internals ────────────────────────────────────────────────────────────

    private boolean allowedHere() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == null) return true;
        return this.inChat.get();
    }

    /**
     * The sample a key gets. Modifiers, space and the two big keys have their
     * own; everything else draws from the 36 ordinary switches, keyed off the
     * GLFW code so one physical key always sounds like itself.
     */
    private SoundEvent sampleFor(int key) {
        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> UiSoundEvents.KEY_SPACE;
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> UiSoundEvents.KEY_ENTER;
            case GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_KEY_DELETE -> UiSoundEvents.KEY_BACKSPACE;
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT,
                 GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL,
                 GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT,
                 GLFW.GLFW_KEY_TAB, GLFW.GLFW_KEY_CAPS_LOCK -> UiSoundEvents.KEY_MODIFIER;
            default -> UiSoundEvents.KEYS[Math.floorMod(key * 31, UiSoundEvents.KEYS.length)];
        };
    }

    private void play(SoundEvent sound, float pitchScale) {
        float gain = this.volume.getFloat() / 100.0f;
        if (gain <= 0.0f || sound == null) return;

        float spread = this.pitchVary.getFloat() / 100.0f;
        float pitch = pitchScale * (1.0f + (random.nextFloat() * 2.0f - 1.0f) * spread);
        MinecraftClient.getInstance().getSoundManager()
                .play(PositionedSoundInstance.ui(sound, Math.max(0.4f, pitch), gain));
    }

    @Override
    protected void onDisable() {
        held.clear();
    }
}
