package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;

import java.util.List;

/**
 * Created by Tomas Prokop on 31.07.2017.
 */
public interface IDataPreprocessor {
    List<EEGDataPackage> preprocessData();
    void setMetadata(EEGStartMessage msg);
}
