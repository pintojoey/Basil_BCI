package cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction;

import java.util.ArrayList;
import java.util.Arrays;

import cz.zcu.kiv.eeg.gtn.data.processing.math.SignalProcessing;

/**
 * Represents a feature vector as 
 * an input for classification
 * 
 * Created by Tomas Prokop on 07.08.2017.
 */
public class FeatureVector {
    private double[] featureVector = null;

    /**
     * Join two feature vectors
     * 
     * @param new feature vector
     */
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
        this.featureVector = SignalProcessing.normalize(featureVector);
    }
}
