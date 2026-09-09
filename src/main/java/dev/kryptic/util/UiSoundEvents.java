package dev.kryptic.util;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class UiSoundEvents {
   public static final SoundEvent GUI_OPEN = register("ui.gui_open");
   public static final SoundEvent GUI_CLOSE = register("ui.gui_close");
   public static final SoundEvent HOVER = register("ui.hover");
   public static final SoundEvent TOGGLE_ON = register("ui.toggle_on");
   public static final SoundEvent TOGGLE_OFF = register("ui.toggle_off");
   public static final SoundEvent SLIDER = register("ui.slider");
   public static final SoundEvent SELECT = register("ui.select");
   public static final SoundEvent KEYBIND = register("ui.keybind");
   public static final SoundEvent NOTIFY_ON = register("ui.notify_on");
   public static final SoundEvent NOTIFY_OFF = register("ui.notify_off");
   public static final SoundEvent STARTUP_SAD = register("startup.sad");
   public static final SoundEvent STARTUP_SONG = register("startup.song");
   public static final SoundEvent STARTUP_TIKI = register("startup.tiki");
   public static final SoundEvent STARTUP_67 = register("startup.67");

   // ── mechanical keyboard set ───────────────────────────────────────────────
   // Thirty-six samples of the same switch. A keyboard never sounds identical
   // twice, and one looped sample is what makes this kind of effect grating.
   public static final SoundEvent[] KEYS = registerKeys();
   public static final SoundEvent KEY_SPACE = register("creamy_keys.space");
   public static final SoundEvent KEY_ENTER = register("creamy_keys.enter");
   public static final SoundEvent KEY_BACKSPACE = register("creamy_keys.backspace");
   public static final SoundEvent KEY_MODIFIER = register("creamy_keys.modifier");
   // each of these carries several variants; Minecraft picks between them
   public static final SoundEvent MOUSE_LEFT = register("creamy_keys.mouse_left");
   public static final SoundEvent MOUSE_RIGHT = register("creamy_keys.mouse_right");
   public static final SoundEvent MOUSE_MIDDLE = register("creamy_keys.mouse_middle");
   public static final SoundEvent SCROLL_UP = register("creamy_keys.scroll_up");
   public static final SoundEvent SCROLL_DOWN = register("creamy_keys.scroll_down");

   private static SoundEvent[] registerKeys() {
      SoundEvent[] out = new SoundEvent[36];
      for (int i = 0; i < out.length; i++) {
         out[i] = register(String.format("creamy_keys.key_%02d", i + 1));
      }
      return out;
   }

   private UiSoundEvents() {
   }

   private static SoundEvent register(String str) {
      Identifier id = Identifier.of("krypticclient", str);
      return (SoundEvent)Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
   }

   public static void bootstrap() {
   }
}
