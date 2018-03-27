package cz.zcu.kiv.eeg.basil.data.processing.classification;

import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.FeatureVector;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomas Prokop on 12.03.2018.
 */
public class CNNDeepLearning4jClassifier extends DeepLearning4jClassifier {

    @Override
    public void train(List<FeatureVector> featureVectors, int numberOfiter){

        // Customizing params of classifier
        final int numRows = featureVectors.get(0).size();   // number of targets on a line
        final int numColumns = 2;   // number of labels needed for classifying
        //this.iterations = numberOfiter; // number of iteration in the learning phase
        int listenerFreq = numberOfiter / 500; // frequency of output strings
        int seed = 123; //  seed - one of parameters. For more info check http://deeplearning4j.org/iris-flower-dataset-tutorial

        DataSet dataSet = createDataSet(featureVectors);

        SplitTestAndTrain tat = dataSet.splitTestAndTrain(0.8);
        Nd4j.ENFORCE_NUMERICAL_STABILITY = true; // Setting to enforce numerical stability

        // Building a neural net
        build(numRows, numColumns, seed, listenerFreq);

        System.out.println("Train model....");
        model.fit(tat.getTrain()); // Learning of neural net with training data
        model.finetune();

        Evaluation eval = new Evaluation(numColumns);
        eval.eval(dataSet.getLabels(), model.output(dataSet.getFeatureMatrix(), Layer.TrainingMode.TEST));
        System.out.println(eval.stats());
    }

    private ConvolutionLayer convInit(String name, int in, int out, int[] kernel, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
    }

    private ConvolutionLayer conv5x5(String name, int out, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(new int[]{5,5}, stride, pad).name(name).nOut(out).biasInit(bias).build();
    }

    private SubsamplingLayer maxPool(String name, int[] kernel) {
        return new SubsamplingLayer.Builder(kernel, new int[]{2,2}).name(name).build();
    }

    private void build(int numRows, int outputNum, int seed, int listenerFreq) {
        System.out.print("Build model....SDA");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .regularization(false).l2(0.005)
                .activation(Activation.RELU)
                .learningRate(0.0001)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .list()
                .layer(0, convInit("cnn1", numRows, 50 ,  new int[]{5, 5}, new int[]{1, 1}, new int[]{0, 0}, 0))
                .layer(1, maxPool("maxpool1", new int[]{2,2}))
                .layer(2, conv5x5("cnn2", 100, new int[]{5, 5}, new int[]{1, 1}, 0))
                .layer(3, maxPool("maxool2", new int[]{2,2}))
                .layer(4, new DenseLayer.Builder().nOut(500).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(2)
                        .activation(Activation.SOFTMAX)
                        .build())
                .backprop(true).pretrain(false)
                .build();
        //MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
/*                //.seed(seed)
                .iterations(1500)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(0.005)
                .updater(Updater.NESTEROVS).momentum(0.9)
                //.regularization(true).dropOut(0.99)
                // .regularization(true).l2(1e-4)
                .list()
                .layer(1, new ConvolutionLayer.Builder(new int []{4, 4}, new int[]{1,1})
                        .nIn(numRows).nOut(20)

                .pretrain(false).backprop(true).build();*/
        //model = new MultiLayerNetwork(conf); // Passing built configuration to instance of multilayer network
        //model.init(); // Initialize mode


        //UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later

        ArrayList listenery = new ArrayList();
        listenery.add(new ScoreIterationListener(500));
        listenery.add(new StatsListener(statsStorage));
        model.setListeners(listenery);
    }

    @Override
    public double classify(FeatureVector fv) {
        double[][] featureVector = fv.getFeatureMatrix(); // Extracting features to vector
        INDArray features = Nd4j.create(featureVector); // Creating INDArray with extracted features
        return model.output(features, Layer.TrainingMode.TEST).getDouble(0); // Result of classifying
    }
}
