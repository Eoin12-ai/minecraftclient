package pkg1;

public final class SwyzzyClientAppearanceAndHUDSettingsModuleHelper {
   private int rgb;

   public SwyzzyClientAppearanceAndHUDSettingsModuleHelper(int var1) {
      this.rgb = var1;
   }

   public int intOf(int var1) {
      return (var1 & 0xFF) << 24 | this.rgb & 16777215;
   }

   public int rgb() {
      return this.rgb;
   }
}
