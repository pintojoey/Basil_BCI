package cz.zcu.kiv.eeg.gtn.online.gui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Reprezentuje jednotlivý stimul s potřebnými atributy
 */
public class Stimul {
    int id;
    String name;
    String url1;
    String url2;
    String description;

    boolean isImgFile1 = false;
    boolean isImgFile2 = false;

    Image image1;
    Image image2;

    /**
     * Konstruktor třídy Stimul
     *
     * @param id          - id stimulu
     * @param name        - název stimulu
     * @param url1        - adresa k souboru 1
     * @param url2        - adresa k souboru 2
     * @param description - popis stimulu
     */
    public Stimul(int id, String name, String url1, String url2, String description) {
        this.id = id;
        this.name = name;
        this.url1 = url1;
        this.url2 = url2;
        this.description = description;
    }

    /**
     * Načítání obrázků
     * Zjištování, zda je soubor obrázek
     */
    public void loadImages() {
        try {
            Image image = ImageIO.read(new File(url1));
            if (image == null) {
                isImgFile1 = false;
            } else {
                this.image1 = image;
                isImgFile1 = true;
            }
        } catch (IOException ex) {
            isImgFile1 = false;
        }
        try {
            Image image2 = ImageIO.read(new File(url2));
            if (image2 == null) {
                isImgFile2 = false;
            } else {
                this.image2 = image2;
                isImgFile2 = true;
            }
        } catch (IOException ex) {
            isImgFile2 = false;
        }

    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
