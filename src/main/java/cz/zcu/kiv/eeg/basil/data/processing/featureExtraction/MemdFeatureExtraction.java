package cz.zcu.kiv.eeg.basil.data.processing.featureExtraction;

import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import hht.DecompositionRunner;
import hht.HhtSimpleRunner;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;
import testing.HhtDataRunner;

import java.util.Arrays;
import java.util.Collections;
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
            Double[][][] ff = features.toArray(new Double[features.size()][][]);
            double[] flat = ArrayUtil.flattenDoubleArray(ff);
            int[] shape = {ff.length, ff[0].length, ff[0][0].length};
            INDArray arr = Nd4j.create(flat, shape);

            return new FeatureVector(arr);
        } catch (Exception e) {
            e.printStackTrace();
            //TODO handle
        }
        return null;
    }

    @Override
    public int getFeatureDimension() {
        return 0;
    }
}
