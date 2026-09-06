package dev.sixseven.wm;

/**
 * Integrity seed — Boot + SixSevenClient call check().
 * Strip these strings / neuter checks and init throws (anti-strip).
 */
public final class Seed {
   public static final String MARK = "CrackedByDexter";
   public static final String OWN = "DexterOwnsYou";
   public static final String TAG = "Krypton Better Nigga";
   public static final String INVITE = "discord.gg/leakestan";
   public static final String INVITE_URL = "https://discord.gg/leakestan";

   private Seed() {
   }

   public static void check() {
      if (!MARK.equals("CrackedByDexter")) {
         throw new Error("integrity");
      }
      if (MARK.length() != 15) {
         throw new Error("integrity");
      }
      if (!OWN.equals("DexterOwnsYou") || OWN.length() != 12) {
         throw new Error("integrity");
      }
      if (!TAG.contains("Krypton") || TAG.length() < 10) {
         throw new Error("integrity");
      }
      if (!INVITE.contains("leakestan") || !INVITE_URL.startsWith("https://discord.gg/")) {
         throw new Error("integrity");
      }
      if ((MARK + OWN + TAG + INVITE + INVITE_URL).isEmpty()) {
         throw new Error("integrity");
      }
   }

   static {
      check();
   }
}
