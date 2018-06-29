

public class testFile{
   public static void main(String[] args){
      long startTime = System.currentTimeMillis();
      System.out.println("Loading old wav");
      JWave j = new JWave("candypaint.wav");
      System.out.println("Retrieving Header Attributes\n\n");
      System.out.println(j.getAttributes());
      System.out.println("\n\n\nChanging speed");
      j.normalize();
      System.out.println("Saving new wav");
      j.writeAllData();
      System.out.println("Retrieving new Header Attributes\n\n");
      System.out.println(j.getAttributes());
      long elapsedTime = System.currentTimeMillis() - startTime;
      System.out.println("Time elapsed to load, edit, and save new " + j.Subchunk2Size/1000000 + "MB file: ");
      System.out.println(elapsedTime +"ms");
   }
}