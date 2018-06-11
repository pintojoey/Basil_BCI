package cz.zcu.kiv.eeg.basil.data.processing.featureExtraction;

import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import hht.DecompositionRunner;
import hht.HhtSimpleRunner;
import testing.HhtDataRunner;

import java.util.List;

/**
 * Created by Tomas Prokop on 20.02.2018.
 */
public class MemdFeatureExtraction implements IFeatureExtraction {

    private DecompositionRunner memd = new DecompositionRunner();

    public void setMaxImfs(int max){
        memd.setMaxImfs(max);
    }

    public int getMaxImfs(){
        return memd.getMaxImfs();
    }

    @Override
    public FeatureVector extractFeatures(EEGDataPackage data) {
        if(data == null || data.getData() == null) {
            throw new IllegalArgumentException("data are null");
        }

        try {
            List<double[][]> features = memd.runMemdWithDefaultCfg(data.getData());
        } catch (Exception e) {
            e.printStackTrace();
            //TODO handle
        }

        FeatureVector fv = new FeatureVector();
        //fv.

        return null;
    }

    @Override
    public int getFeatureDimension() {
        return 0;
    }
}
