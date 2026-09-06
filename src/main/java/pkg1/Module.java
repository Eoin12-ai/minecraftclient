package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public abstract class Module {
   protected static final MinecraftClient class310 = MinecraftClient.getInstance();
   public SwyzzyAddonHelper val;
   public String string;
   public String title;
   public String string2;
   public final General val2 = new General();
   public Setting<Tab> val3;
   private boolean bool;
   private Runnable runnable;
   public static final String string3 = "License Key";

   protected Module(SwyzzyAddonHelper var1, String var2, String var3) {
      this.val = var1;
      this.string = var2;
      this.title = addSetting(var2);
      this.string2 = var3;
      this.val3 = this.val2
         .valOf("Keybind")
         .addSetting(new ModuleHelper2().valOf("bind").valOf2("Key that toggles this module on and off. Delete to unbind.").valOf3(Tab.getVal()).getVal());
   }

   public final Module valOf(SwyzzyAddonHelper var1) {
      this.val = var1;
      return this;
   }

   public final boolean isEnabled() {
      return this.bool;
   }

   public final void run3() {
      this.run(!this.bool);
   }

   public final void run(boolean var1) {
      if (this.bool != var1) {
         if (var1 && !this.string.equals("License Key") && !LicenseKeyModuleUtil.isEnabled()) {
            this.run4("§cLicense required. Enter your key in the §fLicense Key§c module.");
         } else {
            this.bool = var1;
            if (var1) {
               this.run6();
            } else {
               this.run7();
            }

            if (this.runnable != null) {
               this.runnable.run();
            }
         }
      }
   }

   void run2(Runnable var1) {
      this.runnable = var1;
   }

   public void run6() {
   }

   public void run7() {
   }

   public String getString2() {
      return null;
   }

   protected final void run4(String var1, Object... var2) {
      String var3 = String.format(var1, var2);
      if (class310.player != null) {
         class310.player.sendMessage(Text.literal("[" + this.title + "] " + var3), false);
      } else {
         SwyzzyAddon.logger.info("[{}] {}", this.title, var3);
      }
   }

   private static String addSetting(String var0) {
      StringBuilder var1 = new StringBuilder(var0.length());
      boolean var2 = true;

      for (char var6 : var0.toCharArray()) {
         if (var6 != '-' && var6 != '_') {
            var1.append(var2 ? Character.toUpperCase(var6) : var6);
            var2 = false;
         } else {
            var1.append(' ');
            var2 = true;
         }
      }

      return var1.toString();
   }
}
