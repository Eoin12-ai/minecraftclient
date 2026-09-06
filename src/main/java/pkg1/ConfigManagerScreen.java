package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import util.UtilsUtil;

public class ConfigManagerScreen extends Screen {
   private static final int intVal = 320;
   private static final int intVal2 = 324;
   private static final int intVal3 = 22;
   private static final int intVal4 = 4;
   private static final int intVal5 = -11141291;
   private static final int intVal6 = -43691;
   private static final int intVal7 = -6250328;
   private Screen class437;
   private final SWZ1 val = SwyzzyAddon.val8;
   private util.Inner1 val2;
   private util.Inner1 val3;
   private String string = "";
   private int intVal8 = -6250328;
   private int intVal9;
   private int intVal10;

   public ConfigManagerScreen(Screen var1) {
      super(Text.literal("Config Manager"));
      this.class437 = var1;
   }

   protected void init() {
      super.init();
      List var1 = this.val.getList();
      int var2 = Math.max(1, (var1.size() + 4 - 1) / 4);
      this.intVal9 = Math.max(0, Math.min(this.intVal9, var2 - 1));
      this.intVal10 = Math.max(6, this.height / 2 - 162);
      int var3 = this.width / 2 - 160;
      this.val2 = UtilsUtil.valOf2(this.textRenderer, var3 + 4, this.intVal10 + 18, 170, 20, "Config name");
      this.val2.setMaxLength(48);
      this.val2.setPlaceholder(Text.literal("Config name"));
      this.addDrawableChild(this.val2);
      this.addDrawableChild(UtilsUtil.valOf("Save", var3 + 178, this.intVal10 + 18, 56, 20, this::run3));
      this.addDrawableChild(UtilsUtil.valOf("Export", var3 + 238, this.intVal10 + 18, 62, 20, this::run5));
      int var4 = this.intVal10 + 56;

      for (int var5 = 0; var5 < 4; var5++) {
         int var6 = this.intVal9 * 4 + var5;
         if (var6 >= var1.size()) {
            break;
         }

         String var7 = (String)var1.get(var6);
         int var8 = var4 + var5 * 22;
         this.addDrawableChild(UtilsUtil.valOf("Load", var3 + 172, var8, 44, 20, () -> this.run22(var7)));
         this.addDrawableChild(UtilsUtil.valOf("Export", var3 + 220, var8, 46, 20, () -> this.run21(var7)));
         this.addDrawableChild(UtilsUtil.valOf("Delete", var3 + 270, var8, 46, 20, () -> this.run20(var7)));
      }

      if (var2 > 1) {
         this.addDrawableChild(UtilsUtil.valOf("<", var3 + 4, this.intVal10 + 146, 20, 20, this::run19));
         this.addDrawableChild(UtilsUtil.valOf(">", var3 + 28, this.intVal10 + 146, 20, 20, this::run18));
      }

      this.addDrawableChild(UtilsUtil.valOf("Import from file", var3 + 4, this.intVal10 + 184, 150, 20, this::run9));
      this.addDrawableChild(UtilsUtil.valOf("Open shares folder", var3 + 162, this.intVal10 + 184, 154, 20, this::run8));
      this.val3 = UtilsUtil.valOf2(this.textRenderer, var3 + 4, this.intVal10 + 222, 250, 20, "Share code");
      this.val3.setMaxLength(32767);
      this.val3.setPlaceholder(Text.literal("Paste a short share code"));
      this.addDrawableChild(this.val3);
      this.addDrawableChild(UtilsUtil.valOf("Paste", var3 + 260, this.intVal10 + 222, 56, 20, this::run11));
      this.addDrawableChild(UtilsUtil.valOf("Load code", var3 + 4, this.intVal10 + 246, 152, 20, this::run12));
      this.addDrawableChild(UtilsUtil.valOf("Save code as config", var3 + 162, this.intVal10 + 246, 154, 20, this::run13));
      this.addDrawableChild(UtilsUtil.valOf("Done", this.width / 2 - 60, this.intVal10 + 274, 120, 20, this::close));
   }

