/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganograf.Logic;

import steganograf.Types.DataFormat;

public class HiddenData {

    public DataFormat format;
    public boolean isEncrypted;
    public boolean isCompressed;
    public byte pixelsPerByte;
    public byte pixelsPerPixel;
    public long length;
    public String extension;
    public int width;
    public int height;

    public HiddenData(byte[] header){
        switch((char) header[0]){
            case 'M':
                this.format = DataFormat.MESSAGE;
                this.extension = "txt";
                break;
            case 'D':
                this.format = DataFormat.DOCUMENT;
                StringBuilder extension = new StringBuilder();
                for(int j=7; j<header.length-1;j++)
                    extension.append((char) header[j]);
                this.extension = extension.toString();
                break;
        }
        this.isEncrypted = ((char) header[1]) == 'E';
        this.isCompressed = ((char) header[2]) == 'C';
        this.pixelsPerByte = header[3];
        StringBuilder length = new StringBuilder();
        for(int i=4; i<7; i++){
            String l = String.format("%8s",Integer.toBinaryString(header[i])).replace(' ','0');
            length.append(l.substring(l.length() - 8, l.length()));
        }
        this.length = Integer.parseInt(length.toString(), 2);
        
    }
}

