package cz.zcu.kiv.eeg.gtn.data.listeners;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import java.util.List;

/**
 * Created by Tomas Prokop on 15.08.2017.
 */
public interface EEGDataProcessingListener {

    void dataPreprocessed(List<EEGDataPackage> packs);
    void featuresExtracted(EEGDataPackage pack);
    void dataClassified(EEGDataPackage pack);
}
