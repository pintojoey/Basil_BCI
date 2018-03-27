package cz.zcu.kiv.eeg.basil.data.processing.featureExtraction;

import cz.zcu.kiv.eeg.basil.data.processing.math.SignalProcessing;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;

public class WindowedMeansFeatureExtraction implements IFeatureExtraction {

	// time intervals after stimulus in seconds to extract
	private double[][] windows = {{0.2, 0.25}, {0.25, 0.3}, {0.25, 0.3}, {0.3, 0.35}, {0.35, 0.4},
			{0.4, 0.45}, {0.45, 0.5}, {0.5, 0.55}, {0.55, 0.6}, {0.6, 0.65}, {0.65, 0.7}};

	private int numOfChannels = 0;

	private int postStimulus = 750;

	public WindowedMeansFeatureExtraction(int postStimulus) {
		this.postStimulus = postStimulus;
	}

	@Override
	public FeatureVector extractFeatures(EEGDataPackage data) {
		double[][] epoch = data.getData();
		numOfChannels = epoch.length;
		double[] features = new double[numOfChannels * windows.length];
		
		for (int i = 0; i < numOfChannels; i++) {
			for (int j = 0; j < windows.length; j++) {
				double avg = averageInterval(windows[j], epoch[i], data.getMetadata().getSampling());
				features[i * windows.length + j] = avg;
			}
		}
		features = SignalProcessing.normalize(features);
		FeatureVector fv = new FeatureVector(features);
		return fv;
	}

	private double averageInterval(double[] fromToSec, double[] epoch, double sampling) {
		int first_sample = (int) Math.round(sampling * fromToSec[0]);
		int second_sample = (int) Math.round(sampling * fromToSec[1]);
		
		if (first_sample > second_sample || second_sample > postStimulus)
			throw new IllegalArgumentException("Incorrectly selected time windows");
		double sum = 0;
		for (int i = first_sample; i < second_sample; i++ ) {
			sum += epoch[i];
		}
		return sum / (second_sample - first_sample);
	}

	@Override
	public int getFeatureDimension() {
		return numOfChannels * windows.length;
	}

}
