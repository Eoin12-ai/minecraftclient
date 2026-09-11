package dev.kryptic.gui.picker;

import dev.kryptic.settings.BlockListSetting;
import dev.kryptic.settings.ColorSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntSupplier;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class BlockGridModel implements PickerGrid {
   private static List<Block> allBlocks;
   private final BlockListSetting setting;
   private final IntSupplier defaultColor;
   private final String title;
   private List<PickerGrid.Cell> cells;

   public BlockGridModel(BlockListSetting blockListSetting, IntSupplier intSupplier, String text) {
      this.setting = blockListSetting;
      this.defaultColor = intSupplier;
      this.title = text;
   }

   private static List<Block> allBlocks() {
      if (allBlocks == null) {
         ArrayList list = new ArrayList();

         for (Block block2 : Registries.BLOCK) {
            if (block2 != Blocks.AIR && block2 != Blocks.CAVE_AIR && block2 != Blocks.VOID_AIR) {
               list.add(block2);
            }
         }

         allBlocks = list;
      }

      return allBlocks;
   }

   @Override
   public String title() {
      return this.title;
   }

   @Override
   public long activeCount() {
      return this.setting.enabledCount();
   }

   @Override
   public List<PickerGrid.Cell> cells() {
      if (this.cells == null) {
         ArrayList list = new ArrayList(allBlocks().size());

         for (Block block2 : allBlocks()) {
            list.add(new BlockGridModel.BlockCell(block2));
         }

         this.cells = list;
      }

      return this.cells;
   }

   private final class BlockCell implements PickerGrid.Cell {
      private final Block block;
      private final String search;
      private ItemStack icon;

      BlockCell(Block nullx) {
         this.block = nullx;
         Identifier id = Registries.BLOCK.getId(nullx);
         String path = id != null ? id.getPath() : "";
         String text = id != null ? id.getNamespace() : "";
         this.search = (path + " " + text + " " + BlockListSetting.displayName(nullx)).toLowerCase(Locale.ROOT);
      }

      @Override
      public ItemStack icon() {
         if (this.icon == null) {
            Item item = this.block.asItem();
            this.icon = new ItemStack(item == Items.AIR ? Items.BARRIER : item);
         }

         return this.icon;
      }

      @Override
      public String label() {
         return BlockListSetting.displayName(this.block);
      }

      @Override
      public boolean matches(String text) {
         return this.search.contains(text);
      }

      @Override
      public boolean tracked() {
         return BlockGridModel.this.setting.find(this.block) != null;
      }

      @Override
      public boolean enabled() {
         BlockListSetting.Target target = BlockGridModel.this.setting.find(this.block);
         return target != null && target.enabled.get();
      }

      @Override
      public boolean selected() {
         return BlockGridModel.this.setting.find(this.block) != null;
      }

      @Override
      public int color() {
         BlockListSetting.Target target = BlockGridModel.this.setting.find(this.block);
         return target != null ? target.color.get() : BlockGridModel.this.defaultColor.getAsInt();
      }

      @Override
      public void toggle() {
         BlockListSetting.Target target = BlockGridModel.this.setting.find(this.block);
         if (target == null) {
            BlockGridModel.this.setting.add(this.block, true, BlockGridModel.this.defaultColor.getAsInt());
         } else {
            target.enabled.toggle();
         }
      }

      @Override
      public ColorSetting colorTarget() {
         BlockListSetting.Target target = BlockGridModel.this.setting.find(this.block);
         if (target == null) {
            target = BlockGridModel.this.setting.add(this.block, true, BlockGridModel.this.defaultColor.getAsInt());
         }

         return target != null ? target.color : null;
      }
   }
}
