package cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Tomas Prokop on 07.08.2017.
 */
public class FeatureVector {
    private double[] featureVector = null;

    public void addFeatures(double[] features) {
        if (featureVector == null)
            featureVector = features;
        else {
            double[] copy = new double[featureVector.length + features.length];
            System.arraycopy(featureVector, 0, copy, 0, featureVector.length);
            System.arraycopy(features, 0, copy, featureVector.length, features.length);
            featureVector = copy;
        }
    }

    public double[] getFeatureVector() {
        return featureVector;
    }

    public void normalize() {
        //TODO implement normalization
    }
}
