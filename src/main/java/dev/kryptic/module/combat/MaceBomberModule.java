package dev.kryptic.module.combat;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.InventoryHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

public class MaceBomberModule extends Module {
   public final SliderSetting height = this.addSetting(new SliderSetting("Height", "Minimum fall distance before it smashes", 8.0, 3.0, 30.0, 1.0, "m"));
   public final ModeSetting mode = this.addSetting(new ModeSetting("Mode", "Dive style", "Direct", "Direct", "Spiral", "Delayed"));
   public final BooleanSetting autoMace = this.addSetting(new BooleanSetting("Auto Mace", "Swap to a mace in your hotbar before smashing", true));
   public final BooleanSetting switchBack = this.addSetting(new BooleanSetting("Switch Back", "Return to the previous slot after the smash", false));
   private int spiralDir = 1;
   private int spiralTimer;
   private int cooldown;
   private int returnSlot = -1;
   private boolean strafing;

   public MaceBomberModule() {
      super("Mace Bomber", "Times a fully-charged mace smash on the way down", Category.COMBAT);
   }

   @Override
   protected void onDisable() {
      this.releaseStrafe();
      this.cooldown = 0;
      this.returnSlot = -1;
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.interactionManager != null && client.currentScreen == null) {
         if (this.cooldown > 0) {
            this.cooldown--;
         }

         if (client.player.isOnGround()) {
            this.releaseStrafe();
            this.cooldown = 0;
            this.returnSlot = -1;
         } else {
            LivingEntity entity = this.crosshairTarget(client);
            if (entity == null) {
               this.releaseStrafe();
            } else {
               if (this.mode.is("Spiral")) {
                  this.spiral(client);
               } else {
                  this.releaseStrafe();
               }

               if (this.cooldown <= 0
                  && !(client.player.fallDistance < (double)this.height.getFloat())
                  && (!this.mode.is("Delayed") || !(client.player.getVelocity().y > -0.5))) {
                  if (!client.player.getMainHandStack().isOf(Items.MACE)) {
                     if (!this.autoMace.get()) {
                        return;
                     }

                     if (this.returnSlot < 0) {
                        this.returnSlot = client.player.getInventory().getSelectedSlot();
                     }

                     if (!InventoryHelper.swapToItem(Items.MACE)) {
                        return;
                     }
                  }

                  if (!(client.player.getAttackCooldownProgress(0.5F) < 1.0F)) {
                     client.interactionManager.attackEntity(client.player, entity);
                     client.player.swingHand(Hand.MAIN_HAND);
                     this.cooldown = 6;
                     if (this.switchBack.get() && this.returnSlot >= 0) {
                        InventoryHelper.swap(this.returnSlot);
                     }

                     this.returnSlot = -1;
                  }
               }
            }
         }
      } else {
         this.releaseStrafe();
      }
   }

   private LivingEntity crosshairTarget(MinecraftClient client) {
      if (client.crosshairTarget != null && client.crosshairTarget.getType() == Type.ENTITY) {
         if (!(((EntityHitResult)client.crosshairTarget).getEntity() instanceof LivingEntity entity)) {
            return null;
         } else {
            return entity != client.player && entity.isAlive() ? entity : null;
         }
      } else {
         return null;
      }
   }

   private void spiral(MinecraftClient client) {
      if (--this.spiralTimer <= 0) {
         this.spiralDir = -this.spiralDir;
         this.spiralTimer = 6 + client.player.age % 7;
      }

      client.options.leftKey.setPressed(this.spiralDir < 0);
      client.options.rightKey.setPressed(this.spiralDir > 0);
      this.strafing = true;
   }

   private void releaseStrafe() {
      if (this.strafing) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.options != null) {
            client.options.leftKey.setPressed(false);
            client.options.rightKey.setPressed(false);
         }

         this.strafing = false;
      }
   }
}
