package dev.sixseven.hud;

import com.google.gson.JsonObject;
import dev.sixseven.hud.components.ArmorHud;
import dev.sixseven.hud.components.ArrayListHud;
import dev.sixseven.hud.components.InfoHud;
import dev.sixseven.hud.components.KeystrokesHud;
import dev.sixseven.hud.components.PotionsHud;
import dev.sixseven.hud.components.RadarHud;
import dev.sixseven.hud.components.RegionMapHud;
import dev.sixseven.hud.components.SpotifyHud;
import dev.sixseven.hud.components.StaffListHud;
import dev.sixseven.hud.components.StatsHud;
import dev.sixseven.hud.components.WatermarkHud;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.notification.NotificationManager;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.spotify.SpotifyService;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.CpsTracker;
import dev.sixseven.util.TpsTracker;
import dev.sixseven.SixSevenClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import dev.sixseven.module.client.HudModule;
public class HudManager {
   private final List<HudComponent> components = new ArrayList<>();

   /**
    * Components that threw once and are skipped from then on.
    *
    * OverlayRenderer catches anything escaping this class by disabling the
    * whole overlay for the rest of the session, so without this a single bad
    * component takes the watermark, the arraylist and every readout down with
    * it. Isolating failures here keeps the blast radius to the one component.
    */
   private final Set<HudComponent> failed = new HashSet<>();

   private void markFailed(HudComponent component, Throwable error) {
      if (failed.add(component)) {
         SixSevenClient.LOGGER.error(
               "HUD component '{}' failed; disabling it for this session", component.getId(), error);
      }
   }

   public HudManager(ModuleManager moduleManager, ThemeManager themeManager, SpotifyService spotifyService, NotificationManager notificationManager) {
      HudModule hudModule = moduleManager.hud;
      this.components.add(new WatermarkHud(themeManager, () -> hudModule.isEnabled() && hudModule.watermark.get()));
      this.components.add(new ArrayListHud(moduleManager, hudModule, themeManager, () -> hudModule.isEnabled() && hudModule.arrayList.get()));
      this.components
         .add(
            new InfoHud(
               "fps",
               themeManager,
               "FPS",
               () -> Integer.toString(MinecraftClient.getInstance().getCurrentFps()),
               0.006F,
               0.985F,
               () -> hudModule.isEnabled() && hudModule.fps.get()
            )
         );
      this.components.add(new InfoHud("ping", themeManager, "Ping", HudManager::pingString, 0.055F, 0.985F, () -> hudModule.isEnabled() && hudModule.ping.get()));
      this.components.add(new InfoHud("coords", themeManager, "XYZ", HudManager::coordsString, 0.115F, 0.985F, () -> hudModule.isEnabled() && hudModule.coordinates.get()));
      this.components.add(new InfoHud("direction", themeManager, "Facing", HudManager::directionString, 0.24F, 0.985F, () -> hudModule.isEnabled() && hudModule.direction.get()));
      this.components
         .add(new InfoHud("tps", themeManager, "TPS", () -> String.format("%.1f", TpsTracker.get()), 0.33F, 0.985F, () -> hudModule.isEnabled() && hudModule.tps.get()));
      this.components.add(new InfoHud("cps", themeManager, "CPS", () -> {
         int n = CpsTracker.get(0);
         return n + " | " + CpsTracker.get(1);
      }, 0.4F, 0.985F, () -> hudModule.isEnabled() && hudModule.cps.get()));
      this.components.add(new ArmorHud(themeManager, () -> hudModule.isEnabled() && hudModule.armor.get()));
      this.components.add(new PotionsHud(themeManager, () -> hudModule.isEnabled() && hudModule.potions.get()));
      this.components.add(new KeystrokesHud(themeManager, () -> hudModule.isEnabled() && hudModule.keystrokes.get()));
      this.components.add(new RadarHud(hudModule, moduleManager.susChunkFinder, themeManager, () -> hudModule.isEnabled() && hudModule.radar.get()));
      this.components.add(new RegionMapHud(moduleManager.regionMap, themeManager));
      this.components.add(new StaffListHud(moduleManager.staffList, themeManager));
      this.components.add(new SpotifyHud(moduleManager.spotify, spotifyService, themeManager));
      this.components.add(new StatsHud(moduleManager.stats, themeManager));
      this.components.add(notificationManager);
   }

