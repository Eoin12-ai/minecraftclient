package dev.sixseven.module.misc;

import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.BooleanSetting;
import dev.sixseven.settings.SliderSetting;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class GambleRiggerModule extends Module {
   private static final int GRID = 9;
   private static final int INV_START = 9;
   private static final int INV_END = 45;
   private static final int MOVE = 0;
   private static final int QMOVE = 1;
   public final SliderSetting clickDelay = this.addSetting(
      new SliderSetting(
            "Click Delay",
            "Ticks between each moved stack. 0 = instant (whole grid in one tick); raise it only if a server flags rapid container clicks.",
            0.0,
            0.0,
            10.0,
            1.0
         )
         .withLabel(arg -> (int)arg <= 0 ? "Instant" : (int)arg + " tick")
   );
   public final BooleanSetting keepStays = this.addSetting(
      new BooleanSetting(
         "Keep Slot Stays",
         "Leave the picked slot untouched in the container (someone else refills it). Off: pull it out too and restore leaves it empty.",
         true
      )
   );
   public final BooleanSetting chatFeedback = this.addSetting(new BooleanSetting("Chat Feedback", "Print what the rigger is doing to chat.", true));
   private final MinecraftClient mc = MinecraftClient.getInstance();
   private GambleRiggerModule.Phase phase = GambleRiggerModule.Phase.IDLE;
   private int keepSlot = -1;
   private int containerId = -1;
   private final ItemStack[] snapshot = new ItemStack[9];
   private final int[] parked = new int[9];
   private final Deque<int[]> queue = new ArrayDeque<>();
   private int delay = 0;

   public GambleRiggerModule() {
      super(
         "GambleRigger",
         "Rig a dispenser/dropper gamble — a fake slot-panel keypad that pulls every slot but the one you keep, then restores the exact layout.",
         Category.MISC
      );
   }

   public GambleRiggerModule.Phase phase() {
      return this.phase;
   }

   public int keepSlot() {
      return this.keepSlot;
   }

   public int queued() {
      return this.queue.size();
   }

   public boolean busy() {
      return this.phase == GambleRiggerModule.Phase.EXTRACTING || this.phase == GambleRiggerModule.Phase.RESTORING;
   }

   public boolean canExtract() {
      return this.phase == GambleRiggerModule.Phase.IDLE && this.dispenserOpen();
   }

   public boolean canRestore() {
      return this.phase == GambleRiggerModule.Phase.EXTRACTED && this.dispenserOpen();
   }

   public void requestKeep(int n) {
      if (this.canExtract() && n >= 0 && n < 9) {
         ClientPlayerEntity player = this.mc.player;
         if (player != null && player.currentScreenHandler instanceof Generic3x3ContainerScreenHandler generic3x3ContainerScreenHandler) {
            this.containerId = generic3x3ContainerScreenHandler.syncId;
            this.keepSlot = n;
            this.queue.clear();
            this.delay = 0;
            boolean[] visible = new boolean[45];
            int localY = 0;

            for (int step = 0; step < 9; step++) {
               this.snapshot[step] = generic3x3ContainerScreenHandler.getSlot(step).getStack().copy();
               this.parked[step] = -1;
            }

            for (int step2 = 0; step2 < 9; step2++) {
               if ((!this.keepStays.get() || step2 != this.keepSlot) && !this.snapshot[step2].isEmpty()) {
                  int n9 = this.firstEmptyInv(generic3x3ContainerScreenHandler, visible);
                  if (n9 >= 0) {
                     visible[n9] = true;
                     this.parked[step2] = n9;
                     this.queue.add(new int[]{0, step2, n9});
                  } else {
                     localY++;
                     this.queue.add(new int[]{1, step2, 0});
                  }
               }
            }

            if (this.queue.isEmpty()) {
               this.phase = GambleRiggerModule.Phase.EXTRACTED;
               this.feedback("§dGambleRigger §7» grid empty — nothing to take, click §fRestore §7when done");
               return;
            }

            this.phase = GambleRiggerModule.Phase.EXTRACTING;
            if (localY > 0) {
               this.feedback("§eGambleRigger §7» your inventory is nearly full — " + localY + " stack(s) fall back to shift-click and may not restore exactly");
            }

            return;
         }
      }
   }

   public void requestRestore() {
      if (this.canRestore()) {
         ClientPlayerEntity player = this.mc.player;
         if (player != null && player.currentScreenHandler instanceof Generic3x3ContainerScreenHandler generic3x3ContainerScreenHandler && generic3x3ContainerScreenHandler.syncId == this.containerId) {
            this.queue.clear();
            this.delay = 0;
            boolean[] visible = new boolean[45];
            int n = 0;

            for (int localX = 0; localX < 9; localX++) {
               if (localX != this.keepSlot) {
                  ItemStack stack = this.snapshot[localX];
                  if (stack != null && !stack.isEmpty()) {
                     int localZ = this.findSource(generic3x3ContainerScreenHandler, stack, localX, visible);
                     if (localZ < 0) {
                        n++;
                     } else {
                        visible[localZ] = true;
                        this.queue.add(new int[]{0, localZ, localX});
                     }
                  }
               }
            }

            this.phase = GambleRiggerModule.Phase.RESTORING;
            if (n > 0) {
               this.feedback("§eGambleRigger §7» restoring, but §c" + n + " §7stack(s) were not found in your inventory");
            }

            return;
         }

         this.abort(true);
      }
   }

   public void reset() {
      boolean visible = this.phase != GambleRiggerModule.Phase.IDLE || this.keepSlot >= 0;
      this.resetState();
      if (visible) {
         this.feedback("§7GambleRigger » reset");
      }
   }

   @Override
   public void onTick() {
      this.tickRigQueue();
   }

   private void tickRigQueue() {
      if (this.queue.isEmpty()) {
         this.finishIfDrained();
      } else {
         ClientPlayerEntity player = this.mc.player;
         if (player != null
            && this.mc.interactionManager != null
            && player.currentScreenHandler instanceof Generic3x3ContainerScreenHandler
            && player.currentScreenHandler.syncId == this.containerId) {
            boolean visible = this.clickDelay.getInt() <= 0;

            while (!this.queue.isEmpty()) {
               if (this.delay > 0) {
                  this.delay--;
                  return;
               }

               this.executeStep(player, this.queue.poll());
               this.delay = this.clickDelay.getInt();
               if (!visible) {
                  return;
               }
            }

            this.finishIfDrained();
         } else {
            this.abort(true);
         }
      }
   }

   private void finishIfDrained() {
      if (this.queue.isEmpty()) {
         if (this.phase == GambleRiggerModule.Phase.EXTRACTING) {
            this.phase = GambleRiggerModule.Phase.EXTRACTED;
            this.feedback("§aGambleRigger §7» pulled the grid (kept §f#" + (this.keepSlot + 1) + "§7) — click §fRestore §7when ready");
         } else if (this.phase == GambleRiggerModule.Phase.RESTORING) {
            this.feedback("§aGambleRigger §7» layout restored — slot §f#" + (this.keepSlot + 1) + " §7left open");
            this.resetState();
         }
      }
   }

   private void executeStep(PlayerEntity player, int[] n) {
      ScreenHandler screenHandler = player.currentScreenHandler;
      if (n[0] == 1) {
         this.click(player, n[1], 0, SlotActionType.QUICK_MOVE);
      } else {
         this.click(player, n[1], 0, SlotActionType.PICKUP);
         if (!screenHandler.getCursorStack().isEmpty()) {
            this.click(player, n[2], 0, SlotActionType.PICKUP);
         }

         if (!screenHandler.getCursorStack().isEmpty()) {
            this.click(player, n[1], 0, SlotActionType.PICKUP);
         }
      }
   }

   @Override
   protected void onDisable() {
      this.abort(false);
   }

   private boolean dispenserOpen() {
      ClientPlayerEntity player = this.mc.player;
      return player != null && player.currentScreenHandler instanceof Generic3x3ContainerScreenHandler;
   }

   private void click(PlayerEntity player, int n, int offset, SlotActionType slotActionType) {
      this.mc.interactionManager.clickSlot(this.containerId, n, offset, slotActionType, player);
   }

   private int firstEmptyInv(ScreenHandler screenHandler, boolean[] value) {
      for (int temp = 9; temp < 45; temp++) {
         if (!value[temp] && screenHandler.getSlot(temp).getStack().isEmpty()) {
            return temp;
         }
      }

      return -1;
   }

   private int findSource(ScreenHandler screenHandler, ItemStack stack, int n, boolean[] value) {
      int localZ = this.parked[n];
      if (localZ >= 9 && localZ < 45 && !value[localZ] && ItemStack.areEqual(screenHandler.getSlot(localZ).getStack(), stack)) {
         return localZ;
      } else {
         for (int localY = 9; localY < 45; localY++) {
            if (!value[localY] && ItemStack.areEqual(screenHandler.getSlot(localY).getStack(), stack)) {
               return localY;
            }
         }

         for (int step = 9; step < 45; step++) {
            if (!value[step]) {
               ItemStack hotbarStack = screenHandler.getSlot(step).getStack();
               if (!hotbarStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(hotbarStack, stack)) {
                  return step;
               }
            }
         }

         return -1;
      }
   }

   private void abort(boolean value) {
      boolean visible = this.phase != GambleRiggerModule.Phase.IDLE;
      this.resetState();
      if (value && visible) {
         this.feedback("§cGambleRigger §7» aborted (the container closed or changed)");
      }
   }

   private void resetState() {
      this.phase = GambleRiggerModule.Phase.IDLE;
      this.keepSlot = -1;
      this.containerId = -1;
      this.queue.clear();
      this.delay = 0;

      for (int slot = 0; slot < 9; slot++) {
         this.snapshot[slot] = null;
         this.parked[slot] = -1;
      }
   }

   private void feedback(String str) {
      if (this.chatFeedback.get()) {
         ClientPlayerEntity player = this.mc.player;
         if (player != null) {
            player.sendMessage(Text.literal(str), false);
         }
      }
   }

   public static enum Phase {
      IDLE,
      EXTRACTING,
      EXTRACTED,
      RESTORING;

      private static GambleRiggerModule.Phase[] $values() {
         return new GambleRiggerModule.Phase[]{IDLE, EXTRACTING, EXTRACTED, RESTORING};
      }
   }
}
