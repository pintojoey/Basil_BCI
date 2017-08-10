package cz.zcu.kiv.eeg.gtn.data.processing.classification;

import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.FeatureVector;
import org.apache.commons.io.FileUtils;
//import org.canova.api.records.reader.RecordReader;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Creates an instance of Deep Belief Network 
 * @author Pumprdlici group
 * 
 *
 */
public class DBNDeepLearning4jClassifier implements IClassifier {

    
    private MultiLayerNetwork model;    //multi layer neural network with a logistic output layer and multiple hidden neuralNets
    private int iterations;             //Iterations used to classify
    
    /* Default constructor*/
    public DBNDeepLearning4jClassifier() {
    }


    @Override
    public double classify(FeatureVector fv){
        double[] featureVector = fv.getFeatureVector(); // Extracting features to vector
        INDArray features = Nd4j.create(featureVector); // Creating INDArray with extracted features
        return model.output(features, Layer.TrainingMode.TEST).getDouble(0); // Result of classifying
    }

    @Override
    public void train(List<FeatureVector> featureVectors, List<Double> targets, int numberOfiter){
        this.iterations = numberOfiter;
        
        // Customizing params of classifier
        final int numRows = featureVectors.get(0).getFeatureVector().length;   // number of targets on a line
        final int numColumns = 2;   // number of labels needed for classifying
        int seed = 123; //  seed - one of parameters. For more info check http://deeplearning4j.org/iris-flower-dataset-tutorial
        int listenerFreq = this.iterations;  // frequency of output strings

        //Load Data - when target is 0, label[0] is 0 and label[1] is 1.
        double[][] labels = new double[targets.size()][numColumns]; // Matrix of labels for classifier
        double[][] features_matrix = new double[targets.size()][numRows]; // Matrix of features
        for (int i = 0; i < featureVectors.size(); i++) { // Iterating through epochs
            double[] features = featureVectors.get(i).getFeatureVector(); // Feature of each epoch
            for (int j = 0; j < numColumns; j++) {   //setting labels for each column
                labels[i][0] = targets.get(i); // Setting label on position 0 as target
                labels[i][1] = Math.abs(1 - targets.get(i));  // Setting label on position 1 to be different from label[0]
            }
            features_matrix[i] = features; // Saving features to features matrix
        }


        INDArray output_data = Nd4j.create(labels); // Create INDArray with labels(targets)
        INDArray input_data = Nd4j.create(features_matrix); // Create INDArray with features(data)
        DataSet dataSet = new DataSet(input_data, output_data); // Create dataSet with features and labels
        SplitTestAndTrain tat = dataSet.splitTestAndTrain(0.8);
       

        // Building a neural net
        build(numRows, numColumns, seed, listenerFreq);

        System.out.println("Train model....");

        model.fit(tat.getTrain());
        model.finetune();
        System.out.println("Evaluation");
        Evaluation eval = new Evaluation(numColumns);
        eval.eval(dataSet.getLabels(), model.output(dataSet.getFeatureMatrix(), Layer.TrainingMode.TEST));
        System.out.println(eval.stats());
    }

    public void train(List<double[][]> epochs, List<Double> targets, int numberOfIter, IFeatureExtraction fe) {


    }

