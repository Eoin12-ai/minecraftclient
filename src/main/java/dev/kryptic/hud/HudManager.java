package dev.kryptic.hud;

import com.google.gson.JsonObject;
import dev.kryptic.hud.components.ArmorHud;
import dev.kryptic.hud.components.ArrayListHud;
import dev.kryptic.hud.components.InfoHud;
import dev.kryptic.hud.components.KeystrokesHud;
import dev.kryptic.hud.components.MetersHud;
import dev.kryptic.hud.components.PotionsHud;
import dev.kryptic.hud.components.RadarHud;
import dev.kryptic.hud.components.RegionMapHud;
import dev.kryptic.hud.components.SpotifyHud;
import dev.kryptic.hud.components.StaffListHud;
import dev.kryptic.hud.components.StatsHud;
import dev.kryptic.hud.components.WatermarkHud;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.notification.NotificationManager;
import dev.kryptic.render.nanovg.NVGRenderer;
import dev.kryptic.spotify.SpotifyService;
import dev.kryptic.theme.ThemeManager;
import dev.kryptic.util.CpsTracker;
import dev.kryptic.util.TpsTracker;
import dev.kryptic.KrypticClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import dev.kryptic.module.client.HudModule;
public class HudManager {
   private final List<HudComponent> components = new ArrayList<>();
   private final HudModule hudModule;

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
         KrypticClient.LOGGER.error(
               "HUD component '{}' failed; disabling it for this session", component.getId(), error);
      }
   }

   public HudManager(ModuleManager moduleManager, ThemeManager themeManager, SpotifyService spotifyService, NotificationManager notificationManager) {
      HudModule hudModule = moduleManager.hud;
      this.hudModule = hudModule;
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
            ).tinted(shown -> grade(hudModule, parseLeadingInt(shown), 60, 30, false))
         );
      this.components
         .add(
            new InfoHud("ping", themeManager, "Ping", HudManager::pingString, 0.055F, 0.985F, () -> hudModule.isEnabled() && hudModule.ping.get())
               .tinted(shown -> grade(hudModule, parseLeadingInt(shown), 80, 150, true))
         );
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
      this.components.add(new MetersHud(hudModule, themeManager));
      this.components.add(notificationManager);
   }

   /** Readouts carry a unit ("120ms"); only the number in front is graded. */
   private static int parseLeadingInt(String shown) {
      int end = 0;
      while (end < shown.length() && Character.isDigit(shown.charAt(end))) end++;
      return end == 0 ? Integer.MIN_VALUE : Integer.parseInt(shown.substring(0, end));
   }

   /**
    * Green / amber / red for a readout, or the plain text colour when the
    * value could not be read or the user has turned grading off.
    *
    * {@code lowerIsBetter} flips the comparison, so the same helper grades a
    * framerate and a latency.
    */
   private static int grade(HudModule hudModule, int value, int good, int fair, boolean lowerIsBetter) {
      if (!hudModule.colourCode.get() || value == Integer.MIN_VALUE) return COLOUR_NEUTRAL;
      boolean isGood = lowerIsBetter ? value < good : value >= good;
      boolean isFair = lowerIsBetter ? value < fair : value >= fair;
      return isGood ? COLOUR_GOOD : (isFair ? COLOUR_FAIR : COLOUR_POOR);
   }

   private static final int COLOUR_GOOD    = 0xFF6BE675;
   private static final int COLOUR_FAIR    = 0xFFFFC85C;
   private static final int COLOUR_POOR    = 0xFFFF6B6B;
   /** Sentinel meaning "leave it alone"; resolved against the theme at draw time. */
   private static final int COLOUR_NEUTRAL = 0;

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
         return playerListEntry == null ? "—" : playerListEntry.getLatency() + "ms";
      } else {
         return "—";
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

   /**
    * How large a component actually draws: its own size times the HUD-wide one.
    *
    * Layout and render both have to ask this. They each used to call
    * getScale() separately, which was fine while that was the whole answer —
    * the moment a second factor exists, one of them measuring at a different
    * size than the other puts every panel's border in the wrong place.
    */
   public float effectiveScale(HudComponent component) {
      float global = this.hudModule == null ? 1.0F : this.hudModule.scale.getFloat();
      return Math.clamp(component.getScale() * global, 0.25F, 6.0F);
   }

   public List<HudManager.Placement> layout(NVGRenderer nVGRenderer, float f, float f8, boolean value) {
      ArrayList list = new ArrayList();

      for (HudComponent hudComponent : this.components) {
         if (this.failed.contains(hudComponent)) {
            continue;
         }

         try {
            if (value || hudComponent.visible()) {
               float f9 = this.effectiveScale(hudComponent);
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

      float f = this.effectiveScale(component);
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
