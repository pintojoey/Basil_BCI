package cz.zcu.kiv.eeg.gtn.data.processing.classification;

import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.FeatureVector;
import libsvm.svm;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

import java.io.*;
import java.util.List;

/**
 * Class using WEKA library for SVM classification.
 * @author mmedek
 */
public class SVMClassifier implements IClassifier {
	
	/**
	 * instance of WEKA LibSVM classifier
	 */
	private Classifier classifier;

	/**
	 * dataset for WEKA instances in format ".arfff"
	 */
	private Instances instances;
	
	/**
	 * path for file with dataset for WEKA classifier
	 */
	private String ARFF_DATASET = "data/dataset.arff"; /* file name for WEKA dataset */

	/**
	 * Constructor for settings of SVM and disable output to console.
	 * Basic attributes are -S = type of SVM classifier (there is C-SVC type in this case), -K = type of kernel (0 - linear, 1 - polynomial,
	 * 2 - radial basis function, 3 - sigmoid), -C = cost (one of the hyper-attribute), -M = cache size, -W = weight (only for turn off console
	 * output).
	 *
	 * @throws IllegalArgumentException - when some of the attribute will be out of range
	 */
	public SVMClassifier(){
		this.classifier = new LibSVM();
		String[] options;
		try {
			options = weka.core.Utils.splitOptions("-S 0 -K 0 -C 425 -M 40.0 -W 1 -seed 1");
			this.classifier.setOptions(options);
		} catch (Exception e) {
			throw new IllegalArgumentException("Wrong input value! Check that input values are correct.");
		}

		svm.svm_set_print_string_function(new libsvm.svm_print_interface(){
		    @Override public void print(String s) {}
		});
	}

	/**
	 * Constructor for settings of SVM and disable output to console.
	 * Basic attributes are -S = type of SVM classifier (there is C-SVC type in this case), -K = type of kernel (0 - linear, 1 - polynomial,
	 * 2 - radial basis function, 3 - sigmoid), -C = cost (one of the hyper-attribute), -M = cache size, -W = weight (only for turn off console
	 * output).
	 *
	 * @throws IllegalArgumentException - when some of the attribute will be out of range
	 */
	public SVMClassifier(String datasetFileName){
		ARFF_DATASET = datasetFileName;
		this.classifier = new LibSVM();
		String[] options;
		try {
			options = weka.core.Utils.splitOptions("-S 0 -K 0 -C 425 -M 40.0 -W 1 -seed 1");
			this.classifier.setOptions(options);
		} catch (Exception e) {
			throw new IllegalArgumentException("Wrong input value! Check that input values are correct.");
		}

		svm.svm_set_print_string_function(new libsvm.svm_print_interface(){
			@Override public void print(String s) {}
		});
	}

	/**
	 * Constructor for settings of SVM and disable output to console.
	 * Basic attributes are -S = type of SVM classifier (there is C-SVC type in this case), -K = type of kernel (0 - linear, 1 - polynomial,
	 * 2 - radial basis function, 3 - sigmoid), -C = cost (one of the hyper-attribute), -M = cache size, -W = weight (only for turn off console
	 * output).
	 * @param cost - parameter for setting miscellaneous values of cost
	 * @throws IllegalArgumentException - when some of the attribute will be out of range
	 */
	public SVMClassifier(double cost) {
		this.classifier = new LibSVM();
		 String[] options = new String[8];
		 options[0] = "-S";
		 options[1] = "0";
		 options[2] = "-K";
		 options[3] = "0";
		 options[4] = "-C";
		 options[5] = Double.toString(cost);
		 options[6] = "-W";
		 options[7] = "1";
		try {
			this.classifier.setOptions(options);
		} catch (Exception e) {
			throw new IllegalArgumentException("Wrong input value! Check that input values are correct.");
		}
		svm.svm_set_print_string_function(new libsvm.svm_print_interface(){
		    @Override public void print(String s) {}
		});
	}

	@Override
	public void train(List<FeatureVector> featureVectors, List<Double> targets, int numberOfiter) {
		/* creating of special dataset for WEKA classificator*/
		createDataset(featureVectors, targets);
		try {
			classifier.buildClassifier(instances);
		} catch (Exception e) {
			throw new IllegalArgumentException("Fault during creating of dataset!");
		}
	}

