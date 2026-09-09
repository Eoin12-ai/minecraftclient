package dev.kryptic.module;

import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.Setting;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import dev.kryptic.module.combat.AimAssistModule;
import dev.kryptic.module.combat.AnchorMacroModule;
import dev.kryptic.module.misc.ArmorTrimHiderModule;
import dev.kryptic.module.combat.AutoCrystalModule;
import dev.kryptic.module.combat.AutoInventoryTotemModule;
import dev.kryptic.module.combat.AutoTotemModule;
import dev.kryptic.module.misc.AutoTpaModule;
import dev.kryptic.module.misc.AutoWalkModule;
import dev.kryptic.module.render.BlockEntityEspModule;
import dev.kryptic.module.render.BlockEspModule;
import dev.kryptic.module.visuals.BlockOutlineModule;
import dev.kryptic.module.client.ChatMacroModule;
import dev.kryptic.module.render.BreadcrumbsModule;
import dev.kryptic.module.render.ChunkBordersModule;
import dev.kryptic.module.render.ChunkFinderModule;
import dev.kryptic.module.render.TracersModule;
import dev.kryptic.module.client.ClickGuiModule;
import dev.kryptic.module.misc.CoordSnapperModule;
import dev.kryptic.module.visuals.CustomAccessoriesModule;
import dev.kryptic.module.misc.CustomCrosshairModule;
import dev.kryptic.module.misc.CustomFovModule;
import dev.kryptic.module.misc.CustomGlintModule;
import dev.kryptic.module.misc.KeySoundsModule;
import dev.kryptic.module.render.DebugHoleEspModule;
import dev.kryptic.module.combat.DoubleAnchorModule;
import dev.kryptic.module.combat.ElytraSwapModule;
import dev.kryptic.module.misc.FakePayModule;
import dev.kryptic.module.misc.FakeRolesModule;
import dev.kryptic.module.misc.FakeStatsModule;
import dev.kryptic.module.misc.FastUseModule;
import dev.kryptic.module.misc.FreeLookModule;
import dev.kryptic.module.misc.FreecamModule;
import dev.kryptic.module.render.FullbrightModule;
import dev.kryptic.module.combat.HitBoxModule;
import dev.kryptic.module.visuals.HitParticlesModule;
import dev.kryptic.module.combat.HoverTotemModule;
import dev.kryptic.module.client.HudModule;
import dev.kryptic.module.client.JumpCirclesModule;
import dev.kryptic.module.combat.MaceBomberModule;
import dev.kryptic.module.combat.MaceSwapModule;
import dev.kryptic.module.render.MobEspModule;
import dev.kryptic.module.visuals.MotionBlurModule;
import dev.kryptic.module.misc.NameProtectModule;
import dev.kryptic.module.misc.NameTagsModule;
import dev.kryptic.module.Placeholder;
import dev.kryptic.module.render.PlayerEspModule;
import dev.kryptic.module.render.RegionMapModule;
import dev.kryptic.module.combat.ShieldBreakerModule;
import dev.kryptic.module.misc.SkinProtectModule;
import dev.kryptic.module.render.SpawnerNametagsModule;
import dev.kryptic.module.misc.SpawnerProtectModule;
import dev.kryptic.module.client.DiscordPresenceModule;
import dev.kryptic.module.client.SpotifyModule;
import dev.kryptic.module.client.ServerConfigsModule;
import dev.kryptic.module.client.StatsModule;
import dev.kryptic.module.misc.StaffListModule;
import dev.kryptic.module.render.StorageEspModule;
import dev.kryptic.module.render.SusChunkFinderModule;
import dev.kryptic.module.client.SwingSpeedModule;
import dev.kryptic.module.combat.TriggerbotModule;
import dev.kryptic.module.misc.WeatherNotifierModule;
import dev.kryptic.module.misc.ZoomModule;
public class ModuleManager {
   /** Anti-strip tag; the constructor throws if this ever comes back empty. */
   private static String wmTag() {
      return "Kryptic Client";
   }

