package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;

/**
 * Extract epochs using stimuli markers. Each epoch
 * is defined by the occurrence of stimuli (offset) and
 * preStimulus and postStimulus intervals (in milliseconds)
 * 
 * @author lvareka
 *
 */
public class EpochExtraction implements ISegmentation {

	private final int preStimulus;  /* time before the stimulus onset in ms */
	private final int postStimulus; /* time after the stimulus onset in ms */
	
	public EpochExtraction(int preStimulus, int postStimulus) {
		super();
		this.preStimulus = preStimulus;
		this.postStimulus = postStimulus;
	}

	@Override
	public List<EEGDataPackage> split(EEGDataPackage eegData) {
		List<EEGDataPackage> epochs = new ArrayList<>();
		List<EEGMarker> markers = eegData.getMarkers();
		double[][]        data  = eegData.getData();
		double sampling = eegData.getMetadata().getSampling();
		
		for (EEGMarker currentMarker: markers) {
			int startSample =  - (int)((0.001 * this.preStimulus) /* time in s */ * sampling);
			int endSample = (int) ((0.001 * this.postStimulus) /* time in s */ * sampling);
			int offset = currentMarker.getOffset();
			double[][] epochData = new double[data.length][endSample - startSample];
			
			if (offset + startSample < 0) {
				System.err.println("Epoch outside of the expected range");
				continue; /* epoch prestimulus offset outside of the range */
			}
			for (int i = 0; i < data.length; i++) {
				System.arraycopy(data[i], offset + startSample , epochData[i], 0, endSample - startSample);
			}
			epochs.add(new EEGDataPackage(epochData, Arrays.asList(currentMarker),eegData.getMetadata()));
		}
		return epochs;
	}

	@Override
	public int getSegmentSize() {
		return (int)(0.001 * this.preStimulus * this.postStimulus);
	}

    public int getPreStimulus() {
        return preStimulus;
    }

    public int getPostStimulus() {
        return postStimulus;
    }
}
