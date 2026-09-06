package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public final class InternalHelper4 extends Screen {
   private static final int intVal = 260;
   private static final int intVal2 = 84;
   private Screen class437;
   private Setting<Tab> val;

   public InternalHelper4(Screen var1, Setting<Tab> var2) {
      super(Text.literal("Bind " + var2.string));
      this.class437 = var1;
      this.val = var2;
   }

   public boolean keyPressed(KeyInput var1) {
      int var2 = var1.key();
      if (var2 == 256) {
         this.close();
         return true;
      } else {
         this.run(var2 != 261 && var2 != 259 ? var2 : -1);
         return true;
      }
   }

   public boolean mouseClicked(Click var1, boolean var2) {
      this.val.run2(Tab.valOf(var1.button()));
      this.close();
      return true;
   }

   private void run(int var1) {
      this.val.run2(new Tab(var1));
      this.close();
   }

   public void render(DrawContext var1, int var2, int var3, float var4) {
      this.renderBackground(var1, var2, var3, var4);
      int var5 = (this.width - 260) / 2;
      int var6 = (this.height - 84) / 2;
      int var7 = SwyzzyAddon.val7.getVal().intOf(255);
      var1.fill(var5, var6, var5 + 260, var6 + 84, -267645676);
      var1.fill(var5, var6, var5 + 260, var6 + 2, var7);
      var1.drawCenteredTextWithShadow(this.textRenderer, Text.literal("PRESS ANY KEY OR MOUSE BUTTON"), this.width / 2, var6 + 20, var7);
      var1.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Current: " + this.val.getObject().getString3()), this.width / 2, var6 + 38, -4276020);
      var1.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Delete = unbind    Esc = cancel"), this.width / 2, var6 + 56, -8749684);
      super.render(var1, var2, var3, var4);
   }

   public boolean shouldPause() {
      return false;
   }

   public void close() {
      this.client.setScreen(this.class437);
   }
}
