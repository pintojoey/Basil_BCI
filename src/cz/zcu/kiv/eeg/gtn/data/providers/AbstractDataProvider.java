package cz.zcu.kiv.eeg.gtn.data.providers;

import java.util.Observable;

/**
 * Created by Tomas Prokop on 04.07.2017.
 */
public abstract class AbstractDataProvider extends Observable implements Runnable {

    public abstract void run();

    public abstract void stop();
}
