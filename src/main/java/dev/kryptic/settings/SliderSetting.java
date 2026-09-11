package dev.kryptic.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.lang.invoke.StringConcatFactory;
import java.util.function.DoubleFunction;

public class SliderSetting extends Setting<Double> {
   private final double min;
   private final double max;
   private final double step;
   private final String suffix;
   private DoubleFunction<String> labelFn;

   public SliderSetting(String json, String str3, double d, double coord, double currentScore, double coord3) {
      this(json, str3, d, coord, currentScore, coord3, "");
   }

   public SliderSetting(String json, String str4, double d, double coord, double currentScore, double coord3, String str5) {
      super(json, str4, d);
      this.min = coord;
      this.max = currentScore;
      this.step = coord3;
      this.suffix = str5;
   }

   public double getMin() {
      return this.min;
   }

   public double getMax() {
      return this.max;
   }

   public String getSuffix() {
      return this.suffix;
   }

   public float getFloat() {
      return this.get().floatValue();
   }

   public int getInt() {
      return (int)Math.round(this.get());
   }

   public double getNormalized() {
      return (this.get() - this.min) / (this.max - this.min);
   }

   public void setNormalized(double d) {
      this.set(this.min + (this.max - this.min) * Math.clamp(d, 0.0, 1.0));
   }

   public void set(Double value) {
      double d = (double)Math.round(value / this.step) * this.step;
      d = (double)Math.round(d * 1000000.0) / 1000000.0;
      super.set(Math.clamp(d, this.min, this.max));
   }

   public SliderSetting withLabel(DoubleFunction<String> doubleFunction) {
      this.labelFn = doubleFunction;
      return this;
   }

   public String formatValue() {
      if (this.labelFn != null) {
         return this.labelFn.apply(this.get());
      } else {
         double d = this.get();
         String json;
         if (this.step >= 1.0 && d == Math.floor(d)) {
            json = Long.toString((long)d);
         } else {
            json = String.valueOf((double)Math.round(d * 100.0) / 100.0);
            if (json.endsWith(".0")) {
               json = json.substring(0, json.length() - 2);
            }
         }

         return json + this.suffix;
      }
   }

   @Override
   public JsonElement toJson() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void fromJson(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
         this.set(jsonElement.getAsDouble());
      }
   }
}
