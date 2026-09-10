package dev.kryptic.module.render;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.module.ModuleManager;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Draws a coloured silhouette around entities, through terrain.
 *
 * This rides vanilla's own outline pass — the one that makes a glowing entity
 * visible through a wall — rather than replacing the entity render layer. That
 * matters for more than effort: the outline pass is a pipeline Minecraft
 * already maintains across versions, so this survives an update that would
 * break a hand-rolled render layer, and it composites correctly with shaders
 * instead of fighting them.
 *
 * What it is not: a filled model tint. Vanilla's pass draws the model's
 * silhouette, so this is the outline flavour. That is the half that reads
 * through a wall anyway — a filled tint at 40 blocks is a coloured smudge.
 *
 * A box ESP tells you something is there. This tells you which way it is
 * facing and what it is doing, because it is the actual model.
 */
public class ChamsModule extends Module {

   public final BooleanSetting players = this.addSetting(
      new BooleanSetting("Players", "Outline other players", true));

   public final BooleanSetting hostiles = this.addSetting(
      new BooleanSetting("Hostiles", "Outline monsters", false));

   public final BooleanSetting passives = this.addSetting(
      new BooleanSetting("Passives", "Outline animals and villagers", false));

   public final BooleanSetting drops = this.addSetting(
      new BooleanSetting("Dropped Items", "Outline items lying on the ground", false));

   public final BooleanSetting self = this.addSetting(
      new BooleanSetting("Yourself", "Outline your own body in third person", false));

   public final SliderSetting range = this.addSetting(
      new SliderSetting("Reach", "Only outline within this distance", 96.0, 8.0, 256.0, 8.0, "m"));

   public final ColorSetting playerTint = this.addSetting(
      new ColorSetting("Player Tint", "Outline colour for players", -11689985));   // #48B4FF

   public final ColorSetting hostileTint = this.addSetting(
      new ColorSetting("Hostile Tint", "Outline colour for monsters", -45715));    // #FF4D2D

   public final ColorSetting passiveTint = this.addSetting(
      new ColorSetting("Passive Tint", "Outline colour for animals", -12654960));  // #3EC450

   public final ColorSetting dropTint = this.addSetting(
      new ColorSetting("Drop Tint", "Outline colour for dropped items", -22733));  // #FFA733

   public ChamsModule() {
      super("Chams", "Outlines entities through walls using their real model", Category.RENDER);
   }

   public static ChamsModule get() {
      ModuleManager modules = KrypticClient.modules();
      return modules == null ? null : modules.chams;
   }

   /**
    * Whether this entity should be outlined right now.
    *
    * Called from a mixin on a hot path — once per entity per frame — so it
    * stays to field reads and instanceof, with the distance check last because
    * it is the only part that costs anything.
    */
   public boolean outlines(Entity entity) {
      if (!this.isEnabled() || entity == null) {
         return false;
      }

      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null) {
         return false;
      }

      if (entity == client.player) {
         // Your own outline is only ever visible in third person, and in first
         // person it would tint the hand.
         if (!this.self.get() || client.options.getPerspective().isFirstPerson()) {
            return false;
         }
      } else if (!this.wanted(entity)) {
         return false;
      }

      double reach = this.range.get();
      return client.player.squaredDistanceTo(entity) <= reach * reach;
   }

   private boolean wanted(Entity entity) {
      if (entity instanceof PlayerEntity) {
         return this.players.get();
      }

      if (entity instanceof ItemEntity) {
         return this.drops.get();
      }

      if (entity instanceof MobEntity) {
         return entity instanceof Monster ? this.hostiles.get() : this.passives.get();
      }

      return false;
   }

   /**
    * The outline colour for an entity.
    *
    * Vanilla reads this from the entity's team colour, which is why the hook is
    * on getTeamColorValue rather than anywhere in the renderer. The alpha byte
    * is dropped: the outline pass takes a packed RGB and a colour arriving with
    * alpha 0 comes out black.
    */
   public int tint(Entity entity) {
      int argb;
      if (entity instanceof ItemEntity) {
         argb = this.dropTint.get();
      } else if (entity instanceof PlayerEntity) {
         argb = this.playerTint.get();
      } else if (entity instanceof Monster) {
         argb = this.hostileTint.get();
      } else if (entity instanceof MobEntity) {
         argb = this.passiveTint.get();
      } else {
         argb = this.playerTint.get();
      }

      return argb & 0xFFFFFF;
   }
}
