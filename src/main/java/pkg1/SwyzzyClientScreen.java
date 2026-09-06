package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import util.UtilsUtil;

public final class SwyzzyClientScreen extends Screen {
   private static final Path path = FabricLoader.getInstance().getConfigDir().resolve("swyzzy-click-gui.properties");
   private static final int intVal = 230;
   private static final int intVal2 = 20;
   private static final int intVal3 = 66;
   private static final List<SwyzzyClientScreenHelper> list = new ArrayList<>();
   private static boolean bool;
   private TextFieldWidget class342;
   private SwyzzyClientScreenHelper val;
   private double doubleVal;
   private double doubleVal2;
   private Setting<?> val2;
   private SwyzzyClientScreenHelper val3;
   private float floatVal;
   private long longVal = System.nanoTime();
   private static final Map<String, double[]> map = new LinkedHashMap<>();

   public SwyzzyClientScreen() {
      super(Text.literal("Swyzzy Client"));
   }

   protected void init() {
      super.init();
      this.run3();
      int var1 = (this.width - 302) / 2;
      int var2 = this.height - 30;
      this.class342 = new TextFieldWidget(this.textRenderer, var1 + 9, var2 + 6, 212, 12, Text.literal("Cracked By Dexter"));
      this.class342.setDrawsBackground(false);
      this.class342.setMaxLength(48);
      this.class342.setEditableColor(-1);
      this.addSelectableChild(this.class342);
      this.setInitialFocus(this.class342);
      this.addDrawableChild(UtilsUtil.valOf("Configs", var1 + 230 + 6, var2, 66, 20, this::run7));
   }

   private void run3() {
      if (list.isEmpty()) {
         if (SwyzzyAddon.swyzzyAddon != null) {
            java.util.LinkedHashMap<SwyzzyAddonHelper, java.util.List<Module>> var1 = new LinkedHashMap<>();

            for (SwyzzyAddonHelper var3 : SwyzzyAddon.list) {
               var1.put(var3, new ArrayList());
            }

            for (Module var10 : SwyzzyAddon.swyzzyAddon.getVal().getList()) {
               ((java.util.List)var1.computeIfAbsent(var10.val, a0x -> SwyzzyClientScreen.listOf(a0x))).add(var10);
            }

            run4();
            double var9 = 10.0;

            for (Entry var5 : (Iterable<Entry>)(Object)(var1.entrySet())) {
               if (!((List)var5.getValue()).isEmpty()) {
                  double[] var6 = map.get(stringOf((SwyzzyAddonHelper)var5.getKey()));
                  SwyzzyClientScreenHelper var7 = new SwyzzyClientScreenHelper(
                     (SwyzzyAddonHelper)var5.getKey(), (List<Module>)var5.getValue(), var6 == null ? var9 : var6[0], var6 == null ? 10.0 : var6[1]
                  );
                  list.add(var7);
                  var9 += 136.0;
               }
            }
         }
      }
   }

   public void renderBackground(DrawContext var1, int var2, int var3, float var4) {
   }

   public void render(DrawContext var1, int var2, int var3, float var4) {
      long var5 = System.nanoTime();
      float var7 = Math.min(0.1F, (float)(var5 - this.longVal) / 1.0E9F);
      this.longVal = var5;
      this.floatVal = AdminDetectorModuleUtil.floatOf(this.floatVal, 1.0F, var7, 10.0F);
      var1.fill(0, 0, this.width, this.height, AdminDetectorModuleUtil.intOf(-939194869, this.floatVal));
      var1.fillGradient(
         0, 0, this.width, this.height, AdminDetectorModuleUtil.intOf(1075055144, this.floatVal), AdminDetectorModuleUtil.intOf(1711604489, this.floatVal)
      );
      SwyzzyClientScreenUtil.run(var1, this.width, this.height, this.floatVal);
      if (this.val != null) {
         this.val.run(var2 - this.doubleVal, var3 - this.doubleVal2);
      }

      if (this.val2 != null && this.val3 != null) {
         this.val3.run5(this.val2, var2);
      }

      String var8 = this.class342 == null ? "" : this.class342.getText();

      for (SwyzzyClientScreenHelper var10 : list) {
         var10.run2(var1, this.textRenderer, var2, var3, var7, var8, this.height);
      }

      this.run(var1, var2, var3);
      super.render(var1, var2, var3, var4);
   }

