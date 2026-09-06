package pkg1;

import com.swyzzyaddon.SwyzzyAddon;

public final class SwyzzyClientAppearanceAndHUDSettingsModule extends Module {
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("HUD");
   private final ActivityChunkFinderModuleEntry val3_2 = this.val2.valOf("Config");
   private final ActivityChunkFinderModuleEntry val4 = this.val2.valOf("License");
   private final Setting<ActivityChunkFinderModuleHelper4> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("accent-color")
            .valOf2("Accent color used by the complete Swyzzy Client interface. Click to open the color picker.")
            .valOf3(getVal())
            .valOf4(SwyzzyClientAppearanceAndHUDSettingsModule::run4)
            .getVal()
      );
   private final Setting<Tab> val6 = this.val_2
      .addSetting(
         new ModuleHelper2()
            .valOf("menu-key")
            .valOf2("Key used to open the Swyzzy Client menu. Click the row, then press any key.")
            .valOf3(new Tab(SwyzzyAddon.val7.getInt()))
            .valOf4(SwyzzyClientAppearanceAndHUDSettingsModule::run3)
            .getVal()
      );
   private final Setting<Boolean> val7 = this.val2_2
      .addSetting(valOf("active-modules", "Shows enabled modules on the edge of the screen.", FPS.ACTIVE_MODULES));
   private final Setting<Boolean> val8 = this.val2_2.addSetting(valOf("watermark", "Shows the Swyzzy+ watermark.", FPS.WATERMARK));
   private final Setting<Boolean> val9 = this.val2_2.addSetting(valOf("coordinates", "Shows the current coordinates.", FPS.COORDINATES));
   private final Setting<Boolean> val10 = this.val2_2.addSetting(valOf("fps", "Shows the current frames per second.", FPS.FPS));
   private final Setting<Boolean> val11 = this.val2_2.addSetting(valOf("clock", "Shows the current time.", FPS.CLOCK));
   private final Setting<String> val12 = this.val3_2
      .addSetting(new AdminDetectorModuleHelper2().valOf("config-manager").valOf2("Save, load and share your module configs.").valOf3("Open").getVal());
   private final Setting<String> val13 = this.val4
      .addSetting(
         new AdminDetectorModuleHelper2()
            .valOf("license-key")
            .valOf2("Enter your license key to unlock the Swyzzy modules and client.")
            .valOf3("")
            .valOf4(this::run)
            .getVal()
      );

   public SwyzzyClientAppearanceAndHUDSettingsModule() {
      super(SwyzzyAddon.val6, "swyzzy-plus", "Swyzzy Client appearance, HUD and license settings.");
   }

   private void run(String var1) {
      if (var1 != null && !var1.trim().isEmpty()) {
         boolean var2 = LicenseKeyModuleUtil.check(var1);
         if (var2) {
            this.run4("§aLicense activated. Modules and client are unlocked.", new Object[0]);
         } else {
            this.run4("§c" + LicenseKeyModuleUtil.getString(), new Object[0]);
         }
      } else {
         LicenseKeyModuleUtil.run();
         this.run4("§cLicense removed. Modules and client are locked.", new Object[0]);
      }
   }

   @Override
   public String getString2() {
      return LicenseKeyModuleUtil.isEnabled() ? "§aUnlocked" : "§cLocked";
   }

   private static ActivityChunkFinderModuleHelper4 getVal() {
      int var0 = SwyzzyAddon.val7.getVal().rgb();
      return new ActivityChunkFinderModuleHelper4(var0 >> 16 & 0xFF, var0 >> 8 & 0xFF, var0 & 0xFF, 255);
   }

   private static Setting<Boolean> valOf(String var0, String var1, FPS var2) {
      return new ActivityChunkFinderModuleHelper8()
         .valOf(var0)
         .valOf2(var1)
         .valOf3(SwyzzyAddon.val7.check(var2))
         .valOf4(var913 -> SwyzzyClientAppearanceAndHUDSettingsModule.run2(var2, var913))
         .getVal();
   }

   private static void run2(FPS var0, Boolean var1) {
      SwyzzyAddon.val7.run5(var0, var1);
   }

   private static void run3(Tab var0) {
      SwyzzyAddon.val7.run2(var0.getInt2());
      if (SwyzzyAddon.swyzzyAddon != null) {
         SwyzzyAddon.swyzzyAddon.run(var0.getInt2());
      }
   }

   private static void run4(ActivityChunkFinderModuleHelper4 var0) {
      SwyzzyAddon.val7.run(var0.getInt() & 16777215);
   }
}
