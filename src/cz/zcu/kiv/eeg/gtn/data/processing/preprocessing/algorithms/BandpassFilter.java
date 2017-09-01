package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms;

import cz.zcu.kiv.eeg.gtn.data.processing.math.ButterWorthFilter;
import cz.zcu.kiv.eeg.gtn.data.processing.math.IFilter;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;

/**
 * Band-pass filtering of the signal. It is based on Butterworth filtering.
 * Only approximately frequencies between lowFreq and highFreq are
 * preserved in the signal.
 * 
 * 
 * @author lvareka
 *
 */
public class BandpassFilter implements IPreprocessing {
	private IFilter filter;
	private final int SAMPLING_RATE;
	
	/**
	 * 
	 * @param lowFreq low frequency edge of the band-pass in Hz
	 * @param highFreq high frequency edge of the band-pass in Hz
	 * @param samplingRate sampling rate in Hz
	 */
	public BandpassFilter(double lowFreq, double highFreq, int samplingRate) {
		this.SAMPLING_RATE = samplingRate;
		this.filter = new ButterWorthFilter(lowFreq, highFreq, SAMPLING_RATE);
	}



	@Override
	public EEGDataPackage preprocess(EEGDataPackage eegData) {
		double[][] data = eegData.getData();
		
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				data[i][j] = filter.getOutputSample(data[i][j]);
			}
			// reset the memory of the filter
			filter.reset();
		}
		
		// refresh the data and reference this method
		eegData.setData(data, this);
		return eegData;
	}

}
