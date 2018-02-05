package cz.zcu.kiv.eeg.basil.data.processing.classification;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingModelSaver;
import org.deeplearning4j.earlystopping.saver.InMemoryModelSaver;
import org.deeplearning4j.earlystopping.termination.EpochTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.ScoreImprovementEpochTerminationCondition;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.FeatureVector;
import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.IFeatureExtraction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by lukasvareka on 4. 7. 2016.
 */
public class MLPDeepLearning4jEarlyStop extends DeepLearning4jClassifier {
    private final int NEURON_COUNT_DEFAULT = 30;    //default number of neurons
    private int neuronCount;                    // Number of neurons
    private int maxTime =5; //max time in minutes
    private int maxEpochs = 1500;
    private EarlyStoppingResult result;
    private int noImprovementEpochs = 30;
    private EarlyStoppingConfiguration esConf;
    private String pathname = "C:\\Temp\\MLPEStop"; //pathname+file name for saving model
    private String directory = "C:\\Temp\\";

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
    public double classify(FeatureVector fv) {
        double[] featureVector = fv.getFeatureVector(); // Extracting features to vector
        INDArray features = Nd4j.create(featureVector); // Creating INDArray with extracted features
        return model.output(features, Layer.TrainingMode.TEST).getDouble(0); // Result of classifying

    }

    @Override
    public void train(List<FeatureVector> featureVectors, List<Double> targets, int numberOfiter) {

        // Customizing params of classifier
        final int numRows = featureVectors.get(0).getFeatureVector().length;   // number of targets on a line
        final int numColumns = 2;   // number of labels needed for classifying
        this.iterations = numberOfiter; // number of iteration in the learning phase
        int listenerFreq = numberOfiter / 10; // frequency of output strings
        int seed = 123; //  seed - one of parameters. For more info check http://deeplearning4j.org/iris-flower-dataset-tutorial

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

        // Creating INDArrays and DataSet
        INDArray output_data = Nd4j.create(labels); // Create INDArray with labels(targets)
        INDArray input_data = Nd4j.create(features_matrix); // Create INDArray with features(data)
        DataSet dataSet = new DataSet(input_data, output_data); // Create dataSet with features and labels
        //SplitTestAndTrain tat = dataSet.splitTestAndTrain(80);

        /*

        NOT WORKING WITH NEW LIBRARY

        DataSetIterator dataSetTrainIterator = new ListDataSetIterator(tat.getTrain().batchBy(8));
        DataSetIterator dataSetTestIterator = new ListDataSetIterator(tat.getTest().batchBy(8));

        */
        Nd4j.ENFORCE_NUMERICAL_STABILITY = true; // Setting to enforce numerical stability

        // Building a neural net
        MultiLayerConfiguration conf = build(numRows, numColumns, seed, listenerFreq);
        SplitTestAndTrain testAndTrain = dataSet.splitTestAndTrain(80);

        //EarlyStoppingModelSaver saver = new LocalFileModelSaver(directory);
        EarlyStoppingModelSaver saver = new InMemoryModelSaver();

        List<EpochTerminationCondition> list = new ArrayList<EpochTerminationCondition>(2);
        list.add(new MaxEpochsTerminationCondition(maxEpochs));
        list.add(new ScoreImprovementEpochTerminationCondition(noImprovementEpochs));

        esConf = new EarlyStoppingConfiguration.Builder()
                //.epochTerminationConditions(new MaxEpochsTerminationCondition(maxEpochs))
                //.epochTerminationConditions(new ScoreImprovementEpochTerminationCondition(noImprovementEpochs))
                .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(maxTime, TimeUnit.MINUTES))
                //.scoreCalculator(new DataSetLossCalculator(new ListDataSetIterator(testAndTrain.getTest().asList(), 100), true))
                .scoreCalculator(new DataSetLossCalculator(new ListDataSetIterator(testAndTrain.getTrain().asList(), 100), true))
                .evaluateEveryNEpochs(3)
                .modelSaver(saver)
                .epochTerminationConditions(list)
                .build();

        EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConf,conf,new ListDataSetIterator(testAndTrain.getTrain().asList(), 100));
//Conduct early stopping training:
        this.result = trainer.fit();

//Print out the results:
        System.out.println("Termination reason: " + result.getTerminationReason());
        System.out.println("Termination details: " + result.getTerminationDetails());
        System.out.println("Total epochs: " + result.getTotalEpochs());
        System.out.println("Best epoch number: " + result.getBestModelEpoch());
        System.out.println("Score at best epoch: " + result.getBestModelScore());

//Get the best model
        this.model = (MultiLayerNetwork) result.getBestModel();

        Evaluation eval = new Evaluation(numColumns);
        eval.eval(testAndTrain.getTest().getLabels(), model.output(testAndTrain.getTest().getFeatureMatrix(), Layer.TrainingMode.TEST));
        System.out.println(eval.stats());
    }

    //  initialization of neural net with params. For more info check http://deeplearning4j.org/iris-flower-dataset-tutorial where is more about params
    private MultiLayerConfiguration build(int numRows, int outputNum, int seed, int listenerFreq) {
        System.out.print("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(10)
                //.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(0.0005)
               // .updater(Updater.NESTEROVS).momentum(0.9)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numRows).nOut(400)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        //.corruptionLevel(0.2) // Set level of corruption
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(400).nOut(200)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        //.corruptionLevel(0.2) // Set level of corruption
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .weightInit(WeightInit.RELU)
                        .activation(Activation.RELU)
                        .nIn(200).nOut(outputNum).build())
                .pretrain(false).backprop(true).build();


        //model.setListeners(new ScoreIterationListener(10));// Setting listeners
        //model.setListeners(new HistogramIterationListener(10));
                return conf;

    }
}
