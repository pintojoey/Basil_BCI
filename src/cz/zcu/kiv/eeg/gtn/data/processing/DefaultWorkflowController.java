package cz.zcu.kiv.eeg.gtn.data.processing;

import java.util.Arrays;
import java.util.List;

import cz.zcu.kiv.eeg.gtn.data.listeners.EEGDataProcessingListener;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.FeatureVector;
import cz.zcu.kiv.eeg.gtn.data.processing.classification.IClassifier;
import cz.zcu.kiv.eeg.gtn.data.processing.featureExtraction.IFeatureExtraction;
import cz.zcu.kiv.eeg.gtn.data.processing.preprocessing.AbstractDataPreprocessor;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.EEGDataPackage;
import cz.zcu.kiv.eeg.gtn.data.processing.structures.IBuffer;
import cz.zcu.kiv.eeg.gtn.data.providers.AbstractDataProvider;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGDataMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStartMessage;
import cz.zcu.kiv.eeg.gtn.data.providers.messaging.EEGStopMessage;

public class DefaultWorkflowController extends AbstractWorkflowController {

    private boolean finished = false;
    private int minMarkers = 5;

    public DefaultWorkflowController(AbstractDataProvider dataProvider, IBuffer buffer, AbstractDataPreprocessor preprocessor,
                                     List<IFeatureExtraction> featureExtractions, IClassifier classifier) {
        super(dataProvider, buffer, preprocessor, featureExtractions, classifier);
    }

    public DefaultWorkflowController(AbstractDataProvider dataProvider, IBuffer buffer, AbstractDataPreprocessor preprocessor,
                                     List<IFeatureExtraction> featureExtractions, IClassifier classifier, int minMarkers) {
        super(dataProvider, buffer, preprocessor, featureExtractions, classifier);
        this.minMarkers = minMarkers;
    }

    @Override
    public void processData() {
        if (finished || buffer.isFull() || buffer.getMarkersSize() > minMarkers) {
            List<EEGDataPackage> packs = preprocessor.preprocessData();

            if (packs == null || packs.size() == 0) return;

            for(EEGDataProcessingListener ls : listeners){
                ls.dataPreprocessed(packs);
            }

            FeatureVector fv;
            for (EEGDataPackage pack : packs) {
                fv = new FeatureVector();
                for (IFeatureExtraction fe : featureExtractions) {
                    double[] features = fe.extractFeatures(pack);
                    fv.addFeatures(features);
                }

                if(fv.size() > 0 && classifier != null){
                    pack.addFeatureVector(fv);
                    pack.setFeatureExtractions(featureExtractions);
                    fv.normalize();

                    for(EEGDataProcessingListener ls : listeners){
                        ls.featuresExtracted(pack);
                    }

                    double res = classifier.classify(fv);
                    pack.setClassificationResult(res);

                    for(EEGDataProcessingListener ls : listeners){
                        ls.dataClassified(pack);
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
}
