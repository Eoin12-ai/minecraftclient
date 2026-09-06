package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import util.Utils_2;

public final class ChunkReloaderModule extends Module {
   private static final long longVal = 800L;
   private static final long longVal2 = 300L;
   private static final long longVal3 = 20000L;
   private static final long longVal4 = 15000L;
   private static final long longVal5 = 15000L;
   private static final long longVal6 = 150000L;
   private static final int intVal = 6;
   private static final double doubleVal = 64.0;
   private static final double doubleVal2 = 16.0;
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Integer> val2_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("home-slot")
            .valOf2("Home slot used to save and restore the current location.")
            .valOf3(1)
            .valOf4(1, 5)
            .valOf5(1, 5)
            .getVal()
      );
   private final Setting<Integer> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("travel-home")
            .valOf2("Home used temporarily while the original chunks reload.")
            .valOf3(3)
            .valOf4(1, 10)
            .valOf5(1, 10)
            .getVal()
      );
   private final Setting<Double> val4 = this.val_2
      .addSetting(
         new AutoTotemModuleHelper2()
            .valOf("trigger-y")
            .valOf2("Starts the automatic reload sequence below this Y level.")
            .valOf3(-3.0)
            .valOf4(-64.0, 64.0)
            .valOf5(-64.0, 64.0)
            .getVal()
      );
   private final Setting<Integer> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("reload-delay")
            .valOf2("Milliseconds spent at the temporary home before returning.")
            .valOf3(3500)
            .valOf4(500, 10000)
            .valOf5(500, 10000)
            .getVal()
      );
   private final Setting<Boolean> val6 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("chat-feedback").valOf2("Shows progress for each automatic reload step.").valOf3(true).getVal());
   private ChunkReloaderModule.State val7 = ChunkReloaderModule.State.IDLE;
   private long longVal7;
   private long longVal8;
   private int intVal2;
   private int intVal3;
   private int intVal4;
   private double doubleVal3;
   private double doubleVal4;
   private double doubleVal5;
   private String string_2 = "";
   private String string2_2 = "";
   private long longVal9;

   public ChunkReloaderModule() {
      super(SwyzzyAddon.val2, "chunk-reloader", "Automatically reloads chunks using a home sequence.");
      Utils_2.run(this, "Chunk Reloader");
   }

   @Override
   public void run6() {
      this.run19();
   }

   @Override
   public void run7() {
      this.run19();
   }

   @Override
   public String getString2() {
      return this.val7 == ChunkReloaderModule.State.IDLE ? null : this.val7.label;
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      long var2 = System.currentTimeMillis();
      if (class310.player != null && class310.world != null && class310.getNetworkHandler() != null) {
         if (this.val7 == ChunkReloaderModule.State.IDLE) {
            if (var2 >= this.longVal8 && class310.player.getY() < this.val4.getObject()) {
               this.run2(var2);
            }
         } else if (var2 >= this.longVal9 || !this.string2_2.equals(this.getString3())) {
            this.run4("Reload sequence canceled after connection change or timeout.", new Object[0]);
            this.run19();
         } else if (this.val7 == ChunkReloaderModule.State.WAIT_TRAVEL_DONE) {
            if (this.isEnabled2()) {
               this.run8("Temporary home " + this.intVal3 + " loaded. Waiting for chunk reload.");
               this.val7 = ChunkReloaderModule.State.WAIT_RETURN;
               this.longVal7 = var2 + this.val5.getObject().intValue();
            } else if (var2 >= this.longVal7) {
               this.run4("Temporary home " + this.intVal3 + " was not reached. Reload sequence canceled.", new Object[0]);
               this.run4(var2);
            }
         } else if (this.val7 == ChunkReloaderModule.State.WAIT_RETURN_DONE) {
            if (this.isEnabled3()) {
               this.run8("Returned to home " + this.intVal2 + ".");
               this.run4(var2);
            } else if (var2 >= this.longVal7) {
               if (this.intVal4 >= 6) {
                  this.run4("Could not return to home " + this.intVal2 + " after " + this.intVal4 + " attempts.", new Object[0]);
                  this.run4(var2);
               } else {
                  this.run3(var2);
               }
            }
         } else if (var2 >= this.longVal7) {
            switch (this.val7) {
               case IDLE:
               case WAIT_TRAVEL_DONE:
               case WAIT_RETURN_DONE:
               default:
                  break;
               case WAIT_SET_HOME:
                  this.run5("sethome " + this.intVal2);
                  this.run8("Saved home " + this.intVal2 + ".");
                  this.val7 = ChunkReloaderModule.State.WAIT_TRAVEL;
                  this.longVal7 = var2 + 300L;
                  break;
               case WAIT_TRAVEL:
                  this.run5("home " + this.intVal3);
                  this.run8("Loading temporary home " + this.intVal3 + ".");
                  this.val7 = ChunkReloaderModule.State.WAIT_TRAVEL_DONE;
                  this.longVal7 = var2 + 20000L;
                  break;
               case WAIT_RETURN:
                  this.run3(var2);
            }
         }
      } else {
         if (this.val7 != ChunkReloaderModule.State.IDLE && var2 >= this.longVal9) {
            this.run19();
         }
      }
   }

   private void run2(long var1) {
      this.intVal2 = this.val2_2.getObject();
      this.intVal3 = this.val3_2.getObject();
      if (this.intVal2 == this.intVal3) {
         this.run4("Home slot and travel home must be different.", new Object[0]);
         this.longVal8 = var1 + 15000L;
      } else {
         this.doubleVal3 = class310.player.getX();
         this.doubleVal4 = class310.player.getY();
         this.doubleVal5 = class310.player.getZ();
         this.string_2 = this.getString();
         this.string2_2 = this.getString3();
         this.longVal9 = var1 + 150000L;
         this.intVal4 = 0;
         this.run5("delhome " + this.intVal2);
         this.run8("Preparing home " + this.intVal2 + ".");
         this.val7 = ChunkReloaderModule.State.WAIT_SET_HOME;
         this.longVal7 = var1 + 800L;
      }
   }

   private void run3(long var1) {
      this.intVal4++;
      this.run5("home " + this.intVal2);
      this.run8("Returning to home " + this.intVal2 + " (attempt " + this.intVal4 + ").");
      this.val7 = ChunkReloaderModule.State.WAIT_RETURN_DONE;
      this.longVal7 = var1 + 15000L;
   }

   private double getDouble() {
      double var1 = class310.player.getX() - this.doubleVal3;
      double var3 = class310.player.getY() - this.doubleVal4;
      double var5 = class310.player.getZ() - this.doubleVal5;
      return var1 * var1 + var3 * var3 + var5 * var5;
   }

   private boolean isEnabled2() {
      return !this.string_2.equals(this.getString()) || this.getDouble() >= 64.0;
   }

   private boolean isEnabled3() {
      return this.string_2.equals(this.getString()) && this.getDouble() <= 16.0;
   }

   private String getString() {
      return class310.world == null ? "" : class310.world.getRegistryKey().getValue().toString();
   }

   private String getString3() {
      return class310.getCurrentServerEntry() == null ? "singleplayer" : class310.getCurrentServerEntry().address;
   }

   private void run4(long var1) {
      this.val7 = ChunkReloaderModule.State.IDLE;
      this.longVal7 = 0L;
      this.longVal8 = var1 + 15000L;
      this.intVal4 = 0;
      this.longVal9 = 0L;
   }

   private void run5(String var1) {
      class310.getNetworkHandler().sendChatCommand(var1);
   }

   private void run8(String var1) {
      if (this.val6.getObject()) {
         this.run4(var1, new Object[0]);
      }
   }

   private void run19() {
      this.val7 = ChunkReloaderModule.State.IDLE;
      this.longVal7 = 0L;
      this.longVal8 = 0L;
      this.intVal2 = 0;
      this.intVal3 = 0;
      this.intVal4 = 0;
      this.longVal9 = 0L;
      this.doubleVal3 = 0.0;
      this.doubleVal4 = 0.0;
      this.doubleVal5 = 0.0;
      this.string_2 = "";
      this.string2_2 = "";
   }

   enum State {
      IDLE("Idle"),
      WAIT_SET_HOME("Saving"),
      WAIT_TRAVEL("Traveling"),
      WAIT_TRAVEL_DONE("Teleporting"),
      WAIT_RETURN("Reloading"),
      WAIT_RETURN_DONE("Returning");

      final String label;

      private State(String var3) {
         this.label = var3;
      }

      private static ChunkReloaderModule.State[] getValArray() {
         return new ChunkReloaderModule.State[]{IDLE, WAIT_SET_HOME, WAIT_TRAVEL, WAIT_TRAVEL_DONE, WAIT_RETURN, WAIT_RETURN_DONE};
      }
   }
}