   private void run(DrawContext var1, int var2, int var3) {
      int var4 = (this.width - 302) / 2;
      int var5 = this.height - 30;
      boolean var6 = var2 >= var4 && var2 <= var4 + 230 && var3 >= var5 && var3 <= var5 + 20;
      boolean var7 = this.class342 != null && this.class342.isFocused();
      AdminDetectorModuleUtil.run5(var1, var4, var5, 230, 20, 3);
      AdminDetectorModuleUtil.run2(
         var1, var4, var5, 230, 20, 6, !var7 && !var6 ? -267711469 : AdminDetectorModuleUtil.intOf2(-267711469, AdminDetectorModuleUtil.getInt3(), 0.45F)
      );
      AdminDetectorModuleUtil.run2(var1, var4, var5 + 20 - 2, 230, 2, 1, AdminDetectorModuleUtil.intOf(AdminDetectorModuleUtil.getInt(), var7 ? 1.0F : 0.45F));
      if (this.class342 != null) {
         if (this.class342.getText().isEmpty() && !var7) {
            TextRenderer var10001 = this.textRenderer;
            MutableText var10002 = Text.literal("Cracked By Dexter");
            int var10003 = var4 + 9;
            byte var10005 = 20;
            var1.drawText(var10001, var10002, var10003, var5 + 5 + 1, -7565402, false);
         }

         this.class342.render(var1, var2, var3, 0.0F);
      }

      var1.drawText(
         this.textRenderer,
         Text.literal("LEFT CLICK TOGGLE   RIGHT CLICK SETTINGS   DRAG HEADER TO MOVE"),
         (this.width - this.textRenderer.getWidth("LEFT CLICK TOGGLE   RIGHT CLICK SETTINGS   DRAG HEADER TO MOVE")) / 2,
         this.height - 10,
         AdminDetectorModuleUtil.intOf(-7565402, 0.8F),
         false
      );
   }

   public boolean mouseClicked(Click var1, boolean var2) {
      double var3 = var1.x();
      double var5 = var1.y();
      int var7 = var1.button();

      for (int var8 = list.size() - 1; var8 >= 0; var8--) {
         SwyzzyClientScreenHelper var9 = list.get(var8);
         if (var9.check(var3, var5)) {
            if (this.class342 != null) {
               this.class342.setFocused(false);
            }

            Setting var10 = var9.valOf(var3, var5, var7, this);
            if (var10 != null) {
               this.val2 = var10;
               this.val3 = var9;
            }

            list.remove(var8);
            list.add(var9);
            return true;
         }
      }

      return super.mouseClicked(var1, var2);
   }

   public boolean mouseReleased(Click var1) {
      this.val = null;
      this.val2 = null;
      this.val3 = null;
      return super.mouseReleased(var1);
   }

   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      for (int var9 = list.size() - 1; var9 >= 0; var9--) {
         if (list.get(var9).check3(var1, var3, var7)) {
            return true;
         }
      }

      return super.mouseScrolled(var1, var3, var5, var7);
   }

   public boolean keyPressed(int _key, int _scan, int var1) {
      int var2 = var1.key();
      if (var2 == 256) {
         this.close();
         return true;
      } else if (var2 != SwyzzyAddon.val7.getInt() || this.class342 != null && this.class342.isFocused()) {
         return super.keyPressed(var1);
      } else {
         this.close();
         return true;
      }
   }

   public void run2(SwyzzyClientScreenHelper var1, double var2, double var4) {
      this.val = var1;
      this.doubleVal = var2;
      this.doubleVal2 = var4;
   }

   public void close() {
      run6();
      super.close();
   }

   public boolean shouldPause() {
      return false;
   }

   private static String stringOf(SwyzzyAddonHelper var0) {
      return var0.name().toLowerCase(Locale.ROOT).replace(' ', '-');
   }

   private static void run4() {
      if (!bool) {
         bool = true;
         if (Files.exists(path)) {
            Properties var0 = new Properties();

            try (InputStream var1 = Files.newInputStream(path)) {
               var0.load(var1);
            } catch (IOException var8) {
               SwyzzyAddon.logger.warn("Could not load the Swyzzy click GUI layout.", var8);
               return;
            }

            for (String var2 : var0.stringPropertyNames()) {
               String[] var3 = var0.getProperty(var2).split(",");
               if (var3.length == 2) {
                  try {
                     map.put(var2, new double[]{Double.parseDouble(var3[0]), Double.parseDouble(var3[1])});
                  } catch (NumberFormatException var6) {
                  }
               }
            }
         }
      }
   }

   private static void run6() {
      Properties var0 = new Properties();

      for (SwyzzyClientScreenHelper var2 : list) {
         var0.setProperty(stringOf(var2.getVal()), Math.round(var2.getDouble()) + "," + Math.round(var2.getDouble2()));
      }

      try {
         Files.createDirectories(path.getParent());

         try (OutputStream var7 = Files.newOutputStream(path)) {
            var0.store(var7, "Swyzzy click GUI window positions");
         }
      } catch (IOException var6) {
         SwyzzyAddon.logger.warn("Could not save the Swyzzy click GUI layout.", var6);
      }
   }

   private static List listOf(SwyzzyAddonHelper var0) {
      return new ArrayList<>();
   }

   private void run7() {
      this.client.setScreen(new ConfigManagerScreen(this));
   }
}
