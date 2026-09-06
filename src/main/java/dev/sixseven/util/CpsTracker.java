package dev.sixseven.util;

import java.util.ArrayDeque;
import java.util.Deque;

public final class CpsTracker {
   private static final Deque<Long> LEFT = new ArrayDeque<>();
   private static final Deque<Long> RIGHT = new ArrayDeque<>();

   private CpsTracker() {
   }

   public static void onClick(int n) {
      Deque deque = n == 0 ? LEFT : (n == 1 ? RIGHT : null);
      if (deque != null) {
         synchronized (deque) {
            deque.addLast(System.nanoTime());
         }
      }
   }

   public static int get(int n) {
      Deque deque = n == 0 ? LEFT : RIGHT;
      long l = System.nanoTime() - 1000000000L;
      synchronized (deque) {
         while (!deque.isEmpty() && deque.peekFirst() < l) {
            deque.pollFirst();
         }

         return deque.size();
      }
   }
}
