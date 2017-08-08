package cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;

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
	 * @param data data
	 * @return array of features
	 */
    double[] extractFeatures(EEGDataPackage data);
	int getFeatureDimension();
}
