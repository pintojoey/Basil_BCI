package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms;

import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;

/**
 * 
 * Selects only a subset of channels according to their names.
 * Requires that channel names are part of the data packages.
 * 
 * Created by Tomas Prokop on 01.08.2017.
 */
public class ChannelSelectionNames implements IPreprocessing {

	private String[] selectedChannels;
	

    public ChannelSelectionNames(String[] selectedChannels) {
		this.selectedChannels = selectedChannels;
	}
    

	@Override
    public EEGDataPackage preprocess(EEGDataPackage inputPackage) {
		List<Integer> selectedPointers    = new ArrayList<>();
		
		if (inputPackage.getChannelNames() == null || inputPackage.getChannelNames().length == 0) {
			// no channel selection possible - names missing in the data
			return inputPackage; // channel selection not possible
		} 
		
		List<String> currentChannelNames  = new ArrayList<>(Arrays.asList(inputPackage.getChannelNames()));
		List<String> selectedChannelNames = new ArrayList<>(Arrays.asList(selectedChannels));
        
		for (String selectedChannel: selectedChannelNames) {
			int index = currentChannelNames.indexOf(selectedChannel);
			selectedPointers.add(index);
		}
		// set intersection - remove all channel names not contained 
		currentChannelNames.retainAll(selectedChannelNames);
		
        double[][] originalEegData = inputPackage.getData();
        double[][] reducedData = new double[selectedPointers.size()][originalEegData[0].length];
        
        for (int i = 0; i < selectedPointers.size(); i++) {
        	System.arraycopy(originalEegData[selectedPointers.get(i)], 0, reducedData[i], 0, originalEegData[0].length);
        }
        inputPackage.setData(reducedData, this);
        inputPackage.setChannelNames(currentChannelNames.toArray(new String[currentChannelNames.size()]));
        
        return inputPackage;
    }
}
