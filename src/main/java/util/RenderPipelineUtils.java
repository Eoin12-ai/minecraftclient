package util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.pipeline.RenderPipeline.UniformDescription;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.swyzzyaddon.SwyzzyAddon;
import com.swyzzyaddon.mixin.RenderLayerInvoker;
import java.util.Map.Entry;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.util.Identifier;

public final class RenderPipelineUtils {
   private static RenderLayer class1921;
   private static RenderLayer class19212;
   private static boolean bool;

   private RenderPipelineUtils() {
   }

   public static RenderLayer getclass1921() {
      if (class1921 == null && !bool) {
         class1921 = class1921Of("swyzzy_lines_no_depth", RenderPipelines.LINES_TRANSLUCENT, 1536);
      }

      return class1921;
   }

   public static RenderLayer getclass19212() {
      if (class19212 == null && !bool) {
         class19212 = class1921Of("swyzzy_quads_no_depth", RenderPipelines.DEBUG_QUADS, 1536);
      }

      return class19212;
   }

   private static RenderLayer class1921Of(String var0, RenderPipeline var1, int var2) {
      try {
         RenderSetup var3 = RenderSetup.builder(renderPipelineOf(var0, var1)).translucent().expectedBufferSize(var2).build();
         return RenderLayerInvoker.swyzzy$of(var0, var3);
      } catch (Throwable var4) {
         bool = true;
         SwyzzyAddon.logger.warn("Could not build the see-through render layers; ESP falls back to depth tested rendering.", var4);
         return null;
      }
   }

   private static RenderPipeline renderPipelineOf(String var0, RenderPipeline var1) {
      Builder var2 = RenderPipeline.builder(new Snippet[0])
         .withLocation(Identifier.of("swyzzyclient", "pipeline/null"))
         .withVertexShader(var1.getVertexShader())
         .withFragmentShader(var1.getFragmentShader())
         .withVertexFormat(var1.getVertexFormat(), var1.getVertexFormatMode())
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(var1.isCull())
         .withColorWrite(var1.isWriteColor(), var1.isWriteAlpha())
         .withColorLogic(var1.getColorLogic())
         .withPolygonMode(var1.getPolygonMode())
         .withDepthBias(var1.getDepthBiasScaleFactor(), var1.getDepthBiasConstant());
      var1.getBlendFunction().ifPresentOrElse(var2::withBlend, var2::withoutBlend);

      for (String var4 : var1.getSamplers()) {
         var2.withSampler(var4);
      }

      for (UniformDescription var8 : var1.getUniforms()) {
         if (var8.type() != null) {
            if (var8.textureFormat() == null) {
               var2.withUniform(var8.name(), var8.type());
            } else {
               var2.withUniform(var8.name(), var8.type(), var8.textureFormat());
            }
         }
      }

      Defines var7 = var1.getShaderDefines();

      for (String var5 : var7.flags()) {
         var2.withShaderDefine(var5);
      }

      for (Entry var11 : var7.values().entrySet()) {
         run(var2, (String)var11.getKey(), (String)var11.getValue());
      }

      return var2.build();
   }

   private static void run(Builder var0, String var1, String var2) {
      try {
         var0.withShaderDefine(var1, Integer.parseInt(var2));
      } catch (NumberFormatException var5) {
         try {
            var0.withShaderDefine(var1, Float.parseFloat(var2));
         } catch (NumberFormatException var4) {
            SwyzzyAddon.logger.debug("Skipping shader define {} with the non numeric value {}.", var1, var2);
         }
      }
   }
}
