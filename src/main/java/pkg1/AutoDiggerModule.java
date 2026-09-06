package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import util.Utils_2;

public final class AutoDiggerModule extends Module {
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final Setting<Boolean> val2_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8()
            .valOf("switch-back")
            .valOf2("Returns to the previous hotbar slot when the attack key is released.")
            .valOf3(true)
            .getVal()
      );
   private final Setting<Integer> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("switch-delay")
            .valOf2("Delay in ticks before switching to the best tool.")
            .valOf3(0)
            .valOf4(0, 10)
            .valOf5(0, 10)
            .getVal()
      );
   private final Setting<Boolean> val4 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("anti-break").valOf2("Avoids tools that are close to breaking.").valOf3(false).getVal());
   private final Setting<Integer> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("anti-break-percentage")
            .valOf2("Remaining durability percentage at which a tool is no longer selected.")
            .valOf3(10)
            .valOf4(1, 100)
            .valOf5(1, 100)
            .valOf9(this::getBoolean)
            .getVal()
      );
   private BlockPos class2338;
   private int intVal = -1;
   private int intVal2;
   private boolean bool_2;
   private int intVal3 = -1;

   public AutoDiggerModule() {
      super(SwyzzyAddon.val2, "auto-digger", "Automatically switches to the best hotbar tool for the targeted block.");
      Utils_2.run(this, "Auto Tool");
   }

   @Override
   public void run6() {
      this.run13();
      this.bool_2 = false;
      this.intVal3 = -1;
   }

   @Override
   public void run7() {
      if (this.val2_2.getObject() && this.bool_2 && this.intVal3 != -1) {
         class310.player.getInventory().setSelectedSlot(this.intVal3);
      }

      this.run13();
      this.bool_2 = false;
      this.intVal3 = -1;
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null) {
         boolean var2 = class310.options.attackKey.isPressed();
         if (!var2) {
            this.run13();
            if (this.val2_2.getObject() && this.bool_2 && this.intVal3 != -1) {
               class310.player.getInventory().setSelectedSlot(this.intVal3);
               this.intVal3 = -1;
            }

            this.bool_2 = false;
         } else if (class310.crosshairTarget instanceof BlockHitResult var3) {
            BlockPos var8 = var3.getBlockPos();
            BlockState var5 = class310.world.getBlockState(var8);
            if (!var5.isAir()) {
               int var6 = this.intOf(var5);
               int var7 = class310.player.getInventory().getSelectedSlot();
               if (var6 != -1 && var6 != var7) {
                  this.class2338 = var8.toImmutable();
                  this.intVal = var6;
                  if (this.intVal2 <= 0 && this.intVal != -1) {
                     this.run12();
                  } else if (this.intVal2 > 0) {
                     this.intVal2--;
                  }
               } else if (var6 == var7) {
                  this.run13();
               }
            } else {
               this.run13();
            }
         } else {
            this.run13();
         }
      } else {
         this.run13();
         this.bool_2 = false;
         this.intVal3 = -1;
      }
   }

   private int intOf(BlockState var1) {
      int var2 = -1;
      double var3 = -1.0;

      for (int var5 = 0; var5 < 9; var5++) {
         ItemStack var6 = class310.player.getInventory().getStack(var5);
         if (!var6.isEmpty() && !this.check(var6)) {
            float var7 = var6.getMiningSpeedMultiplier(var1);
            boolean var8 = var6.isSuitableFor(var1);
            if (!(var7 <= 1.0F) || var8) {
               double var9 = var7 * 1000.0 + (var8 ? 100000.0 : 0.0);
               if (var9 > var3) {
                  var3 = var9;
                  var2 = var5;
               }
            }
         }
      }

      return var2;
   }

   private boolean check(ItemStack var1) {
      if (this.val4.getObject() && var1.isDamageable()) {
         int var2 = var1.getMaxDamage() - var1.getDamage();
         return var2 <= var1.getMaxDamage() * this.val5.getObject() / 100;
      } else {
         return false;
      }
   }

   private void run12() {
      if (this.intVal >= 0 && this.intVal <= 8) {
         if (this.val2_2.getObject() && this.intVal3 == -1) {
            this.intVal3 = class310.player.getInventory().getSelectedSlot();
         }

         class310.player.getInventory().setSelectedSlot(this.intVal);
         this.bool_2 = true;
         this.run13();
      }
   }

   private void run13() {
      this.class2338 = null;
      this.intVal = -1;
      this.intVal2 = this.val3_2.getObject();
   }

   private Boolean getBoolean() {
      return this.val4.getObject();
   }
}
