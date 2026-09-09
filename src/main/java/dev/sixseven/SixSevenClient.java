package dev.sixseven;

import dev.sixseven.config.ConfigManager;
import dev.sixseven.config.ConfigStore;
import dev.sixseven.config.ServerConfigs;
import dev.sixseven.gui.ClickGuiScreen;
import dev.sixseven.gui.ClickGuiState;
import dev.sixseven.hud.HudManager;
import dev.sixseven.module.Category;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.module.misc.FreecamModule;
import dev.sixseven.notification.NotificationManager;
import dev.sixseven.render.MotionBlurRenderer;
import dev.sixseven.render.OverlayRenderer;
import dev.sixseven.render.WorldRenderHook;
import dev.sixseven.render.NvgDrawable;
import dev.sixseven.render.SusChunkRenderer;
import dev.sixseven.spotify.SpotifyService;
import dev.sixseven.suschunk.ServerLightCache;
import dev.sixseven.theme.SoundSettings;
import dev.sixseven.theme.ThemeManager;
import dev.sixseven.util.UiSoundEvents;
import dev.sixseven.util.UiSounds;
import java.util.function.Supplier;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.StartTick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterInit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.sixseven.discord.DiscordPresenceService;
import dev.sixseven.stats.StatsTracker;
import dev.sixseven.wm.Seed;
public class SixSevenClient implements ClientModInitializer {
   public static final String MOD_ID = "sixsevenclient";
   public static final String NAME = "Epstein Client";
   public static final String VERSION = "1.6.2";
   public static final Logger LOGGER = LoggerFactory.getLogger("EpsteinClient");
   private static ModuleManager modules;
   private static ThemeManager themes;
   private static ConfigManager config;
   private static ConfigStore configStore;
   private static HudManager hud;
   private static NotificationManager notifications;
   private static SpotifyService spotify;
   private static DiscordPresenceService discord;
   private static ServerConfigs serverConfigs;
   private static SoundSettings soundSettings;
   private static boolean startupSoundPlayed;

   public static ModuleManager modules() {
      return modules;
   }

   public static ThemeManager themes() {
      return themes;
   }

   public static ConfigManager config() {
      return config;
   }

   public static ConfigStore configStore() {
      return configStore;
   }

   public static HudManager hud() {
      return hud;
   }

   public static SpotifyService spotify() {
      return spotify;
   }

   public static DiscordPresenceService discord() {
      return discord;
   }

   public static SoundSettings sounds() {
      return soundSettings;
   }

   public static NotificationManager notifications() {
      return notifications;
   }

   public static ServerConfigs serverConfigs() {
      return serverConfigs;
   }

