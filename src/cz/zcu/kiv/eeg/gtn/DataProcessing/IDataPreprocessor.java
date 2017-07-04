package cz.zcu.kiv.eeg.gtn.DataProcessing;

import cz.zcu.kiv.eeg.gtn.Data.EEGDataPackage;

/**
 * Created by Tomas Prokop on 04.07.2017.
 */
public interface IDataPreprocessor {
    EEGDataPackage preprocess(EEGDataPackage data);
}
