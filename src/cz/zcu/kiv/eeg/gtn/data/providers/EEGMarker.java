package cz.zcu.kiv.eeg.gtn.data.providers;

/**
 * Created by Tomas Prokop on 17.07.2017.
 */
public class EEGMarker {
    private String name;
    private int offset;

    public EEGMarker(String name, int offset) {
        this.name = name;
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }
}
