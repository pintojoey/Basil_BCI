package cz.zcu.kiv.eeg.gtn.online.gui;

/**
 * Created by Marek on 4. 4. 2017.
 */
public class Stimul {
    int id;
    String name;
    String file1;
    String file2;
    String desc;

    public Stimul(int id, String name, String file1, String file2, String desc) {
        this.id = id;
        this.name = name;
        this.file1 = file1;
        this.file2 = file2;
        this.desc = desc;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFile1() {
        return file1;
    }

    public String getFile2() {
        return file2;
    }

    public String getDesc() {return desc;}
}
