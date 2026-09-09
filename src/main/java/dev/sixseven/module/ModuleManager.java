package dev.sixseven.module;

import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.ModeSetting;
import dev.sixseven.settings.Setting;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import dev.sixseven.module.combat.AimAssistModule;
import dev.sixseven.module.combat.AnchorMacroModule;
import dev.sixseven.module.misc.ArmorTrimHiderModule;
import dev.sixseven.module.misc.AutoClickerModule;
import dev.sixseven.module.combat.AutoCrystalModule;
import dev.sixseven.module.combat.AutoInventoryTotemModule;
import dev.sixseven.module.combat.AutoTotemModule;
import dev.sixseven.module.misc.AutoTpaModule;
import dev.sixseven.module.misc.AutoWalkModule;
import dev.sixseven.module.render.BlockEntityEspModule;
import dev.sixseven.module.render.BlockEspModule;
import dev.sixseven.module.visuals.BlockOutlineModule;
import dev.sixseven.module.client.ChatMacroModule;
import dev.sixseven.module.render.BreadcrumbsModule;
import dev.sixseven.module.render.ChunkBordersModule;
import dev.sixseven.module.render.ChunkFinderModule;
import dev.sixseven.module.render.TracersModule;
import dev.sixseven.module.client.ClickGuiModule;
import dev.sixseven.module.misc.CoordSnapperModule;
import dev.sixseven.module.visuals.CustomAccessoriesModule;
import dev.sixseven.module.misc.CustomCrosshairModule;
import dev.sixseven.module.misc.CustomFovModule;
import dev.sixseven.module.misc.CustomGlintModule;
import dev.sixseven.module.render.DebugHoleEspModule;
import dev.sixseven.module.combat.DoubleAnchorModule;
import dev.sixseven.module.combat.ElytraSwapModule;
import dev.sixseven.module.misc.FakePayModule;
import dev.sixseven.module.misc.FakeRolesModule;
import dev.sixseven.module.misc.FakeStatsModule;
import dev.sixseven.module.misc.FastUseModule;
import dev.sixseven.module.misc.FreeLookModule;
import dev.sixseven.module.misc.FreecamModule;
import dev.sixseven.module.render.FullbrightModule;
import dev.sixseven.module.misc.GambleRiggerModule;
import dev.sixseven.module.combat.HitBoxModule;
import dev.sixseven.module.visuals.HitParticlesModule;
import dev.sixseven.module.combat.HoverTotemModule;
import dev.sixseven.module.client.HudModule;
import dev.sixseven.module.client.JumpCirclesModule;
import dev.sixseven.module.combat.MaceBomberModule;
import dev.sixseven.module.combat.MaceSwapModule;
import dev.sixseven.module.render.MobEspModule;
import dev.sixseven.module.visuals.MotionBlurModule;
import dev.sixseven.module.misc.NameProtectModule;
import dev.sixseven.module.misc.NameTagsModule;
import dev.sixseven.module.Placeholder;
import dev.sixseven.module.render.PlayerEspModule;
import dev.sixseven.module.render.RegionMapModule;
import dev.sixseven.module.combat.ShieldBreakerModule;
import dev.sixseven.module.misc.SkinProtectModule;
import dev.sixseven.module.render.SpawnerNametagsModule;
import dev.sixseven.module.misc.SpawnerProtectModule;
import dev.sixseven.module.client.DiscordPresenceModule;
import dev.sixseven.module.client.SpotifyModule;
import dev.sixseven.module.client.ServerConfigsModule;
import dev.sixseven.module.client.StatsModule;
import dev.sixseven.module.misc.StaffListModule;
import dev.sixseven.module.render.StorageEspModule;
import dev.sixseven.module.render.SusChunkFinderModule;
import dev.sixseven.module.client.SwingSpeedModule;
import dev.sixseven.module.combat.TriggerbotModule;
import dev.sixseven.module.misc.WeatherNotifierModule;
import dev.sixseven.module.misc.ZoomModule;
public class ModuleManager {
   /** Anti-strip tag; the constructor throws if this ever comes back empty. */
   private static String wmTag() {
      return "Epstein Client";
   }

