package cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.algorithms;

import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;

/**
 * 
 * This method uses only channel indices
 * to select a subset of channels.
 * Use if channel names may not be provided.
 * 
 * @author lvareka
 *
 */
public class ChannelSelectionPointers implements IPreprocessing {
	
	private List<Integer> channelPointers;
    
    public ChannelSelectionPointers(List<Integer> selectedPointers) {
    	this.channelPointers  = selectedPointers;
    }

	@Override
    public EEGDataPackage preprocess(EEGDataPackage inputPackage) {
		
		
		if (channelPointers == null || channelPointers.size() == 0) { // input not filled out
			return inputPackage; // no change
		} 
		
        double[][] originalEegData = inputPackage.getData();
        double[][] reducedData = new double[channelPointers.size()][originalEegData[0].length];
        
        for (int i = 0; i < channelPointers.size(); i++) {
        	if (channelPointers.get(i) < 0 || channelPointers.get(i) >= originalEegData.length) {
        		System.err.println("Channel index " + channelPointers.get(i) + " out of the original data range!");
        	}
        	else {
        		System.arraycopy(originalEegData[channelPointers.get(i)], 0, reducedData[i], 0, originalEegData[0].length);
        	}
        }
        
        inputPackage.setData(reducedData, this);
        
        return inputPackage;
    }
}
