package steganograf.Logic;

import steganograf.Exceptions.CannotDecodeException;
import steganograf.Exceptions.CannotEncodeException;
import steganograf.Exceptions.UnsupportedImageTypeException;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageSteganography extends BaseSteganography {

    BufferedImage image;

    public ImageSteganography(File input, boolean isEncrypted, boolean isCompressed, byte pixelsPerByte) throws IOException, UnsupportedImageTypeException{
        this.isEncrypted = isEncrypted;
        this.isCompressed = isCompressed;
        this.pixelsPerByte = pixelsPerByte;
        this.image = ImageIO.read(input);
        this.capacity = this.image.getHeight()*this.image.getWidth()/this.pixelsPerByte;
        if(this.image.getType() == BufferedImage.TYPE_CUSTOM || this.image.getType() >= 8)
            throw new UnsupportedImageTypeException("Image type "+this.image.getType()+" is unsuported");
    }

    public ImageSteganography(File input) throws IOException, UnsupportedImageTypeException { this(input, false, false, (byte) 1); }

    public BufferedImage getImage(){
        return this.image;
    }

    public byte[] getHeader() throws CannotDecodeException{
        int b;
        List<Byte> header = new ArrayList<>();
        if (revealByte(0,0,(byte)1) != (byte) 'M' && revealByte(0,0,(byte)1) != (byte) 'D' && revealByte(0,0,(byte)1) != (byte) 'I')
            throw new CannotDecodeException("There is nothing embedded in this image");
        do{
            b = revealByte(this.i, this.j, (byte)1);
            increment();
            header.add((byte)b);
        }while(b != (byte) '!');
        this.header = Utils.toByteArray(header);
        return Utils.toByteArray(header);
    }

    void writeHeader(byte[] header){
        for(byte b : header){
            hideByte(b, this.i, this.j, (byte)1);
            increment();
        }
    }

    public void encode(byte[] message, File output) throws IOException, CannotEncodeException{
        //header M atau D disimpan lalu
        this.writeHeader(this.setHeader(message));
        //setiap karaker dibuah ke ascii dlm bentuk byte lalu disimpan
        for (byte b : message) {    
            hideByte(b, this.i, this.j, this.pixelsPerByte);
            increment();
        }
        ImageIO.write(this.image, "png", output);
    }

    public void encode(File doc, File output) throws IOException, CannotEncodeException{
        this.writeHeader(this.setHeader(doc));
        FileInputStream fis = new FileInputStream(doc);
        byte[] buffer = new byte[256];
        while(fis.read(buffer) > 0)        
            for(byte b : buffer) {
                hideByte(b, this.i, this.j, this.pixelsPerByte);
                increment();
            }
        ImageIO.write(this.image, "png", output);
    }

    public void decode(File file) throws IOException, CannotDecodeException{
        reset();
        int pos = 0;
        this.setSecretInfo(new HiddenData(this.getHeader()));
        FileOutputStream fos = new FileOutputStream(file);
        do{
            fos.write(revealByte(this.i, this.j, secretInfo.pixelsPerByte));
            increment();
            pos++;
        }while(pos<secretInfo.length);
        fos.close();
    }

    private void embed(byte b, int i, int j, byte pixelsPerByte){
        // 0xF8 = 248, 0xFE = 254, 0x07 = 7, 0x01 = 1
        int pixelMask = (pixelsPerByte == 1) ? 0xF8 : 0xFE, bitMask = (pixelsPerByte == 1) ? 0x07 : 0x01, shift = (pixelsPerByte == 1) ? 3 : 1;
        Color oldColor = new Color(this.image.getRGB(j,i));
        int red = oldColor.getRed(), green = oldColor.getGreen(), blue = oldColor.getBlue();
        red = red & pixelMask | b & bitMask; b = (byte) (b >> shift);
        green = green & 0xFC | b & 0x03; b = (byte) (b >> 2);
        blue = blue & pixelMask | b & bitMask; b = (byte) (b >> shift);
        Color newColor = new Color(red, green,blue);
        this.image.setRGB(j,i,newColor.getRGB());
    }

    private void hideByte(byte b, int i, int j, byte pixelsPerByte){
        embed(b, i, j, pixelsPerByte);
        if(pixelsPerByte == 2){
            b = (byte) (b >> 4);
            embed(b, this.image.getHeight()-i-1, this.image.getWidth()-j-1, pixelsPerByte);
        }
    }

    private byte extract(int i, int j, byte pixelsPerByte){
        int b;
        int pixelMask = (pixelsPerByte == 1) ? 0x07 : 0x01, shift = (pixelsPerByte == 1) ? 3 : 1;
        Color color = new Color(this.image.getRGB(j,i));
        int red = color.getRed(), green = color.getGreen(), blue = color.getBlue();
        b = (blue & pixelMask) ; b = b << 2;
        b = b | (green & 0x03); b = b << shift;
        b = b | (red & pixelMask);
        return (byte) b;
    }

    private byte revealByte(int i, int j, byte pixelsPerByte){
        byte b = extract(i, j, pixelsPerByte);
        if(pixelsPerByte == 2){
            byte c = extract(this.image.getHeight()-i-1, this.image.getWidth()-j-1, pixelsPerByte);
            c = (byte) (c << 4); b = (byte) (c | b);
        }
        return b;
    }

    protected void increment(){
        this.j++;
        if(this.j==this.image.getWidth()-1){ this.j=0;this.i++; }
    }

    protected void reset(){ this.i = 0; this.j = 0; }

}
