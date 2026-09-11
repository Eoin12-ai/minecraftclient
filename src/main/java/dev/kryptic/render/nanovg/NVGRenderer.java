package dev.kryptic.render.nanovg;

import dev.kryptic.KrypticClient;
import dev.kryptic.util.Colors;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class NVGRenderer {
   public static final String FONT_XUONG = "xuong";
   public static final String FONT_VANILLA = "vanilla";
   public static final String FONT_TEN = "ten";
   public static final String FONT_MONO = "mono";
   public static final String FONT_BOLD = "bold";
   public static final String FONT_KRYPTIC = "kryptic";
   private static NVGRenderer instance;
   private final long ctx = NanoVGGL3.nvgCreate(1);
   private boolean xuongLoaded;
   private boolean vanillaLoaded;
   private boolean tenLoaded;
   private boolean monoLoaded;
   private boolean boldLoaded;
   private boolean krypticLoaded;
   private final List<ByteBuffer> retainedFontData = new ArrayList<>();
   private String activeFont = "kryptic";
   private final ArrayDeque<Float> alphaStack = new ArrayDeque<>();
   private float appliedAlpha = 1.0F;

   private NVGRenderer() {
      if (this.ctx == 0L) {
         throw new IllegalStateException("Failed to create NanoVG context");
      } else {
         this.xuongLoaded = this.loadFont("xuong", "assets/krypticclient/fonts/Xuong-Regular.ttf");
         this.vanillaLoaded = this.loadFont("vanilla", "assets/krypticclient/fonts/Monocraft.ttf");
         this.tenLoaded = this.loadFont("ten", "assets/krypticclient/fonts/MinecraftTen.ttf");
         this.monoLoaded = this.loadFont("mono", "assets/krypticclient/fonts/JetBrainsMono-Regular.ttf");
         this.boldLoaded = this.loadFont("bold", "assets/krypticclient/fonts/Inter-Bold.ttf");
         this.krypticLoaded = this.loadFont("kryptic", "assets/krypticclient/fonts/Kryptic-Regular.ttf");
         if (!this.xuongLoaded && !this.vanillaLoaded && !this.tenLoaded
               && !this.monoLoaded && !this.boldLoaded && !this.krypticLoaded) {
            KrypticClient.LOGGER.error(
                  "No font loaded from assets/krypticclient/fonts/ - the menu and HUD "
                + "will draw shapes but no text.");
         }

         if (this.vanillaLoaded) {
            // Monocraft carries the glyphs the display faces do not. Xuong is
            // broad but not complete, and Minecraft Ten is a logo face with 99
            // codepoints — it has no middot and no infinity sign, both of which
            // the HUD uses, so without a fallback those draw as blanks.
            if (this.xuongLoaded) NanoVG.nvgAddFallbackFont(this.ctx, "xuong", "vanilla");
            if (this.tenLoaded) NanoVG.nvgAddFallbackFont(this.ctx, "ten", "vanilla");
            if (this.monoLoaded) NanoVG.nvgAddFallbackFont(this.ctx, "mono", "vanilla");
            if (this.boldLoaded) NanoVG.nvgAddFallbackFont(this.ctx, "bold", "vanilla");
            if (this.krypticLoaded) NanoVG.nvgAddFallbackFont(this.ctx, "kryptic", "vanilla");
         }
      }
   }

   /** Class loader first, then the resource manager. */
   private static InputStream openFont(String path) {
      InputStream direct = NVGRenderer.class.getClassLoader().getResourceAsStream(path);
      if (direct != null) return direct;
      try {
         // assets/<namespace>/<rest> -> Identifier(namespace, rest)
         String[] parts = path.split("/", 3);
         if (parts.length == 3 && parts[0].equals("assets")) {
            var opt = net.minecraft.client.MinecraftClient.getInstance()
                  .getResourceManager()
                  .getResource(net.minecraft.util.Identifier.of(parts[1], parts[2]));
            if (opt.isPresent()) return opt.get().getInputStream();
         }
      } catch (Throwable ignored) {
         // the resource manager may not be up yet; the class loader was the
         // real attempt and this is only the second chance
      }
      return null;
   }

   public static NVGRenderer get() {
      if (instance == null) {
         instance = new NVGRenderer();
      }

      return instance;
   }

   public long ctx() {
      return this.ctx;
   }

   /**
    * Reads a bundled face, trying both ways a mod's own assets can be reached.
    *
    * The class loader is the usual route, but it is not guaranteed: how a mod
    * jar sits on the classpath varies with the launcher, and when it fails
    * here, every face fails together -- which used to skip the whole NanoVG
    * frame in silence. Minecraft's resource manager reaches the same file by a
    * different mechanism, so one of the two answers.
    */
   private boolean loadFont(String str, String text3) {
      try {
         boolean found;
         try (InputStream inputStream = openFont(text3)) {
            if (inputStream == null) {
               KrypticClient.LOGGER.warn("Font resource missing: {}", text3);
               return false;
            }

            byte[] b = inputStream.readAllBytes();
            ByteBuffer byteBuffer = MemoryUtil.memAlloc(b.length);
            byteBuffer.put(b).flip();
            int n = NanoVG.nvgCreateFontMem(this.ctx, str, byteBuffer, false);
            if (n == -1) {
               MemoryUtil.memFree(byteBuffer);
               KrypticClient.LOGGER.warn("NanoVG rejected font {}", text3);
               return false;
            }

            this.retainedFontData.add(byteBuffer);
            found = true;
         }

         return found;
      } catch (IOException ex) {
         KrypticClient.LOGGER.error("Failed to load font {}", text3, ex);
         return false;
      }
   }

   /**
    * Picks the face for the chosen mode, falling through to whatever did load.
    *
    * A font that failed to load must never become the active face — NanoVG
    * silently draws nothing for an unknown face name, which looks like the
    * whole overlay has broken rather than like one missing file.
    */
   public void setFontMode(String str) {
      String wanted = switch (str == null ? "" : str) {
         case "Xuong" -> "xuong";
         case "Ten" -> "ten";
         case "Mono" -> "mono";
         case "Vanilla" -> "vanilla";
         case "Bold" -> "bold";
         default -> "kryptic";
      };
      if (isLoaded(wanted)) {
         this.activeFont = wanted;
         return;
      }
      for (String candidate : new String[]{"kryptic", "bold", "vanilla", "xuong", "mono", "ten"}) {
         if (isLoaded(candidate)) {
            this.activeFont = candidate;
            return;
         }
      }
      this.activeFont = null;
   }

   private boolean isLoaded(String face) {
      return switch (face) {
         case "xuong" -> this.xuongLoaded;
         case "vanilla" -> this.vanillaLoaded;
         case "ten" -> this.tenLoaded;
         case "mono" -> this.monoLoaded;
         case "bold" -> this.boldLoaded;
         case "kryptic" -> this.krypticLoaded;
         default -> false;
      };
   }

   public boolean hasFont() {
      return this.activeFont != null;
   }

   public void beginFrame(float f, float f4, float f5) {
      this.alphaStack.clear();
      this.appliedAlpha = 1.0F;
      NanoVG.nvgBeginFrame(this.ctx, f, f4, f5);
   }

   public void endFrame() {
      NanoVG.nvgEndFrame(this.ctx);
   }

   public void save() {
      this.alphaStack.push(this.appliedAlpha);
      NanoVG.nvgSave(this.ctx);
   }

   public void restore() {
      if (!this.alphaStack.isEmpty()) {
         this.appliedAlpha = this.alphaStack.pop();
      }

      NanoVG.nvgRestore(this.ctx);
   }

   public void scale(float f) {
      NanoVG.nvgScale(this.ctx, f, f);
   }

   public void translate(float f, float f3) {
      NanoVG.nvgTranslate(this.ctx, f, f3);
   }

   public void alpha(float f) {
      this.appliedAlpha = this.appliedAlpha * Math.clamp(f, 0.0F, 1.0F);
      NanoVG.nvgGlobalAlpha(this.ctx, this.appliedAlpha);
   }

   public void scissor(float f, float f5, float f6, float f7) {
      NanoVG.nvgIntersectScissor(this.ctx, f, f5, f6, f7);
   }

   public void rect(float f, float f6, float f7, float f8, float f9, int n) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgRoundedRect(this.ctx, f, f6, f7, f8, f9);
         NanoVG.nvgFillColor(this.ctx, color(memoryStack, n));
         NanoVG.nvgFill(this.ctx);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   public void rectGradient(float f, float f8, float f9, float f10, float f11, int n, int offset, boolean value) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NVGPaint nVGPaint = NVGPaint.malloc(memoryStack);
         float f12 = value ? f : f + f9;
         float f13 = value ? f8 + f10 : f8;
         NanoVG.nvgLinearGradient(this.ctx, f, f8, f12, f13, color(memoryStack, n), color(memoryStack, offset), nVGPaint);
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgRoundedRect(this.ctx, f, f8, f9, f10, f11);
         NanoVG.nvgFillPaint(this.ctx, nVGPaint);
         NanoVG.nvgFill(this.ctx);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   public void rectVaryingGradient(float f, float f9, float f10, float f11, float f12, float f13, float f14, float f15, int n, int offset) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NVGPaint nVGPaint = NVGPaint.malloc(memoryStack);
         NanoVG.nvgLinearGradient(this.ctx, f, f9, f, f9 + f11, color(memoryStack, n), color(memoryStack, offset), nVGPaint);
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgRoundedRectVarying(this.ctx, f, f9, f10, f11, f12, f13, f14, f15);
         NanoVG.nvgFillPaint(this.ctx, nVGPaint);
         NanoVG.nvgFill(this.ctx);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   public void chevron(float f, float f5, float f6, float f7, int n, boolean value) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         if (value) {
            NanoVG.nvgMoveTo(this.ctx, f - f6, f5 - f6 / 2.0F);
            NanoVG.nvgLineTo(this.ctx, f, f5 + f6 / 2.0F);
            NanoVG.nvgLineTo(this.ctx, f + f6, f5 - f6 / 2.0F);
         } else {
            NanoVG.nvgMoveTo(this.ctx, f - f6 / 2.0F, f5 - f6);
            NanoVG.nvgLineTo(this.ctx, f + f6 / 2.0F, f5);
            NanoVG.nvgLineTo(this.ctx, f - f6 / 2.0F, f5 + f6);
         }

         NanoVG.nvgStrokeColor(this.ctx, color(memoryStack, n));
         NanoVG.nvgStrokeWidth(this.ctx, f7);
         NanoVG.nvgLineCap(this.ctx, 1);
         NanoVG.nvgLineJoin(this.ctx, 1);
         NanoVG.nvgStroke(this.ctx);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   public void triangle(float f, float f7, float f8, float f9, float f10, float f11, int n) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgMoveTo(this.ctx, f, f7);
         NanoVG.nvgLineTo(this.ctx, f8, f9);
         NanoVG.nvgLineTo(this.ctx, f10, f11);
         NanoVG.nvgClosePath(this.ctx);
         NanoVG.nvgFillColor(this.ctx, color(memoryStack, n));
         NanoVG.nvgFill(this.ctx);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   public void rectOutline(float f, float f7, float f8, float f9, float f10, float f11, int n) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgRoundedRect(this.ctx, f + f11 / 2.0F, f7 + f11 / 2.0F, f8 - f11, f9 - f11, f10);
         NanoVG.nvgStrokeColor(this.ctx, color(memoryStack, n));
         NanoVG.nvgStrokeWidth(this.ctx, f11);
         NanoVG.nvgStroke(this.ctx);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   public void glow(float f, float f7, float f8, float f9, float f10, float f11, int n) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NVGPaint nVGPaint = NVGPaint.malloc(memoryStack);
         NanoVG.nvgBoxGradient(this.ctx, f, f7, f8, f9, f10, f11 * 2.0F, color(memoryStack, n), color(memoryStack, Colors.withAlpha(n, 0)), nVGPaint);
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgRoundedRect(this.ctx, f - f11, f7 - f11, f8 + f11 * 2.0F, f9 + f11 * 2.0F, f10 + f11);
         NanoVG.nvgFillPaint(this.ctx, nVGPaint);
         NanoVG.nvgFill(this.ctx);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   public void circle(float f, float f4, float f5, int n) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgCircle(this.ctx, f, f4, f5);
         NanoVG.nvgFillColor(this.ctx, color(memoryStack, n));
         NanoVG.nvgFill(this.ctx);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   public void circleGlow(float f, float f5, float f6, float f7, int n) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NVGPaint nVGPaint = NVGPaint.malloc(memoryStack);
         NanoVG.nvgRadialGradient(this.ctx, f, f5, f6 * 0.25F, f6 + f7, color(memoryStack, n), color(memoryStack, Colors.withAlpha(n, 0)), nVGPaint);
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgCircle(this.ctx, f, f5, f6 + f7);
         NanoVG.nvgFillPaint(this.ctx, nVGPaint);
         NanoVG.nvgFill(this.ctx);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   public void line(float f, float f6, float f7, float f8, float f9, int n) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgMoveTo(this.ctx, f, f6);
         NanoVG.nvgLineTo(this.ctx, f7, f8);
         NanoVG.nvgStrokeColor(this.ctx, color(memoryStack, n));
         NanoVG.nvgStrokeWidth(this.ctx, f9);
         NanoVG.nvgLineCap(this.ctx, 1);
         NanoVG.nvgStroke(this.ctx);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   public void checkmark(float f, float f11, float f12, float f13, int n) {
      float f14 = f + f12 * 0.22F;
      float f15 = f11 + f12 * 0.55F;
      float f16 = f + f12 * 0.42F;
      float f17 = f11 + f12 * 0.74F;
      float f18 = f + f12 * 0.78F;
      float f19 = f11 + f12 * 0.3F;
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgMoveTo(this.ctx, f14, f15);
         NanoVG.nvgLineTo(this.ctx, f16, f17);
         NanoVG.nvgLineTo(this.ctx, f18, f19);
         NanoVG.nvgStrokeColor(this.ctx, color(memoryStack, n));
         NanoVG.nvgStrokeWidth(this.ctx, f13);
         NanoVG.nvgLineCap(this.ctx, 1);
         NanoVG.nvgLineJoin(this.ctx, 1);
         NanoVG.nvgStroke(this.ctx);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   public void cross(float f, float f6, float f7, float f8, int n) {
      float f9 = f7 * 0.3F;
      this.line(f + f9, f6 + f9, f + f7 - f9, f6 + f7 - f9, f8, n);
      this.line(f + f7 - f9, f6 + f9, f + f9, f6 + f7 - f9, f8, n);
   }

   public float text(String str, float f, float f4, float f5, int n) {
      return this.text(str, f, f4, f5, n, this.activeFont);
   }

   public float text(String str, float f, float f5, float f6, int n, String text3) {
      if (text3 == null) {
         return 0.0F;
      } else {
         MemoryStack memoryStack = MemoryStack.stackPush();

         float f7;
         try {
            NanoVG.nvgFontFace(this.ctx, text3);
            NanoVG.nvgFontSize(this.ctx, f6);
            NanoVG.nvgTextAlign(this.ctx, 17);
            NanoVG.nvgFillColor(this.ctx, color(memoryStack, n));
            f7 = NanoVG.nvgText(this.ctx, f, f5, str) - f;
         } catch (Throwable temp) {
            if (memoryStack != null) {
               try {
                  memoryStack.close();
               } catch (Throwable ex) {
                  temp.addSuppressed(ex);
               }
            }

            throw temp;
         }

         if (memoryStack != null) {
            memoryStack.close();
         }

         return f7;
      }
   }

   public float textGradient(String str, float f, float f5, float f6, int n, int offset) {
      if (this.activeFont == null) {
         return 0.0F;
      } else {
         MemoryStack memoryStack = MemoryStack.stackPush();

         float f7;
         try {
            NVGPaint nVGPaint = NVGPaint.malloc(memoryStack);
            NanoVG.nvgLinearGradient(this.ctx, f, f5 - f6 / 2.0F, f, f5 + f6 / 2.0F, color(memoryStack, n), color(memoryStack, offset), nVGPaint);
            NanoVG.nvgFontFace(this.ctx, this.activeFont);
            NanoVG.nvgFontSize(this.ctx, f6);
            NanoVG.nvgTextAlign(this.ctx, 17);
            NanoVG.nvgFillPaint(this.ctx, nVGPaint);
            f7 = NanoVG.nvgText(this.ctx, f, f5, str) - f;
         } catch (Throwable temp) {
            if (memoryStack != null) {
               try {
                  memoryStack.close();
               } catch (Throwable ex) {
                  temp.addSuppressed(ex);
               }
            }

            throw temp;
         }

         if (memoryStack != null) {
            memoryStack.close();
         }

         return f7;
      }
   }

   public void textGlow(String str, float f, float f4, float f5, int n) {
      if (this.activeFont != null) {
         MemoryStack memoryStack = MemoryStack.stackPush();

         try {
            NanoVG.nvgFontFace(this.ctx, this.activeFont);
            NanoVG.nvgFontSize(this.ctx, f5);
            NanoVG.nvgTextAlign(this.ctx, 17);
            NanoVG.nvgFontBlur(this.ctx, 4.0F);
            NanoVG.nvgFillColor(this.ctx, color(memoryStack, n));
            NanoVG.nvgText(this.ctx, f, f4, str);
            NanoVG.nvgFontBlur(this.ctx, 0.0F);
         } catch (Throwable temp) {
            if (memoryStack != null) {
               try {
                  memoryStack.close();
               } catch (Throwable ex) {
                  temp.addSuppressed(ex);
               }
            }

            throw temp;
         }

         if (memoryStack != null) {
            memoryStack.close();
         }
      }
   }

   public float textTruncated(String str, float f, float f5, float f6, int n, float f7) {
      if (this.textWidth(str, f6) <= f7) {
         return this.text(str, f, f5, f6, n);
      } else {
         String text3 = str;

         while (text3.length() > 1 && this.textWidth(text3 + "…", f6) > f7) {
            text3 = text3.substring(0, text3.length() - 1);
         }

         return this.text(text3 + "…", f, f5, f6, n);
      }
   }

   public float textWidth(String str, float f) {
      if (this.activeFont == null) {
         return 0.0F;
      } else {
         NanoVG.nvgFontFace(this.ctx, this.activeFont);
         NanoVG.nvgFontSize(this.ctx, f);
         NanoVG.nvgTextAlign(this.ctx, 17);
         return NanoVG.nvgTextBounds(this.ctx, 0.0F, 0.0F, str, (FloatBuffer)null);
      }
   }

   public void rotate(float f) {
      NanoVG.nvgRotate(this.ctx, f);
   }

   public void imagePattern(int n, float f, float f10, float f11, float f12, float f13, float f14, float f15, float f16, float f17) {
      if (n > 0) {
         MemoryStack memoryStack = MemoryStack.stackPush();

         try {
            NVGPaint nVGPaint = NVGPaint.malloc(memoryStack);
            NanoVG.nvgImagePattern(this.ctx, f, f10, f11, f12, 0.0F, n, f17, nVGPaint);
            NanoVG.nvgBeginPath(this.ctx);
            NanoVG.nvgRect(this.ctx, f13, f14, f15, f16);
            NanoVG.nvgFillPaint(this.ctx, nVGPaint);
            NanoVG.nvgFill(this.ctx);
         } catch (Throwable temp) {
            if (memoryStack != null) {
               try {
                  memoryStack.close();
               } catch (Throwable ex) {
                  temp.addSuppressed(ex);
               }
            }

            throw temp;
         }

         if (memoryStack != null) {
            memoryStack.close();
         }
      }
   }

   public void circleOutline(float f, float f5, float f6, float f7, int n) {
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         NanoVG.nvgBeginPath(this.ctx);
         NanoVG.nvgCircle(this.ctx, f, f5, f6);
         NanoVG.nvgStrokeColor(this.ctx, color(memoryStack, n));
         NanoVG.nvgStrokeWidth(this.ctx, f7);
         NanoVG.nvgStroke(this.ctx);
      } catch (Throwable temp) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable ex) {
               temp.addSuppressed(ex);
            }
         }

         throw temp;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   public void image(int n, float f, float f5, float f6, float f7, int offset) {
      if (n > 0) {
         MemoryStack memoryStack = MemoryStack.stackPush();

         try {
            NVGPaint nVGPaint = NVGPaint.malloc(memoryStack);
            NanoVG.nvgImagePattern(this.ctx, f, f5, f6, f7, 0.0F, n, 1.0F, nVGPaint);
            nVGPaint.innerColor(color(memoryStack, offset));
            NanoVG.nvgBeginPath(this.ctx);
            NanoVG.nvgRect(this.ctx, f, f5, f6, f7);
            NanoVG.nvgFillPaint(this.ctx, nVGPaint);
            NanoVG.nvgFill(this.ctx);
         } catch (Throwable temp) {
            if (memoryStack != null) {
               try {
                  memoryStack.close();
               } catch (Throwable ex) {
                  temp.addSuppressed(ex);
               }
            }

            throw temp;
         }

         if (memoryStack != null) {
            memoryStack.close();
         }
      }
   }

   private static NVGColor color(MemoryStack memoryStack, int n) {
      return NanoVG.nvgRGBA((byte)Colors.red(n), (byte)Colors.green(n), (byte)Colors.blue(n), (byte)Colors.alpha(n), NVGColor.malloc(memoryStack));
   }
}
