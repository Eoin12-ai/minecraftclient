package dev.sixseven.util;

public final class TpsTracker {
   private static long lastPacketNanos = -1L;
   private static float tps = 20.0F;

   private TpsTracker() {
   }

   public static void onTimePacket() {
      long l = System.nanoTime();
      if (lastPacketNanos > 0L) {
         float f = (float)(l - lastPacketNanos) / 1.0E9F;
         if (f > 0.05F) {
            float f3 = Math.clamp(20.0F / f, 0.0F, 20.0F);
            tps = tps * 0.7F + f3 * 0.3F;
         }
      }

      lastPacketNanos = l;
   }

   public static void reset() {
      lastPacketNanos = -1L;
      tps = 20.0F;
   }

   public static float get() {
      return tps;
   }
}
