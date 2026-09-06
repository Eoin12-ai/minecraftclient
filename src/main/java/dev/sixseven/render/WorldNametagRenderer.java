package dev.sixseven.render;

import dev.sixseven.SixSevenClient;
import dev.sixseven.module.ModuleManager;
import dev.sixseven.module.misc.NameProtectModule;
import dev.sixseven.module.misc.NameTagsModule;
import dev.sixseven.module.render.SpawnerNametagsModule;
import dev.sixseven.render.nanovg.NVGRenderer;
import dev.sixseven.theme.Theme;
import dev.sixseven.util.Colors;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;

import dev.sixseven.module.client.HudModule;
public final class WorldNametagRenderer {
   private static final int SPAWNER_ACCENT = -22733;
   private static final int MAX_TAGS = 80;

   private WorldNametagRenderer() {
   }

   public static void render(NVGRenderer nVGRenderer) {
      if (WorldProjection.isValid()) {
         ModuleManager moduleManager = SixSevenClient.modules();
         if (moduleManager != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientWorld world = client.world;
            ClientPlayerEntity player = client.player;
            if (world != null && player != null) {
               NameTagsModule nameTagsModule = moduleManager.nameTags;
               SpawnerNametagsModule spawnerNametagsModule = moduleManager.spawnerNametags;
               boolean enabled = nameTagsModule != null && nameTagsModule.isEnabled();
               boolean enabled2 = spawnerNametagsModule != null && spawnerNametagsModule.isEnabled() && spawnerNametagsModule.nametag.get();
               if (enabled || enabled2) {
                  float f = WorldProjection.partialTick();
                  Theme theme = SixSevenClient.themes().current();
                  HudModule hudModule = moduleManager.hud;
                  int n = hudModule != null && !hudModule.themeSync.get() ? hudModule.listColor.get() : theme.accent();
                  ArrayList list = new ArrayList();
                  if (enabled) {
                     double d = nameTagsModule.range.get();
                     double coord = d * d;
                     float f8 = nameTagsModule.scale.getFloat();
                     float f9 = (float)(nameTagsModule.opacity.get() / 100.0);
                     if (nameTagsModule.players.get()) {
                        NameProtectModule nameProtectModule = moduleManager.nameProtect;
                        boolean enabled3 = nameProtectModule != null && nameProtectModule.isEnabled();
                        boolean ok = nameTagsModule.self.get() && !client.options.getPerspective().isFirstPerson();

                        for (AbstractClientPlayerEntity abstractClientPlayerEntity : world.getPlayers()) {
                           boolean ok2 = abstractClientPlayerEntity == player;
                           if ((!ok2 || ok) && !abstractClientPlayerEntity.isSpectator() && abstractClientPlayerEntity.isAlive()) {
                              double currentScore = player.squaredDistanceTo(abstractClientPlayerEntity);
                              if (ok2 || !(currentScore > coord)) {
                                 String name2 = abstractClientPlayerEntity.getGameProfile().name();
                                 if (enabled3) {
                                    String text6 = nameProtectModule.replacementForDisplay(name2);
                                    if (text6 != null) {
                                       name2 = text6;
                                    }
                                 }

                                 float f10 = -1.0F;
                                 if (nameTagsModule.health.get()) {
                                    float f11 = abstractClientPlayerEntity.getMaxHealth();
                                    if (f11 > 0.0F) {
                                       f10 = MathHelper.clamp(abstractClientPlayerEntity.getHealth() / f11, 0.0F, 1.0F);
                                    }
                                 }

                                 String text2 = !ok2 && nameTagsModule.distance.get() ? "2" + (int)Math.sqrt(currentScore) : null;
                                 if (!name2.isEmpty() || text2 != null || !(f10 < 0.0F)) {
                                    list.add(entityTag(abstractClientPlayerEntity, f, Math.sqrt(currentScore), name2, text2, n, f10, f8, f9));
                                 }
                              }
                           }
                        }
                     }

                     if (nameTagsModule.items.get()) {
                        for (Entity entity : world.getEntities()) {
                           if (entity instanceof ItemEntity) {
                              ItemEntity itemEntity = (ItemEntity)entity;
                              if (itemEntity.isAlive()) {
                                 double coord3 = player.squaredDistanceTo(itemEntity);
                                 if (!(coord3 > coord)) {
                                    ItemStack stack = itemEntity.getStack();
                                    if (!stack.isEmpty()) {
                                       String text3 = itemSuffix(
                                          stack.getCount(), nameTagsModule.itemAmount.get(), nameTagsModule.distance.get() ? "2" + (int)Math.sqrt(coord3) : null
                                       );
                                       list.add(entityTag(itemEntity, f, Math.sqrt(coord3), stack.getName().getString(), text3, n, -1.0F, f8, f9));
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (enabled2) {
                     boolean ok3 = spawnerNametagsModule.distance.get();
                     float f12 = (float)(spawnerNametagsModule.opacity.get() / 100.0);

                     for (BlockPos pos : spawnerNametagsModule.scan.get()) {
                        double coord4 = (double)pos.getX() + 0.5;
                        double coord5 = (double)pos.getY() + 1.35;
                        double coord6 = (double)pos.getZ() + 0.5;
                        double coord7 = player.squaredDistanceTo(coord4, (double)pos.getY() + 0.5, coord6);
                        String text4 = ok3 ? "2" + (int)Math.sqrt(coord7) : null;
                        list.add(
                           new WorldNametagRenderer.Tag(Math.sqrt(coord7), coord4, coord5, coord6, spawnerName(world, pos), text4, -22733, -1.0F, 1.0F, f12)
                        );
                     }
                  }

                  if (!list.isEmpty()) {
                     list.sort(Comparator.comparingDouble(WorldNametagRenderer.Tag::dist));
                     int localX = Math.min(list.size(), 80);

                     for (int localZ = localX - 1; localZ >= 0; localZ--) {
                        WorldNametagRenderer.Tag tag = (WorldNametagRenderer.Tag)list.get(localZ);
                        float[] f13 = WorldProjection.project(tag.wx, tag.wy, tag.wz);
                        if (f13 != null) {
                           drawTag(nVGRenderer, theme, f13[0], f13[1], tag);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static WorldNametagRenderer.Tag entityTag(
      Entity entity, float f, double d, String name2, String str3, int n, float f5, float f6, float f7
   ) {
      double coord = MathHelper.lerp((double)f, entity.lastRenderX, entity.getX());
      double currentScore = MathHelper.lerp((double)f, entity.lastRenderY, entity.getY()) + (double)entity.getHeight() + 0.5;
      double coord3 = MathHelper.lerp((double)f, entity.lastRenderZ, entity.getZ());
      return new WorldNametagRenderer.Tag(d, coord, currentScore, coord3, name2, str3, n, f5, f6, f7);
   }

   private static String itemSuffix(int n, boolean value, String name2) {
      StringBuilder sb = new StringBuilder();
      if (value && n > 1) {
         sb.append('x').append(n);
      }

      if (name2 != null) {
         if (sb.length() > 0) {
            sb.append("  ");
         }

         sb.append(name2);
      }

      return sb.length() == 0 ? null : sb.toString();
   }

   private static String spawnerName(ClientWorld world, BlockPos pos) {
      try {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof MobSpawnerBlockEntity mobSpawnerBlockEntity) {
            Entity entity = mobSpawnerBlockEntity.getLogic().getRenderedEntity(world, pos);
            return entity != null ? entity.getType().getName().getString() + " Spawner" : "Spawner";
         }

         if (blockEntity instanceof TrialSpawnerBlockEntity trialSpawnerBlockEntity) {
            Entity entity3 = trialSpawnerBlockEntity.getSpawner().getData().setDisplayEntity(trialSpawnerBlockEntity.getSpawner(), world, trialSpawnerBlockEntity.getSpawnerState());
            return entity3 != null ? "Trial: " + entity3.getType().getName().getString() : "Trial Spawner";
         }
      } catch (Exception ex) {
      }

      return "Spawner";
   }

   private static float pillHeight(float f, boolean value) {
      float f3 = 12.5F * f + 3.5F * f * 2.0F;
      return f3 + (value ? 3.0F * f + 3.5F * f : 0.0F);
   }

   public static void renderEquipment(DrawContext context) {
      if (WorldProjection.isValid()) {
         ModuleManager moduleManager = SixSevenClient.modules();
         if (moduleManager != null) {
            NameTagsModule nameTagsModule = moduleManager.nameTags;
            if (nameTagsModule != null && nameTagsModule.isEnabled() && nameTagsModule.players.get()) {
               boolean enabled = nameTagsModule.armor.get();
               boolean enabled2 = nameTagsModule.heldItem.get();
               if (enabled || enabled2) {
                  MinecraftClient client = MinecraftClient.getInstance();
                  if (client.currentScreen == null && !client.options.hudHidden) {
                     ClientWorld world = client.world;
                     ClientPlayerEntity player = client.player;
                     if (world != null && player != null) {
                        float f = WorldProjection.partialTick();
                        double d = nameTagsModule.range.get();
                        double coord = d * d;
                        float f13 = nameTagsModule.scale.getFloat();
                        float f14 = pillHeight(f13, nameTagsModule.health.get());
                        float f15 = OverlayRenderer.uiScale();
                        double currentScore = (double)client.getWindow().getScaleFactor();
                        boolean enabled3 = nameTagsModule.self.get() && !client.options.getPerspective().isFirstPerson();
                        ArrayList list = new ArrayList();

                        for (AbstractClientPlayerEntity abstractClientPlayerEntity : world.getPlayers()) {
                           boolean ok = abstractClientPlayerEntity == player;
                           if ((!ok || enabled3) && !abstractClientPlayerEntity.isSpectator() && abstractClientPlayerEntity.isAlive() && (ok || !(player.squaredDistanceTo(abstractClientPlayerEntity) > coord))) {
                              list.clear();
                              if (enabled) {
                                 addItem(list, abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.HEAD));
                                 addItem(list, abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.CHEST));
                                 addItem(list, abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.LEGS));
                                 addItem(list, abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.FEET));
                              }

                              if (enabled2) {
                                 addItem(list, abstractClientPlayerEntity.getMainHandStack());
                                 addItem(list, abstractClientPlayerEntity.getEquippedStack(EquipmentSlot.OFFHAND));
                              }

                              if (!list.isEmpty()) {
                                 double coord3 = MathHelper.lerp((double)f, abstractClientPlayerEntity.lastRenderX, abstractClientPlayerEntity.getX());
                                 double coord4 = MathHelper.lerp((double)f, abstractClientPlayerEntity.lastRenderY, abstractClientPlayerEntity.getY())
                                    + (double)abstractClientPlayerEntity.getHeight()
                                    + 0.5;
                                 double coord5 = MathHelper.lerp((double)f, abstractClientPlayerEntity.lastRenderZ, abstractClientPlayerEntity.getZ());
                                 float[] f16 = WorldProjection.projectRaw(coord3, coord4, coord5);
                                 if (f16 != null) {
                                    float f17 = (float)((double)f16[0] / currentScore);
                                    float f18 = (float)((double)(f16[1] - f14 * f15) / currentScore) - 3.0F;
                                    float f19 = 11.0F * f13;
                                    float f20 = f19 + 1.5F;
                                    float f21 = (float)list.size() * f19 + (float)(list.size() - 1) * 1.5F;
                                    float f22 = f17 - f21 / 2.0F;
                                    float f23 = f18 - f19;
                                    Matrix3x2fStack matrix3x2fStack = context.getMatrices();

                                    for (int n = 0; n < list.size(); n++) {
                                       ItemStack stack = (ItemStack)list.get(n);
                                       matrix3x2fStack.pushMatrix();
                                       matrix3x2fStack.translate(f22 + (float)n * f20, f23);
                                       matrix3x2fStack.scale(f19 / 16.0F, f19 / 16.0F);
                                       context.drawItem(abstractClientPlayerEntity, stack, 0, 0, 0);
                                       context.drawStackOverlay(client.textRenderer, stack, 0, 0);
                                       matrix3x2fStack.popMatrix();
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void addItem(List<ItemStack> list, ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         list.add(stack);
      }
   }

   private static void drawTag(NVGRenderer nVGRenderer, Theme theme, float f, float f23, WorldNametagRenderer.Tag tag) {
      float f24 = tag.scale;
      int n = Colors.lighten(tag.accent, 0.35F);
      float f25 = 12.5F * f24;
      float f26 = 10.0F * f24;
      float f27 = 6.0F * f24;
      float f28 = 3.5F * f24;
      float f29 = 5.0F * f24;
      boolean enabled = tag.healthFrac >= 0.0F;
      float f30 = 3.0F * f24;
      float f31 = nVGRenderer.textWidth(tag.name, f25);
      float f32 = tag.suffix != null ? f29 + nVGRenderer.textWidth(tag.suffix, f26) : 0.0F;
      float f33 = f31 + f32 + f27 * 2.0F;
      float f34 = f25 + f28 * 2.0F;
      float f35 = f34 + (enabled ? f30 + f28 : 0.0F);
      float f36 = f - f33 / 2.0F;
      float f37 = f23 - f35;
      float f38 = Math.min(6.0F * f24, f35 / 2.0F);
      boolean enabled2 = tag.opacity < 0.999F;
      if (enabled2) {
         nVGRenderer.save();
         nVGRenderer.alpha(tag.opacity);
      }

      nVGRenderer.glow(f36, f37, f33, f35, f38, 4.0F, Colors.withAlpha(tag.accent, 0.12F));
      nVGRenderer.rectGradient(f36, f37, f33, f35, f38, theme.background(), theme.backgroundTo(), true);
      float f39 = f37 + f28 + f25 / 2.0F;
      float f40 = f36 + f27;
      f40 += nVGRenderer.textGradient(tag.name, f40, f39, f25, n, tag.accent);
      if (tag.suffix != null) {
         f40 += f29;
         nVGRenderer.text(tag.suffix, f40, f39, f26, theme.textMuted());
      }

      if (enabled) {
         float f41 = f37 + f34;
         float f42 = f36 + f27;
         float f43 = f33 - f27 * 2.0F;
         nVGRenderer.rect(f42, f41, f43, f30, f30 / 2.0F, Colors.withAlpha(-16777216, 0.55F));
         int offset = Colors.lerp(-2080450, -11671924, tag.healthFrac);
         nVGRenderer.rect(f42, f41, Math.max(f30, f43 * tag.healthFrac), f30, f30 / 2.0F, offset);
      }

      if (enabled2) {
         nVGRenderer.restore();
      }
   }

   private static record Tag(double dist, double wx, double wy, double wz, String name, String suffix, int accent, float healthFrac, float scale, float opacity) {
   }
}
