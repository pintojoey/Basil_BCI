package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;

public class EpochExtraction  implements IEpochExtraction {
	private double preStimulus;
	private double postStimulus;

	
	
	public EpochExtraction(double preStimulus, double postStimulus) {
		super();
		this.preStimulus = preStimulus;
		this.postStimulus = postStimulus;
	}



	@Override
	public List<EEGDataPackage> extractEpochs(EEGDataPackage eegData) {
		List<EEGDataPackage> epochs = new ArrayList<EEGDataPackage>();
		return null;
		
	}

}
