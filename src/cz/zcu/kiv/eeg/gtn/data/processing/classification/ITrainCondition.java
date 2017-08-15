package cz.zcu.kiv.eeg.gtn.data.processing.classification;

import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.FeatureVector;

import java.util.List;

/**
 * Created by Tomas Prokop on 15.08.2017.
 */
public interface ITrainCondition {
    boolean canAddSample(int expectedClass, String marker);
    void addSample(FeatureVector fv, int expectedClass, String marker);
    List<FeatureVector> getFeatureVectors();
    List<Double> getExpectedClasses();
}
