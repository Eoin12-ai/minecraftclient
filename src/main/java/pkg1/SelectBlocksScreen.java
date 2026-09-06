package pkg1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import util.UtilsUtil;

public final class SelectBlocksScreen extends Screen {
   private static final int intVal = 300;
   private static final int intVal2 = 18;
   private static final int intVal3 = 12;
   private Screen class437;
   private Setting<List<Block>> val;
   private final List<SelectBlocksScreen.Inner1> list = new ArrayList<>();
   private final List<SelectBlocksScreen.Inner1> list2 = new ArrayList<>();
   private final Set<Block> set = new LinkedHashSet<>();
   private util.Inner1 val2;
   private int intVal4;
   private int intVal5;
   private int intVal6;
   private int intVal7;
   private int intVal8;
   private float floatVal;

   public SelectBlocksScreen(Screen var1, Setting<List<Block>> var2) {
      super(Text.literal("Select Blocks"));
      this.class437 = var1;
      this.val = var2;
      this.set.addAll((Collection<? extends Block>)var2.getObject());
   }

   protected void init() {
      super.init();
      if (this.list.isEmpty()) {
         this.run3();
      }

      this.intVal6 = Math.min(this.height - 40, 300);
      this.intVal5 = (this.height - this.intVal6) / 2;
      this.intVal4 = (this.width - 300) / 2;
      this.val2 = UtilsUtil.valOf2(this.textRenderer, this.intVal4 + 12, this.intVal5 + 30, 276, 18, "Search blocks...");
      this.val2.setMaxLength(64);
      this.val2.setPlaceholder(Text.literal("Search blocks..."));
      this.val2.setChangedListener(this::run5);
      this.addSelectableChild(this.val2);
      this.setInitialFocus(this.val2);
      int var1 = this.intVal5 + this.intVal6 - 26;
      this.intVal7 = this.intVal5 + 54;
      this.intVal8 = var1 - 8 - this.intVal7;
      this.addDrawableChild(UtilsUtil.valOf("Clear all", this.intVal4 + 12, var1, 80, 20, this::run2));
      this.addDrawableChild(UtilsUtil.valOf("Done", this.intVal4 + 300 - 12 - 80, var1, 80, 20, this::close));
      this.run(this.val2.getText());
   }

   private void run3() {
      for (Block var2 : Registries.BLOCK) {
         if (var2 != Blocks.AIR && var2 != Blocks.CAVE_AIR && var2 != Blocks.VOID_AIR) {
            Identifier var3 = Registries.BLOCK.getId(var2);
            String var4 = var2.getName().getString();
            String var5 = var3 == null ? "" : var3.getPath();
            this.list.add(new Inner1(var2, var4, var5, "null null".toLowerCase(Locale.ROOT)));
         }
      }

      this.list.sort(SelectBlocksScreen::intOf);
   }

   private void run(String var1) {
      String var2 = var1 == null ? "" : var1.trim().toLowerCase(Locale.ROOT);
      this.list2.clear();

      for (SelectBlocksScreen.Inner1 var4 : this.list) {
         if (var2.isEmpty() || var4.search.contains(var2)) {
            this.list2.add(var4);
         }
      }

      this.run7();
   }

   private void run2() {
      this.set.clear();
      this.run6();
   }

   private void run6() {
      this.val.run2(new ArrayList<>(this.set));
   }

