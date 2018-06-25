import java.io.*;
import java.nio.*;
import java.math.BigInteger;

public class JWave{
   File f;
   
   public int ChunkID;
   public int ChunkSize;
   public int Format;
   public int Subchunk1ID;
   public int Subchunk1Size;
   public short AudioFormat;
   public short NumChannels;
   public int SampleRate;
   public int ByteRate;
   public short BlockAlign;
   public short BitsPerSample;
   public int Subchunk2ID;
   public int Subchunk2Size;
   FileInputStream datafis;
   FileOutputStream datafos;
   
   
   public JWave(File f){
      this.f = f;
      try{
         if(!this.f.exists()){
            this.f.createNewFile();
         }
         this.datafis = new FileInputStream(f);
         this.datafos = new FileOutputStream(f);
         datafis.skip(44);
      }catch(IOException e){
         System.out.println(e.toString());
      }
      generateAttributes();
   }
   
   public JWave(String s){
      this.f = new File(s);
      try{
         /**if(!this.f.exists()){
            this.f.createNewFile();
         }*/
         this.datafis = new FileInputStream(f);
         this.datafos = new FileOutputStream(f);
         //datafis.skip(44);
      }catch(IOException e){
         System.out.println(e.toString());
      }
      //generateAttributes();
   }
   
   public void close(){
      try{
         datafis.close();
         datafos.close();
      }catch(IOException e){
         System.out.println(e.toString());
      }
   }
   
   public void readSamples(int[] in, int totalSamples){
      int[] out = new int[(int)(NumChannels*BitsPerSample/8)];
      for(int i = 0; i < totalSamples; i++){
         for(int j = 0; j < (int)(NumChannels*BitsPerSample/8); j++){
            if(BitsPerSample == 16){
               out[i * (int)(NumChannels*BitsPerSample/8) + j] = (int)(readShort(datafis, "little"));
            }else if(BitsPerSample == 32){
               out[i * (int)(NumChannels*BitsPerSample/8) + j] = readInt(datafis, "little");
            }else if(BitsPerSample == 8){
               out[i * (int)(NumChannels*BitsPerSample/8) + j] = (int)(readChar(datafis));
            }
         }
      }
      in = out;
   }
   
   public void readFrames(int[] in, int totalFrames){
      int[] out = new int[(int)(BitsPerSample/8)];
      for(int i = 0; i < totalFrames; i++){
         if(BitsPerSample == 16){
            out[i * (int)(BitsPerSample/8)] = (int)(readShort(datafis, "little"));
         }else if(BitsPerSample == 32){
            out[i * (int)(BitsPerSample/8)] = readInt(datafis, "little");
         }else if(BitsPerSample == 8){
            out[i * (int)(BitsPerSample/8)] = (int)(readChar(datafis));
         }
      }
      in = out;
   }
    
   public String getAttributes(){
      return("ChunkID: "+ChunkID +"\n"+
      "ChunkSize: "+ChunkSize +"\n"+
      "Format: "+Format +"\n"+
      "Subchunk1ID: "+Subchunk1ID +"\n"+
      "Subchunk1Size: "+Subchunk1Size +"\n"+
      "AudioFormat: "+AudioFormat +"\n"+
      "NumChannels: "+NumChannels +"\n"+
      "SampleRate: "+SampleRate +"\n"+
      "ByteRate: "+ByteRate +"\n"+
      "BlockAlign: "+BlockAlign +"\n"+
      "BitsPerSample: "+BitsPerSample +"\n"+
      "Subchunk2ID: "+Subchunk2ID +"\n"+
      "Subchunk2Size: "+Subchunk2Size);
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
         if(endian.equals("big")){
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
         if(endian.equals("big")){
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
   
   private char readChar(FileInputStream fis){
      try{
         return((char)(fis.read()));
      }catch(IOException e){
         System.out.println(e.toString());
      }
      return(0x00);
   }
   
   private int intFromByteArray(byte[] bytes, boolean bigEndian) {
      if(bigEndian){
         return(new BigInteger(bytes).intValue());
      }else{
         return(new BigInteger(1,bytes).intValue());
      }
   }
   
   private short shortFromByteArray(byte[] bytes, boolean bigEndian) {
      if(bigEndian){
         return((short)(new BigInteger(bytes).intValue()));
      }else{
         return((short)(new BigInteger(1,bytes).intValue()));
      }
   }
   
   private void writeAttributes(){
      try{
         FileOutputStream fos = new FileOutputStream(f);// open new fos object
         
         //convert to bytes (with correct endianess) and write sequentially
         fos.write(intToBigEndian(ChunkID));
         fos.write(intToLittleEndian(ChunkSize));
         fos.write(intToBigEndian(Format));
         fos.write(intToBigEndian(Subchunk1ID));
         fos.write(intToLittleEndian(Subchunk1Size));
         fos.write(shortToLittleEndian(AudioFormat));
         fos.write(shortToLittleEndian(NumChannels));
         fos.write(intToLittleEndian(SampleRate));
         fos.write(intToLittleEndian(ByteRate));
         fos.write(shortToLittleEndian(BlockAlign));
         fos.write(shortToLittleEndian(BitsPerSample));
         fos.write(intToBigEndian(Subchunk2ID));
         fos.write(intToLittleEndian(Subchunk2Size));
         
         fos.close(); // close fos
      }
      catch(IOException e){
         System.out.println(e.toString());
      }
   }
   
   private static byte[] intToLittleEndian(int num) {
   	byte[] b = new byte[4];
   	b[0] = (byte) (num & 0xFF);
   	b[1] = (byte) ((num >> 8) & 0xFF);
   	b[2] = (byte) ((num >> 16) & 0xFF);
   	b[3] = (byte) ((num >> 24) & 0xFF);
   	return b;
   }
   
   private static byte[] intToBigEndian(int num) {
   	byte[] b = new byte[4];
   	b[0] = (byte) ((num >> 24) & 0xFF);
   	b[1] = (byte) ((num >> 16) & 0xFF);
   	b[2] = (byte) ((num >> 8) & 0xFF);
   	b[3] = (byte) (num & 0xFF);
   	return b;
   }
   
   private static byte[] shortToLittleEndian(short num) {
   	byte[] b = new byte[2];
   	b[0] = (byte) (num & 0xFF);
   	b[1] = (byte) ((num >> 8) & 0xFF);
   	return b;
   }
   
   private static byte[] shortToBigEndian(short num) {
   	byte[] b = new byte[2];
   	b[0] = (byte) ((num >> 8) & 0xFF);
   	b[1] = (byte) (num & 0xFF);
   	return b;
   }

}