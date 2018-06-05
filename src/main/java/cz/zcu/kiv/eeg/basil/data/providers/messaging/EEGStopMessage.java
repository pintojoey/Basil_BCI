package cz.zcu.kiv.eeg.basil.data.providers.messaging;

/**
 * EEG message sent when data provider read all data or stop data providing
 *
 * Created by Tomas Prokop on 17.07.2017.
 */
public class EEGStopMessage extends EEGMessage {

    /**
     * Creates new stop message with given ID
     * @param messageId unique ID
     */
    public EEGStopMessage(int messageId) {
        super(messageId);
    }
}