    //  initialization of neural net with params. For more info check http://deeplearning4j.org/iris-flower-dataset-tutorial where is more about params
    private void build(int numRows, int outputNum, int seed, int listenerFreq) {
        System.out.print("Build model....DBN");
        Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder() // Starting builder pattern
                //.seed(seed) // Locks in weight initialization for tuning
                .iterations(20) // # training iterations predict/classify & backprop
//                .miniBatch(true)
                .learningRate(0.01) // Optimization step size
                .optimizationAlgo(OptimizationAlgorithm.LINE_GRADIENT_DESCENT) // Backprop to calculate gradients
                //.updater(Updater.NESTEROVS).momentum(0.9)
                //.l2(0.01).regularization(true).l2(0.001) // Setting regularization, decreasing model size and speed of learning
                //.useDropConnect(true) // Generalizing neural net, dropping part of connections
                .list() // # NN layers (doesn't count input layer)
                .layer(0, new RBM.Builder(RBM.HiddenUnit.GAUSSIAN, RBM.VisibleUnit.GAUSSIAN) // Setting layer to Restricted Boltzmann machine
                        .nIn(numRows) // # input nodes
                        .nOut(250) // # fully connected hidden layer nodes. Add list if multiple layers.
                        //.k(3) // # contrastive divergence iterations
                        .lossFunction(LossFunction.KL_DIVERGENCE)
                        //.dropOut(0.5) // Dropping part of connections
                        .build() // Build on set configuration
                ).layer(1, new RBM.Builder().nIn(250).nOut(125)
                        .lossFunction(LossFunction.MCXENT)
                        .activation(Activation.RELU)
                        .lossFunction(LossFunction.KL_DIVERGENCE)
                        .build()//
                ) // NN layer type
                .layer(2, new OutputLayer.Builder(LossFunction.MSE) //Override default output layer that classifies input by Iris label using softmax
                        //.weightInit(WeightInit.XAVIER) // Weight initialization
                        .nIn(125) // # input nodes
                        .nOut(outputNum) // # output nodes
                        .activation(Activation.SIGMOID) // Activation function type
                        .build() // Build on set configuration
                ) // NN layer type
                .pretrain(true).backprop(true).build(); // Build on set configuration
        model = new MultiLayerNetwork(conf); // Passing built configuration to instance of multilayer network
        model.init(); // Initialize model
        model.setListeners(new ScoreIterationListener(10));// Setting listeners
        //model.setListeners(new ScoreIterationListener(listenerFreq)); // Setting listeners
    }

    // method for testing the classifier.
    @Override
    public ClassificationStatistics test(List<FeatureVector> featureVectors, List<Double> targets) {
        ClassificationStatistics resultsStats = new ClassificationStatistics(); // initialization of classifier statistics
        for (int i = 0; i < featureVectors.size(); i++) {   //iterating epochs
            double output = this.classify(featureVectors.get(i));  //   output means score of a classifier from method classify
            resultsStats.add(output, targets.get(i));   // calculating statistics
        }
        return resultsStats;    //  returns classifier statistics
    }


    // method not implemented. For saving use method save(String file)
    @Override
    public void load(InputStream is) {

    }

    // method not implemented. For loading use load(String file)
    @Override
    public void save(OutputStream dest) {

    }

    /**
     * saves network to zip file
     * @param file path and file name without .zip
     */
    public void save(String file){
        File locationToSave = new File(file + ".zip");      //Where to save the network. Note: the file is in .zip format - can be opened externally
        boolean saveUpdater = true;   //Updater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this if you want to train your network more in the future
        try {
            ModelSerializer.writeModel(model, locationToSave, saveUpdater);
            System.out.println("Saved network params " + model.params());
            System.out.println("Saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * loads network from zip file
     * @param file path and file name without .zip
     */
    public void load(String file){
        File locationToLoad = new File(file + ".zip");
        try {
            model = ModelSerializer.restoreMultiLayerNetwork(locationToLoad);
            System.out.println("Loaded");
            System.out.println("Loaded network params " + model.params());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveOld(String file) {
        OutputStream fos;
        // Choose the name of classifier and coefficient file to save based on the feature extraction, which is used
        String coefficientsName = this.getClass().getName() +  ".bin";

        try {
            // Save classifier and coefficients, used methods come from Nd4j library
            fos = Files.newOutputStream(Paths.get("data/test_classifiers_and_settings/" + coefficientsName));
            DataOutputStream dos = new DataOutputStream(fos);
            Nd4j.write(model.params(), dos);
            dos.flush();
            dos.close();
            FileUtils.writeStringToFile(new File(file), model.getLayerWiseConfigurations().toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadOld(String file) {
        MultiLayerConfiguration confFromJson = null;
        INDArray newParams = null;
        // Choose the name of coefficient file to load based on the feature extraction, which is used
        String coefficientsName = this.getClass().getName() +  ".bin";

        try {
            // Load classifier and coefficients, used methods come from Nd4j library
            confFromJson = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(file)));
            DataInputStream dis = new DataInputStream(new FileInputStream("data/test_classifiers_and_settings/" + coefficientsName));
            newParams = Nd4j.read(dis);
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Initialize network with loaded params
        model = new MultiLayerNetwork(confFromJson);
        model.init();
        model.setParams(newParams);
        System.out.println("Original network params " + model.params());
        System.out.println("Loaded");
    }
}
