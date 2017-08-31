package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms;

import cz.zcu.kiv.eeg.gtn.data.processing.math.ButterWorthFilter;
import cz.zcu.kiv.eeg.gtn.data.processing.math.IFilter;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.bva.OffLineDataProvider;

public class BandpassFilter implements IPreprocessing {
	private IFilter filter;
	private final int SAMPLING_RATE;

	
	
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
			filter.reset();
		}
		
		
		eegData.setData(data, this);
		return eegData;
	}

}
