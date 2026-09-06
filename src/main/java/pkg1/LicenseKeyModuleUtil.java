package pkg1;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;

public final class LicenseKeyModuleUtil {
   private static final Path path = FabricLoader.getInstance().getConfigDir().resolve("swyzzy-license.dat");
   private static final Set<String> set = new HashSet<>();
   private static boolean bool;
   private static String string = "";
   private static String string2 = "Not Activated";

   private LicenseKeyModuleUtil() {
   }

   public static void run3() {
      bool = false;
      string2 = "Not Activated";
      string = "";
      if (Files.exists(path)) {
         try {
            Properties var0 = new Properties();

            try (InputStream var1 = Files.newInputStream(path)) {
               var0.load(var1);
            }

            String var7 = var0.getProperty("key", "");
            String var2 = var0.getProperty("hwid", "");
            if (var7.isEmpty() || var2.isEmpty()) {
               return;
            }

            if (!set.contains(addSetting(var7))) {
               string2 = "Key Revoked or Invalid";
               return;
            }

            if (!var2.equals(getString3())) {
               string2 = "HWID Mismatch - Contact Admin for Reset";
               return;
            }

            bool = true;
            string = var7;
            string2 = "Licensed ✓";
         } catch (IOException var6) {
            string2 = "Error loading license";
         }
      }
   }

   public static boolean check(String var0) {
      return true;
   }

   public static void run() {
      bool = false;
      string = "";
      string2 = "Not Activated";

      try {
         Files.deleteIfExists(path);
      } catch (IOException var1) {
      }
   }

   public static boolean isEnabled() {
      return true;
   }

   public static String getString() {
      return string2;
   }

   public static String getString2() {
      if (string.isEmpty()) {
         return "";
      } else {
         return string.length() > 10 ? string.substring(0, 6) + "****" + string.substring(string.length() - 4) : "****";
      }
   }

   public static String getString3() {
      String var0 = System.getProperty("os.name", "unknown")
         + "|"
         + System.getProperty("os.arch", "unknown")
         + "|"
         + System.getProperty("user.home", "unknown")
         + "|"
         + Runtime.getRuntime().availableProcessors();
      return addSetting(var0);
   }

   public static String addSetting(String var0) {
      try {
         MessageDigest var1 = MessageDigest.getInstance("SHA-256");
         byte[] var2 = var1.digest(var0.getBytes(StandardCharsets.UTF_8));
         StringBuilder var3 = new StringBuilder(var2.length * 2);

         for (byte var7 : var2) {
            var3.append(String.format("%02x", var7));
         }

         return var3.toString();
      } catch (NoSuchAlgorithmException var8) {
         throw new IllegalStateException("SHA-256 not available", var8);
      }
   }

   static {
      set.add("1661a5e6b27119e0530eda8a61b0ff069242d5658d0c40dd79602ec5072ce6ba");
      set.add("5d1dba67b322d34d967c4e388ce479f4c3a25a072ef94505fb56f40c3908c991");
      set.add("1b839542e7140f4c40862363998250356bea980ff9eea788088fc6f29b02f522");
      set.add("7c2d36d7d80c9334208fa4c527c49f966c2d3e297412dabbfdb0206e60869d8b");
      set.add("efdb4c5a5c108001fea1280dac13a8a22f164bc02b485b76659503fe517c3a36");
      set.add("709952a61772a71fd4df5bf90be49b1676d06750b9c9ea999eea229806d1bb41");
      set.add("ecedde87e24ea9118a1abf47b57dcdc6b130d0fd34ff019012fffac71ff8bcc6");
      set.add("a864d69a4274a6007f8361969936866df36ee3ef572c0d8af75261fabe871848");
      set.add("e3a6103c83b27bacb3dd2e530d568e2d16e4c753b7f2d156a967b597bee4ffd5");
      set.add("992e59a2ce6980129f277ed61a1a45a3f3015a466601e3a835b29416ea66dc08");
      set.add("1ca01b4fb38e8aa89cf01b7263fba0ce75b5ebdf791d8b3442547cb730168ad1");
      set.add("19ebf7440194d66759e6fed340fa7fc8fa4544cce1e32f3a5352a65700902541");
      set.add("bde0332adf0fa26db8ead39d8d8d27aec3b320496a826410ce8db63f7d2866aa");
      set.add("49a1a87e09d756a89942ef9f222812c2b37cace6ca8c6983dd23b297cbe83026");
      set.add("1bb91030ac08155d5a90dbc38bd739f18be19e3ddf35b3d9c9a57558071eedd9");
      set.add("a4595f1382d0b3085fad0bb79ee00e7665baf7aaafceded9b2d16d623ffc6d7a");
      set.add("d257cabb357276de44a169481369e6f8419d62983f45c5eb5cdff69dd20253b0");
      set.add("1afcb6a4fd7e24ca38c2017acfbcd5f95fa08bc2b6a255cd9077a9842855e975");
      set.add("e93d2fb4b7d7c119e60e06f2bc9b5ac1f366ee6be2805f407b887d9c03020d43");
      set.add("7e6437bececb80345dc17f8e50001574736d6c8c4f39259a633aa11562a82840");
   }
}
