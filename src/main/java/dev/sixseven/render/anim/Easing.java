package dev.sixseven.render.anim;

public enum Easing {
   LINEAR {
      @Override
      public float apply(float temp) {
         return temp;
      }
   },
   EASE_OUT_CUBIC {
      @Override
      public float apply(float f) {
         float f3 = 1.0F - f;
         return 1.0F - f3 * f3 * f3;
      }
   },
   EASE_IN_OUT_QUAD {
      @Override
      public float apply(float f) {
         return f < 0.5F ? 2.0F * f * f : 1.0F - (float)Math.pow((double)(-2.0F * f + 2.0F), 2.0) / 2.0F;
      }
   };

   public abstract float apply(float f);

   private static Easing[] $values() {
      return new Easing[]{LINEAR, EASE_OUT_CUBIC, EASE_IN_OUT_QUAD};
   }
}
