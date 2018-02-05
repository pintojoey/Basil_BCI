package cz.zcu.kiv.eeg.basil.data.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.zcu.kiv.eeg.basil.data.listeners.DataProviderListener;
import cz.zcu.kiv.eeg.basil.data.listeners.EEGMessageListener;


/**
 * Data provider collects the data on-line or off-line
 * and informs other layers using EEGMessageListener.
 * 
 * Provides basic metadata.
 * 
 * Created by Tomas Prokop on 04.07.2017.
 */
public abstract class AbstractDataProvider implements Runnable {

    private int msgCounter = 0;

    protected List<EEGMessageListener> eegMessageListeners = Collections.synchronizedList(new ArrayList<EEGMessageListener>());

    protected List<DataProviderListener> dataProviderListeners = Collections.synchronizedList(new ArrayList<DataProviderListener>());

    protected String[] availableChannels;
    
    protected int samplingRate;

    protected boolean trainingMode;

    protected IMetadataProvider metadataProvider;

    public abstract void run();

    public abstract void stop();

    public synchronized void addEEGMessageListener(EEGMessageListener listener){
        eegMessageListeners.add(listener);
    }

    public synchronized void removeEEGMessageListener(EEGMessageListener listener){
        if(eegMessageListeners.contains(listener)){
            eegMessageListeners.remove(listener);
        }
    }

    public synchronized void addDataProviderListener(DataProviderListener listener){
        dataProviderListeners.add(listener);
    }

    public synchronized void removedataProviderListener(DataProviderListener listener){
        if(dataProviderListeners.contains(listener)){
            dataProviderListeners.remove(listener);
        }
    }

    protected int getMessageId(){
        return msgCounter++;
    }

    public String[] getAvailableChannels() {
        return availableChannels;
    }

	public int getSamplingRate() {
		return samplingRate;
	}

    public boolean isTrainingMode() {
        return trainingMode;
    }

    public IMetadataProvider getMetadataProvider() {
        return metadataProvider;
    }

    public void setMetadataProvider(IMetadataProvider metadataProvider) {
        this.metadataProvider = metadataProvider;
    }
}
