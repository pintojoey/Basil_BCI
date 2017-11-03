package cz.zcu.kiv.eeg.gtn.data.evaluation;

import java.util.Arrays;
import java.util.List;


import cz.zcu.kiv.eeg.gtn.data.listeners.EEGDataProcessingListener;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;

/**
 * 
 * Class used to predict the number though (1 - 9) in
 * the Guess the Number experiment
 * 
 * @author lvareka
 *
 */
public class GTNDetection implements EEGDataProcessingListener {
	private final double[] classificationResults;
	private final int[] classificationCounters;
	public int NUMBER_OF_STIMULI = 9;

	private double[] weightedResults;

	public GTNDetection() {
		this.classificationCounters = new int[NUMBER_OF_STIMULI];
	    this.classificationResults  = new double[NUMBER_OF_STIMULI];
	        
	    Arrays.fill(classificationCounters, 0);
	    Arrays.fill(classificationResults, 0);
	}

	private double[] calcClassificationResults() {
		//  double[] wResults = new double[Const.GUESSED_NUMBERS];
	   double[] wResults = new double[NUMBER_OF_STIMULI];
	   for (int i = 0; i < wResults.length; i++) {
		   if (classificationCounters[i] == 0) {
			   wResults[i] = 0;
	       } else {
	    	   wResults[i] = classificationResults[i] / classificationCounters[i];
	       }
	   }

	   return wResults;
	}

	   

	public double[] getWeightedResults() {
		return weightedResults;
	}

	public int[] getClassificationCounters() {
		return classificationCounters;
	}
	
	@Override
	public void dataPreprocessed(List<EEGDataPackage> packs) {
	// TODO Auto-generated method stub
			
	}

	@Override
	public void featuresExtracted(EEGDataPackage pack) {
	// TODO Auto-generated method stub
			
	}

	@Override
	public void dataClassified(EEGDataPackage pack) {
		double classificationResult = pack.getClassificationResult();
		List<EEGMarker> markers = pack.getMarkers();
		if (markers == null || markers.size() != 1 || markers.get(0).getName() == null)
			return;
		
	    int stimulusID = Integer.parseInt(markers.get(0).getName().replaceAll("[\\D]", "")); 

	    if (stimulusID < NUMBER_OF_STIMULI) {
	    	classificationCounters[stimulusID]++;
	        classificationResults[stimulusID] += classificationResult;
	        this.weightedResults = this.calcClassificationResults();
	        
	    }
	}
	
}
