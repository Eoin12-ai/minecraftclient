package dev.kryptic.module;

import dev.kryptic.module.combat.AimAssistModule;

public interface AimAssistCompute {
   double[] computePixels(AimAssistModule aimAssistModule, double d2, double d, double d3);

   void reset();
}
