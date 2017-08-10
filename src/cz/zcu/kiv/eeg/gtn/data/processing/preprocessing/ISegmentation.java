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
	List<EEGDataPackage> split(EEGDataPackage eegData);
	int getSegmentSize();
	void setSampling(int sampling);
}
