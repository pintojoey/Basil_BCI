package cz.zcu.kiv.eeg.gtn.data.providers.messaging;

/**
 * Created by Tomas Prokop on 17.07.2017.
 */
public class EEGStopMessage extends EEGMessage {

    public EEGStopMessage(MessageType msgType, int messageNumber) {
        super(msgType, messageNumber);
    }
}
