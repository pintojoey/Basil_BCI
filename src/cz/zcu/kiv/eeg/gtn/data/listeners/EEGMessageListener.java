package cz.zcu.kiv.eeg.gtn.data.listeners;

import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;

/**
 * 
 * Listener for EEG data events
 *  
 * Created by Tomas Prokop on 01.08.2017.
 */
public interface EEGMessageListener {
	
	/**
	 * Start -> a start package with metadata about EEG received
	 * @param msg message containing metadata about current EEG connection
	 */
    void startMessageSent(EEGStartMessage msg);
    
    /**
     * A common data package received -> collect and process the data
     * @param msg message containing data (channels x samples + possibly markers)
     */
    void dataMessageSent(EEGDataMessage msg);
    
    /**
     * A stop message received. Close the connection and finish related operations.
     * @param msg stop message 
     */
    void stopMessageSent(EEGStopMessage msg);
}
