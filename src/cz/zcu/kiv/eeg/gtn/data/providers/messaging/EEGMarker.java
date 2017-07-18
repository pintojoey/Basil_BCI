package cz.zcu.kiv.eeg.gtn.data.providers.messaging;

/**
 * Created by Tomas Prokop on 17.07.2017.
 */
public class EEGMarker {

    private final String name;

    private final int offset;

    private boolean isTarget = false;

    public EEGMarker(String name, int offset) {
        this.name = name;
        this.offset = offset;
    }

    public boolean isTarget() {
        return isTarget;
    }

    public void setTarget(boolean target) {
        isTarget = target;
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }
}
