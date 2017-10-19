package cz.zcu.kiv.eeg.gtn.data.processing.classification;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.FeatureVector;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.random.NguyenWidrowRandomizer;

import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.utils.Const;

/**
 *
 * Classification based on multi-layer perceptron using Neuroph library
 *
 * @author Lukas Vareka
 *
 */
public class MLPClassifier implements LearningEventListener, IClassifier {

    private NeuralNetwork<BackPropagation> neuralNetwork; 		/* neural network implementation */

    private int numberOfIters = 0;
    
    private double[] featureAverage;
    
    private boolean log = true;
    
    private double lastIteration = 0;
    private double maxValidationAccuracy = 0;
    private DataSet[] trainingTesting;
    
    private final static int DEFAULT_OUTPUT_NEURONS = 1;
    private final static double LEARNING_RATE = 0.1;

    public MLPClassifier() {
        neuralNetwork = new MultiLayerPerceptron(DEFAULT_OUTPUT_NEURONS);
    }

    /**
     *
     * @param params contains number of neurons in the layers
     */
    public MLPClassifier(ArrayList<Integer> params) {
        neuralNetwork = new MultiLayerPerceptron(params);
        neuralNetwork.randomizeWeights(new NguyenWidrowRandomizer(0.3, 0.7));

    }

    /**
     * Train with the original dataset
     *
     * @param dataset dataset containing training data (feature vectors and
     * expected classes)
     * @param maxIters maximum allowed number of iterations
     * @param learningRate learning rate
     */
    private void train(DataSet dataset, int maxIters, double learningRate) {
        BackPropagation backP = new BackPropagation();
        backP.setMaxIterations(maxIters);
        backP.setLearningRate(learningRate);
        backP.addListener(this);
        neuralNetwork.learn(dataset, backP);
    }

    @Override
    public double classify(FeatureVector fv) {
    	double[] featureVector = fv.getFeatureVector();

        // feature vector dimension must correspond to the number of input neurons
        if (featureVector.length != neuralNetwork.getInputsCount()) {
            throw new ArrayIndexOutOfBoundsException("Feature vector dimension "
                    + featureVector.length + " must be the same as the number of input neurons: "
                    + neuralNetwork.getInputsCount() + ".");
        }
        
        neuralNetwork.setInput(featureVector);
        neuralNetwork.calculate();
        double[] output = neuralNetwork.getOutput();
        return output[0];
    }

    @Override
    public ClassificationStatistics test(List<FeatureVector> featureVectors, List<Double> targets) {
        ClassificationStatistics resultsStats = new ClassificationStatistics();
        for (int i = 0; i < featureVectors.size(); i++) {
            double output = this.classify(featureVectors.get(i));
            resultsStats.add(output, targets.get(i));
        }
        return resultsStats;
    }

    @Override
    public void train(List<FeatureVector> featureVectors, List<Double> targets, int numberOfIter) {
        int targetsSize = neuralNetwork.getOutputsCount();
        this.numberOfIters = numberOfIter;

        if (featureVectors == null || featureVectors.size() == 0 || featureVectors.get(0).getFeatureVector() == null) {
        	System.out.println("Missing data in feature vectors!");
        	return;
        }
        
        int len = featureVectors.get(0).getFeatureVector().length;
        // fill in the neuroph data structure for holding the training set
        DataSet dataset = new DataSet(len, targetsSize);
       
        for (int i = 0; i < featureVectors.size(); i++) {
            double[] features = featureVectors.get(i).getFeatureVector();
            double[] target = new double[targetsSize];
            target[0] = targets.get(i);
            dataset.addRow(features, target);
        }
      
        dataset.shuffle();
        trainingTesting = dataset.createTrainingAndTestSubsets(80, 20);
     
        // train the NN
        this.maxValidationAccuracy = 0;
        this.train(trainingTesting[0], numberOfIter, LEARNING_RATE);
        System.out.println("-----------------------------\nEnd of training: training data accuracy: " + this.testNeuralNetwork(trainingTesting[0]) +  ", testing data accuracy: " + this.testNeuralNetwork(trainingTesting[1]));
    }

    @Override
    public void load(InputStream is) {
        this.neuralNetwork = NeuralNetwork.load(is);
    }

    @Override
    public void save(OutputStream dest) {

    }

    @Override
    public void save(String file) {
        this.neuralNetwork.save(file);
    }

    @Override
    public void load(String file) {
    	System.out.println("MLP: loading from file: " + file);
        this.neuralNetwork = NeuralNetwork.createFromFile(file);
    }
    
    @Override
    public String toString() {
    	String returnString =  "MLP: ( ";
    	for (Layer layer: this.neuralNetwork.getLayers()) {
    		returnString += layer.getNeuronsCount() + " ";
    	}
    	returnString  += ")";
    	returnString += ": iters: " + this.numberOfIters;
    	return returnString;
    }

	@Override
	public void handleLearningEvent(LearningEvent learningEvent) {
	    BackPropagation bp = (BackPropagation) learningEvent.getSource();
	    lastIteration = bp.getTotalNetworkError();
	    if (log && bp.getCurrentIteration() % 50 == 0) {
	    	//System.out.println("Current iteration: " + bp.getCurrentIteration());
	    	//System.out.println("Error: " + bp.getTotalNetworkError());
	    	double validationAccuracy = testNeuralNetwork(trainingTesting[1]);
	    	//System.out.println("Validation accuracy: " + validationAccuracy);
	    	if (this.maxValidationAccuracy < validationAccuracy) {
	    		this.maxValidationAccuracy = validationAccuracy;
	    		//System.out.println("-----------------------------\nBest validation accuracy: " + this.maxValidationAccuracy);
	    		this.save("best.txt");
	    	}
	    }
       
        lastIteration = bp.getTotalNetworkError();
		
	}
	
	 public double testNeuralNetwork(DataSet testSet) {
		 	int correct = 0, incorrect = 0;
		    for(DataSetRow dataRow : testSet.getRows()) {
		        this.neuralNetwork.setInput(dataRow.getInput());
		        this.neuralNetwork.calculate();
		        double[] networkOutput = this.neuralNetwork.getOutput();
		        if (Math.round(dataRow.getDesiredOutput()[0]) == Math.round(networkOutput[0])) {
		        	correct++;
		        } else {
		        	incorrect++;
		        }
		        
		       // System.out.print("Input: " + Arrays.toString(dataRow.getInput()) );
		       // System.out.println(" Output: " + Arrays.toString(networkOutput) );
		        

		    }
		    return ((double)correct) / (correct + incorrect);

		}
}
