package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.Arrays;
import java.util.List;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

public final class StatsChangerModule extends Module {
   private static final String string_2 = " :»-|_";
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Icons");
   private final Setting<String> val3_2 = this.val_2
      .addSetting(new AdminDetectorModuleHelper2().valOf("title").valOf2("Replaces the sidebar title. & works as the color code.").valOf3("").getVal());
   private final Setting<String> val4 = this.val_2.addSetting(valOf("money", "Value shown behind the $ icon."));
   private final Setting<String> val5 = this.val_2.addSetting(valOf("shards", "Value shown behind the shard icon."));
   private final Setting<String> val6 = this.val_2.addSetting(valOf("kills", "Value shown behind the sword icon."));
   private final Setting<String> val7 = this.val_2.addSetting(valOf("deaths", "Value shown behind the skull icon."));
   private final Setting<String> val8 = this.val_2.addSetting(valOf("playtime", "Value shown behind the clock icon."));
   private final Setting<Boolean> val9 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("debug")
            .valOf2("Logs every icon and the value learned behind it, to check what the server sends.")
            .valOf3(false)
            .getVal()
      );
   private final Setting<String> val10 = this.val2_2.addSetting(valOf2("money-icon", "$"));
   private final Setting<String> val11 = this.val2_2.addSetting(valOf2("shards-icon", "★"));
   private final Setting<String> val12 = this.val2_2.addSetting(valOf2("kills-icon", "\ud83d\udde1"));
   private final Setting<String> val13 = this.val2_2.addSetting(valOf2("deaths-icon", "☠"));
   private final Setting<String> val14 = this.val2_2.addSetting(valOf2("playtime-icon", "⌚"));
   private final List<Setting<String>> list = List.of(this.val4, this.val5, this.val6, this.val7, this.val8);
   private final List<Setting<String>> list2 = List.of(this.val10, this.val11, this.val12, this.val13, this.val14);
   private final String[] stringArray = new String[5];
   private Text class2561;

   public StatsChangerModule() {
      super(SwyzzyAddon.val2, "stats-changer", "Changes the sidebar stats client side.");
   }

   private static Setting<String> valOf(String var0, String var1) {
      return new AdminDetectorModuleHelper2().valOf(var0).valOf2(var1).valOf3("").getVal();
   }

   private static Setting<String> valOf2(String var0, String var1) {
      return new AdminDetectorModuleHelper2()
         .valOf(var0)
         .valOf2("Sidebar symbol this stat sits behind. Adjust it if your server uses another one.")
         .valOf3(var1)
         .getVal();
   }

   @Override
   public void run6() {
      Arrays.fill(this.stringArray, null);
      this.class2561 = null;
   }

   @Override
   public void run7() {
      if (class310.world != null && this.class2561 != null) {
         ScoreboardObjective var1 = class310.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
         if (var1 != null) {
            var1.setDisplayName(this.class2561);
         }
      }

      this.class2561 = null;
   }

   @Override
   public String getString2() {
      int var1 = 0;

      for (String var5 : this.stringArray) {
         if (var5 != null && !var5.isEmpty()) {
            var1++;
         }
      }

      return var1 == 0 ? null : var1 + "/" + this.stringArray.length;
   }

   public String addSetting(String var1) {
      if (!this.isEnabled()) {
         return var1;
      } else {
         for (int var2 = 0; var2 < this.stringArray.length; var2++) {
            String var3 = this.list.get(var2).getObject();
            String var4 = this.list2.get(var2).getObject();
            if (!var3.isEmpty() && !var4.isEmpty() && this.stringArray[var2] != null && !this.stringArray[var2].isEmpty() && var1.contains(var4)) {
               var1 = var1.replace(this.stringArray[var2], var3.replace('&', '§'));
            }
         }

         return var1;
      }
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.world != null) {
         Scoreboard var2 = class310.world.getScoreboard();
         ScoreboardObjective var3 = var2.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
         if (var3 != null) {
            String var4 = this.val3_2.getObject();
            if (!var4.isEmpty()) {
               if (this.class2561 == null) {
                  this.class2561 = var3.getDisplayName();
               }

               var3.setDisplayName(Text.literal(var4.replace('&', '§')));
            }

            for (ScoreboardEntry var6 : var2.getScoreboardEntries(var3)) {
               if (!var6.hidden()) {
                  Team var7 = var2.getScoreHolderTeam(var6.owner());
                  this.run2(Team.decorateName(var7, var6.name()).getString());
               }
            }
         }
      }
   }

   private void run2(String var1) {
      for (int var2 = 0; var2 < this.stringArray.length; var2++) {
         String var3 = this.list2.get(var2).getObject();
         if (!var3.isEmpty()) {
            int var4 = var1.indexOf(var3);
            if (var4 >= 0) {
               String var5 = addSetting2(var1.substring(var4 + var3.length()));
               if (!var5.isEmpty() && !var5.equals(this.stringArray[var2])) {
                  this.stringArray[var2] = var5;
                  if (this.val9.getObject()) {
                     this.run4("%s -> %s", new Object[]{var3, var5});
                  }
               }
            }
         }
      }
   }

   private static String addSetting2(String var0) {
      int var1 = 0;
      int var2 = var0.length();

      while (var1 < var2 && " :»-|_".indexOf(var0.charAt(var1)) >= 0) {
         var1++;
      }

      while (var2 > var1 && " :»-|_".indexOf(var0.charAt(var2 - 1)) >= 0) {
         var2--;
      }

      return var0.substring(var1, var2);
   }

   public static StatsChangerModule getVal() {
      return FakeRankModuleHelper.getVal().valOf(StatsChangerModule.class);
   }
}
