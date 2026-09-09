package dev.kryptic.module.misc;

import com.mojang.authlib.GameProfile;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.StringSetting;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

public class NameProtectModule extends Module {
   public final StringSetting ownName = this.addSetting(new StringSetting("Your Alias", "What your own name is replaced with", "You", 16, "You"));
   public final ModeSetting style = this.addSetting(
      new ModeSetting("Others", "How other players' names are replaced", "Aliases", "Aliases", "Blank", "Player #")
   );
   public final BooleanSetting selfOnly = this.addSetting(new BooleanSetting("Self Only", "Only hide your own name, leave others alone", false));
   private static final int SEEN_CAP = 256;
   private final LinkedHashMap<String, String> seen = new LinkedHashMap<String, String>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, String> entry) {
         return this.size() > 256;
      }
   };
   private String cacheSig = null;
   private Map<String, String> cachedTargets = Map.of();
   private Pattern cachedPattern = null;
   private ClientPlayNetworkHandler lastConnection = null;

   public NameProtectModule() {
      super("Name Protect", "Hides player names in clips", Category.MISC);
   }

   public String selfName() {
      MinecraftClient client = MinecraftClient.getInstance();
      return client.player == null ? null : client.player.getGameProfile().name();
   }

   private boolean isSelf(String text) {
      String text3 = this.selfName();
      return text3 != null && text3.equalsIgnoreCase(text);
   }

   public String styledFor(String text) {
      if (!this.isSelf(text)) {
         if (this.style.is("Blank")) {
            return "";
         } else if (this.style.is("Player #")) {
            int n = Math.floorMod(text.toLowerCase(Locale.ROOT).hashCode(), 99);
            return "Player " + (n + 1);
         } else {
            return "Player";
         }
      } else {
         String text3 = this.ownName.get();
         return text3 != null && !text3.isBlank() ? text3 : "You";
      }
   }

   private Map<String, String> buildTargets() {
      LinkedHashMap linkedHashMap = new LinkedHashMap();
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayNetworkHandler clientPlayNetworkHandler = client.getNetworkHandler();
      if (clientPlayNetworkHandler != this.lastConnection) {
         this.seen.clear();
         this.lastConnection = clientPlayNetworkHandler;
      }

      String text = this.selfName();
      if (text != null && !text.isEmpty()) {
         this.seen.put(text.toLowerCase(Locale.ROOT), text);
         linkedHashMap.put(text, this.styledFor(text));
      }

      boolean found = !this.selfOnly.get();
      if (clientPlayNetworkHandler != null) {
         for (PlayerListEntry playerListEntry : clientPlayNetworkHandler.getPlayerList()) {
            GameProfile gameProfile = playerListEntry.getProfile();
            String name2 = gameProfile == null ? null : gameProfile.name();
            if (name2 != null && !name2.isEmpty()) {
               this.seen.put(name2.toLowerCase(Locale.ROOT), name2);
               if (found) {
                  linkedHashMap.putIfAbsent(name2, this.styledFor(name2));
               }
            }
         }
      }

      if (found) {
         for (String text3 : new ArrayList<>(this.seen.values())) {
            linkedHashMap.putIfAbsent(text3, this.styledFor(text3));
         }
      }

      linkedHashMap.entrySet().removeIf(arg -> { java.util.Map.Entry<String,String> e = (java.util.Map.Entry<String,String>)arg; return e.getKey().equalsIgnoreCase(e.getValue()); });
      return linkedHashMap;
   }

   private void ensureCache() {
      MinecraftClient client = MinecraftClient.getInstance();
      int n = client.player == null ? -1 : client.player.age;
      String text = n + "|" + this.selfOnly.get() + "|" + this.style.get() + "|" + this.ownName.get();
      if (!text.equals(this.cacheSig)) {
         this.cacheSig = text;
         this.cachedTargets = this.buildTargets();
         this.cachedPattern = buildPattern(this.cachedTargets);
      }
   }

   private static Pattern buildPattern(Map<String, String> map) {
      if (map.isEmpty()) {
         return null;
      } else {
         ArrayList<String> list = new ArrayList<>(map.keySet());
         list.sort((arg, arg2) -> Integer.compare(arg2.length(), arg.length()));
         StringBuilder sb = new StringBuilder("[\u0013!G:ÃÂ»ÃÂ°ÃÂ\u0092ÃÂºÃÂÃÂ¹ÃÂ¢ÃÂÃÂ©ÃÂÃÂ³ÃÂÃÂ®ÃÂ¨ÃÂµÃÂ");

         for (int n = 0; n < list.size(); n++) {
            if (n > 0) {
               sb.append('|');
            }

            sb.append(Pattern.quote((String)list.get(n)));
         }

         sb.append(")(?![A-Za-z0-9_])");
         return Pattern.compile(sb.toString());
      }
   }

   public String replacementForDisplay(String text) {
      if (text != null && !text.isEmpty()) {
         this.ensureCache();
         if (this.cachedPattern == null) {
            return null;
         } else {
            String text3 = this.replaceNames(text);
            return text3.equals(text) ? null : text3;
         }
      } else {
         return null;
      }
   }

   public Text censorChat(Text text) {
      if (text == null) {
         return null;
      } else {
         this.ensureCache();
         return this.cachedPattern == null ? text : this.rewrite(text);
      }
   }

   private Text rewrite(Text text) {
      TextContent textContent = text.getContent();
      MutableText mutableText;
      if (textContent instanceof PlainTextContent plainTextContent) {
         mutableText = MutableText.of(PlainTextContent.of(this.replaceNames(plainTextContent.string())));
      } else if (textContent instanceof TranslatableTextContent translatableTextContent) {
         Object[] obj = translatableTextContent.getArgs();
         Object[] obj4 = new Object[obj.length];

         for (int n = 0; n < obj.length; n++) {
            Object value = obj[n];
            if (value instanceof Text text3) {
               obj4[n] = this.rewrite(text3);
            } else if (value instanceof String textVal) {
               obj4[n] = this.replaceNames(textVal);
            } else {
               obj4[n] = value;
            }
         }

         mutableText = MutableText.of(new TranslatableTextContent(translatableTextContent.getKey(), translatableTextContent.getFallback(), obj4));
      } else {
         mutableText = MutableText.of(textContent);
      }

      mutableText.setStyle(text.getStyle());

      for (Text text2 : text.getSiblings()) {
         mutableText.append(this.rewrite(text2));
      }

      return mutableText;
   }

   private String replaceNames(String text) {
      if (text != null && !text.isEmpty() && this.cachedPattern != null) {
         Matcher matcher = this.cachedPattern.matcher(text);
         if (!matcher.find()) {
            return text;
         } else {
            StringBuilder sb = new StringBuilder(text.length());
            int n = 0;

            do {
               String name2 = matcher.group(1);
               String text5 = this.aliasFor(name2);
               sb.append(text, n, matcher.start());
               sb.append(text5 != null ? text5 : name2);
               n = matcher.end();
            } while (matcher.find());

            sb.append(text, n, text.length());
            return sb.toString();
         }
      } else {
         return text;
      }
   }

   private String aliasFor(String text) {
      String text3 = this.cachedTargets.get(text);
      if (text3 != null) {
         return text3;
      } else {
         for (Entry entry : this.cachedTargets.entrySet()) {
            if (((String)entry.getKey()).equalsIgnoreCase(text)) {
               return (String)entry.getValue();
            }
         }

         return null;
      }
   }
}
