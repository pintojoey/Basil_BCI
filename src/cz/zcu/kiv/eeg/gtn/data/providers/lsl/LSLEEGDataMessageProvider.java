package cz.zcu.kiv.eeg.gtn.data.providers.lsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import cz.zcu.kiv.eeg.gtn.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.MessageType;

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
	private volatile float[][] data; /* data  - BLOCK*/
	private volatile List<EEGMarker> markers; /* stimuli markers */ 
	private LSLEEGCollector eegCollector;   /* provider for EEG data */
	private LSLMarkerCollector markerCollector;  /* provider for markers */
	private int dataPointer; /* points at the current EEG data sample in an array */ 
	private int blockCounter; /* counter for blocks that have been sent to observers */
	
	public LSLEEGDataMessageProvider(Observer observer) {
		this.data           = new float[BLOCK_SIZE][];
		this.markers        = new ArrayList<EEGMarker>();
		this.eegCollector    = new LSLEEGCollector(this);
		this.markerCollector = new LSLMarkerCollector(this);
		this.dataPointer    = 0;
		this.blockCounter   = 0;
		this.addObserver(observer);
	}

	@Override
	public void run() {
		this.eegCollector.start();
		this.markerCollector.start();
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
		data[dataPointer] = eegSample;
		dataPointer++;
		
		/* if maximum size is reached, transfer the data */
		if (dataPointer == BLOCK_SIZE) {
			EEGDataMessage eegDataMessage = new EEGDataMessage(MessageType.DATA, blockCounter, markers.toArray(new EEGMarker[markers.size()]), data);
			this.notifyObservers(eegDataMessage);
			this.setChanged();
			this.blockCounter++;
			this.data    = new float[BLOCK_SIZE][];
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
