package cz.zcu.kiv.eeg.gtn.online.gui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Marek on 4. 4. 2017.
 */
public class Stimul {
    int id;
    String name;
    String url1;
    String url2;
    String description;

    boolean isImgFile1=false;
    boolean isImgFile2=false;

    Image image1;

    public Stimul(int id, String name, String url1, String url2, String description) {
        this.id = id;
        this.name = name;
        this.url1 = url1;
        this.url2 = url2;
        this.description = description;
    }
    public void loadImages() {
        try {
            Image image = ImageIO.read(new File(url1));
            if (image == null) {
                isImgFile1 = false;
                System.out.println("The file"+url1+"could not be opened , it is not an image");
            }
            else{
                this.image1=image;
                isImgFile1=true;
            }
        } catch(IOException ex) {
            isImgFile1 = false;
            System.out.println("The file"+url1+"could not be opened , an error occurred.");
        }

    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl1() {
        return url1;
    }

    public String getUrl2() {
        return url2;
    }

    public String getDescription() {return description;}

}