   private static String coordsString() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null) {
         return "0, 0, 0";
      } else {
         BlockPos pos = player.getBlockPos();
         int n = pos.getX();
         return n + ", " + pos.getY() + ", " + pos.getZ();
      }
   }

   private static String pingString() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.getNetworkHandler() != null) {
         PlayerListEntry playerListEntry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
         return playerListEntry == null ? "CA;" : "H{" + playerListEntry.getLatency();
      } else {
         return "CA;";
      }
   }

   private static String directionString() {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (player == null) {
         return "=";
      } else {
         Direction direction = player.getHorizontalFacing();

         return switch (direction) {
            case NORTH -> "N  -Z";
            case SOUTH -> "S  +Z";
            case WEST -> "W  -X";
            case EAST -> "E  +X";
            default -> direction.getId().toUpperCase();
         };
      }
   }

   public List<HudComponent> getComponents() {
      return this.components;
   }

   public List<HudManager.Placement> layout(NVGRenderer nVGRenderer, float f, float f8, boolean value) {
      ArrayList list = new ArrayList();

      for (HudComponent hudComponent : this.components) {
         if (this.failed.contains(hudComponent)) {
            continue;
         }

         try {
            if (value || hudComponent.visible()) {
               float f9 = hudComponent.getScale();
               float f10 = hudComponent.measureWidth(nVGRenderer) * f9;
               float f11 = hudComponent.measureHeight(nVGRenderer) * f9;
               float f12 = hudComponent.getFx() * (f - f10);
               float f13 = hudComponent.getFy() * (f8 - f11);
               list.add(new HudManager.Placement(hudComponent, f12, f13, f10, f11));
            }
         } catch (Throwable error) {
            this.markFailed(hudComponent, error);
         }
      }

      return list;
   }

   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2) {
      for (HudManager.Placement placement : this.layout(nVGRenderer, tickDelta, tickDelta2, false)) {
         this.renderPlacement(nVGRenderer, placement);
      }
   }

   public void renderPlacement(NVGRenderer nVGRenderer, HudManager.Placement placement) {
      HudComponent component = placement.component();
      if (this.failed.contains(component)) {
         return;
      }

      float f = component.getScale();
      // save/restore must be balanced even when the component throws, or the
      // NanoVG state stack is left skewed and every later draw is affected
      nVGRenderer.save();

      try {
         nVGRenderer.translate((float)Math.round(placement.x()), (float)Math.round(placement.y()));
         nVGRenderer.scale(f);
         component.render(nVGRenderer, 0.0F, 0.0F, placement.w() / f, placement.h() / f);
      } catch (Throwable error) {
         this.markFailed(component, error);
      } finally {
         nVGRenderer.restore();
      }
   }

   public JsonObject toJson() {
      JsonObject jsonObject = new JsonObject();

      for (HudComponent hudComponent : this.components) {
         JsonObject jsonObject2 = new JsonObject();
         jsonObject2.addProperty("fx", hudComponent.getFx());
         jsonObject2.addProperty("fy", hudComponent.getFy());
         jsonObject2.addProperty("scale", hudComponent.getScale());
         jsonObject.add(hudComponent.getId(), jsonObject2);
      }

      return jsonObject;
   }

   public void fromJson(JsonObject jsonObject) {
      for (HudComponent hudComponent : this.components) {
         JsonObject id = jsonObject.getAsJsonObject(hudComponent.getId());
         if (id != null && id.has("fx") && id.has("fy")) {
            hudComponent.setPosition(id.get("fx").getAsFloat(), id.get("fy").getAsFloat());
            if (id.has("scale")) {
               hudComponent.setScale(id.get("scale").getAsFloat());
            }
         }
      }
   }

   public static record Placement(HudComponent component, float x, float y, float w, float h) {
      public boolean contains(float f, float f3) {
         return f >= this.x && f <= this.x + this.w && f3 >= this.y && f3 <= this.y + this.h;
      }
   }
}
