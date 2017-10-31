package cz.zcu.kiv.eeg.gtn.data.processing.structures;

import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;

/**
 * Created by Tomas Prokop on 31.07.2017.
 */
public interface IBuffer {

    /**
     * Initializes buffer
     * @param meta EEG start message containing recording metadata - sampling frequency, channel info, etc.
     */
    void initialize(EEGStartMessage meta);

    /**
     * Get EEG recoring metadata - sampling frequency, channel info, etc.
     * @return metadata
     */
    EEGStartMessage getMetadata();

    /**
     * Adds data into buffer
     *
     * @param data    data
     * @param markers markers
     */
    void add(double[][] data, List<EEGMarker> markers);

    /**
     * Get all data in buffer
     *
     * @return data
     */
    EEGDataPackage get();

    /**
     * Get required number of samples.
     * Should return null if buffer contains less samples.
     *
     * @param size samples
     * @return data
     */
    EEGDataPackage getAndRemove(int size);

    /**
     * Get all markers
     * @return markers
     */
    List<EEGMarker> getMarkers();

    /**
     * get required number of markers
     * @param count max number of returned and deleted markers
     * @return markers
     */
    List<EEGMarker> getAndRemoveMarkers(int count);

    /**
     * Current number of samples in buffer
     * @return buffer size
     */
    int size();

    /**
     * Buffer capacity
     * @return capacity
     */
    int getCapacity();

    /**
     * Number of markers in buffer
     * @return Number of markers
     */
    int getMarkersSize();

    /**
     * Get if buffer is full.
     * @return True if buffer is full
     */
    boolean isFull();

    /**
     * Clear buffer
     */
    void clear();


}
