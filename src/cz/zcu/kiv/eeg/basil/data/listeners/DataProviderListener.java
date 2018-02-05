package cz.zcu.kiv.eeg.basil.data.listeners;

/**
 * Created by Tomas Prokop on 30.01.2018.
 */
public interface DataProviderListener {
    void dataReadStart();
    void dataReadEnd();
    void dataReadError(Exception ex);
}
