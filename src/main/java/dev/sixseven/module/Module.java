package dev.sixseven.module;

import dev.sixseven.settings.KeybindSetting;
import dev.sixseven.settings.Setting;
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

   public void setEnabled(boolean enabled2) {
      if (this.enabled != enabled2) {
         this.enabled = enabled2;
         if (enabled2) {
            this.onEnable();
         } else {
            this.onDisable();
         }

         if (this.toggleCallback != null) {
            this.toggleCallback.accept(this, enabled2);
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
