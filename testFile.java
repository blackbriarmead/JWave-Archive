

public class testFile{
   public static void main(String[] args){
      JWave j = new JWave("aa.wav");
      System.out.println(j.getAttributes());
      byte[] data = j.getData();
      j.changeSpeed(0.5);
      j.writeAllData();
   }
}