package cz.zcu.kiv.eeg.basil.data.listeners;

import java.util.List;

import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;

/**
 * Created by Tomas Prokop on 15.08.2017.
 */
public interface EEGDataProcessingListener {

    void dataPreprocessed(List<EEGDataPackage> packs);
    void featuresExtracted(EEGDataPackage pack);
    void dataClassified(EEGDataPackage pack);
}
