import java.io.*;
import java.nio.*;

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
   
   byte[] data;
   
   
   public JWave(File f){
      this.f = f;
      loadAllData();
   }
   
   public JWave(String s){
      this.f = new File(s);
      loadAllData();
   }
   
   /**public void readSamples(int[] in, int totalSamples){
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
   }*/
    
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
   
   private void loadAttributes(){
      //read raw bytes and convert to ints and shorts
      try{
         FileInputStream fis = new FileInputStream(f);
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
         fis.close();
      } catch(Exception e){
         e.printStackTrace();
      }
      
   }
   
   private void writeAttributes(FileOutputStream fos){
      try{
         
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
      }
      catch(IOException e){
         System.out.println(e.toString());
      }
   }
   
   public byte[] getData(){
      loadData();
      return(data);
   }
   
   private void loadData(){
      try{
         FileInputStream fis = new FileInputStream(f);
         //read raw bytes and convert to ints and shorts
         fis.skip(44);
         int available = fis.available();
         data = new byte[available];
         fis.read(data);
         fis.close();
         
      }
      catch(Exception e){
         e.printStackTrace();
      }
      
   }
   
   private void writeData(FileOutputStream fos){
      try{
         fos.write(data);
      }
      catch(IOException e){
         System.out.println(e.toString());
      }
   }
   
   private void loadAllData(){
      loadAttributes();
      loadData();
   }
   
   public void writeAllData(){
      try{
         FileOutputStream fos = new FileOutputStream(f);
         writeAttributes(fos);
         writeData(fos);
         fos.close();
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   public void changeSpeed(double multiplier){
      try{
         int newSamples = (int)(Subchunk2Size / multiplier / (BitsPerSample/8)* NumChannels);
         double sampleNum = 0;
         int roundedSampleNum = 0;
         byte[] buffer = new byte[BitsPerSample/8*NumChannels];
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream(BitsPerSample / 8 * NumChannels);
         ByteArrayInputStream bais = new ByteArrayInputStream(data);
         
         
         while(sampleNum < newSamples){
            int difference = (int)(Math.floor(sampleNum - roundedSampleNum));
            if(difference >= 2){
               roundedSampleNum += Math.floor(difference);
               bais.skip((difference-1)*BitsPerSample/8*NumChannels);
               bais.read(buffer);
            }else if(difference >=1){
               roundedSampleNum += Math.floor(difference);
               bais.read(buffer);
            }
            baos.write(buffer);
            sampleNum += multiplier;
         }
         
         data = baos.toByteArray();
         
         Subchunk2Size = data.length/2;
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   
   
   private void putByteArray(byte[] parent, byte[] child, int index){
      for(int i = index; i < index + child.length; i++){
         parent[i] = child[i-index];
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
   
   private int intFromByteArray(byte[] b, boolean bigEndian) {
      if(bigEndian){
         return   b[3] & 0xFF |
            (b[2] & 0xFF) << 8 |
            (b[1] & 0xFF) << 16 |
            (b[0] & 0xFF) << 24;
      }else{
         return   b[0] & 0xFF |
            (b[1] & 0xFF) << 8 |
            (b[2] & 0xFF) << 16 |
            (b[3] & 0xFF) << 24;
      }
   }
   
   private short shortFromByteArray(byte[] b, boolean bigEndian) {
      if(bigEndian){
         return   (short)(b[1] & 0xFF |
            (b[0] & 0xFF) << 8);
      }else{
         return   (short)(b[0] & 0xFF |
            (b[1] & 0xFF) << 8);
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