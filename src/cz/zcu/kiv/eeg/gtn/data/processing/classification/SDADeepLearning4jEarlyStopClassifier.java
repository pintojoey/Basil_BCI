package cz.zcu.kiv.eeg.gtn.data.processing.classification;

import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.FeatureVector;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.EpochTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.termination.ScoreImprovementEpochTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.AutoEncoder;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.deeplearning4j.earlystopping.saver.*;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// creates instance of Stacked Denoising Autoencoder @author Pumprdlici group
public class SDADeepLearning4jEarlyStopClassifier extends DeepLearning4jClassifier {
    private final int NEURON_COUNT_DEFAULT = 30;    //default number of neurons
    private int neuronCount;                    // Number of neurons
    private String directory = "C:\\Temp\\";
    private int maxTime =5; //max time in minutes
    private int maxEpochs = 10000;
    private EarlyStoppingResult result;
    private int noImprovementEpochs = 20;
    private EarlyStoppingConfiguration esConf;
    private String pathname = "C:\\Temp\\SDAEStop"; //pathname+file name for saving model


    /*Default constructor*/
    public SDADeepLearning4jEarlyStopClassifier() {
        this.neuronCount = NEURON_COUNT_DEFAULT; // sets count of neurons in layer(0) to default number
    }

    /*Parametric constructor */
    public SDADeepLearning4jEarlyStopClassifier(int neuronCount) {
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
        Nd4j.ENFORCE_NUMERICAL_STABILITY = true; // Setting to enforce numerical stability

        // Building a neural net
        MultiLayerConfiguration conf = this.createConfiguration(numRows, numColumns, seed, listenerFreq);
        //model = new MultiLayerNetwork(conf); // Passing built configuration to instance of multilayer network
       // model.init(); // Initialize model
        //model.setListeners(Collections.singletonList((IterationListener) new ScoreIterationListener(listenerFreq))); // Setting listeners
        //model.setListeners(new ScoreIterationListener(100));

        SplitTestAndTrain testAndTrain = dataSet.splitTestAndTrain(0.35);
        //EarlyStoppingModelSaver saver = new LocalFileModelSaver(directory);
        InMemoryModelSaver <MultiLayerNetwork> saver = new InMemoryModelSaver();


        List<EpochTerminationCondition> list = new ArrayList<>(2);
        list.add(new MaxEpochsTerminationCondition(maxEpochs));
        list.add(new ScoreImprovementEpochTerminationCondition(noImprovementEpochs, 0.00001));
        //list.add(new ScoreImprovementEpochTerminationCondition(noImprovementEpochs));

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        //DataSetIterator testData= new TestDataSetIterator(dataSet);
        //DataSetIterator trainData= new TestDataSetIterator(dataSet);


                esConf = new EarlyStoppingConfiguration.Builder()
                //.epochTerminationConditions(new MaxEpochsTerminationCondition(maxEpochs))
                .iterationTerminationConditions(new MaxTimeIterationTerminationCondition(maxTime, TimeUnit.MINUTES))
                //.epochTerminationConditions(new ScoreImprovementEpochTerminationCondition(noImprovementEpochs))
                //
                //.scoreCalculator(new DataSetLossCalculator(new ListDataSetIterator(dataSet.asList()),true))
                .scoreCalculator(new DataSetLossCalculator(new ListDataSetIterator(testAndTrain.getTest().asList(), 100), true))
                .evaluateEveryNEpochs(3)
                .modelSaver(saver)
                .epochTerminationConditions(list)
                .build();

        //create Estop trainer
        EarlyStoppingTrainer trainer = new EarlyStoppingTrainer(esConf, net, new ListDataSetIterator(testAndTrain.getTrain().asList(), 100));
        //prepare UI
        UIServer uiServer = UIServer.getInstance();
        //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later
        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
        uiServer.attach(statsStorage);

        //Then add the StatsListener to collect this information from the network, as it trains
        ArrayList listeners = new ArrayList();
        listeners.add(new ScoreIterationListener(100));
        listeners.add(new StatsListener(statsStorage));
        net.setListeners(listeners);
        result = trainer.fit();

        model = (MultiLayerNetwork) result.getBestModel();

        System.out.println("Termination reason: " + result.getTerminationReason());
        System.out.println("Termination details: " + result.getTerminationDetails());
        System.out.println("Best epoch number: " + result.getBestModelEpoch());
        System.out.println("Score at best epoch: " + result.getBestModelScore());

        Evaluation eval = new Evaluation(numColumns);
        eval.eval(dataSet.getLabels(), model.output(dataSet.getFeatureMatrix(), Layer.TrainingMode.TEST));
        System.out.println(eval.stats());
    }

    //  initialization of neural net with params. For more info check http://deeplearning4j.org/iris-flower-dataset-tutorial where is more about params
    private MultiLayerConfiguration createConfiguration(int numRows, int outputNum, int seed, int listenerFreq) {
        System.out.print("Build model....SDA EStop");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder() // Starting builder pattern
                .seed(seed) // Locks in weight initialization for tuning
                //.weightInit(WeightInit.XAVIER)
                //.activation(Activation.LEAKYRELU)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(0.05)
                .iterations(1)
                //.momentum(0.5) // Momentum rate
                //.momentumAfter(Collections.singletonMap(3, 0.9)) //Map of the iteration to the momentum rate to apply at that iteration
                .list() // # NN layers (doesn't count input layer)
                .layer(0, new AutoEncoder.Builder()
                        .nIn(numRows)
                        .nOut(96)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        //.corruptionLevel(0.2) // Set level of corruption
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .build())
                .layer(1, new AutoEncoder.Builder().nOut(48).nIn(96)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        //.corruptionLevel(0.1) // Set level of corruption
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .build())
                .layer(2, new AutoEncoder.Builder().nOut(12).nIn(48)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        //.corruptionLevel(0.1) // Set level of corruption
                        .lossFunction(LossFunction.MCXENT)
                        .build())
                .layer(3, new OutputLayer.Builder(LossFunction.MCXENT)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX)
                        .nOut(outputNum).nIn(12).build())
                .pretrain(false) // Do pre training
                .backprop(true)
                .build(); // Build on set configuration
        return conf;
    }
}
