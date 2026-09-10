package dev.kryptic.module.render;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;
import dev.kryptic.util.Colors;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Marks dropped items on the ground.
 *
 * Name Tags already labels items, so this deliberately does not: it owns the
 * marker in the world and the question of <em>which</em> drops are worth
 * marking. On a busy server the floor is a carpet of cobblestone and rotten
 * flesh, and an ESP that highlights all of it is the same as no ESP.
 *
 * Drops are sorted into three tiers and each tier gets its own colour, so the
 * filter is legible at a glance rather than being an on/off you have to
 * remember the state of. The tiers are by what a drop is worth picking up for,
 * not by Minecraft's own rarity: an Elytra is Rarity.COMMON and a name tag is
 * Rarity.UNCOMMON, which is not the ordering anyone playing wants.
 */
public class ItemEspModule extends Module {

   public final ModeSetting filter = this.addSetting(new ModeSetting("Loot Filter",
      "Which drops get marked. Worth Taking hides the floor litter; Rare Only leaves the short list.",
      "Worth Taking", "Everything", "Worth Taking", "Rare Only"));

   public final SliderSetting range = this.addSetting(
      new SliderSetting("Reach", "How far out drops are marked", 64.0, 8.0, 192.0, 4.0, "m"));

   public final ModeSetting style = this.addSetting(new ModeSetting("Marker Style",
      "An outline alone, or an outline with the box shaded in", "Outline", "Outline", "Shaded"));

   public final SliderSetting opacity = this.addSetting(
      new SliderSetting("Marker Opacity", "How solid the shading is", 30.0, 0.0, 100.0, 2.0, "%"));

   public final BooleanSetting beam = this.addSetting(new BooleanSetting("Rare Beacon",
      "Stand a column of light on a rare drop so it reads from across the map", true));

   public final BooleanSetting tracers = this.addSetting(new BooleanSetting("Guide Lines",
      "Draw a line from the crosshair to each marked drop", false));

   public final ColorSetting commonTint = this.addSetting(
      new ColorSetting("Common Tint", "Colour for ordinary drops", -5000269));      // #B3B3B3

   public final ColorSetting valuableTint = this.addSetting(
      new ColorSetting("Valuable Tint", "Colour for drops worth a detour", -11689985)); // #48B4FF

   public final ColorSetting rareTint = this.addSetting(
      new ColorSetting("Rare Tint", "Colour for the short list", -22733));          // #FFA733

   public ItemEspModule() {
      super("Item ESP", "Marks dropped items, filtered by what they are worth", Category.RENDER);
   }

   /** What a drop is worth stopping for. Ordered, so a comparison means something. */
   public enum Tier {
      COMMON,
      VALUABLE,
      RARE
   }

   /**
    * The short list: things you would cross a map for, or cannot make again.
    */
   private static final Set<String> RARE = Set.of(
         "netherite_ingot", "netherite_scrap", "netherite_block", "ancient_debris",
         "netherite_sword", "netherite_pickaxe", "netherite_axe", "netherite_shovel",
         "netherite_hoe", "netherite_helmet", "netherite_chestplate",
         "netherite_leggings", "netherite_boots", "netherite_upgrade_smithing_template",
         "elytra", "totem_of_undying", "enchanted_golden_apple", "nether_star",
         "beacon", "dragon_egg", "heart_of_the_sea", "shulker_box", "trident",
         "end_crystal", "mace", "wither_skeleton_skull", "recovery_compass",
         "echo_shard", "creaking_heart"
   );

   /**
    * Worth a detour: ores, gear, brewing and building blocks people actually
    * hoard. Not exhaustive on purpose — anything unlisted falls to COMMON,
    * which is visible under "Everything" and hidden under the tighter filters.
    */
   private static final Set<String> VALUABLE = Set.of(
         "diamond", "diamond_block", "diamond_ore", "deepslate_diamond_ore",
         "diamond_sword", "diamond_pickaxe", "diamond_axe", "diamond_shovel",
         "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots",
         "emerald", "emerald_block", "gold_ingot", "gold_block", "golden_apple",
         "iron_ingot", "iron_block", "obsidian", "crying_obsidian", "respawn_anchor",
         "ender_pearl", "ender_eye", "blaze_rod", "blaze_powder", "ghast_tear",
         "experience_bottle", "enchanted_book", "book", "lapis_lazuli",
         "amethyst_shard", "copper_ingot", "redstone", "quartz",
         "shulker_shell", "chorus_fruit", "phantom_membrane", "nautilus_shell",
         "name_tag", "saddle", "lead", "music_disc_pigstep", "disc_fragment_5",
         "golden_carrot", "glowstone", "sea_lantern", "prismarine_crystals",
         "bow", "crossbow", "shield", "anvil", "enchanting_table", "cake"
   );

   /** Which tier a stack falls in. Never null; unlisted is COMMON. */
   public static Tier tierOf(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return Tier.COMMON;
      }

      Identifier id = Registries.ITEM.getId(stack.getItem());
      if (id == null) {
         return Tier.COMMON;
      }

      String path = id.getPath();
      if (RARE.contains(path)) {
         return Tier.RARE;
      }

      // Shulkers are dyed into sixteen separate items, and every one of them is
      // a portable double chest. Matching the suffix catches all of them without
      // listing each colour.
      if (path.endsWith("shulker_box")) {
         return Tier.RARE;
      }

      if (VALUABLE.contains(path)) {
         return Tier.VALUABLE;
      }

      return Tier.COMMON;
   }

   /** Whether the current filter lets this tier through. */
   public boolean shows(Tier tier) {
      if (this.filter.is("Rare Only")) {
         return tier == Tier.RARE;
      }

      if (this.filter.is("Worth Taking")) {
         return tier != Tier.COMMON;
      }

      return true;
   }

   public int tintFor(Tier tier) {
      return switch (tier) {
         case RARE -> this.rareTint.get();
         case VALUABLE -> this.valuableTint.get();
         default -> this.commonTint.get();
      };
   }

   /** The shading colour, or 0 when the style is outline-only. */
   public int shadeFor(Tier tier) {
      if (this.style.is("Outline")) {
         return 0;
      }

      return Colors.withAlpha(this.tintFor(tier), this.opacity.getFloat() / 100.0F);
   }

   public boolean beamsRare() {
      return this.beam.get();
   }
}