	/**
	 * Method for creating arff dataset file used for training LibSVM classifier.
	 * ARFF FORMAT
	 * 
	 * @ name_of_attribute_1 type_of_attribute (f.e. numeric)
	 * @ name_of_attribute_2 type_of_attribute (f.e. numeric)
	 * @ name_of_attribute_3 type_of_attribute (f.e. {0,1})
	 * 
	 * @ DATA
	 * 2.1,3.5,1
	 * 
	 * @param featureVectors list of feature vectors
	 * @param targets list of targets
	 */
	private void createDataset(List<FeatureVector> featureVectors, List<Double> targets) {
		FastVector attributes;
		Instances helpDataset;
		double[] values;
		attributes = new FastVector();

		Instance firstInstance = new Instance(targets.get(0), featureVectors.get(0).getFeatureVector());
		int numValues = firstInstance.numValues();
		/* creating fields of attribute of dataset */
		for (int i = 0; i < firstInstance.numValues(); i++) {
			attributes.addElement(new Attribute("att" + (i + 1)));
		}

		attributes.addElement(new Attribute("target"));
		helpDataset = new Instances("ESDN", attributes, 0);

		int j = 0;
		/* creating the rest of dataset */
		for (FeatureVector fv : featureVectors) {
			double[] features = fv.getFeatureVector();
			values = new double[helpDataset.numAttributes()];
			System.arraycopy(features, 0, values, 0, numValues);
			values[numValues] = targets.get(j++);
			helpDataset.add(new Instance(1.0, values));
		}
		instances = helpDataset;
		/* dataset needs class variable in nominal format - last (class variable) is convert from numeric to nominal */
		NumericToNominal convert = new NumericToNominal();
		String[] options = new String[2];
		options[0] = "-R";
		options[1] = "last";
		try {
			convert.setOptions(options);
			convert.setInputFormat(instances);
			instances = Filter.useFilter(instances, convert);
		} catch (Exception e) {
			throw new IllegalArgumentException("Converting of numeric class index failed!");
		}
		instances.setClassIndex(numValues);
		saveDataset();
	}

	@Override
	public ClassificationStatistics test(List<FeatureVector> featureVectors, List<Double> targets) {
		ClassificationStatistics resultsStats = new ClassificationStatistics();
		for (int i = 0; i < instances.numInstances() - 1; i++) {
			Object predictedClassValue;
			try {
				predictedClassValue = classifier.classifyInstance(instances
						.instance(i));
			} catch (Exception e) {
				throw new IllegalArgumentException("Fault during classifying one of the epoch! Try to check dataset.");
			}
			Object realClassValue = instances.instance(i).classValue();
			System.out.println(instances.instance(i).classValue());
			resultsStats.add((Double) realClassValue,
					(Double) predictedClassValue);
		}

		return resultsStats;
	}

	@Override
	public double classify(FeatureVector fv) {
		double[] feature = fv.getFeatureVector();
		Instance instance = new Instance(1, feature);
		instances.add(instance);
		instances.setClassIndex(instances.numAttributes() - 1);
		Object predictedClassValue;
		try {
			predictedClassValue = this.classifier.classifyInstance(instances.lastInstance());
		} catch (Exception e) {
			throw new IllegalArgumentException("Fault during classifying one of the epoch! Try to check dataset.");
		}
		return (Double) predictedClassValue;
	}

	@Override
	public void load(InputStream is) throws IOException, ClassNotFoundException {
		if(is == null)
			throw new NullPointerException("Can't read from null input stream!");

			ObjectInput input = new ObjectInputStream(is);
			this.classifier = (Classifier) input.readObject();
			input.close();
	}


	@Override
	public void load(String file) throws IOException, ClassNotFoundException {
		if(file == null || file.isEmpty())
			throw  new NullPointerException("Given file name is null or empty string!");

			InputStream fileF = new FileInputStream(file);
			InputStream buffer = new BufferedInputStream(fileF);
			ObjectInput input = new ObjectInputStream(buffer);
			this.classifier = (Classifier) input.readObject();
			input.close();
	}

	@Override
	public void save(OutputStream dest) throws IOException {
		if(dest == null)
			throw new NullPointerException("Can't write to null output stream!");

			ObjectOutput output = new ObjectOutputStream(dest);
			output.writeObject(classifier);
			output.close();
	}

	@Override
	public void save(String file) throws IOException{
		if(file == null || file.isEmpty())
			throw  new NullPointerException("Given file name is null or empty string!");
		ObjectOutput output = null;
		try {
			OutputStream fileF;
			fileF = new FileOutputStream(file);
			OutputStream buffer = new BufferedOutputStream(fileF);
			output = new ObjectOutputStream(buffer);
			output.writeObject(classifier);
		} finally {
			if(output != null) {
				output.close();
			}
		}
	}

	/**
	 * Method for reading saved "dataset.arff" and saving data to global attribut called instances
	 * @throws IOException 
	 */
	public void loadDataset(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					ARFF_DATASET));
			ArffReader arff = new ArffReader(reader);
			Instances data = arff.getData();
			data.setClassIndex(data.numAttributes() - 1);
			this.instances = data;
		} catch (Exception e) {
			System.out.println("Something get wrong during loading the file dataset.arff!");
		}
	}

	/**
	 * Method for saving "dataset.arff"
	 * @throws IOException 
	 */
	public void saveDataset(){
		ArffSaver arffSaverInstance = new ArffSaver();
		arffSaverInstance.setInstances(instances);
		File file = new File(ARFF_DATASET);
		file.delete();
		try {
			arffSaverInstance.setFile(new File(ARFF_DATASET));
			arffSaverInstance.writeBatch();
		} catch (IOException e) {
			System.out.println("Something get wrong during saving the file dataset.arff!");
		}
	}
}
