package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.processing.math.SignalProcessing;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;

/**
 * 
 * Corrects the baseline by subtracting average voltage values in the baseline
 * correction part from the rest of the signal. Especially useful for ERP epochs.
 * 
 * Created by Tomas Prokop on 01.08.2017.
 *  
 */
public class BaselineCorrection implements IPreprocessing {
	private double startTime; /* in milliseconds */
	private double endTime;   /* in milliseconds */
	
    public BaselineCorrection(double startTime, double endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
    public EEGDataPackage preprocess(EEGDataPackage inputPackage) {
        double[][] eegData = inputPackage.getData();
        double sampling = inputPackage.getMetadata().getSampling();

 /*       for (int i = 0; i < eegData.length; i++){
        	correct(eegData[i], 100);
		}*/

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

	public static void correct(double[] epoch, int prefix) {

		double baseline = 0;

		for (int i = 0; i < prefix; i++) {
			baseline += epoch[i];
		}

		baseline /= prefix;

		for (int i = 0; i < epoch.length; i++) {
			epoch[i] -= baseline;
		}
	}
}
