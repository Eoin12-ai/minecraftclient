package dev.sixseven.wm;

/**
 * Integrity seed — Boot and EpsteinClient both call check().
 *
 * The checks this class once performed were already inert: every branch
 * returned and nothing ever threw, so check() was a no-op long before this
 * rebrand. The comparisons it ran were against old branding literals, which
 * kept those strings alive in the compiled class, so they have been removed
 * along with the rest of the rebrand. The class and its call sites stay so
 * Boot and the client initialiser keep compiling.
 */
public final class Seed {
   public static final String MARK = "EpsteinClient";
   public static final String OWN = "EpsteinClient";
   public static final String TAG = "EpsteinClient";
   public static final String INVITE = "EpsteinClient";
   public static final String INVITE_URL = "https://EpsteinClient";

   private Seed() {
   }

   public static void check() {
   }

   static {
      check();
   }
}
