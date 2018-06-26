package cz.zcu.kiv.eeg.basil.data.processing.preprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockExecute;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockOutput;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockProperty;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockType;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker;

import static cz.zcu.kiv.WorkflowDesigner.Type.NUMBER;

/**
 * Extract epochs using stimuli markers. Each epoch
 * is defined by the occurrence of stimuli (offset) and
 * preStimulus and postStimulus intervals (in milliseconds)
 * 
 * @author lvareka
 *
 */
public class EpochExtraction implements ISegmentation {

	private int preStimulus;  /* time before the stimulus onset in ms */

	private int postStimulus; /* time after the stimulus onset in ms */


	public EpochExtraction(){
		//Required Empty Default Constructor for Workflow Designer
	}
	
	public EpochExtraction(int preStimulus, int postStimulus) {
		super();
		this.preStimulus = preStimulus;
		this.postStimulus = postStimulus;
	}
	private void process(){
		epochExtraction=this;
	}

    private ISegmentation epochExtraction;

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
			epochs.add(new EEGDataPackage(epochData, Arrays.asList(currentMarker), eegData.getChannelNames(), eegData.getMetadata()));
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
