package dev.kryptic.theme;

import dev.kryptic.KrypticClient;

/**
 * One place that decides whether a module draws in its own colour or the
 * theme's.
 *
 * Themes used to stop at the menu and the HUD: every ESP, tracer, outline and
 * beam carried its own colour picker and ignored the theme entirely, so
 * choosing a theme changed the chrome and left the game looking exactly as it
 * did before. This closes that gap without taking the pickers away.
 *
 * <p><b>What it deliberately does not touch.</b> Only modules whose colour is
 * decoration go through here. Where a module uses several colours to say
 * something — Item ESP's three tiers, Mob ESP's hostile against passive, Block
 * ESP per block, Storage ESP per container — tinting them all one colour would
 * not unify them, it would delete what they were telling you. Those keep their
 * own colours whatever the theme is set to.
 */
public final class ThemeColors {

   private ThemeColors() {
   }

   /**
    * The theme accent when the user has asked modules to follow it, otherwise
    * the colour they picked for that module.
    *
    * <p>Alpha is taken from the module's own colour rather than the accent, so
    * a translucent overlay stays translucent when it starts following the
    * theme. Returning the accent whole would slam a fill from 18% to opaque.
    *
    * @param own the module's own colour, ARGB
    */
   public static int resolve(int own) {
      ThemeManager themes = KrypticClient.themes();
      if (themes == null || !themes.moduleColors.get()) {
         return own;
      }

      return (own & 0xFF000000) | (themes.current().accent() & 0x00FFFFFF);
   }

   /**
    * The same, for the places that work in packed RGB with no alpha byte —
    * the world renderers that build their own alpha further down.
    */
   public static int resolveRgb(int ownRgb) {
      ThemeManager themes = KrypticClient.themes();
      if (themes == null || !themes.moduleColors.get()) {
         return ownRgb;
      }

      return themes.current().accent() & 0x00FFFFFF;
   }
}
