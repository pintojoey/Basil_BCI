package icp.application.classification;

import org.apache.commons.io.FileUtils;
import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by lukasvareka on 4. 7. 2016.
 */
public class MLPDeepLearning4jEarlyStop implements IERPClassifier {
    private final int NEURON_COUNT_DEFAULT = 30;    //default number of neurons
    private IFeatureExtraction fe;                //type of feature extraction (MatchingPursuit, FilterAndSubampling or WaveletTransform)
    private MultiLayerNetwork model;            //multi layer neural network with a logistic output layer and multiple hidden neuralNets
    private int neuronCount;                    // Number of neurons
    private int iterations;                    //Iterations used to classify

    /*Default constructor*/
    public MLPDeepLearning4jEarlyStop() {
        this.neuronCount = NEURON_COUNT_DEFAULT; // sets count of neurons in layer(0) to default number
    }

    /*Parametric constructor */
    public MLPDeepLearning4jEarlyStop(int neuronCount) {
        this.neuronCount = neuronCount; // sets count of neurons in layer(0) to param
    }

    /*Classifying features*/
    @Override
    public double classify(double[][] epoch) {
        double[] featureVector = this.fe.extractFeatures(epoch); // Extracting features to vector
        INDArray features = Nd4j.create(featureVector); // Creating INDArray with extracted features
        return model.output(features, Layer.TrainingMode.TEST).getDouble(0); // Result of classifying
    }

    @Override
    public void train(List<double[][]> epochs, List<Double> targets, int numberOfiter, IFeatureExtraction fe) {

        // Customizing params of classifier
        final int numRows = fe.getFeatureDimension();   // number of targets on a line
        final int numColumns = 2;   // number of labels needed for classifying
        this.iterations = numberOfiter; // number of iteration in the learning phase
        int listenerFreq = numberOfiter / 10; // frequency of output strings
        int seed = 123; //  seed - one of parameters. For more info check http://deeplearning4j.org/iris-flower-dataset-tutorial

        //Load Data - when target is 0, label[0] is 0 and label[1] is 1.
        double[][] labels = new double[targets.size()][numColumns]; // Matrix of labels for classifier
        double[][] features_matrix = new double[targets.size()][numRows]; // Matrix of features
        for (int i = 0; i < epochs.size(); i++) { // Iterating through epochs
            double[][] epoch = epochs.get(i); // Each epoch
            double[] features = fe.extractFeatures(epoch); // Feature of each epoch
            for (int j = 0; j < numColumns; j++) {   //setting labels for each column
                labels[i][0] = targets.get(i); // Setting label on position 0 as target
                labels[i][1] = Math.abs(1 - targets.get(i));  // Setting label on position 1 to be different from label[0]
            }
            features_matrix[i] = features; // Saving features to features matrix
        }

        // Creating INDArrays and DataSet
        INDArray output_data = Nd4j.create(labels); // Create INDArray with labels(targets)
        INDArray input_data = Nd4j.create(features_matrix); // Create INDArray with features(data)
        DataSet dataSet = new DataSet(input_data, output_data); // Create dataSet with features and labels
        SplitTestAndTrain tat = dataSet.splitTestAndTrain(0.8);
        DataSetIterator dataSetTrainIterator = new ListDataSetIterator(tat.getTrain().batchBy(32));
        DataSetIterator dataSetTestIterator = new ListDataSetIterator(tat.getTest().batchBy(32));

        Nd4j.ENFORCE_NUMERICAL_STABILITY = true; // Setting to enforce numerical stability

        // Building a neural net
        MultiLayerConfiguration conf = build(numRows, numColumns, seed, listenerFreq);

        //System.out.println("Train model....");
        // model.fit(tat.getTrain()); // Learning of neural net with training data


        EarlyStoppingConfiguration esConf = new EarlyStoppingConfiguration.Builder()
                //.epochTerminationConditions(new MaxEpochsTerminationCondition(50))
                .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(2, TimeUnit.MINUTES))
                .scoreCalculator(new DataSetLossCalculator(dataSetTestIterator, true))
                .evaluateEveryNEpochs(1)
                //.modelSaver(new LocalFileModelSaver(directory))
                .build();

        EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConf,conf,dataSetTrainIterator);

//Conduct early stopping training:
        EarlyStoppingResult result = trainer.fit();