   private final List<Module> modules = new ArrayList<>();
   private final Map<Category, List<Module>> byCategory = new LinkedHashMap<>();
   /** byCategory reordered for display; rebuilt when the Sort setting changes. */
   private final Map<Category, List<Module>> displayOrder = new LinkedHashMap<>();
   private String displayOrderFor = "";
   /** Bumped whenever the display order is rebuilt, so panels can re-sync. */
   private int orderStamp;
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
   public KeySoundsModule keySounds;
   public CustomAccessoriesModule customAccessories;
   public ChunkBordersModule chunkBorders;
   public TracersModule tracers;
   public BreadcrumbsModule breadcrumbs;
   public ChunkFinderModule chunkFinder;
   public FreeLookModule freeLook;
   public CoordSnapperModule coordSnapper;
   public RegionMapModule regionMap;
   public ChatMacroModule chatMacro;
   public FakePayModule fakePay;
   public FakeStatsModule fakeStats;
   public FakeRolesModule fakeRoles;
   public StaffListModule staffList;
   public ArmorTrimHiderModule armorTrimHider;
   public SpawnerProtectModule spawnerProtect;
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
      this.register(this.keySounds = new KeySoundsModule());
      this.register(this.customFov = new CustomFovModule());
      this.register(this.coordSnapper = new CoordSnapperModule());
      this.register(this.autoWalk = new AutoWalkModule());
      this.register(this.zoom = new ZoomModule());
      this.register(this.freeLook = new FreeLookModule());
      this.register(this.spawnerProtect = new SpawnerProtectModule());
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
      this.displayOrderFor = "";
   }

   public List<Module> all() {
      return this.modules;
   }

   /**
    * The modules of one category, in the order the menu should list them.
    *
    * Registration order is whatever the constructor happened to do, which is no
    * order at all once there are sixty of them. The list is sorted for display
    * instead, and the registration order is kept intact underneath so "Default"
    * can still hand it back.
    *
    * Rebuilt only when the setting changes: this is called once per column per
    * frame, and re-sorting five lists sixty times a second to get the same
    * answer would be a waste.
    */
   public List<Module> inCategory(Category category) {
      String mode = this.clickGui == null ? "Default" : this.clickGui.sort.get();
      if (!mode.equals(this.displayOrderFor)) {
         this.rebuildDisplayOrder(mode);
         this.displayOrderFor = mode;
      }
      List<Module> ordered = this.displayOrder.get(category);
      return ordered == null ? this.byCategory.get(category) : ordered;
   }

   private void rebuildDisplayOrder(String mode) {
      Comparator<Module> byName = Comparator.comparing(m -> m.getName().toLowerCase(Locale.ROOT));
      Comparator<Module> order = switch (mode) {
         case "A-Z" -> byName;
         case "Z-A" -> byName.reversed();
         // enabled float to the top, each group still alphabetical inside itself
         case "Enabled First" -> Comparator.comparing((Module m) -> !m.isEnabled()).thenComparing(byName);
         default -> null;
      };
      this.displayOrder.clear();
      this.orderStamp++;
      for (Map.Entry<Category, List<Module>> entry : this.byCategory.entrySet()) {
         List<Module> copy = new ArrayList<>(entry.getValue());
         if (order != null) copy.sort(order);
         this.displayOrder.put(entry.getKey(), copy);
      }
   }

   /** Changes whenever inCategory would hand back a different order. */
   public int orderStamp() {
      return this.orderStamp;
   }

   public void setOpenGuiAction(Runnable runnable) {
      this.openGuiAction = runnable;
   }

   public void setToggleListener(BiConsumer<Module, Boolean> biConsumer) {
      this.toggleListener = biConsumer;
   }

   public void notifyToggle(Module module, boolean value) {
      // "Enabled First" orders by exactly this, so its cached answer is now stale
      this.displayOrderFor = "";
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
