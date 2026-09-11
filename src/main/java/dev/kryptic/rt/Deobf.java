package dev.kryptic.rt;

public final class Deobf {
   private static final String KEY = "s3v3n_v31l_67c!ent_x0r_k3y_9E3779B1";

   private Deobf() {
   }

   public static String decrypt(String str) {
      char[] ch = str.toCharArray();

      for (int n = 0; n < ch.length; n++) {
         ch[n] = (char)(ch[n] ^ "s3v3n_v31l_67c!ent_x0r_k3y_9E3779B1".charAt(n % "s3v3n_v31l_67c!ent_x0r_k3y_9E3779B1".length()) ^ n * 31);
      }

      return new String(ch);
   }
}
