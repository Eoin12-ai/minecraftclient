package dev.kryptic.render.anim;

public class Animation {
   private final float durationMs;
   private final Easing easing;
   private float from;
   private float target;
   private long startNanos;

   public Animation(float f, float f3) {
      this(f, f3, Easing.EASE_OUT_CUBIC);
   }

   public Animation(float f, float f3, Easing easing2) {
      this.durationMs = f;
      this.easing = easing2;
      this.from = f3;
      this.target = f3;
      this.startNanos = System.nanoTime();
   }

   public void setTarget(float f) {
      if (f != this.target) {
         this.from = this.value();
         this.target = f;
         this.startNanos = System.nanoTime();
      }
   }

   public void snapTo(float f) {
      this.from = f;
      this.target = f;
   }

   public float getTarget() {
      return this.target;
   }

   public float value() {
      float f = (float)(System.nanoTime() - this.startNanos) / 1000000.0F / this.durationMs;
      return f >= 1.0F ? this.target : this.from + (this.target - this.from) * this.easing.apply(Math.max(f, 0.0F));
   }

   public boolean isDone() {
      return (float)(System.nanoTime() - this.startNanos) / 1000000.0F >= this.durationMs;
   }
}
