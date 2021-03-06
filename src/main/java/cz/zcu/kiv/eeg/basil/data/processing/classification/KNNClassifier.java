package cz.zcu.kiv.eeg.basil.data.processing.classification;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;

import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.FeatureVector;
import cz.zcu.kiv.eeg.basil.data.processing.classification.KNN.KNearestNeighborsLocal;

/**
 * K Nearest Neighbors Classifier 
 * (preselected number of nearest neighbors is evaluated from
 * the perspective of their class labels and maximum votes decides the class)
 * 
 * @author lvareka
 *
 */
public class KNNClassifier implements IClassifier {

	/**
	 * Attribute for the instance of KNearestNeighborsLocal classifier.
	 */
	private KNearestNeighborsLocal classifier;
	
	/**
	 * Number of nearest neighbors that will be used for classification.
	 */
	private int k_cnt;
	
	/**
	 * Default number of nearest neighbors.	
	 */
	private static final int K_CNT_DEFAULT = 5;
	
	/**
	 * Constructor for this classifier that uses default number of nearest neighbors (5)
	 * and doesn't use weighted distances.
	 */
	public KNNClassifier() {
		this(K_CNT_DEFAULT);
	}
	
	/**
	 * Constructor for this classifier with variable number of nearest neighbors.
	 * @param k number of nearest neighbors
	 */
	public KNNClassifier(int k) {
		this.k_cnt = k;
		this.classifier = new KNearestNeighborsLocal(k_cnt);
	}

	@Override
	public void train(List<FeatureVector> featureVectors, int numberOfiter) {
		for(int i = 0; i < featureVectors.size(); i++) {
			FeatureVector fv = featureVectors.get(i);
			double[] vector = fv.getFeatureArray();
			classifier.addNeighbor(vector, fv.getExpectedOutput());
		}
	}

	@Override
	public ClassificationStatistics test(List<FeatureVector> featureVectors, List<Double> targets) {
		ClassificationStatistics resultsStats = new ClassificationStatistics();

		for(int i = 0; i < featureVectors.size(); i++) {
			double result = this.classify(featureVectors.get(i));
			resultsStats.add(result, targets.get(i));
		}

		return resultsStats;
	}

	@Override
	public double classify(FeatureVector fv) {
		double[] feature = fv.getFeatureArray();
		double score = classifier.getScore(feature);

		return score;
	}

	@Override
	public void load(InputStream is) {

	}

	@Override
	public void save(OutputStream dest) {

	}

	@Override
	public void load(String file) {
		try   {
			 InputStream fileF = new FileInputStream(file);
			 InputStream buffer = new BufferedInputStream(fileF);
			 ObjectInput input = new ObjectInputStream (buffer);
				 
			 // deserialize the List
			 this.classifier = (KNearestNeighborsLocal)input.readObject();
			 input.close();
		} catch(ClassNotFoundException ex){
			   ex.printStackTrace();
		} catch(IOException ex){
			  ex.printStackTrace();
		}
	}

	@Override
	public void save(String file) {
		OutputStream fileF;
		try {
			fileF = new FileOutputStream(file);
			OutputStream buffer = new BufferedOutputStream(fileF);
		    ObjectOutput output = new ObjectOutputStream(buffer);
		    
		      output.writeObject(classifier);
		      output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getK_cnt() {
		return this.k_cnt;
	}

}
