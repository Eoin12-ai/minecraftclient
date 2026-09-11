package dev.kryptic.render.nanovg;

import dev.kryptic.KrypticClient;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.system.MemoryUtil;

public final class NVGImages {
   private static final Map<Identifier, Integer> RESOURCE_CACHE = new HashMap<>();
   private static final Map<Integer, Integer> GL_HANDLE_CACHE = new HashMap<>();
   private static int fileImageHandle = -1;
   private static int fileImageVersion = -1;
   private static Path fileImagePath;

   private NVGImages() {
   }

   /**
    * Never throws. A missing or broken image returns -1 and the caller draws
    * its fallback; letting one propagate would reach the overlay's catch-all
    * and disable the whole overlay for the session over a decoration.
    */
   public static int fromResource(Identifier id) {
      try {
         return RESOURCE_CACHE.computeIfAbsent(id, NVGImages::loadResource);
      } catch (Throwable error) {
         KrypticClient.LOGGER.warn("Image {} could not be loaded", id, error);
         return -1;
      }
   }

   private static int loadResource(Identifier id) {
      long l = NVGRenderer.get().ctx();
      Optional opt = MinecraftClient.getInstance().getResourceManager().getResource(id);
      if (opt.isEmpty()) {
         return -1;
      } else {
         try {
            int temp;
            try (InputStream inputStream = ((Resource)opt.get()).getInputStream()) {
               byte[] b = inputStream.readAllBytes();
               ByteBuffer byteBuffer = MemoryUtil.memAlloc(b.length);

               try {
                  byteBuffer.put(b).flip();
                  temp = NanoVG.nvgCreateImageMem(l, 32, byteBuffer);
               } finally {
                  MemoryUtil.memFree(byteBuffer);
               }
            }

            return temp;
         } catch (Exception ex) {
            KrypticClient.LOGGER.warn("Failed to load image {}", id, ex);
            return -1;
         }
      }
   }

   public static int fromFile(Path path, int n) {
      if (n == fileImageVersion && path.equals(fileImagePath)) {
         return fileImageHandle;
      } else {
         long l = NVGRenderer.get().ctx();
         if (fileImageHandle > 0) {
            NanoVG.nvgDeleteImage(l, fileImageHandle);
            fileImageHandle = -1;
         }

         fileImageVersion = n;
         fileImagePath = path;

         try {
            byte[] b = Files.readAllBytes(path);
            ByteBuffer byteBuffer = MemoryUtil.memAlloc(b.length);

            try {
               byteBuffer.put(b).flip();
               fileImageHandle = NanoVG.nvgCreateImageMem(l, 0, byteBuffer);
            } finally {
               MemoryUtil.memFree(byteBuffer);
            }
         } catch (Exception ex) {
            fileImageHandle = -1;
         }

         return fileImageHandle;
      }
   }

   private static int getTextureId(net.minecraft.client.texture.AbstractTexture t) {
      try { java.lang.reflect.Method m = t.getClass().getMethod("getGlId"); return (int)m.invoke(t); } catch (Exception e) { return 0; }
   }

   public static int wrapGlTexture(Identifier id, int n, int localX) {
      AbstractTexture client = MinecraftClient.getInstance().getTextureManager().getTexture(id);
      if (client != null) {
         int localZ = getTextureId(client);
         return GL_HANDLE_CACHE.computeIfAbsent(localZ, arg -> NanoVGGL3.nvglCreateImageFromHandle(NVGRenderer.get().ctx(), arg, n, localX, 65536));
      } else {
         return -1;
      }
   }

   public static int createDynamic(int n, int localX) {
      long l = NVGRenderer.get().ctx();
      ByteBuffer byteBuffer = MemoryUtil.memCalloc(n * localX * 4);

      int localZ;
      try {
         localZ = NanoVG.nvgCreateImageRGBA(l, n, localX, 32, byteBuffer);
      } finally {
         MemoryUtil.memFree(byteBuffer);
      }

      return localZ;
   }

   public static void updateDynamic(int n, ByteBuffer byteBuffer) {
      if (n > 0) {
         NanoVG.nvgUpdateImage(NVGRenderer.get().ctx(), n, byteBuffer);
      }
   }

   public static void deleteImage(int n) {
      if (n > 0) {
         NanoVG.nvgDeleteImage(NVGRenderer.get().ctx(), n);
      }
   }

   public static void drawSubImage(
      NVGRenderer nVGRenderer,
      int n,
      float f,
      float f14,
      float f15,
      float f16,
      float f17,
      float f18,
      float f19,
      float f20,
      float f21,
      float f22,
      float f23
   ) {
      if (n > 0) {
         float f24 = f21 / (f17 - f15);
         float f25 = f22 / (f18 - f16);
         nVGRenderer.imagePattern(n, f19 - f15 * f24, f20 - f16 * f25, f * f24, f14 * f25, f19, f20, f21, f22, f23);
      }
   }
}
