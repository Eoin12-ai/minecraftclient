package dev.kryptic.module.combat;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

public class TriggerbotModule extends Module {
   public final SliderSetting minDelay = this.addSetting(new SliderSetting("Min Delay", "", 9.0, 0.0, 20.0, 1.0));
   public final SliderSetting maxDelay = this.addSetting(new SliderSetting("Max Delay", "", 11.0, 0.0, 20.0, 1.0));
   public final BooleanSetting onlyItem = this.addSetting(new BooleanSetting("Only Item", "", false));
   public final ModeSetting itemFilter = this.addSetting(new ModeSetting("Item Filter", "", "Sword", "Sword", "2T-", "Hand"));
   public final BooleanSetting onlyCrit = this.addSetting(new BooleanSetting("Only Crit", "", false));
   public final BooleanSetting checkShield = this.addSetting(new BooleanSetting("Check Shield", "", false));
   private final Random random = new Random();
   private int tickCounter;
   private int currentDelay = 10;

   public TriggerbotModule() {
      super("Triggerbot", "Auto-attacks entities on crosshair", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.tickCounter = 0;
      this.randomizeDelay();
   }

   @Override
   protected void onDisable() {
      this.tickCounter = 0;
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.interactionManager != null && client.currentScreen == null) {
         if (client.crosshairTarget != null && client.crosshairTarget.getType() == Type.ENTITY) {
            Entity entity = ((EntityHitResult)client.crosshairTarget).getEntity();
            if (entity instanceof LivingEntity && entity != client.player) {
               if (this.onlyItem.get()) {
                  if (this.itemFilter.is("Sword") && !this.isSword(client.player.getMainHandStack().getItem())) {
                     return;
                  }

                  if (this.itemFilter.is("2T-") && !(client.player.getMainHandStack().getItem() instanceof AxeItem)) {
                     return;
                  }

                  if (this.itemFilter.is("Hand") && !client.player.getMainHandStack().isEmpty()) {
                     return;
                  }
               }

               if (!this.onlyCrit.get() || !client.player.isOnGround() && !(client.player.fallDistance <= 0.0)) {
                  if (this.checkShield.get() && entity instanceof PlayerEntity player && player.isBlocking()) {
                     return;
                  }

                  if (!(client.player.getAttackCooldownProgress(0.5F) < 1.0F)) {
                     this.tickCounter++;
                     if (this.tickCounter >= this.currentDelay) {
                        client.interactionManager.attackEntity(client.player, entity);
                        client.player.swingHand(Hand.MAIN_HAND);
                        this.tickCounter = 0;
                        this.randomizeDelay();
                     }
                  }
               }
            }
         } else {
            this.tickCounter = 0;
         }
      }
   }

   private boolean isSword(Item item) {
      return item == Items.WOODEN_SWORD
         || item == Items.STONE_SWORD
         || item == Items.IRON_SWORD
         || item == Items.GOLDEN_SWORD
         || item == Items.DIAMOND_SWORD
         || item == Items.NETHERITE_SWORD;
   }

   private void randomizeDelay() {
      int delay = this.minDelay.getInt();
      int delay2 = this.maxDelay.getInt();
      if (delay2 < delay) {
         delay2 = delay;
      }

      this.currentDelay = delay + (delay2 > delay ? this.random.nextInt(delay2 - delay + 1) : 0);
   }
}
