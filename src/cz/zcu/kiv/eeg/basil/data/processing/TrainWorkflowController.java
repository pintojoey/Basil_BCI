package cz.zcu.kiv.eeg.basil.data.processing;

import java.util.*;

import cz.zcu.kiv.eeg.basil.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.basil.data.processing.classification.ITrainCondition;
import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.FeatureVector;
import cz.zcu.kiv.eeg.basil.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.basil.data.processing.preprocessing.AbstractDataPreprocessor;
import cz.zcu.kiv.eeg.basil.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.basil.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.basil.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.basil.data.providers.messaging.EEGStopMessage;

/**
 * Controller used to train classifiers
 *
 * Created by Tomas Prokop on 15.08.2017.
 */
public class TrainWorkflowController extends AbstractWorkflowController {

    private int minMarkers = 5;

    /**
     * Finished data processing => start training
     */
    private boolean finished = false;

    /**
     * Implementation of train condition used to manage training process - e.g. selection of same number of target and
     * nontarget feature vectors.
     */
    private final ITrainCondition trainCondition;

    /**
     * Number of training iterations
     */
    private int numOfIterations = 1000;

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
            	
                String marker;
                if(dataPackage.getMarkers() == null || dataPackage.getMarkers().get(0) == null)
                    continue;
                marker = dataPackage.getMarkers().get(0).getName();
                
                FeatureVector fv = new FeatureVector();
                for (IFeatureExtraction fe : featureExtractions) {
                    FeatureVector features = fe.extractFeatures(dataPackage);
                    fv.addFeatures(features);
                }

                trainCondition.addSample(fv, dataPackage.getMetadata().getTargetMarker(), marker);
            }
        }
    }

    @Override
    public void start(EEGStartMessage start) {
        finished = false;
    }

    @Override
    public void stop(EEGStopMessage stop) {
        processData();
        buffer.clear();
    }

    @Override
    public void storeData(EEGDataMessage data) {
        buffer.add(data.getData(), Arrays.asList(data.getMarkers()));
    }

    @Override
    protected void onDataReadEnd() {
        super.onDataReadEnd();
        finished = true;
        classifier.train(trainCondition.getFeatureVectors(), numOfIterations);
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
