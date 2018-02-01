package cz.zcu.kiv.eeg.gtn.data.providers.messaging;

import java.util.HashMap;

/**
 * Created by Tomas Prokop on 17.07.2017.
 */
public class EEGStartMessage extends EEGMessage {

    /**
     * Currently available channels.
     */
    private final String[] availableChannels;

    /**
     * Resolution of channels
     */
    private final double[] resolutions;

    /**
     * Channel count
     */
    private final int channelCount;

    /**
     * Data sampling in samples/sec
     */
    private final double sampling;

    /**
     * Name of data file if available
     */
    private String dataFileName;

    /**
     * Name of current target stimulus if available. It is usualy used for training purpose.
     */
    private String targetStimulus;

    private HashMap<String, Object> additionalInfo;

    /**
     * Creates new start message
     * @param messageNumber message ID
     * @param availableChannels array containing names of available channels
     * @param resolutions resolution of channels
     * @param sampling sampling frequecy in samples/sec
     */
    public EEGStartMessage(int messageNumber, String[] availableChannels, double[] resolutions, double sampling) {
        super(MessageType.START, messageNumber);
        this.availableChannels = availableChannels;
        this.resolutions = resolutions;
        this.channelCount = availableChannels.length;
        this.sampling = sampling;
        additionalInfo = new HashMap<>();

    }

    /**
     * Get the data file name of current data.
     * @return file name
     */
    public String getDataFileName() {
        return dataFileName;
    }

    /**
     * Set data file name of current data
     * @param dataFileName path to data file
     */
    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    /**
     * Get all available channels in current experiment
     * @return array of available channel names
     */
    public String[] getAvailableChannels() {
        return availableChannels;
    }

    /**
     * Resolution of channels
     * @return resolution of channels.
     */
    public double[] getResolutions() {
        return resolutions;
    }

    /**
     * Number of channels
     * @return number of channels
     */
    public int getChannelCount() {
        return channelCount;
    }

    /**
     * Sampling frequency in samples/sec
     * @return sampling
     */
    public double getSampling() {
        return sampling;
    }

    /**
     * Name of target stimulus of current experiment
     * @return Target stimulus name
     */
    public String getTargetMarker() {
        return targetStimulus;
    }

    /**
     * Set name of target stimulus
     * @param targetStimulus target stimulus name
     */
    public void setTargetMarker(String targetStimulus) {
        this.targetStimulus = targetStimulus;
    }

    /**
     * Get map that contains any other relevant informations about data, experiment, etc.
     * @return HashMap containing info about data
     */
    public HashMap<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Add multiple information into additionalInfo hash map
     * @param additionalInfo additional metadata
     */
    public void addAdditionalInfo(HashMap<String, Object> additionalInfo) {
        if(additionalInfo == null) return;

        this.additionalInfo.putAll(additionalInfo);
    }

    public void addAdditionalInfo(String key, Object value) {
        if(key == null) return;

        additionalInfo.put(key, value);
    }
}
