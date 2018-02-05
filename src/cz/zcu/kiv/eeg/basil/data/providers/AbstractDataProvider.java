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

    /**
     * Used to generate message ID
     */
    private int msgCounter = 0;

    /**
     * EEG message listeners listen to start, stop and data messages from data provider
     * @see AbstractDataProvider
     * @see EEGMessageListener
     */
    protected List<EEGMessageListener> eegMessageListeners = Collections.synchronizedList(new ArrayList<EEGMessageListener>());

    /**
     * Data provider listeners listen to start, stop and error calls from data provider
     * @see AbstractDataProvider
     * @see DataProviderListener
     */
    protected List<DataProviderListener> dataProviderListeners = Collections.synchronizedList(new ArrayList<DataProviderListener>());

    /**
     * metadata provider loads additional metadata if necessary. It is optional.
     */
    protected IMetadataProvider metadataProvider;

    /**
     * Starts data providing
     */
    public abstract void run();

    /**
     * Stops data providing
     */
    public abstract void stop();

    /**
     * Add EEG message listener
     * @see EEGMessageListener
     * @param listener EEG message listener
     */
    public synchronized void addEEGMessageListener(EEGMessageListener listener){
        eegMessageListeners.add(listener);
    }

    /**
     * Delete EEG message listener
     * @see EEGMessageListener
     * @param listener EEg message listener
     */
    public synchronized void removeEEGMessageListener(EEGMessageListener listener){
        if(eegMessageListeners.contains(listener)){
            eegMessageListeners.remove(listener);
        }
    }

    /**
     * Add Data provider listener
     * @see DataProviderListener
     * @param listener Data provider listener
     */
    public synchronized void addDataProviderListener(DataProviderListener listener){
        dataProviderListeners.add(listener);
    }

    /**
     * Remove Data provider listener
     * @see DataProviderListener
     * @param listener Data provider listener
     */
    public synchronized void removeDataProviderListener(DataProviderListener listener){
        if(dataProviderListeners.contains(listener)){
            dataProviderListeners.remove(listener);
        }
    }

    /**
     * Get ID of next EEG message
     * @return ID
     */
    protected int getMessageId(){
        return msgCounter++;
    }

    /**
     * Returns implementation of IMetadataProvider
     * @see IMetadataProvider
     * @return metadata provider
     */
    public IMetadataProvider getMetadataProvider() {
        return metadataProvider;
    }

    /**
     * Set implementation of metadata provider
     * @see IMetadataProvider
     * @param metadataProvider metadata provider
     */
    public void setMetadataProvider(IMetadataProvider metadataProvider) {
        this.metadataProvider = metadataProvider;
    }
}
