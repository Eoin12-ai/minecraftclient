package dev.sixseven.gui.config;

import dev.sixseven.SixSevenClient;
import dev.sixseven.config.ConfigStore;
import dev.sixseven.render.anim.Animation;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.theme.Theme;
import dev.sixseven.util.Colors;
import dev.sixseven.util.UiSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;

public class ConfigPanel {
   private static final float CARD_W = 600.0F;
   private static final float HEADER_H = 64.0F;
   private static final float FOOTER_H = 38.0F;
   private static final float ROW_H = 60.0F;
   private static final float ROW_GAP = 8.0F;
   private static final float PAD = 18.0F;
   private static final float BTN_INSET = 16.0F;
   private static final int DANGER = -45730;
   private static final int DANGER_BRIGHT = -37252;
   private final Animation openAnim = new Animation(160.0F, 0.0F);
   private boolean open;
   private float cardX;
   private float cardY;
   private float cardH;
   private final List<ConfigPanel.Hit> hits = new ArrayList<>();
   private float closeX;
   private float closeY;
   private float closeSize;
   private int renamingSlot = -1;
   private final StringBuilder renameBuffer = new StringBuilder();
   private ConfigPanel.Confirm pendingConfirm;
   private float confirmOkX;
   private float confirmOkY;
   private float confirmOkW;
   private float confirmOkH;
   private float confirmCancelX;
   private float confirmCancelY;
   private float confirmCancelW;
   private float confirmCancelH;
   private float confirmCardX;
   private float confirmCardY;
   private float confirmCardW;
   private float confirmCardH;

   public boolean isOpen() {
      return this.open;
   }

   public void open() {
      this.open = true;
      this.openAnim.setTarget(1.0F);
   }

   public void close() {
      this.open = false;
      this.openAnim.setTarget(0.0F);
      this.cancelRename();
      this.pendingConfirm = null;
   }

   public boolean isListening() {
      return this.open && this.renamingSlot >= 0;
   }

   private ConfigStore store() {
      return SixSevenClient.configStore();
   }

   public void render(NVGRenderer nVGRenderer, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      float f = this.openAnim.value();
      if (!(f <= 0.002F) || this.open) {
         Theme theme = SixSevenClient.themes().current();
         nVGRenderer.rect(0.0F, 0.0F, tickDelta3, tickDelta4, 0.0F, Colors.withAlpha(-16316918, 0.55F * f));
         nVGRenderer.save();
         nVGRenderer.alpha(f);
         float f4 = 0.97F + 0.03F * f;
         nVGRenderer.translate(tickDelta3 / 2.0F, tickDelta4 / 2.0F);
         nVGRenderer.scale(f4);
         nVGRenderer.translate(-tickDelta3 / 2.0F, -tickDelta4 / 2.0F);
         ConfigStore configStore = this.store();
         this.cardH = 434.0F;
         this.cardX = (tickDelta3 - 600.0F) / 2.0F;
         this.cardY = (tickDelta4 - this.cardH) / 2.0F;
         nVGRenderer.glow(this.cardX, this.cardY, 600.0F, this.cardH, 18.0F, 22.0F, Colors.withAlpha(-16777216, 0.45F));
         nVGRenderer.rectVaryingGradient(this.cardX, this.cardY, 600.0F, this.cardH, 18.0F, 18.0F, 18.0F, 18.0F, theme.background(), theme.backgroundTo());
         nVGRenderer.rectOutline(this.cardX, this.cardY, 600.0F, this.cardH, 18.0F, 1.2F, Colors.withAlpha(theme.accent(), 0.3F));
         this.renderHeader(nVGRenderer, theme, tickDelta, tickDelta2);
         this.hits.clear();
         float f5 = this.cardY + 64.0F;

         for (int n = 0; n < 5; n++) {
            this.renderSlot(nVGRenderer, theme, configStore.slot(n), this.cardX + 18.0F, totalW, 564.0F, tickDelta, tickDelta2, configStore.activeIndex() == n);
            totalW += 68.0F;
         }

         this.renderFooter(nVGRenderer, theme);
         if (this.pendingConfirm != null) {
            this.renderConfirm(nVGRenderer, theme, tickDelta, tickDelta2, tickDelta3, tickDelta4);
         }

         nVGRenderer.restore();
      }
   }

