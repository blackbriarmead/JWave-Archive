import java.io.*;
import java.nio.*;


public class JWave{
   File f;
   
   int ChunkID;
   int ChunkSize;
   int Format;
   int Subchunk1ID;
   int Subchunk1Size;
   short AudioFormat;
   short NumChannels;
   int SampleRate;
   int ByteRate;
   short BlockAlign;
   short BitsPerSample;
   int Subchunk2ID;
   int Subchunk2Size;
   
   public JWave(File f){
      this.f = f;
      generateAttributes();
   }
   
   public JWave(String s){
      this.f = new File(s);
      generateAttributes();
   }
   
   public long getFileSize(){
      return(f.length());
   }
   
   private void generateAttributes(){
      try{
         FileInputStream fis = new FileInputStream(f);// open new fis object
         
         //read raw bytes and convert to ints and shorts
         ChunkID = readInt(fis, "big");
         ChunkSize = readInt(fis, "little");
         Format = readInt(fis, "big");
         Subchunk1ID = readInt(fis, "big");
         Subchunk1Size = readInt(fis, "little");
         AudioFormat = readShort(fis, "little");
         NumChannels = readShort(fis, "little");
         SampleRate = readInt(fis, "little");
         ByteRate = readInt(fis, "little");
         BlockAlign = readShort(fis, "little");
         BitsPerSample = readShort(fis, "little");
         Subchunk2ID = readInt(fis, "big");
         Subchunk2Size = readInt(fis, "little");
         
         fis.close(); // close fis
      }
      catch(IOException e){
         System.out.println(e.toString());
      }
      
   }
   
   private int readInt(FileInputStream fis, String endian){
      try{
         if(endian.compareTo("big")==0){
            byte[] b = new byte[4];
            fis.read(b);
            return(intFromByteArray(b, true));
         }else{
            byte[] b = new byte[4];
            fis.read(b);
            return(intFromByteArray(b, false));
         }
      }catch(IOException e){
         System.out.println(e.toString());
      }
      return(-1);
   }
   
   private short readShort(FileInputStream fis, String endian){
      try{
         if(endian.compareTo("big")==0){
            byte[] b = new byte[2];
            fis.read(b);
            return(shortFromByteArray(b, true));
         }else{
            byte[] b = new byte[2];
            fis.read(b);
            return(shortFromByteArray(b, false));
         }
      }catch(IOException e){
         System.out.println(e.toString());
      }
      return(-1);
   }
   
   private int intFromByteArray(byte[] bytes, boolean bigEndian) {
      if(bigEndian){
         return(ByteBuffer.wrap(bytes).getInt());
      }else{
         return(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt());
      }
   }
   
   private short shortFromByteArray(byte[] bytes, boolean bigEndian) {
      if(bigEndian){
         return(ByteBuffer.wrap(bytes).getShort());
      }else{
         return(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort());
      }
   }
   
   private void updateAttributes(){
      
   }
   
   public int getChunkID(){
      return(ChunkID);
   }
}