import java.io.*;
import java.nio.*;
import java.util.*;

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
   
   public byte[] data;
   
   
   
   
   public JWave(File f){
      this.f = f;
      loadAllData();
   }
   
   public JWave(String s){
      this.f = new File(s);
      loadAllData();
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
         SampleRate = readInt(fis, "littl e");
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
   
   public void writeAllData(File file){
      try{
         FileOutputStream fos = new FileOutputStream(file);
         Subchunk2Size = data.length;
         writeAttributes(fos);
         writeData(fos);
         fos.close();
      }catch(Exception e){
         e.printStackTrace();
      }
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
   
   public short getMaxAmplitude(){
      try{
         int totalSamples = Subchunk2Size / BitsPerSample * 8;
         int sampleNum = 0;      
         byte[] buffer = new byte[BitsPerSample / 8];
         short largest = 0;
         
         ByteArrayInputStream bais = new ByteArrayInputStream(data);
         while(sampleNum < totalSamples && bais.available() > 0){
            bais.read(buffer);
            short temp = shortFromByteArray(buffer, false);
            if(temp<0){
               temp = (short)(0 - temp);
            }
            if(temp > largest){
               largest = temp;
            }
            sampleNum++;
         }
         return(largest);
      }catch(Exception e){
         e.printStackTrace();
         return(0);
      }
   }
   
   public void normalize(){
      try{
         short largest = getMaxAmplitude();
         
         double mult = (double)Short.MAX_VALUE /(double)largest;
         if(mult>1){
            amplify(mult);
         }
         
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   public void amplify(double multiplier){
      try{
         int totalSamples = Subchunk2Size / BitsPerSample * 8;
         int sampleNum = 0;      
         byte[] buffer = new byte[BitsPerSample / 8];
         ByteBuffer bbuffer = ByteBuffer.allocate(BitsPerSample / 8);
         
         ByteArrayInputStream bais = new ByteArrayInputStream(data);
         ByteArrayOutputStream baos = new ByteArrayOutputStream(BitsPerSample / 8);
         while(sampleNum < totalSamples && bais.available() > 0){
            bais.read(buffer);
            short intermediate = shortFromByteArray(buffer, false);
            intermediate *= multiplier;
            if(intermediate > Short.MAX_VALUE){
               intermediate = Short.MAX_VALUE;
            }else if(intermediate < Short.MIN_VALUE){
               intermediate = Short.MIN_VALUE;
            }
            baos.write(shortToLittleEndian(intermediate));
            sampleNum++;
         }
         data = baos.toByteArray();
         
         
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   public void changeSpeed(double multiplier){
      int cores = Runtime.getRuntime().availableProcessors();
      try{
         int newSamples = (int)(Subchunk2Size / multiplier / (BitsPerSample/8)* NumChannels);
         double sampleNum = 0;
         int roundedSampleNum = 0;
         byte[] next = new byte[BitsPerSample/8*NumChannels];
         byte[] last = new byte[BitsPerSample/8*NumChannels];
         byte[] buffer = new byte[BitsPerSample/8*NumChannels];
         
         int lastpos = 0;
         int nextpos = 1;
         
         int streampos = 0;
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream(BitsPerSample / 8 * NumChannels);
         ByteArrayInputStream bais = new ByteArrayInputStream(data);
         
         
         /*while(sampleNum < newSamples && bais.available() > 0){
            double exactDifference = sampleNum - roundedSampleNum;
            int difference = (int)(Math.floor(exactDifference));
            if(difference >= 2){
               roundedSampleNum = (int)Math.floor(sampleNum);
               exactDifference -= difference;
               bais.skip((difference-1)*BitsPerSample/8*NumChannels);
               bais.read(buffer);
            }else if(difference ==1){
               roundedSampleNum =(int)Math.floor(sampleNum);
               exactDifference -= difference;
               bais.read(buffer);
            }/**if(exactDifference > 0 && exactDifference < 1){
               buffer = 
            }*//**
            baos.write(buffer);
            sampleNum += multiplier;
         }**/
         
         while(sampleNum < newSamples && bais.available() > 0){
            double exactDifference = sampleNum - lastpos;
            double bias = exactDifference-Math.floor(exactDifference);
            if(exactDifference >= 1){
               if(!(lastpos == Math.floor(sampleNum) || nextpos == Math.floor(sampleNum) + 1)){
                  if(lastpos == Math.floor(sampleNum) - 1){
                     //System.out.println("lastpos " + lastpos + "nextpos " + nextpos + "sampleNum " + sampleNum + "exactDifference " + exactDifference);
                     last = next.clone();
                     lastpos = nextpos;
                     bais.read(next);
                     nextpos = lastpos + 1;
                  }
                  else if(lastpos < Math.floor(sampleNum) - 1){
                     //System.out.println("lastpos " + lastpos + "nextpos " + nextpos + "sampleNum " + sampleNum + "exactDifference " + exactDifference);
                     bais.skip((long)((Math.floor(sampleNum)-lastpos*BitsPerSample/8*NumChannels)));
                     bais.read(last);
                     bais.read(next);
                     lastpos = (int)Math.floor(sampleNum);
                     nextpos = (int)Math.floor(sampleNum) + 1;
                  }
               }
            }
            
            
            byte[] prevleft = new byte[BitsPerSample/8];
            byte[] prevright = new byte[BitsPerSample/8];
            byte[] nextleft = new byte[BitsPerSample/8];
            byte[] nextright = new byte[BitsPerSample/8];
            System.arraycopy(last, 0, prevleft, 0, BitsPerSample/8);
            System.arraycopy(last, BitsPerSample/8, prevright, 0, BitsPerSample/8);
            System.arraycopy(next, 0, nextleft, 0, BitsPerSample/8);
            System.arraycopy(next, BitsPerSample/8, nextright, 0, BitsPerSample/8);
            double value = shortFromByteArray(prevleft, false)*(1-bias) + shortFromByteArray(nextleft, false)*bias;
            buffer = shortToLittleEndian((int)value);
            baos.write(buffer);
            value = shortFromByteArray(prevright, false)*(1-bias) + shortFromByteArray(nextright, false)*bias;
            buffer = shortToLittleEndian((int)value);
            baos.write(buffer);
            sampleNum += multiplier;
         }
         
         byte[] temp = baos.toByteArray();
         data = new byte[(int)(temp.length)];
         baos.close();
         java.lang.System.arraycopy(temp,0,data,0,(int)(temp.length));
         Subchunk2Size = data.length;
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   public void combine(JWave other){
      if(other.Subchunk2Size <= Subchunk2Size){
         ByteArrayOutputStream baos = new ByteArrayOutputStream(BitsPerSample / 8);
         short amp1 = getMaxAmplitude();
         short amp2 = other.getMaxAmplitude();
         int totalAmp = amp1 + amp2;
         double newMult = (double)Short.MAX_VALUE / (double)totalAmp;
         amplify(newMult);
         other.amplify(newMult);
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
   
   private static byte[] shortToLittleEndian(int num) {
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
   
   public ArrayList<Sample> getSamples(byte[] data){
      ArrayList<Sample> samples = new ArrayList<Sample>();
      try{
         int totalSamples = Subchunk2Size / BitsPerSample * 8;
         int sampleNum = 0;      
         byte[] buffer = new byte[BitsPerSample / 8];
         short largest = 0;
         
         ByteArrayInputStream bais = new ByteArrayInputStream(data);
         while(sampleNum < totalSamples && bais.available() > 1){
            bais.read(buffer);
            short left = shortFromByteArray(buffer, false);
            bais.read(buffer);
            short right = shortFromByteArray(buffer, false);
            samples.add(new Sample(left,right));
            sampleNum+=2;
         }
         return(samples);
      }catch(Exception e){
         e.printStackTrace();
         return(null);
      }
   }

}