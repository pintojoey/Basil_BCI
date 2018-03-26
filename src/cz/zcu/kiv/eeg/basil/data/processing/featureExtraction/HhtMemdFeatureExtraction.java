package cz.zcu.kiv.eeg.basil.data.processing.featureExtraction;

import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import hht.HhtSimpleRunner;
import testing.HhtDataRunner;

/**
 * Created by Tomas Prokop on 20.02.2018.
 */
public class HhtMemdFeatureExtraction implements IFeatureExtraction {

    @Override
    public double[] extractFeatures(EEGDataPackage data) {


        return new double[0];
    }

    @Override
    public int getFeatureDimension() {
        return 0;
    }
}
