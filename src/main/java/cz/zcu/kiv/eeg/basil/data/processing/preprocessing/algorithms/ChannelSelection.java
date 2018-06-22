package cz.zcu.kiv.eeg.basil.data.processing.preprocessing.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.zcu.kiv.WorkflowDesigner.Annotations.*;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.IPreprocessing;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;

import static cz.zcu.kiv.WorkflowDesigner.Type.STRING_ARRAY;

/**
 * 
 * Selects only a subset of channels according to their names.
 * Requires that channel names are part of the data packages.
 * 
 * Created by Tomas Prokop on 01.08.2017.
 */
@BlockType(type="ChannelSelection",family = "Preprocessing")
public class ChannelSelection implements IPreprocessing {

    @BlockProperty(name="channels",type = STRING_ARRAY)
	private List<String> selectedChannels;

    @BlockOutput(name="ChannelSelection",type="IPreprocessing")
    private ChannelSelection channelSelection;

    public ChannelSelection(){
        //Required Empty Default Constructor for WorkflowDesigner
    }

    public ChannelSelection(String[] selectedChannels) {
		this.selectedChannels = Arrays.asList(selectedChannels);
	}

    public ChannelSelection(List<String> selectedChannels) {
        this.selectedChannels = selectedChannels;
    }

    @BlockExecute
    private void process(){
        channelSelection=this;
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