   private final List<Module> modules = new ArrayList<>();
   private final Map<Category, List<Module>> byCategory = new LinkedHashMap<>();
   public final ClickGuiModule clickGui;
   public final HudModule hud;
   public final SpotifyModule spotify;
   public final DiscordPresenceModule discordRpc;
   public final StatsModule stats;
   public final ServerConfigsModule serverConfigs;
   public final BlockOutlineModule blockOutline;
   public final SusChunkFinderModule susChunkFinder;
   public FullbrightModule fullbright;
   public AutoWalkModule autoWalk;
   public WeatherNotifierModule weatherNotifier;
   public SwingSpeedModule swingSpeed;
   public StorageEspModule storageEsp;
   public BlockEspModule blockEsp;
   public AutoTotemModule autoTotem;
   public MaceSwapModule maceSwap;
   public AnchorMacroModule anchorMacro;
   public AutoCrystalModule autoCrystal;
   public HitBoxModule hitBox;
   public ElytraSwapModule elytraSwap;
   public HoverTotemModule hoverTotem;
   public ShieldBreakerModule shieldBreaker;
   public TriggerbotModule triggerbot;
   public AimAssistModule aimAssist;
   public DoubleAnchorModule doubleAnchor;
   public MaceBomberModule maceBomber;
   public NameProtectModule nameProtect;
   public SkinProtectModule skinProtect;
   public NameTagsModule nameTags;
   public FastUseModule fastUse;
   public AutoInventoryTotemModule autoInventoryTotem;
   public PlayerEspModule playerEsp;
   public MobEspModule mobEsp;
   public BlockEntityEspModule blockEntityEsp;
   public SpawnerNametagsModule spawnerNametags;
   public DebugHoleEspModule debugHoleEsp;
   public FreecamModule freecam;
   public AutoTpaModule autoTpa;
   public JumpCirclesModule jumpCircles;
   public CustomCrosshairModule customCrosshair;
   public ZoomModule zoom;
   public CustomFovModule customFov;
   public HitParticlesModule hitParticles;
   public MotionBlurModule motionBlur;
   public CustomGlintModule customGlint;
   public CustomAccessoriesModule customAccessories;
   public ChunkBordersModule chunkBorders;
   public TracersModule tracers;
   public BreadcrumbsModule breadcrumbs;
   public ChunkFinderModule chunkFinder;
   public FreeLookModule freeLook;
   public AutoClickerModule autoClicker;
   public CoordSnapperModule coordSnapper;
   public RegionMapModule regionMap;
   public ChatMacroModule chatMacro;
   public FakePayModule fakePay;
   public FakeStatsModule fakeStats;
   public FakeRolesModule fakeRoles;
   public StaffListModule staffList;
   public ArmorTrimHiderModule armorTrimHider;
   public SpawnerProtectModule spawnerProtect;
   public GambleRiggerModule gambleRigger;
   private Runnable openGuiAction = () -> {
   };
   private BiConsumer<Module, Boolean> toggleListener = (arg, arg2) -> {
   };

   public ModuleManager() {
      if (wmTag().isEmpty()) {
         throw new Error("integrity");
      }
      for (Category category : Category.values()) {
         this.byCategory.put(category, new ArrayList<>());
      }

      this.susChunkFinder = new SusChunkFinderModule();
      this.registerPlaceholders();
      this.register(this.blockOutline = new BlockOutlineModule());
      this.register(this.hitParticles = new HitParticlesModule());
      this.register(this.customAccessories = new CustomAccessoriesModule());
      this.register(this.chunkBorders = new ChunkBordersModule());
      this.register(this.tracers = new TracersModule());
      this.register(this.breadcrumbs = new BreadcrumbsModule());
      this.register(this.motionBlur = new MotionBlurModule());
      this.register(this.hud = new HudModule());
      this.register(this.spotify = new SpotifyModule());
      this.register(this.discordRpc = new DiscordPresenceModule());
      this.register(this.stats = new StatsModule());
      this.register(this.serverConfigs = new ServerConfigsModule());
      this.register(this.chatMacro = new ChatMacroModule());
      this.ph("ConfigShare", "Import/export configs via codes", Category.CLIENT, new BooleanSetting("Include HUD Layout", "Share HUD positions too", true));
      this.register(this.swingSpeed = new SwingSpeedModule());
      this.register(this.jumpCircles = new JumpCirclesModule());
      this.register(this.clickGui = new ClickGuiModule());
   }

