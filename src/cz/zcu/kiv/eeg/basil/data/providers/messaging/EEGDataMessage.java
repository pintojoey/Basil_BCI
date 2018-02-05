/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zcu.kiv.eeg.basil.data.providers.messaging;

import java.util.Arrays;

/**
 * @author Prokop
 */
public class EEGDataMessage extends EEGMessage {

    private final EEGMarker[] markers;

    private final double[][] data; /* NUMBER_OF_CHANNELS x BLOCK_SIZE */

    public EEGDataMessage(MessageType msgType, int messageNumber, EEGMarker[] markers, double[][] data) {
        super(msgType, messageNumber);
        this.markers = markers;
        this.data = data;
    }

    public EEGMarker[] getMarkers() {

        return markers;
    }

    public double[][] getData() {
        return data;
    }
    
    @Override
    public String toString() {
		return "Markers: " + Arrays.toString(markers) + "\n " + "Data: " + Arrays.deepToString(data);
    	
    }
}