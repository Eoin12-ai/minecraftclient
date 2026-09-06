package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;

public final class PearlMetaModule extends Module {
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<String> val2_2 = this.val_2
      .addSetting(
         new AdminDetectorModuleHelper2()
            .valOf("command")
            .valOf2("Command sent when the pearl leaves your hand. Without the leading slash.")
            .valOf3("rtp")
            .getVal()
      );
   private final Setting<Integer> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("max-y")
            .valOf2("Only fires while you are below this Y level.")
            .valOf3(-5)
            .valOf4(-64, 320)
            .valOf5(-64, 100)
            .getVal()
      );
   private final Setting<Integer> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("cooldown")
            .valOf2("Ticks before the command can fire again.")
            .valOf3(20)
            .valOf4(1, 200)
            .valOf5(1, 100)
            .getVal()
      );
   private int intVal;
   private boolean bool_2;

   public PearlMetaModule() {
      super(SwyzzyAddon.val2, "pearl-meta", "Sends a command when you throw a pearl underground.");
   }

   @Override
   public void run6() {
      this.intVal = 0;
      this.bool_2 = false;
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (this.intVal > 0) {
         this.intVal--;
      }

      if (this.bool_2) {
         this.bool_2 = false;
         String var2 = this.val2_2.getObject().trim();
         if (var2.startsWith("/")) {
            var2 = var2.substring(1);
         }

         if (!var2.isEmpty() && class310.player != null && class310.getNetworkHandler() != null) {
            class310.getNetworkHandler().sendChatCommand(var2);
         }
      }
   }

   @InternalHelper5
   private void run2(PearlMetaModuleData var1) {
      if (this.intVal <= 0 && !this.bool_2) {
         if (var1.object instanceof PlayerInteractItemC2SPacket var2) {
            if (class310.player != null && !(class310.player.getY() >= this.val3_2.getObject().intValue())) {
               if (class310.player.getStackInHand(var2.getHand()).isOf(Items.ENDER_PEARL)) {
                  this.bool_2 = true;
                  this.intVal = this.val4.getObject();
               }
            }
         }
      }
   }

   @Override
   public String getString2() {
      return this.intVal > 0 ? String.valueOf(this.intVal) : null;
   }
}
