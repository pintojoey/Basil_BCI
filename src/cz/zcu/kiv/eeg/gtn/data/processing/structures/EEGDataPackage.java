package cz.zcu.kiv.eeg.gtn.data.processing.structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.FeatureVector;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.IPreprocessing;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;

/**
 * Class represents EEG data. It remembers data processing steps performed on the data it carries.
 *
 * Created by Tomas Prokop on 04.07.2017.
 */
public class EEGDataPackage {

    private ArrayList<IPreprocessing> preprocessingSteps;

    private List<IFeatureExtraction> featureExtractions;

    private IClassifier classifier;

    private String[] channelNames;

    /**
     * EEG data
     */
    private double[][] data;
    
    private List<EEGMarker> markers;

    private List<FeatureVector> featureVectors;

    private int expectedClass;

    private int outputClass;

    private double classificationResult;

    public EEGDataPackage(){
        preprocessingSteps = new ArrayList<>();
    }

    public EEGDataPackage(double[][] data, List<EEGMarker> markers) {
        this.data = data;
        this.markers = markers;
        this.channelNames = null;
        this.preprocessingSteps = new ArrayList<>();
        this.featureExtractions = new ArrayList<>();
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
     * @param step Last preprocessings step
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
     * Get all preprocessings steps performed on current data
     * @return ordered preprocessings steps
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

    public IClassifier getClassifier() {
        return classifier;
    }

    public void setClassifier(IClassifier classifier) {
        this.classifier = classifier;
    }

    public int getExpectedClass() {
        return expectedClass;
    }

    public void setExpectedClass(int expectedClass) {
        this.expectedClass = expectedClass;
    }

    public int getOutputClass() {
        return outputClass;
    }

    public void setOutputClass(int outputClass) {
        this.outputClass = outputClass;
    }

    public double getClassificationResult() {
        return classificationResult;
    }

    public void setClassificationResult(double classificationResult) {
        this.classificationResult = classificationResult;
    }

    public void setPreprocessingSteps(ArrayList<IPreprocessing> preprocessingSteps) {
        this.preprocessingSteps = preprocessingSteps;
    }

    public List<IFeatureExtraction> getFeatureExtractions() {
        return featureExtractions;
    }

    public void setFeatureExtractions(List<IFeatureExtraction> featureExtractions) {
        this.featureExtractions = featureExtractions;
    }

    public List<FeatureVector> getFeatureVectors() {
        return featureVectors;
    }

    public void setFeatureVectors(List<FeatureVector> featureVectors) {
        this.featureVectors = featureVectors;
    }

    public void addFeatureVector(FeatureVector fv){
        if(featureVectors == null)
            featureVectors = new ArrayList<>();

        featureVectors.add(fv);
    }
    
    @Override
    public String toString() {
    	String returnString = "";
    	boolean channelsOK = true;
    	if (channelNames == null || channelNames.length != data.length) {
    		returnString = "ChannelNames missing or its size and data size are different!\n";
    		channelsOK = false;
       	}
    	
    	for (int i = 0; i < data.length; i++) {
    		returnString += channelsOK? channelNames[i] : "?" + ": " + Arrays.toString(data[i]) + "\n";
    	}
    	return returnString;
    }
}
