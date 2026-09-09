package dev.kryptic.suschunk;

import dev.kryptic.KrypticClient;
import dev.kryptic.util.UiSounds;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import dev.kryptic.module.render.SusChunkFinderModule;
public class SusChunkScanner {
   public static final int POINTS_PER_SENSITIVITY = 5;
   private static final int GEODE_LINK_DISTANCE = 5;
   private static final boolean DEBUG_LOG = Boolean.getBoolean("kryptic.sus.debug");
   private final SusChunkFinderModule module;
   private final Map<Long, SusChunkScanner.ChunkScore> scores = new ConcurrentHashMap<>();
   private final Deque<Long> queue = new ArrayDeque<>();
   private final Set<Long> lightRescans = ConcurrentHashMap.newKeySet();
   private final Set<Long> alertedChunks = new HashSet<>();
   private volatile List<SusChunkScanner.Flag> flags = List.of();
   private volatile List<SusChunkScanner.Zone> zones = List.of();
   private ChunkPos lastQueueCenter;
   private int tickCounter;

   public SusChunkScanner(SusChunkFinderModule susChunkFinderModule) {
      this.module = susChunkFinderModule;
      ServerLightCache serverLightCache = ServerLightCache.get();
      Set set = this.lightRescans;
      serverLightCache.addDirtyListener(set::add);
   }

   public List<SusChunkScanner.Flag> flags() {
      return this.flags;
   }

   public List<SusChunkScanner.Zone> zones() {
      return this.zones;
   }

   public int threshold() {
      return this.module.sensitivity.getInt() * 5;
   }

   public void clear() {
      this.scores.clear();
      this.queue.clear();
      this.lightRescans.clear();
      this.alertedChunks.clear();
      this.flags = List.of();
      this.zones = List.of();
      this.lastQueueCenter = null;
   }

