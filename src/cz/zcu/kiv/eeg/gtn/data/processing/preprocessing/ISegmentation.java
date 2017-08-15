package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;


/**
 * Interface for segmentation 
 * - epoch extraction for ERP data
 * - fixed size segmentation for continuous EEG data
 * 
 * @author lvareka
 *
 */
public interface ISegmentation {

	/**
	 * Splits package
	 * @param eegData package with data
	 * @return list of packages
	 */
	List<EEGDataPackage> split(EEGDataPackage eegData);

	/**
	 * Segment size in samples
	 * @return segment size
	 */
	int getSegmentSize();

	/**
	 * Sets sampling frequency
	 * @param sampling sampling frequency in samples/second
	 */
	void setSampling(int sampling);
}