   private void renderHeader(NVGRenderer nVGRenderer, Theme theme, float tickDelta, float tickDelta2) {
      float f = this.cardX + 18.0F;
      float f3 = this.cardY + 22.0F;
      nVGRenderer.rect(f + 4.0F, f3 + 3.0F, 15.0F, 12.0F, 3.0F, Colors.withAlpha(theme.accent(), 0.45F));
      nVGRenderer.rect(f, f3, 15.0F, 12.0F, 3.0F, theme.accentBright());
      nVGRenderer.text("Configs", f + 28.0F, this.cardY + 26.0F, 19.0F, theme.textPrimary());
      nVGRenderer.text("Save, activate & share your full client setup", f + 28.0F, this.cardY + 44.0F, 12.0F, theme.textMuted());
      this.closeSize = 16.0F;
      this.closeX = this.cardX + 600.0F - 18.0F - this.closeSize;
      this.closeY = this.cardY + 20.0F;
      boolean ok = tickDelta >= this.closeX - 4.0F
         && tickDelta <= this.closeX + this.closeSize + 4.0F
         && tickDelta2 >= this.closeY - 4.0F
         && tickDelta2 <= this.closeY + this.closeSize + 4.0F;
      if (ok) {
         nVGRenderer.rect(this.closeX - 5.0F, this.closeY - 5.0F, this.closeSize + 10.0F, this.closeSize + 10.0F, 6.0F, Colors.withAlpha(theme.accent(), 0.18F));
      }

      nVGRenderer.cross(this.closeX, this.closeY, this.closeSize, 1.8F, ok ? theme.accentBright() : theme.textMuted());
      nVGRenderer.rect(this.cardX + 18.0F, this.cardY + 64.0F - 2.0F, 564.0F, 1.0F, 0.5F, Colors.withAlpha(theme.accent(), 0.2F));
   }

   private void renderSlot(NVGRenderer nVGRenderer, Theme theme, ConfigStore.Slot slot, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4, float tickDelta5, boolean value) {
      boolean ok = tickDelta4 >= tickDelta && tickDelta4 <= tickDelta + tickDelta3 && tickDelta5 >= tickDelta2 && tickDelta5 <= tickDelta2 + 60.0F;
      int n = value ? theme.moduleActiveFill() : Colors.withAlpha(-16777216, ok ? 0.32F : 0.22F);
      nVGRenderer.rect(tickDelta, tickDelta2, tickDelta3, 60.0F, 12.0F, n);
      if (value) {
         nVGRenderer.rect(tickDelta, tickDelta2 + 10.0F, 3.0F, 40.0F, 1.5F, theme.accentBright());
         nVGRenderer.rectOutline(tickDelta, tickDelta2, tickDelta3, 60.0F, 12.0F, 1.1F, Colors.withAlpha(theme.accent(), 0.5F));
      }

      float f = tickDelta + 16.0F;
      float f5 = tickDelta2 + 30.0F;
      if (value) {
         nVGRenderer.circleGlow(f, totalW, 4.0F, 5.0F, theme.accent());
         nVGRenderer.circle(f, totalW, 4.0F, theme.accentBright());
      } else if (slot.filled()) {
         nVGRenderer.circle(f, totalW, 3.5F, theme.statusEnabled());
      } else {
         nVGRenderer.circleOutline(f, totalW, 3.5F, 1.2F, theme.statusDisabled());
      }

      float f6 = tickDelta + 32.0F;
      if (this.renamingSlot == slot.index()) {
         this.renderRenameField(nVGRenderer, theme, f6, tickDelta2 + 12.0F, 190.0F);
      } else {
         nVGRenderer.textTruncated(slot.name(), f6, tickDelta2 + 22.0F, 15.0F, theme.textPrimary(), 200.0F);
         if (value) {
            float f7 = nVGRenderer.textWidth(slot.name(), 15.0F);
            this.drawTag(nVGRenderer, theme, f6 + Math.min(f7, 200.0F) + 8.0F, tickDelta2 + 22.0F, "ACTIVE");
         }
      }

      String text2 = this.renamingSlot == slot.index()
         ? "Enter to confirm · Esc to cancel"
         : (slot.filled() ? "Saved · " + relativeTime(slot.savedAt()) : "Empty slot");
      nVGRenderer.text(text2, f6, tickDelta2 + 40.0F, 11.5F, theme.textMuted());
      this.layoutButtons(nVGRenderer, theme, slot, tickDelta + tickDelta3 - 16.0F, tickDelta2, tickDelta4, tickDelta5);
   }

