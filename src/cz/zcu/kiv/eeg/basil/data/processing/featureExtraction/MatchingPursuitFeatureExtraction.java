package cz.zcu.kiv.eeg.basil.data.processing.featureExtraction;

import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eegdsp.matchingpursuit.MatchingPursuit;

/**
 * Feature extractor using Matching Pursuit algorithm implemented in eegdsp library.
 * @author Karel Silhavy
 *
 */
public class MatchingPursuitFeatureExtraction implements IFeatureExtraction {

	/**
	 * Subsampling factor
	 */
	private int downSmplFactor = 8;

	/**
	 * Private instance of singleton.
	 */
	private MatchingPursuit instance;

	private int epochSize = 0;

	private int numOfChannels = 0;
		
	/**
	 * Prepare instance for use.
	 * Default number of iterations is 4.
	 */
	public MatchingPursuitFeatureExtraction() {
		this.instance = MatchingPursuit.getInstance();
		this.instance.setIterationCount(4);
	}
	
	/**
	 * Prepare instance for use, adjustable number of iterations.
	 */
	public MatchingPursuitFeatureExtraction(int numberOfIterations) {
		this.instance = MatchingPursuit.getInstance();
		this.instance.setIterationCount(numberOfIterations);
	}
	
	
	@Override
	public double[] extractFeatures(EEGDataPackage data) {

		double[][] channels = data.getData();
		numOfChannels = channels.length;
		epochSize = channels[0].length;
		int numberOfChannels = channels.length;
		double[] signal = new double[getFeatureDimension()];
		double[] processingPart = new double[epochSize / downSmplFactor];
		
		int k = 0;
		for(int i = 0; i < numberOfChannels; i++) {
			for(int j = 0; j < epochSize / downSmplFactor; j++) {
				processingPart[j] = channels[i][j * downSmplFactor];
			}
			processingPart = instance.processSignal(processingPart).getReconstruction();
			for(int j = 0; j < processingPart.length; j++) {
				signal[k] = processingPart[j];
				k++;
			}
		}

		return signal;
		
	}

	@Override
	public int getFeatureDimension() {
		int lenghtOfProcessedEpoch = epochSize / downSmplFactor;
		int i = 1;
		while(lenghtOfProcessedEpoch > Math.pow(2, i)) {
			i++;
		}
		return ((int) Math.pow(2, i) * numOfChannels);
	}
	
	
	/**
	 * Sets number of iterations.
	 * 
	 * Each iteration adds one atom.
	 * 
	 * @param iterationCount number of iterations
	 */
	public void setIterationCount(int iterationCount) {
		if (iterationCount < 1) {
			throw new IllegalArgumentException("Number of iterations must be >= 1");
		}
		this.instance.setIterationCount(iterationCount);
	}
	
	
	/**
	 * Sets size of epoch to use for feature extraction
	 * 
	 * @param epochSize	size of epoch to use
	 */
	public void setEpochSize(int epochSize) {
		if (epochSize > 0) {
			this.epochSize = epochSize;
		} else {
			throw new IllegalArgumentException("Epoch Size must be > 0");
		}
	}
	
	/**
	 * Setter for downSmplFactor attribute. It requires value greater than 0.
	 * @param downSmplFactor
	 * @throws IllegalArgumentException
	 */
	public void setDownSmplFactor(int downSmplFactor) {
		if(downSmplFactor >= 1) {
			this.downSmplFactor = downSmplFactor;
		}
		else {
			throw new IllegalArgumentException("Wrong input value! Sub-sampling factor must be >= 1.");
		}
	}
	
	/**
	 * Gets number of iterations.
	 * 
	 * @return iterationCount number of iterations
	 */
	public int getIterationCount() {
		return this.instance.getIterationCount();
	}
	
	/**
	 * Getter for epochSize attribute.
	 * @return epochSize epochSize
	 */
	public int getEpochSize() {
		return epochSize;
	}
	
	/**
	 * Getter for downSmplFactor attribute.
	 * @return downSmplFactor downSmplFactor
	 */
	public int getDownSmplFactor() {
		return downSmplFactor;
	}
}
