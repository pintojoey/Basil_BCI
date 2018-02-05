package cz.zcu.kiv.eeg.basil.data.processing.featureExtraction;

import cz.zcu.kiv.eeg.basil.data.processing.math.IFilter;
import cz.zcu.kiv.eeg.basil.data.processing.math.IirBandpassFilter;
import cz.zcu.kiv.eeg.basil.data.processing.math.SignalProcessing;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;

public class FilterFeatureExtraction implements IFeatureExtraction {

    private final int downSampleFactor;
    private final IFilter filter;
    private int numberOfChannels = 0;
    private int epochSize = 0;

    public FilterFeatureExtraction(int downSampleFactor, IFilter filter) {
        this.downSampleFactor = downSampleFactor;
        this.filter = filter;
    }

    @Override
    public double[] extractFeatures(EEGDataPackage data) {
        numberOfChannels = data.getChannelNames().length;
        double[][] channels = data.getData();
        epochSize = channels[0].length;
        double[] features = new double[channels[0].length * numberOfChannels];
        int i = 0;

        for (double[] channel : channels) {
            for (int j = 0; j < channel.length; j++) {
                features[i * channel.length + j] = filter.getOutputSample(channel[j]);
            }
            i++;
        }

        features = SignalProcessing.normalize(features);
        return features;
    }

    @Override
    public int getFeatureDimension() {
        return numberOfChannels * epochSize / downSampleFactor;
    }

}
