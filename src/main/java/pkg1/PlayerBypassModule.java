package pkg1;

import com.swyzzyaddon.SwyzzyAddon;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import util.Utils_2;

public final class PlayerBypassModule extends Module {
   private static final long longVal = 500L;
   private static final long longVal2 = 1000L;
   private static final int intVal = 8;
   private static final double doubleVal = 0.06;
   private static final Set<Block> set = Set.of(
      Blocks.SHULKER_BOX,
      Blocks.WHITE_SHULKER_BOX,
      Blocks.ORANGE_SHULKER_BOX,
      Blocks.MAGENTA_SHULKER_BOX,
      Blocks.LIGHT_BLUE_SHULKER_BOX,
      Blocks.YELLOW_SHULKER_BOX,
      Blocks.LIME_SHULKER_BOX,
      Blocks.PINK_SHULKER_BOX,
      Blocks.GRAY_SHULKER_BOX,
      Blocks.LIGHT_GRAY_SHULKER_BOX,
      Blocks.CYAN_SHULKER_BOX,
      Blocks.PURPLE_SHULKER_BOX,
      Blocks.BLUE_SHULKER_BOX,
      Blocks.BROWN_SHULKER_BOX,
      Blocks.GREEN_SHULKER_BOX,
      Blocks.RED_SHULKER_BOX,
      Blocks.BLACK_SHULKER_BOX,
      Blocks.ENDER_CHEST,
      Blocks.BEACON,
      Blocks.CONDUIT,
      Blocks.ENCHANTING_TABLE,
      Blocks.HOPPER
   );
   private static final Set<Block> set2 = Set.of(
      Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER, Blocks.BREWING_STAND, Blocks.CRAFTING_TABLE
   );
   private final ActivityChunkFinderModuleEntry val_2 = this.val2.getVal();
   private final ActivityChunkFinderModuleEntry val2_2 = this.val2.valOf("Notifications");
   private final Setting<ActivityChunkFinderModuleHelper4> val3_2 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("high-suspicion")
            .valOf2("Color for high suspicion chunks.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 0, 0, 180))
            .getVal()
      );
   private final Setting<ActivityChunkFinderModuleHelper4> val4 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper10()
            .valOf("scan-color")
            .valOf2("Color used to render active scan chunks.")
            .valOf3(new ActivityChunkFinderModuleHelper4(255, 255, 0, 255))
            .getVal()
      );
   private final Setting<Integer> val5 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("scan-transparency")
            .valOf2("Transparency of active scan chunk highlights.")
            .valOf3(140)
            .valOf4(0, 255)
            .valOf5(0, 255)
            .getVal()
      );
   private final Setting<Integer> val6 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("transparency")
            .valOf2("General transparency for all rendered highlights.")
            .valOf3(255)
            .valOf4(0, 255)
            .valOf5(0, 255)
            .getVal()
      );
   private final Setting<Integer> val7 = this.val_2
      .addSetting(new ActivityChunkFinderModuleHelper6().valOf("radius").valOf2("Scan radius in chunks.").valOf3(12).valOf4(4, 32).valOf5(4, 32).getVal());
   private final Setting<Integer> val8 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("sensitivity")
            .valOf2("Higher values make detection less aggressive (harder to flag). 0 also logs every scan to chat.")
            .valOf3(50)
            .valOf4(0, 100)
            .valOf5(0, 100)
            .getVal()
      );
   private final Setting<Integer> val9 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("inhabited-threshold")
            .valOf2("Higher values increase the inhabited-time threshold (harder to count as visited).")
            .valOf3(1200)
            .valOf4(0, 12000)
            .valOf5(0, 12000)
            .getVal()
      );
   private final Setting<Integer> val10 = this.val_2
      .addSetting(
         new ActivityChunkFinderModuleHelper6()
            .valOf("y-level")
            .valOf2("Y coordinate to render chunk flags at.")
            .valOf3(66)
            .valOf4(-64, 320)
            .valOf5(-64, 320)
            .getVal()
      );
   private final Setting<Boolean> val11 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("chat-notify").valOf2("Send a chat message when a new chunk gets flagged.").valOf3(true).getVal()
      );
   private final Setting<String> val12 = this.val2_2
      .addSetting(
         new AdminDetectorModuleHelper2()
            .valOf("chat-message")
            .valOf2("Chat text. Placeholders: {x} {z} {score} {distance} {inhabited}.")
            .valOf3("Base found at {x}, {z} - score {score}, {distance} blocks away")
            .valOf5(this.val11::getObject)
            .getVal()
      );
   private final Setting<Boolean> val13 = this.val2_2
      .addSetting(
         new ActivityChunkFinderModuleHelper8().valOf("toast-notify").valOf2("Show a toast popup when a new chunk gets flagged.").valOf3(true).getVal()
      );
   private final Setting<Boolean> val14 = this.val2_2
      .addSetting(new ActivityChunkFinderModuleHelper8().valOf("sound-notify").valOf2("Play a sound when a new chunk gets flagged.").valOf3(true).getVal());
   private final Setting<PlayerBypassModule.State> val15 = this.val2_2
      .addSetting(
         new BaseEspModuleHelper2<PlayerBypassModule.State>()
            .valOf("sound")
            .valOf2("Which sound the find alert plays.")
            .valOf3(PlayerBypassModule.State.PLING)
            .valOf5(this.val14::getObject)
            .getVal()
      );
   private final Setting<Double> val16 = this.val2_2
      .addSetting(
         new AutoTotemModuleHelper2()
            .valOf("sound-volume")
            .valOf2("Volume of the find alert.")
            .valOf3(1.0)
            .valOf4(0.0, 2.0)
            .valOf5(0.0, 2.0)
            .valOf7(this.val14::getObject)
            .getVal()
      );
   private final Setting<Double> val17 = this.val2_2
      .addSetting(
         new AutoTotemModuleHelper2()
            .valOf("sound-pitch")
            .valOf2("Pitch of the find alert.")
            .valOf3(1.2)
            .valOf4(0.5, 2.0)
            .valOf5(0.5, 2.0)
            .valOf7(this.val14::getObject)
            .getVal()
      );
   private final Map<ChunkPos, Integer> map = new ConcurrentHashMap<>();
   private final Map<ChunkPos, Long> map2 = new ConcurrentHashMap<>();
   private final Map<ChunkPos, Long> map3 = new ConcurrentHashMap<>();
   private final Set<ChunkPos> set3 = ConcurrentHashMap.newKeySet();
   private final Set<ChunkPos> set4 = ConcurrentHashMap.newKeySet();
   private final Set<ChunkPos> set5 = ConcurrentHashMap.newKeySet();
   private final Queue<ChunkPos> queue = new ConcurrentLinkedQueue<>();
   private long longVal3;

   public PlayerBypassModule() {
      super(SwyzzyAddon.val2, "player-bypass", "Deep world analysis + continuous chunk scanning.");
      Utils_2.run(this, "Player Bypass");
   }

   @Override
   public void run6() {
      this.run15();
      this.longVal3 = 0L;
   }

   @Override
   public void run7() {
      this.run15();
   }

   @Override
   public String getString2() {
      return this.map.isEmpty() ? null : String.valueOf(this.map.size());
   }

   @InternalHelper5
   private void run(ActivityChunkFinderModuleHelper3 var1) {
      if (class310.player != null && class310.world != null) {
         ChunkPos var2 = class310.player.getChunkPos();
         long var3 = System.currentTimeMillis();
         int var5 = this.val7.getObject();
         if (var3 - this.longVal3 > 500L) {
            this.longVal3 = var3;

            for (int var6 = -var5; var6 <= var5; var6++) {
               for (int var7 = -var5; var7 <= var5; var7++) {
                  int var8 = var2.x + var6;
                  int var9 = var2.z + var7;
                  if (class310.world.getChunkManager().getWorldChunk(var8, var9) != null) {
                     ChunkPos var10 = new ChunkPos(var8, var9);
                     if (!this.set5.contains(var10) && !this.set4.contains(var10) && !this.map3.containsKey(var10)) {
                        this.queue.add(var10);
                        this.set4.add(var10);
                     }
                  }
               }
            }
         }

         for (int var14 = 0; var14 < 8; var14++) {
            ChunkPos var15 = this.queue.poll();
            if (var15 == null) {
               break;
            }

            this.set4.remove(var15);
            this.map3.put(var15, System.currentTimeMillis());

            try {
               if (this.check(var15)) {
                  this.set5.add(var15);
               }
            } finally {
               this.map3.put(var15, System.currentTimeMillis());
            }
         }
      }
   }

   @InternalHelper5
   private void run2(ActivityChunkFinderModuleData var1) {
      if (class310.player != null && class310.world != null) {
         double var2 = this.val10.getObject().intValue();
         ActivityChunkFinderModuleEntry2 var4 = this.val4.getObject();
         int var5 = Math.min(255, this.val5.getObject() * this.val6.getObject() / 255);
         ActivityChunkFinderModuleEntry2 var6 = new ActivityChunkFinderModuleEntry2(var4.intVal, var4.intVal2, var4.intVal3, var5);
         long var7 = System.currentTimeMillis();

         for (Entry var10 : this.map3.entrySet()) {
            if (var7 - (Long)var10.getValue() > 1000L) {
               this.map3.remove(var10.getKey());
            } else {
               ChunkPos var11 = (ChunkPos)var10.getKey();
               var1.val
                  .run(
                     var11.getStartX(),
                     var2,
                     var11.getStartZ(),
                     var11.getStartX() + 16.0,
                     var2 + 0.06,
                     var11.getStartZ() + 16.0,
                     var6,
                     var6,
                     RenderMode.Sides,
                     0
                  );
            }
         }

         ActivityChunkFinderModuleEntry2 var14 = this.val3_2.getObject();
         int var15 = Math.min(255, var14.intVal4 * this.val6.getObject() / 255);
         ActivityChunkFinderModuleEntry2 var16 = new ActivityChunkFinderModuleEntry2(var14.intVal, var14.intVal2, var14.intVal3, var15);

         for (ChunkPos var13 : this.map.keySet()) {
            this.run4(var1, var13, var2, var16);
         }
      }
   }

   private int getInt() {
      return Math.max(1, this.val8.getObject() * 5);
   }

   private int getInt5() {
      return this.val9.getObject() * 5;
   }

   private boolean check(ChunkPos var1) {
      if (class310.world == null) {
         return false;
      } else {
         try {
            WorldChunk var2 = class310.world.getChunkManager().getWorldChunk(var1.x, var1.z);
            if (var2 == null) {
               this.map.remove(var1);
               return false;
            } else if (var2.isEmpty()) {
               if (this.val8.getObject() == 0) {
                  this.run4("Skipping empty chunk: %d,%d", new Object[]{var1.x, var1.z});
               }

               return false;
            } else {
               int var3 = 0;
               long var4 = var2.getInhabitedTime();
               this.map2.put(var1, var4);
               int var6 = this.getInt5();
               if (var4 > var6 * 20L) {
                  var3 += 30;
               } else if (var4 > var6 * 5L) {
                  var3 += 15;
               } else if (var4 > var6) {
                  var3 += 5;
               }

               int[] var7 = new int[]{0};
               int[] var8 = new int[]{0};
               var2.forEachBlockMatchingPredicate(PlayerBypassModule::check4, (var914, var916) -> PlayerBypassModule.run8(var7, var914, var916));
               var2.forEachBlockMatchingPredicate(PlayerBypassModule::check2, (var914, var916) -> PlayerBypassModule.run5(var8, var914, var916));
               var3 += var7[0] * 40;
               if (var8[0] >= 12) {
                  var3 += 50;
               } else if (var8[0] >= 8) {
                  var3 += 30;
               } else if (var8[0] >= 5) {
                  var3 += 10;
               }

               for (BlockEntity var10 : var2.getBlockEntities().values()) {
                  BlockEntityType var11 = var10.getType();
                  if (var11 == BlockEntityType.SHULKER_BOX || var11 == BlockEntityType.ENDER_CHEST) {
                     var3 += 20;
                  }
               }

               boolean var14 = var3 >= this.getInt();
               if (var14) {
                  this.map.put(var1, var3);
               } else {
                  this.map.remove(var1);
               }

               if (this.val8.getObject() == 0) {
                  this.run4("Scanned chunk %d,%d score=%d inhabited=%d", new Object[]{var1.x, var1.z, var3, var4});
               }

               if (var14 && this.set3.add(var1)) {
                  this.run3(var1, var3, var4);
               }

               return true;
            }
         } catch (Exception var12) {
            this.map.remove(var1);
            return false;
         }
      }
   }

   private void run3(ChunkPos var1, int var2, long var3) {
      String var5 = this.stringOf(this.val12.getObject(), var1, var2, var3);
      if (this.val11.getObject()) {
         this.run4("%s", new Object[]{var5});
      }

      if (this.val13.getObject()) {
         ChunkFinderV2ModuleUtil.run10("Chunk found - remapping", var5);
      }

      if (this.val14.getObject() && class310.player != null) {
         class310.player.playSound(this.val15.getObject().getclass3414(), this.val16.getObject().floatValue(), this.val17.getObject().floatValue());
      }
   }

   private String stringOf(String var1, ChunkPos var2, int var3, long var4) {
      return var1.replace("{x}", Integer.toString(var2.getCenterX()))
         .replace("{z}", Integer.toString(var2.getCenterZ()))
         .replace("{score}", Integer.toString(var3))
         .replace("{distance}", Integer.toString(this.intOf(var2)))
         .replace("{inhabited}", Long.toString(var4));
   }

   private int intOf(ChunkPos var1) {
      if (class310.player == null) {
         return 0;
      } else {
         double var2 = var1.getCenterX() - class310.player.getX();
         double var4 = var1.getCenterZ() - class310.player.getZ();
         return (int)Math.round(Math.sqrt(var2 * var2 + var4 * var4));
      }
   }

   private void run4(ActivityChunkFinderModuleData var1, ChunkPos var2, double var3, ActivityChunkFinderModuleEntry2 var5) {
      double var6 = var2.getStartX() - 16.0;
      double var8 = var2.getStartZ() - 16.0;
      double var10 = var6 + 48.0;
      double var12 = var8 + 48.0;
      ActivityChunkFinderModuleEntry2 var14 = new ActivityChunkFinderModuleEntry2(var5.intVal, var5.intVal2, var5.intVal3, Math.max(40, var5.intVal4 / 4));
      ActivityChunkFinderModuleEntry2 var15 = new ActivityChunkFinderModuleEntry2(var5.intVal, var5.intVal2, var5.intVal3, Math.max(180, var5.intVal4));
      ActivityChunkFinderModuleEntry2 var16 = new ActivityChunkFinderModuleEntry2(var5.intVal, var5.intVal2, var5.intVal3, Math.max(100, var5.intVal4 / 2));
      var1.val.run(var6, var3, var8, var10, var3 + 3.0, var12, var14, var15, RenderMode.Both, 0);
      double var17 = var3 + 0.1;

      for (byte var19 = 8; var19 < 48; var19 += 8) {
         var1.val.run6(var6 + var19, var17, var8, var6 + var19, var17, var12, var16);
         var1.val.run6(var6, var17, var8 + var19, var10, var17, var8 + var19, var16);
      }
   }

   private void run15() {
      this.map.clear();
      this.map2.clear();
      this.map3.clear();
      this.set3.clear();
      this.set4.clear();
      this.set5.clear();
      this.queue.clear();
   }

   private static void run5(int[] var0, BlockPos var1, BlockState var2) {
      var0[0]++;
   }

   private static boolean check2(BlockState var0) {
      return set2.contains(var0.getBlock());
   }

   private static void run8(int[] var0, BlockPos var1, BlockState var2) {
      var0[0]++;
   }

   private static boolean check4(BlockState var0) {
      return set.contains(var0.getBlock());
   }

   public enum State {
      PLING(PlayerBypassModule.State::getclass341411),
      BELL(PlayerBypassModule.State::getclass341410),
      HARP(PlayerBypassModule.State::getclass34149),
      XP_ORB(PlayerBypassModule.State::getclass34148),
      LEVEL_UP(PlayerBypassModule.State::getclass34147),
      ANVIL(PlayerBypassModule.State::getclass34146),
      BEACON(PlayerBypassModule.State::getclass34145),
      AMETHYST(PlayerBypassModule.State::getclass34144),
      DRAGON(PlayerBypassModule.State::getclass34143),
      BUTTON(PlayerBypassModule.State::getclass34142);

      private Supplier<SoundEvent> sound;

      private State(Supplier<SoundEvent> var3) {
         this.sound = var3;
      }

      public SoundEvent getclass3414() {
         return this.sound.get();
      }

      private static SoundEvent getclass34142() {
         return (SoundEvent)SoundEvents.UI_BUTTON_CLICK.value();
      }

      private static SoundEvent getclass34143() {
         return SoundEvents.ENTITY_ENDER_DRAGON_GROWL;
      }

      private static SoundEvent getclass34144() {
         return SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
      }

      private static SoundEvent getclass34145() {
         return SoundEvents.BLOCK_BEACON_ACTIVATE;
      }

      private static SoundEvent getclass34146() {
         return SoundEvents.BLOCK_ANVIL_LAND;
      }

      private static SoundEvent getclass34147() {
         return SoundEvents.ENTITY_PLAYER_LEVELUP;
      }

      private static SoundEvent getclass34148() {
         return SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
      }

      private static SoundEvent getclass34149() {
         return (SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_HARP.value();
      }

      private static SoundEvent getclass341410() {
         return (SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_BELL.value();
      }

      private static SoundEvent getclass341411() {
         return (SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_PLING.value();
      }

      private static PlayerBypassModule.State[] getValArray() {
         return new PlayerBypassModule.State[]{PLING, BELL, HARP, XP_ORB, LEVEL_UP, ANVIL, BEACON, AMETHYST, DRAGON, BUTTON};
      }
   }
}
