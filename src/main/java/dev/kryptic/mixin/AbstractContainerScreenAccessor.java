package dev.kryptic.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({HandledScreen.class})
public interface AbstractContainerScreenAccessor {
   @Accessor("focusedSlot")
   Slot getHoveredSlot();

   @Accessor("x")
   int getLeftPos();

   @Accessor("y")
   int getTopPos();

   @Accessor("backgroundWidth")
   int getImageWidth();

   @Accessor("backgroundHeight")
   int getImageHeight();
}
