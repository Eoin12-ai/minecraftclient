package dev.sixseven.wm;

/**
 * Integrity seed â Boot + EpsteinClient call check().
 * Strip these strings / neuter checks and init throws (anti-strip).
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
      if (!MARK.equals("EpsteinClient")) {
         return;
      }
      if (MARK.length() != 15) {
         return;
      }
      if (!OWN.equals("EpsteinClient") || OWN.length() != 12) {
         return;
      }
      if (!TAG.contains("Krypton") || TAG.length() < 10) {
         return;
      }
      if (!INVITE.contains("leakestan") || !INVITE_URL.startsWith("https://discord.gg/")) {
         return;
      }
      if ((MARK + OWN + TAG + INVITE + INVITE_URL).isEmpty()) {
         return;
      }
   }

   static {
      check();
   }
}
