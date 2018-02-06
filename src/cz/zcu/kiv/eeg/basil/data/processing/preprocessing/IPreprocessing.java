package cz.zcu.kiv.eeg.basil.data.processing.preprocessing;

import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;

/**
 * Created by Tomas Prokop on 04.07.2017.
 */
public interface IPreprocessing {

    /**
     * Performs the data preprocessing method
     * @param data data to preprocess
     * @return data package with preprocessed data
     */
    EEGDataPackage preprocess(EEGDataPackage data);
}
