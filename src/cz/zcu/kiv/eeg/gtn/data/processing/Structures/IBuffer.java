package cz.zcu.kiv.eeg.gtn.data.processing.Structures;

import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;

/**
 * Created by Tomas Prokop on 31.07.2017.
 */
public interface IBuffer {

    /**
     * Adds data into buffer
     *
     * @param data    data
     * @param markers markers
     */
    void add(float[][] data, EEGMarker markers);

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
    EEGDataPackage get(int size);

    /**
     * Get all markers
     * @return
     */
    EEGMarker getMarkers();

    /**
     * get required number of markers
     * @param count
     * @return
     */
    EEGMarker getMarkers(int count);

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
