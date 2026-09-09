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
   // Near-black with a warm cast rather than the blue-violet greys this UI
   // carried before. A violet base tints every neutral toward the accent it
   // used to have, which is half of why the old look read as one specific
   // client rather than as a piece of software.
   //
   // The panels are opaque now. Translucent bodies over a blurred world meant
   // every label sat on whatever happened to be behind it, so text contrast
   // changed as you turned around.

   /** #0F0D0A — the card body. */
   public int background() {
      return -15790838;
   }

   /** #17140F — the same body, a step lighter, for the gradient's far end. */
   public int backgroundTo() {
      return -15264753;
   }

   /** #1C1813 — the column header strip. */
   public int headerTop() {
      return -14936045;
   }

   public int headerBottom() {
      return -15264753;
   }

   /**
    * The fill behind an enabled module.
    *
    * Only a fifth of the way to the accent: a row has an accent bar and an
    * accent dot already, and a fully saturated fill under bright text is the
    * thing that makes a long enabled column hard to read.
    */
   public int moduleActiveFill() {
      return Colors.withAlpha(Colors.lerp(-15790838, this.accent(), 0.20F), 0.98F);
   }

   /** #F2EDE4 */
   public int textPrimary() {
      return -856604;
   }

   /** #A79C8A */
   public int textMuted() {
      return -5792630;
   }

   /** #6E6558 */
   public int textDisabled() {
      return -9542312;
   }

   public int statusEnabled() {
      return -11671924;
   }

   public int statusDisabled() {
      return -11054753;
   }
}
