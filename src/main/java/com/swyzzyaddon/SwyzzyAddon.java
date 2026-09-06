package com.swyzzyaddon;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;
import pkg1.ActivityChunkFinderModule;
import pkg1.ActivityChunkFinderModuleData2;
import pkg1.ActivityChunkFinderModuleHelper3;
import pkg1.AdminDetectorModule;
import pkg1.AdminDetectorModuleData;
import pkg1.AmethystChunkFinderModule;
import pkg1.AmethystChunkFinderModuleHelper;
import pkg1.AncientDebrisFinderModule;
import pkg1.AutoDiggerModule;
import pkg1.AutoExpModule;
import pkg1.AutoFlyModule;
import pkg1.AutoTotemModule;
import pkg1.BaseChunksModule;
import pkg1.BaseEspModule;
import pkg1.BlockEspPlusModule;
import pkg1.ChunkDebugModule;
import pkg1.ChunkFinderV2Module;
import pkg1.ChunkReloaderModule;
import pkg1.EntityDebugModule;
import pkg1.FakeRankModule;
import pkg1.FreecamModule;
import pkg1.FreelookModule;
import pkg1.FutureDebugModule;
import pkg1.Helper;
import pkg1.Helper2;
import pkg1.KeyPearlModule;
import pkg1.LicenseKeyModule;
import pkg1.LicenseKeyModuleUtil;
import pkg1.Module;
import pkg1.NoHitDelayModule;
import pkg1.NovaDebugModule;
import pkg1.PacketChunkFinderModule;
import pkg1.PacketChunkFinderModuleData;
import pkg1.PearlMetaModule;
import pkg1.PearlMetaModuleData;
import pkg1.PedroAmethystModule;
import pkg1.PlayerBypassModule;
import pkg1.PlayerRadarModule;
import pkg1.PrimeBaseEspModule;
import pkg1.PrimeChunkFinderModule;
import pkg1.RedstoneActivityModule;
import pkg1.RtpMapModule;
import pkg1.SWZ1;
import pkg1.SpawnerNotifierModule;
import pkg1.StatsChangerModule;
import pkg1.StorageEspModule;
import pkg1.SusChunkModule;
import pkg1.SwingSpeedModule;
import pkg1.SwyzzyAddonHelper;
import pkg1.SwyzzyAddonHelper2;
import pkg1.SwyzzyAddonHelper3;
import pkg1.SwyzzyClientAppearanceAndHUDSettingsModule;
import pkg1.SwyzzyClientScreen;
import pkg1.ToastsModule;
import pkg1.TuffChunkFinderModule;
import pkg1.UdRelogModule;
import pkg1.WaterChunkFinderModule;
import pkg1.WeatherNotifierModule;
import util.ListUtils;
import util.MISC;
import util.SwyzzyAddonUtils;

public final class SwyzzyAddon implements ClientModInitializer {
   public static final Logger logger = LogUtils.getLogger();
   public static final SwyzzyAddonHelper val = new SwyzzyAddonHelper("Combat");
   public static final SwyzzyAddonHelper val2 = new SwyzzyAddonHelper("Base Finding");
   public static final SwyzzyAddonHelper val3 = new SwyzzyAddonHelper("Storage");
   public static final SwyzzyAddonHelper val4 = new SwyzzyAddonHelper("Render");
   public static final SwyzzyAddonHelper val5 = new SwyzzyAddonHelper("Misc");
   public static final SwyzzyAddonHelper val6 = new SwyzzyAddonHelper("Client");
   public static final List<SwyzzyAddonHelper> list = List.of(val, val2, val3, val4, val5, val6);
   public static final Helper val7 = new Helper();
   public static SWZ1 val8;
   public static SwyzzyAddon swyzzyAddon;
   private final SwyzzyAddonHelper2 val9 = new SwyzzyAddonHelper2();
   private List<SwyzzyAddon.Inner1> list2 = List.of();
   private Helper2 val10;
   private SwyzzyAddonHelper3 val11;
   private KeyBinding class304;
   private ClientWorld class638;

