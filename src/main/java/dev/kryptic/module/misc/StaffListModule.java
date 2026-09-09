package dev.kryptic.module.misc;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.settings.StringSetting;
import dev.kryptic.staff.StaffDetector;
import dev.kryptic.staff.StaffEntry;
import dev.kryptic.staff.StaffTracker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public class StaffListModule extends Module {
   public final ModeSetting detectBy = this.addSetting(
      new ModeSetting(
         "Detect By",
         "How staff are recognised: the DonutSMP coloured star, a text rank prefix, or both",
         "Star + Rank",
         "Star + Rank",
         "Star Only",
         "Rank Only",
         "Names Only"
      )
   );
   public final StringSetting staffNames = this.addSetting(
      new StringSetting("Staff Names", "Extra known staff usernames (comma-separated) — always shown when online", "", 220, "e.g. Notch, jeb_")
   );
   public final StringSetting rankKeywords = this.addSetting(
      new StringSetting(
         "Rank Keywords",
         "Words in a name tag that mark staff (comma-separated), most senior first",
         StaffDetector.DEFAULT_RANK_KEYWORDS_STRING,
         256,
         "owner, admin, mod…"
      )
   );
   public final StringSetting starSymbols = this.addSetting(
      new StringSetting(
         "Star Symbols",
         "Marker glyphs that mean 'staff'. Paste the server's star here if detection misses",
         "\u2605\u2606\u272A\u2726\u2727\u2B50\u272F\u269D",
         96,
         "\u2605\u2606\u272A\u2726\u2727\u2B50\u272F\u269D"
      )
   );
   public final BooleanSetting fontIcons = this.addSetting(
      new BooleanSetting("Font Icons", "Also treat custom resource-pack icons (private-use glyphs) as staff stars", true)
   );
   public final BooleanSetting showRank = this.addSetting(
      new BooleanSetting("Show Rank", "Show each staff member's rank label when the server exposes one", true)
   );
   public final BooleanSetting showPing = this.addSetting(new BooleanSetting("Show Ping", "Show each staff member's latency", false));
   public final BooleanSetting vanished = this.addSetting(
      new BooleanSetting("Vanished Staff", "Include soft-vanished staff (spectator / hidden from tab), marked separately", true)
   );
   public final SliderSetting maxRows = this.addSetting(
      new SliderSetting("Max Rows", "Most staff rows to show before collapsing into a '+N more' line", 6.0, 1.0, 20.0, 1.0)
   );
   public final ModeSetting alerts = this.addSetting(
      new ModeSetting("Alerts", "Announce when a new staff member appears in your tab", "Toast", "Toast", "Chat", "Off")
   );
   public final BooleanSetting alertSound = this.addSetting(new BooleanSetting("Alert Sound", "Play a bell when a staff alert fires", true));
   public final StaffTracker tracker = new StaffTracker(this);

   public StaffListModule() {
      super("StaffList", "Lists online staff on the HUD (DonutSMP coloured-star detection)", Category.MISC);
      this.rankKeywords.visibleWhen(this::usesRank);
      this.starSymbols.visibleWhen(this::usesStar);
      this.fontIcons.visibleWhen(this::usesStar);
      this.alertSound.visibleWhen(() -> !this.alerts.is("Off"));
   }

   private boolean usesStar() {
      return this.detectBy.is("Star + Rank") || this.detectBy.is("Star Only");
   }

   private boolean usesRank() {
      return this.detectBy.is("Star + Rank") || this.detectBy.is("Rank Only");
   }

   @Override
   protected void onEnable() {
      this.tracker.reset();
   }

   @Override
   protected void onDisable() {
      this.tracker.clear();
   }

   @Override
   public void onTick() {
      this.tracker.tick();
   }

   public List<StaffEntry> staff() {
      return this.tracker.current();
   }

   public StaffDetector.DetectConfig detectConfig() {
      List list = parseKeywords(this.rankKeywords.get());
      return new StaffDetector.DetectConfig(
         this.detectBy.get(),
         parseNames(this.staffNames.get()),
         list.isEmpty() ? StaffDetector.DEFAULT_RANK_KEYWORDS : list,
         this.starSymbols.get(),
         this.fontIcons.get(),
         this.vanished.get()
      );
   }

   public void onStaffAppear(StaffEntry staffEntry) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && !this.alerts.is("Off")) {
         String text = staffEntry.rankLabel().isEmpty() ? "Staff" : staffEntry.rankLabel();
         String text3 = staffEntry.vanished() ? " (vanished)" : "";
         if (this.alerts.is("Toast")) {
            if (KrypticClient.notifications() != null) {
               KrypticClient.notifications().pushInfo(text + " " + staffEntry.name() + " online" + text3);
            }
         } else if (this.alerts.is("Chat")) {
            client.player.sendMessage(Text.literal("§d[Kryptic] §f" + text + " §b" + staffEntry.name() + "§7 is online" + text3), false);
         }

         if (this.alertSound.get()) {
            client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1.5F));
         }
      }
   }

   private static Set<String> parseNames(String text) {
      HashSet set = new HashSet();

      for (String item : text.split(",")) {
         if (!item.isEmpty()) {
            set.add(item.toLowerCase(Locale.ROOT));
         }
      }

      return set;
   }

   private static List<String> parseKeywords(String text) {
      ArrayList list = new ArrayList();

      for (String text3 : text.split(",")) {
         String trimmed = text3.trim().toLowerCase(Locale.ROOT);
         if (!trimmed.isEmpty()) {
            list.add(trimmed);
         }
      }

      return list;
   }
}
