package dev.sixseven.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockListSetting extends Setting<List<BlockListSetting.Target>> {
   public static final int DEFAULT_COLOR = -16711736;
   private final Set<Identifier> ids = new HashSet<>();

   public BlockListSetting(String str, String str3) {
      super(str, str3, new ArrayList<>());
   }

   public List<BlockListSetting.Target> targets() {
      return this.value;
   }

   public int size() {
      return this.value.size();
   }

   public long enabledCount() {
      return this.value.stream().filter(arg -> arg.enabled.get()).count();
   }

   public boolean contains(Identifier id2) {
      return this.ids.contains(id2);
   }

   public BlockListSetting.Target find(Block block2) {
      if (block2 == null) {
         return null;
      } else {
         Identifier id2 = Registries.BLOCK.getId(block2);
         if (id2 != null && this.ids.contains(id2)) {
            for (BlockListSetting.Target temp : this.value) {
               if (temp.id().equals(id2)) {
                  return temp;
               }
            }

            return null;
         } else {
            return null;
         }
      }
   }

   public boolean isActive(Block block2) {
      BlockListSetting.Target target = this.find(block2);
      return target != null && target.enabled.get();
   }

   public BlockListSetting.Target add(Block block2, boolean value, int n) {
      if (block2 != null && block2 != Blocks.AIR) {
         Identifier id2 = Registries.BLOCK.getId(block2);
         if (id2 != null && !this.ids.contains(id2)) {
            BlockListSetting.Target target = new BlockListSetting.Target(id2, block2, value, n);
            this.value.add(target);
            this.ids.add(id2);
            return target;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public void remove(BlockListSetting.Target target) {
      if (this.value.remove(target)) {
         this.ids.remove(target.id());
      }
   }

   public void clear() {
      this.value.clear();
      this.ids.clear();
   }

   public List<Block> searchRegistry(String str, int n) {
      String trimmed = str == null ? "" : str.trim().toLowerCase(Locale.ROOT);
      ArrayList list = new ArrayList();
      if (!trimmed.isEmpty() && n > 0) {
         for (Block block2 : Registries.BLOCK) {
            if (block2 != Blocks.AIR && block2 != Blocks.CAVE_AIR && block2 != Blocks.VOID_AIR) {
               Identifier id2 = Registries.BLOCK.getId(block2);
               if (id2 != null && !this.ids.contains(id2)) {
                  String path = id2.getPath().toLowerCase(Locale.ROOT);
                  String text6 = id2.getNamespace().toLowerCase(Locale.ROOT);
                  String text7 = displayName(block2).toLowerCase(Locale.ROOT);
                  if (path.contains(trimmed) || text6.contains(trimmed) || text7.contains(trimmed)) {
                     list.add(block2);
                     if (list.size() >= n) {
                        break;
                     }
                  }
               }
            }
         }

         return list;
      } else {
         return list;
      }
   }

   public static String displayName(Block block2) {
      try {
         return block2.getName().getString();
      } catch (Throwable ex) {
         Identifier id2 = Registries.BLOCK.getId(block2);
         return id2 != null ? id2.getPath() : "block";
      }
   }

   public void seedDefaults() {
      this.clear();
      this.add(Blocks.DIAMOND_ORE, true, -16711736);
      this.add(Blocks.DEEPSLATE_DIAMOND_ORE, true, -16711736);
      this.add(Blocks.EMERALD_ORE, false, -16711868);
      this.add(Blocks.DEEPSLATE_EMERALD_ORE, false, -16711868);
      this.add(Blocks.ANCIENT_DEBRIS, true, -39356);
      this.add(Blocks.NETHER_GOLD_ORE, false, -10496);
      this.add(Blocks.GOLD_ORE, false, -10496);
      this.add(Blocks.DEEPSLATE_GOLD_ORE, false, -10496);
      this.add(Blocks.IRON_ORE, false, -3618616);
      this.add(Blocks.DEEPSLATE_IRON_ORE, false, -3618616);
      this.add(Blocks.COAL_ORE, false, -12303292);
      this.add(Blocks.DEEPSLATE_COAL_ORE, false, -12303292);
      this.add(Blocks.COPPER_ORE, false, -4689101);
      this.add(Blocks.DEEPSLATE_COPPER_ORE, false, -4689101);
      this.add(Blocks.LAPIS_ORE, false, -12490271);
      this.add(Blocks.DEEPSLATE_LAPIS_ORE, false, -12490271);
      this.add(Blocks.REDSTONE_ORE, false, -65536);
      this.add(Blocks.DEEPSLATE_REDSTONE_ORE, false, -65536);
      this.add(Blocks.SPAWNER, false, -7846657);
      this.add(Blocks.END_PORTAL_FRAME, false, -12255250);
      this.add(Blocks.CHEST, false, -22016);
   }

   @Override
   public JsonElement toJson() {
      JsonArray jsonArray = new JsonArray();

      for (BlockListSetting.Target target : this.value) {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("id", target.id().toString());
         jsonObject.addProperty("enabled", target.enabled.get());
         jsonObject.addProperty("color", target.color.get());
         jsonArray.add(jsonObject);
      }

      return jsonArray;
   }

   @Override
   public void fromJson(JsonElement jsonElement) {
      if (jsonElement != null && jsonElement.isJsonArray()) {
         this.clear();
         Iterator it = jsonElement.getAsJsonArray().iterator();

         while (true) {
            JsonObject jsonObject;
            Identifier id2;
            while (true) {
               if (!it.hasNext()) {
                  return;
               }

               JsonElement jsonElement2 = (JsonElement)it.next();
               if (jsonElement2.isJsonObject()) {
                  jsonObject = jsonElement2.getAsJsonObject();
                  if (jsonObject.has("id")) {
                     try {
                        id2 = Identifier.of(jsonObject.get("id").getAsString());
                        break;
                     } catch (Exception ex) {
                     }
                  }
               }
            }

            Block block2 = (Block)Registries.BLOCK.get(id2);
            if (block2 != null && block2 != Blocks.AIR) {
               boolean enabled2 = !jsonObject.has("enabled") || jsonObject.get("enabled").getAsBoolean();
               int n = jsonObject.has("color") ? jsonObject.get("color").getAsInt() : -16711736;
               this.add(block2, enabled2, n);
            }
         }
      }
   }

   public static final class Target {
      private final Identifier id;
      private final Block block;
      public final BooleanSetting enabled;
      public final ColorSetting color;

      Target(Identifier id2, Block block2, boolean enabled2, int n) {
         this.id = id2;
         this.block = block2;
         this.enabled = new BooleanSetting("Enabled", "Highlight this block", enabled2);
         this.color = new ColorSetting(BlockListSetting.displayName(block2), "Highlight color", n);
      }

      public Identifier id() {
         return this.id;
      }

      public Block block() {
         return this.block;
      }

      public String label() {
         return this.color.getName();
      }
   }
}
