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
public class ChannelSelection implements IPreprocessing {

	private List<String> selectedChannels;

    public ChannelSelection(String[] selectedChannels) {
		this.selectedChannels = Arrays.asList(selectedChannels);
	}

    public ChannelSelection(List<String> selectedChannels) {
        this.selectedChannels = selectedChannels;
    }

	@Override
    public EEGDataPackage preprocess(EEGDataPackage inputPackage) {
        String[] channels = inputPackage.getChannelNames();
		if (channels == null || channels.length == 0)
			return inputPackage; // no channel selection possible - names missing in the data

        List<String> currentChannelNames  = new ArrayList<>(Arrays.asList(channels));
        List<String> selectedChannelNames = new ArrayList<>(selectedChannels);
        List<Integer> selectedPointers    = new ArrayList<>();

        for (String selectedChannel : selectedChannels) {
            int index = currentChannelNames.indexOf(selectedChannel);
            if(index > -1)
                selectedPointers.add(index);
            else
                selectedChannelNames.remove(selectedChannel);
        }

        double[][] originalEegData = inputPackage.getData();
        double[][] reducedData = new double[selectedPointers.size()][originalEegData[0].length];

        for (int i = 0; i < selectedPointers.size(); i++) {
        	System.arraycopy(originalEegData[selectedPointers.get(i)], 0, reducedData[i], 0, originalEegData[0].length);
        }

        inputPackage.setData(reducedData, this);
        String[] array = new String[selectedChannelNames.size()];
        inputPackage.setChannelNames(selectedChannelNames.toArray(array));
        
        return inputPackage;
    }
}
