package cz.zcu.kiv.eeg.basil.data.processing.classification;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.AutoEncoder;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.optimize.listeners.*;
import org.jfree.data.xml.DatasetReader;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator;
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher;
import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.FeatureVector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukasvareka on 27. 6. 2016.
 */
public class SDADeepLearning4jClassifier extends DeepLearning4jClassifier {
    private final int NEURON_COUNT_DEFAULT = 30;    //default number of neurons
    protected final int neuronCount;                    // Number of neurons

    /*Default constructor*/
    public SDADeepLearning4jClassifier() {
        this.neuronCount = NEURON_COUNT_DEFAULT;
    }

    /*Parametric constructor */
    public SDADeepLearning4jClassifier(int neuronCount) {
        this.neuronCount = neuronCount; // sets count of neurons in layer(0) to param
    }

    /*Classifying features*/
    @Override
    public double classify(FeatureVector fv) {
        double[][] featureVector = fv.getFeatureMatrix(); // Extracting features to vector
        INDArray features = Nd4j.create(featureVector); // Creating INDArray with extracted features
        return model.output(features, Layer.TrainingMode.TEST).getDouble(0); // Result of classifying
    }

    @Override
    public void train(List<FeatureVector> featureVectors, int numberOfiter) {

        // Customizing params of classifier
        final int numRows = featureVectors.get(0).size();   // number of targets on a line
        final int numColumns = 2;   // number of labels needed for classifying
        //this.iterations = numberOfiter; // number of iteration in the learning phase
        int listenerFreq = numberOfiter / 500; // frequency of output strings
        int seed = 123; //  seed - one of parameters. For more info check http://deeplearning4j.org/iris-flower-dataset-tutorial

        DataSet dataSet2 = createDataSet(featureVectors);

/*        final List<DataSet> lst = new ArrayList<>(featureVectors.size());
        for (int i = 0; i < featureVectors.size(); i++) { // Iterating through epochs
            FeatureVector fv = featureVectors.get(i); // Feature of each epoch
            DataSet d;
            INDArray matrix = Nd4j.create(fv.getFeatureMatrix());
            double[] l = {fv.getExpectedOutput(),Math.abs(1 - fv.getExpectedOutput())};
            INDArray label = Nd4j.create(l);
            d = new DataSet(matrix, label);
            lst.add(d);
        }*/

/*        BaseDataFetcher fetcher = new BaseDataFetcher() {
            @Override
            public void fetch(int numExamples) {
                totalExamples = lst.size();

                int from = this.cursor;
                int to = this.cursor + numExamples;
                if (to > this.totalExamples) {
                    to = this.totalExamples;
                }

                initializeCurrFromList(lst.subList(from, to));
                this.cursor += numExamples;
            }
        };



        BaseDatasetIterator it = new BaseDatasetIterator(1, featureVectors.size(), fetcher);*/

        SplitTestAndTrain tat = dataSet2.splitTestAndTrain(0.8);
        Nd4j.ENFORCE_NUMERICAL_STABILITY = true; // Setting to enforce numerical stability

        // Building a neural net
        build(numRows, numColumns, seed, listenerFreq);

        System.out.println("Train model....");
        DataSet d = tat.getTrain();

        for(int i = 0; i  < 2000; i++) {
            model.fit(d); // Learning of neural net with training data
        }

        //model.finetune();

        Evaluation eval = new Evaluation(numColumns);
        DataSet tst = tat.getTest();
        eval.eval(tst.getLabels(), model.output(tst.getFeatureMatrix(), Layer.TrainingMode.TEST));
        System.out.println(eval.stats());
    }

    //  initialization of neural net with params. For more info check http://deeplearning4j.org/iris-flower-dataset-tutorial where is more about params
    private void build(int numRows, int outputNum, int seed, int listenerFreq) {
        System.out.print("Build model....SDA");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                //.seed(seed)
                //.iterations(1500)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(0.005,0.9))
                //.regularization(true).dropOut(0.99)
                // .regularization(true)
                //.l2(1e-4)
                .gradientNormalizationThreshold(1)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .list()
/*                .layer(0, new AutoEncoder.Builder()
                        .nIn(numRows)
                        .nOut(48)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        //.corruptionLevel(0.2) // Set level of corruption
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .build())*/
                .layer(0, new AutoEncoder.Builder().nOut(24).nIn(48)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        //.corruptionLevel(0.1) // Set level of corruption
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .build())
                .layer(1, new AutoEncoder.Builder().nOut(12).nIn(24)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        //.corruptionLevel(0.1) // Set level of corruption
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX)
                        .nOut(outputNum).nIn(12).build())
                .pretrain(false).backprop(true).build();
/*                .list()
                .layer(0, new AutoEncoder.Builder().nIn(numRows).nOut(24)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        //.corruptionLevel(0.2) // Set level of corruption
                        .lossFunction(LossFunctions.LossFunction.RMSE_XENT)
                        .build())
                .layer(1, new AutoEncoder.Builder().nIn(24).nOut(12)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        //.corruptionLevel(0.2) // Set level of corruption
                        .lossFunction(LossFunctions.LossFunction.RMSE_XENT)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX).weightInit(WeightInit.XAVIER)
                        .nIn(12).nOut(outputNum).build())
                .pretrain(true).backprop(true).build();*/
        model = new MultiLayerNetwork(conf); // Passing built configuration to instance of multilayer network
        //model.setIterationCount(1500);
        model.init(); // Initialize mode

        //UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later
        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
        //uiServer.attach(statsStorage);

        ArrayList listenery = new ArrayList();
        listenery.add(new ScoreIterationListener(500));
        listenery.add(new StatsListener(statsStorage));
        model.setListeners(listenery);
        //model.setListeners(new ScoreIterationListener(listenerFreq));// Setting listeners
        //model.setListeners(new HistogramIterationListener(10));
    }
}