   public void onInitializeClient() {
      swyzzyAddon = this;
      logger.info("Initializing standalone Swyzzy Addon");
      val7.run3();
      LicenseKeyModuleUtil.run3();
      this.class304 = MISC.class304Of(val7.getInt());
      SwyzzyClientAppearanceAndHUDSettingsModule var1 = new SwyzzyClientAppearanceAndHUDSettingsModule();
      var1.valOf(val6);
      this.val9.run(var1);
      ArrayList var2 = new ArrayList();
      this.swyzzyAddonOf("License Key", new LicenseKeyModule(), val6);
      boolean var10000 = false;
      var2.addAll(
         List.of(
            this.swyzzyAddonOf("Key Pearl", new KeyPearlModule(), val),
            this.swyzzyAddonOf("Auto Totem", new AutoTotemModule(), val),
            this.swyzzyAddonOf("No Hit Delay", new NoHitDelayModule(), val),
            this.swyzzyAddonOf("Activity Chunk Finder", new ActivityChunkFinderModule(), val2),
            this.swyzzyAddonOf("Base Chunks", new BaseChunksModule(), val2),
            this.swyzzyAddonOf("Base Esp", new BaseEspModule(), val2),
            this.swyzzyAddonOf("Chunk Debug", new ChunkDebugModule(), val2),
            this.swyzzyAddonOf("Chunk Detector", new WaterChunkFinderModule(), val2),
            this.swyzzyAddonOf("Chunk Finder V2", new ChunkFinderV2Module(), val2),
            this.swyzzyAddonOf("Packet Chunk Finder", new PacketChunkFinderModule(), val2),
            this.swyzzyAddonOf("Player Bypass", new PlayerBypassModule(), val2),
            this.swyzzyAddonOf("Sus Chunk", new SusChunkModule(), val2),
            this.swyzzyAddonOf("Redstone Activity", new RedstoneActivityModule(), val2),
            this.swyzzyAddonOf("Prime Base Esp", new PrimeBaseEspModule(), val2),
            this.swyzzyAddonOf("Prime Chunk Finder", new PrimeChunkFinderModule(), val2),
            this.swyzzyAddonOf("Tuff Chunk Finder", new TuffChunkFinderModule(), val2),
            this.swyzzyAddonOf("Amethyst Chunk Finder", new AmethystChunkFinderModule(), val2),
            this.swyzzyAddonOf("Pedro Amethyst", new PedroAmethystModule(), val2),
            this.swyzzyAddonOf("Future Debug", new FutureDebugModule(), val2),
            this.swyzzyAddonOf("Nova Debug", new NovaDebugModule(), val2),
            this.swyzzyAddonOf("Entity Debug", new EntityDebugModule(), val2),
            this.swyzzyAddonOf("Spawner Notifier", new SpawnerNotifierModule(), val2),
            this.swyzzyAddonOf("Block Esp Plus", new BlockEspPlusModule(), val3),
            this.swyzzyAddonOf("Storage ESP", new StorageEspModule(), val3),
            this.swyzzyAddonOf("Freecam", new FreecamModule(), val4),
            this.swyzzyAddonOf("Freelook", new FreelookModule(), val4),
            this.swyzzyAddonOf("Player Radar", new PlayerRadarModule(), val4),
            this.swyzzyAddonOf("Rtp Map", new RtpMapModule(), val4),
            this.swyzzyAddonOf("Admin Detector", new AdminDetectorModule(), val5),
            this.swyzzyAddonOf("Ancient Debris Finder", new AncientDebrisFinderModule(), val5),
            this.swyzzyAddonOf("Auto Chunk Reloader", new ChunkReloaderModule(), val5),
            this.swyzzyAddonOf("Auto Digger", new AutoDiggerModule(), val5),
            this.swyzzyAddonOf("Auto EXP", new AutoExpModule(), val5),
            this.swyzzyAddonOf("Auto Fly", new AutoFlyModule(), val5),
            this.swyzzyAddonOf("Fake Rank", new FakeRankModule(), val5),
            this.swyzzyAddonOf("Pearl Meta", new PearlMetaModule(), val5),
            this.swyzzyAddonOf("Stats Changer", new StatsChangerModule(), val5),
            this.swyzzyAddonOf("Swing Speed", new SwingSpeedModule(), val5),
            this.swyzzyAddonOf("Toasts", new ToastsModule(), val5),
            this.swyzzyAddonOf("Ud Relog", new UdRelogModule(), val5),
            this.swyzzyAddonOf("Weather Notifier", new WeatherNotifierModule(), val5)
         )
      );
      this.list2 = List.copyOf(var2);
      this.val9.run4();
      val8 = new SWZ1();
      this.val10 = new Helper2(this.list2, val7);
      this.val11 = new SwyzzyAddonHelper3(this.val9::run7);
      SwyzzyAddonUtils.run(this);
      ClientChunkEvents.CHUNK_LOAD.register(this::run9);
      ClientChunkEvents.CHUNK_UNLOAD.register(SwyzzyAddon::run8);
      ClientTickEvents.END_CLIENT_TICK.register(this::run7);
      ClientLifecycleEvents.CLIENT_STOPPING.register(this::run6);
   }

