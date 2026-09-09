package dev.kryptic.module.client;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.BooleanSetting;

public class SpotifyModule extends Module {
   public final ModeSetting source = this.addSetting(
      new ModeSetting("Source", "Auto uses the real Spotify session (Windows); Demo shows sample data", "Auto", "Auto", "Demo")
   );
   public final BooleanSetting controls = this.addSetting(new BooleanSetting("Controls", "Show prev / play / next buttons", true));
   public final BooleanSetting volume = this.addSetting(new BooleanSetting("Volume", "Show a slider for Spotify's app volume (Windows)", true));
   public final BooleanSetting hideWhenIdle = this.addSetting(new BooleanSetting("Hide When Idle", "Hide the card when nothing plays", true));

   public SpotifyModule() {
      super("SpotifyHUD", "Now playing — skip and seek from the HUD", Category.CLIENT);
      this.setEnabled(true);
   }
}
