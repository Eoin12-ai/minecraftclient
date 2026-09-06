package dev.sixseven.wm;

import net.fabricmc.api.ClientModInitializer;

public class Boot implements ClientModInitializer {
   @Override
   public void onInitializeClient() {
      Seed.check();
   }
}