//Print out the results:
        System.out.println("Termination reason: " + result.getTerminationReason());
        System.out.println("Termination details: " + result.getTerminationDetails());
        System.out.println("Total epochs: " + result.getTotalEpochs());
        System.out.println("Best epoch number: " + result.getBestModelEpoch());
        System.out.println("Score at best epoch: " + result.getBestModelScore());

//Get the best model:
        model = result.getBestModel();

        Evaluation eval = new Evaluation(numColumns);
        eval.eval(tat.getTest().getLabels(), model.output(tat.getTest().getFeatureMatrix(), Layer.TrainingMode.TEST));
        System.out.println(eval.stats());
    }

    //  initialization of neural net with params. For more info check http://deeplearning4j.org/iris-flower-dataset-tutorial where is more about params
    private MultiLayerConfiguration build(int numRows, int outputNum, int seed, int listenerFreq) {
        System.out.print("Build model....");

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(10000)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(0.001)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .list(3)
                .layer(0, new DenseLayer.Builder().nIn(numRows).nOut(20)
                        .weightInit(WeightInit.XAVIER)
                        .activation("relu")
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(20).nOut(10)
                        .weightInit(WeightInit.XAVIER)
                        .activation("relu")
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.XAVIER)
                        .activation("softmax").weightInit(WeightInit.XAVIER)
                        .nIn(10).nOut(outputNum).build())
                .pretrain(true).backprop(true).build();


        model = new MultiLayerNetwork(conf); // Passing built configuration to instance of multilayer network
        model.init(); // Initialize mode
        model.setListeners(new ScoreIterationListener(10));// Setting listeners
        return conf;
        // model.setListeners(new HistogramIterationListener(10));
    }

    // method for testing the classifier.
    @Override
    public ClassificationStatistics test(List<double[][]> epochs, List<Double> targets) {
        ClassificationStatistics resultsStats = new ClassificationStatistics(); // initialization of classifier statistics
        for (int i = 0; i < epochs.size(); i++) {   //iterating epochs
            double output = this.classify(epochs.get(i));   //   output means score of a classifier from method classify
            resultsStats.add(output, targets.get(i));   // calculating statistics
        }
        return resultsStats;    //  returns classifier statistics
    }

    // method not implemented. For loading use load(String file)
    @Override
    public void load(InputStream is) {

    }

    // method not implemented. For saving use method save(String file)
    @Override
    public void save(OutputStream dest) {

    }

    @Override
    public void save(String file) {
        OutputStream fos;
        // Choose the name of classifier and coefficient file to save based on the feature extraction, which is used
        String coefficientsName = "wrong.bin";
        if (fe.getClass().getSimpleName().equals("FilterAndSubsamplingFeatureExtraction")) {
            coefficientsName = "coefficients19.bin";
        } else if (fe.getClass().getSimpleName().equals("WaveletTransformFeatureExtraction")) {
            coefficientsName = "coefficients20.bin";
        } else if (fe.getClass().getSimpleName().equals("MatchingPursuitFeatureExtraction")) {
            coefficientsName = "coefficients21.bin";
        }
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

    @Override
    public void load(String file) {
        MultiLayerConfiguration confFromJson = null;
        INDArray newParams = null;
        // Choose the name of coefficient file to load based on the feature extraction, which is used
        String coefficientsName = "wrong.bin";
        if (fe.getClass().getSimpleName().equals("FilterAndSubsamplingFeatureExtraction")) {
            coefficientsName = "coefficients19.bin";
        } else if (fe.getClass().getSimpleName().equals("WaveletTransformFeatureExtraction")) {
            coefficientsName = "coefficients20.bin";
        } else if (fe.getClass().getSimpleName().equals("MatchingPursuitFeatureExtraction")) {
            coefficientsName = "coefficients21.bin";
        }
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
        if (confFromJson != null) {
            model = new MultiLayerNetwork(confFromJson);
        }
        model.init();
        model.setParams(newParams);
        System.out.println("Original network params " + model.params());
        System.out.println("Loaded");
    }

    @Override
    public IFeatureExtraction getFeatureExtraction() {
        return fe;
    }

    @Override
    public void setFeatureExtraction(IFeatureExtraction fe) {
        this.fe = fe;
    }
}
