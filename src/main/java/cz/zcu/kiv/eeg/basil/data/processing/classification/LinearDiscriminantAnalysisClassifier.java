package cz.zcu.kiv.eeg.basil.data.processing.classification;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.FeatureVector;
import cz.zcu.kiv.eeg.basil.data.processing.classification.LDA.LinearDiscriminantAnalysisAlgorithms;

public class LinearDiscriminantAnalysisClassifier implements IClassifier {

	private LinearDiscriminantAnalysisAlgorithms lda;

	public LinearDiscriminantAnalysisClassifier() {
		this.lda = new LinearDiscriminantAnalysisAlgorithms();
	}

	/**
	 * Train the classifier using information from the supervisor
	 * 
	 * @param featureVectors
	 *            - feature vectors
	 * @param numberOfiter
	 *            - number of training iterations
	 */
	public void train(List<FeatureVector> featureVectors,
					  int numberOfiter) {
		lda.train(featureVectors);
	}

	/**
	 * Test the classifier using the data with known resulting classes
	 * 
	 * @param featureVectors
	 *            - feature vectors
	 * @param targets
	 *            - target classes - list of expected classes (0 or 1)
	 * @return
	 */
	public ClassificationStatistics test(List<FeatureVector> featureVectors,
			List<Double> targets) {
		ClassificationStatistics resultsStats = new ClassificationStatistics();

		for (int i = 0; i < featureVectors.size(); i++) {
			double result = this.classify(featureVectors.get(i));
			resultsStats.add(result, targets.get(i));
		}

		return resultsStats;
	}

	@Override
	public double classify(FeatureVector fv) {
		double[] featureVector = fv.getFeatureArray();
		return lda.classify(featureVector);
	}

	@Override
	public void load(InputStream is) {

	}

	@Override
	public void save(OutputStream dest) {

	}

	public void save(String file) {
		lda.save(file);
	}

	public void load(String file) {
		lda.load(file);
	}
}
