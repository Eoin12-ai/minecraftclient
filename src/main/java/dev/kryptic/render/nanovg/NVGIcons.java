package dev.kryptic.render.nanovg;

import dev.kryptic.KrypticClient;
import dev.kryptic.module.Category;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.nanovg.NSVGImage;
import org.lwjgl.nanovg.NanoSVG;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryUtil;

public final class NVGIcons {
   private static final int RASTER_SIZE = 64;
   private static final Map<String, Integer> ICONS = new HashMap<>();
   private static boolean loaded;

   private NVGIcons() {
   }

   public static int get(Category category) {
      ensureLoaded();
      return ICONS.getOrDefault(category.getIconId(), -1);
   }

   private static void ensureLoaded() {
      if (!loaded) {
         loaded = true;

         for (Category category : Category.values()) {
            int n = loadSvg("assets/krypticclient/icons/" + category.getIconId() + ".svg");
            if (n > 0) {
               ICONS.put(category.getIconId(), n);
            }
         }
      }
   }

   private static int loadSvg(String str) {
      long l = NVGRenderer.get().ctx();

      try {
         int temp;
         try (InputStream inputStream = NVGIcons.class.getClassLoader().getResourceAsStream(str)) {
            if (inputStream == null) {
               KrypticClient.LOGGER.warn("Icon resource missing: {}", str);
               return -1;
            }

            String text3 = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            text3 = text3.replaceAll("#[0-9a-fA-F]{6}", "#ffffff")
               .replaceAll("#[0-9a-fA-F]{3}\\b", "#fff")
               .replace("currentColor", "#ffffff")
               .replace("\"black\"", "\"white\"");
            ByteBuffer byteBuffer = MemoryUtil.memUTF8(text3, true);
            ByteBuffer byteBuffer2 = MemoryUtil.memASCII("px");
            NSVGImage nSVGImage = null;
            long l3 = 0L;
            ByteBuffer byteBuffer3 = null;

            try {
               nSVGImage = NanoSVG.nsvgParse(byteBuffer, byteBuffer2, 96.0F);
               if (nSVGImage == null) {
                  return -1;
               }

               l3 = NanoSVG.nsvgCreateRasterizer();
               float f = 64.0F / Math.max(nSVGImage.width(), nSVGImage.height());
               byteBuffer3 = MemoryUtil.memAlloc(16384);
               NanoSVG.nsvgRasterize(l3, nSVGImage, 0.0F, 0.0F, f, byteBuffer3, 64, 64, 256);
               temp = NanoVG.nvgCreateImageRGBA(l, 64, 64, 0, byteBuffer3);
            } finally {
               if (byteBuffer3 != null) {
                  MemoryUtil.memFree(byteBuffer3);
               }

               if (l3 != 0L) {
                  NanoSVG.nsvgDeleteRasterizer(l3);
               }

               if (nSVGImage != null) {
                  NanoSVG.nsvgDelete(nSVGImage);
               }

               MemoryUtil.memFree(byteBuffer);
               MemoryUtil.memFree(byteBuffer2);
            }
         }

         return temp;
      } catch (Exception ex) {
         KrypticClient.LOGGER.error("Failed to rasterize icon {}", str, ex);
         return -1;
      }
   }
}
