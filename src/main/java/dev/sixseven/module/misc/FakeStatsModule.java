package dev.sixseven.module.misc;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.SliderSetting;
import dev.sixseven.settings.StringSetting;
import dev.sixseven.util.Amounts;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class FakeStatsModule extends Module {
   private static final int GRAY = 11184810;
   private static final int GREEN = 5635925;
   private static final String[] MONEY_KEYS   = {"bal", "balance", "money", "$", "coins", "dollars"};
   private static final String[] SHARD_KEYS   = {"shard", "shards", "crystal", "crystals"};
   private static final String[] KILL_KEYS    = {"kill", "kills", "k/d", "kdr"};
   private static final String[] DEATH_KEYS   = {"death", "deaths", "died"};
   private static final String[] PLAYTIME_KEYS = {"played", "playtime", "time", "hrs", "hours", "online"};
   public final StringSetting money = this.addSetting(
      new StringSetting("Money", "Your fake balance (1m, 67k, 250000). Wired to FakePay. Blank = off.", "BA", 32, "e.g. 1m, 250k")
   );
   public final SliderSetting moneyLine = this.addSetting(
      new SliderSetting("Money Line", "Which sidebar line is money (count from the top). 0 = auto by name.", 1.0, 0.0, 15.0, 1.0)
         .withLabel(FakeStatsModule::lineLabel)
   );
   public final StringSetting shards = this.addSetting(new StringSetting("Shards", "Fake shard count. Blank = off.", "", 32, "e.g. 9,999"));
   public final SliderSetting shardsLine = this.addSetting(
      new SliderSetting("Shards Line", "Which sidebar line is shards. 0 = auto by name.", 2.0, 0.0, 15.0, 1.0).withLabel(FakeStatsModule::lineLabel)
   );
   public final StringSetting kills = this.addSetting(new StringSetting("Kills", "Fake kill count. Blank = off.", "", 32, "e.g. 1,000"));
   public final SliderSetting killsLine = this.addSetting(
      new SliderSetting("Kills Line", "Which sidebar line is kills. 0 = auto by name.", 3.0, 0.0, 15.0, 1.0).withLabel(FakeStatsModule::lineLabel)
   );
   public final StringSetting deaths = this.addSetting(new StringSetting("Deaths", "Fake death count. Blank = off.", "", 32, "e.g. 0"));
   public final SliderSetting deathsLine = this.addSetting(
      new SliderSetting("Deaths Line", "Which sidebar line is deaths. 0 = auto by name.", 4.0, 0.0, 15.0, 1.0).withLabel(FakeStatsModule::lineLabel)
   );
   public final StringSetting playtime = this.addSetting(new StringSetting("Playtime", "Fake playtime, any text. Blank = off.", "", 32, "e.g. 365d 12h"));
   public final SliderSetting playtimeLine = this.addSetting(
      new SliderSetting("Playtime Line", "Which sidebar line is playtime. 0 = auto by name.", 5.0, 0.0, 15.0, 1.0).withLabel(FakeStatsModule::lineLabel)
   );
   public final BooleanSetting sidebar = this.addSetting(new BooleanSetting("Sidebar", "Rewrite the server scoreboard (leaderboard) on the right.", true));
   public final BooleanSetting balanceCommand = this.addSetting(
      new BooleanSetting("Balance Command", "Intercept /bal & /balance to show your fake balance.", true)
   );
   public final BooleanSetting deductOnPay = this.addSetting(new BooleanSetting("Deduct On Pay", "FakePay subtracts what you pay from Money.", true));
   private String lastMoneyText;
   private double liveBalance;
   private int widthIndex;
   private int drawIndex;

   public FakeStatsModule() {
      super("FakeStats", "Fake balance + editable leaderboard, wired to FakePay", Category.MISC);
      this.moneyLine.visibleWhen(() -> this.sidebar.get() && !this.money.get().isBlank());
      this.shardsLine.visibleWhen(() -> this.sidebar.get() && !this.shards.get().isBlank());
      this.killsLine.visibleWhen(() -> this.sidebar.get() && !this.kills.get().isBlank());
      this.deathsLine.visibleWhen(() -> this.sidebar.get() && !this.deaths.get().isBlank());
      this.playtimeLine.visibleWhen(() -> this.sidebar.get() && !this.playtime.get().isBlank());
   }

   @Override
   protected void onEnable() {
      this.lastMoneyText = null;
      this.syncFromSetting();
   }

   private static String lineLabel(double d) {
      return (int)d == 0 ? "Auto" : "Line " + (int)d;
   }

   private void syncFromSetting() {
      String text2 = this.money.get();
      if (!Objects.equals(text2, this.lastMoneyText)) {
         this.lastMoneyText = text2;
         double d = Amounts.parse(text2);
         this.liveBalance = Double.isNaN(d) ? 0.0 : Math.max(0.0, d);
      }
   }

   public double getLiveBalance() {
      this.syncFromSetting();
      return this.liveBalance;
   }

   public void deduct(double d) {
      this.syncFromSetting();
      this.liveBalance = Math.max(0.0, this.liveBalance - Math.max(0.0, d));
   }

   public void beginSidebar() {
      this.widthIndex = 0;
      this.drawIndex = 0;
   }

   public Text rewriteForWidth(Text text) {
      return this.rewriteLine(text, true);
   }

   public Text rewriteForDraw(Text text) {
      return this.rewriteLine(text, false);
   }

   private Text rewriteLine(Text text, boolean value) {
      if (this.isEnabled() && this.sidebar.get()) {
         String text2 = stripCodes(text.getString());
         if (text2.isBlank()) {
            return text;
         } else {
            int n;
            if (value) {
               int localX = this.widthIndex;
               n = localX;
               this.widthIndex = localX + 1;
            } else {
               int localZ = this.drawIndex;
               n = localZ;
               this.drawIndex = localZ + 1;
            }

            return this.applyOverride(n, text2.toLowerCase(Locale.ROOT), text);
         }
      } else {
         return text;
      }
   }

   private Text applyOverride(int n, String text2, Text text) {
      if (!this.money.get().isBlank() && matches(this.moneyLine, n, text2, MONEY_KEYS)) {
         return Amounts.replaceNumberStyled(text, Amounts.shortForm(this.getLiveBalance()));
      } else if (!this.shards.get().isBlank() && matches(this.shardsLine, n, text2, SHARD_KEYS)) {
         return Amounts.replaceNumberStyled(text, this.shards.get().trim());
      } else if (!this.kills.get().isBlank() && matches(this.killsLine, n, text2, KILL_KEYS)) {
         return Amounts.replaceNumberStyled(text, this.kills.get().trim());
      } else if (!this.deaths.get().isBlank() && matches(this.deathsLine, n, text2, DEATH_KEYS)) {
         return Amounts.replaceNumberStyled(text, this.deaths.get().trim());
      } else {
         return !this.playtime.get().isBlank() && matches(this.playtimeLine, n, text2, PLAYTIME_KEYS)
            ? Amounts.replaceValueStyled(text, this.playtime.get().trim())
            : text;
      }
   }

   private static boolean matches(SliderSetting sliderSetting, int n, String text2, String[] item) {
      int offset = sliderSetting.getInt();
      return offset > 0 ? n + 1 == offset : containsAny(text2, item);
   }

   private static boolean containsAny(String text2, String[] str4) {
      for (String item : str4) {
         if (text2.contains(item)) {
            return true;
         }
      }

      return false;
   }

   public boolean tryInterceptBalance(String text2) {
      if (this.isEnabled() && this.balanceCommand.get()) {
         String[] str5 = text2.trim().split("/_c");
         if (str5.length == 0) {
            return false;
         } else {
            String text6 = str5[0];
            if (text6.startsWith("\\")) {
               text6 = text6.substring(1);
            }

            if (!text6.equalsIgnoreCase("bal") && !text6.equalsIgnoreCase("balance")) {
               return false;
            } else {
               MinecraftClient client = MinecraftClient.getInstance();
               if (client.player == null) {
                  return false;
               } else if (str5.length >= 2 && !str5[1].equalsIgnoreCase(client.player.getGameProfile().getName())) {
                  return false;
               } else {
                  String text7 = Amounts.shortForm(this.getLiveBalance());
                  MutableText text = Text.empty()
                     .append(Text.literal("You have ").withColor(11184810))
                     .append(Text.literal(text7).withColor(5635925));
                  client.player.sendMessage(text, false);
                  client.player.sendMessage(text, true);
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   private static String stripCodes(String text2) {
      return text2 == null ? "" : text2.replaceAll("[\u0013!Gµ\u009füÇðĺńĥĈǝǜǦǃ", "");
   }
}