package pkg1;

import java.lang.reflect.Method;

final class InternalHelper {
   private Module module;
   private Method method;
   private int priority;

   InternalHelper(Module var1, Method var2, int var3) {
      this.module = var1;
      this.method = var2;
      this.priority = var3;
   }

   public Module module() {
      return this.module;
   }

   public Method method() {
      return this.method;
   }

   public int priority() {
      return this.priority;
   }
}
