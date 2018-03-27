package cz.zcu.kiv.eeg.basil.data.processing.classification;

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
        import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.FeatureVector;

        import java.util.List;

/**
 * Creates an instance of Deep Belief Network 
 * @author Pumprdlici group
 * 
 *
 */
public class DBNDeepLearning4jClassifier extends DeepLearning4jClassifier {
    
    private MultiLayerNetwork model;    //multi layer neural network with a logistic output layer and multiple hidden neuralNets
    private int iterations;             //Iterations used to classify
    
    /* Default constructor*/
    public DBNDeepLearning4jClassifier() {
    }


    @Override
    public double classify(FeatureVector fv){
        double[][] featureVector = fv.getFeatureMatrix(); // Extracting features to vector
        INDArray features = Nd4j.create(featureVector); // Creating INDArray with extracted features
        return model.output(features, Layer.TrainingMode.TEST).getDouble(0); // Result of classifying
    }

    @Override
    public void train(List<FeatureVector> featureVectors, int numberOfiter){
        this.iterations = numberOfiter;
        
        // Customizing params of classifier
        final int numRows = featureVectors.get(0).size();   // number of targets on a line
        final int numColumns = 2;   // number of labels needed for classifying
        int seed = 123; //  seed - one of parameters. For more info check http://deeplearning4j.org/iris-flower-dataset-tutorial
        int listenerFreq = this.iterations;  // frequency of output strings

        DataSet dataSet = createDataSet(featureVectors);
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
}
