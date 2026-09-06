package dev.sixseven.module.client;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.ColorSetting;
import dev.sixseven.settings.SliderSetting;

public class HudModule extends Module {
   public final BooleanSetting watermark = this.addSetting(new BooleanSetting("Watermark", "The 67 badge", true));
   public final BooleanSetting arrayList = this.addSetting(new BooleanSetting("ArrayList", "Enabled modules list", true));
   public final BooleanSetting fps = this.addSetting(new BooleanSetting("FPS", "Framerate readout", true));
   public final BooleanSetting ping = this.addSetting(new BooleanSetting("Ping", "Latency readout", false));
   public final BooleanSetting coordinates = this.addSetting(new BooleanSetting("Coordinates", "Block position readout", true));
   public final BooleanSetting direction = this.addSetting(new BooleanSetting("Direction", "Facing readout", true));
   public final BooleanSetting tps = this.addSetting(new BooleanSetting("TPS", "Server tick rate estimate", false));
   public final BooleanSetting cps = this.addSetting(new BooleanSetting("CPS", "Clicks per second", false));
   public final BooleanSetting armor = this.addSetting(new BooleanSetting("Armor", "Equipped armor + durability", false));
   public final BooleanSetting potions = this.addSetting(new BooleanSetting("Potions", "Active effects with timers", false));
   public final BooleanSetting keystrokes = this.addSetting(new BooleanSetting("Keystrokes", "WASD + mouse + space display", false));
   public final BooleanSetting radar = this.addSetting(new BooleanSetting("Radar", "Circular player radar", true));
   public final BooleanSetting themeSync = this.addSetting(new BooleanSetting("List Theme Sync", "ArrayList follows the theme color", true));
   public final ColorSetting listColor = this.addSetting(new ColorSetting("List Color", "ArrayList color when Theme Sync is off", -49508));
   public final SliderSetting radarRange = this.addSetting(new SliderSetting("Radar Range", "Scan radius in blocks", 48.0, 16.0, 128.0, 4.0, "m"));
   public final BooleanSetting radarHeads = this.addSetting(new BooleanSetting("Radar Heads", "Skin faces instead of dots", true));
   public final BooleanSetting notifications = this.addSetting(
      new BooleanSetting("Notifications", "Themed toasts when modules toggle & the weather changes", true)
   );
   public final SliderSetting notifyDuration = this.addSetting(
      new SliderSetting("Notify Duration", "How long a toast lingers before fading out", 2.5, 1.0, 6.0, 0.5, "s")
   );

   public HudModule() {
      super("HUD", "All HUD elements — move & resize via chat (T)", Category.CLIENT);
      this.setEnabled(true);
      SliderSetting sliderSetting = this.notifyDuration;
      BooleanSetting booleanSetting = this.notifications;
      sliderSetting.visibleWhen(booleanSetting::get);
   }
}
