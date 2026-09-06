package pkg1;

import com.mojang.authlib.GameProfile;
import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class FakeRankModule extends Module {
   private static final String string_2 = "★";
   private static final String string2_2 = new String(Character.toChars(128249));
   private static final String string3_2 = "+++";
   private static final String string4 = "(?i)§[0-9A-FK-OR]";
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<FakeRankModule.State> val2_2 = this.val_2
      .addSetting(
         new BaseEspModuleHelper2<FakeRankModule.State>()
            .valOf("rank-style")
            .valOf2("Which fake rank icon should be shown before your own name.")
            .valOf3(FakeRankModule.State.Star)
            .getVal()
      );
   private final Setting<String> val3_2 = this.val_2
      .addSetting(
         new AdminDetectorModuleHelper2()
            .valOf("donut-camera-symbol")
            .valOf2("Video camera symbol used for the Donut Camera rank.")
            .valOf3(string2_2)
            .valOf5(this::getBoolean6)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("donut-camera-color")
            .valOf2("Pink color of the Donut Camera rank.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 0, 128, 255))
            .valOf5(this::getBoolean5)
            .getVal()
      );
   private final Setting<String> val5 = this.val_2
      .addSetting(new AdminDetectorModuleHelper2().valOf("star-symbol").valOf2("Symbol used for the star rank.").valOf3("★").valOf5(this::getBoolean4).getVal());
   private final Setting<String> val6 = this.val_2
      .addSetting(
         new AdminDetectorModuleHelper2()
            .valOf("donut-plus-symbol")
            .valOf2("Symbol used for the Donut Plus rank.")
            .valOf3("+++")
            .valOf5(this::getBoolean)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val7 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("star-color")
            .valOf2("Color of the star fake rank.")
            .valOf3(new ActivityChunkFinderModuleHelper4(85, 255, 85, 255))
            .valOf5(this::getBoolean3)
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val8 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("donut-plus-color")
            .valOf2("Light blue color of the Donut Plus rank.")
            .valOf3(new ActivityChunkFinderModuleHelper4(42, 207, 206, 255))
            .valOf5(this::getBoolean2)
            .getVal()
      );

   public FakeRankModule() {
      super(SwyzzyAddon.val2, "fake-rank", "Shows a client-side fake rank before your own name.");
   }

   public static Text class2561Of(GameProfile var0, Text var1) {
      FakeRankModule var2 = getVal();
      return (Text)(var2 != null && var2.check(var0) && !var2.check5(var1) ? var2.getclass5250().append(var1.copy()) : var1);
   }

   public static Text class2561Of2(PlayerEntity var0, Text var1) {
      return var0 == null ? var1 : class2561Of(var0.getGameProfile(), var1);
   }

   public static Text class2561Of3(String var0, Text var1) {
      FakeRankModule var2 = getVal();
      return (Text)(var2 != null && var2.check2(var0) && !var2.check5(var1) ? var2.getclass5250().append(var1.copy()) : var1);
   }

   public static Text addSetting(Text var0) {
      FakeRankModule var1 = getVal();
      return (Text)(var1 != null && var1.check3(var0) && !var1.check5(var0) ? var1.getclass5250().append(var0.copy()) : var0);
   }

   public static Text class2561Of4(String var0, Text var1) {
      FakeRankModule var2 = getVal();
      if (var2 == null || var2.check5(var1)) {
         return var1;
      } else {
         return (Text)(!var2.check2(var0) && !var2.check3(var1) ? var1 : var2.getclass5250().append(var1.copy()));
      }
   }

   public static String stringOf(PlayerEntity var0, String var1) {
      FakeRankModule var2 = getVal();
      return var2 != null && var0 != null && var2.check(var0.getGameProfile()) && !var2.check6(var1) ? var2.getString() + var1 : var1;
   }

   public static String addSetting2(String var0) {
      MinecraftClient var1 = MinecraftClient.getInstance();
      return stringOf(var1.player, var0);
   }

   private static FakeRankModule getVal() {
      FakeRankModule var0 = FakeRankModuleHelper.getVal().valOf(FakeRankModule.class);
      return var0 != null && var0.isEnabled() ? var0 : null;
   }

   private boolean check(GameProfile var1) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      if (var2.player != null && var1 != null) {
         GameProfile var3 = var2.player.getGameProfile();
         return var1.id() != null && var3.id() != null ? var1.id().equals(var3.id()) : var1.equals(var3);
      } else {
         return false;
      }
   }

   private boolean check2(String var1) {
      if (var1 != null && !var1.isBlank()) {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.player == null) {
            return false;
         } else {
            String var3 = addSetting3(var1);
            return var3.equals(addSetting3(var2.player.getNameForScoreboard()))
               || var3.equals(addSetting3(var2.player.getName().getString()))
               || var3.equals(addSetting3(var2.player.getDisplayName().getString()));
         }
      } else {
         return false;
      }
   }

   private boolean check3(Text var1) {
      if (var1 == null) {
         return false;
      } else {
         MinecraftClient var2 = MinecraftClient.getInstance();
         if (var2.player == null) {
            return false;
         } else {
            String var3 = addSetting3(var1.getString());
            return check4(var3, var2.player.getNameForScoreboard())
               || check4(var3, var2.player.getName().getString())
               || check4(var3, var2.player.getDisplayName().getString());
         }
      }
   }

   private static boolean check4(String var0, String var1) {
      String var2 = addSetting3(var1);
      return !var2.isBlank() && var0.contains(var2);
   }

   private boolean check5(Text var1) {
      return var1 != null && this.check6(var1.getString());
   }

   private boolean check6(String var1) {
      return var1 != null && !var1.isBlank() ? addSetting3(var1).startsWith(addSetting3(this.getString())) : false;
   }

   private static String addSetting3(String var0) {
      return var0 == null ? "" : var0.replaceAll("(?i)§[0-9A-FK-OR]", "");
   }

   private String getString() {
      return switch ((FakeRankModule.State)this.val2_2.getObject()) {
         case DonutCamera -> (String)this.val3_2.getObject() + " ";
         case DonutPlus -> (String)this.val6.getObject() + " ";
         case Star -> (String)this.val5.getObject() + " ";
      };
   }

   private MutableText getclass5250() {
      return switch ((FakeRankModule.State)this.val2_2.getObject()) {
         case DonutCamera -> Text.literal(this.getString()).setStyle(Style.EMPTY.withColor(this.val4.getObject().getInt()));
         case DonutPlus -> Text.literal(this.getString()).setStyle(Style.EMPTY.withColor(this.val8.getObject().getInt()));
         case Star -> Text.literal(this.getString()).setStyle(Style.EMPTY.withColor(this.val7.getObject().getInt()));
      };
   }

   private Boolean getBoolean2() {
      return this.val2_2.getObject() == FakeRankModule.State.DonutPlus;
   }

   private Boolean getBoolean3() {
      return this.val2_2.getObject() == FakeRankModule.State.Star;
   }

   private Boolean getBoolean() {
      return this.val2_2.getObject() == FakeRankModule.State.DonutPlus;
   }

   private Boolean getBoolean4() {
      return this.val2_2.getObject() == FakeRankModule.State.Star;
   }

   private Boolean getBoolean5() {
      return this.val2_2.getObject() == FakeRankModule.State.DonutCamera;
   }

   private Boolean getBoolean6() {
      return this.val2_2.getObject() == FakeRankModule.State.DonutCamera;
   }

   public enum State {
      DonutCamera,
      DonutPlus,
      Star;

      private static FakeRankModule.State[] getValArray() {
         return new FakeRankModule.State[]{DonutCamera, DonutPlus, Star};
      }
   }
}
