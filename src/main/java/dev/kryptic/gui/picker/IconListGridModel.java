package dev.kryptic.gui.picker;

import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.IconListSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;

public final class IconListGridModel implements PickerGrid {
   private final IconListSetting setting;
   private List<PickerGrid.Cell> cells;

   public IconListGridModel(IconListSetting iconListSetting) {
      this.setting = iconListSetting;
   }

   @Override
   public String title() {
      return this.setting.getName();
   }

   @Override
   public long activeCount() {
      return this.setting.enabledCount();
   }

   @Override
   public List<PickerGrid.Cell> cells() {
      if (this.cells == null) {
         ArrayList list = new ArrayList(this.setting.entries().size());

         for (IconListSetting.Entry entry2 : this.setting.entries()) {
            list.add(new IconListGridModel.EntryCell(entry2));
         }

         this.cells = list;
      }

      return this.cells;
   }

   private static final class EntryCell implements PickerGrid.Cell {
      private final IconListSetting.Entry entry;
      private final ItemStack icon;

      EntryCell(IconListSetting.Entry entry2) {
         this.entry = entry2;
         this.icon = new ItemStack(entry2.icon());
      }

      @Override
      public ItemStack icon() {
         return this.icon;
      }

      @Override
      public String label() {
         return this.entry.label();
      }

      @Override
      public boolean matches(String str) {
         return this.entry.matches(str);
      }

      @Override
      public boolean tracked() {
         return true;
      }

      @Override
      public boolean enabled() {
         return this.entry.enabled.get();
      }

      @Override
      public boolean selected() {
         return this.entry.enabled.get();
      }

      @Override
      public int color() {
         return this.entry.color.get();
      }

      @Override
      public void toggle() {
         this.entry.enabled.toggle();
      }

      @Override
      public ColorSetting colorTarget() {
         return this.entry.color;
      }
   }
}
