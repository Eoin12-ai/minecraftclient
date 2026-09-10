package dev.kryptic.theme;

import dev.kryptic.util.Colors;

public class Theme {
   /** #0A0A0B — the stock card body, and what an unset surface falls back to. */
   private static final int DEFAULT_SURFACE = -16119285;

   private final String name;
   private int accent;
   private final boolean custom;

   /**
    * The custom surface, or 0 for "use the stock one".
    *
    * Only a custom theme can set this. The built-in themes deliberately share
    * one neutral surface and vary only by accent — that is what keeps them
    * reading as one client rather than seven skins.
    *
    * A custom theme picks a surface and nothing else: every other tone is
    * derived from it below. Five separate pickers is how you end up with grey
    * text on a grey panel, and the person who built it is the last one to
    * notice.
    */
   private int surface;

   public Theme(String str, int n, boolean value) {
      this.name = str;
      this.accent = n;
      this.custom = value;
   }

   public String getName() {
      return this.name;
   }

   public int accent() {
      return this.accent;
   }

   public void setAccent(int n) {
      this.accent = n;
   }

   public boolean isCustom() {
      return this.custom;
   }

   /** The chosen surface, or the stock one when none is set. */
   public int surface() {
      return this.surface == 0 ? DEFAULT_SURFACE : this.surface;
   }

   public boolean hasCustomSurface() {
      return this.custom && this.surface != 0;
   }

   /** Ignored on a built-in theme; pass 0 to go back to the stock surface. */
   public void setSurface(int value) {
      if (this.custom) {
         this.surface = value == 0 ? 0 : (value | 0xFF000000);
      }
   }

   /**
    * Whether this theme's surface is light enough that the stock near-white
    * text would disappear on it. Drives the text tones below.
    */
   private boolean lightSurface() {
      return Colors.luminance(this.surface()) > 0.5F;
   }

   public int accentBright() {
      return Colors.lighten(this.accent(), 0.35F);
   }

   public int accentHover() {
      return Colors.withAlpha(this.accent(), 0.35F);
   }

   // ── surfaces ─────────────────────────────────────────────────────────────
   //
   // Neutral greys with no cast at all. A tinted base pulls every surface
   // toward whatever the accent is, which is what made this menu recognisable
   // as one particular client no matter which colour it was set to.
   //
   // The panels are opaque. Translucent bodies over a blurred world meant text
   // contrast changed as the player turned around.

   /** #0A0A0B by default — the card body. */
   public int background() {
      return this.surface();
   }

   /** A step toward the light, for the gradient's far end. */
   public int backgroundTo() {
      if (!this.hasCustomSurface()) {
         return -15658733;
      }

      return step(0.06F);
   }

   /** The column header strip: a further step, so the band reads as a band. */
   public int headerTop() {
      if (!this.hasCustomSurface()) {
         return -15263974;
      }

      return step(0.13F);
   }

   public int headerBottom() {
      if (!this.hasCustomSurface()) {
         return -15658733;
      }

      return step(0.06F);
   }

   /**
    * One step away from the surface, in whichever direction is away from it.
    *
    * Lightening works on a dark theme and does nothing visible on a near-white
    * one, so a light surface steps down instead. Without this a pale custom
    * theme loses its header strip and its gradient entirely.
    */
   private int step(float amount) {
      return this.lightSurface()
            ? Colors.darken(this.surface(), amount)
            : Colors.lighten(this.surface(), amount);
   }

   /**
    * The fill behind an enabled module.
    *
    * A fifth of the way to the accent. A row already carries an accent bar and
    * a dot; a saturated fill under bright text is what makes a long enabled
    * column hard to read.
    */
   public int moduleActiveFill() {
      return Colors.withAlpha(Colors.lerp(this.surface(), this.accent(), 0.20F), 0.98F);
   }

   /** #EDEDEF on the stock surface; flipped to near-black on a light one. */
   public int textPrimary() {
      if (!this.hasCustomSurface()) {
         return -1184273;
      }

      return this.lightSurface() ? -14737633 : -1184273;
   }

   /** #8E8E96 by default — the primary tone, faded toward the surface. */
   public int textMuted() {
      if (!this.hasCustomSurface()) {
         return -7434602;
      }

      return Colors.lerp(this.textPrimary(), this.surface(), 0.45F);
   }

   /** #5A5A62 by default — faded further, but still off the surface. */
   public int textDisabled() {
      if (!this.hasCustomSurface()) {
         return -10855838;
      }

      return Colors.lerp(this.textPrimary(), this.surface(), 0.68F);
   }

   public int statusEnabled() {
      return -11671924;
   }

   public int statusDisabled() {
      return -11054753;
   }
}
