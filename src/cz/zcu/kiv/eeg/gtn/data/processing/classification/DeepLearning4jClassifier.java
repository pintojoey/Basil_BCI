package cz.zcu.kiv.eeg.gtn.data.processing.classification;

import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.FeatureVector;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by Tomas Prokop on 15.01.2018.
 */
public abstract class DeepLearning4jClassifier implements IClassifier {

    protected MultiLayerNetwork model;            //multi layer neural network with a logistic output layer and multiple hidden neuralNets
    protected int iterations;                    //Iterations used to classify

    /*Parametric constructor */
    public DeepLearning4jClassifier() {
    }

    // method for testing the classifier.
    @Override
    public ClassificationStatistics test(List<FeatureVector> featureVectors, List<Double> targets) {
        ClassificationStatistics resultsStats = new ClassificationStatistics(); // initialization of classifier statistics
        for (int i = 0; i < featureVectors.size(); i++) {   //iterating epochs
            double output = this.classify(featureVectors.get(i));   //   output means score of a classifier from method classify
            resultsStats.add(output, targets.get(i));   // calculating statistics
        }
        return resultsStats;    //  returns classifier statistics
    }

    // method not implemented. For loading use load(String file)
    @Override
    public void load(InputStream is) {
        throw new NotImplementedException();
    }

    // method not implemented. For saving use method save(String file)
    @Override
    public void save(OutputStream dest) {
        throw new NotImplementedException();
    }

    /**
     * Save Model to zip file
     * using save methods from library deeplearning4j
     *
     * @param pathname path name and file name. File name should end with .zip extension.
     */
    public void save(String pathname) {
        File locationToSave = new File(pathname);      //Where to save the network. Note: the file is in .zip format - can be opened externally
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
     * Loads Model from file.
     * It uses load methods from library deepalerning4j
     *
     * @param pathname pathname and file name of loaded Model
     */
    public void load(String pathname) {
        File locationToLoad = new File(pathname);
        try {
            model = ModelSerializer.restoreMultiLayerNetwork(locationToLoad);
            System.out.println("Loaded");
            System.out.println("Loaded network params " + model.params());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
