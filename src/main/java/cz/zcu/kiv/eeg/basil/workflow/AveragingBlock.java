package cz.zcu.kiv.eeg.basil.workflow;

import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockExecute;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockInput;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockOutput;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockType;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker;
import cz.zcu.kiv.eeg.basil.gui.ShowChart;

import java.util.List;

/**
 * Average a list of epochs using one stimuli marker
 * @author lvareka
 *
 */
@BlockType(type="AveragingBlock",family = "Preprocessing")
public class AveragingBlock {

	@BlockInput(name = "Markers",type="EEGMarker[]")
	private List<EEGMarker> markers;

	@BlockInput(name = "EEGData", type = "EEGData[]")
	private List<EEGDataPackage> epochs;

	@BlockOutput(name = "EEGData", type = "EEGData")
	private EEGDataPackage eegData;

	public AveragingBlock(){
		//Required Empty Default Constructor for Workflow Designer
	}

	@BlockExecute
    public void process(){
	    eegData=average(epochs);
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

        ShowChart showChart = new ShowChart("EEG signal visualization");
            showChart.update(average, epochs.get(0).getChannelNames());
            int i=-1;
            while(i<0){
                i++;i--;
            }
        return new EEGDataPackage(average, markers, epochs.get(0).getChannelNames());
	}

    public List<EEGMarker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<EEGMarker> markers) {
        this.markers = markers;
    }

    public List<EEGDataPackage> getEpochs() {
        return epochs;
    }

    public void setEpochs(List<EEGDataPackage> epochs) {
        this.epochs = epochs;
    }

    public EEGDataPackage getEegData() {
        return eegData;
    }

    public void setEegData(EEGDataPackage eegData) {
        this.eegData = eegData;
    }
}
