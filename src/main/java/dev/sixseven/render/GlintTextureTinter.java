package dev.sixseven.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import dev.sixseven.SixSevenClient;
import dev.sixseven.module.misc.CustomGlintModule;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public final class GlintTextureTinter {
   private static final Identifier[] TEXTURES = new Identifier[]{ItemRenderer.ITEM_ENCHANTMENT_GLINT, ItemRenderer.ENTITY_ENCHANTMENT_GLINT};
   private static final NativeImage[] originals = new NativeImage[TEXTURES.length];
   private static final NativeImage[] scratch = new NativeImage[TEXTURES.length];
   private static final GpuTexture[] lastUploaded = new GpuTexture[TEXTURES.length];
   private static final Map<String, NativeImage> customCache = new HashMap<>();
   private static boolean loaded;
   private static boolean written;
   private static String lastKey;

   private GlintTextureTinter() {
   }

   public static void tick() {
      CustomGlintModule customGlintModule = SixSevenClient.modules() == null ? null : SixSevenClient.modules().customGlint;
      if (customGlintModule != null) {
         if (customGlintModule.isActive()) {
            if (customGlintModule.usesTexture()) {
               applyTexture(customGlintModule.textureName(), customGlintModule.strengthUnit());
            } else {
               applyTint(customGlintModule.glintColor());
            }
         } else if (written) {
            restore();
         }
      }
   }

   private static void ensureLoaded() {
      if (!loaded) {
         loaded = true;
         MinecraftClient client = MinecraftClient.getInstance();

         for (int n = 0; n < TEXTURES.length; n++) {
            Optional opt = client.getResourceManager().getResource(TEXTURES[n]);
            if (!opt.isEmpty()) {
               try (InputStream inputStream = ((Resource)opt.get()).getInputStream()) {
                  originals[n] = NativeImage.read(inputStream);
               } catch (Exception ex) {
                  originals[n] = null;
               }
            }
         }
      }
   }

   private static void applyTint(int n) {
      ensureLoaded();
      upload("tint:" + n, arg -> tintInto(arg, originals[arg], n));
   }

   private static void applyTexture(String str, float f) {
      ensureLoaded();
      NativeImage nativeImage = loadCustom(str);
      if (nativeImage != null) {
         int n = Math.round(f * 255.0F);
         upload("tex:" + str + ":" + n, arg -> sampleInto(arg, nativeImage, f));
      }
   }

   private static void upload(String str, GlintTextureTinter.Source source) {
      TextureManager client = MinecraftClient.getInstance().getTextureManager();
      boolean found = false;

      for (int n = 0; n < TEXTURES.length; n++) {
         AbstractTexture abstractTexture = client.getTexture(TEXTURES[n]);
         if (abstractTexture != null && abstractTexture.getGlTexture() != lastUploaded[n]) {
            found = true;
         }
      }

      if (!written || !str.equals(lastKey) || found) {
         for (int offset = 0; offset < TEXTURES.length; offset++) {
            if (originals[offset] != null) {
               AbstractTexture abstractTexture2 = client.getTexture(TEXTURES[offset]);
               GpuTexture gpuTexture = abstractTexture2 == null ? null : abstractTexture2.getGlTexture();
               if (gpuTexture != null) {
                  NativeImage nativeImage = source.build(offset);
                  if (nativeImage != null) {
                     RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpuTexture, nativeImage);
                     lastUploaded[offset] = gpuTexture;
                  }
               }
            }
         }

         written = true;
         lastKey = str;
      }
   }

   private static void restore() {
      TextureManager client = MinecraftClient.getInstance().getTextureManager();

      for (int n = 0; n < TEXTURES.length; n++) {
         NativeImage nativeImage = originals[n];
         if (nativeImage != null) {
            AbstractTexture abstractTexture = client.getTexture(TEXTURES[n]);
            GpuTexture gpuTexture = abstractTexture == null ? null : abstractTexture.getGlTexture();
            if (gpuTexture != null) {
               RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpuTexture, nativeImage);
               lastUploaded[n] = gpuTexture;
            }
         }
      }

      written = false;
      lastKey = null;
   }

   private static NativeImage tintInto(int n, NativeImage nativeImage, int n12) {
      int n13 = nativeImage.getWidth();
      int n14 = nativeImage.getHeight();
      NativeImage nativeImage2 = ensureScratch(n, n13, n14);
      int n15 = ColorHelper.getRed(n12);
      int n16 = ColorHelper.getGreen(n12);
      int n17 = ColorHelper.getBlue(n12);

      for (int n18 = 0; n18 < n14; n18++) {
         for (int n19 = 0; n19 < n13; n19++) {
            int n20 = nativeImage.getColorArgb(n19, n18);
            int n21 = Math.max(ColorHelper.getRed(n20), Math.max(ColorHelper.getGreen(n20), ColorHelper.getBlue(n20)));
            nativeImage2.setColorArgb(
               n19, n18, ColorHelper.getArgb(ColorHelper.getAlpha(n20), n15 * n21 / 255, n16 * n21 / 255, n17 * n21 / 255)
            );
         }
      }

      return nativeImage2;
   }

   private static NativeImage sampleInto(int n, NativeImage nativeImage, float f) {
      int n12 = originals[n].getWidth();
      int n13 = originals[n].getHeight();
      int n14 = nativeImage.getWidth();
      int n15 = nativeImage.getHeight();
      NativeImage nativeImage2 = ensureScratch(n, n12, n13);

      for (int n16 = 0; n16 < n13; n16++) {
         float f4 = ((float)n16 + 0.5F) * (float)n15 / (float)n13 - 0.5F;

         for (int n17 = 0; n17 < n12; n17++) {
            float f5 = ((float)n17 + 0.5F) * (float)n14 / (float)n12 - 0.5F;
            int n18 = bilinearWrapped(nativeImage, f5, f4, n14, n15);
            int n19 = Math.round((float)ColorHelper.getRed(n18) * f);
            int n20 = Math.round((float)ColorHelper.getGreen(n18) * f);
            int n21 = Math.round((float)ColorHelper.getBlue(n18) * f);
            nativeImage2.setColorArgb(n17, n16, ColorHelper.getArgb(ColorHelper.getAlpha(n18), n19, n20, n21));
         }
      }

      return nativeImage2;
   }

   private static NativeImage ensureScratch(int n, int localX, int localZ) {
      NativeImage nativeImage = scratch[n];
      if (nativeImage == null || nativeImage.getWidth() != localX || nativeImage.getHeight() != localZ) {
         if (nativeImage != null) {
            nativeImage.close();
         }

         nativeImage = new NativeImage(localX, localZ, false);
         scratch[n] = nativeImage;
      }

      return nativeImage;
   }

   private static int bilinearWrapped(NativeImage nativeImage, float f, float f5, int n, int n15) {
      int n16 = Math.floorMod((int)Math.floor((double)f), n);
      int n17 = Math.floorMod((int)Math.floor((double)f5), n15);
      int n18 = (n16 + 1) % n;
      int n19 = (n17 + 1) % n15;
      float f6 = f - (float)Math.floor((double)f);
      float f7 = f5 - (float)Math.floor((double)f5);
      int n20 = nativeImage.getColorArgb(n16, n17);
      int n21 = nativeImage.getColorArgb(n18, n17);
      int n22 = nativeImage.getColorArgb(n16, n19);
      int n23 = nativeImage.getColorArgb(n18, n19);
      int n24 = mix(
         ColorHelper.getAlpha(n20), ColorHelper.getAlpha(n21), ColorHelper.getAlpha(n22), ColorHelper.getAlpha(n23), f6, f7
      );
      int n25 = mix(
         ColorHelper.getRed(n20), ColorHelper.getRed(n21), ColorHelper.getRed(n22), ColorHelper.getRed(n23), f6, f7
      );
      int n26 = mix(
         ColorHelper.getGreen(n20), ColorHelper.getGreen(n21), ColorHelper.getGreen(n22), ColorHelper.getGreen(n23), f6, f7
      );
      int n27 = mix(
         ColorHelper.getBlue(n20), ColorHelper.getBlue(n21), ColorHelper.getBlue(n22), ColorHelper.getBlue(n23), f6, f7
      );
      return ColorHelper.getArgb(n24, n25, n26, n27);
   }

   private static int mix(int n, int localZ, int localY, int step, float f, float f5) {
      float f6 = (float)n + (float)(localZ - n) * f;
      float f7 = (float)localY + (float)(step - localY) * f;
      return Math.round(f6 + (f7 - f6) * f5);
   }

   private static NativeImage loadCustom(String str) {
      if (customCache.containsKey(str)) {
         return customCache.get(str);
      } else {
         NativeImage nativeImage = null;
         Identifier id = Identifier.of("sixsevenclient", "textures/misc/glints/" + str + ".png");
         Optional opt = MinecraftClient.getInstance().getResourceManager().getResource(id);
         if (opt.isPresent()) {
            try (InputStream inputStream = ((Resource)opt.get()).getInputStream()) {
               nativeImage = NativeImage.read(inputStream);
            } catch (Exception ex) {
               nativeImage = null;
            }
         }

         customCache.put(str, nativeImage);
         return nativeImage;
      }
   }

   private interface Source {
      NativeImage build(int n);
   }
}
