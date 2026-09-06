package dev.sixseven.module.misc;

import com.google.common.collect.LinkedHashMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.sixseven.SixSevenClient;
import dev.sixseven.module.Category;
import dev.sixseven.module.Module;
import dev.sixseven.settings.ModeSetting;
import dev.sixseven.settings.StringSetting;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.texture.PlayerSkinTexture;

public class SkinProtectModule extends Module {
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
   public final StringSetting ign = this.addSetting(new StringSetting("Skin IGN", "Username whose skin is applied", "epsteinclient", 16, "Type a username…"));
   public final ModeSetting applyTo = this.addSetting(new ModeSetting("Apply To", "Whose skin gets replaced", "Everyone", "Everyone", "Others", "Self"));
   private volatile SkinTextures replacement;
   private volatile String fetchedFor = "";
   private volatile boolean fetching;

   public SkinProtectModule() {
      super("SkinProtect", "Replaces skins so clips can't dox skins", Category.MISC);
   }

   @Override
   protected void onEnable() {
      if (this.replacement == null) {
         this.fetchedFor = "";
      }

      this.ensureFetched();
   }

   @Override
   public void onTick() {
      this.ensureFetched();
   }

   public SkinTextures replacementSkin() {
      return this.replacement;
   }

   public boolean shouldReplace(UUID uuid) {
      if (uuid == null) {
         return false;
      } else {
         MinecraftClient client = MinecraftClient.getInstance();
         UUID uuid2 = client.player == null ? null : client.player.getUuid();
         if (this.applyTo.is("Everyone")) {
            return true;
         } else {
            return this.applyTo.is("Self") ? uuid2 != null && uuid2.equals(uuid) : uuid2 == null || !uuid2.equals(uuid);
         }
      }
   }

   private void ensureFetched() {
      String trimmed = this.ign.get().trim();
      if (!trimmed.isEmpty() && !this.fetching && !trimmed.equalsIgnoreCase(this.fetchedFor)) {
         this.fetching = true;
         this.fetchedFor = trimmed;
         CompletableFuture.runAsync(() -> this.resolve(trimmed)).whenComplete((arg, arg2) -> this.fetching = false);
      }
   }

   private void resolve(String trimmed) {
      try {
         JsonObject jsonObject = getJson("https://api.mojang.com/users/profiles/minecraft/" + trimmed);
         if (jsonObject == null || !jsonObject.has("id")) {
            SixSevenClient.LOGGER.warn("(\u007f#\u0007|\u0094¾\u0085½ĞĊėĞǐǆǚǵȕȞɂȲ˙ʀˑʾ̗̝̌͌ϕίϖ\u03a2π", trimmed);
            return;
         }

         UUID uuid = dashify(jsonObject.get("id").getAsString());
         String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : trimmed;
         String text = uuid.toString();
         String text2 = "^";
         JsonObject jsonObject2 = getJson("https://sessionserver.mojang.com/session/minecraft/profile/" + text.replace(text2, "") + "?unsigned=false");
         if (jsonObject2 == null || !jsonObject2.has("properties")) {
            SixSevenClient.LOGGER.warn("(\u007f#\u0007|\u0094¾\u0085½ĞĊėĞǐǝǛƾȋȃɚȺʐʙˇ˻̜͕̊̄υϧΓΪΝщСНЭӒҪ", trimmed);
            return;
         }

         LinkedHashMultimap linkedHashMultimap = LinkedHashMultimap.create();

         for (JsonElement jsonElement : jsonObject2.getAsJsonArray("properties")) {
            JsonObject jsonObject3 = jsonElement.getAsJsonObject();
            if ("textures".equals(jsonObject3.get("name").getAsString())) {
               String json = jsonObject3.get("value").getAsString();
               String json2 = jsonObject3.has("signature") ? jsonObject3.get("signature").getAsString() : null;
               linkedHashMultimap.put("textures", json2 == null ? new Property("textures", json) : new Property("textures", json, json2));
            }
         }

         GameProfile gameProfile = new GameProfile(uuid, name);
         gameProfile.properties().putAll(linkedHashMultimap);
         MinecraftClient.getInstance().getSkinProvider().fetchSkinTextures(gameProfile).thenAccept(arg -> {
            if (arg.isPresent()) {
               this.replacement = (SkinTextures)arg.get();
               SixSevenClient.LOGGER.info("(\u007f#\u0007|\u0094¾\u0085½ĞĊėĞǐǟǛǿȟȔɑɼʊʞˋʵ̟͓̓͞ΐϮ\u038b", name);
            } else {
               SixSevenClient.LOGGER.warn("(\u007f#\u0007|\u0094¾\u0085½ĞĊėĞǐǐǛǫȗȕȕȲʖʁʂʷ̘̘̑́σϾΟηΝћЫЗѹӜҥӢҡՃիՊԌן\u05fb", name);
            }
         });
      } catch (Exception ex) {
         SixSevenClient.LOGGER.warn("(\u007f#\u0007|\u0094¾\u0085½ĞĊėĞǐǕǕǷȗȔɑɼʍʚʂʽ̛̟͉̍ΐϦΝΰϓЏШЀѿ҉ҬӺһԅտՅ", trimmed, ex.toString());
      }
   }

   private static JsonObject getJson(String trimmed) throws Exception {
      HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(trimmed)).timeout(Duration.ofSeconds(8L)).header("Accept", "application/json").GET().build();
      HttpResponse httpResponse = HTTP.send(httpRequest, BodyHandlers.ofString());
      return httpResponse.statusCode() == 200 && httpResponse.body() != null && !((String)httpResponse.body()).isBlank()
         ? JsonParser.parseString((String)httpResponse.body()).getAsJsonObject()
         : null;
   }

   private static UUID dashify(String trimmed) {
      return UUID.fromString(trimmed.replaceFirst("[p?\u0015*¹åÂ\u0095ČĒŗľǙƻǨǩȀɅɈɵˑʩ˕ʠ͕͊̄̉ϬϢ\u038dϨΏђѧ", "$1-$2-$3-$4-$5"));
   }
}
