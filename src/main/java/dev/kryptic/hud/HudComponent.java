package dev.kryptic.hud;

import dev.kryptic.render.nanovg.NVGRenderer;
import java.util.function.BooleanSupplier;

public abstract class HudComponent {
   public static final float MIN_SCALE = 0.5F;
   public static final float MAX_SCALE = 2.0F;
   private final String id;
   private final BooleanSupplier visibility;
   private float width ;
   private float width2 ;
   private float scale = 1.0F;

   protected HudComponent(String str, float f, float f3, BooleanSupplier booleanSupplier) {
      this.id = str;
      this.width = f;
      this.width2 = f3;
      this.visibility = booleanSupplier;
   }

   public String getId() {
      return this.id;
   }

   public float getFx() {
      return this.width;
   }

   public float getFy() {
      return this.width2;
   }

   public void setPosition(float f, float f3) {
      this.width = Math.clamp(f, 0.0F, 1.0F);
      this.width2 = Math.clamp(f3, 0.0F, 1.0F);
   }

   public float getScale() {
      return this.scale;
   }

   public void setScale(float f) {
      this.scale = Math.clamp(f, 0.5F, 2.0F);
   }

   public final boolean visible() {
      return this.visibility.getAsBoolean();
   }

   public abstract float measureWidth(NVGRenderer nVGRenderer);

   public abstract float measureHeight(NVGRenderer nVGRenderer);

   public abstract void render(NVGRenderer nVGRenderer, float f, float f3, float f4, float f2);

   public boolean onEditClick(float f, float f3) {
      return false;
   }

   public boolean rightAnchored() {
      return this.width > 0.5F;
   }
}
