

public class testFile{
   public static void main(String[] args){
      long startTime = System.currentTimeMillis();
      System.out.println("Loading old wav");
      JWave j = new JWave("america.wav");
      System.out.println("Retrieving Header Attributes\n\n");
      System.out.println(j.getAttributes());
      System.out.println("\n\nChanging speed");
      for(int i = 0; i < 15; i++){
         j.changeSpeed(1.327);
         j.changeSpeed(1/1.327);
      }
      System.out.println(j.getMaxAmplitude());
      j.normalize();
      System.out.println(j.getMaxAmplitude());
      System.out.println("Saving new wav");
      j.writeAllData();
      System.out.println("Retrieving new Header Attributes\n\n");
      System.out.println(j.getAttributes());
      long elapsedTime = System.currentTimeMillis() - startTime;
      System.out.println("Time elapsed to load, edit, and save new " + j.Subchunk2Size/1000000 + "MB file: ");
      System.out.println(elapsedTime +"ms");
   }
}