package cz.zcu.kiv.eeg.gtn.data.listeners;

import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;

/**
 * Created by Tomas Prokop on 01.08.2017.
 */
public interface EEGMessageListener {
    void startMessageSent(EEGStartMessage msg);
    void dataMessageSent(EEGDataMessage msg);
    void stopMessageSent(EEGStopMessage msg);
}
