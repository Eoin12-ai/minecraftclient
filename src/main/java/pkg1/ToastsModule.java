package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import util.Utils_2;

public final class ToastsModule extends Module {
   private static ToastsModule val_2;
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Filters");
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Appearance");
   private final Setting<Boolean> val4 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("sound").valOf2("Plays a short sound for new notifications.").valOf3(true).getVal());
   private final Setting<Boolean> val5 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("sus-chunks").valOf2("Shows suspicious chunk and amethyst finder notifications.").valOf3(true).getVal()
      );
   private final Setting<Boolean> val6 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("spawners").valOf2("Shows spawner finder notifications.").valOf3(true).getVal());
   private final Setting<Boolean> val7 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("shulkers").valOf2("Shows shulker related notifications.").valOf3(true).getVal());
   private final Setting<Boolean> val8 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("compact-amethyst-text")
            .valOf2("Uses a shorter single-line message for amethyst detections.")
            .valOf3(false)
            .getVal()
      );
   private final Setting<Integer> val9 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("duration-seconds")
            .valOf2("How long each notification remains visible.")
            .valOf3(5)
            .valOf4(1, 15)
            .valOf5(1, 15)
            .getVal()
      );
   private final Setting<Integer> val10 = this.val3_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("max-visible")
            .valOf2("Maximum notifications displayed at the same time.")
            .valOf3(3)
            .valOf4(1, 6)
            .valOf5(1, 6)
            .getVal()
      );
   private final Deque<ToastsModule.Inner2> deque = new ArrayDeque<>();

   public ToastsModule() {
      super(SwyzzyAddon.val6, "toasts", "Controls and displays Swyzzy Client finder notifications.");
      Utils_2.run(this, "Toasts");
      val_2 = this;
   }

   @Override
   public void run7() {
      this.deque.clear();
   }

   public static void run10(String var0, String var1) {
      ToastsModule var2 = val_2;
      if (var2 != null && var2.isEnabled()) {
         var2.run17(var0, var1);
      }
   }

   private void run17(String var1, String var2) {
      ToastsModule.State var3 = ToastsModule.State.valOf(var1, var2);
      if (this.check(var3)) {
         long var4 = System.currentTimeMillis();
         String var6 = this.val8.getObject() && var3 == ToastsModule.State.SUS_CHUNK ? addSetting(var2) : var2;
         this.deque.addLast(new Inner2(var1, var6, var4 + this.val9.getObject().intValue() * 1000L));

         while (this.deque.size() > 12) {
            this.deque.removeFirst();
         }

         if (this.val4.getObject() && class310.player != null) {
            class310.player.playSound((SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.8F, 1.25F);
         }
      }
   }

   @InternalHelper5
   private void run(AdminDetectorModuleData var1) {
      if (class310.getWindow() != null && class310.textRenderer != null) {
         long var2 = System.currentTimeMillis();
         this.deque.removeIf(var905 -> ToastsModule.check2(var2, var905));
         if (!this.deque.isEmpty()) {
            int var4 = class310.getWindow().getScaledWidth();
            byte var5 = 8;
            int var6 = Math.max(0, this.deque.size() - this.val10.getObject());
            int var7 = 0;
            int var8 = SwyzzyAddon.val7.getVal().intOf(255);

            for (ToastsModule.Inner2 var10 : this.deque) {
               if (var7++ >= var6) {
                  String var11 = stringOf(var10.title().toUpperCase(Locale.ROOT), 30);
                  String var12 = stringOf(var10.message(), 54);
                  int var13 = Math.max(class310.textRenderer.getWidth(var11), class310.textRenderer.getWidth(var12)) + 14;
                  int var14 = var4 - var13 - 8;
                  var1.class332.fill(var14, var5, var14 + var13, var5 + 27, -451931623);
                  var1.class332.fill(var14, var5, var14 + 3, var5 + 27, var8);
                  var1.class332.drawText(class310.textRenderer, Text.literal(var11), var14 + 7, var5 + 4, -1, true);
                  var1.class332.drawText(class310.textRenderer, Text.literal(var12), var14 + 7, var5 + 15, -2499096, false);
                  var5 += 31;
               }
            }
         }
      }
   }

   private boolean check(ToastsModule.State var1) {
      return switch (var1) {
         case SUS_CHUNK -> this.val5.getObject();
         case SPAWNER -> this.val6.getObject();
         case SHULKER -> this.val7.getObject();
         case GENERAL -> true;
      };
   }

   private static String addSetting(String var0) {
      int var1 = var0.toLowerCase(Locale.ROOT).lastIndexOf(" at ");
      return var1 >= 0 ? "Amethyst detected" + var0.substring(var1) : stringOf(var0, 38);
   }

   private static String stringOf(String var0, int var1) {
      return var0.length() <= var1 ? var0 : var0.substring(0, Math.max(1, var1 - 3)) + "...";
   }

   private static boolean check2(long var0, ToastsModule.Inner2 var2) {
      return var2.expiresAt() <= var0;
   }

   final class Inner2 {
      private String title;
      private String message;
      private long expiresAt;

      Inner2(String var1, String var2, long var3) {
         this.title = var1;
         this.message = var2;
         this.expiresAt = var3;
      }

      public String title() {
         return this.title;
      }

      public String message() {
         return this.message;
      }

      public long expiresAt() {
         return this.expiresAt;
      }
   }

   enum State {
      SUS_CHUNK,
      SPAWNER,
      SHULKER,
      GENERAL;

      static ToastsModule.State valOf(String var0, String var1) {
         String var2 = "null null".toLowerCase(Locale.ROOT);
         if (var2.contains("spawner")) {
            return SPAWNER;
         } else if (var2.contains("shulker")) {
            return SHULKER;
         } else {
            return !var2.contains("amethyst") && !var2.contains("geode") && !var2.contains("sus chunk") && !var2.contains("tuff chunk") ? GENERAL : SUS_CHUNK;
         }
      }

      private static ToastsModule.State[] getValArray() {
         return new ToastsModule.State[]{SUS_CHUNK, SPAWNER, SHULKER, GENERAL};
      }
   }
}
