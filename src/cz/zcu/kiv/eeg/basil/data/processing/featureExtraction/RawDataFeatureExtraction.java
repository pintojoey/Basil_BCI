package cz.zcu.kiv.eeg.basil.data.processing.featureExtraction;

import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;

/**
 * Created by Tomas Prokop on 08.04.2018.
 */
public class RawDataFeatureExtraction implements IFeatureExtraction {

    @Override
    public FeatureVector extractFeatures(EEGDataPackage data) {
        FeatureVector fv = new FeatureVector(data.getData());
        return fv;
    }

    @Override
    public int getFeatureDimension() {
        return 0;
    }
}
