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
import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.basil.data.processing.math.CorrelationAlgorithms;

/**
 * Classifier using correlation.
 * @author Karel Silhavy
 *
 */
public class CorrelationClassifier implements IClassifier {
	
	/**
	 * Feature extractor
	 */
	private IFeatureExtraction fe;
	
	/**
	 * Attribute for the instance of Correlation classifier.
	 */
	private CorrelationAlgorithms classifier;
	
	
	public CorrelationClassifier() {
		this.classifier = new CorrelationAlgorithms();
	}

	/**
	 * Training this classifier is just loading waveform of P3 from the file.
	 */
	public void train() {
		this.classifier.loadP300();
	}

	@Override
	public void train(List<FeatureVector> featureVectors, List<Double> targets, int numberOfiter) {
		train();
	}

	@Override
	public ClassificationStatistics test(List<FeatureVector> featureVectors, List<Double> targets) {
		return null;
	}

	@Override
	public double classify(FeatureVector fv) {
		double[] feature = fv.getFeatureVector();
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
			 
			 this.classifier = (CorrelationAlgorithms)input.readObject();
			 input.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e){
			  e.printStackTrace();
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
	
}
