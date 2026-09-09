package dev.kryptic.theme;

import dev.kryptic.util.Colors;

public class Theme {
   private final String name;
   private int accent;
   private final boolean custom;

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

   /** #0A0A0B — the card body. */
   public int background() {
      return -16119285;
   }

   /** #111113 — a step lighter, for the gradient's far end. */
   public int backgroundTo() {
      return -15658733;
   }

   /** #17171A — the column header strip. */
   public int headerTop() {
      return -15263974;
   }

   public int headerBottom() {
      return -15658733;
   }

   /**
    * The fill behind an enabled module.
    *
    * A fifth of the way to the accent. A row already carries an accent bar and
    * a dot; a saturated fill under bright text is what makes a long enabled
    * column hard to read.
    */
   public int moduleActiveFill() {
      return Colors.withAlpha(Colors.lerp(-16119285, this.accent(), 0.20F), 0.98F);
   }

   /** #EDEDEF */
   public int textPrimary() {
      return -1184273;
   }

   /** #8E8E96 */
   public int textMuted() {
      return -7434602;
   }

   /** #5A5A62 */
   public int textDisabled() {
      return -10855838;
   }

   public int statusEnabled() {
      return -11671924;
   }

   public int statusDisabled() {
      return -11054753;
   }
}