   private void run3() {
      String var1 = this.val2.getText().trim();
      if (var1.isEmpty()) {
         this.run14("Enter a name first.", -43691);
      } else {
         boolean var2 = this.val.check(var1);
         if (this.val.check2(var1)) {
            this.val2.setText("");
            this.run14(var2 ? "Overwrote \"null\"." : "Saved \"null\".", -11141291);
            this.run15();
         } else {
            this.run14("Could not save the config.", -43691);
         }
      }
   }

   private void run(String var1) {
      if (this.val.check4(var1)) {
         this.run14("Loaded \"null\".", -11141291);
      } else {
         this.run14("\"null\" is damaged and could not be loaded.", -43691);
      }
   }

   private void run2(String var1) {
      this.val.run20(var1);
      this.run14("Deleted \"null\".", -11141291);
      this.run15();
   }

   private void run4(int var1) {
      this.intVal9 += var1;
      this.run15();
   }

   private void run5() {
      String var1 = this.val2.getText().trim();
      this.run7(var1.isEmpty() ? "config" : var1, this.val::pathOf2);
   }

   private void run6(String var1) {
      this.run7(var1, var905 -> this.pathOf(var1, var905));
   }

   private void run7(String var1, ConfigManagerScreen.Inner1 var2) {
      try {
         Path var3 = var2.export(var1);
         this.run14("Exported to " + var3.getFileName() + " — send that file to share it.", -11141291);
      } catch (IOException var4) {
         this.run14("Could not export \"null\".", -43691);
      }
   }

   private void run8() {
      try {
         Files.createDirectories(this.val.getPath());
      } catch (IOException var2) {
      }

      Util.getOperatingSystem().open(this.val.getPath());
   }

   private void run9() {
      this.run14("Opening the file picker...", -6250328);
      Thread var1 = new Thread(this::run16, "Swyzzy-Import-Dialog");
      var1.setDaemon(true);
      var1.start();
   }

   private void run10(String var1, String var2) {
      if (var1 != null && var2 != null) {
         Path var3 = Paths.get(var1, var2);
         String var4 = SWZ1.stringOf(var3);
         if (var4 == null) {
            this.run14("That file doesn't contain a valid Swyzzy config.", -43691);
         } else {
            String var5 = addSetting(var2);
            boolean var6 = this.val.check(var5);
            if (this.val.check5(var4) && this.val.check3(var5, var4)) {
               this.run14((var6 ? "Updated \"" : "Imported \"") + var5 + "\" and applied it.", -11141291);
               this.run15();
            } else {
               this.run14("That file doesn't contain a valid Swyzzy config.", -43691);
            }
         }
      } else {
         this.run14("Import cancelled.", -6250328);
      }
   }

   private static String addSetting(String var0) {
      int var1 = var0.lastIndexOf(46);
      return var1 > 0 ? var0.substring(0, var1) : var0;
   }

   private void run11() {
      this.val3.setText(this.client.keyboard.getClipboard().trim());
      this.run14("Pasted from your clipboard.", -6250328);
   }

   private void run12() {
      String var1 = this.val3.getText().trim();
      if (var1.isEmpty()) {
         this.run14("Paste a share code first.", -43691);
      } else {
         if (this.val.check5(var1)) {
            this.run14("Config loaded from the code.", -11141291);
         } else {
            this.run14("That code is invalid or was cut short when it was copied.", -43691);
         }
      }
   }

