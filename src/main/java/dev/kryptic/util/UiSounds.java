package dev.kryptic.util;

import dev.kryptic.theme.SoundSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;

public final class UiSounds {
   private static SoundSettings settings = new SoundSettings();
   private static long lastHoverNanos;
   private static long lastSliderNanos;

   private UiSounds() {
   }

   public static void init(SoundSettings soundSettings) {
      settings = soundSettings;
   }

   /**
    * @param pitch  playback rate; 1.0 is the file as authored
    * @param volume this sound's share of the master, 0-1
    *
    * <p>The volume used to be accepted and dropped on the floor. This called
    * {@code ui(sound, pitch)}, and that overload does not default the volume to
    * 1.0 -- it hardcodes <b>0.25</b> and calls the three-argument form. So every
    * UI sound played at a quarter volume, the per-event balance below did
    * nothing, and the master slider worked only as a mute.
    *
    * <p>Both trailing parameters are floats and their names are erased in the
    * remapped jar, so the order was read off the two-argument overload's own
    * bytecode rather than assumed: it loads slot 2 into the constructor's volume
    * and slot 1 into its pitch, which makes the signature (sound, pitch, volume).
    */
   private static void play(SoundEvent sound, float pitch, float volume) {
      float master = settings.volume();
      if (master <= 0.01F) {
         return;
      }

      float level = Math.clamp(volume * master, 0.0F, 1.0F);
      MinecraftClient.getInstance().getSoundManager()
            .play(PositionedSoundInstance.ui(sound, pitch, level));
   }

   public static void guiOpen() {
      if (settings.guiSounds.get()) {
         play(UiSoundEvents.GUI_OPEN, 1.0F, 0.85F);
      }
   }

   public static void guiClose() {
      if (settings.guiSounds.get()) {
         play(UiSoundEvents.GUI_CLOSE, 1.0F, 0.75F);
      }
   }

   public static void hover() {
      if (settings.hoverSounds.get()) {
         long l = System.nanoTime();
         if (l - lastHoverNanos >= 45000000L) {
            lastHoverNanos = l;
            play(UiSoundEvents.HOVER, 0.95F + (float)(l % 7L) * 0.015F, 0.5F);
         }
      }
   }

   public static void toggle(boolean value) {
      if (settings.clickSounds.get()) {
         play(value ? UiSoundEvents.TOGGLE_ON : UiSoundEvents.TOGGLE_OFF, 1.0F, 0.8F);
      }
   }

   public static void checkbox(boolean value) {
      if (settings.clickSounds.get()) {
         play(value ? UiSoundEvents.TOGGLE_ON : UiSoundEvents.TOGGLE_OFF, 1.25F, 0.55F);
      }
   }

   public static void select() {
      if (settings.clickSounds.get()) {
         play(UiSoundEvents.SELECT, 1.0F, 0.65F);
      }
   }

   public static void sliderTick(float f) {
      if (settings.clickSounds.get()) {
         long l = System.nanoTime();
         if (l - lastSliderNanos >= 60000000L) {
            lastSliderNanos = l;
            play(UiSoundEvents.SLIDER, 0.85F + f * 0.55F, 0.5F);
         }
      }
   }

   public static void keybindListen() {
      if (settings.clickSounds.get()) {
         play(UiSoundEvents.KEYBIND, 0.8F, 0.6F);
      }
   }

   public static void keybindSet() {
      if (settings.clickSounds.get()) {
         play(UiSoundEvents.KEYBIND, 1.1F, 0.6F);
      }
   }

   public static void notification(boolean value) {
      if (settings.notificationSounds.get()) {
         play(value ? UiSoundEvents.NOTIFY_ON : UiSoundEvents.NOTIFY_OFF, 1.0F, 0.7F);
      }
   }

   public static void panelCollapse() {
      if (settings.clickSounds.get()) {
         play(UiSoundEvents.SELECT, 0.8F, 0.5F);
      }
   }

}
