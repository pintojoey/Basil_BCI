package cz.zcu.kiv.eeg.basil.data.processing.preprocessing.algorithms;

import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockExecute;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockOutput;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockProperty;
import cz.zcu.kiv.WorkflowDesigner.Annotations.BlockType;
import cz.zcu.kiv.eeg.basil.data.processing.math.SignalProcessing;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.IPreprocessing;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;

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
public class BaselineCorrection implements IPreprocessing {

    @BlockProperty(name="StartTime",type = NUMBER, defaultValue = "")
	private double startTime; /* in milliseconds */

    @BlockProperty(name="EndTime",type = NUMBER, defaultValue = "")
	private double endTime;   /* in milliseconds */

    @BlockOutput(name="BaselineCoorection",type="IPreprocessing")
    private BaselineCorrection baselineCorrection;

	public BaselineCorrection(){
		//Required Empty Default Constructor for Workflow Designer
	}

	@BlockExecute
	private void process(){
	    baselineCorrection=this;
    }
    public BaselineCorrection(double startTime, double endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
    public EEGDataPackage preprocess(EEGDataPackage inputPackage) {
        double[][] eegData = inputPackage.getData();
        double sampling = inputPackage.getMetadata().getSampling();

        // for all channels
        for (int i = 0; i < eegData.length; i++) {
        	// calculate the baseline only in the requested interval
			int start = (int) (0.001 * startTime * sampling);
			int end = (int) (0.001 * endTime * sampling);
        	double averageBaseline = SignalProcessing.average(eegData[i], start, end);
        	
        	// subtract the baseline from the rest of the signal
        	for (int j = 0; j < eegData[i].length; j++) {
        		eegData[i][j] = eegData[i][j] - averageBaseline; 
        	}
        }
        inputPackage.setData(eegData, this);
        
        return inputPackage;
    }
}
