package pkg1;

enum HEADER {
   HEADER,
   MODULE,
   SETTING;

   private static HEADER[] getValArray() {
      return new HEADER[]{HEADER, MODULE, SETTING};
   }
}
