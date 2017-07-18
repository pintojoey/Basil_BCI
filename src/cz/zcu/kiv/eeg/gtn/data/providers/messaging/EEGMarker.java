package cz.zcu.kiv.eeg.gtn.data.providers.messaging;

/**
 * Created by Tomas Prokop on 17.07.2017.
 */
public class EEGMarker {

    private final String name;

    private final int offset;

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
    
    @Override
    public String toString() {
    	return name + ", offset: " + offset;
    }
}
