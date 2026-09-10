package dev.kryptic.module.client;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;

public class HudModule extends Module {
   public final BooleanSetting watermark = this.addSetting(new BooleanSetting("Watermark", "The Kryptic badge", true));
   public final BooleanSetting arrayList = this.addSetting(new BooleanSetting("Module List", "Enabled modules list", true));
   public final BooleanSetting fps = this.addSetting(new BooleanSetting("FPS", "Framerate readout", true));
   public final BooleanSetting ping = this.addSetting(new BooleanSetting("Ping", "Latency readout", false));
   public final BooleanSetting coordinates = this.addSetting(new BooleanSetting("Coordinates", "Block position readout", true));
   public final BooleanSetting direction = this.addSetting(new BooleanSetting("Direction", "Facing readout", true));
   public final BooleanSetting tps = this.addSetting(new BooleanSetting("TPS", "Server tick rate estimate", false));
   public final BooleanSetting cps = this.addSetting(new BooleanSetting("CPS", "Clicks per second", false));
   public final BooleanSetting armor = this.addSetting(new BooleanSetting("Armor", "Equipped armor + durability", false));
   public final BooleanSetting potions = this.addSetting(new BooleanSetting("Potions", "Active effects with timers", false));
   public final BooleanSetting keystrokes = this.addSetting(new BooleanSetting("Keystrokes", "WASD + mouse + space display", false));
   public final BooleanSetting hotkeys = this.addSetting(new BooleanSetting("Hotkeys", "Every module you have bound a key to, and its key", true));
   public final BooleanSetting radar = this.addSetting(new BooleanSetting("Radar", "Circular player radar", true));
   public final SliderSetting scale = this.addSetting(new SliderSetting(
         "Scale", "Size of every HUD element at once, on top of each one's own size", 1.0, 0.5, 3.0, 0.05, "x"));
   public final ModeSetting style = this.addSetting(new ModeSetting(
         "Style", "Panel backing \u2014 Card matches the menu, Flat is a plain fill",
         "Card", "Card", "Flat"));
   public final BooleanSetting meters = this.addSetting(new BooleanSetting("Meters", "A card of extra readouts, stacked and self-sizing", true));
   public final BooleanSetting meterSpeed = this.addSetting(new BooleanSetting("Speed", "Horizontal blocks per second", true));
   public final BooleanSetting meterBiome = this.addSetting(new BooleanSetting("Biome", "Biome you are standing in", true));
   public final BooleanSetting meterLight = this.addSetting(new BooleanSetting("Light Level", "Block and sky light where you stand", false));
   public final BooleanSetting meterTime = this.addSetting(new BooleanSetting("World Time", "World time as a clock", false));
   public final BooleanSetting meterSession = this.addSetting(new BooleanSetting("Session Length", "How long this sitting has run", true));
   public final BooleanSetting meterPortal = this.addSetting(new BooleanSetting("Portal Coords", "Your coordinates in the other dimension", true));
   public final BooleanSetting colourCode = this.addSetting(new BooleanSetting("Color Coded Numbers", "Tint the FPS and ping numbers by how good they are", true));
   public final BooleanSetting themeSync = this.addSetting(new BooleanSetting("List Follows Theme", "ArrayList follows the theme color", true));
   public final ColorSetting listColor = this.addSetting(new ColorSetting("List Color", "Module list color when List Follows Theme is off", -1));
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
      this.meterSpeed.visibleWhen(this.meters::get);
      this.meterBiome.visibleWhen(this.meters::get);
      this.meterLight.visibleWhen(this.meters::get);
      this.meterTime.visibleWhen(this.meters::get);
      this.meterSession.visibleWhen(this.meters::get);
      this.meterPortal.visibleWhen(this.meters::get);
   }
}
