package dev.kryptic.module.misc;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.KeybindSetting;
import dev.kryptic.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;

public class CoordSnapperModule extends Module {
   public final ModeSetting format = this.addSetting(new ModeSetting("Format", "Clipboard format.", "X Y Z", "X Y Z", "JSON", "Command"));
   public final ModeSetting target = this.addSetting(new ModeSetting("Target", "Which coordinates to copy.", "Looked-at Block", "Looked-at Block", "Player"));
   public final KeybindSetting copyKey = this.addSetting(new KeybindSetting("Copy Key", "Press to copy the coordinates.", -1));
   public final BooleanSetting notify = this.addSetting(new BooleanSetting("Notify", "Show a confirmation when copied.", true));
   private String lastCopied = "";

   public CoordSnapperModule() {
      super("Coord Snapper", "Copies looked-at coordinates with one key.", Category.MISC);
   }

   public String lastCopied() {
      return this.lastCopied;
   }

   @Override
   public boolean onKeyPress(int n) {
      if (!this.copyKey.matches(n)) {
         return false;
      } else {
         this.snap();
         return true;
      }
   }

   private void snap() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         BlockPos pos = this.resolvePos(client);
         if (pos == null) {
            if (this.notify.get()) {
               KrypticClient.notifications().pushInfo("CoordSnapper · no block in view");
            }
         } else {
            String text = this.format.get();

            String json = switch (text) {
               case "JSON" -> "{\"x\": " + pos.getX() + ", \"y\": " + pos.getY() + ", \"z\": " + pos.getZ() + "}";
               case "Command" -> "/tp " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
               default -> pos.getX() + " " + pos.getY() + " " + pos.getZ();
            };
            client.keyboard.setClipboard(json);
            this.lastCopied = json;
            if (this.notify.get()) {
               KrypticClient.notifications().pushInfo("Copied · " + json);
            }
         }
      }
   }

   private BlockPos resolvePos(MinecraftClient client) {
      if (this.target.is("Player")) {
         return client.player.getBlockPos();
      } else {
         HitResult hit = client.crosshairTarget;
         if (hit instanceof BlockHitResult blockHit && hit.getType() == Type.BLOCK) {
            return blockHit.getBlockPos();
         }

         return null;
      }
   }
}
