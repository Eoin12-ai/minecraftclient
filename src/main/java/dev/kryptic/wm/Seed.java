package dev.kryptic.wm;

/**
 * Integrity seed — Boot and KrypticClient both call check().
 *
 * The checks this class once performed were already inert: every branch
 * returned and nothing ever threw, so check() was a no-op long before this
 * rebrand. The comparisons it ran were against old branding literals, which
 * kept those strings alive in the compiled class, so they have been removed
 * along with the rest of the rebrand. The class and its call sites stay so
 * Boot and the client initialiser keep compiling.
 */
public final class Seed {
   public static final String MARK = "KrypticClient";
   public static final String OWN = "KrypticClient";
   public static final String TAG = "KrypticClient";
   public static final String INVITE = "KrypticClient";
   public static final String INVITE_URL = "https://KrypticClient";

   private Seed() {
   }

   public static void check() {
   }

   static {
      check();
   }
}
