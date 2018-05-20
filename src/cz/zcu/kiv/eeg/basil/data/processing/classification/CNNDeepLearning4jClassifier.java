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
import org.deeplearning4j.ui.api.UIServer;
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

    //ComputationGraph graphModel;

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


            model.fit(trDs); // Learning of neural net with training data
        }
        //model.fit(); // Learning of neural net with training data
        //model.finetune();

        Evaluation eval = new Evaluation(numColumns);
        //eval.eval(dataSet.getLabels(), graphModel.output(false, dataSet.getFeatureMatrix()));
        System.out.println(eval.stats());
    }

    private void build(int numRows, int outputNum, int seed, int listenerFreq) {
        System.out.print("Build model....CNN");
        int[] kernel = new int[]{3,20};
        int[] stride = new int[]{3,10};

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .activation(Activation.LEAKYRELU)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(0.0005, 0.9))
                .list()
                .layer(0, new ConvolutionLayer.Builder(kernel,stride).nIn(1).nOut(35).activation(Activation.LEAKYRELU)
                        .weightInit(WeightInit.XAVIER).build())
                .layer(1, new SubsamplingLayer.Builder(PoolingType.MAX)
                        .kernelSize(1,10)
                        .stride(1,5)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(kernel,stride).activation(Activation.RELU).nOut(50)
                .weightInit(WeightInit.XAVIER).kernelSize(1,5).stride(1,3).build())
                .layer(3, new DenseLayer.Builder().nOut(25).weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU).build())
                .layer(4, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nIn(25).nOut(2).build())
                .setInputType(InputType.convolutional(3, 512, 1))
                .backprop(true).pretrain(false)
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        //graphModel = createModel();

        //Initialize the user interface backend
        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later
        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
        uiServer.attach(statsStorage);

        ArrayList listeners = new ArrayList();
        listeners.add(new ScoreIterationListener(500));
        listeners.add(new StatsListener(statsStorage));

        model.setListeners(listeners);
    }

    public static ComputationGraph createModel() {
        int[] kernel = new int[]{3,50};
        int[] stride = new int[]{3,50};
        ComputationGraphConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(0.0001, 0.9))
                .graphBuilder()
                .addInputs("trainFeatures")
                .setOutputs("out1")
                .setInputTypes(InputType.convolutional(3, 512, 1))
                .addLayer("cnn1",  new ConvolutionLayer.Builder(kernel,stride).nIn(1).nOut(35).activation(Activation.IDENTITY)
                        .weightInit(WeightInit.XAVIER).build(),"trainFeatures")
                //.addLayer("pool", n)
                .addLayer("dense1", new DenseLayer.Builder().nOut(5).weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU).build(),"cnn1")
                .addLayer("out1", new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
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
        //return graphModel.outputSingle(features).getDouble(0); // Result of classifying
        return model.output(features, Layer.TrainingMode.TEST).getDouble(0);
    }
}
