package dev.sixseven.module;

import dev.sixseven.module.combat.AimAssistModule;

public interface AimAssistCompute {
   double[] computePixels(AimAssistModule aimAssistModule, double d2, double d, double d3);

   void reset();
}
