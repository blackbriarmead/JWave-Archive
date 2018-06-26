

public class testFile{
   public static void main(String[] args){
      System.out.println("Loading old wav");
      JWave j = new JWave("aa.wav");
      System.out.println("Retrieving Header Attributes\n\n");
      System.out.println(j.getAttributes());
      System.out.println("\n\n\nChanging speed");
      j.changeSpeed(3);
      System.out.println("Saving new wav");
      j.writeAllData();
      System.out.println("Retrieving new Header Attributes\n\n");
      System.out.println(j.getAttributes());
   }
}