   private void renderRenameField(NVGRenderer nVGRenderer, Theme theme, float tickDelta, float tickDelta2, float tickDelta3) {
      float f = 20.0F;
      nVGRenderer.rect(tickDelta, tickDelta2, tickDelta3, f, f / 2.0F, Colors.withAlpha(-16777216, 0.5F));
      nVGRenderer.rectOutline(tickDelta, tickDelta2, tickDelta3, f, f / 2.0F, 1.2F, Colors.withAlpha(theme.accentBright(), 0.9F));
      float f5 = tickDelta + 8.0F;
      float f6 = tickDelta2 + f / 2.0F;
      float f7 = nVGRenderer.text(this.renameBuffer.toString(), totalW, f6, 12.5F, theme.textPrimary());
      if (System.nanoTime() / 400000000L % 2L == 0L) {
         nVGRenderer.rect(f5 + f7 + 1.5F, f6 - 6.0F, 1.4F, 12.0F, 0.7F, theme.accentBright());
      }
   }

   private void drawTag(NVGRenderer nVGRenderer, Theme theme, float f, float f4, String text2) {
      float f5 = nVGRenderer.textWidth(text2, 9.5F);
      nVGRenderer.rect(f, f4 - 7.0F, f5 + 12.0F, 14.0F, 7.0F, Colors.withAlpha(theme.accent(), 0.22F));
      nVGRenderer.text(text2, f + 6.0F, f4, 9.5F, theme.accentBright());
   }

   private void layoutButtons(NVGRenderer nVGRenderer, Theme theme, ConfigStore.Slot slot, float f3, float f2, float f, float f4) {
      ArrayList list = new ArrayList();

      record Spec(ConfigPanel.Action action, String label, boolean primary, boolean danger) {
      }

      if (slot.filled()) {
         list.add(new Spec(ConfigPanel.Action.ACTIVATE, "Activate", true, false));
         list.add(new Spec(ConfigPanel.Action.SAVE, "Save", false, false));
         list.add(new Spec(ConfigPanel.Action.RENAME, "Rename", false, false));
         list.add(new Spec(ConfigPanel.Action.EXPORT, "Export", false, false));
         list.add(new Spec(ConfigPanel.Action.IMPORT, "Import", false, false));
         list.add(new Spec(ConfigPanel.Action.DELETE, "Delete", false, true));
      } else {
         list.add(new Spec(ConfigPanel.Action.SAVE, "Save", true, false));
         list.add(new Spec(ConfigPanel.Action.IMPORT, "Import", false, false));
      }

      float f = 26.0F;
      float f2 = 11.0F;
      float f3 = 6.0F;
      float f4 = 12.0F;
      float f5 = 0.0F;
      float[] f6 = new float[list.size()];

      for (int n = 0; n < list.size(); n++) {
         widths[n] = temp.textWidth(((Spec)list.get(n)).label(), f4) + f2 * 2.0F;
         totalW += widths[n] + (n > 0 ? f3 : 0.0F);
      }

      float f = tmp12 - temp3;
      float f2 = tmp9 + (60.0F - tmp13) / 2.0F;

      for (int n = 0; n < tmp5.size(); n++) {
         Spec spec = (Spec)tmp5.get(n);
         float f3 = temp[n];
         boolean ok = tmp8 >= f && tmp8 <= f + f3 && tmp10 >= f2 && tmp10 <= f2 + tmp13;
         this.drawButton(tmp11, tmp6, f, f2, f3, tmp13, spec.label(), temp2, spec.primary(), ok, spec.danger());
         this.hits.add(new ConfigPanel.Hit(spec.action(), tmp7.index(), f, f2, f3, tmp13, spec.primary()));
         f += f3 + temp4;
      }
   }

