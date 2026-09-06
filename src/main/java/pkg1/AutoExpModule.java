package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import util.Utils_2;

public final class AutoExpModule extends Module {
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Integer> val2_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("throw-delay")
            .valOf2("Ticks between experience bottle throws. Zero throws once every client tick.")
            .valOf3(1)
            .valOf4(0, 20)
            .valOf5(0, 20)
            .getVal()
      );
   private final Setting<Boolean> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("require-use-key")
            .valOf2("Only throws bottles while the normal use-item key is held.")
            .valOf3(false)
            .getVal()
      );
   private final Setting<Boolean> val4 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("swing-hand").valOf2("Shows the hand swing when a bottle is thrown.").valOf3(true).getVal());
   private final Setting<Boolean> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("pause-in-gui")
            .valOf2("Stops throwing while an inventory or another screen is open.")
            .valOf3(true)
            .getVal()
      );
   private int intVal;

   public AutoExpModule() {
      super(SwyzzyAddon.val2, "auto-exp", "Automatically throws experience bottles held in either hand.");
      Utils_2.run(this, "Auto EXP");
   }

   @Override
   public void run6() {
      this.intVal = 0;
   }

   @Override
   public void run7() {
      this.intVal = 0;
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null && class310.interactionManager != null) {
         if (!this.val5.getObject() || class310.currentScreen == null) {
            if (!this.val3_2.getObject() || class310.options.useKey.isPressed()) {
               Hand var2 = this.getclass1268();
               if (var2 == null) {
                  this.intVal = 0;
               } else if (this.intVal > 0) {
                  this.intVal--;
               } else {
                  class310.interactionManager.interactItem(class310.player, var2);
                  if (this.val4.getObject()) {
                     class310.player.swingHand(var2);
                  }

                  this.intVal = this.val2_2.getObject();
               }
            }
         }
      }
   }

   private Hand getclass1268() {
      if (class310.player.getMainHandStack().isOf(Items.EXPERIENCE_BOTTLE)) {
         return Hand.MAIN_HAND;
      } else {
         return class310.player.getOffHandStack().isOf(Items.EXPERIENCE_BOTTLE) ? Hand.OFF_HAND : null;
      }
   }
}
