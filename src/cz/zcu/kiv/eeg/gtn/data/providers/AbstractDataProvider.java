package cz.zcu.kiv.eeg.gtn.data.providers;

import cz.zcu.kiv.eeg.gtn.data.listeners.EEGMessageListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Data provider collects the data on-line or off-line
 * and informs other layers using EEGMessageListener.
 * 
 * Provides basic metadata.
 * 
 * Created by Tomas Prokop on 04.07.2017.
 */
public abstract class AbstractDataProvider implements Runnable {

    protected List<EEGMessageListener> listeners = new ArrayList<EEGMessageListener>();

    private String[] availableChannels;
    
    protected int samplingRate;

    public abstract void run();

    public abstract void stop();

    public void addListener(EEGMessageListener listener){
        listeners.add(listener);
    }

    public String[] getAvailableChannels() {
        return availableChannels;
    }

    protected void setAvailableChannels(String[] availableChannels) {
        this.availableChannels = availableChannels;
    }

	public int getSamplingRate() {
		return samplingRate;
	}
    
    
}
