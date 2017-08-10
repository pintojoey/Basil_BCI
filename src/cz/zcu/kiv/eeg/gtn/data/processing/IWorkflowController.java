package cz.zcu.kiv.eeg.gtn.data.processing;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;

/**
 * 
 * Workflows serves as an engine that connects data providers and
 * processing methods together.
 * 
 *  Created by Tomas Prokop on 04.07.2017.
 *  
 */
public interface IWorkflowController {
	/**
	 * Runs the whole processing toolchain on the available data
	 */
    void processData();
    
    /**
     * Start message received
     * @param start
     */
    void start(EEGStartMessage start);
    
    /**
     * Data message received
     * @param stop
     */
    void stop(EEGStopMessage stop);
    
    /**
     * Store the current data message
     * @param data
     */
    void storeData(EEGDataMessage data);
}
