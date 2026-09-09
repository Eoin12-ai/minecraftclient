package dev.kryptic.module.render;

import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.BooleanSetting;
import dev.kryptic.settings.ColorSetting;
import dev.kryptic.settings.IconListSetting;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.SliderSetting;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public class BlockEntityEspModule extends Module {
   private static final String OTHER = "other";
   private static final int MAX_ENTRIES = 8192;
   public final IconListSetting blockEntities = this.addSetting(new IconListSetting("Block Entities", "Which block-entity types to highlight"));
   public final ModeSetting mode = this.addSetting(new ModeSetting("Mode", "Box style — hollow outline or translucent fill", "Full", "Full", "Outline"));
   public final SliderSetting range = this.addSetting(new SliderSetting("Range", "Max distance a block entity is highlighted", 128.0, 16.0, 512.0, 8.0));
   public final SliderSetting highlightAlpha = this.addSetting(new SliderSetting("Highlight Alpha", "Box opacity (0-255)", 180.0, 0.0, 255.0, 1.0));
   public final BooleanSetting tracers = this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each block entity", false));
   public final BooleanSetting showGhosts = this.addSetting(
      new BooleanSetting("Show Ghosts", "Keep entries the server sent but that are gone client-side", true)
   );
   public final ColorSetting ghostTint = this.addSetting(new ColorSetting("Ghost Tint", "Color blended into ghost entries", -922791856));
   public final BooleanSetting chunkPackets = this.addSetting(new BooleanSetting("Chunk Packets", "Read block entities from chunk-data packets", true));
   public final BooleanSetting beUpdatePackets = this.addSetting(
      new BooleanSetting("BE Update Packets", "Read block entities from block-entity update packets", true)
   );
   public final BooleanSetting worldRescan = this.addSetting(new BooleanSetting("World Rescan", "Also snapshot already-loaded chunks when enabled", true));
   private final Map<String, String> aliases = new HashMap<>();
   private final Map<Long, BlockEntityEspModule.Cached> cache = new ConcurrentHashMap<>();

   public BlockEntityEspModule() {
      super("BlockEntityESP", "Highlights block entities from raw packets", Category.RENDER);
      ColorSetting colorSetting = this.ghostTint;
      BooleanSetting booleanSetting = this.showGhosts;
      colorSetting.visibleWhen(booleanSetting::get);
      this.type("minecraft:chest", "Chest", Items.CHEST, -22016, true);
      this.type("minecraft:trapped_chest", "Trapped Chest", Items.TRAPPED_CHEST, -65536, true);
      this.type("minecraft:ender_chest", "Ender Chest", Items.ENDER_CHEST, -8912641, true);
      this.type("minecraft:shulker_box", "Shulker Box", Items.SHULKER_BOX, -47873, true);
      this.type("minecraft:barrel", "Barrel", Items.BARREL, -7842560, true);
      this.type("minecraft:mob_spawner", "Spawner", Items.SPAWNER, -16711936, true);
      this.type("minecraft:hopper", "Hopper", Items.HOPPER, -7829368, false);
      this.type("minecraft:furnace", "Furnace", Items.FURNACE, -7566196, false);
      this.alias("minecraft:blast_furnace", "minecraft:furnace");
      this.alias("minecraft:smoker", "minecraft:furnace");
      this.type("minecraft:dispenser", "Dispenser", Items.DISPENSER, -10066330, false);
      this.alias("minecraft:dropper", "minecraft:dispenser");
      this.type("minecraft:brewing_stand", "Brewing Stand", Items.BREWING_STAND, -3372801, false);
      this.type("minecraft:beehive", "Beehive", Items.BEEHIVE, -13312, false);
      this.type("minecraft:enchanting_table", "Enchanting Table", Items.ENCHANTING_TABLE, -7864065, false);
      this.type("minecraft:sign", "Sign", Items.OAK_SIGN, -3355444, false);
      this.alias("minecraft:hanging_sign", "minecraft:sign");
      this.type("minecraft:bed", "1I,", Items.RED_BED, -30584, false);
      this.type("minecraft:skull", "Skull", Items.SKELETON_SKULL, -2236963, false);
      this.type("minecraft:banner", "Banner", Items.WHITE_BANNER, -1118482, false);
      this.type("minecraft:crafter", "Crafter", Items.CRAFTER, -12276993, false);
      this.type("minecraft:vault", "Vault", Items.VAULT, -10496, false);
      this.type("minecraft:trial_spawner", "Trial Spawner", Items.TRIAL_SPAWNER, -16711766, false);
      this.type("other", "Other", Items.BEDROCK, -5592406, true);
   }

   private void type(String text2, String str3, Item item, int n, boolean value) {
      this.blockEntities.add(text2, str3, item, value, n);
   }

   private void alias(String text2, String str3) {
      this.aliases.put(text2, str3);
   }

   private String canonicalKey(String text2) {
      if (this.blockEntities.get(text2) != null) {
         return text2;
      } else {
         String text5 = this.aliases.get(text2);
         if (text5 != null) {
            return text5;
         } else {
            int n = text2.indexOf(58);
            if (n >= 0) {
               String json = text2.substring(n + 1);
               String text = "minecraft:" + json;
               if (this.blockEntities.get(text) != null) {
                  return text;
               }

               if (this.aliases.containsKey(text)) {
                  return this.aliases.get(text);
               }
            }

            return "other";
         }
      }
   }

   public boolean chunkPacketsEnabled() {
      return this.chunkPackets.get();
   }

   public boolean beUpdatePacketsEnabled() {
      return this.beUpdatePackets.get();
   }

   public void record(BlockPos pos, BlockEntityType<?> blockEntityType) {
      if (pos != null && blockEntityType != null) {
         Identifier id = Registries.BLOCK_ENTITY_TYPE.getId(blockEntityType);
         String text2 = this.canonicalKey(id != null ? id.toString() : String.valueOf(blockEntityType));
         this.cache.put(pos.asLong(), new BlockEntityEspModule.Cached(pos.toImmutable(), text2, System.currentTimeMillis()));
         if (this.cache.size() > 8192) {
            this.pruneOldest();
         }
      }
   }

   private void pruneOldest() {
      long l = Long.MAX_VALUE;
      Long value = null;

      for (Entry entry : this.cache.entrySet()) {
         if (((BlockEntityEspModule.Cached)entry.getValue()).lastSeenMs() < l) {
            l = ((BlockEntityEspModule.Cached)entry.getValue()).lastSeenMs();
            value = (Long)entry.getKey();
         }
      }

      if (value != null) {
         this.cache.remove(value);
      }
   }

   public Collection<BlockEntityEspModule.Cached> entries() {
      return this.cache.values();
   }

   public void clear() {
      this.cache.clear();
   }

   public int cachedCount() {
      return this.cache.size();
   }

   @Override
   protected void onEnable() {
      this.cache.clear();
      if (this.worldRescan.get()) {
         this.rescanLoadedChunks();
      }
   }

   @Override
   protected void onDisable() {
      this.cache.clear();
   }

   @Override
   public void onTick() {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity player = client.player;
      if (client.world != null && player != null) {
         double d = this.range.get();
         double coord = d * d;
         double currentScore = player.getX();
         double coord3 = player.getY();
         double coord4 = player.getZ();
         this.cache.values().removeIf(arg -> {
            BlockPos pos = arg.pos();
            double coord5 = (double)pos.getX() + 0.5 - currentScore;
            double coord6 = (double)pos.getY() + 0.5 - coord3;
            double coord7 = (double)pos.getZ() + 0.5 - coord4;
            return coord5 * coord5 + coord6 * coord6 + coord7 * coord7 > coord;
         });
      }
   }

   private void rescanLoadedChunks() {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientWorld world = client.world;
      ClientPlayerEntity player = client.player;
      if (world != null && player != null) {
         int n = Math.min(32, (int)Math.ceil(this.range.get() / 16.0) + 2);
         int chunkPos = player.getChunkPos().x;
         int chunkPos2 = player.getChunkPos().z;
         Mutable mutablePos = new Mutable();

         for (int n12 = chunkPos - n; n12 <= chunkPos + n; n12++) {
            for (int n13 = chunkPos2 - n; n13 <= chunkPos2 + n; n13++) {
               if (world.getChunkManager().isChunkLoaded(n12, n13)) {
                  WorldChunk chunk = world.getChunk(n12, n13);
                  ChunkSection[] arr = chunk.getSectionArray();
                  int n14 = chunk.getBottomSectionCoord();
                  int n15 = chunk.getPos().getStartX();
                  int n16 = chunk.getPos().getStartZ();

                  for (int n17 = 0; n17 < arr.length; n17++) {
                     ChunkSection chunkSection = arr[n17];
                     if (!chunkSection.isEmpty() && chunkSection.hasAny(AbstractBlockState::hasBlockEntity)) {
                        int n18 = n14 + n17 << 4;

                        for (int n19 = 0; n19 < 16; n19++) {
                           for (int n20 = 0; n20 < 16; n20++) {
                              for (int n21 = 0; n21 < 16; n21++) {
                                 BlockState state = chunkSection.getBlockState(n21, n19, n20);
                                 if (state.hasBlockEntity()) {
                                    Block block = state.getBlock();
                                    if (block instanceof BlockEntityProvider) {
                                       BlockEntityProvider blockEntityProvider = (BlockEntityProvider)block;
                                       mutablePos.set(n15 + n21, n18 + n19, n16 + n20);

                                       try {
                                          BlockEntity blockEntity = blockEntityProvider.createBlockEntity(mutablePos.toImmutable(), state);
                                          if (blockEntity != null) {
                                             this.record(mutablePos, blockEntity.getType());
                                          }
                                       } catch (Exception ex) {
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
   }

   public static record Cached(BlockPos pos, String typeKey, long lastSeenMs) {
   }
}