   private void drawButton(
      NVGRenderer nVGRenderer, Theme theme, float f, float f6, float f7, float f8, String text2, float f9, boolean value, boolean value2, boolean value3
   ) {
      int n = value3 ? -45730 : theme.accent();
      int localZ = value3 ? -37252 : theme.accentBright();
      if (value) {
         int localY = value2 ? Colors.lighten(localZ, 0.1F) : localZ;
         nVGRenderer.rectGradient(f, f6, f7, f8, f8 / 2.0F, localY, n, true);
         if (value2) {
            nVGRenderer.glow(f, f6, f7, f8, f8 / 2.0F, 5.0F, Colors.withAlpha(n, 0.35F));
         }

         nVGRenderer.text(text2, f + (f7 - nVGRenderer.textWidth(text2, f9)) / 2.0F, f6 + f8 / 2.0F, f9, -15593706);
      } else {
         nVGRenderer.rect(f, f6, f7, f8, f8 / 2.0F, Colors.withAlpha(-1, value2 ? 0.12F : 0.06F));
         nVGRenderer.rectOutline(f, f6, f7, f8, f8 / 2.0F, 1.0F, Colors.withAlpha(value2 ? localZ : n, value2 ? 0.7F : 0.28F));
         int step = value3 ? Colors.withAlpha(n, 0.85F) : theme.textMuted();
         nVGRenderer.text(text2, f + (f7 - nVGRenderer.textWidth(text2, f9)) / 2.0F, f6 + f8 / 2.0F, f9, value2 ? (value3 ? localZ : theme.textPrimary()) : step);
      }
   }

   private void renderFooter(NVGRenderer nVGRenderer, Theme theme) {
      float f = this.cardY + this.cardH - 19.0F;
      nVGRenderer.text("Export copies to your clipboard & a file · Import pastes it", this.cardX + 18.0F, f, 11.0F, theme.textMuted());
      String text2 = "v1";
      nVGRenderer.text(text2, this.cardX + 600.0F - 18.0F - nVGRenderer.textWidth(text2, 11.0F), f, 11.0F, theme.textDisabled());
   }

