package cz.zcu.kiv.eeg.basil.data.providers.lsl;

import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.eeg.basil.data.listeners.DataProviderListener;
import cz.zcu.kiv.eeg.basil.data.listeners.EEGMessageListener;
import cz.zcu.kiv.eeg.basil.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker;

/**
 * Provides EEG data with markers using
 * two threads that collect both data streams 
 * and subsequently join the information together. 
 * 
 * @author lvareka
 *
 */
public class LSLEEGDataMessageProvider extends AbstractDataProvider  {
	private final int BLOCK_SIZE = 5000; /* size of a single data block before  transferring to observers */
	private volatile double[][] data; /* data  - BLOCK*/
	private volatile List<EEGMarker> markers; /* stimuli markers */ 
	private LSLEEGCollector eegCollector;   /* provider for EEG data */
	private LSLMarkerCollector markerCollector;  /* provider for markers */
	private int dataPointer; /* points at the current EEG data sample in an array */ 
	private int blockCounter; /* counter for blocks that have been sent to observers */
	
	public LSLEEGDataMessageProvider() {
		this.data           = null;
		this.markers        = new ArrayList<EEGMarker>();
		this.eegCollector    = new LSLEEGCollector(this);
		this.markerCollector = new LSLMarkerCollector(this);
		this.dataPointer    = 0;
		this.blockCounter   = 0;
	}

	@Override
	public void run() {
		synchronized (super.dataProviderListeners) {
			for (DataProviderListener ls : super.dataProviderListeners) {
				ls.dataReadStart();
			}
		}

		this.eegCollector.start();
		this.markerCollector.start();

		synchronized (super.dataProviderListeners) {
			for (DataProviderListener ls : super.dataProviderListeners) {
				ls.dataReadEnd();
			}
		}
    }

	@Override
	public void stop() {
		this.eegCollector.terminate();
		this.markerCollector.terminate();
	}

    /**
	 * One EEG sample (from all channels has) has been received
	 * -> update
	 * 
	 * @param eegSample
	 */
	public synchronized void addEEGSample(float[] eegSample) {
		if (data == null)
			data = new double[eegSample.length][BLOCK_SIZE];
		
		// fill data array using the obtained sample
		for (int i = 0; i < eegSample.length; i++)
			data[i][dataPointer] = eegSample[i];
		
		
		dataPointer++;
		
		/* if maximum size is reached, transfer the data */
		if (dataPointer == BLOCK_SIZE) {
			EEGDataMessage eegDataMessage = new EEGDataMessage(blockCounter, markers.toArray(new EEGMarker[markers.size()]), data);
			for (EEGMessageListener ls : super.eegMessageListeners) {
				ls.dataMessageSent(eegDataMessage);
			}
			this.blockCounter++;
			this.data = new double[eegSample.length][BLOCK_SIZE];
			this.markers = new ArrayList<EEGMarker>();
			this.dataPointer = 0;
		}
	}

	/**
	 * Marker has been received, update the corresponding list
	 * 
	 * @param marker
	 */
	public synchronized void addMarker(String[] marker) {
		EEGMarker newMarker = new EEGMarker(marker[0], dataPointer);
		this.markers.add(newMarker);
	}

}
