package cz.zcu.kiv.eeg.gtn.data.processing.classification;

import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.FeatureVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomas Prokop on 15.08.2017.
 */
public class ErpTrainCondition implements ITrainCondition {

    private int targetCnt = 0;
    private int nontargetCnt = 0;

    private ArrayList<FeatureVector> featureVectors = new ArrayList<>();

    private ArrayList<Double> expectedClasses = new ArrayList<>();

    @Override
    public boolean canAddSample(int expectedClass, String marker) {
        int val = 0;
        try {
            val = Integer.parseInt(marker);
        } catch (NumberFormatException e) {
            return false;
        }

        if (val == expectedClass) {
            if (targetCnt <= nontargetCnt)
                return true;
        } else {
            if (nontargetCnt <= targetCnt)
                return true;
        }

        return false;
    }

    @Override
    public void addSample(FeatureVector fv, int expectedClass, String marker) {
        if (canAddSample(expectedClass, marker)) {
            featureVectors.add(fv);
            expectedClasses.add((double) expectedClass);
        }
    }

    @Override
    public List<FeatureVector> getFeatureVectors() {
        return featureVectors;
    }

    @Override
    public List<Double> getExpectedClasses() {
        return expectedClasses;
    }
}
