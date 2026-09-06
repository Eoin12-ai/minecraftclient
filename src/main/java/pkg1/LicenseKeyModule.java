package pkg1;

import com.swyzzyaddon.SwyzzyAddon;

public final class LicenseKeyModule extends Module {
   private final Setting<String> val_2 = this.val2
      .getVal()
      .addSetting(
         new AdminDetectorModuleHelper2()
            .valOf("license-key")
            .valOf2("Enter your license key here (e.g. G-XXXXXXXXXX-XXXXXXXXXX)")
            .valOf3("")
            .valOf4(this::run)
            .getVal()
      );

   public LicenseKeyModule() {
      super(SwyzzyAddon.val6, "License Key", "Enter your license key to unlock all modules.");
   }

   @Override
   public void run6() {
      this.run12();
      if (!this.isEnabled()) {
         super.run(true);
      }
   }

   private void run(String var1) {
      if (var1 != null && !var1.trim().isEmpty()) {
         boolean var2 = LicenseKeyModuleUtil.check(var1);
         if (var2) {
            this.run4("§aLicense activated successfully! All modules are now unlocked.", new Object[0]);
         } else {
            this.run4("§c" + LicenseKeyModuleUtil.getString(), new Object[0]);
         }
      }
   }

   private void run12() {
      if (class310.player != null) {
         String var1 = LicenseKeyModuleUtil.getString();
         if (LicenseKeyModuleUtil.isEnabled()) {
            this.run4("§aStatus: null", new Object[0]);
         } else {
            this.run4("§cStatus: null", new Object[0]);
            this.run4("§ePlease enter your license key to unlock modules.", new Object[0]);
         }
      }
   }

   @Override
   public String getString2() {
      return LicenseKeyModuleUtil.isEnabled() ? "§aActive" : "§cInactive";
   }
}
