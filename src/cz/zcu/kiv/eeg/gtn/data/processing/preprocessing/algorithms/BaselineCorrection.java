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
	private int startIndex;
	private int endIndex;
	
    public BaselineCorrection(int startIndex, int endIndex) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	@Override
    public EEGDataPackage preprocess(EEGDataPackage data) {
        double[][] eegData = data.getData();
        
        // for all channels
        for (int i = 0; i < eegData.length; i++) {
        	// calculate the baseline only in the requested interval
        	double averageBaseline = SignalProcessing.average(eegData[i], startIndex, endIndex);
        	
        	// subtract the baseline from the rest of the signal
        	for (int j = 0; j < eegData[i].length; j++) {
        		eegData[i][j] = eegData[i][j] - averageBaseline; 
        	}
        }
        data.setData(eegData, this);
        
        return data;
    }
}
