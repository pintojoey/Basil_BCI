package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;

public interface IEpochExtraction {
	public List<EEGDataPackage> extractEpochs(EEGDataPackage eegData);
}