   private void registerPlaceholders() {
      this.register(this.autoTotem = new AutoTotemModule());
      this.register(this.autoCrystal = new AutoCrystalModule());
      this.register(this.anchorMacro = new AnchorMacroModule());
      this.register(this.doubleAnchor = new DoubleAnchorModule());
      this.register(this.autoInventoryTotem = new AutoInventoryTotemModule());
      this.register(this.aimAssist = new AimAssistModule());
      this.register(this.maceSwap = new MaceSwapModule());
      this.register(this.hitBox = new HitBoxModule());
      this.register(this.elytraSwap = new ElytraSwapModule());
      this.register(this.hoverTotem = new HoverTotemModule());
      this.register(this.shieldBreaker = new ShieldBreakerModule());
      this.register(this.triggerbot = new TriggerbotModule());
      this.register(this.maceBomber = new MaceBomberModule());
      this.register(this.skinProtect = new SkinProtectModule());
      this.register(this.nameProtect = new NameProtectModule());
      this.register(this.freecam = new FreecamModule());
      this.register(this.autoTpa = new AutoTpaModule());
      this.register(this.autoClicker = new AutoClickerModule());
      this.register(this.fastUse = new FastUseModule());
      this.register(this.nameTags = new NameTagsModule());
      this.register(this.fakePay = new FakePayModule());
      this.register(this.weatherNotifier = new WeatherNotifierModule());
      this.register(this.fakeStats = new FakeStatsModule());
      this.register(this.fakeRoles = new FakeRolesModule());
      this.register(this.armorTrimHider = new ArmorTrimHiderModule());
      this.register(this.customCrosshair = new CustomCrosshairModule());
      this.ph(
         "Media/StaffNames/Icons",
         "Marks media & staff players with icons",
         Category.MISC,
         new BooleanSetting("Media", "Show media icons", true),
         new BooleanSetting("Staff", "Show staff icons", true),
         new ModeSetting("Position", "Icon position", "Prefix", "Prefix", "Suffix")
      );
      this.register(this.staffList = new StaffListModule());
      this.register(this.customGlint = new CustomGlintModule());
      this.register(this.customFov = new CustomFovModule());
      this.register(this.coordSnapper = new CoordSnapperModule());
      this.register(this.autoWalk = new AutoWalkModule());
      this.register(this.zoom = new ZoomModule());
      this.register(this.freeLook = new FreeLookModule());
      this.register(this.spawnerProtect = new SpawnerProtectModule());
      this.register(this.gambleRigger = new GambleRiggerModule());
      this.register(this.blockEsp = new BlockEspModule());
      this.register(this.storageEsp = new StorageEspModule());
      this.register(this.blockEntityEsp = new BlockEntityEspModule());
      this.register(this.debugHoleEsp = new DebugHoleEspModule());
      this.register(this.fullbright = new FullbrightModule());
      this.register(this.playerEsp = new PlayerEspModule());
      this.register(this.mobEsp = new MobEspModule());
      this.register(this.spawnerNametags = new SpawnerNametagsModule());
      this.register(this.susChunkFinder);
      this.register(this.chunkFinder = new ChunkFinderModule());
      this.register(this.regionMap = new RegionMapModule());
   }

   private void ph(String str, String str3, Category category, Setting<?>... temp) {
      this.register(new Placeholder(str, str3, category, temp));
   }

   public void register(Module module) {
      this.modules.add(module);
      this.byCategory.get(module.getCategory()).add(module);
      module.setToggleCallback(this::notifyToggle);
   }

   public List<Module> all() {
      return this.modules;
   }

   public List<Module> inCategory(Category category) {
      return this.byCategory.get(category);
   }

   public void setOpenGuiAction(Runnable runnable) {
      this.openGuiAction = runnable;
   }

   public void setToggleListener(BiConsumer<Module, Boolean> biConsumer) {
      this.toggleListener = biConsumer;
   }

   public void notifyToggle(Module module, boolean value) {
      this.toggleListener.accept(module, Boolean.valueOf(value));
   }

   public boolean onKeyPressed(int n) {
      if (this.clickGui.getKeybind().matches(n)) {
         this.openGuiAction.run();
         return true;
      } else {
         boolean matches2 = false;

         for (Module module : this.modules) {
            if (module != this.clickGui && module.getKeybind().matches(n)) {
               module.toggle();
               matches2 = true;
            }
         }

         for (Module module2 : this.modules) {
            if (module2.isEnabled() && module2.onKeyPress(n)) {
               matches2 = true;
            }
         }

         return matches2;
      }
   }

   public void onTick() {
      for (Module module : this.modules) {
         if (module.isEnabled() && module.getCategory() != Category.COMBAT) {
            module.onTick();
         }
      }
   }

   public void onCombatTick() {
      for (Module module : this.modules) {
         if (module.isEnabled() && module.getCategory() == Category.COMBAT) {
            module.onTick();
         }
      }
   }
}
