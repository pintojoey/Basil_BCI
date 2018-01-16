package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms;

import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;

/**
 * Created by Tomas Prokop on 16.01.2018.
 */
public class IntervalSelection implements IPreprocessing {

    private final int startIndex;
    private final int samples;

    public IntervalSelection(int startIndex, int samples) {
        this.startIndex = startIndex;
        this.samples = samples;
    }

    @Override
    public EEGDataPackage preprocess(EEGDataPackage inputPackage) {
        int start = startIndex, samp = samples;

        double[][] originalEegData = inputPackage.getData();
        int dataLen = originalEegData[0].length;
        if(startIndex < 0 || startIndex >= dataLen)
            start = 0;

        if(samples < 0 || samples >= dataLen - start + 1)
            samp = dataLen - start + 1;

        double[][] reducedData = new double[originalEegData.length][samp];

        for (int i = 0; i < originalEegData.length; i++) {
            System.arraycopy(originalEegData[i], start, reducedData[i], 0, samp);
        }

        inputPackage.setData(reducedData, this);

        return inputPackage;
    }
}
