package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.Locale;
import util.Utils_2;

public final class UdRelogModule extends Module {
   private static final int intVal = 16;
   private static final int intVal2 = 10;
   private static final int intVal3 = 10;
   private static final double doubleVal = 64.0;
   private static final int intVal4 = 600;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Integer> val2_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("trigger-y")
            .valOf2("Starts the relog sequence at or below this Y level.")
            .valOf3(-2)
            .valOf4(-64, -2)
            .valOf5(-64, -2)
            .getVal()
      );
   private final Setting<Integer> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("home-slot")
            .valOf2("Home slot used to return underground after RTP.")
            .valOf3(1)
            .valOf4(1, 3)
            .valOf5(1, 3)
            .getVal()
      );
   private final Setting<Integer> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("redo-delay")
            .valOf2("Cooldown in seconds before another sequence can start.")
            .valOf3(10)
            .valOf4(1, 120)
            .valOf5(1, 60)
            .getVal()
      );
   private final Setting<Boolean> val5 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("chat-feedback").valOf2("Shows the current relog step in chat.").valOf3(true).getVal());
   private UdRelogModule.State val6 = UdRelogModule.State.WAITING_FOR_Y;
   private int intVal5;
   private int intVal6;
   private int intVal7;
   private double doubleVal2;
   private double doubleVal3;
   private double doubleVal4;
   private boolean bool_2;

   public UdRelogModule() {
      super(SwyzzyAddon.val2, "ud-relog", "Runs the underground home, RTP and return sequence.");
      Utils_2.run(this, "UD Relog");
   }

   @Override
   public void run6() {
      this.run12();
   }

   @Override
   public void run7() {
      this.run12();
   }

   @Override
   public String getString2() {
      return this.val6 == UdRelogModule.State.WAITING_FOR_Y ? null : this.val6.label;
   }

   private void run12() {
      this.val6 = UdRelogModule.State.WAITING_FOR_Y;
      this.intVal5 = 0;
      this.intVal6 = 0;
      this.intVal7 = 0;
      this.bool_2 = false;
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null) {
         if (this.intVal6 > 0) {
            this.intVal6--;
         } else {
            switch (this.val6) {
               case WAITING_FOR_Y:
                  if (class310.player.getY() > this.val2_2.getObject().intValue()) {
                     return;
                  }

                  this.run3("delhome " + this.val3_2.getObject());
                  this.run4("Deleting home " + this.val3_2.getObject() + ".");
                  this.intVal5 = 16;
                  this.val6 = UdRelogModule.State.WAIT_SET_HOME;
                  break;
               case WAIT_SET_HOME:
                  if (!this.isEnabled3()) {
                     return;
                  }

                  this.run3("sethome " + this.val3_2.getObject());
                  this.run4("Saved underground home " + this.val3_2.getObject() + ".");
                  this.intVal5 = 10;
                  this.val6 = UdRelogModule.State.WAIT_RTP;
                  break;
               case WAIT_RTP:
                  if (!this.isEnabled3()) {
                     return;
                  }

                  this.run3("rtp");
                  this.run4("Waiting for RTP.");
                  this.doubleVal2 = class310.player.getX();
                  this.doubleVal3 = class310.player.getY();
                  this.doubleVal4 = class310.player.getZ();
                  this.intVal7 = 0;
                  this.bool_2 = false;
                  this.val6 = UdRelogModule.State.WAIT_RTP_DONE;
                  break;
               case WAIT_RTP_DONE:
                  this.intVal7++;
                  if (!this.bool_2 && !this.isEnabled2() && this.intVal7 < 600) {
                     return;
                  }

                  this.intVal5 = 10;
                  this.val6 = UdRelogModule.State.WAIT_HOME;
                  break;
               case WAIT_HOME:
                  if (!this.isEnabled3()) {
                     return;
                  }

                  this.run3("home " + this.val3_2.getObject());
                  this.run4("Returning to home " + this.val3_2.getObject() + ".");
                  this.intVal6 = this.val4.getObject() * 20;
                  this.val6 = UdRelogModule.State.WAITING_FOR_Y;
            }
         }
      }
   }

   @InternalHelper5
   private void run2(UdRelogModuleHelper var1) {
      if (this.val6 == UdRelogModule.State.WAIT_RTP_DONE) {
         String var2 = var1.getclass2561().getString().toLowerCase(Locale.ROOT);
         if (var2.contains("random location")
            || var2.contains("randomly teleported")
            || var2.contains("teleported")
            || var2.contains("rtp complete")
            || var2.contains("rtp successful")) {
            this.bool_2 = true;
         }
      }
   }

   private boolean isEnabled2() {
      double var1 = class310.player.getX() - this.doubleVal2;
      double var3 = class310.player.getY() - this.doubleVal3;
      double var5 = class310.player.getZ() - this.doubleVal4;
      return var1 * var1 + var3 * var3 + var5 * var5 >= 64.0;
   }

   private boolean isEnabled3() {
      if (this.intVal5 <= 0) {
         return true;
      } else {
         this.intVal5--;
         return false;
      }
   }

   private void run3(String var1) {
      if (class310.getNetworkHandler() != null) {
         class310.getNetworkHandler().sendChatCommand(var1);
      }
   }

   private void run4(String var1) {
      if (this.val5.getObject()) {
         this.run4(var1, new Object[0]);
      }
   }

   enum State {
      WAITING_FOR_Y("Idle"),
      WAIT_SET_HOME("Saving"),
      WAIT_RTP("Starting RTP"),
      WAIT_RTP_DONE("RTP"),
      WAIT_HOME("Returning");

      final String label;

      private State(String var3) {
         this.label = var3;
      }

      private static UdRelogModule.State[] getValArray() {
         return new UdRelogModule.State[]{WAITING_FOR_Y, WAIT_SET_HOME, WAIT_RTP, WAIT_RTP_DONE, WAIT_HOME};
      }
   }
}
