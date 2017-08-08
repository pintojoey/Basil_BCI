package cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction;

import cz.zcu.kiv.eeg.gtn.data.processing.math.ButterWorthFilter;
import cz.zcu.kiv.eeg.gtn.data.processing.math.IFilter;
import cz.zcu.kiv.eeg.gtn.data.processing.math.SignalProcessing;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;

public class FilterFeatureExtraction implements IFeatureExtraction {

    private final int downSampleFactor;
    private IFilter filter;
    private int numberOfChannels = 0;
    private int epochSize = 0;

    public FilterFeatureExtraction(int downSampleFactor) {
        this.downSampleFactor = downSampleFactor;
        this.filter = new ButterWorthFilter();
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
