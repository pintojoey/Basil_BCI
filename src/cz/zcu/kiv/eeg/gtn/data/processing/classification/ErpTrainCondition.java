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
        boolean isTarget = isTraget(expectedClass, marker);
        return  canAddSample(isTarget);
    }

    public int getTargetCnt() {
        return targetCnt;
    }

    public int getNontargetCnt() {
        return nontargetCnt;
    }

    private boolean canAddSample(boolean isTarget){
        if (isTarget) {
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
        boolean isTarget = isTraget(expectedClass, marker);
        if (canAddSample(isTarget)) {
            featureVectors.add(fv);
            expectedClasses.add(isTarget ? 1.0 : 0.0);
        }
    }

    private boolean isTraget(int expectedClass, String marker){
        return ((expectedClass) + "").equals(marker);
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
