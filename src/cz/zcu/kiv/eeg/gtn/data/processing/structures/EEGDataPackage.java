package cz.zcu.kiv.eeg.gtn.data.processing.structures;

import java.util.ArrayList;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;

/**
 * Class represents EEG data. It remembers data processing steps performed on the data it carries.
 *
 * Created by Tomas Prokop on 04.07.2017.
 */
public class EEGDataPackage {

    private ArrayList<IPreprocessing> preprocessingSteps;
    private String[] channelNames;

    /**
     * EEG data
     */
    private double[][] data;
    
    private List<EEGMarker> markers;

    public EEGDataPackage(){
        preprocessingSteps = new ArrayList<>();
    }

    public EEGDataPackage(double[][] data, List<EEGMarker> markers) {
        this.data = data;
        this.markers = markers;
        this.channelNames = null;
        this.preprocessingSteps = new ArrayList<>();
    }
    
    

    public EEGDataPackage(double[][] data, List<EEGMarker> markers, String[] channelNames) {
		this(data, markers);
		this.channelNames = channelNames;
		
	}

	/**
     * Returns data
     * @return data
     */
    public double[][] getData() {
        return data;
    }

    /**
     * Set EEG data
     * @param data data
     * @param step Last preprocessing step
     */
    public void setData(double[][] data, IPreprocessing step) {
        preprocessingSteps.add(step);
        this.data = data;
    }

    public List<EEGMarker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<EEGMarker> markers) {
        this.markers = markers;
    }

    public void addMarker(EEGMarker marker){
        this.markers.add(marker);
    }

    public void setData(double[][] data) {
        this.data = data;
    }

    /**
     * Get all preprocessing steps performed on current data
     * @return ordered preprocessing steps
     */
    public List<IPreprocessing> getPreprocessingSteps() {
        return preprocessingSteps;
    }

	public String[] getChannelNames() {
		return channelNames;
	}

	public void setChannelNames(String[] channelNames) {
		this.channelNames = channelNames;
	}
    
    
}
