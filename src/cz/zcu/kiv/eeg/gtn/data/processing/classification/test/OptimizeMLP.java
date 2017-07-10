package cz.zcu.kiv.eeg.gtn.data.processing.classification.test;

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
public class OptimizeMLP {
	
	
	public static void main(String[] args) throws InterruptedException, IOException {
		double accuracy = 0;
		List<Double> accuracies = new ArrayList<Double>();
		double maxAccuracy = 0;
		
		// do not stop until the accuracy achieved reaches a certain threshold
		while (accuracy < 0.9) {
			int numberOfIters = 10; // +random.nextInt(2000)
			int middleNeurons = 8; // +random.nextInt(15);
			TrainUsingOfflineProvider trainOfflineProvider = new TrainUsingOfflineProvider(numberOfIters, middleNeurons);
			System.out.println("New classifier: " + trainOfflineProvider.getClassifier());
			System.out.println("New feature extraction: " + trainOfflineProvider.getClassifier().getFeatureExtraction());
			TestClassificationAccuracy testAccuracy = null;
			try {
				testAccuracy = new TestClassificationAccuracy( trainOfflineProvider.getClassifier());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			} catch (ExecutionException ex) {
                        Logger.getLogger(OptimizeMLP.class.getName()).log(Level.SEVERE, null, ex);
                    }
			Map<String, Statistics> stats = testAccuracy.getStats();
			int okNumber = 0;
			for (Map.Entry<String, Statistics> entry : stats.entrySet()) {
				if (entry.getValue().getRank() == 1) {
					okNumber++;
			}
			
			}
			accuracy = (double) okNumber / stats.size();
			accuracies.add(accuracy);
			if (accuracy > maxAccuracy) {
				maxAccuracy = accuracy;
				System.out.println("New accuracy record: " + accuracy);
				trainOfflineProvider.getClassifier().save("data/bestOfAllTimes.txt");
				
			}
			else {
				System.out.println("No record: current accuracy = " + accuracy * 100 + ", max_accuracy = " + maxAccuracy * 100);
			}
			
			System.out.println ("Average value = " + calculateAverage (accuracies) * 100 + " %, calculated from " + accuracies.size() + " samples.");


		}
		
		
	}
	
	private static double calculateAverage(List <Double> marks) {
		  double sum = 0;
		  if(!marks.isEmpty()) {
		    for (double mark : marks) {
		        sum += mark;
		    }
		    return sum / marks.size();
		  }
		  return sum;
	}

}
