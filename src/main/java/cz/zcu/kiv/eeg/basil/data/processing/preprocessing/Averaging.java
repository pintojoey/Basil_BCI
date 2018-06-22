package cz.zcu.kiv.eeg.basil.data.processing.preprocessing;

import java.util.List;

import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockExecute;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockInput;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockOutput;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockType;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker;

/**
 * Average a list of epochs using one stimuli marker
 * @author lvareka
 *
 */
@BlockType(type="Averaging",family = "Preprocessing")
public class Averaging {

	@BlockInput(name = "Markers",type="EEGMarker[]")
	private List<EEGMarker> markers;

	@BlockOutput(name="Averaging",type="Averaging")
    private Averaging averaging;

	public Averaging(){
		//Required Empty Default Constructor for Workflow Designer
	}

	@BlockExecute
	private void process(){
	    averaging=this;
    }
	
	public Averaging(List<EEGMarker> markers) {
		this.markers = markers;
		
	}

	public EEGDataPackage average(List<EEGDataPackage> epochs) {
		if (epochs == null || epochs.size() == 0 || this.markers == null)
			return null;
		double[][] firstEpoch = epochs.get(0).getData();
		double[][] average = new double[firstEpoch.length][firstEpoch[0].length];
		
		
		// sum of all related epochs
		int numberOfEpochs = 0;
		for (EEGDataPackage epoch: epochs) {
			for (EEGMarker marker: this.markers) {
				if (marker.getName().equals(epoch.getMarkers().get(0).getName())) {
					double[][] currData = epoch.getData();
					for (int i = 0; i < average.length; i++) {
						for (int j = 0; j < average[i].length; j++) {
							average[i][j] += currData[i][j];
						}
					}
					numberOfEpochs++;
				}
			}
		}
		
		// average by dividing 
		for (int i = 0; i < average.length; i++) {
			for (int j = 0; j < average[i].length; j++) {
				average[i][j] = average[i][j] / numberOfEpochs;
			}
		}

		EEGDataPackage averagePackage = new EEGDataPackage(average, markers, epochs.get(0).getChannelNames(), epochs.get(0).getMetadata());
		return averagePackage;
	}
}
