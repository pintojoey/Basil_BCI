package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import java.util.Map;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;

/**
 * Created by Tomas Prokop on 04.07.2017.
 */
public interface IPreprocessing {
    EEGDataPackage preprocess(EEGDataPackage data);
}
