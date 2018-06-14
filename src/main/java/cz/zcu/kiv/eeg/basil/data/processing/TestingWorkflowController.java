package cz.zcu.kiv.eeg.basil.data.processing;

import java.util.Arrays;
import java.util.List;

import cz.zcu.kiv.eeg.basil.data.listeners.EEGDataProcessingListener;
import cz.zcu.kiv.eeg.basil.data.processing.classification.IClassifier;
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
 * 
 * This workflow controller can be used
 * for example for preprocessing, feature extraction
 * and training of the classifiers 
 * 
 * @author lvareka
 *
 */
public class TestingWorkflowController extends AbstractWorkflowController {

    private boolean finished = false;
    private int MIN_MARKERS = 5; // TODO: ???

    public TestingWorkflowController(AbstractDataProvider dataProvider, IBuffer buffer, AbstractDataPreprocessor preprocessor,
                                     List<IFeatureExtraction> featureExtractions, IClassifier classifier) {
        super(dataProvider, buffer, preprocessor, featureExtractions, classifier);
    }

    public TestingWorkflowController(AbstractDataProvider dataProvider, IBuffer buffer, AbstractDataPreprocessor preprocessor,
                                     List<IFeatureExtraction> featureExtractions, IClassifier classifier, int minMarkers) {
        super(dataProvider, buffer, preprocessor, featureExtractions, classifier);
        
        this.MIN_MARKERS = minMarkers;
    }

    @Override
    public void processData() {
        if (finished || buffer.isFull() || buffer.getMarkersSize() > MIN_MARKERS) {
            List<EEGDataPackage> dataPackages = preprocessor.preprocessData();

            if (dataPackages == null || dataPackages.size() == 0) return;
            
            for (EEGDataProcessingListener ls : listeners) {
                ls.dataPreprocessed(dataPackages);
            }

            FeatureVector fv;
            for (EEGDataPackage dataPackage : dataPackages) {
                fv = new FeatureVector();
                if (featureExtractions != null) {
                	for (IFeatureExtraction fe : featureExtractions) {
                		FeatureVector features = fe.extractFeatures(dataPackage);
                		fv.addFeatures(features);
                	}
                }

                if (fv.size() > 0 && classifier != null){
                    dataPackage.addFeatureVector(fv);
                    dataPackage.setFeatureExtractions(featureExtractions);
                    
                    for (EEGDataProcessingListener ls : listeners){
                        ls.featuresExtracted(dataPackage);
                    }

                    double res = classifier.classify(fv);
                    dataPackage.setClassificationResult(res);

                    for (EEGDataProcessingListener ls : listeners) {
                        ls.dataClassified(dataPackage);
                    }
                }
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

        buffer.clear();
    }

    @Override
    public void storeData(EEGDataMessage data) {
        buffer.add(data.getData(), Arrays.asList(data.getMarkers()));
    }

    public int getMinMarkers() {
        return MIN_MARKERS;
    }

    public void setMinMarkers(int minMarkers) {
        this.MIN_MARKERS = minMarkers;
    }
}
