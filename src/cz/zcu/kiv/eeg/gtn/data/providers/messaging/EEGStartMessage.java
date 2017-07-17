package cz.zcu.kiv.eeg.gtn.data.providers.messaging;

/**
 * Created by Tomas Prokop on 17.07.2017.
 */
public class EEGStartMessage extends EEGMessage {

    private final String[] availableChannels;

    private final double[] resolutions;

    private final int channelCount;

    private final double sampling;

    public EEGStartMessage(MessageType msgType, int messageNumber, String[] availableChannels, double[] resolutions, int channelCount, double sampling) {
        super(msgType, messageNumber);
        this.availableChannels = availableChannels;
        this.resolutions = resolutions;
        this.channelCount = channelCount;
        this.sampling = sampling;
    }

    public String[] getAvailableChannels() {
        return availableChannels;
    }

    public double[] getResolutions() {
        return resolutions;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public double getSampling() {
        return sampling;
    }
}
