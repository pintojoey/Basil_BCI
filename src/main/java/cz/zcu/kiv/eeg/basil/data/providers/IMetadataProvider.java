package cz.zcu.kiv.eeg.basil.data.providers;

import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStartMessage;

/**
 * Interface for class that loads metadata and adds it to provided data.
 *
 * Created by Tomas Prokop on 29.01.2018.
 */
public interface IMetadataProvider {

    /**
     * Loads metadata and returns it in EEG start message object
     * @see EEGStartMessage
     * @param msgId Start message ID
     * @return EEG start message with loaded metadata
     */
    EEGStartMessage loadMetadata(int msgId);

    /**
     * Set metadata file name if necessary
     * @param fileName file name with stored metadata
     */
    void setFileName(String fileName);
}