   private void renderConfirm(NVGRenderer nVGRenderer, Theme theme, float tickDelta, float tickDelta2, float tickDelta3, float tickDelta4) {
      nVGRenderer.rect(this.cardX, this.cardY, 600.0F, this.cardH, 18.0F, Colors.withAlpha(-16316918, 0.55F));
      this.confirmCardW = 380.0F;
      this.confirmCardH = 148.0F;
      this.confirmCardX = (tickDelta3 - this.confirmCardW) / 2.0F;
      this.confirmCardY = (tickDelta4 - this.confirmCardH) / 2.0F;
      boolean ok = this.pendingConfirm.action() == ConfigPanel.Action.DELETE;
      nVGRenderer.glow(this.confirmCardX, this.confirmCardY, this.confirmCardW, this.confirmCardH, 16.0F, 20.0F, Colors.withAlpha(-16777216, 0.5F));
      nVGRenderer.rectVaryingGradient(
         this.confirmCardX, this.confirmCardY, this.confirmCardW, this.confirmCardH, 16.0F, 16.0F, 16.0F, 16.0F, theme.headerTop(), theme.background()
      );
      nVGRenderer.rectOutline(
         this.confirmCardX, this.confirmCardY, this.confirmCardW, this.confirmCardH, 16.0F, 1.2F, Colors.withAlpha(ok ? -45730 : theme.accent(), 0.45F)
      );
      nVGRenderer.text(this.pendingConfirm.title(), this.confirmCardX + 22.0F, this.confirmCardY + 34.0F, 16.0F, theme.textPrimary());
      nVGRenderer.text(this.pendingConfirm.body(), this.confirmCardX + 22.0F, this.confirmCardY + 58.0F, 12.0F, theme.textMuted());
      float f = 30.0F;
      float f4 = 10.0F;
      float f5 = this.confirmCardY + this.confirmCardH - f - 20.0F;
      this.confirmOkW = 118.0F;
      this.confirmCancelW = 92.0F;
      this.confirmOkH = f;
      this.confirmCancelH = f;
      this.confirmOkX = this.confirmCardX + this.confirmCardW - 22.0F - this.confirmOkW;
      this.confirmOkY = f5;
      this.confirmCancelX = this.confirmOkX - f4 - this.confirmCancelW;
      this.confirmCancelY = f5;
      boolean ok2 = tickDelta >= this.confirmOkX && tickDelta <= this.confirmOkX + this.confirmOkW && tickDelta2 >= f5 && tickDelta2 <= f5 + f;
      boolean ok3 = tickDelta >= this.confirmCancelX && tickDelta <= this.confirmCancelX + this.confirmCancelW && tickDelta2 >= f5 && tickDelta2 <= f5 + f;
      this.drawButton(nVGRenderer, theme, this.confirmCancelX, this.confirmCancelY, this.confirmCancelW, this.confirmCancelH, "Cancel", 12.5F, false, ok3, false);
      this.drawButton(nVGRenderer, theme, this.confirmOkX, this.confirmOkY, this.confirmOkW, this.confirmOkH, this.confirmLabel(), 12.5F, true, ok2, ok);
   }

   private String confirmLabel() {
      return switch (this.pendingConfirm.action()) {
         case IMPORT -> "Import";
         case DELETE -> "Delete";
         default -> "Overwrite";
      };
   }

   public boolean mouseClicked(float f, float f3, int n) {
      if (!this.open) {
         return false;
      } else {
         if (this.renamingSlot >= 0) {
            this.commitRename();
         }

         if (this.pendingConfirm != null) {
            if (this.hit(f, f3, this.confirmOkX, this.confirmOkY, this.confirmOkW, this.confirmOkH)) {
               this.runConfirm();
            } else if (this.hit(f, f3, this.confirmCancelX, this.confirmCancelY, this.confirmCancelW, this.confirmCancelH)
               || !this.hit(f, f3, this.confirmCardX, this.confirmCardY, this.confirmCardW, this.confirmCardH)) {
               this.pendingConfirm = null;
               UiSounds.select();
            }

            return true;
         } else if (f >= this.closeX - 5.0F
            && f <= this.closeX + this.closeSize + 5.0F
            && f3 >= this.closeY - 5.0F
            && f3 <= this.closeY + this.closeSize + 5.0F) {
            this.close();
            UiSounds.guiClose();
            return true;
         } else {
            for (ConfigPanel.Hit hit2 : this.hits) {
               if (hit2.contains(f, f3)) {
                  this.dispatch(hit2.action, hit2.slot);
                  return true;
               }
            }

            if (f < this.cardX || f > this.cardX + 600.0F || f3 < this.cardY || f3 > this.cardY + this.cardH) {
               this.close();
               UiSounds.guiClose();
            }

            return true;
         }
      }
   }

