package cz.zcu.kiv.eeg.gtn.data.processing;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;

/**
 * Created by Tomas Prokop on 04.07.2017.
 */
public interface IWorkflowController {
    void processData();
    void start(EEGStartMessage start);
    void stop(EEGStopMessage stop);
    void storeData(EEGDataMessage data);
}
