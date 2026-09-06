package dev.sixseven.module.misc;

import dev.sixseven.mixin.MinecraftAccessor;
import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.ModeSetting;
import dev.sixseven.settings.SliderSetting;
import net.minecraft.client.MinecraftClient;

public class AutoClickerModule extends Module {
   public final BooleanSetting inScreens = this.addSetting(new BooleanSetting("While In Screens", "Whether to click while a screen is open.", true));
   public final ModeSetting leftMode = this.addSetting(
      new ModeSetting("Left Click Mode", "The method of clicking for left clicks.", "Press", "Disabled", "Hold", "Press")
   );
   public final SliderSetting leftDelay = this.addSetting(
      new SliderSetting("Left Click Delay", "Delay between left clicks in ticks.", 2.0, 0.0, 60.0, 1.0, " ticks")
   );
   public final ModeSetting rightMode = this.addSetting(
      new ModeSetting("Right Click Mode", "The method of clicking for right clicks.", "Press", "Disabled", "Hold", "Press")
   );
   public final SliderSetting rightDelay = this.addSetting(
      new SliderSetting("Right Click Delay", "Delay between right clicks in ticks.", 2.0, 0.0, 60.0, 1.0, " ticks")
   );
   private int leftTimer;
   private int rightTimer;

   public AutoClickerModule() {
      super("AutoClicker", "Automatically clicks.", Category.MISC);
      this.leftDelay.visibleWhen(() -> this.leftMode.is("Press"));
      this.rightDelay.visibleWhen(() -> this.rightMode.is("Press"));
   }

   @Override
   protected void onEnable() {
      this.leftTimer = 0;
      this.rightTimer = 0;
      this.release();
   }

   @Override
   protected void onDisable() {
      this.release();
   }

   private void release() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.options != null) {
         client.options.attackKey.setPressed(false);
         client.options.useKey.setPressed(false);
      }
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && (this.inScreens.get() || client.currentScreen == null)) {
         String text = this.leftMode.get();
         switch (text) {
            case "Hold":
               client.options.attackKey.setPressed(true);
               break;
            case "Press":
               this.leftTimer++;
               if (this.leftTimer > this.leftDelay.getInt()) {
                  this.leftClick(client);
                  this.leftTimer = 0;
               }
         }

         text = this.rightMode.get();
         switch (text) {
            case "Hold":
               client.options.useKey.setPressed(true);
               break;
            case "Press":
               this.rightTimer++;
               if (this.rightTimer > this.rightDelay.getInt()) {
                  this.rightClick(client);
                  this.rightTimer = 0;
               }
         }
      }
   }

   private void leftClick(MinecraftClient client) {
      MinecraftAccessor minecraftAccessor = (MinecraftAccessor)client;
      if (minecraftAccessor.getItemUseCooldown() == 10000) {
         minecraftAccessor.setItemUseCooldown(0);
      }

      client.options.attackKey.setPressed(true);
      ((MinecraftAccessor)client).sixsevenclient$startAttack();
      client.options.attackKey.setPressed(false);
   }

   private void rightClick(MinecraftClient client) {
      ((MinecraftAccessor)client).sixsevenclient$startUseItem();
   }
}
