package steganograf.Logic;

import steganograf.Modals.AlertBox;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public final class Utils {

    public static String getFileExtension(File file) {
        String filename = file.getName();
        return filename.substring(filename.lastIndexOf(".")+1);
    }

    public static byte[] toByteArray(List<Byte> in) {
        final int n = in.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++)
            ret[i] = in.get(i);
        return ret;
    }

    public static String hashImage(File image){
        try{
            BufferedImage img = ImageIO.read(image);
            long pwd = 0;
            for (int i=0; i < img.getWidth() && i < img.getHeight(); i++)
                pwd += img.getRGB(i,i);
            return String.valueOf(pwd);
        }catch (IOException e) {
            AlertBox.error("Error while handling the key image", e.getMessage());
            return null;
        }
    }

}
