package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.processing.math.SignalProcessing;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;

/**
 * Created by Tomas Prokop on 01.08.2017.
 * 
 * Corrects the baseline by subtracting average voltage values in the baseline
 * correction part from the rest of the signal. Especially useful for ERP epochs.
 * 
 * 
 */
public class BaselineCorrection implements IPreprocessing {
	private double startTime; /* in milliseconds */
	private double endTime;   /* in milliseconds */
	private final int SAMPLING_RATE;
	
    public BaselineCorrection(double startTime, double endTime, int SAMPLING_RATE) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.SAMPLING_RATE = SAMPLING_RATE;
	}

	@Override
    public EEGDataPackage preprocess(EEGDataPackage inputPackage) {
        double[][] eegData = inputPackage.getData();
        
        // for all channels
        for (int i = 0; i < eegData.length; i++) {
        	// calculate the baseline only in the requested interval
        	double averageBaseline = SignalProcessing.average(eegData[i], (int) (0.001 * startTime * SAMPLING_RATE), (int) (0.001 * endTime * SAMPLING_RATE));
        	
        	// subtract the baseline from the rest of the signal
        	for (int j = 0; j < eegData[i].length; j++) {
        		eegData[i][j] = eegData[i][j] - averageBaseline; 
        	}
        }
        inputPackage.setData(eegData, this);
        
        return inputPackage;
    }
}