   private SwyzzyAddon.Inner1 swyzzyAddonOf(String var1, Module var2, SwyzzyAddonHelper var3) {
      var2.valOf(var3);
      var2.title = var1;
      this.val9.run(var2);
      return new Inner1(var1, var2);
   }

   public void run(int var1) {
      MISC.run(this.class304, var1);
   }

   public static void run2(Object var0) {
      if (swyzzyAddon != null) {
         swyzzyAddon.val9.run3(new PacketChunkFinderModuleData(var0));
      }
   }

   public static void run3(Object var0) {
      if (swyzzyAddon != null) {
         swyzzyAddon.val9.run3(new PearlMetaModuleData(var0));
      }
   }

   public static void run4(BlockPos var0, BlockState var1) {
      if (swyzzyAddon != null) {
         swyzzyAddon.val9.run3(new ActivityChunkFinderModuleData2(var0, var1));
      }
   }

   public static void run5(DrawContext var0) {
      if (swyzzyAddon != null) {
         AdminDetectorModuleData var1 = new AdminDetectorModuleData(var0);
         swyzzyAddon.val9.run3(var1);
         if (swyzzyAddon.val11 != null) {
            swyzzyAddon.val11.run(var1);
         }
      }
   }

   public SwyzzyAddonHelper2 getVal() {
      return this.val9;
   }

   public Helper2 getVal2() {
      return this.val10;
   }

   private void run6(MinecraftClient var1) {
      this.val9.run7();
      val7.run7();
   }

   private void run7(MinecraftClient var1) {
      if (var1.world == null && this.class638 != null) {
         ListUtils.run3();
         this.class638 = null;
      }

      this.val9.run3(new ActivityChunkFinderModuleHelper3());
      this.val9.run6();
      if (var1.currentScreen == null && var1.getWindow() != null) {
         this.val9.run2(var1.getWindow().getHandle());
      }

      if (this.class304 != null && this.class304.wasPressed() && var1.currentScreen == null) {
         var1.setScreen(new SwyzzyClientScreen());
      }
   }

   private static void run8(ClientWorld var0, WorldChunk var1) {
      ListUtils.run2(var1);
   }

   private void run9(ClientWorld var1, WorldChunk var2) {
      if (var1 != this.class638) {
         ListUtils.run3();
         this.class638 = var1;
      }

      ListUtils.run(var2);
      this.val9.run3(new AmethystChunkFinderModuleHelper(var2));
   }

   public final class Inner1 {
      private String label;
      private Module module;

      public Inner1(String var1, Module var2) {
         this.label = var1;
         this.module = var2;
      }

      public String label() {
         return this.label;
      }

      public Module module() {
         return this.module;
      }
   }
}
