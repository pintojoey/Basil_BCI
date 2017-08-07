package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;

public interface ISegmentation {
	List<EEGDataPackage> split(EEGDataPackage eegData);
	int getSegmentSize();
	void setSampling(int sampling);
}
