package pkg1;

public enum RenderMode {
   Lines,
   Sides,
   Both;

   private static RenderMode[] getValArray() {
      return new RenderMode[]{Lines, Sides, Both};
   }
}
