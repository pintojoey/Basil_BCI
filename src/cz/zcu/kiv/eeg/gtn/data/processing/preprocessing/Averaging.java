package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing;

import java.util.Arrays;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;


public class Averaging {

	public EEGDataPackage average(List<EEGDataPackage> epochs, EEGMarker marker) {
		if (epochs == null || epochs.size() == 0 || marker == null)
			return null;
		double[][] firstEpoch = epochs.get(0).getData();
		double[][] average = new double[firstEpoch.length][firstEpoch[0].length];
		
		
		// sum of all related epochs
		int numberOfEpochs = 0;
		for (EEGDataPackage epoch: epochs) {
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
		
		// average by dividing 
		for (int i = 0; i < average.length; i++) {
			for (int j = 0; j < average[i].length; j++) {
				average[i][j] = average[i][j] / numberOfEpochs;
			}
		}
		
		EEGDataPackage averagePackage = new EEGDataPackage(average, Arrays.asList(marker));
		return averagePackage;
	}
}
