/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zcu.kiv.eeg.gtn.data.providers.messaging;

import java.util.Arrays;

/**
 * @author Prokop
 */
public class EEGDataMessage extends EEGMessage {

    private final EEGMarker[] markers;

    private final float[][] data;

    public EEGDataMessage(MessageType msgType, int messageNumber, EEGMarker[] markers, float[][] data) {
        super(msgType, messageNumber);
        this.markers = markers;
        this.data = data;
    }

    public EEGMarker[] getMarkers() {

        return markers;
    }

    public float[][] getData() {
        return data;
    }
    
    @Override
    public String toString() {
		return "Markers: " + Arrays.toString(markers) + "\n " + "Data: " + Arrays.deepToString(data);
    	
    }
}