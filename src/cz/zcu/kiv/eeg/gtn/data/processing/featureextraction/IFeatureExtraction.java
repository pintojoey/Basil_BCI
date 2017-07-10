package cz.zcu.kiv.eeg.gtn.data.processing.featureextraction;

/**
 * 
 * Interface for extracting features from an epoch
 * 
 * @author Lukas Vareka
 *
 */
public interface IFeatureExtraction {
	
	/**
	 * 
	 * @param epoch source epoch 
	 * @return feature vector
	 */
    double[] extractFeatures(double[][] epoch);
	int getFeatureDimension();
}
