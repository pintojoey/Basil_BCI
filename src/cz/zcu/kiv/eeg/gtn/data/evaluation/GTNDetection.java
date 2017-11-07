package cz.zcu.kiv.eeg.gtn.data.evaluation;

import java.util.Arrays;
import java.util.List;
import cz.zcu.kiv.eeg.gtn.data.listeners.EEGDataProcessingListener;
import cz.zcu.kiv.eeg.gtn.data.listeners.EEGMessageListener;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGMarker;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;



/**
 * 
 * Class used to predict the number though (1 - 9) in
 * the Guess the Number experiment
 * 
 * @author lvareka
 *
 */
public class GTNDetection implements EEGDataProcessingListener, EEGMessageListener {
	private final double[] classificationResults;
	private final int[] classificationCounters;
	private String sourceFileName;
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
	public void dataPreprocessed(List<EEGDataPackage> dataPackages) {
	// TODO Auto-generated method stub
			
	}

	@Override
	public void featuresExtracted(EEGDataPackage dataPackage) {
	// TODO Auto-generated method stub
			
	}

	@Override
	public void dataClassified(EEGDataPackage dataPackage) {
		
		double classificationResult = dataPackage.getClassificationResult();
		List<EEGMarker> markers = dataPackage.getMarkers();
		if (markers == null || markers.size() != 1 || markers.get(0).getName() == null)
			return;
		
	    int stimulusID = Integer.parseInt(markers.get(0).getName().replaceAll("[\\D]", "")); 

	    if (stimulusID < NUMBER_OF_STIMULI) {
	    	classificationCounters[stimulusID]++;
	        classificationResults[stimulusID] += classificationResult;
	        this.weightedResults = this.calcClassificationResults();
	        
	    }
	}

	@Override
	public void startMessageSent(EEGStartMessage msg) {
		 Arrays.fill(classificationCounters, 0);
		 Arrays.fill(classificationResults, 0);
		 this.sourceFileName = msg.getDataFileName();
		 
		 System.out.println("Start message sent");
		
	}

	@Override
	public void dataMessageSent(EEGDataMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopMessageSent(EEGStopMessage msg) {
		System.out.println("Stop message sent: classification result: " + Arrays.toString(this.weightedResults));
		
	}


}
