package cz.zcu.kiv.eeg.basil.data.processing.preprocessing;

import java.util.List;

import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGMarker;

/**
 * Average a list of epochs using one stimuli marker
 * @author lvareka
 *
 */
public class Averaging {
	private List<EEGMarker> markers;
	
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
