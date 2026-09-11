package dev.kryptic.settings;

import com.google.gson.JsonElement;
import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
   private final String name;
   private final String description;
   protected T value;
   protected final T defaultValue;
   private BooleanSupplier visibility = () -> true;

   protected Setting(String str, String str3, T t) {
      this.name = str;
      this.description = str3;
      this.value = (T)t;
      this.defaultValue = (T)t;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public T get() {
      return this.value;
   }

   public void set(T t) {
      this.value = (T)t;
   }

   public void reset() {
      this.value = this.defaultValue;
   }

   public void visibleWhen(BooleanSupplier booleanSupplier) {
      this.visibility = booleanSupplier;
   }

   public boolean isVisible() {
      return this.visibility.getAsBoolean();
   }

   public abstract JsonElement toJson();

   public abstract void fromJson(JsonElement jsonElement);
}
