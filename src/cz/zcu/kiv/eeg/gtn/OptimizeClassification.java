package cz.zcu.kiv.eeg.gtn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * 
 * This class is used to automatically optimize classifiers. It iterates training and testing phases
 * multiple times with different parameters and reports classification accuracy achieved on testing and
 * validation sets. For each iteration, the results are also compared with human experts estimates.
 * 
 * @author lvareka
 *
 */
public class OptimizeClassification {
	
	
	public static void main(String[] args) throws InterruptedException, IOException {
		double accuracy = 0;
		List<Double> accuracies = new ArrayList<Double>();
		double maxAccuracy = 0;
		
		// do not stop until the accuracy achieved reaches a certain threshold
		while (accuracy < 0.9) {
		}
		
		
	}
	
	

}
