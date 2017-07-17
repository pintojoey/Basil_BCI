package cz.zcu.kiv.eeg.gtn.data.providers;

import java.util.Observable;

/**
 * Created by Tomas Prokop on 04.07.2017.
 */
public abstract class AbstractDataProvider extends Observable implements Runnable {

    private String[] availableChannels;

    public abstract void run();

    public abstract void stop();

    public String[] getAvailableChannels() {
        return availableChannels;
    }

    protected void setAvailableChannels(String[] availableChannels) {
        this.availableChannels = availableChannels;
    }
}
