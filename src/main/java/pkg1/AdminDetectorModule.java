package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;

public class AdminDetectorModule extends Module {
   private static final int intVal = 10;
   private static final int intVal2 = 10;
   private static final int intVal3 = 10;
   private static final int intVal4 = 8;
   private static final int intVal5 = 26;
   private static final int intVal6 = 20;
   private static final int intVal7 = 16;
   private static final int intVal8 = -301265901;
   private static final int intVal9 = -10754677;
   private static final int intVal10 = -1021334;
   private static final int intVal11 = -1381654;
   private static final Pattern pattern = Pattern.compile("[a-z0-9_]{1,16}");
   private static final List<String> list = List.of(
      "archivepedro", "frwost", "w1zox_", "fluffymaster07", "bautiedgar", "showered", "pastagamer08", "itszdeath", "0gsummer"
   );
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("HUD");
   private final Setting<Boolean> val3_2 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("alerts").valOf2("Toast and sound on join or leave.").valOf3(true).getVal());
   private final Setting<Boolean> val4 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("admin-list").valOf2("Show the admin list panel.").valOf3(true).getVal());
   private final Setting<String> val5 = this.val_2
      .addSetting(
         new AdminDetectorModuleHelper2()
            .valOf("extra-players")
            .valOf2("Extra watched players. Use commas, semicolons, or new lines for multiple names.")
            .valOf3("")
            .getVal()
      );
   private final Setting<Integer> val6 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6().valOf("hud-x").valOf2("Panel X position (-1 for default).").valOf3(-1).valOf6(-1).valOf7(4000).getVal()
      );
   private final Setting<Integer> val7 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6().valOf("hud-y").valOf2("Panel Y position (-1 for default).").valOf3(-1).valOf6(-1).valOf7(4000).getVal()
      );
   private final Setting<Integer> val8 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("admin-list-size")
            .valOf2("Scale of the admin list panel.")
            .valOf3(100)
            .valOf4(70, 150)
            .valOf5(70, 150)
            .getVal()
      );
   private final Set<String> set = new HashSet<>();
   private final Map<String, AdminDetectorModule.Inner4> map = new LinkedHashMap<>();
   private final Map<String, SkinTextures> map2 = new HashMap<>();
   private final AdminDetectorModule.Inner2 val9 = new AdminDetectorModule.Inner2();
   private final Map<String, Float> map3 = new HashMap<>();
   private long longVal;
   private int intVal12;

   public AdminDetectorModule() {
      super(SwyzzyAddon.val2, "admin-detector", "Shows watched admins and custom players with their online status.");
   }

   @Override
   public void run6() {
      this.run15();
      if (class310.player != null && class310.world != null && class310.getNetworkHandler() != null) {
         this.run16();
      }
   }

   @Override
   public void run7() {
      this.run15();
   }

   @Override
   public String getString2() {
      if (this.map.isEmpty()) {
         return "0/0";
      } else {
         long var1 = this.map.values().stream().filter(AdminDetectorModule.Inner4::online).count();
         return var1 + "/" + this.map.size();
      }
   }

   private void run15() {
      this.set.clear();
      this.map.clear();
      this.map2.clear();
      this.val9.run3();
      this.intVal12 = 0;
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null && class310.getNetworkHandler() != null) {
         this.intVal12++;
         if (this.intVal12 % 10 == 0) {
            this.run16();
         }
      }
   }

   @InternalHelper5
   private void run2(AdminDetectorModuleData var1) {
      if (this.val4.getObject()) {
         this.run5(var1.class332);
      }

      this.val9.run2(var1.class332, class310);
   }

   private void run16() {
      LinkedHashMap var1 = this.getLinkedHashMap();
      Map var2 = this.mapOf(var1.keySet(), class310.getNetworkHandler().getPlayerList());
      this.run3(var1.keySet(), var2);
      LinkedHashMap var3 = new LinkedHashMap();
      HashSet var4 = new HashSet();

      for (Entry var6 : (Iterable<Entry>)(Object)(var1.entrySet())) {
         String var7 = (String)var6.getKey();
         String var8 = (String)var6.getValue();
         AdminDetectorModule.Inner1 var9 = (AdminDetectorModule.Inner1)var2.get(var7);
         String var10 = var9 != null ? var9.displayName() : var8;
         boolean var11 = var9 != null;
         SkinTextures var12 = var9 != null ? var9.skinTextures() : this.map2.get(var7);
         var3.put(var7, new Inner4(var7, var10, var11, var12));
         if (var11) {
            var4.add(var7);
         }
      }

      if (!this.map.isEmpty() && this.val3_2.getObject()) {
         HashSet var13 = new HashSet(var4);
         var13.removeAll(this.set);
         HashSet var14 = new HashSet<>(this.set);
         var14.removeAll(var4);

         for (String var17 : (Iterable<String>)(Object)(var13)) {
            AdminDetectorModule.Inner4 var19 = (AdminDetectorModule.Inner4)var3.get(var17);
            if (var19 != null) {
               this.run8(var19, true);
            }
         }

         for (String var18 : (Iterable<String>)(Object)(var14)) {
            AdminDetectorModule.Inner4 var20 = this.map.get(var18);
            if (var20 != null) {
               this.run8(var20, false);
            }
         }
      }

      this.map.clear();
      this.map.putAll(var3);
      this.set.clear();
      this.set.addAll(var4);
   }

   private LinkedHashMap<String, String> getLinkedHashMap() {
      LinkedHashMap var1 = new LinkedHashMap();

      for (String var3 : list) {
         var1.put(var3, var3);
      }

      for (String var5 : this.val5.getObject().split("[,;\\r\\n]+")) {
         String var6 = var5.trim();
         if (!var6.isEmpty()) {
            var1.put(addSetting(var6), var6);
         }
      }

      return var1;
   }

   private Map<String, AdminDetectorModule.Inner1> mapOf(Set<String> var1, Collection<PlayerListEntry> var2) {
      HashMap var3 = new HashMap();

      for (PlayerListEntry var5 : var2) {
         String var6 = var5.getProfile() != null ? var5.getProfile().getName() : "";
         SkinTextures var7 = var5.getSkinTextures();
         String var8 = var5.getDisplayName() != null ? var5.getDisplayName().getString() : "";

         for (String var10 : this.setOf(var1, var6, var8)) {
            String var11 = !var6.isBlank() ? var6 : addSetting2(var8);
            var3.put(var10, new Inner1(var11, var7));
            this.run11(var10, var7);
         }
      }

      return var3;
   }

   private void run3(Set<String> var1, Map<String, AdminDetectorModule.Inner1> var2) {
      if (class310.world != null) {
         for (PlayerEntity var4 : class310.world.getPlayers()) {
            String var5 = var4.getGameProfile() != null ? var4.getGameProfile().getName() : "";
            String var6 = var4.getDisplayName() != null ? var4.getDisplayName().getString() : "";

            for (String var8 : this.setOf(var1, var5, var6)) {
               String var9 = !var5.isBlank() ? var5 : addSetting2(var6);
               SkinTextures var10 = var4 instanceof AbstractClientPlayerEntity var11 ? var11.getSkinTextures() : this.map2.get(var8);
               var2.putIfAbsent(var8, new Inner1(var9, var10));
               this.run11(var8, var10);
            }
         }
      }
   }

   private Set<String> setOf(Set<String> var1, String var2, String var3) {
      HashSet var4 = new HashSet();
      String var5 = addSetting(var2);
      if (var1.contains(var5)) {
         var4.add(var5);
      }

      String var6 = addSetting(var3);
      if (var1.contains(var6)) {
         var4.add(var6);
      }

      Matcher var7 = pattern.matcher(var6);

      while (var7.find()) {
         String var8 = var7.group();
         if (var1.contains(var8)) {
            var4.add(var8);
         }
      }

      return var4;
   }

   private void run5(DrawContext var1) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      TextRenderer var3 = var2.textRenderer;
      if (var2.getWindow() != null && var3 != null) {
         List var4 = this.getList();
         long var5 = System.nanoTime();
         float var7 = this.longVal == 0L ? 0.016666668F : Math.min(0.1F, (float)(var5 - this.longVal) / 1.0E9F);
         this.longVal = var5;
         this.map3.keySet().removeIf(var905 -> AdminDetectorModule.check(var4, var905));
         float var8 = this.val8.getObject().intValue() / 100.0F;
         int var9 = this.intOf2(var3, var4);
         int var10 = intOf(var4.size());
         int var11 = ((Integer)this.val6.getObject()) >= 0 ? this.val6.getObject() : 10;
         int var12 = ((Integer)this.val7.getObject()) >= 0 ? this.val7.getObject() : 10;
         int var13 = AdminDetectorModuleUtil.getInt();
         net.minecraft.client.util.math.MatrixStack var14 = var1.getMatrices();
         var14.push();
         var14.translate((double)var11, (double)var12, 0.0);
         var14.scale(var8, var8, var8);
         AdminDetectorModuleUtil.run2(var1, 0, 0, var9, var10, 6, -301265901);
         AdminDetectorModuleUtil.run3(var1, 0, 0, var9, 3, 3, 0, var13);
         var1.drawText(var3, Text.literal("ADMIN LIST"), 8, 10, var13, false);
         String var15 = var4.size() + " ONLINE";
         int var16 = var3.getWidth(var15) + 10;
         AdminDetectorModuleUtil.run2(var1, var9 - 8 - var16, 7, var16, 12, 6, AdminDetectorModuleUtil.intOf(var13, 0.22F));
         var1.drawText(var3, Text.literal(var15), var9 - 8 - var16 + 5, 10, AdminDetectorModuleUtil.intOf2(var13, -1, 0.55F), false);
         AdminDetectorModuleUtil.run(var1, 8, 22, var9 - 16, 1, AdminDetectorModuleUtil.intOf(var13, 0.22F));
         if (var4.isEmpty()) {
            var1.drawText(var3, Text.literal("No admins online"), 8, 30, -7565402, false);
            var14.pop();
         } else {
            byte var17 = 26;
            int var18 = 0;

            for (AdminDetectorModule.Inner4 var20 : (Iterable<AdminDetectorModule.Inner4>)(Object)(var4)) {
               float var21 = AdminDetectorModuleUtil.floatOf(this.map3.getOrDefault(var20.displayName(), 0.0F), 1.0F, var7, 9.0F);
               this.map3.put(var20.displayName(), var21);
               this.run4(var1, var3, var20, var17, var9, var18, var21, var13);
               var17 += 20;
               var18++;
            }

            var14.pop();
         }
      }
   }

   private void run4(DrawContext var1, TextRenderer var2, AdminDetectorModule.Inner4 var3, int var4, int var5, int var6, float var7, int var8) {
      int var9 = Math.round((1.0F - var7) * 6.0F);
      int var10 = var4 + var9;
      int var11 = var6 % 2 == 0 ? -15461089 : -15724262;
      AdminDetectorModuleUtil.run2(var1, 5, var10, var5 - 10, 18, 4, AdminDetectorModuleUtil.intOf(var11, 0.75F * var7));
      if (var3.online()) {
         AdminDetectorModuleUtil.run2(var1, 5, var10 + 3, 2, 12, 1, AdminDetectorModuleUtil.intOf(var8, var7));
      }

      int var13 = var10 + 1;
      AdminDetectorModuleUtil.run2(var1, 9, var13 - 1, 18, 18, 3, AdminDetectorModuleUtil.intOf(var8, 0.35F * var7));
      this.run9(var1, var3, 10, var13, 16);
      var1.drawText(var2, Text.literal(var3.displayName()), 32, var10 + 5, AdminDetectorModuleUtil.intOf(-1381654, var7), false);
      String var15 = var3.online() ? "online" : "offline";
      int var16 = var3.online() ? -10754677 : -1021334;
      int var17 = var2.getWidth(var15) + 14;
      int var18 = var5 - 8 - var17;
      int var19 = var10 + 3;
      AdminDetectorModuleUtil.run2(var1, var18, var19, var17, 11, 5, AdminDetectorModuleUtil.intOf(var16, 0.18F * var7));
      AdminDetectorModuleUtil.run2(var1, var18 + 5, var19 + 4, 3, 3, 1, AdminDetectorModuleUtil.intOf(var16, var7));
      var1.drawText(var2, Text.literal(var15), var18 + 11, var19 + 2, AdminDetectorModuleUtil.intOf(var16, var7), false);
   }

   private static int intOf(int var0) {
      return 26 + Math.max(var0, 1) * 20 + 8 - 2;
   }

   private int intOf2(TextRenderer var1, List<AdminDetectorModule.Inner4> var2) {
      int var3 = var1.getWidth("ADMIN LIST") + var1.getWidth(var2.size() + " ONLINE") + 24;

      for (AdminDetectorModule.Inner4 var5 : var2) {
         String var6 = var5.online() ? "online" : "offline";
         var3 = Math.max(var3, var1.getWidth(var5.displayName()) + var1.getWidth(var6) + 32);
      }

      return Math.max(180, 40 + var3);
   }

   int getInt() {
      TextRenderer var1 = class310.textRenderer;
      return var1 == null ? Math.round(180 * this.val8.getObject() / 100.0F) : Math.round(this.intOf2(var1, this.getList()) * this.val8.getObject() / 100.0F);
   }

   int getInt5() {
      return Math.round(intOf(this.getList().size()) * this.val8.getObject() / 100.0F);
   }

   private List<AdminDetectorModule.Inner4> getList() {
      return this.map.values().stream().filter(AdminDetectorModule.Inner4::online).sorted(Comparator.comparing(AdminDetectorModule::stringOf)).toList();
   }

   private void run8(AdminDetectorModule.Inner4 var1, boolean var2) {
      String var3 = var1.displayName();
      SkinTextures var4 = var1.skinTextures() != null ? var1.skinTextures() : this.map2.get(var1.key());
      this.val9.run(new Inner4(var1.key(), var3, var2, var4));
      if (class310.player != null) {
         String var5 = var2 ? "joined" : "left";
         class310.player.sendMessage(Text.literal("null null."), false);
         class310.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, var2 ? 1.2F : 0.8F);
      }
   }

   private void run9(DrawContext var1, AdminDetectorModule.Inner4 var2, int var3, int var4, int var5) {
      if (var2.skinTextures() != null) {
         PlayerSkinDrawer.draw(var1, var2.skinTextures(), var3, var4, var5);
      } else {
         this.run10(var1, var2.online(), var3, var4, var5);
      }
   }

   private void run10(DrawContext var1, boolean var2, int var3, int var4, int var5) {
      int var6 = var2 ? -14861524 : -12969182;
      int var7 = var2 ? -10754677 : -1021334;
      AdminDetectorModuleUtil.run2(var1, var3, var4, var5, var5, 3, var6);
      AdminDetectorModuleUtil.run2(var1, var3 + 4, var4 + 5, 2, 2, 1, -1);
      AdminDetectorModuleUtil.run2(var1, var3 + var5 - 6, var4 + 5, 2, 2, 1, -1);
      AdminDetectorModuleUtil.run2(var1, var3 + 5, var4 + var5 - 6, var5 - 10, 2, 1, var7);
   }

   private void run11(String var1, SkinTextures var2) {
      if (var2 != null) {
         this.map2.put(var1, var2);
      }
   }

   private static String addSetting(String var0) {
      String var1 = addSetting2(var0 == null ? "" : var0);
      return Normalizer.normalize(var1, Form.NFKC)
         .replace("\u200b", "")
         .replace("\u200c", "")
         .replace("\u200d", "")
         .replace("\ufeff", "")
         .trim()
         .toLowerCase(Locale.ROOT);
   }

   private static String addSetting2(String var0) {
      return var0.replaceAll("(?i)Â§[0-9A-FK-OR]", "").trim();
   }

   private static String stringOf(AdminDetectorModule.Inner4 var0) {
      return var0.displayName().toLowerCase(Locale.ROOT);
   }

   private static boolean check(List var0, String var1) {
      return var0.stream().noneMatch(var905 -> AdminDetectorModule.check2(var1, (AdminDetectorModule.Inner4)var905));
   }

   private static boolean check2(String var0, AdminDetectorModule.Inner4 var1) {
      return var1.displayName().equals(var0);
   }

   final class Inner1 {
      private String displayName;
      private SkinTextures skinTextures;

      Inner1(String var1, SkinTextures var2) {
         this.displayName = var1;
         this.skinTextures = var2;
      }

      public String displayName() {
         return this.displayName;
      }

      public SkinTextures skinTextures() {
         return this.skinTextures;
      }
   }

   final class Inner2 {
      private static final long longVal = 3000L;
      private static final int intVal = 150;
      private static final int intVal2 = 24;
      private final ConcurrentLinkedQueue<AdminDetectorModule.Inner3> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

      void run(AdminDetectorModule.Inner4 var1) {
         while (this.concurrentLinkedQueue.size() >= 3) {
            this.concurrentLinkedQueue.poll();
         }

         this.concurrentLinkedQueue.add(new Inner3(var1, System.currentTimeMillis() + 3000L));
      }

      void run2(DrawContext var1, MinecraftClient var2) {
         if (var2 != null && var2.getWindow() != null && var2.textRenderer != null) {
            long var3 = System.currentTimeMillis();
            this.concurrentLinkedQueue.removeIf(a0x -> AdminDetectorModule.Inner2.check(var3, a0x));
            int var5 = var2.getWindow().getScaledWidth() - 150 - 10;
            byte var6 = 10;

            for (AdminDetectorModule.Inner3 var8 : this.concurrentLinkedQueue) {
               AdminDetectorModule.Inner4 var9 = var8.player();
               int var10 = var9.online() ? -10754677 : -1021334;
               String var11 = var9.displayName() + (var9.online() ? " joined" : " left");
               var1.fill(var5, var6, var5 + 150, var6 + 24, -871296751);
               var1.fill(var5, var6, var5 + 3, var6 + 24, var10);
               if (var9.skinTextures() != null) {
                  PlayerSkinDrawer.draw(var1, var9.skinTextures(), var5 + 8, var6 + 4, 16);
               }

               var1.drawText(var2.textRenderer, Text.literal(var11), var5 + 30, var6 + 8, -1, false);
               var6 += 28;
            }
         }
      }

      void run3() {
         this.concurrentLinkedQueue.clear();
      }

      private static boolean check(long var0, AdminDetectorModule.Inner3 var2) {
         return var2.expiresAt() < var0;
      }
   }

   final class Inner3 {
      private AdminDetectorModule.Inner4 player;
      private long expiresAt;

      Inner3(AdminDetectorModule.Inner4 var1, long var2) {
         this.player = var1;
         this.expiresAt = var2;
      }

      public AdminDetectorModule.Inner4 player() {
         return this.player;
      }

      public long expiresAt() {
         return this.expiresAt;
      }
   }

   final class Inner4 {
      private String key;
      private String displayName;
      private boolean online;
      private SkinTextures skinTextures;

      Inner4(String var1, String var2, boolean var3, SkinTextures var4) {
         this.key = var1;
         this.displayName = var2;
         this.online = var3;
         this.skinTextures = var4;
      }

      public String key() {
         return this.key;
      }

      public String displayName() {
         return this.displayName;
      }

      public boolean online() {
         return this.online;
      }

      public SkinTextures skinTextures() {
         return this.skinTextures;
      }
   }
}