   private void run13() {
      String var1 = this.val3.getText().trim();
      String var2 = this.val2.getText().trim();
      if (var1.isEmpty()) {
         this.run14("Paste a share code first.", -43691);
      } else if (var2.isEmpty()) {
         this.run14("Enter a name for the code.", -43691);
      } else {
         if (this.val.check3(var2, var1)) {
            this.val2.setText("");
            this.run14("Saved the code as \"null\".", -11141291);
            this.run15();
         } else {
            this.run14("That code is invalid or was cut short when it was copied.", -43691);
         }
      }
   }

   private void run14(String var1, int var2) {
      this.string = var1;
      this.intVal8 = var2;
   }

   private void run15() {
      String var1 = this.val2.getText();
      String var2 = this.val3.getText();
      this.clearAndInit();
      this.val2.setText(var1);
      this.val3.setText(var2);
   }

   public void render(DrawContext var1, int var2, int var3, float var4) {
      UtilsUtil.run(var1, this.width, this.height);
      int var5 = this.width / 2 - 160;
      UtilsUtil.run9(var1, var5 - 8, this.intVal10 - 10, 336, 324);
      UtilsUtil.run2(var1, this.textRenderer, "Configs", var5 + 4, this.intVal10);
      List var6 = this.val.getList();
      var1.drawTextWithShadow(this.textRenderer, Text.literal("Saved configs"), var5 + 4, this.intVal10 + 44, -6250328);
      if (var6.isEmpty()) {
         var1.drawTextWithShadow(this.textRenderer, Text.literal("No configs yet — save your current settings above."), var5 + 4, this.intVal10 + 60, -6250328);
      } else {
         int var7 = this.intVal10 + 56;

         for (int var8 = 0; var8 < 4; var8++) {
            int var9 = this.intVal9 * 4 + var8;
            if (var9 >= var6.size()) {
               break;
            }

            String var10 = (String)var6.get(var9);
            int var11 = var7 + var8 * 22 + 6;
            var1.drawTextWithShadow(this.textRenderer, Text.literal(this.textRenderer.trimToWidth(var10, 160)), var5 + 4, var11, -1);
         }

         int var12 = Math.max(1, (var6.size() + 4 - 1) / 4);
         if (var12 > 1) {
            var1.drawTextWithShadow(this.textRenderer, Text.literal("Page " + (this.intVal9 + 1) + " / " + var12), var5 + 54, this.intVal10 + 152, -6250328);
         }
      }

      var1.drawTextWithShadow(this.textRenderer, Text.literal("Share via file (recommended)"), var5 + 4, this.intVal10 + 172, -6250328);
      var1.drawTextWithShadow(this.textRenderer, Text.literal("Or paste a short code"), var5 + 4, this.intVal10 + 210, -6250328);
      if (!this.string.isEmpty()) {
         var1.drawCenteredTextWithShadow(this.textRenderer, Text.literal(this.string), this.width / 2, this.intVal10 + 300, this.intVal8);
      }

      super.render(var1, var2, var3, var4);
   }

   public void close() {
      this.client.setScreen(this.class437);
   }

   private void run16() {
      FileDialog var1 = new FileDialog((Frame)null, "Import a Swyzzy config", 0);

      try {
         Files.createDirectories(this.val.getPath());
         var1.setDirectory(this.val.getPath().toString());
      } catch (Exception var4) {
      }

      var1.setVisible(true);
      String var2 = var1.getDirectory();
      String var3 = var1.getFile();
      if (this.client != null) {
         this.client.execute(() -> this.run17(var3, var3));
      }
   }

   private void run17(String var1, String var2) {
      this.run10(var1, var2);
   }

   private Path pathOf(String var1, String var2) throws IOException {
      return this.val.pathOf3(var1);
   }

   private void run18() {
      this.run4(1);
   }

   private void run19() {
      this.run4(-1);
   }

   private void run20(String var1) {
      this.run2(var1);
   }

   private void run21(String var1) {
      this.run6(var1);
   }

   private void run22(String var1) {
      this.run(var1);
   }

   @FunctionalInterface
   interface Inner1 {
      Path export(String var1) throws IOException;
   }
}
