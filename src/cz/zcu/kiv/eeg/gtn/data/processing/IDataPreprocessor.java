package cz.zcu.kiv.eeg.gtn.data.processing;

import cz.zcu.kiv.eeg.gtn.data.EEGDataPackage;

/**
 * Created by Tomas Prokop on 04.07.2017.
 */
public interface IDataPreprocessor {
    EEGDataPackage preprocess(EEGDataPackage data);
}
