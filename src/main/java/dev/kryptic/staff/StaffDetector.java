package dev.kryptic.staff;

import java.lang.invoke.StringConcatFactory;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public final class StaffDetector {
   public static final String MODE_STAR_RANK = "Star + Rank";
   public static final String MODE_STAR = "Star Only";
   public static final String MODE_RANK = "Rank Only";
   public static final String MODE_NAMES = "Names Only";
   public static final String DEFAULT_SYMBOLS = ".TI.TJ.Pj.Pk.Pf.Pe.Pg.P`.Pa.Pb.Pc.a\\.P|.QN.VQ.Px.Py.Pz.P{.Pt.Pu.AS";
   public static final List<String> DEFAULT_RANK_KEYWORDS = List.of(
      "coowner",
      "owner",
      "manager",
      "administrator",
      "admin",
      "developer",
      "dev",
      "srmod",
      "seniormod",
      "moderator",
      "mod",
      "srhelper",
      "seniorhelper",
      "helper",
      "trialmod",
      "trial",
      "builder",
      "support",
      "staff"
   );
   public static final String DEFAULT_RANK_KEYWORDS_STRING = String.join(", ", DEFAULT_RANK_KEYWORDS);
   private static final Map<String, String> RANK_LABELS = Map.ofEntries(
      Map.entry("coowner", "Co-Owner"),
      Map.entry("owner", "Owner"),
      Map.entry("manager", "Manager"),
      Map.entry("administrator", "Admin"),
      Map.entry("admin", "Admin"),
      Map.entry("developer", "7I>"),
      Map.entry("dev", "7I>"),
      Map.entry("srmod", "Sr.Mod"),
      Map.entry("seniormod", "Sr.Mod"),
      Map.entry("moderator", ">C,"),
      Map.entry("mod", ">C,"),
      Map.entry("srhelper", "Sr.Helper"),
      Map.entry("seniorhelper", "Sr.Helper"),
      Map.entry("helper", "Helper"),
      Map.entry("trialmod", "Trial"),
      Map.entry("trial", "Trial"),
      Map.entry("builder", "Builder"),
      Map.entry("support", "Support"),
      Map.entry("staff", "Staff")
   );

   private StaffDetector() {
   }

   public static StaffEntry classify(String str, Text text, Text text2, Text text3, String str7, boolean value, int n, StaffDetector.DetectConfig detectConfig) {
      if (str != null && !str.isEmpty()) {
         boolean matches = detectConfig.names().contains(str.toLowerCase(Locale.ROOT));
         Integer num = scanMarker(text, detectConfig);
         if (num == null) {
            num = scanMarker(text2, detectConfig);
         }

         if (num == null) {
            num = scanMarker(text3, detectConfig);
         }

         boolean found = num != null;
         String text8 = plain(text);
         String combined = text8 + " " + plain(text2) + " " + plain(text3) + " " + (str7 == null ? "" : str7);
         StaffDetector.Rank rank = deriveRank(stripName(combined, str), detectConfig.rankKeywords());
         boolean found2 = rank != null;
         String text10 = detectConfig.mode();

         if (!matches && !(switch (text10) {
            case "Star Only" -> found;
            case "Rank Only" -> found2;
            case "Names Only" -> false;
            default -> found || found2;
         })) {
            return null;
         } else {
            int localX = found ? num : 0;
            String text11 = found2 ? rank.label() : "";
            int localZ = found2 ? rank.priority() : (found ? 1 : 0);
            return new StaffEntry(str, text11, localX, value, n, localZ);
         }
      } else {
         return null;
      }
   }

   static Integer scanMarker(Text text, StaffDetector.DetectConfig detectConfig) {
      return text == null ? null : (Integer)text.visit((arg, arg2) -> {
         int index = 0;

         while (index < arg2.length()) {
            int n = arg2.codePointAt(index);
            index += Character.charCount(n);
            if (isMarker(n, detectConfig)) {
               TextColor textColor = arg.getColor();
               return Optional.of(textColor != null ? 0xFF000000 | textColor.getRgb() : 0);
            }
         }

         return Optional.empty();
      }, Style.EMPTY).orElse(null);
   }

   private static boolean isMarker(int n, StaffDetector.DetectConfig detectConfig) {
      if (detectConfig.symbols().indexOf(n) >= 0) {
         return true;
      } else {
         return !detectConfig.fontIcons() ? false : n >= 57344 && n <= 63743 || n >= 983040 && n <= 1048573 || n >= 1048576 && n <= 1114109;
      }
   }

   private static StaffDetector.Rank deriveRank(String name, List<String> list) {
      String name2 = lettersOnly(name.toLowerCase(Locale.ROOT));
      if (name2.isEmpty()) {
         return null;
      } else {
         for (int n = 0; n < list.size(); n++) {
            String name3 = (String)list.get(n);
            if (!name3.isEmpty() && name2.contains(name3)) {
               return new StaffDetector.Rank(labelFor(name3), list.size() - n);
            }
         }

         return null;
      }
   }

   private static String labelFor(String str) {
      String text3 = RANK_LABELS.get(str);
      if (text3 != null) {
         return text3;
      } else if (str.isEmpty()) {
         return "Staff";
      } else {
         char ch = Character.toUpperCase(str.charAt(0));
         return ch + str.substring(1);
      }
   }

   private static String plain(Text text) {
      return text == null ? "" : text.getString();
   }

   private static String lettersOnly(String str) {
      StringBuilder sb = new StringBuilder(str.length());

      for (int n = 0; n < str.length(); n++) {
         char ch = str.charAt(n);
         if (ch >= 'a' && ch <= 'z') {
            sb.append(ch);
         }
      }

      return sb.toString();
   }

   private static String stripName(String str, String text3) {
      return text3 != null && !text3.isEmpty() ? str.replaceAll("(?i)" + Pattern.quote(text3), "S") : str;
   }

   public static record DetectConfig(String mode, Set<String> names, List<String> rankKeywords, String symbols, boolean fontIcons, boolean showVanished) {
   }

   private static record Rank(String label, int priority) {
   }
}
