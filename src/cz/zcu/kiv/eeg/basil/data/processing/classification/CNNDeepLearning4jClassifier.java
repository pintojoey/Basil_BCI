package cz.zcu.kiv.eeg.basil.data.processing.classification;

import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.FeatureVector;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomas Prokop on 12.03.2018.
 */
public class CNNDeepLearning4jClassifier extends DeepLearning4jClassifier {

    ComputationGraph graphModel;

    @Override
    public void train(List<FeatureVector> featureVectors, int numberOfiter){

        // Customizing params of classifier
        final int numRows = featureVectors.get(0).size();   // number of targets on a line
        final int numColumns = 2;   // number of labels needed for classifying
        //this.iterations = numberOfiter; // number of iteration in the learning phase
        int listenerFreq = numberOfiter / 500; // frequency of output strings
        int seed = 123; //  seed - one of parameters. For more info check http://deeplearning4j.org/iris-flower-dataset-tutorial

        //List<DataSet> dataSet = createDataSet3(featureVectors);
        DataSet dataSet = createDataSet3(featureVectors);

        SplitTestAndTrain tat = dataSet.splitTestAndTrain(0.8);
        Nd4j.ENFORCE_NUMERICAL_STABILITY = true; // Setting to enforce numerical stability
        // Building a neural net
        build(numRows, numColumns, seed, listenerFreq);
        DataSet trDs = tat.getTrain();
        int[] shape = {trDs.numExamples(), 1, 3, 512 };
        trDs.setFeatures(trDs.getFeatures().reshape(shape));
        System.out.println("Train model....");

        shape = trDs.getFeatures().shape();
        //List<DataSet> ds = dataSet.asList();
        for(int i = 0; i  < 2000; i++) {
            //for (DataSet d : dataSet) {
              //  model.fit(d);
            //}


            graphModel.fit(trDs); // Learning of neural net with training data
        }
        //model.fit(); // Learning of neural net with training data
        //model.finetune();

        Evaluation eval = new Evaluation(numColumns);
        //eval.eval(dataSet.getLabels(), model.output(dataSet.getFeatureMatrix(), Layer.TrainingMode.TEST));
        System.out.println(eval.stats());
    }

    private ConvolutionLayer convInit_1d(String name, int in, int out, int kernel, int stride, int pad, double bias) {
        return new Convolution1D.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
    }

    private ConvolutionLayer conv5_1d(String name, int in, int out, int stride, int pad, double bias) {
        return new Convolution1D.Builder(3, stride, pad).name(name).nOut(out).biasInit(bias).nIn(in).build();
    }

    private SubsamplingLayer maxPool_1d(String name, int kernel) {
        return new Subsampling1DLayer.Builder(kernel, 2).name(name).build();
    }

    private ConvolutionLayer convInit(String name, int in, int out, int kernel, int stride, int pad, double bias) {
        return new Convolution1D.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
    }

    private ConvolutionLayer conv5x5(String name, int in, int out, int stride, int pad, double bias) {
        return new Convolution1D.Builder(5, stride, pad).name(name).nOut(out).biasInit(bias).nIn(in).build();
    }

    private SubsamplingLayer maxPool(String name, int kernel) {
        return new Subsampling1DLayer.Builder(kernel, 2).name(name).build();
    }

    private void build(int numRows, int outputNum, int seed, int listenerFreq) {
        System.out.print("Build model....CNN");
/*        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                //.iterations(iterations)
                ///.regularization(false).l2(0.005)
                .activation(Activation.RELU)
                //.learningRate(0.0001)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(0.0001, 0.9))
                .list()
                .layer(0, convInit_1d("cnn1", 1, 50 ,  3, 1, 0, 0))
                .layer(1, maxPool_1d("maxpool1", 1))
                .layer(2, conv5_1d("cnn2", 50, 100, 5, 1, 0))
                .layer(3, maxPool_1d("maxool2", 1))
                .layer(4, new DenseLayer.Builder().nOut(500).nIn(100).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(2)
                        .nIn(500)
                        .activation(Activation.SOFTMAX)
                        .build())
                .backprop(true).pretrain(false)
                .build();*/

        int[] kernel = new int[]{3,3};
        int[] stride = new int[]{3,3};

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(0.0001, 0.9))
                //.regularization(true).dropOut(0.99)
                // .regularization(true).l2(1e-4)
                .list()
                .layer(0, new ConvolutionLayer.Builder(kernel,stride).nIn(1).nOut(10).activation(Activation.IDENTITY).build())
                .layer(1, new DenseLayer.Builder().nIn(5).nOut(5)
                        .activation(Activation.RELU).build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(5).nOut(2).build())
                .pretrain(false).backprop(true).build();

        //model = new MultiLayerNetwork(conf); // Passing built configuration to instance of multilayer network
        //model.init(); // Initialize mode

        graphModel = createModel();

        //UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later

        ArrayList listeners = new ArrayList();
        listeners.add(new ScoreIterationListener(500));
        listeners.add(new StatsListener(statsStorage));
        graphModel.setListeners(listeners);
    }

    public static ComputationGraph createModel() {
        int[] kernel = new int[]{3,3};
        int[] stride = new int[]{3,3};
        ComputationGraphConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(0.0001, 0.9))
                .graphBuilder()
                .addInputs("trainFeatures")
                .setOutputs("out1")
                .setInputTypes(InputType.convolutional(3, 512, 1))
                .addLayer("cnn1",  new ConvolutionLayer.Builder(kernel,stride).nIn(1).nOut(10).activation(Activation.IDENTITY).build(),"trainFeatures")
                .addLayer("dense1", new DenseLayer.Builder().nOut(5)
                        .activation(Activation.RELU).build(),"cnn1")
                .addLayer("out1", new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(5).nOut(2).build(),"dense1")
                .pretrain(false).backprop(true)
                .build();

        // Construct and initialize model
        ComputationGraph model = new ComputationGraph(config);
        model.init();

        return model;
    }

    @Override
    public double classify(FeatureVector fv) {
        double[][] featureVector = fv.getFeatureMatrix(); // Extracting features to vector
        INDArray features = Nd4j.create(featureVector); // Creating INDArray with extracted features
        int[] shape = {1, 1, 3, 512 };
        features = features.reshape(shape);
        return graphModel.outputSingle(features).getDouble(0); // Result of classifying
    }
}