   public boolean mouseClicked(double _cx, double _cy, int _cb) {
      double var3 = _cx;
      double var5 = _cy;
      if (var3 >= this.intVal4 && var3 <= this.intVal4 + 300 && var5 >= this.intVal7 && var5 < this.intVal7 + this.intVal8) {
         int var7 = (int)((var5 - this.intVal7 + this.floatVal) / 18.0);
         if (var7 >= 0 && var7 < this.list2.size()) {
            Block var8 = this.list2.get(var7).block;
            if (!this.set.remove(var8)) {
               this.set.add(var8);
            }

            this.run6();
            return true;
         } else {
            return true;
         }
      } else {
         return super.mouseClicked(var1, var2);
      }
   }

   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      if (var1 >= this.intVal4 && var1 <= this.intVal4 + 300 && var3 >= this.intVal7 && var3 < this.intVal7 + this.intVal8) {
         this.floatVal -= (float)var7 * 18.0F * 2.0F;
         this.run7();
         return true;
      } else {
         return super.mouseScrolled(var1, var3, var5, var7);
      }
   }

   private void run7() {
      float var1 = Math.max(0, this.list2.size() * 18 - this.intVal8);
      if (this.floatVal > var1) {
         this.floatVal = var1;
      }

      if (this.floatVal < 0.0F) {
         this.floatVal = 0.0F;
      }
   }

   public void render(DrawContext var1, int var2, int var3, float var4) {
      UtilsUtil.run(var1, this.width, this.height);
      UtilsUtil.run9(var1, this.intVal4, this.intVal5, 300, this.intVal6);
      UtilsUtil.run2(var1, this.textRenderer, "Select Blocks", this.intVal4 + 12, this.intVal5 + 12);
      String var5 = this.set.size() + " selected";
      var1.drawText(this.textRenderer, Text.literal(var5), this.intVal4 + 300 - 12 - this.textRenderer.getWidth(var5), this.intVal5 + 12, -8749684, false);
      this.val2.render(var1, var2, var3, var4);
      this.run4(var1, var2, var3);
      super.render(var1, var2, var3, var4);
   }

   private void run4(DrawContext var1, int var2, int var3) {
      AdminDetectorModuleUtil.run2(var1, this.intVal4 + 12 - 2, this.intVal7 - 2, 280, this.intVal8 + 4, 4, -16119023);
      var1.enableScissor(this.intVal4 + 12 - 2, this.intVal7, this.intVal4 + 300 - 12 + 2, this.intVal7 + this.intVal8);
      int var4 = AdminDetectorModuleUtil.getInt();
      int var5 = Math.max(0, (int)(this.floatVal / 18.0F));
      int var6 = Math.min(this.list2.size(), var5 + this.intVal8 / 18 + 2);

      for (int var7 = var5; var7 < var6; var7++) {
         SelectBlocksScreen.Inner1 var8 = this.list2.get(var7);
         int var9 = this.intVal7 + var7 * 18 - Math.round(this.floatVal);
         boolean var10 = var2 >= this.intVal4 + 12
            && var2 <= this.intVal4 + 300 - 12
            && var3 >= var9
            && var3 < var9 + 18
            && var3 >= this.intVal7
            && var3 < this.intVal7 + this.intVal8;
         boolean var11 = this.set.contains(var8.block);
         if (var11) {
            AdminDetectorModuleUtil.run(var1, this.intVal4 + 12 - 2, var9, 280, 18, AdminDetectorModuleUtil.intOf(var4, 0.18F));
            AdminDetectorModuleUtil.run(var1, this.intVal4 + 12 - 2, var9, 2, 18, var4);
         } else if (var10) {
            AdminDetectorModuleUtil.run(var1, this.intVal4 + 12 - 2, var9, 280, 18, AdminDetectorModuleUtil.intOf(var4, 0.08F));
         }

         var1.drawItem(new ItemStack(var8.block), this.intVal4 + 12, var9 + 1);
         int var12 = var11 ? AdminDetectorModuleUtil.getInt2() : (var10 ? -1644558 : -7565402);
         String var13 = this.textRenderer.trimToWidth(var8.name, 242);
         TextRenderer var10001 = this.textRenderer;
         MutableText var10002 = Text.literal(var13);
         int var10003 = this.intVal4 + 12 + 22;
         byte var10005 = 18;
         var1.drawText(var10001, var10002, var10003, var9 + 4 + 1, var12, false);
         if (var11) {
            var10001 = this.textRenderer;
            var10002 = Text.literal("✔");
            var10003 = this.intVal4 + 300 - 12 - this.textRenderer.getWidth("✔");
            var10005 = 18;
            var1.drawText(var10001, var10002, var10003, var9 + 4 + 1, var4, false);
         }
      }

      var1.disableScissor();
      if (this.list2.isEmpty()) {
         String var15 = "No blocks match your search.";
         var1.drawText(
            this.textRenderer,
            Text.literal(var15),
            this.intVal4 + (300 - this.textRenderer.getWidth(var15)) / 2,
            this.intVal7 + this.intVal8 / 2 - 4,
            -8749684,
            false
         );
      }

      float var16 = Math.max(0, this.list2.size() * 18 - this.intVal8);
      if (var16 > 0.0F) {
         int var17 = Math.max(12, Math.round((float)this.intVal8 * this.intVal8 / (this.list2.size() * 18)));
         int var18 = this.intVal7 + Math.round((this.intVal8 - var17) * (this.floatVal / var16));
         AdminDetectorModuleUtil.run2(var1, this.intVal4 + 300 - 12, var18, 2, var17, 1, AdminDetectorModuleUtil.intOf(var4, 0.7F));
      }
   }

   public boolean shouldPause() {
      return false;
   }

   public void close() {
      this.client.setScreen(this.class437);
   }

   private static int intOf(SelectBlocksScreen.Inner1 var0, SelectBlocksScreen.Inner1 var1) {
      return var0.name.compareToIgnoreCase(var1.name);
   }

   private void run5(String var1) {
      this.run(var1);
   }

   final class Inner1 {
      final Block block;
      final String name;
      private String id;
      final String search;

      Inner1(Block var1, String var2, String var3, String var4) {
         this.block = var1;
         this.name = var2;
         this.id = var3;
         this.search = var4;
      }

      public Block block() {
         return this.block;
      }

      public String name() {
         return this.name;
      }

      public String id() {
         return this.id;
      }

      public String search() {
         return this.search;
      }
   }
}
