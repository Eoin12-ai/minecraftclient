package dev.kryptic.util;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public final class Amounts {
   private static final int[] POWER = new int[]{18, 15, 12, 9, 6, 3};
   private static final String[] SUFFIX = new String[]{"qt", "q", "t", "b", "m", "k"};
   private static final String[] DISPLAY = new String[]{"qt", "Q", "T", "B", "M", "k"};
   private static final Pattern NUMBER = Pattern.compile("\\d[\\d.,]*(?:\\s?(?:[qQ][tT]|[kKmMbBtTqQ]))?");

   private Amounts() {
   }

   public static double parse(String text2) {
      if (text2 == null) {
         return Double.NaN;
      } else {
         String trimmed = text2.trim().toLowerCase(Locale.ROOT).replaceAll("(\u0000\u0014\u001dM\u0099", "");

         while (!trimmed.isEmpty() && (trimmed.charAt(0) == '$' || trimmed.charAt(0) == 8364 || trimmed.charAt(0) == 163)) {
            trimmed = trimmed.substring(1);
         }

         if (trimmed.isEmpty()) {
            return Double.NaN;
         } else {
            for (int n = 0; n < SUFFIX.length; n++) {
               if (trimmed.endsWith(SUFFIX[n]) && trimmed.length() > SUFFIX[n].length()) {
                  String json = trimmed.substring(0, trimmed.length() - SUFFIX[n].length());

                  try {
                     return Double.parseDouble(json) * Math.pow(10.0, (double)POWER[n]);
                  } catch (NumberFormatException ex) {
                     return Double.NaN;
                  }
               }
            }

            try {
               return Double.parseDouble(trimmed);
            } catch (NumberFormatException ex2) {
               return Double.NaN;
            }
         }
      }
   }

   public static String shortForm(double d) {
      double coord = Math.abs(d);

      for (int n = 0; n < POWER.length; n++) {
         double currentScore = Math.pow(10.0, (double)POWER[n]);
         if (coord >= currentScore) {
            double coord3 = (double)Math.round(d / currentScore * 10.0) / 10.0;
            String text2 = trimZero(coord3);
            return text2 + DISPLAY[n];
         }
      }

      return Long.toString(Math.round(d));
   }

   public static String comma(double d) {
      return String.format(Locale.US, "%,d", (long)Math.floor(d));
   }

   public static String plain(double d) {
      return Long.toString((long)Math.floor(d));
   }

   public static String format(double d, String text2) {
      return switch (text2) {
         case "Short" -> shortForm(d);
         case "Plain" -> plain(d);
         default -> comma(d);
      };
   }

   private static String trimZero(double temp) {
      return temp == Math.floor(temp) && !Double.isInfinite(temp) ? Long.toString((long)temp) : String.valueOf(temp);
   }

   public static int[] lastNumberSpan(String text2) {
      Matcher matcher = NUMBER.matcher(text2);
      int bestSlot = -1;

      int n;
      for (n = -1; matcher.find(); n = matcher.end()) {
         bestSlot = matcher.start();
      }

      return bestSlot < 0 ? null : new int[]{bestSlot, n};
   }

   public static int[] valueSpan(String text2) {
      for (int n = 0; n < text2.length(); n++) {
         if (Character.isDigit(text2.charAt(n))) {
            return new int[]{n, text2.length()};
         }
      }

      return null;
   }

   public static Text replaceNumberStyled(Text text, String text2) {
      return spliceStyled(text, text2, false);
   }

   public static Text replaceValueStyled(Text text, String text2) {
      return spliceStyled(text, text2, true);
   }

   private static Text spliceStyled(Text text, String text2, boolean value) {
      ArrayList list = new ArrayList();
      ArrayList<String> list2 = new ArrayList();
      text.visit((arg, arg2) -> {
         list.add(arg);
         list2.add(arg2);
         return Optional.empty();
      }, Style.EMPTY);
      StringBuilder sb = new StringBuilder();

      for (String item : list2) {
         sb.append(item);
      }

      int[] n = value ? valueSpan(sb.toString()) : lastNumberSpan(sb.toString());
      if (n == null) {
         return text;
      } else {
         int step2 = n[0];
         int chunkZ = n[1];
         MutableText mutableText = Text.empty();
         int n9 = 0;
         boolean active = false;

         for (int n10 = 0; n10 < list2.size(); n10++) {
            String trimmed = (String)list2.get(n10);
            Style style = (Style)list.get(n10);
            int n11 = n9 + trimmed.length();
            int n12 = Math.min(n11, step2);
            if (n12 > n9) {
               mutableText.append(Text.literal(trimmed.substring(0, n12 - n9)).setStyle(style));
            }

            if (!active && step2 >= n9 && step2 < n11) {
               mutableText.append(Text.literal(text2).setStyle(style));
               active = true;
            }

            int n13 = Math.max(n9, chunkZ);
            if (n11 > n13) {
               mutableText.append(Text.literal(trimmed.substring(n13 - n9)).setStyle(style));
            }

            n9 = n11;
         }

         if (!active) {
            mutableText.append(Text.literal(text2));
         }

         return mutableText;
      }
   }
}
