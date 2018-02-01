package cz.zcu.kiv.eeg.gtn.data.providers;

import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;

/**
 * Created by Tomas Prokop on 29.01.2018.
 */
public interface IMetadataProvider {
    EEGStartMessage loadMetadata(int msgId);
    void setFileName(String fileName);
}
