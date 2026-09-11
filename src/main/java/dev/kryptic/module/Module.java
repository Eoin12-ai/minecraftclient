package dev.kryptic.module;

import dev.kryptic.KrypticClient;

import dev.kryptic.settings.KeybindSetting;
import dev.kryptic.settings.Setting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class Module {
   private final String name;
   private final String description;
   private final Category category;
   private final KeybindSetting keybind;
   private final List<Setting<?>> settings = new ArrayList<>();
   private boolean enabled;
   private BiConsumer<Module, Boolean> toggleCallback;

   protected Module(String str, String str3, Category category2) {
      this.name = str;
      this.description = str3;
      this.category = category2;
      this.keybind = new KeybindSetting("Keybind", "Toggles " + str, -1);
   }

   protected <T extends Setting<?>> T addSetting(T t) {
      this.settings.add(t);
      return (T)t;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public Category getCategory() {
      return this.category;
   }

   public KeybindSetting getKeybind() {
      return this.keybind;
   }

   public List<Setting<?>> getSettings() {
      return Collections.unmodifiableList(this.settings);
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   /**
    * Turn this module on or off.
    *
    * The flag is set before onEnable runs, deliberately: a module that decides
    * during onEnable that it cannot run disables itself, and that only works if
    * it is already marked on.
    *
    * <p>Nothing a module does here is allowed to reach the game. onEnable talks
    * to the world, the player, the network and the renderer, any of which can
    * be in a state the module did not expect, and an exception escaping this
    * method comes out of a mouse click and takes Minecraft down with it. A
    * module that throws on the way up is forced back off instead -- it is not
    * running, so leaving it marked as running would be a lie, and the next
    * click is a fresh attempt rather than a second crash.
    */
   public void setEnabled(boolean enabled2) {
      if (this.enabled != enabled2) {
         this.enabled = enabled2;

         try {
            if (enabled2) {
               this.onEnable();
            } else {
               this.onDisable();
            }
         } catch (Throwable error) {
            KrypticClient.LOGGER.error("{} threw while turning {}; forcing it off",
                  this.getName(), enabled2 ? "on" : "off", error);
            this.enabled = false;
         }

         if (this.toggleCallback != null) {
            try {
               // the real state, which is not always the one asked for
               this.toggleCallback.accept(this, this.enabled);
            } catch (Throwable error) {
               KrypticClient.LOGGER.error("A listener threw on {} toggling", this.getName(), error);
            }
         }
      }
   }

   void setToggleCallback(BiConsumer<Module, Boolean> biConsumer) {
      this.toggleCallback = biConsumer;
   }

   public void toggle() {
      this.setEnabled(!this.enabled);
   }

   protected void onEnable() {
   }

   protected void onDisable() {
   }

   public void onTick() {
   }

   public boolean onKeyPress(int n) {
      return false;
   }
}
