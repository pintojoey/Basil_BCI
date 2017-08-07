package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;

public class EpochExtraction implements ISegmentation {

	private final int preStimulus;
	private final int postStimulus;
	private int sampling;
	
	public EpochExtraction(int preStimulus, int postStimulus) {
		super();
		this.preStimulus = preStimulus;
		this.postStimulus = postStimulus;
	}

	@Override
	public List<EEGDataPackage> split(EEGDataPackage eegData) {
		List<EEGDataPackage> epochs = new ArrayList<EEGDataPackage>();
		return null;
	}

	@Override
	public int getSegmentSize() {
		return postStimulus;
	}

    @Override
    public void setSampling(int sampling) {
        this.sampling = sampling;
    }

    public int getPreStimulus() {
        return preStimulus;
    }

    public int getPostStimulus() {
        return postStimulus;
    }

    public int getSampling() {
        return sampling;
    }
}