   public void onInitializeClient() {
      Seed.check();
      LOGGER.info("{} {} initializing", "EpsteinClient", "1.6.2");
      UiSoundEvents.bootstrap();
      themes = new ThemeManager();
      soundSettings = new SoundSettings();
      modules = new ModuleManager();
      spotify = new SpotifyService();
      discord = new DiscordPresenceService();
      serverConfigs = new ServerConfigs();
      notifications = new NotificationManager(themes, modules.hud);
      hud = new HudManager(modules, themes, spotify, notifications);
      config = new ConfigManager(modules, themes);
      ConfigManager configManager = config;
      HudManager hudManager = hud;
      Supplier supplier = hudManager::toJson;
      HudManager hudManager2 = hud;
      configManager.addSection("hud", supplier, hudManager2::fromJson);
      configManager = config;
      ClickGuiState clickGuiState = ClickGuiScreen.state();
      Supplier supplier2 = clickGuiState::toJson;
      ClickGuiState clickGuiState2 = ClickGuiScreen.state();
      configManager.addSection("panels", supplier2, clickGuiState2::fromJson);
      configManager = config;
      SoundSettings soundSettings2 = soundSettings;
      Supplier supplier3 = soundSettings2::toJson;
      SoundSettings soundSettings3 = soundSettings;
      configManager.addSection("sounds", supplier3, soundSettings3::fromJson);
      configManager = config;
      StatsTracker statsTracker = modules.stats.tracker();
      Supplier supplier4 = statsTracker::toJson;
      StatsTracker statsTracker2 = modules.stats.tracker();
      configManager.addSection("stats", supplier4, statsTracker2::fromJson);
      configManager = config;
      ServerConfigs serverConfigs2 = serverConfigs;
      Supplier supplier5 = serverConfigs2::toJson;
      ServerConfigs serverConfigs3 = serverConfigs;
      configManager.addSection("serverConfigs", supplier5, serverConfigs3::fromJson);
      config.load();
      configStore = new ConfigStore(config);
      configStore.loadAll();
      UiSounds.init(soundSettings);
      modules.setOpenGuiAction(() -> MinecraftClient.getInstance().setScreen(new ClickGuiScreen()));
      modules.setToggleListener(
         (arg, freecamModule) -> {
            if (!ConfigStore.applying
               && arg.getCategory() != Category.CLIENT
               && MinecraftClient.getInstance().world != null
               && modules.hud.notifications.get()) {
               notifications.push(arg.getName(), freecamModule);
               UiSounds.notification(freecamModule);
            }
         }
      );
      OverlayRenderer.init(hud, notifications);

      // Everything drawn into the world — the ESPs, the cosmetics, the jump
      // circles — went through a mixin that had been stubbed out to a comment,
      // so none of it had ever rendered. This is the hook that calls them.
      WorldRenderHook.init();
      ScreenEvents.AFTER_INIT.register((AfterInit)(arg, freecamModule, arg3, arg4) -> {
         if (freecamModule instanceof TitleScreen && !startupSoundPlayed) {
            startupSoundPlayed = true;
            UiSounds.playStartup();
         }

         // Screens that draw themselves through NanoVG get the overlay drawn
         // after vanilla finishes with them. This also covers the ClickGUI
         // opened from a menu, where there is no world and therefore no HUD
         // render to piggyback on.
         if (freecamModule instanceof NvgDrawable) {
            ScreenEvents.afterRender(freecamModule).register((screen, ctx, mx, my, delta) -> OverlayRenderer.render());
         }
      });

      // In-world overlay. Fabric's HUD event replaces the old mixin, which
      // targeted a class that does not exist at this Minecraft version and so
      // never bound — leaving the entire HUD and ClickGUI undrawn. Screens that
      // implement NvgDrawable are skipped here because the hook registered
      // above already draws them, and doing both would render twice.
      HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
         if (MinecraftClient.getInstance().currentScreen instanceof NvgDrawable) return;
         OverlayRenderer.render();
      });
      spotify.start();
      ClientPlayConnectionEvents.JOIN.register((Join)(arg, freecamModule, arg3) -> {
         clearSusState();
         if (modules.serverConfigs != null) {
            modules.serverConfigs.onJoinedServer();
         }
      });
      ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(arg, freecamModule) -> clearSusState());
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)arg -> modules.onTick());
      ClientTickEvents.START_CLIENT_TICK.register((StartTick)arg -> {
         FreecamModule freecamModule = modules.freecam;
         if (freecamModule != null && freecamModule.isActive()) {
            FreecamModule.reapplyBodyInput(arg);
         }

         modules.onCombatTick();
      });
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)arg -> {
         config.save();
         spotify.stop();
         discord.stop();
      });
   }

   private static void clearSusState() {
      ServerLightCache.get().clear();
      modules.susChunkFinder.scanner.clear();
      SusChunkRenderer.reset();
      if (modules.chunkFinder != null) {
         modules.chunkFinder.clear();
      }

      if (modules.blockEntityEsp != null) {
         modules.blockEntityEsp.clear();
      }

      if (modules.jumpCircles != null) {
         modules.jumpCircles.clear();
      }

      if (modules.hitParticles != null) {
         modules.hitParticles.clear();
      }

      if (modules.customAccessories != null) {
         modules.customAccessories.clear();
      }

      MotionBlurRenderer.reset();
   }
}
