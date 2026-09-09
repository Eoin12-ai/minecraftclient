package dev.kryptic.module.misc;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

public class FakeRolesModule extends Module {
   public static final String ROLE_NONE = "None";
   public static final String ROLE_SRMOD = "SR.MOD";
   public static final String ROLE_MEDIA = "MEDIA";
   public static final String ROLE_SRADMIN = "SR.ADMIN";
   private static final int GRAY = 8355711;
   private static final int GREEN = 5635925;
   private static final int PINK = 16733695;
   private static final int RED = 16733269;
   private static final int WHITE = 16777215;
   public final ModeSetting role = this.addSetting(
      new ModeSetting("Role", "Which fake rank tag to wear in front of your name", "None", "None", "SR.MOD", "MEDIA", "SR.ADMIN")
   );
   public final BooleanSetting nametag = this.addSetting(new BooleanSetting("Nametag", "Show the tag on your floating nametag (3rd person)", true));
   public final BooleanSetting tabList = this.addSetting(new BooleanSetting("Tab List", "Show the tag before your name in the player list (Tab)", true));
   public final BooleanSetting chat = this.addSetting(new BooleanSetting("Chat", "Show the tag before your name in chat messages", true));

   public FakeRolesModule() {
      super("FakeRoles", "Fake [SR.MOD] / [MEDIA] / [SR.ADMIN] rank tag on your own name", Category.MISC);
   }

   public boolean isActive() {
      MinecraftClient client = MinecraftClient.getInstance();
      return this.isEnabled() && !this.role.is("None") && client.player != null;
   }

   private String selfName() {
      MinecraftClient client = MinecraftClient.getInstance();
      return client.player == null ? null : client.player.getGameProfile().name();
   }

   private boolean isSelf(String text4) {
      String text3 = this.selfName();
      return text3 != null && !text3.isEmpty() && text3.equals(text4);
   }

   private int roleColor() {
      String text4 = this.role.get();

      return switch (text4) {
         case "SR.MOD" -> 5635925;
         case "MEDIA" -> 16733695;
         case "SR.ADMIN" -> 16733269;
         default -> 16777215;
      };
   }

   private Style bracketStyle() {
      return Style.EMPTY.withColor(8355711).withBold(false);
   }

   private Style tagStyle() {
      return Style.EMPTY.withColor(this.roleColor()).withBold(true);
   }

   private Style nameStyle() {
      String text4 = this.role.get();

      return switch (text4) {
         case "SR.MOD" -> Style.EMPTY.withColor(5635925).withBold(true);
         case "SR.ADMIN" -> Style.EMPTY.withColor(16733269).withBold(true);
         case "MEDIA" -> Style.EMPTY.withColor(16777215).withBold(false);
         default -> Style.EMPTY;
      };
   }

   public Text tagComponent() {
      return Text.empty()
         .append(Text.literal("(").setStyle(this.bracketStyle()))
         .append(Text.literal(this.role.get()).setStyle(this.tagStyle()))
         .append(Text.literal("] ").setStyle(this.bracketStyle()));
   }

   public Text buildPrefixedDisplayName(String text4) {
      return this.isActive() && text4 != null
         ? Text.empty().append(this.tagComponent()).append(Text.literal(text4).setStyle(this.nameStyle()))
         : null;
   }

   public Text decorateNametag(Text text) {
      return this.isActive() && this.nametag.get() ? this.modifyText(text) : text;
   }

   public Text decorateTab(Text text, String text4) {
      if (this.isActive() && this.tabList.get()) {
         Text text2 = this.isSelf(text4) ? this.buildPrefixedDisplayName(text4) : null;
         return text2 != null ? text2 : text;
      } else {
         return text;
      }
   }

   public Text decorateChat(Text text) {
      return this.isActive() && this.chat.get() ? this.modifyText(text) : text;
   }

   private Text modifyText(Text text) {
      if (text == null) {
         return null;
      } else {
         String text4 = this.selfName();
         return text4 != null && !text4.isBlank() && text.getString().contains(text4) ? this.splice(text, text4, new boolean[]{false}) : text;
      }
   }

   private Text splice(Text text, String text4, boolean[] value) {
      TextContent textContent = text.getContent();
      MutableText mutableText;
      if (!value[0] && textContent instanceof PlainTextContent plainTextContent) {
         String text42 = plainTextContent.string();
         int n = text42.indexOf(text4);
         if (n >= 0) {
            value[0] = true;
            mutableText = Text.empty();
            if (n > 0) {
               mutableText.append(Text.literal(text42.substring(0, n)).setStyle(text.getStyle()));
            }

            mutableText.append(this.buildPrefixedDisplayName(text4));
            int localY = n + text4.length();
            if (localY < text42.length()) {
               mutableText.append(Text.literal(text42.substring(localY)).setStyle(text.getStyle()));
            }
         } else {
            mutableText = MutableText.of(textContent).setStyle(text.getStyle());
         }
      } else if (!value[0] && textContent instanceof TranslatableTextContent translatableTextContent) {
         Object[] obj = translatableTextContent.getArgs();
         Object[] obj4 = new Object[obj.length];
         boolean[] found2 = new boolean[]{false};
         for (int step = 0; step < obj.length; step++) {
            Object elem = obj[step];
            if (elem instanceof Text text3) {
               obj4[step] = this.splice(text3, text4, found2);
            } else {
               if (!found2[0] && elem instanceof String) {
                  String text5 = (String)elem;
                  if (text5.contains(text4)) {
                     found2[0] = true;
                     int step2 = text5.indexOf(text4);
                     MutableText mutableText2 = Text.empty();
                     if (step2 > 0) {
                        mutableText2.append(Text.literal(text5.substring(0, step2)));
                     }

                     mutableText2.append(this.buildPrefixedDisplayName(text4));
                     int n9 = step2 + text4.length();
                     if (n9 < text5.length()) {
                        mutableText2.append(Text.literal(text5.substring(n9)));
                     }

                     obj4[step] = mutableText2;
                     continue;
                  }
               }

               obj4[step] = value;
            }
         }

         mutableText = MutableText.of(new TranslatableTextContent(translatableTextContent.getKey(), translatableTextContent.getFallback(), obj4)).setStyle(text.getStyle());
      } else {
         mutableText = MutableText.of(textContent).setStyle(text.getStyle());
      }

      for (Text text2 : text.getSiblings()) {
         mutableText.append(this.splice(text2, text4, value));
      }

      return mutableText;
   }
}