   public void tick() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world != null && client.player != null) {
         try {
            this.refillQueueIfNeeded(client);
            int n = this.module.scanSpeed.getInt();
            long l = System.nanoTime() + 2000000L;
            int offset = this.scanLightRescans(client, n, l);
            this.scanQueue(client, n - offset, l);
            if (++this.tickCounter % 10 == 0) {
               this.rebuild(client);
            }
         } catch (Exception ex) {
            KrypticClient.LOGGER.warn("SusChunkFinder scan error: {}", ex.toString());
         }
      }
   }

   private int scanLightRescans(MinecraftClient client, int n, long l) {
      if (this.lightRescans.isEmpty()) {
         return 0;
      } else {
         int offset = 0;
         Iterator it = this.lightRescans.iterator();

         while (it.hasNext() && offset < n && System.nanoTime() < l) {
            long l3 = (Long)it.next();
            it.remove();
            WorldChunk chunk = client.world.getChunkManager().getWorldChunk(ChunkPos.getPackedX(l3), ChunkPos.getPackedZ(l3), false);
            if (chunk != null && this.scores.containsKey(l3)) {
               this.scores.put(l3, this.scanChunk(client, chunk));
               offset++;
            }
         }

         return offset;
      }
   }

   private void scanQueue(MinecraftClient client, int n, long l) {
      int localX = 0;
      int localZ = 0;

      while (localX < n && localZ < 128 && !this.queue.isEmpty() && System.nanoTime() < l) {
         localZ++;
         long l3 = this.queue.pollFirst();
         if (!this.scores.containsKey(l3)) {
            WorldChunk chunk = client.world.getChunkManager().getWorldChunk(ChunkPos.getPackedX(l3), ChunkPos.getPackedZ(l3), false);
            if (chunk != null) {
               this.scores.put(l3, this.scanChunk(client, chunk));
               localX++;
            }
         }
      }
   }

   private void refillQueueIfNeeded(MinecraftClient client) {
      ChunkPos chunkPos = client.player.getChunkPos();
      if (this.queue.isEmpty()
         || this.lastQueueCenter == null
         || Math.max(Math.abs(chunkPos.x - this.lastQueueCenter.x), Math.abs(chunkPos.z - this.lastQueueCenter.z)) >= 3) {
         this.lastQueueCenter = chunkPos;
         this.queue.clear();
         int n = Math.min((Integer)client.options.getViewDistance().getValue() + 1, 16);
         ArrayList list = new ArrayList();

         for (int localX = -n; localX <= n; localX++) {
            for (int localZ = -n; localZ <= n; localZ++) {
               long chunkKey2 = ChunkPos.toLong(chunkPos.x + localX, chunkPos.z + localZ);
               if (!this.scores.containsKey(chunkKey2)) {
                  list.add(chunkKey2);
               }
            }
         }

         list.sort(
            Comparator.comparingDouble(
               (Long arg) -> Math.hypot((double)(ChunkPos.getPackedX(arg) - chunkPos.x), (double)(ChunkPos.getPackedZ(arg) - chunkPos.z))
            )
         );
         this.queue.addAll(list);
      }
   }

   private static boolean isPlantTarget(BlockState state) {
      Block block = state.getBlock();
      return block == Blocks.KELP
         || block == Blocks.KELP_PLANT
         || block == Blocks.BAMBOO
         || block == Blocks.SWEET_BERRY_BUSH
         || block == Blocks.VINE
         || block == Blocks.POINTED_DRIPSTONE;
   }

   private static boolean isAmethystStructure(BlockState state) {
      return state.isOf(Blocks.BUDDING_AMETHYST) || state.isOf(Blocks.AMETHYST_BLOCK);
   }

   private SusChunkScanner.ChunkScore scanChunk(MinecraftClient client, WorldChunk chunk) {
      SusChunkScanner.ChunkScore chunkKey2 = new SusChunkScanner.ChunkScore(chunk.getPos().toLong());
      if (this.module.amethyst.get()) {
         this.detectAmethyst(client, chunk, chunkKey2);
      }

      this.detectGrowth(client, chunk, chunkKey2);
      chunkKey2.computeScore();
      if (DEBUG_LOG && chunkKey2.coord > 0.0) {
         KrypticClient.LOGGER.info("SusChunk: chunk {} scored {} on {} hits", new Object[]{chunk.getPos(), chunkKey2.coord, chunkKey2.hits});
      }

      return chunkKey2;
   }

   private void detectAmethyst(MinecraftClient client, WorldChunk chunk, SusChunkScanner.ChunkScore chunkScore) {
      List<net.minecraft.util.math.BlockPos> list = ServerLightCache.get().light5Positions(chunk.getPos().x, chunk.getPos().z);
      if (!list.isEmpty()) {
         Mutable mutablePos = new Mutable();

         for (BlockPos pos : list) {
            BlockState state = client.world.getBlockState(pos);
            if ((state.isAir() || state.isOf(Blocks.AMETHYST_CLUSTER)) && hasAmethystNeighbour(client, pos, mutablePos)) {
               chunkScore.amethystCells.add(pos.toImmutable());
               if (DEBUG_LOG) {
                  KrypticClient.LOGGER.info("SusChunk: candidate chunk {} scored {} on {} hits", pos, state.isAir() ? "hidden" : "visible");
               }
            }
         }
      }
   }

   private static boolean hasAmethystNeighbour(MinecraftClient client, BlockPos pos, Mutable mutablePos) {
      for (int bestSlot = -1; bestSlot <= 1; bestSlot++) {
         for (int bestSlot2 = -1; bestSlot2 <= 1; bestSlot2++) {
            for (int bestSlot3 = -1; bestSlot3 <= 1; bestSlot3++) {
               if (bestSlot != 0 || bestSlot2 != 0 || bestSlot3 != 0) {
                  mutablePos.set(pos.getX() + bestSlot, pos.getY() + bestSlot2, pos.getZ() + bestSlot3);
                  if (isAmethystStructure(client.world.getBlockState(mutablePos))) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private void detectGrowth(MinecraftClient client, WorldChunk chunk, SusChunkScanner.ChunkScore chunkScore) {
      ChunkSection[] arr = chunk.getSectionArray();
      int n = chunk.getBottomSectionCoord();
      int n13 = ChunkSectionPos.getSectionCoord(-12);
      int n14 = ChunkSectionPos.getSectionCoord(80);
      int n15 = chunk.getPos().getStartX();
      int n16 = chunk.getPos().getStartZ();
      Mutable mutablePos = new Mutable();

      for (int n17 = 0; n17 < arr.length; n17++) {
         int n18 = n + n17;
         if (n18 >= n13 && n18 <= n14) {
            ChunkSection chunkSection = arr[n17];
            if (!chunkSection.isEmpty() && chunkSection.getBlockStateContainer().hasAny(SusChunkScanner::isPlantTarget)) {
               int n19 = n18 << 4;

               for (int n20 = 0; n20 < 16; n20++) {
                  int n21 = n19 + n20;
                  if (n21 >= -12 && n21 <= 80) {
                     for (int n22 = 0; n22 < 16; n22++) {
                        for (int n23 = 0; n23 < 16; n23++) {
                           BlockState state = chunkSection.getBlockState(n23, n20, n22);
                           if (isPlantTarget(state)) {
                              mutablePos.set(n15 + n23, n21, n16 + n22);
                              this.inspectPlant(client, state, mutablePos, chunkScore);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void inspectPlant(MinecraftClient client, BlockState state, BlockPos pos, SusChunkScanner.ChunkScore chunkScore) {
      Block block = state.getBlock();
      if ((block == Blocks.KELP || block == Blocks.KELP_PLANT) && this.module.kelp.get()) {
         BlockState state2 = client.world.getBlockState(pos.up());
         if (state2.isOf(Blocks.KELP) || state2.isOf(Blocks.KELP_PLANT)) {
            return;
         }

         int n = columnLength(client, pos, Direction.DOWN, Blocks.KELP, Blocks.KELP_PLANT);
         boolean matches = block == Blocks.KELP && state.contains(Properties.AGE_25) && (Integer)state.get(Properties.AGE_25) == 25;
         if (matches && n >= 8 || n >= 14) {
            chunkScore.add(SusChunkScanner.SignalType.KELP, pos);
            if (DEBUG_LOG) {
               KrypticClient.LOGGER.info("SusChunk: candidate chunk {} scored {} on {} hits", new Object[]{pos, n, matches});
            }
         }
      } else if (block == Blocks.BAMBOO && this.module.bamboo.get()) {
         if (client.world.getBlockState(pos.down()).isOf(Blocks.BAMBOO)) {
            return;
         }

         int localZ = columnLength(client, pos, Direction.UP, Blocks.BAMBOO);
         if (localZ >= 12) {
            chunkScore.add(SusChunkScanner.SignalType.BAMBOO, pos);
            if (DEBUG_LOG) {
               KrypticClient.LOGGER.info("SusChunk: candidate chunk {} scored {} on {} hits", pos, localZ);
            }
         }
      } else if (block == Blocks.SWEET_BERRY_BUSH && this.module.berries.get()) {
         if (state.contains(Properties.AGE_3) && (Integer)state.get(Properties.AGE_3) == 3) {
            chunkScore.add(SusChunkScanner.SignalType.BERRIES, pos);
            if (DEBUG_LOG) {
               KrypticClient.LOGGER.info("SusChunk: candidate chunk {} scored {} on {} hits", pos);
            }
         }
      } else if (block == Blocks.VINE && this.module.vines.get()) {
         if (client.world.getBlockState(pos.up()).isOf(Blocks.VINE)) {
            return;
         }

         int localY = columnLength(client, pos, Direction.DOWN, Blocks.VINE);
         if (localY >= 7) {
            chunkScore.add(SusChunkScanner.SignalType.VINES, pos);
            if (DEBUG_LOG) {
               KrypticClient.LOGGER.info("SusChunk: candidate chunk {} scored {} on {} hits", pos, localY);
            }
         }
      } else if (block == Blocks.POINTED_DRIPSTONE && this.module.dripstone.get()) {
         if (client.world.getBlockState(pos.up()).isOf(Blocks.POINTED_DRIPSTONE)
            || !client.world.getBlockState(pos.down()).isOf(Blocks.POINTED_DRIPSTONE)) {
            return;
         }

         int step = columnLength(client, pos, Direction.DOWN, Blocks.POINTED_DRIPSTONE);
         if (step >= 5) {
            chunkScore.add(SusChunkScanner.SignalType.DRIPSTONE, pos);
            if (DEBUG_LOG) {
               KrypticClient.LOGGER.info("SusChunk: candidate chunk {} scored {} on {} hits", pos, step);
            }
         }
      }
   }

   private static int columnLength(MinecraftClient client, BlockPos pos, Direction direction, Block... temp) {
      byte b = 1;
      Mutable mutablePos = pos.mutableCopy();
      if (b < 40) {
         mutablePos.move(direction);
         BlockState state = client.world.getBlockState(mutablePos);

         for (Block block : temp) {
            if (state.isOf(block)) {
            }
         }
      }

      return b;
   }

   private void rebuild(MinecraftClient client) {
      this.scores
         .keySet()
         .removeIf(arg -> client.world.getChunkManager().getWorldChunk(ChunkPos.getPackedX(arg), ChunkPos.getPackedZ(arg), false) == null);
      int n = this.threshold();
      HashMap<Long, SusChunkScanner.FlagAggregate> map = new HashMap<>();

      for (SusChunkScanner.ChunkScore chunkScore : this.scores.values()) {
         if (chunkScore.coord >= (double)n) {
            SusChunkScanner.FlagAggregate flagAggregate = map.computeIfAbsent(chunkScore.chunkKey, k -> { SusChunkScanner.FlagAggregate fa = new SusChunkScanner.FlagAggregate(k); return fa; });
            flagAggregate.coord = Math.max(flagAggregate.coord, chunkScore.coord);
            flagAggregate.hitWeight = flagAggregate.hitWeight + chunkScore.hitWeight;
            flagAggregate.hitX = flagAggregate.hitX + chunkScore.hitX;
            flagAggregate.hitZ = flagAggregate.hitZ + chunkScore.hitZ;
         }
      }

      for (SusChunkScanner.Zone geode : this.clusterGeodes()) {
         if (!(geode.totalScore() < (double)n)) {
            for (long l : geode.members()) {
               SusChunkScanner.FlagAggregate flagAggregate2 = map.computeIfAbsent(l, k -> { SusChunkScanner.FlagAggregate fa = new SusChunkScanner.FlagAggregate(k); return fa; });
               flagAggregate2.coord = Math.max(flagAggregate2.coord, geode.totalScore());
            }

            double d = SusChunkScanner.SignalType.AMETHYST.weight;

            for (long chunkLong : geode.members()) { BlockPos pos = new net.minecraft.util.math.BlockPos((int)(chunkLong >> 32), 64, (int)(chunkLong & 0xFFFFFFFFL));
               SusChunkScanner.FlagAggregate chunkKey2 = (SusChunkScanner.FlagAggregate)map.get(
                  ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4)
               );
               if (chunkKey2 != null) {
                  chunkKey2.hitWeight += d;
                  chunkKey2.hitX = chunkKey2.hitX + ((double)pos.getX() + 0.5) * d;
                  chunkKey2.hitZ = chunkKey2.hitZ + ((double)pos.getZ() + 0.5) * d;
               }
            }

            if (DEBUG_LOG) {
               KrypticClient.LOGGER
                  .info(
                     "(_=\u001dOÃÂ¤\u008bÃÂ¯\u0086ÃÂ¿ÃÂ¬ÃÂÃÂ¸ÃÂÃÂ³ÃÂÃÂ»ÃÂÃÂÃÂÃÂ¼ÃÂÃÂÃÂÃÂ¦ÃÂÃÂÃÂÃÂÃÂÃÂ¾ÃÂÃÂµÃÂÃÂÃÂ­ÃÂÃÂ¿ÃÂÃ·ÃÂ¼ÃÂ¼ÃÂÃÂ©ÃÂÃÂÃÂ¢\u05caÃÂ²ÃÂ",
                     new Object[]{geode.members().size(), geode.members().size(), geode.totalScore()}
                  );
            }
         }
      }

      ArrayList list = new ArrayList();

      for (SusChunkScanner.FlagAggregate flagAggregate3 : map.values()) {
         list.add(new SusChunkScanner.Flag(flagAggregate3.chunkKey, flagAggregate3.coord));
      }

      this.flags = List.copyOf(list);
      this.zones = this.buildZones(list, map);
      this.fireAlerts(client);
   }

   private List<SusChunkScanner.Zone> clusterGeodes() {
      ArrayList list = new ArrayList();

      for (SusChunkScanner.ChunkScore chunkScore : this.scores.values()) {
         list.addAll(chunkScore.amethystCells);
      }

      int n = list.size();
      if (n == 0) {
         return List.of();
      } else {
         int[] step2 = new int[n];
         int n9 = 0;

         while (n9 < n) {
            step2[n9] = n9++;
         }

         for (int n10 = 0; n10 < n; n10++) {
            BlockPos pos = (BlockPos)list.get(n10);

            for (int n11 = n10 + 1; n11 < n; n11++) {
               BlockPos pos4 = (BlockPos)list.get(n11);
               int n12 = Math.max(
                  Math.abs(pos.getX() - pos4.getX()),
                  Math.max(Math.abs(pos.getY() - pos4.getY()), Math.abs(pos.getZ() - pos4.getZ()))
               );
               if (n12 <= 5) {
                  step2[find(step2, n10)] = find(step2, n11);
               }
            }
         }

         HashMap<Integer, ArrayList<BlockPos>> map2 = new HashMap<>();

         for (int n13 = 0; n13 < n; n13++) {
            map2.computeIfAbsent(find(step2, n13), k -> new ArrayList<>()).add((BlockPos)list.get(n13));
         }

         ArrayList<SusChunkScanner.Zone> list2 = new ArrayList<>();

         for (ArrayList<BlockPos> list3 : map2.values()) {
            HashSet set = new HashSet();
            double d = 0.0;
            double currentScore = 0.0;

            for (BlockPos pos3 : list3) {
               set.add(ChunkPos.toLong(pos3.getX() >> 4, pos3.getZ() >> 4));
               d += (double)pos3.getX() + 0.5;
               currentScore += (double)pos3.getZ() + 0.5;
            }

            double currentScore2 = SusChunkScanner.SignalType.AMETHYST.weight * (double)Math.min(list3.size(), SusChunkScanner.SignalType.AMETHYST.cap);
            list2.add(new SusChunkScanner.Zone(new java.util.HashSet<Long>(set), d / (double)list3.size(), currentScore / (double)list3.size(), currentScore2, currentScore2));
         }

         return list2;
      }
   }

   private static int find(int[] n, int offset) {
      while (n[offset] != offset) {
         n[offset] = n[n[offset]];
         offset = n[offset];
      }

      return offset;
   }

   private List<SusChunkScanner.Zone> buildZones(List<SusChunkScanner.Flag> list, Map<Long, SusChunkScanner.FlagAggregate> map) {
      int radius = Math.max(1, this.module.mergeRadius.getInt());
      HashMap map2 = new HashMap();

      for (SusChunkScanner.Flag item : list) {
         map2.put(item.chunkKey(), item);
      }

      ArrayList<SusChunkScanner.Zone> list2 = new ArrayList<>();
      HashSet set = new HashSet();

      for (SusChunkScanner.Flag item2 : list) {
         if (set.add(item2.chunkKey())) {
            ArrayList list3 = new ArrayList();
            ArrayDeque arrayDeque = new ArrayDeque<>(List.of(item2));

            while (!arrayDeque.isEmpty()) {
               SusChunkScanner.Flag queued = (SusChunkScanner.Flag)arrayDeque.poll();
               list3.add(queued);
               int n = ChunkPos.getPackedX(queued.chunkKey());
               int offset = ChunkPos.getPackedZ(queued.chunkKey());

               for (int radius2 = -radius; radius2 <= radius; radius2++) {
                  for (int radius3 = -radius; radius3 <= radius; radius3++) {
                     if (radius2 != 0 || radius3 != 0) {
                        SusChunkScanner.Flag chunkKey2 = (SusChunkScanner.Flag)map2.get(ChunkPos.toLong(n + radius2, offset + radius3));
                        if (chunkKey2 != null && set.add(chunkKey2.chunkKey())) {
                           arrayDeque.add(chunkKey2);
                        }
                     }
                  }
               }
            }

            list2.add(this.makeZone(list3, map));
         }
      }

      list2.sort(Comparator.comparingDouble(SusChunkScanner.Zone::totalScore).reversed());
      return List.copyOf(list2);
   }

   private SusChunkScanner.Zone makeZone(List<SusChunkScanner.Flag> list, Map<Long, SusChunkScanner.FlagAggregate> map) {
      HashSet set = new HashSet();
      double d = 0.0;
      double currentScore = 0.0;
      double currentScore2 = 0.0;
      double coord3 = 0.0;
      double coord4 = 0.0;
      double coord5 = 0.0;
      double coord6 = 0.0;

      for (SusChunkScanner.Flag item : list) {
         set.add(item.chunkKey());
         d += item.coord();
         currentScore = Math.max(currentScore, item.coord());
         SusChunkScanner.FlagAggregate flagAggregate = (SusChunkScanner.FlagAggregate)map.get(item.chunkKey());
         if (flagAggregate != null && flagAggregate.hitWeight > 0.0) {
            currentScore2 += flagAggregate.hitWeight;
            coord3 += flagAggregate.hitX;
            coord4 += flagAggregate.hitZ;
         }

         coord5 += (double)(ChunkPos.getPackedX(item.chunkKey()) * 16 + 8) * item.coord();
         coord6 += (double)(ChunkPos.getPackedZ(item.chunkKey()) * 16 + 8) * item.coord();
      }

      double coord7 = currentScore2 > 0.0 ? coord3 / currentScore2 : coord5 / d;
      double coord8 = currentScore2 > 0.0 ? coord4 / currentScore2 : coord6 / d;
      return new SusChunkScanner.Zone(Set.copyOf(set), coord7, coord8, d, currentScore);
   }

   private void fireAlerts(MinecraftClient client) {
      for (SusChunkScanner.Zone zone : this.zones) {
         boolean matches = true;

         for (long l : zone.members()) {
            if (this.alertedChunks.contains(l)) {
               matches = false;
               break;
            }
         }

         if (!matches) {
            this.alertedChunks.addAll(zone.members());
         } else {
            this.alertedChunks.addAll(zone.members());
            if (!this.module.notifications.is("<J.")) {
               int n = (int)Math.round(zone.centroidX());
               int localX = (int)Math.round(zone.centroidZ());
               int localZ = (int)Math.hypot((double)n - client.player.getX(), (double)localX - client.player.getZ());
               String text = "Sus zone \u00B7 " + localZ + "m \u00B7 " + n + ", " + localX;
               if (this.module.notifications.is("Toast") && KrypticClient.notifications() != null) {
                  KrypticClient.notifications().pushInfo(text);
                  UiSounds.notification(true);
               } else if (this.module.notifications.is("Chat")) {
                  client.player.sendMessage(Text.literal("\u00A7d[Kryptic] \u00A7f" + text), false);
               }
            }
         }
      }
   }

   public static final class ChunkScore {
      public final long chunkKey;
      public final EnumMap<SusChunkScanner.SignalType, Integer> hits = new EnumMap<>(SusChunkScanner.SignalType.class);
      public final List<BlockPos> amethystCells = new ArrayList<>();
      public double coord;
      double hitWeight;
      double hitX;
      double hitZ;

      ChunkScore(long l) {
         this.chunkKey = l;
      }

      void add(SusChunkScanner.SignalType signalType, BlockPos pos) {
         this.hits.merge(signalType, Integer.valueOf(1), Integer::sum);
         this.hitWeight = this.hitWeight + signalType.weight;
         this.hitX = this.hitX + ((double)pos.getX() + 0.5) * signalType.weight;
         this.hitZ = this.hitZ + ((double)pos.getZ() + 0.5) * signalType.weight;
      }

      void computeScore() {
         double d = 0.0;

         for (Entry entry : this.hits.entrySet()) {
            d += ((SusChunkScanner.SignalType)entry.getKey()).weight
               * (double)Math.min((Integer)entry.getValue(), ((SusChunkScanner.SignalType)entry.getKey()).cap);
         }

         this.coord = d;
      }
   }

   public static record Flag(long chunkKey, double coord) {
   }

   private static final class FlagAggregate {
      final long chunkKey;
      double coord;
      double hitWeight;
      double hitX;
      double hitZ;

      FlagAggregate(long l) {
         this.chunkKey = l;
      }
   }

   public static record Geode(List<BlockPos> cells, Set<Long> chunks, double coord, double centroidX, double centroidZ) {
   }

   public static enum SignalType {
      AMETHYST(6.0, 16),
      KELP(2.0, 6),
      BAMBOO(2.0, 6),
      BERRIES(2.0, 5),
      VINES(2.0, 6),
      DRIPSTONE(2.0, 5);

      public final double weight;
      public final int cap;

      private SignalType(double nullxx, int nullxxx) {
         this.weight = nullxx;
         this.cap = nullxxx;
      }

      private static SusChunkScanner.SignalType[] $values() {
         return new SusChunkScanner.SignalType[]{AMETHYST, KELP, BAMBOO, BERRIES, VINES, DRIPSTONE};
      }
   }

   public static record Zone(Set<Long> members, double centroidX, double centroidZ, double totalScore, double maxScore) {
   }
}
