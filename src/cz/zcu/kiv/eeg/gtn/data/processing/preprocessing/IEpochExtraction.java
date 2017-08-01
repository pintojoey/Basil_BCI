package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.EEGDataPackage;

public interface IEpochExtraction {
	public List<EEGDataPackage> extractEpochs(EEGDataPackage eegData);
}