   public boolean keyPressed(int n) {
      if (!this.open) {
         return false;
      } else if (this.renamingSlot >= 0) {
         switch (n) {
            case 256:
               this.cancelRename();
               break;
            case 257:
            case 335:
               this.commitRename();
               break;
            case 259:
               if (this.renameBuffer.length() > 0) {
                  this.renameBuffer.deleteCharAt(this.renameBuffer.length() - 1);
               }
         }

         return true;
      } else if (this.pendingConfirm != null) {
         switch (n) {
            case 256:
               this.pendingConfirm = null;
               UiSounds.select();
               break;
            case 257:
            case 335:
               this.runConfirm();
         }

         return true;
      } else if (n == 256) {
         this.close();
         UiSounds.guiClose();
         return true;
      } else {
         return true;
      }
   }

   public boolean charTyped(int n) {
      if (this.open && this.renamingSlot >= 0) {
         if (this.renameBuffer.length() >= 24) {
            return true;
         } else {
            char ch = (char)n;
            if (ch >= ' ' && ch < 127) {
               this.renameBuffer.append(ch);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private void dispatch(ConfigPanel.Action action, int n) {
      ConfigStore configStore = this.store();
      switch (action) {
         case ACTIVATE:
            this.doActivate(n);
            break;
         case SAVE:
            if (configStore.slot(n).filled()) {
               this.pendingConfirm = new ConfigPanel.Confirm(
                  ConfigPanel.Action.SAVE,
                  n,
                  "Overwrite \"" + configStore.slot(n).name() + "\"?",
                  "This replaces the config saved in slot " + (n + 1) + ".",
                  (String)null
               );
               UiSounds.select();
            } else {
               this.doSave(n);
            }
            break;
         case RENAME:
            this.beginRename(n);
            break;
         case EXPORT:
            this.doExport(n);
            break;
         case IMPORT:
            String text2 = this.readClipboard();
            if (text2 == null || text2.isBlank()) {
               this.toast("Nothing on the clipboard to import");
               UiSounds.select();
               return;
            }

            if (configStore.slot(n).filled()) {
               this.pendingConfirm = new ConfigPanel.Confirm(
                  ConfigPanel.Action.IMPORT,
                  n,
                  "Import over \"" + configStore.slot(n).name() + "\"?",
                  "This replaces slot " + (n + 1) + " with the pasted config.",
                  text2
               );
               UiSounds.select();
            } else {
               this.doImport(n, text2);
            }
            break;
         case DELETE:
            this.pendingConfirm = new ConfigPanel.Confirm(
               ConfigPanel.Action.DELETE, n, "Delete \"" + configStore.slot(n).name() + "\"?", "This permanently removes slot " + (n + 1) + ".", (String)null
            );
            UiSounds.select();
      }
   }

   private void runConfirm() {
      ConfigPanel.Confirm confirm = this.pendingConfirm;
      this.pendingConfirm = null;
      if (confirm != null) {
         if (confirm.action() == ConfigPanel.Action.SAVE) {
            this.doSave(confirm.slot());
         } else if (confirm.action() == ConfigPanel.Action.IMPORT) {
            this.doImport(confirm.slot(), confirm.payload());
         } else if (confirm.action() == ConfigPanel.Action.DELETE) {
            this.doDelete(confirm.slot());
         }
      }
   }

   private void doDelete(int n) {
      String text2 = this.store().slot(n).name();
      if (this.store().delete(n)) {
         this.toast("Deleted \"" + text2 + "\"");
         UiSounds.select();
      } else {
         this.toast("Couldn't delete the config");
      }
   }

   private void doSave(int n) {
      if (this.store().save(n)) {
         String text2 = this.store().slot(n).name();
         this.toast("Saved to \"" + text2 + "\"");
         UiSounds.toggle(true);
      } else {
         this.toast("Couldn't save the config");
      }
   }

   private void doActivate(int n) {
      if (this.store().activate(n)) {
         String text2 = this.store().slot(n).name();
         this.toast("Activated \"" + text2 + "\"");
         UiSounds.toggle(true);
      } else {
         this.toast("That slot is empty");
      }
   }

   private void doExport(int n) {
      String text2 = this.store().export(n);
      if (text2 == null) {
         this.toast("That slot is empty");
      } else {
         this.setClipboard(text2);
         String name2 = this.store().slot(n).name();
         this.toast("Copied \"" + name2 + "\" to clipboard");
         UiSounds.select();
      }
   }

   private void doImport(int n, String text2) {
      ConfigStore.ImportResult importResult = this.store().importInto(n, text2);
      this.toast(importResult.message());
      UiSounds.toggle(true);
   }

   private void beginRename(int n) {
      this.renamingSlot = n;
      this.renameBuffer.setLength(0);
      this.renameBuffer.append(this.store().slot(n).name());
      UiSounds.select();
   }

   private void commitRename() {
      if (this.renamingSlot >= 0) {
         this.store().rename(this.renamingSlot, this.renameBuffer.toString());
         this.renamingSlot = -1;
      }
   }

   private void cancelRename() {
      this.renamingSlot = -1;
   }

   private boolean hit(float f, float f7, float f8, float f9, float f10, float f11) {
      return f >= f8 && f <= f8 + f10 && f7 >= f9 && f7 <= f9 + f11;
   }

   private void toast(String text2) {
      if (SixSevenClient.notifications() != null) {
         SixSevenClient.notifications().pushInfo(text2);
      }
   }

   private String readClipboard() {
      try {
         return MinecraftClient.getInstance().keyboard.getClipboard();
      } catch (Exception ex) {
         return null;
      }
   }

   private void setClipboard(String text2) {
      try {
         MinecraftClient.getInstance().keyboard.setClipboard(text2);
      } catch (Exception ex) {
      }
   }

   private static String relativeTime(long l) {
      if (l <= 0L) {
         return "just now";
      } else {
         long l5 = System.currentTimeMillis() - l;
         if (l5 < 60000L) {
            return "just now";
         } else {
            long l6 = l5 / 60000L;
            if (l6 < 60L) {
               return l6 + "m ago";
            } else {
               long l7 = l6 / 60L;
               return l7 < 24L ? l7 + "h ago" : l7 / 24L + "d ago";
            }
         }
      }
   }

   public void debugBeginRename(int n) {
      this.open();
      this.beginRename(n);
   }

   public void debugConfirmOverwrite(int n) {
      this.open();
      this.pendingConfirm = new ConfigPanel.Confirm(
         ConfigPanel.Action.SAVE,
         n,
         "Overwrite \"" + this.store().slot(n).name() + "\"?",
         "This replaces the config saved in slot " + (n + 1) + ".",
         (String)null
      );
   }

   public void debugConfirmDelete(int n) {
      this.open();
      this.pendingConfirm = new ConfigPanel.Confirm(
         ConfigPanel.Action.DELETE,
         n,
         "Delete \"" + this.store().slot(n).name() + "\"?",
         "This permanently removes slot " + (n + 1) + ".",
         (String)null
      );
   }

   private static enum Action {
      ACTIVATE,
      SAVE,
      RENAME,
      EXPORT,
      IMPORT,
      DELETE;

      private static ConfigPanel.Action[] $values() {
         return new ConfigPanel.Action[]{ACTIVATE, SAVE, RENAME, EXPORT, IMPORT, DELETE};
      }
   }

   private static record Confirm(ConfigPanel.Action action, int slot, String title, String body, String payload) {
   }

   private static final class Hit {
      final ConfigPanel.Action action;
      final int slot;
      final float x;
      final float y;
      final float w;
      final float h;
      final boolean primary;

      Hit(ConfigPanel.Action action, int n, float f3, float f4, float f2, float f, boolean ok) {
         this.action = action;
         this.slot = n;
         this.x = f3;
         this.y = f4;
         this.w = f2;
         this.h = f;
         this.primary = ok;
      }

      boolean contains(float f, float f3) {
         return f >= this.x && f <= this.x + this.w && f3 >= this.y && f3 <= this.y + this.h;
      }
   }
}
