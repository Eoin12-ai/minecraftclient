package dev.sixseven.util;

import dev.sixseven.theme.SoundSettings;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
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

   private static void play(SoundEvent sound, float f, float f4) {
      float f5 = settings.volume();
      if (!(f5 <= 0.01F)) {
         MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(sound, f));
      }
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

   public static void playStartup() {
      ArrayList<SoundEvent> list = new ArrayList<>();
      if (settings.startup67.get()) {
         list.add(UiSoundEvents.STARTUP_67);
      }

      if (settings.startupSad.get()) {
         list.add(UiSoundEvents.STARTUP_SAD);
      }

      if (settings.startupSong.get()) {
         list.add(UiSoundEvents.STARTUP_SONG);
      }

      if (settings.startupTiki.get()) {
         list.add(UiSoundEvents.STARTUP_TIKI);
      }

      if (!list.isEmpty()) {
         SoundEvent sound = list.get(ThreadLocalRandom.current().nextInt(list.size()));
         play(sound, 1.0F, 0.9F);
      }
   }
}
