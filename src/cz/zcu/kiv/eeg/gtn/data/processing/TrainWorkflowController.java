package cz.zcu.kiv.eeg.gtn.data.processing;

import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.ITrainCondition;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.FeatureVector;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.AbstractDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;

import java.util.*;

/**
 * Created by Tomas Prokop on 15.08.2017.
 */
public class TrainWorkflowController extends AbstractWorkflowController {

    private int minMarkers = 5;

    private boolean finished = false;

    private final ITrainCondition trainCondition;

    private int numOfIterations = 1000;
    
    private final String TARGET_MARKER = "S  2"; // TODO: place somewhere else - this is just for testing

    public TrainWorkflowController(AbstractDataProvider dataProvider, IBuffer buffer, AbstractDataPreprocessor preprocessor,
                                   List<IFeatureExtraction> featureExtractions, IClassifier classifier, ITrainCondition trainCondition) {
        super(dataProvider, buffer, preprocessor, featureExtractions, classifier);
        this.trainCondition = trainCondition;
    }

    @Override
    public void processData() {
        if (buffer.isFull() || buffer.getMarkersSize() > minMarkers || finished) {
            List<EEGDataPackage> dataPackages = preprocessor.preprocessData();
            if (dataPackages == null || dataPackages.size() == 0) return;
            for (EEGDataPackage dataPackage : dataPackages) {
            	dataPackage.setTargetMarker(TARGET_MARKER); // TODO: place somewhere else
            	
                String marker;
                if(dataPackage.getMarkers() == null || dataPackage.getMarkers().get(0) == null)
                    continue;
                marker = dataPackage.getMarkers().get(0).getName();
                
                FeatureVector fv = new FeatureVector();
                for (IFeatureExtraction fe : featureExtractions) {
                    double[] features = fe.extractFeatures(dataPackage);
                    fv.addFeatures(features);
                }
                trainCondition.addSample(fv, dataPackage.getTargetMarker(), marker);
            }
        }
    }

    @Override
    public void start(EEGStartMessage start) {
        finished = false;
    }

    @Override
    public void stop(EEGStopMessage stop) {
        finished = true;
        processData();

        classifier.train(trainCondition.getFeatureVectors(), trainCondition.getExpectedClasses(), numOfIterations);
        super.removeDataListener();
    }

    @Override
    public void storeData(EEGDataMessage data) {
        buffer.add(data.getData(), Arrays.asList(data.getMarkers()));
    }


    public int getMinMarkers() {
        return minMarkers;
    }

    public void setMinMarkers(int minMarkers) {
        this.minMarkers = minMarkers;
    }

    public int getNumOfIterations() {
        return numOfIterations;
    }

    public void setNumOfIterations(int numOfIterations) {
        this.numOfIterations = numOfIterations;
    }

    public ITrainCondition getTrainCondition() {
        return trainCondition;
    }
}
