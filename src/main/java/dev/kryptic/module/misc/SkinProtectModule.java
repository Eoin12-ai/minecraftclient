package dev.kryptic.module.misc;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import dev.kryptic.KrypticClient;
import dev.kryptic.module.Category;
import dev.kryptic.module.Module;
import dev.kryptic.settings.ModeSetting;
import dev.kryptic.settings.StringSetting;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.SkinTextures;

public class SkinProtectModule extends Module {
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
   public final StringSetting ign = this.addSetting(new StringSetting("Skin IGN", "Username whose skin is applied", "krypticclient", 16, "Type a username…"));
   public final ModeSetting applyTo = this.addSetting(new ModeSetting("Apply To", "Whose skin gets replaced", "Everyone", "Everyone", "Others", "Self"));
   private volatile SkinTextures replacement;
   private volatile String fetchedFor = "";
   private volatile boolean fetching;

   public SkinProtectModule() {
      super("Skin Protect", "Replaces skins so clips can't dox skins", Category.MISC);
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
            KrypticClient.LOGGER.warn("SkinProtect: no Mojang profile found for '{}'", trimmed);
            return;
         }

         UUID uuid = dashify(jsonObject.get("id").getAsString());
         String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : trimmed;
         String text = uuid.toString();
         String text2 = "^";
         JsonObject jsonObject2 = getJson("https://sessionserver.mojang.com/session/minecraft/profile/" + text.replace(text2, "") + "?unsigned=false");
         if (jsonObject2 == null || !jsonObject2.has("properties")) {
            KrypticClient.LOGGER.warn("SkinProtect: session server returned no properties for '{}'", trimmed);
            return;
         }

         Multimap<String, Property> properties = LinkedHashMultimap.create();

         for (JsonElement jsonElement : jsonObject2.getAsJsonArray("properties")) {
            JsonObject jsonObject3 = jsonElement.getAsJsonObject();
            if ("textures".equals(jsonObject3.get("name").getAsString())) {
               String json = jsonObject3.get("value").getAsString();
               String json2 = jsonObject3.has("signature") ? jsonObject3.get("signature").getAsString() : null;
               properties.put("textures", json2 == null ? new Property("textures", json) : new Property("textures", json, json2));
            }
         }

         // GameProfile is a record now, so the properties go in at construction
         GameProfile gameProfile = new GameProfile(uuid, name, new PropertyMap(properties));
         MinecraftClient.getInstance().getSkinProvider().fetchSkinTextures(gameProfile).thenAccept(skinTextures -> {
             if (skinTextures != null) {
                this.replacement = skinTextures.orElse(null);
               KrypticClient.LOGGER.info("SkinProtect: loaded replacement skin from {}", name);
            } else {
               KrypticClient.LOGGER.warn("SkinProtect: {} has no skin texture to borrow", name);
            }
         });
      } catch (Exception ex) {
         KrypticClient.LOGGER.warn("(\u007f#\u0007|\u0094Â¾\u0085Â½ÄÄÄÄÇÇÇÇ·ÈÈÉÉ¼ÊÊÊÊ½ÌÌÌÍÎÏ¦ÎÎ°ÏÐÐ¨ÐÑ¿ÒÒ¬ÓºÒ»ÔÕ¿Õ", trimmed, ex.toString());
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
      return UUID.fromString(trimmed.replaceFirst("[p?\u0015*Â¹Ã¥Ã\u0095ÄÄÅÄ¾ÇÆ»Ç¨Ç©ÈÉÉÉµËÊ©ËÊ ÍÌÍÌÏ¬Ï¢\u038dÏ¨ÎÑÑ§", "$1-$2-$3-$4-$5"));
   }
}
