package cz.zcu.kiv.eeg.gtn.data.providers.messaging;

/**
 * Created by Tomas Prokop on 17.07.2017.
 */
public class EEGMessage {

    private final MessageType msgType;

    private final int messageNumber;

    private String message;

    public EEGMessage(MessageType msgType, int messageNumber) {
        this.msgType = msgType;
        this.messageNumber = messageNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public int getMessageNumber() {
        return messageNumber;
    }
}
