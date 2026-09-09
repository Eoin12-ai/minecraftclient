package dev.kryptic.staff;

import dev.kryptic.module.misc.StaffListModule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.GameMode;

public final class StaffTracker {
   private static final int SCAN_INTERVAL = 10;
   private final StaffListModule module;
   private volatile List<StaffEntry> current = List.of();
   private Set<String> lastNames = new HashSet<>();
   private boolean primed;
   private int ticks;
   private static volatile List<StaffEntry> debugInject;

   public StaffTracker(StaffListModule staffListModule) {
      this.module = staffListModule;
   }

   public List<StaffEntry> current() {
      List list = debugInject;
      return list != null ? list : this.current;
   }

   public void reset() {
      this.lastNames = new HashSet<>();
      this.primed = false;
      this.current = List.of();
      this.ticks = 0;
   }

   public void clear() {
      this.reset();
   }

   public void tick() {
      if (debugInject == null && ++this.ticks % 10 == 0) {
         this.scan();
      }
   }

   private void scan() {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayNetworkHandler clientPlayNetworkHandler = client.getNetworkHandler();
      if (clientPlayNetworkHandler != null && client.player != null) {
         StaffDetector.DetectConfig detectConfig = this.module.detectConfig();
         UUID uuid = client.player.getUuid();
         HashSet set = new HashSet();

         for (PlayerListEntry playerListEntry : clientPlayNetworkHandler.getListedPlayerListEntries()) {
            set.add(playerListEntry.getProfile().id());
         }

         ArrayList<StaffEntry> list = new ArrayList<>();

         for (PlayerListEntry playerListEntry2 : clientPlayNetworkHandler.getPlayerList()) {
            UUID uuid2 = playerListEntry2.getProfile().id();
            if (!uuid2.equals(uuid)) {
               String name2 = playerListEntry2.getProfile().name();
               if (name2 != null && !name2.isEmpty()) {
                  boolean matches = playerListEntry2.getGameMode() == GameMode.SPECTATOR || !set.contains(uuid2);
                  if (!matches || detectConfig.showVanished()) {
                     Team team = playerListEntry2.getScoreboardTeam();
                     StaffEntry staffEntry = StaffDetector.classify(
                        name2,
                        playerListEntry2.getDisplayName(),
                        team == null ? null : team.getPrefix(),
                        team == null ? null : team.getSuffix(),
                        team == null ? null : team.getName(),
                        matches,
                        playerListEntry2.getLatency(),
                        detectConfig
                     );
                     if (staffEntry != null) {
                        list.add(staffEntry);
                     }
                  }
               }
            }
         }

         list.sort(
            Comparator.comparing(StaffEntry::vanished)
               .thenComparing(Comparator.comparingInt(StaffEntry::priority).reversed())
               .thenComparing(arg -> arg.name().toLowerCase(Locale.ROOT))
         );
         HashSet set2 = new HashSet();

         for (StaffEntry staffEntry2 : list) {
            set2.add(staffEntry2.name());
         }

         if (this.primed) {
            for (StaffEntry staffEntry3 : list) {
               if (!this.lastNames.contains(staffEntry3.name())) {
                  this.module.onStaffAppear(staffEntry3);
               }
            }
         }

         this.lastNames = set2;
         this.primed = true;
         this.current = List.copyOf(list);
      } else {
         this.current = List.of();
         this.lastNames = new HashSet<>();
         this.primed = false;
      }
   }

   public static void injectForTest(List<StaffEntry> list) {
      debugInject = list == null ? null : List.copyOf(list);
   }

   public static void clearInject() {
      debugInject = null;
   }
}
