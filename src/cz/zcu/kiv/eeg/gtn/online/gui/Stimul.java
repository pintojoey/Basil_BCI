package cz.zcu.kiv.eeg.gtn.online.gui;

/**
 * Created by Marek on 4. 4. 2017.
 */
public class Stimul {
    int id;
    String name;
    String file1;
    String file2;

    public Stimul(int id, String name, String file1, String file2) {
        this.id = id;
        this.name = name;
        this.file1 = file1;
        this.file2 = file2;
    }


}
