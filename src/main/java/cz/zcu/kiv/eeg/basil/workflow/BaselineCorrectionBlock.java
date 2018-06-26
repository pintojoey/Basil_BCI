package cz.zcu.kiv.eeg.basil.workflow;

import cz.zcu.kiv.WorkflowDesigner.Annotations.*;
import cz.zcu.kiv.eeg.basil.data.processing.math.SignalProcessing;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.IPreprocessing;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;

import java.util.List;

import static cz.zcu.kiv.WorkflowDesigner.Type.NUMBER;

/**
 * 
 * Corrects the baseline by subtracting average voltage values in the baseline
 * correction part from the rest of the signal. Especially useful for ERP epochs.
 * 
 * Created by Tomas Prokop on 01.08.2017.
 *  
 */
@BlockType(type="BaselineCorrection",family="Preprocessing")
public class BaselineCorrectionBlock {

    @BlockProperty(name="StartTime",type = NUMBER, defaultValue = "")
	private double startTime; /* in milliseconds */

    @BlockProperty(name="EndTime",type = NUMBER, defaultValue = "")
	private double endTime;   /* in milliseconds */

	@BlockInput(name = "EEGData", type = "EEGData[]")
	@BlockOutput(name = "EEGData", type = "EEGData[]")
	private List<EEGDataPackage> eegDataList=null;

	public BaselineCorrectionBlock(){
		//Required Empty Default Constructor for Workflow Designer
	}

	@BlockExecute
	public void process(){
	    for(EEGDataPackage eegData:eegDataList){
            double[][] data = eegData.getData();

            // for all channels
            for (int i = 0; i < data.length; i++) {
                double sampling = eegData.getConfiguration().getSamplingInterval();
                // calculate the baseline only in the requested interval
                int start = (int) (0.001 * startTime * sampling);
                int end = (int) (0.001 * endTime * sampling);
                double averageBaseline = SignalProcessing.average(data[i], start, end);

                // subtract the baseline from the rest of the signal
                for (int j = 0; j < data[i].length; j++) {
                    data[i][j] = data[i][j] - averageBaseline;
                }
            }
        }

    }

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}

    public List<EEGDataPackage> getEegDataList() {
        return eegDataList;
    }

    public void setEegDataList(List<EEGDataPackage> eegDataList) {
        this.eegDataList = eegDataList;
    }
}
